<script setup>
defineProps({
  active: { type: String, default: 'hangup' },
});
const emit = defineEmits(['hangup', 'info']);
</script>

<template>
  <nav class="game-nav">
    <button class="nav-item" :class="{ active: active === 'hangup' }" @click="emit('hangup')">
      <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M14.6 3.2l6.2 6.2-9.1 9.1H5.1v-6.6l9.5-8.7z" />
        <path d="M12.4 5.9l5.7 5.7" />
      </svg>
      <span>挂机</span>
    </button>
    <button class="nav-item" :class="{ active: active === 'info' }" @click="emit('info')">
      <svg class="nav-icon" viewBox="0 0 24 24" aria-hidden="true">
        <circle cx="12" cy="7.6" r="3.6" />
        <path d="M4.8 20c.9-4.2 3.6-6.3 7.2-6.3s6.3 2.1 7.2 6.3" />
      </svg>
      <span>角色</span>
    </button>
  </nav>
</template>

<style scoped>
.game-nav {
  position: fixed;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  z-index: 20;
  width: 100%;
  max-width: 560px;
  display: flex;
  gap: 8px;
  padding: 6px 12px calc(6px + var(--safe-bottom));
  background: linear-gradient(180deg, rgba(15, 34, 47, 0.94), rgba(5, 14, 22, 0.99));
  border-top: 1px solid rgba(214, 166, 91, 0.55);
  box-shadow: 0 -8px 26px rgba(0, 0, 0, 0.45), inset 0 1px 0 rgba(255, 224, 160, 0.14);
  backdrop-filter: blur(10px);
}

.nav-item {
  position: relative;
  flex: 1;
  min-height: 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border-radius: 10px;
  color: var(--text-dim);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 1px;
  transition: color 0.15s ease, background 0.15s ease;
}

.nav-item.active {
  color: var(--gold);
  background: radial-gradient(circle at 50% 120%, rgba(255, 190, 70, 0.24), transparent 68%);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  top: -6px;
  left: 50%;
  width: 34px;
  height: 3px;
  transform: translateX(-50%);
  border-radius: 2px;
  background: linear-gradient(90deg, transparent, var(--gold), transparent);
}

.nav-item:active {
  background: rgba(255, 208, 100, 0.1);
}

.nav-icon {
  width: 22px;
  height: 22px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
