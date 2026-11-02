<script setup>
import { ref } from 'vue';
import { api, setSession } from '../api';

const emit = defineEmits(['success']);

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');
const logoUrl = '/assets/ui/Logo/Wizet/44.png';

async function login() {
  if (loading.value) return;
  error.value = '';
  if (!username.value.trim() || !password.value) {
    error.value = '请输入账号和密码';
    return;
  }
  loading.value = true;
  try {
    const data = await api.login(username.value.trim(), password.value);
    setSession(data.token, data.account);
    emit('success', data.account);
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login">
    <div class="logo-wrap">
      <img class="logo pixel-img" :src="logoUrl" alt="Wizet" />
      <div class="title">北斗冒险岛</div>
      <div class="subtitle">H5 挂机</div>
    </div>

    <form class="card form" @submit.prevent="login">
      <label class="field">
        <span>账号</span>
        <input v-model="username" type="text" autocomplete="username" placeholder="请输入账号" />
      </label>
      <label class="field">
        <span>密码</span>
        <input v-model="password" type="password" autocomplete="current-password" placeholder="请输入密码" />
      </label>

      <div v-if="error" class="error">{{ error }}</div>

      <button class="btn block" type="submit" :disabled="loading">
        {{ loading ? '登录中…' : '登录' }}
      </button>
      <p class="hint muted">与端游数据互通，收益由服务端结算</p>
    </form>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  padding: 42px 20px calc(24px + var(--safe-bottom));
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 26px;
}

.logo-wrap {
  text-align: center;
}

.logo {
  width: 132px;
  height: auto;
  filter: drop-shadow(0 6px 20px rgba(90, 210, 255, 0.35));
}

.title {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 3px;
  color: #fff;
  text-shadow: 0 2px 0 #12306b, 0 4px 18px rgba(90, 210, 255, 0.45);
}

.subtitle {
  margin-top: 4px;
  font-size: 12px;
  letter-spacing: 6px;
  color: var(--cyan);
  text-transform: uppercase;
}

.form {
  padding: 20px 18px 18px;
}

.field {
  display: block;
  margin-bottom: 14px;
}

.field > span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--text-dim);
}

.field > input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border-radius: 6px;
  border: 1px solid var(--line);
  background: rgba(8, 14, 28, 0.75);
  font-size: 15px;
  transition: border-color 0.15s ease;
}

.field > input:focus {
  border-color: #6fa8ff;
  box-shadow: 0 0 0 2px rgba(79, 141, 255, 0.2);
}

.error {
  margin: 0 0 12px;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 13px;
  color: #ffd7d7;
  background: rgba(255, 107, 107, 0.12);
  border: 1px solid rgba(255, 107, 107, 0.4);
}

.hint {
  margin: 14px 0 0;
  font-size: 11px;
  text-align: center;
}
</style>
