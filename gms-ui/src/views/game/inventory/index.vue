<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.inventory')">
      <character-selector @use-character="useCharacter" />
      <a-tabs
        :default-active-key="1"
        lazy-load
        destroy-on-hide
        :active-key="tab"
        @change="tabChange"
      >
        <a-tab-pane
          v-for="data in typeList"
          :key="data.inventoryType"
          :title="data.name"
        >
          <inventory-list
            :character-id="currentCid"
            :current-type="currentType"
          />
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
  import CharacterSelector from '@/views/game/inventory/characterSelector.vue';

  const typeList = ref<InventoryTypeState[]>([]);
  const currentType = ref<string | number>(1);
  const currentCid = ref<number | undefined>(undefined);

  const loadType = async () => {
    const { data } = await getInventoryTypeList();
    typeList.value = data;
  };

  loadType();

  const tab = ref<number>(0);
  const tabChange = (t: string | number) => {
    currentType.value = t;
    tab.value = t as number;
  };

  const useCharacter = (cid: number) => {
    currentCid.value = cid;
    tabChange(0);
  };
</script>

<script lang="ts">
  export default {
    name: 'Inventory',
  };
</script>

<style lang="less" scoped></style>
