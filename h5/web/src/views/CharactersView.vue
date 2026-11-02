<script setup>
import { onMounted, ref } from 'vue';
import { api, jobName, formatNumber, getAccount } from '../api';

const emit = defineEmits(['select', 'logout']);

const loading = ref(true);
const error = ref('');
const characters = ref([]);
const account = getAccount();

async function load() {
  loading.value = true;
  error.value = '';
  try {
    const list = await api.characters();
    characters.value = list || [];
    if (characters.value.length === 1) emit('select', characters.value[0]);
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="chars">
    <header class="head">
      <div>
        <div class="hello">欢迎回来</div>
        <div class="name">{{ account?.name || '' }}</div>
      </div>
      <button class="btn ghost small" @click="emit('logout')">退出</button>
    </header>

    <div v-if="loading" class="tip muted">正在加载角色…</div>
    <div v-else-if="error" class="tip error">
      {{ error }}
      <button class="btn ghost small retry" @click="load">重试</button>
    </div>
    <div v-else-if="characters.length === 0" class="tip muted">该账号下没有角色</div>

    <ul v-else class="list">
      <li
        v-for="chr in characters"
        :key="chr.characterId"
        class="card chr"
        @click="emit('select', chr)"
      >
        <div class="avatar">
          <span>{{ chr.level }}</span>
        </div>
        <div class="info">
          <div class="row1">
            <strong>{{ chr.characterName }}</strong>
            <span class="chip">{{ jobName(chr.job) }}</span>
          </div>
          <div class="row2 muted">
            Lv.{{ chr.level }} · 战斗力 {{ formatNumber(chr.dps) }}
            <span v-if="chr.mapName"> · {{ chr.mapName }}</span>
            <span v-else> · 未选择挂机地图</span>
          </div>
          <div class="row3">
            <span class="stat">STR {{ chr.str }}</span>
            <span class="stat">DEX {{ chr.dex }}</span>
            <span class="stat">INT {{ chr.intValue }}</span>
            <span class="stat">LUK {{ chr.luk }}</span>
          </div>
        </div>
        <div class="arrow">›</div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.chars {
  min-height: 100vh;
  padding: 22px 16px calc(24px + var(--safe-bottom));
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.hello {
  font-size: 12px;
  color: var(--text-dim);
}

.name {
  font-size: 20px;
  font-weight: 800;
  margin-top: 2px;
}

.tip {
  padding: 40px 0;
  text-align: center;
}

.tip.error {
  color: var(--red);
}

.retry {
  margin-top: 12px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chr {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  cursor: pointer;
  transition: transform 0.12s ease, border-color 0.15s ease;
}

.chr:active {
  transform: scale(0.985);
  border-color: #6fa8ff;
}

.avatar {
  flex: 0 0 48px;
  height: 48px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 16px;
  color: #08203d;
  background: radial-gradient(circle at 32% 28%, #a9dcff, #4b8df0 68%, #2352a8);
  box-shadow: inset 0 0 0 2px rgba(255, 255, 255, 0.45), 0 4px 12px rgba(0, 0, 0, 0.4);
}

.info {
  flex: 1;
  min-width: 0;
}

.row1 {
  display: flex;
  align-items: center;
  gap: 8px;
}

.row1 strong {
  font-size: 16px;
}

.row2 {
  margin-top: 4px;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row3 {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.stat {
  font-size: 11px;
  color: var(--text-dim);
  padding: 1px 6px;
  border-radius: 4px;
  background: rgba(47, 64, 102, 0.35);
  border: 1px solid var(--line-soft);
}

.arrow {
  color: var(--text-dim);
  font-size: 22px;
  line-height: 1;
}
</style>
