<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import {
  api,
  formatNumber,
  formatDuration,
  jobName,
  itemIconUrl,
  mapThumbUrl,
  mobIconUrl,
} from '../api';
import BottomNav from '../components/BottomNav.vue';
import PowerHelp from '../components/PowerHelp.vue';

const props = defineProps({
  character: { type: Object, required: true },
});
const emit = defineEmits(['back', 'open-info', 'character-change']);

const character = ref({ ...props.character });
const state = ref(null);
const regions = ref([]);
const activeRegion = ref('victoria');
const activeSubRegion = ref('');
const loadingMaps = ref(true);
const detail = ref(null);
const detailLoading = ref(false);
const sheetOpen = ref(false);
const picked = ref(new Set());
const discardPicked = ref(new Set());
const busy = ref('');
const toast = ref('');
const powerHelpOpen = ref(false);

// 当前拾取白名单管理
const wlOpen = ref(false);
const wlLoading = ref(false);
const wlSaving = ref(false);
const wlMapId = ref(0);
const wlMapName = ref('');
const wlItems = ref([]);
const wlAll = ref([]);
const wlEnabled = ref(new Set());
const wlCount = ref(0);
const nowTick = ref(Date.now());
let pollTimer = null;
let tickTimer = null;

const active = computed(() => !!(state.value && state.value.active));
const sessionSeconds = computed(() => {
  if (!active.value || !state.value?.sessionStartTime) return 0;
  return Math.max(0, Math.floor((nowTick.value - state.value.sessionStartTime) / 1000));
});

const activeRegionData = computed(() => regions.value.find((r) => r.region === activeRegion.value) || null);
const subRegions = computed(() => activeRegionData.value?.subRegions || []);
const showSubTabs = computed(() => subRegions.value.length > 1);
const currentSubRegion = computed(() => {
  if (!showSubTabs.value) return '';
  return subRegions.value.some((s) => s.subRegion === activeSubRegion.value)
    ? activeSubRegion.value
    : subRegions.value[0]?.subRegion || '';
});

const currentSubRegionName = computed(
  () => subRegions.value.find((s) => s.subRegion === currentSubRegion.value)?.subRegionName || ''
);

const regionMaps = computed(() => {
  if (!showSubTabs.value) return activeRegionData.value?.maps || [];
  const sub = subRegions.value.find((s) => s.subRegion === currentSubRegion.value);
  return sub ? sub.maps : [];
});

function mapIncome(map) {
  return (Number(map?.mesoPerHour) || 0) + (Number(map?.expPerHour) || 0);
}

// 金币 + 经验总量从高到低，收益最高的地图自动排在最前
const sortedRegionMaps = computed(() =>
  [...regionMaps.value].sort((a, b) => mapIncome(b) - mapIncome(a))
);

function selectRegion(region) {
  activeRegion.value = region.region;
  activeSubRegion.value = region.subRegions?.[0]?.subRegion || '';
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 已启用但不在当前地图掉落的物品，允许在白名单管理里取消
const wlOtherItems = computed(() => {
  const mapIds = new Set(wlItems.value.map((it) => it.itemId));
  return wlAll.value.filter((it) => !mapIds.has(it.itemId));
});

const pendingItemCount = computed(() => (state.value?.pendingItems || []).reduce((s, i) => s + i.quantity, 0));
const hasPending = computed(() => !!(state.value && (state.value.pendingMeso > 0 || state.value.pendingExp > 0 || pendingItemCount.value > 0)));

// 秒级跳动的实时收益：以最近一次 progress 的增速做本地插值，每 2 秒与服务端校准
const live = reactive({ meso: 0, exp: 0, kills: 0, mps: 0, eps: 0, kps: 0, at: 0 });

function applyProgress(data) {
  if (!data) return;
  const sec = Math.max(1, Number(data.settledSeconds) || 2);
  live.mps = Number(data.gainedMeso) / sec;
  live.eps = Number(data.gainedExp) / sec;
  live.kps = Number(data.gainedKills) / sec;
  live.meso = Number(data.pendingMeso) || 0;
  live.exp = Number(data.pendingExp) || 0;
  live.kills = Number(data.sessionKills) || 0;
  live.at = Date.now();
}

function interpolated(base, rate) {
  const drift = Math.max(0, (nowTick.value - live.at) / 1000);
  return base + rate * drift;
}

const liveMeso = computed(() => (active.value ? interpolated(live.meso, live.mps) : Number(state.value?.pendingMeso) || 0));
const liveExp = computed(() => (active.value ? interpolated(live.exp, live.eps) : Number(state.value?.pendingExp) || 0));
const liveKills = computed(() => (active.value ? interpolated(live.kills, live.kps) : Number(state.value?.sessionKills) || 0));

// 当前挂机地图的怪物列表（战斗卡展示）
const battleMobs = ref([]);
const thumbBroken = ref(false);
let battleMapId = 0;

watch(
  () => state.value?.mapId,
  (mapId) => {
    if (!mapId) {
      battleMobs.value = [];
      return;
    }
    if (battleMapId === mapId && battleMobs.value.length) return;
    battleMapId = mapId;
    thumbBroken.value = false;
    loadBattleMobs(mapId);
  },
  { immediate: true }
);

async function loadBattleMobs(mapId) {
  try {
    const data = await api.mapDetail(character.value.characterId, mapId);
    if (state.value?.mapId !== mapId) return;
    battleMobs.value = (data.mobs || []).slice(0, 8);
  } catch {
    // 战斗卡怪物加载失败不影响主流程
  }
}

function notify(text) {
  toast.value = text;
  clearTimeout(notify.timer);
  notify.timer = setTimeout(() => (toast.value = ''), 2200);
}

async function loadState() {
  try {
    state.value = await api.state(character.value.characterId);
    applyProgress(state.value);
    syncPolling();
  } catch (e) {
    notify(e.message);
  }
}

function syncPolling() {
  if (active.value) {
    startPolling();
    startTicking();
  } else {
    stopPolling();
    stopTicking();
  }
}

function startTicking() {
  if (tickTimer) return;
  nowTick.value = Date.now();
  tickTimer = setInterval(() => {
    nowTick.value = Date.now();
  }, 1000);
}

function stopTicking() {
  if (tickTimer) {
    clearInterval(tickTimer);
    tickTimer = null;
  }
}

function startPolling() {
  if (pollTimer) return;
  pollTimer = setInterval(async () => {
    nowTick.value = Date.now();
    if (!state.value?.active) return;
    try {
      const data = await api.progress(character.value.characterId);
      if (data) {
        state.value = { ...(state.value || {}), ...data };
        applyProgress(data);
      }
      nowTick.value = Date.now();
    } catch {
      // 刷新失败不影响挂机，下一轮重试
    }
  }, 2000);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}

async function startHangup() {
  if (busy.value) return;
  const mapId = state.value?.mapId || character.value?.mapId;
  if (!mapId) {
    notify('请先选择挂机地图');
    return;
  }
  busy.value = 'start';
  try {
    const data = await api.start(character.value.characterId, mapId);
    state.value = { ...(state.value || {}), ...data };
    applyProgress(data);
    nowTick.value = Date.now();
    syncPolling();
    notify(`已开始挂机：${data.mapName}`);
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

async function loadMaps() {
  loadingMaps.value = true;
  try {
    const data = await api.maps(character.value.characterId);
    regions.value = data.regions || [];
    if (data.character) applyCharacter(data.character);
    const mapId = character.value.mapId;
    const r = mapId ? regions.value.find((rg) => rg.maps.some((m) => m.mapId === mapId)) : null;
    if (r) {
      activeRegion.value = r.region;
      const sub = (r.subRegions || []).find((s) => s.maps.some((m) => m.mapId === mapId));
      activeSubRegion.value = sub ? sub.subRegion : r.subRegions?.[0]?.subRegion || '';
    } else if (regions.value.length > 0) {
      selectRegion(regions.value[0]);
    }
  } catch (e) {
    notify(e.message);
  } finally {
    loadingMaps.value = false;
  }
}

function applyCharacter(chr) {
  character.value = { ...character.value, ...chr };
  emit('character-change', character.value);
}

async function settle() {
  if (busy.value) return;
  busy.value = 'settle';
  try {
    const data = await api.settle(character.value.characterId);
    const first = data?.characters?.[0];
    if (first) {
      state.value = { ...(state.value || {}), ...first };
      applyProgress(first);
    }
    stopPolling();
    stopTicking();
    const gained = first ? `+${formatNumber(first.gainedMeso)} 金币 / +${formatNumber(first.gainedExp)} 经验 / 击杀 ${formatNumber(first.sessionKills)} 只` : '结算完成';
    notify(`已结算并停止挂机 ${gained}`);
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

async function claim() {
  if (busy.value) return;
  busy.value = 'claim';
  try {
    const data = await api.claim(character.value.characterId);
    const given = (Number(data.givenMeso) || 0) + (Number(data.givenExp) || 0);
    if (given > 0) {
      notify(`已发放 ${formatNumber(data.givenMeso)} 金币、${formatNumber(data.givenExp)} 经验`);
    } else {
      // 角色离线时收益保留在待领取，登录游戏后自动发放
      notify(`角色不在线，${formatNumber(data.pendingMeso)} 金币、${formatNumber(data.pendingExp)} 经验将在登录游戏时自动发放`);
    }
    await loadState();
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

function toggleDiscard(item) {
  const next = new Set(discardPicked.value);
  if (next.has(item.itemId)) next.delete(item.itemId);
  else next.add(item.itemId);
  discardPicked.value = next;
}

async function discardSelected() {
  if (busy.value || !discardPicked.value.size) return;
  if (!window.confirm(`确定丢弃选中的 ${discardPicked.value.size} 件战利品？丢弃后无法恢复。`)) return;
  busy.value = 'discard';
  try {
    const data = await api.discardRewardItems(character.value.characterId, [...discardPicked.value]);
    if (data) {
      state.value = { ...(state.value || {}), ...data };
      applyProgress(data);
    }
    discardPicked.value = new Set();
    notify('已丢弃所选战利品');
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

// 待领取物品变化（领取/新增/结算）后剔除已消失的勾选，保留仍存在的选择
watch(
  () => (state.value?.pendingItems || []).map((it) => it.itemId).join(','),
  () => {
    if (!discardPicked.value.size) return;
    const alive = new Set((state.value?.pendingItems || []).map((it) => it.itemId));
    const next = new Set([...discardPicked.value].filter((id) => alive.has(id)));
    if (next.size !== discardPicked.value.size) discardPicked.value = next;
  }
);

async function openMap(map) {
  detailLoading.value = true;
  sheetOpen.value = true;
  try {
    const data = await api.mapDetail(character.value.characterId, map.mapId);
    detail.value = data;
    picked.value = new Set((data.drops || []).filter((d) => d.whitelisted).map((d) => d.itemId));
  } catch (e) {
    notify(e.message);
    sheetOpen.value = false;
    detail.value = null;
  } finally {
    detailLoading.value = false;
  }
}

function closeSheet() {
  sheetOpen.value = false;
  detail.value = null;
}

function toggleItem(item) {
  const next = new Set(picked.value);
  if (next.has(item.itemId)) next.delete(item.itemId);
  else next.add(item.itemId);
  picked.value = next;
}

async function saveWhitelist() {
  if (!detail.value || busy.value) return;
  busy.value = 'whitelist';
  try {
    const mapId = detail.value.map.mapId;
    // 只覆盖当前地图的勾选项，保留其他地图已启用的物品
    const current = await api.drops(character.value.characterId, mapId);
    const mapItemIds = new Set((detail.value.drops || []).map((d) => d.itemId));
    const keep = (current.whitelistedItems || [])
      .map((it) => it.itemId)
      .filter((id) => !mapItemIds.has(id));
    const itemIds = [...new Set([...picked.value, ...keep])];
    await api.saveWhitelist(character.value.characterId, mapId, itemIds);
    wlCount.value = itemIds.length;
    notify(`白名单已保存（${itemIds.length} 项）`);
    await openMap(detail.value.map);
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

function applyWhitelistData(data) {
  wlItems.value = data?.items || [];
  wlAll.value = data?.whitelistedItems || [];
  wlEnabled.value = new Set(wlAll.value.map((it) => it.itemId));
  wlCount.value = wlEnabled.value.size;
}

async function loadWhitelistMeta() {
  try {
    const data = await api.drops(character.value.characterId, 0);
    wlCount.value = (data?.whitelistedItems || []).length;
  } catch {
    // 白名单数量读取失败不影响主流程
  }
}

async function openWhitelist() {
  if (wlLoading.value) return;
  const mapId = state.value?.mapId || character.value?.mapId || 0;
  wlMapId.value = mapId;
  wlMapName.value = mapId ? state.value?.mapName || character.value?.mapName || '' : '';
  wlOpen.value = true;
  wlLoading.value = true;
  try {
    const data = await api.drops(character.value.characterId, mapId);
    applyWhitelistData(data);
  } catch (e) {
    notify(e.message);
    wlOpen.value = false;
  } finally {
    wlLoading.value = false;
  }
}

function toggleWlItem(item) {
  const next = new Set(wlEnabled.value);
  if (next.has(item.itemId)) next.delete(item.itemId);
  else next.add(item.itemId);
  wlEnabled.value = next;
}

async function saveCurrentWhitelist() {
  if (wlSaving.value) return;
  if (!wlEnabled.value.size && !window.confirm('未启用任何拾取物品时挂机不会产出掉落物，确定保存？')) return;
  wlSaving.value = true;
  try {
    const itemIds = [...wlEnabled.value];
    const data = await api.saveWhitelist(character.value.characterId, wlMapId.value, itemIds);
    applyWhitelistData(data);
    notify(`白名单已保存（${itemIds.length} 项）`);
    wlOpen.value = false;
  } catch (e) {
    notify(e.message);
  } finally {
    wlSaving.value = false;
  }
}

async function chooseMap() {
  if (!detail.value || busy.value) return;
  if (active.value) {
    notify('挂机中不能更换地图，请先结算收益');
    return;
  }
  busy.value = 'select';
  try {
    const chr = await api.selectMap(character.value.characterId, detail.value.map.mapId);
    applyCharacter(chr);
    await loadMaps();
    await loadState();
    notify(`已选择挂机地图：${chr.mapName}`);
    closeSheet();
  } catch (e) {
    notify(e.message);
  } finally {
    busy.value = '';
  }
}

onMounted(async () => {
  await loadState();
  await loadMaps();
  await loadWhitelistMeta();
});

onUnmounted(() => {
  stopPolling();
  stopTicking();
});
</script>

<template>
  <div class="hangup">
    <header class="topbar">
      <button class="back" @click="emit('back')">‹</button>
      <div class="who">
        <strong>挂机大厅</strong>
        <span class="muted">{{ character.characterName }} · Lv.{{ character.level }}</span>
      </div>
      <span class="top-emblem" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M12 2.2l2.1 3.9 3.8-1.1-1.4 3.6 4.2 1.6-3.2 2.4 2.2 3.4-4-.3.4 4.5-4.1-2-4.1 2 .4-4.5-4-.3 2.2-3.4-3.2-2.4 4.2-1.6L8.9 5l3.8 1.1z" />
        </svg>
      </span>
    </header>

    <section class="card hero">
      <div class="hero-head">
        <div class="hero-avatar">
          <span>Lv</span>
          <strong>{{ character.level }}</strong>
        </div>
        <div class="hero-id">
          <strong>{{ character.characterName }}</strong>
          <span class="muted">{{ jobName(character.job) }} · {{ character.level }} 级</span>
        </div>
        <div class="power">
          <span class="power-label">
            战斗力
            <button class="help-dot" aria-label="战斗力计算说明" @click="powerHelpOpen = true">?</button>
          </span>
          <strong class="power-value">{{ formatNumber(character.dps) }}</strong>
          <span class="power-sub">估算 DPS</span>
        </div>
      </div>
      <div class="hero-stats">
        <div class="cell">
          <span class="muted">今日剩余</span>
          <strong>{{ !state ? '—' : state.dailyRemainSeconds < 0 ? '不限' : formatDuration(state.dailyRemainSeconds) }}</strong>
        </div>
        <div class="cell">
          <span class="muted">当前地图</span>
          <strong class="ellipsis">{{ state?.mapName || '未选择' }}</strong>
        </div>
        <div class="cell">
          <span class="muted">状态</span>
          <strong :class="{ 'gold-text': active }">{{ active ? '挂机中' : '未挂机' }}</strong>
        </div>
      </div>
      <div v-if="active" class="session-tip">
        本次挂机 <strong>{{ formatDuration(sessionSeconds) }}</strong>
        · 已击杀 <strong>{{ formatNumber(liveKills) }}</strong> 只
        <span v-if="state?.online" class="muted">（角色在线，暂停累计）</span>
        <span class="muted">· 收益实时刷新</span>
      </div>
      <div v-else-if="!state?.mapName" class="session-tip muted">
        先在地图列表里选择一张挂机地图，再点击「开始挂机」
      </div>
    </section>

    <section class="card summary">
      <div class="panel-title">
        <strong>挂机收益</strong>
        <span class="muted">服务端权威结算</span>
      </div>
      <div class="wallet">
        <div class="money">
          <span class="muted">待领取金币</span>
          <strong class="gold-text" :class="{ bump: active }">{{ formatNumber(liveMeso) }}</strong>
        </div>
        <div class="money">
          <span class="muted">待领取经验</span>
          <strong class="exp-text" :class="{ bump: active }">{{ formatNumber(liveExp) }}</strong>
        </div>
        <div class="money">
          <span class="muted">待领取物品</span>
          <strong>{{ pendingItemCount }}</strong>
        </div>
      </div>
      <div class="whitelist-entry">
        <div class="whitelist-info">
          <span class="muted">拾取白名单</span>
          <strong :class="{ 'gold-text': wlCount > 0 }">
            {{ wlCount > 0 ? `已启用 ${wlCount} 项` : '未启用，挂机不产出掉落物' }}
          </strong>
        </div>
        <button class="btn ghost small" @click="openWhitelist">修改</button>
      </div>
      <div class="actions">
        <button v-if="!active" class="btn gold" :disabled="!!busy" @click="startHangup">
          {{ busy === 'start' ? '开始中…' : '开始挂机' }}
        </button>
        <button v-else class="btn ghost" :disabled="!!busy" @click="settle">
          {{ busy === 'settle' ? '结算中…' : '结算收益（停止）' }}
        </button>
        <button class="btn" :class="active ? 'gold' : 'ghost'" :disabled="!!busy || !hasPending" @click="claim">
          {{ busy === 'claim' ? '领取中…' : state?.online ? '领取到角色' : '登录时自动发放' }}
        </button>
      </div>
      <div v-if="hasPending && !state?.online" class="session-tip muted">
        角色离线中，待领取收益将在登录游戏时自动发放；角色在线时点击按钮立即入账
      </div>
      <div v-if="state?.pendingItems?.length" class="pending-items">
        <div
          v-for="it in state.pendingItems"
          :key="it.itemId"
          class="pending-item"
          :class="{ on: discardPicked.has(it.itemId) }"
          @click="toggleDiscard(it)"
        >
          <img
            class="check pixel-img"
            :src="discardPicked.has(it.itemId) ? '/assets/ui/Basic/CheckBox/1.png' : '/assets/ui/Basic/CheckBox/0.png'"
            alt=""
          />
          <img :src="itemIconUrl(it.itemId)" class="icon pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
          <span>{{ it.itemName || it.itemId }}</span>
          <span class="qty">×{{ it.quantity }}</span>
        </div>
        <div class="pending-actions">
          <span class="muted">已选 {{ discardPicked.size }} 项</span>
          <button class="btn ghost" :disabled="!discardPicked.size || !!busy" @click="discardSelected">
            {{ busy === 'discard' ? '丢弃中…' : '丢弃所选战利品' }}
          </button>
        </div>
      </div>
    </section>

    <section v-if="state?.mapId" class="card battle">
      <div class="battle-thumb">
        <img
          v-if="!thumbBroken"
          :src="mapThumbUrl(state.mapId)"
          class="pixel-img"
          alt=""
          @error="thumbBroken = true"
        />
        <div v-else class="thumb-fallback">
          <strong>{{ state.mapName || '未知地图' }}</strong>
        </div>
      </div>
      <div class="battle-side">
        <div class="battle-title">
          <strong>{{ state.mapName || '当前地图' }}</strong>
          <span v-if="active" class="chip on">击杀 {{ formatNumber(liveKills) }}</span>
          <span v-else class="chip">已停止</span>
        </div>
        <div v-if="battleMobs.length" class="battle-mobs">
          <div v-for="m in battleMobs" :key="m.mobId" class="battle-mob" :title="`${m.name} Lv.${m.level}`">
            <img :src="mobIconUrl(m.mobId)" class="pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
            <span class="name">{{ m.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="maps">
      <div class="region-tabs">
        <button
          v-for="rg in regions"
          :key="rg.region"
          class="region-tab"
          :class="{ active: rg.region === activeRegion }"
          @click="selectRegion(rg)"
        >
          {{ rg.regionName }}
          <span class="count">{{ rg.mapCount }}</span>
        </button>
      </div>

      <div v-if="showSubTabs" class="sub-tabs">
        <button
          v-for="sub in subRegions"
          :key="sub.subRegion"
          class="sub-tab"
          :class="{ active: sub.subRegion === currentSubRegion }"
          @click="activeSubRegion = sub.subRegion"
        >
          {{ sub.subRegionName }}
          <span class="count">{{ sub.mapCount }}</span>
        </button>
      </div>

      <div v-if="loadingMaps" class="tip muted">正在加载地图…</div>
      <template v-else>
        <div class="list-head">
          <strong>{{ showSubTabs ? currentSubRegionName : activeRegionData?.regionName || '地图' }}</strong>
          <span class="muted">按综合收益排序 · {{ sortedRegionMaps.length }} 张</span>
        </div>
        <div class="map-list">
          <div
            v-for="(map, idx) in sortedRegionMaps"
            :key="map.mapId"
            class="card map"
            @click="openMap(map)"
          >
            <div class="map-thumb-wrap">
              <img
                :src="mapThumbUrl(map.mapId)"
                class="pixel-img"
                loading="lazy"
                alt=""
                @error="(e) => (e.target.style.visibility = 'hidden')"
              />
            </div>
            <div class="map-body">
              <div class="map-head">
                <strong>{{ map.name }}</strong>
                <span v-if="idx === 0" class="chip top">收益最高</span>
                <span v-if="map.selected" class="chip on">{{ active ? '挂机中' : '已选' }}</span>
                <span v-if="map.hasBoss" class="chip boss">BOSS</span>
              </div>
              <div class="map-sub muted">
                {{ map.streetName }} · Lv.{{ map.minLevel }}-{{ map.maxLevel }} · {{ map.spawnCount }} 只怪
              </div>
              <div class="map-stats">
                <span class="stat total">综合/h {{ formatNumber(mapIncome(map)) }}</span>
                <span class="stat exp">经验/h {{ formatNumber(map.expPerHour) }}</span>
                <span class="stat meso">金币/h {{ formatNumber(map.mesoPerHour) }}</span>
                <span class="stat muted">击杀/h {{ formatNumber(map.killsPerHour) }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>
    </section>

    <transition name="fade">
      <div v-if="sheetOpen" class="mask" @click.self="closeSheet">
        <div class="sheet">
          <div class="sheet-grab"></div>
          <header class="sheet-head">
            <div>
              <strong>{{ detail?.map?.name || '加载中…' }}</strong>
              <div class="muted">{{ detail?.map?.streetName }}</div>
            </div>
            <button class="close" @click="closeSheet">✕</button>
          </header>

          <div class="sheet-scroll">
            <div v-if="detailLoading" class="tip muted">正在读取地图数据…</div>

            <template v-else-if="detail">
              <div class="sheet-stats">
                <div><span class="muted">综合/h</span><strong class="total-text">{{ formatNumber(mapIncome(detail.map)) }}</strong></div>
                <div><span class="muted">击杀/h</span><strong>{{ formatNumber(detail.map.killsPerHour) }}</strong></div>
                <div><span class="muted">经验/h</span><strong class="exp-text">{{ formatNumber(detail.map.expPerHour) }}</strong></div>
                <div><span class="muted">金币/h</span><strong class="gold-text">{{ formatNumber(detail.map.mesoPerHour) }}</strong></div>
              </div>

              <div class="section-title">地图怪物</div>
              <div class="mob-list">
                <div v-for="mob in detail.mobs" :key="mob.mobId" class="mob">
                  <img :src="mobIconUrl(mob.mobId)" class="icon pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
                  <span class="mob-name">{{ mob.name }}</span>
                  <span class="muted">Lv.{{ mob.level }}</span>
                  <span class="muted">HP {{ formatNumber(mob.hp) }}</span>
                  <span class="muted">EXP {{ formatNumber(mob.exp) }}</span>
                  <span class="chip">×{{ mob.spawnCount }}</span>
                  <span v-if="mob.boss" class="chip boss">BOSS</span>
                </div>
              </div>

              <div class="section-title">
                掉落白名单
                <span class="muted">已选 {{ picked.size }} 项（不设上限）</span>
              </div>
              <div class="drop-list">
                <div
                  v-for="drop in detail.drops"
                  :key="drop.itemId"
                  class="drop"
                  :class="{ on: picked.has(drop.itemId) }"
                  @click="toggleItem(drop)"
                >
                  <img
                    class="check pixel-img"
                    :src="picked.has(drop.itemId) ? '/assets/ui/Basic/CheckBox/1.png' : '/assets/ui/Basic/CheckBox/0.png'"
                    alt=""
                  />
                  <img :src="itemIconUrl(drop.itemId)" class="icon pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
                  <div class="drop-main">
                    <span class="drop-name">{{ drop.itemName || drop.itemId }}</span>
                    <span class="muted">掉率 {{ drop.chancePercent }}% · 约 {{ formatNumber(drop.expectedPerHour) }}/h</span>
                  </div>
                </div>
                <div v-if="detail.drops.length === 0" class="tip muted">该地图暂无普通掉落</div>
              </div>
            </template>
          </div>

          <div v-if="detail && !detailLoading" class="sheet-actions">
            <button class="btn ghost" :disabled="!!busy" @click="saveWhitelist">
              {{ busy === 'whitelist' ? '保存中…' : '保存白名单' }}
            </button>
            <button class="btn" :disabled="!!busy || active" @click="chooseMap">
              {{ active ? '挂机中不可更换' : busy === 'select' ? '设置中…' : detail.map.selected ? '重新挂机此地图' : '设为挂机地图' }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="wlOpen" class="mask" @click.self="wlOpen = false">
        <div class="sheet">
          <div class="sheet-grab"></div>
          <header class="sheet-head">
            <div>
              <strong>拾取白名单</strong>
              <div class="muted">{{ wlMapName ? `当前地图：${wlMapName}` : '尚未选择挂机地图' }}</div>
            </div>
            <button class="close" @click="wlOpen = false">✕</button>
          </header>

          <div class="sheet-scroll">
            <div v-if="wlLoading" class="tip muted">正在读取白名单…</div>

            <template v-else>
              <div class="section-title">
                当前地图可掉落
                <span class="muted">已选 {{ wlEnabled.size }} 项</span>
              </div>
              <div class="drop-list">
                <div
                  v-for="drop in wlItems"
                  :key="drop.itemId"
                  class="drop"
                  :class="{ on: wlEnabled.has(drop.itemId) }"
                  @click="toggleWlItem(drop)"
                >
                  <img
                    class="check pixel-img"
                    :src="wlEnabled.has(drop.itemId) ? '/assets/ui/Basic/CheckBox/1.png' : '/assets/ui/Basic/CheckBox/0.png'"
                    alt=""
                  />
                  <img :src="itemIconUrl(drop.itemId)" class="icon pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
                  <div class="drop-main">
                    <span class="drop-name">{{ drop.itemName || drop.itemId }}</span>
                    <span class="muted">掉率 {{ drop.chancePercent }}% · 约 {{ formatNumber(drop.expectedPerHour) }}/h</span>
                  </div>
                </div>
                <div v-if="!wlItems.length" class="tip muted">
                  {{ wlMapName ? '该地图暂无普通掉落' : '请先选择挂机地图，再勾选希望拾取的物品' }}
                </div>
              </div>

              <template v-if="wlOtherItems.length">
                <div class="section-title">
                  其他已启用物品
                  <span class="muted">不在当前地图掉落，可取消勾选</span>
                </div>
                <div class="drop-list">
                  <div
                    v-for="item in wlOtherItems"
                    :key="item.itemId"
                    class="drop"
                    :class="{ on: wlEnabled.has(item.itemId) }"
                    @click="toggleWlItem(item)"
                  >
                    <img
                      class="check pixel-img"
                      :src="wlEnabled.has(item.itemId) ? '/assets/ui/Basic/CheckBox/1.png' : '/assets/ui/Basic/CheckBox/0.png'"
                      alt=""
                    />
                    <img :src="itemIconUrl(item.itemId)" class="icon pixel-img" alt="" @error="(e) => (e.target.style.visibility = 'hidden')" />
                    <div class="drop-main">
                      <span class="drop-name">{{ item.itemName || item.itemId }}</span>
                      <span class="muted">不在当前地图掉落</span>
                    </div>
                  </div>
                </div>
              </template>

              <div v-if="!wlEnabled.size" class="session-tip muted">
                未勾选任何物品时，挂机不会产出掉落物
              </div>
            </template>
          </div>

          <div v-if="!wlLoading" class="sheet-actions">
            <button class="btn ghost" :disabled="wlSaving" @click="wlOpen = false">取消</button>
            <button class="btn" :disabled="wlSaving" @click="saveCurrentWhitelist">
              {{ wlSaving ? '保存中…' : '保存白名单' }}
            </button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="toast">
      <div v-if="toast" class="toast">{{ toast }}</div>
    </transition>

    <BottomNav active="hangup" @hangup="scrollToTop" @info="emit('open-info')" />
    <PowerHelp :show="powerHelpOpen" @close="powerHelpOpen = false" />
  </div>
</template>

<style scoped>
.hangup {
  min-height: 100vh;
  padding: 0 14px calc(84px + var(--safe-bottom));
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 0;
  background: linear-gradient(180deg, rgba(7, 24, 34, 0.94) 70%, rgba(7, 24, 34, 0));
  backdrop-filter: blur(8px);
}

.back {
  width: 36px;
  height: 36px;
  font-size: 26px;
  line-height: 1;
  color: var(--text);
  border-radius: 8px;
}

.who {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.who strong {
  font-size: 16px;
  letter-spacing: 2px;
}

.who .muted {
  font-size: 11px;
}

.top-emblem {
  flex: 0 0 auto;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  color: var(--gold);
}

.top-emblem svg {
  width: 24px;
  height: 24px;
  fill: currentColor;
  filter: drop-shadow(0 0 6px rgba(255, 190, 70, 0.45));
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 16px;
  margin-top: 2px;
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(320px 130px at 92% 0%, rgba(255, 190, 70, 0.16), transparent 70%);
}

.hero-head {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-avatar {
  flex: 0 0 52px;
  height: 52px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  border: 1px solid #fff0b8;
  color: #3d2400;
  background: linear-gradient(180deg, #ffe08a 0%, #ffc14d 48%, #ee921d 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7), 0 3px 0 #8d5109;
}

.hero-avatar span {
  font-size: 9px;
  font-weight: 700;
  line-height: 1;
  opacity: 0.75;
}

.hero-avatar strong {
  font-size: 18px;
  line-height: 1.15;
}

.hero-id {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.hero-id strong {
  font-size: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hero-id .muted {
  font-size: 11px;
}

.power {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.power-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  color: var(--text-dim);
}

.help-dot {
  width: 20px;
  height: 20px;
  padding: 0;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid rgba(255, 208, 92, 0.6);
  color: var(--gold);
  background: rgba(255, 208, 92, 0.1);
  font-size: 11px;
  font-weight: 800;
}

.power-value {
  font-size: 26px;
  font-weight: 900;
  line-height: 1.15;
  color: var(--gold);
  font-variant-numeric: tabular-nums;
  text-shadow: 0 0 12px rgba(255, 190, 70, 0.35), 0 2px 0 rgba(0, 0, 0, 0.35);
}

.power-sub {
  font-size: 9px;
  letter-spacing: 0.5px;
  color: var(--text-dim);
}

.hero-stats {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed rgba(214, 166, 91, 0.35);
}

.summary {
  padding: 14px;
}

.panel-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 10px;
}

.panel-title strong {
  font-size: 14px;
  color: var(--gold);
}

.panel-title .muted {
  font-size: 10px;
}

.cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.cell strong {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell .muted {
  font-size: 11px;
}

.session-tip {
  margin-top: 10px;
  padding: 7px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  font-size: 12px;
  color: var(--text-dim);
}

.session-tip strong {
  color: var(--gold);
}

.wallet strong.bump {
  animation: bump 1s ease-in-out infinite;
}

@keyframes bump {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.04); }
}

.battle {
  display: flex;
  gap: 12px;
  margin-top: 10px;
  padding: 10px;
}

.battle-thumb {
  flex: 0 0 128px;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--line);
  background: rgba(10, 16, 34, 0.6);
}

.battle-thumb img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  image-rendering: pixelated;
}

.thumb-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: var(--text-dim);
  background: linear-gradient(135deg, #182446, #101a36);
}

.battle-side {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.battle-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.battle-mobs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.battle-mob {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px 4px 4px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: rgba(20, 44, 60, 0.7);
  font-size: 11px;
  color: var(--text-dim);
}

.battle-mob img {
  width: 26px;
  height: 26px;
  object-fit: contain;
  image-rendering: pixelated;
}

.battle-mob .name {
  max-width: 72px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wallet {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.money {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.money .muted {
  font-size: 11px;
}

.money strong {
  font-size: 18px;
}

.whitelist-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed var(--line-soft);
}

.whitelist-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.whitelist-info .muted {
  font-size: 11px;
}

.whitelist-info strong {
  font-size: 13px;
}

.gold-text {
  color: var(--gold);
}

.exp-text {
  color: var(--green);
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

.actions .btn {
  flex: 1;
}

.pending-items {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.pending-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  padding: 4px 6px;
  border-radius: 8px;
  cursor: pointer;
}

.pending-item.on {
  background: rgba(255, 208, 74, 0.1);
}

.pending-item .check {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
}

.pending-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 4px;
}

.icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  flex: 0 0 24px;
}

.pending-item .qty {
  margin-left: auto;
  color: var(--gold);
}

.region-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 16px 0 10px;
  scrollbar-width: none;
}

.region-tabs::-webkit-scrollbar {
  display: none;
}

.region-tab {
  flex: 0 0 auto;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--text-dim);
  border: 1px solid var(--line);
  background: rgba(20, 44, 60, 0.8);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.region-tab.active {
  color: #3d2400;
  font-weight: 800;
  border-color: #fff0b8;
  background: linear-gradient(180deg, #fff0a8, #ffc14d);
}

.region-tab .count {
  font-size: 11px;
  opacity: 0.75;
}

.sub-tabs {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding: 0 0 10px;
  scrollbar-width: none;
}

.sub-tabs::-webkit-scrollbar {
  display: none;
}

.sub-tab {
  flex: 0 0 auto;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 12px;
  color: var(--text-dim);
  border: 1px solid var(--line);
  background: rgba(20, 44, 60, 0.55);
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.sub-tab.active {
  color: var(--gold);
  border-color: var(--gold);
  background: rgba(255, 196, 73, 0.12);
}

.sub-tab .count {
  font-size: 10px;
  opacity: 0.7;
}

.list-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin: 2px 2px 8px;
}

.list-head strong {
  font-size: 13px;
  color: var(--gold);
}

.list-head .muted {
  font-size: 10px;
}

.map-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.map {
  display: flex;
  gap: 10px;
  padding: 10px;
  cursor: pointer;
  transition: transform 0.12s ease, border-color 0.15s ease;
}

.map:active {
  transform: scale(0.99);
  border-color: rgba(255, 209, 92, 0.85);
}

.map-thumb-wrap {
  flex: 0 0 70px;
  height: 54px;
  display: grid;
  place-items: center;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(214, 166, 91, 0.35);
  background: linear-gradient(135deg, #1b3a4b, #0b1c28);
}

.map-thumb-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  image-rendering: pixelated;
}

.map-body {
  flex: 1;
  min-width: 0;
}

.map-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.map-head strong {
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip.top {
  color: #3d2400;
  border-color: #fff0b8;
  background: linear-gradient(180deg, #fff0a8, #ffc14d);
  font-weight: 800;
}

.chip.boss {
  color: #ffd9d9;
  border-color: rgba(255, 107, 107, 0.55);
  background: rgba(255, 107, 107, 0.16);
  font-weight: 700;
}

.map-sub {
  margin-top: 4px;
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.map-stats {
  margin-top: 7px;
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.stat {
  font-size: 10px;
  padding: 3px 7px;
  border-radius: 4px;
  background: rgba(8, 20, 30, 0.65);
  border: 1px solid var(--line-soft);
}

.stat.total {
  color: #ffd98a;
  border-color: rgba(214, 166, 91, 0.5);
  background: rgba(255, 190, 70, 0.1);
  font-weight: 700;
}

.stat.exp {
  color: var(--green);
}

.stat.meso {
  color: var(--gold);
}

.mask {
  position: fixed;
  inset: 0;
  z-index: 30;
  background: rgba(3, 12, 18, 0.75);
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.sheet {
  width: 100%;
  max-width: 560px;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 18px 18px 0 0;
  background: linear-gradient(180deg, #1b394b 0%, #0b1d29 100%);
  border-top: 1px solid rgba(214, 166, 91, 0.65);
  box-shadow: 0 -10px 42px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 224, 160, 0.12);
}

.sheet-grab {
  flex: none;
  width: 44px;
  height: 4px;
  margin: 8px auto 0;
  border-radius: 2px;
  background: rgba(255, 208, 100, 0.35);
}

.sheet-head {
  flex: none;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 16px 8px;
}

.sheet-head strong {
  font-size: 17px;
  color: var(--gold);
}

.sheet-head .muted {
  font-size: 11px;
  margin-top: 3px;
}

.close {
  width: 32px;
  height: 32px;
  flex: 0 0 32px;
  border-radius: 50%;
  color: var(--text-dim);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.07);
}

.sheet-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px 8px;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}

.sheet-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin: 12px 0;
}

.sheet-stats > div {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 8px 10px;
  border-radius: 8px;
  background: rgba(7, 18, 28, 0.62);
  border: 1px solid rgba(214, 166, 91, 0.24);
}

.sheet-stats .muted {
  font-size: 10px;
}

.sheet-stats strong {
  font-size: 15px;
}

.total-text {
  color: #ffd98a;
}

.section-title {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 16px 0 8px;
  font-size: 13px;
  font-weight: 700;
}

.section-title .muted {
  font-size: 10px;
  font-weight: 400;
}

.mob-list,
.drop-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mob {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 8px 10px;
  border-radius: 6px;
  background: rgba(10, 30, 42, 0.55);
  border: 1px solid var(--line-soft);
  font-size: 12px;
}

.mob-name {
  font-weight: 700;
}

.drop {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid var(--line-soft);
  background: rgba(10, 30, 42, 0.5);
  cursor: pointer;
}

.drop.on {
  border-color: rgba(94, 227, 154, 0.6);
  background: rgba(47, 143, 94, 0.14);
}

.check {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
}

.drop-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.drop-name {
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.drop-main .muted {
  font-size: 10px;
}

.sheet-actions {
  flex: none;
  display: flex;
  gap: 10px;
  padding: 10px 16px calc(12px + var(--safe-bottom));
  border-top: 1px solid rgba(214, 166, 91, 0.32);
  background: linear-gradient(180deg, rgba(10, 24, 35, 0.72), rgba(7, 18, 27, 0.98));
  box-shadow: 0 -6px 20px rgba(0, 0, 0, 0.45);
}

.sheet-actions .btn {
  flex: 1;
}

.tip {
  padding: 20px 0;
  text-align: center;
  color: var(--text-dim);
  font-size: 12px;
}

.toast {
  position: fixed;
  left: 50%;
  bottom: calc(82px + var(--safe-bottom));
  transform: translateX(-50%);
  z-index: 40;
  max-width: 84vw;
  padding: 10px 16px;
  border-radius: 999px;
  font-size: 13px;
  color: #eaf3ff;
  background: rgba(18, 30, 54, 0.95);
  border: 1px solid #3b5488;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.18s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.toast-enter-active,
.toast-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, 8px);
}
</style>
