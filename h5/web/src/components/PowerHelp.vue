<script setup>
defineProps({
  show: { type: Boolean, default: false },
});
const emit = defineEmits(['close']);
</script>

<template>
  <transition name="power-fade">
    <div v-if="show" class="power-mask" @click.self="emit('close')">
      <div class="power-sheet">
        <header class="power-head">
          <div>
            <strong>战斗力计算说明</strong>
            <div class="power-sub">估算每秒伤害（DPS），用于挂机收益预估</div>
          </div>
          <button class="power-close" @click="emit('close')">✕</button>
        </header>

        <div class="power-scroll">
          <p class="power-lead">
            战斗力不是游戏内的实时面板数值，而是服务端按角色属性、已学技能和武器攻速估算出的
            每秒伤害。物理与魔法职业使用不同公式，并会取已学主动攻击技能中
            「伤害倍率 × 攻击段数」最高的一个。
          </p>

          <div class="power-block">
            <span class="power-label">物理职业</span>
            <code>
              ceil((武器倍率 × 主属性 + 副属性) / 100 × 攻击力)
              × 技能伤害% × 攻击段数 × 客户端倍率 × 攻速系数
            </code>
          </div>

          <div class="power-block">
            <span class="power-label">魔法职业</span>
            <code>
              ((魔法力² / 1000 + 魔法力) / 30 + INT / 200)
              × 技能伤害% × 元素增幅 × 攻击段数 × 客户端倍率 × 攻速系数
            </code>
          </div>

          <ul class="power-notes">
            <li>主属性按武器类型取值：弓/弩/枪用 DEX，拳套/短刀用 LUK，其余用 STR。</li>
            <li>魔法力 = 智力 + 装备魔法力；元素增幅只对对应的法师职业生效。</li>
            <li>客户端倍率、攻速系数由服务端配置决定，当前默认物理 2.0、魔法 0.9、攻速 1.6。</li>
            <li>估算不包含怪物防御、暴击、Buff、命中/回避等实战因素，因此会与游戏内实际伤害有差异。</li>
          </ul>

          <div class="power-block survival">
            <span class="power-label">挂机击杀数量</span>
            <code>
              击杀/h = min(DPS ÷ 地图怪物平均血量 × 3600, 7200) × (1 - 生存惩罚)
            </code>
            <p class="power-note">
              生存惩罚 =（地图最高怪攻击力 - max(物防, 魔防)）÷ 有效血量。
              有效血量对法师按 HP + MP 计算，其余职业按 HP 计算。
            </p>
          </div>
        </div>

        <footer class="power-actions">
          <button class="power-ok" @click="emit('close')">知道了</button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.power-mask {
  position: fixed;
  inset: 0;
  z-index: 45;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(3, 8, 14, 0.76);
}

.power-sheet {
  width: 100%;
  max-width: 560px;
  max-height: 86vh;
  display: flex;
  flex-direction: column;
  border-radius: 18px 18px 0 0;
  border-top: 1px solid rgba(214, 166, 91, 0.65);
  background: linear-gradient(180deg, #1c3a4c 0%, #0b1d2a 100%);
  box-shadow: 0 -10px 42px rgba(0, 0, 0, 0.6), inset 0 1px 0 rgba(255, 224, 160, 0.12);
}

.power-head {
  flex: none;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 10px;
}

.power-head strong {
  font-size: 18px;
  color: var(--gold);
}

.power-sub {
  margin-top: 4px;
  font-size: 11px;
  color: var(--text-dim);
}

.power-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--text-dim);
  background: rgba(255, 255, 255, 0.07);
}

.power-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 4px 18px 14px;
  overscroll-behavior: contain;
}

.power-lead {
  margin: 6px 0 14px;
  font-size: 13px;
  line-height: 1.75;
  color: var(--text-dim);
}

.power-block {
  margin-bottom: 10px;
  padding: 11px 12px;
  border-radius: 10px;
  border: 1px solid rgba(214, 166, 91, 0.32);
  background: rgba(6, 18, 27, 0.6);
}

.power-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: var(--gold);
}

.power-block code {
  display: block;
  font-family: 'Cascadia Mono', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #d9ecf4;
  word-break: break-word;
}

.power-notes {
  margin: 12px 0 16px;
  padding-left: 18px;
  font-size: 12px;
  line-height: 1.8;
  color: var(--text-dim);
}

.power-notes li + li {
  margin-top: 4px;
}

.power-block.survival {
  border-color: rgba(111, 227, 255, 0.3);
}

.power-note {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.65;
  color: var(--text-dim);
}

.power-actions {
  flex: none;
  padding: 10px 18px calc(12px + var(--safe-bottom));
  border-top: 1px solid rgba(214, 166, 91, 0.32);
  background: linear-gradient(180deg, rgba(10, 24, 35, 0.7), rgba(7, 18, 27, 0.98));
}

.power-ok {
  width: 100%;
  min-height: 46px;
  border-radius: 10px;
  border: 1px solid #ffe0a0;
  font-size: 15px;
  font-weight: 800;
  color: #3d2400;
  background: linear-gradient(180deg, #ffe08a 0%, #ffbf4d 50%, #ee921d 100%);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.65), 0 3px 0 #8d5109;
}

.power-fade-enter-active,
.power-fade-leave-active {
  transition: opacity 0.18s ease;
}
.power-fade-enter-from,
.power-fade-leave-to {
  opacity: 0;
}
</style>
