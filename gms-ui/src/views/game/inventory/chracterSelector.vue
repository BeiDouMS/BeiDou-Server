<script setup lang="ts">
  import { ref } from 'vue';

  const visible = ref(false);
  const openSelector = () => {
    visible.value = true;
  };
  const handleOk = () => {
    visible.value = false;
  };
  const handleCancel = () => {
    visible.value = false;
  };
</script>

<script lang="ts">
  export default {
    name: 'CharacterSelector',
  };
</script>

<template>
  <a-space>
    <a-input-search
      placeholder="选择玩家"
      readonly="true"
      :search-button="true"
      size="large"
      @search="openSelector"
    >
      <template #button-icon>
        <icon-more />
      </template>
    </a-input-search>
  </a-space>
  <a-modal
    v-model:visible="visible"
    width="auto"
    @ok="handleOk"
    @cancel="handleCancel"
  >
    <template #title>
      <a-space>
        <a-input placeholder="请输入角色id" />
        <a-input placeholder="请输入角色名" />
        <a-button type="primary">查询</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<style scoped lang="less"></style>
