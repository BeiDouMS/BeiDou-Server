<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.inventory')">
      <a-tabs
        :default-active-key="1"
        lazy-load
        destroy-on-hide
        @change="tabChange"
      >
        <a-tab-pane
          v-for="data in typeList"
          :key="data.type"
          :title="data.name"
        >
          <inventory-list :current-type="currentType" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { getInventoryTypeList } from '@/api/inventory';
  import { InventoryTypeState } from '@/store/modules/inventory/type';
  import { ref } from 'vue';
  import InventoryList from '@/views/game/inventory/table.vue';

  const typeList = ref<InventoryTypeState[]>([]);
  const currentType = ref<string | number>(1);

  const loadType = async () => {
    const { data } = await getInventoryTypeList();
    typeList.value = data;
  };

  loadType();

  const tabChange = (tab: string | number) => {
    currentType.value = tab;
  };
</script>

<script lang="ts">
  export default {
    name: 'Inventory',
  };
</script>

<style lang="less" scoped></style>
