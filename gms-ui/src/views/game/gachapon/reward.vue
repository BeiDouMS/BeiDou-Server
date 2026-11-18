<template>
  <a-modal v-model:visible="visible" :width="1000" :ok-loading="loading">
    <template #title> {{ title }} </template>
    <a-row>
      <a-col>
        <a-space>
          <a-button type="primary" @click="insertClick">
            {{ $t('gachapon.reward.button.create') }}
          </a-button>
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
          :width="50"
          cell-class="td-nowrap"
        />
        <a-table-column
          title=" ID "
          data-index="id"
          :width="80"
          align="center"
          cell-class="td-nowrap"
        />
        <a-table-column
          :title="$t('gachapon.reward.column.poolId')"
          data-index="poolId"
          :width="80"
          align="center"
        />
        <a-table-column
          :title="$t('gachapon.reward.column.itemId')"
          :width="100"
          align="center"
        >
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.itemId }}</span>
            <a-input-number v-else v-model="record.itemId" />
          </template>
        </a-table-column>
        <a-table-column
          :title="$t('gachapon.reward.column.itemName')"
          :width="140"
          align="center"
        >
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.itemName }}</span>
            <a-input-number v-else v-model="record.itemId" />
          </template>
        </a-table-column>
        <a-table-column
          :title="$t('gachapon.reward.column.itemIcon')"
          :width="90"
          align="center"
        >
          <template #cell="{ record }">
            <img :src="getIconUrl('item', record.itemId)" alt="" />
          </template>
        </a-table-column>
        <a-table-column
          :title="$t('gachapon.reward.column.quantity')"
          :width="80"
          align="center"
        >
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.quantity }}</span>
            <a-input-number v-else v-model="record.quantity" />
          </template>
        </a-table-column>
        <a-table-column
          :title="$t('gachapon.reward.column.comment')"
          :width="220"
          align="center"
        >
          <template #cell="{ record }">
            <span v-if="editId !== record.id"> {{ record.comment }}</span>
            <a-input v-else v-model="record.comment" />
          </template>
        </a-table-column>
        <a-table-column :title="$t('gachapon.reward.column.operate')">
          <template #cell="{ record }">
            <a-button
              v-if="editId !== record.id"
              type="text"
              size="mini"
              @click="editId = record.id"
            >
              {{ $t('button.edit') }}
            </a-button>
            <a-popconfirm
              v-if="editId !== record.id"
              type="error"
              :content="$t('gachapon.reward.deleteTips')"
              @ok="deleteClick(record)"
            >
              <a-button size="mini" status="danger" type="text">
                {{ $t('button.delete') }}
              </a-button>
            </a-popconfirm>
            <a-button
              v-if="editId === record.id"
              type="text"
              size="mini"
              @click="saveClick(record)"
            >
              {{ $t('button.save') }}
            </a-button>
            <a-button
              v-if="editId === record.id"
              type="text"
              size="mini"
              @click="editId = -1"
            >
              {{ $t('button.cancel') }}
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
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const title = ref<string>(t('gachapon.reward.title'));

  const curPool = ref<GachaponPoolState>({});
  const initForm = (data: GachaponPoolState) => {
    title.value = t('gachapon.reward.titleWithPool', { name: data.name });
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
      Message.success(t('gachapon.message.rewardSaved'));
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
      Message.success(t('gachapon.message.rewardDeleted'));
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
