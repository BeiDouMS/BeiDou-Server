<template>
  <a-modal v-model:visible="visible" :width="1000" :ok-loading="loading">
    <template #title> {{ title }} </template>
    <a-row>
      <a-col>
        <a-space>
          <a-button type="primary" @click="insertClick">新建</a-button>
        </a-space>
      </a-col>
    </a-row>
    <a-table
      row-key="id"
      :loading="loading"
      :data="tableData"
      column-resizable
      :pagination="{
        pageSizeOptions: [10, 20, 50],
        showPageSize: true,
        showJumper: true,
      }"
      :bordered="{ cell: true }"
    >
      <template #columns>
        <a-table-column
          title="#"
          data-index="index"
          align="center"
          :width=50
          cell-class="td-nowrap"
        />
        <a-table-column
          title=" ID "
          data-index="id"
          :width=80
          align="center"
          cell-class="td-nowrap"
        />
        <a-table-column
          title="奖池ID"
          data-index="poolId"
          :width="80"
          align="center"
        />
        <a-table-column title="物品ID" :width="100" align="center">
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.itemId }}</span>
            <a-input-number v-else v-model="record.itemId" />
          </template>
        </a-table-column>
        <a-table-column title="物品名称" :width="140" align="center">
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.itemName }}</span>
            <a-input-number v-else v-model="record.itemId" />
          </template>
        </a-table-column>
        <a-table-column title="物品图标" :width="90" align="center">
          <template #cell="{ record }">
            <img :src="getIconUrl('item', record.itemId)" alt="" />
          </template>
        </a-table-column>
        <a-table-column title="数量" :width="80" align="center">
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.quantity }}</span>
            <a-input-number v-else v-model="record.quantity" />
          </template>
        </a-table-column>
        <a-table-column title="备注" :width="220" align="center">
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.comment }}</span>
            <a-input v-else v-model="record.comment" />
          </template>
        </a-table-column>
        <a-table-column title="操作">
          <template #cell="{ record }">
            <a-button
              v-if="editId !== record.id"
              type="text"
              size="mini"
              @click="editId = record.id"
            >
              编辑
            </a-button>
            <a-popconfirm
              v-if="editId !== record.id"
              type="error"
              content="你确定要删除这个奖品吗？"
              @ok="deleteClick(record)"
            >
              <a-button size="mini" status="danger" type="text">
                删除
              </a-button>
            </a-popconfirm>
            <a-button
              v-if="editId === record.id"
              type="text"
              size="mini"
              @click="saveClick(record)"
            >
              保存
            </a-button>
            <a-button
              v-if="editId === record.id"
              type="text"
              size="mini"
              @click="editId = -1"
            >
              取消
            </a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-modal>
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import { getIconUrl } from '@/utils/mapleStoryAPI';
  import { ref } from 'vue';
  import {
    GachaponPoolState,
    GachaponRewardState,
  } from '@/store/modules/gachapon/type';
  import { deleteReward, getRewards, updateReward } from '@/api/gachapon';
  import { Message } from '@arco-design/web-vue';

  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const title = ref<string>('奖品列表');

  const curPool = ref<GachaponPoolState>({});
  const initForm = (data: GachaponPoolState) => {
    title.value = `[${data.name}] 奖品列表`;
    curPool.value = data;
    loadData();
    visible.value = true;
  };
  defineExpose({ initForm });

  const tableData = ref<GachaponRewardState[]>([]);
  const loadData = async () => {
    setLoading(true);
    editId.value = -1;
    try {
      const { data } = await getRewards(curPool.value);
      tableData.value = data.map((obj: any, i: number) => ({
        ...obj,
        index: i + 1,
      }));
    } finally {
      setLoading(false);
    }
  };

  const editId = ref<number | undefined>(-1);

  const saveClick = async (data: GachaponRewardState) => {
    setLoading(true);
    try {
      await updateReward(data);
      Message.success('奖品已保存');
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const insertClick = () => {
    tableData.value.unshift({ poolId: curPool.value.id, quantity: 1 });
    editId.value = undefined;
  };

  const deleteClick = async (data: GachaponRewardState) => {
    setLoading(true);
    try {
      await deleteReward(data);
      Message.success('奖品已删除');
      await loadData();
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'GachaponRewardForm',
  };
</script>

<style lang="less" scoped></style>
