<template>
  <a-config-provider>
    <router-view />
    <global-setting />
  </a-config-provider>
</template>

<script lang="ts" setup>
  import GlobalSetting from '@/components/global-setting/index.vue';
</script>

<style lang="less">
  .container {
    padding: 0 20px 20px 20px;
  }

  :deep(.arco-table-th) {
    &:last-child {
      .arco-table-th-item-title {
        margin-left: 16px;
      }
    }
  }

  .arco-row {
    margin-bottom: 10px;
  }
</style>
