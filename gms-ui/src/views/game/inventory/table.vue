<template>
  <a-table
    row-key="id"
    :loading="loading"
    :data="tableData"
    column-resizable
    :pagination="false"
    :bordered="{ cell: true }"
  >
    <template #columns>
      <a-table-column title="ID" data-index="id" align="center" :width="100" />
      <a-table-column title="角色ID" data-index="characterId" align="center" />
      <a-table-column title="在线" data-index="online" align="center">
        <template #cell="{ record }">
          <a-tag v-if="record.online" color="green">在线</a-tag>
          <a-tag v-else color="gray">离线</a-tag>
        </template>
      </a-table-column>
      <a-table-column title="物品ID" data-index="itemId" align="center" />
      <a-table-column title="物品" align="center">
        <template #cell="{ record }">
          <img :src="getIconUrl('item', record.itemId)" alt="" />
        </template>
      </a-table-column>
      <a-table-column title="类型" data-index="itemType" align="center" />
      <!--      <a-table-column-->
      <!--        title="inventoryType"-->
      <!--        data-index="inventoryType"-->
      <!--        align="center"-->
      <!--      />-->
      <a-table-column title="位置" data-index="position" align="center" />
      <a-table-column title="数量" align="center" :width="160">
        <template #cell="{ record }">
          <span v-if="editId !== record.id">
            {{ record.quantity }}
          </span>
          <a-input-number v-else v-model="record.quantity" />
        </template>
      </a-table-column>
      <a-table-column title="签名" data-index="owner" align="center" />
      <a-table-column title="宠物ID" data-index="petId" align="center" />
      <a-table-column title="Flag" data-index="flag" align="center" />
      <a-table-column title="赠送人" data-index="giftFrom" align="center" />
      <a-table-column title="到期时间" align="center">
        <template #cell="{ record }">
          <span v-if="editId !== record.id">
            {{ timestampToChineseTime(record.expiration) }}
          </span>
          <a-input-number v-else v-model="record.expiration" />
        </template>
      </a-table-column>
      <!--      <a-table-column title="装备" data-index="equipment" align="center" />-->
      <a-table-column title="操作" :width="200">
        <template #cell="{ record }">
          <a-button
            v-if="editId !== record.id"
            type="text"
            size="mini"
            @click="editClick(record)"
          >
            编辑
          </a-button>
          <a-button
            v-if="editId === record.id"
            type="text"
            size="mini"
            status="success"
            @click="saveClick(record)"
          >
            保存
          </a-button>
          <a-button
            v-if="editId === record.id"
            type="text"
            size="mini"
            @click="editId = undefined"
          >
            取消
          </a-button>
          <a-popconfirm
            v-if="editId !== record.id"
            type="error"
            content="你确定要删除这个道具吗？"
            @ok="deleteClick(record)"
          >
            <a-button type="text" size="mini" status="danger"> 删除 </a-button>
          </a-popconfirm>
        </template>
      </a-table-column>
    </template>
  </a-table>
  <inventory-equip-form ref="inventoryEquipFormRef" @load-data="loadData" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import {
    deleteInventory,
    getInventoryList,
    InventoryCondition,
    updateInventory,
  } from '@/api/inventory';
  import useLoading from '@/hooks/loading';
  import { InventoryState } from '@/store/modules/inventory/type';
  import { getIconUrl } from '@/utils/mapleStoryAPI';
  import InventoryEquipForm from '@/views/game/inventory/inventoryEquipForm.vue';
  import { timestampToChineseTime } from '@/utils/stringUtils';

  const { setLoading, loading } = useLoading(false);
  const tableData = ref<InventoryState[]>([]);

  const props = defineProps<{
    currentType: string | number;
    characterId: number | undefined;
  }>();
  const editId = ref<number | undefined>(undefined);

  const loadData = async () => {
    editId.value = undefined;
    // 可在选择完玩家后刷新
    if (!props || !props.characterId) {
      return;
    }
    setLoading(true);
    try {
      const condition: InventoryCondition = {
        inventoryType: props.currentType as number,
        characterId: props.characterId as number,
        pageNo: 1,
        pageSize: 20,
      };
      const { data } = await getInventoryList(condition);
      tableData.value = data;
    } finally {
      setLoading(false);
    }
  };
  loadData();

  const saveClick = async (data: InventoryState) => {
    setLoading(true);
    try {
      await updateInventory(data);
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const deleteClick = async (data: InventoryState) => {
    setLoading(true);
    try {
      await deleteInventory(data);
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const inventoryEquipFormRef = ref();
  const editClick = (data: InventoryState) => {
    if (data.equipment) {
      inventoryEquipFormRef.value.initForm(data);
    } else {
      editId.value = data.id;
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'InventoryList',
  };
</script>

<style lang="less" scoped></style>
