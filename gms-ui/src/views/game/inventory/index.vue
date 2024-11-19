<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.inventory')">
      <div class="button-group">
        <character-selector @use-character="useCharacter" />
        <a-button
          type="primary"
          class="margin-left"
          :disabled="
            !currentCid ||
            currentType === 0 ||
            currentType === -1 ||
            currentType === 6
          "
          @click="openInventoryUI(currentCid, currentType)"
        >
          {{ $t('inventory.placeholder.inventoryDraw') }}
        </a-button>
      </div>
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

    <!-- 模态框 -->
    <a-modal
      v-model:visible="inventoryVisible"
      :title="`${
        currentCid && currentCName ? `[${currentCid}][${currentCName}] - ` : ''
      }${$t('inventory.placeholder.inventoryDraw')} (${$t(
        typeMap[currentType as keyof typeof typeMap]
      )})`"
      :width="800"
      :footer="false"
      :draggable="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <InventoryUI
        v-if="inventoryVisible"
        :character-id="currentCid || 0"
        :inventory-type="Number(currentType) || 1"
      />
      <div style="display: flex; justify-content: flex-end; margin-top: 16px">
        <a-button type="primary" @click="handleOk"
          >{{ $t('inventory.placeholder.confirm') }}
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
  import { getInventoryTypeList } from '@/api/inventory';
  import { InventoryTypeState } from '@/store/modules/inventory/type';
  import { ref } from 'vue';
  import InventoryList from '@/views/game/inventory/table.vue';
  import CharacterSelector from '@/views/game/inventory/characterSelector.vue';
  import InventoryUI from '@/views/game/inventory/InventoryUI.vue';

  const typeMap = {
    0: 'inventory.type.undefined',
    1: 'inventory.type.equipment',
    2: 'inventory.type.consumable',
    3: 'inventory.type.setting',
    4: 'inventory.type.other',
    5: 'inventory.type.cash',
    6: 'inventory.type.canPickup',
    '-1': 'inventory.type.equipped',
  };

  const typeList = ref<InventoryTypeState[]>([]);
  const currentType = ref<string | number>(1);
  const currentCid = ref<number | undefined>(undefined);
  const currentCName = ref<string | undefined>(undefined);
  const inventoryVisible = ref(false); // 控制模态框显隐

  const loadType = async () => {
    const { data } = await getInventoryTypeList();
    typeList.value = data;
  };

  loadType();

  const tab = ref<number>(0);
  const tabChange = (key: string | number) => {
    currentType.value = key;
    tab.value = key as number;
  };

  const useCharacter = (cid: number, cName: string) => {
    currentCid.value = cid;
    currentCName.value = cName; // 存储 cName
    tabChange(0);
  };

  // 打开 InventoryUI 的模态框，并传递参数
  const openInventoryUI = (
    characterId: number | undefined,
    inventoryType: string | number
  ) => {
    currentCid.value = characterId;
    currentType.value = inventoryType;
    inventoryVisible.value = true;
  };

  // 确定和取消方法
  const handleOk = () => {
    inventoryVisible.value = false;
  };

  const handleCancel = () => {
    inventoryVisible.value = false;
  };
</script>

<script lang="ts">
  export default {
    name: 'Inventory',
  };
</script>

<style lang="less" scoped>
  .button-group {
    display: flex;
    align-items: center;
    margin-bottom: 16px; /* 添加底部间距 */
  }

  .margin-left {
    margin-left: 16px; /* 调整此值以增加按钮之间的间距 */
  }
</style>
