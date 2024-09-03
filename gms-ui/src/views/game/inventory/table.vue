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
      <a-table-column title="数量" data-index="quantity" align="center" />
      <a-table-column title="签名" data-index="owner" align="center" />
      <a-table-column title="宠物ID" data-index="petId" align="center" />
      <a-table-column title="Flag" data-index="flag" align="center" />
      <a-table-column title="赠送人" data-index="giftFrom" align="center" />
      <a-table-column title="到期时间" align="center">
        <template #cell="{ record }">
          {{ timestampToChineseTime(record.expiration) }}
        </template>
      </a-table-column>
      <!--      <a-table-column title="装备" data-index="equipment" align="center" />-->
    </template>
  </a-table>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { getInventoryList, inventoryCondition } from '@/api/inventory';
  import useLoading from '@/hooks/loading';
  import { InventoryState } from '@/store/modules/inventory/type';
  import { getIconUrl } from '@/utils/mapleStoryAPI';

  const { setLoading, loading } = useLoading(false);
  const tableData = ref<InventoryState[]>([]);

  const props = defineProps<{
    currentType: string | number;
    characterId: number | undefined;
  }>();

  const loadData = async () => {
    // 可在选择完玩家后刷新
    if (!props || !props.characterId) {
      return;
    }
    setLoading(true);
    try {
      const condition: inventoryCondition = {
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

  const timestampToChineseTime = (timestamp: number) => {
    if (timestamp === -1) return '永久';
    // 创建一个 Date 对象，传入毫秒时间戳
    const date = new Date(timestamp);

    // 获取年、月、日、时、分、秒
    const year = date.getFullYear();
    const month = date.getMonth() + 1; // 月份从0开始，所以加1
    const day = date.getDate();
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const seconds = date.getSeconds();

    // 格式化输出为中文时间字符串
    return `${year}年${month}月${day}日 ${hours}时${minutes}分${seconds}秒`;
  };
</script>

<script lang="ts">
  export default {
    name: 'InventoryList',
  };
</script>

<style lang="less" scoped></style>
