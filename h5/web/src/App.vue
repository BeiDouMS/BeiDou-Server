<script setup>
import { ref } from 'vue';
import LoginView from './views/LoginView.vue';
import CharactersView from './views/CharactersView.vue';
import HangupView from './views/HangupView.vue';
import CharacterInfoView from './views/CharacterInfoView.vue';
import { api, clearSession, getSavedCharacter, getToken, saveCharacter } from './api';

const view = ref(getToken() ? 'characters' : 'login');
const character = ref(getSavedCharacter());

function onLogin() {
  view.value = 'characters';
}

function onSelect(chr) {
  character.value = chr;
  saveCharacter(chr);
  view.value = 'hangup';
}

function onCharacterChange(chr) {
  character.value = chr;
  saveCharacter(chr);
}

function onBack() {
  view.value = 'characters';
  saveCharacter(null);
}

function onOpenInfo() {
  view.value = 'info';
}

function onCloseInfo() {
  view.value = 'hangup';
}

async function onLogout() {
  try {
    await api.logout();
  } catch {
    // 忽略退出接口错误，本地清理即可
  }
  clearSession();
  character.value = null;
  view.value = 'login';
}
</script>

<template>
  <LoginView v-if="view === 'login'" @success="onLogin" />
  <CharactersView
    v-else-if="view === 'characters'"
    @select="onSelect"
    @logout="onLogout"
  />
  <HangupView
    v-else-if="view === 'hangup' && character"
    :character="character"
    @back="onBack"
    @open-info="onOpenInfo"
    @character-change="onCharacterChange"
  />
  <CharacterInfoView
    v-else-if="view === 'info' && character"
    :character="character"
    @back="onCloseInfo"
  />
</template>
