<template>
  <a-modal
    v-model:visible="visible"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    :width="600"
    @cancel="handleCancel"
  >
    <template #title> {{ title }} </template>
    <div>
      <a-form :model="formData">
        <a-form-item label="奖池ID">
          {{ formData.id }}
        </a-form-item>
        <a-form-item label="公共池">
          <a-switch v-model="formData.isPublic">
            <template #checked-icon>
              <icon-check />
            </template>
            <template #unchecked-icon>
              <icon-close />
            </template>
          </a-switch>
        </a-form-item>
        <a-form-item label="奖池名称">
          <a-input v-model="formData.name" />
        </a-form-item>
        <a-form-item v-if="!formData.isPublic" label="百宝箱ID">
          <a-input-number v-model="formData.gachaponId" />
        </a-form-item>
        <a-form-item
          v-if="!formData.isPublic && tableData.length > 0"
          label="概率"
        >
          <a-slider
            v-model="formData.weight"
            :min="0"
            :max="10000"
            :format-tooltip="formatter"
          />
        </a-form-item>
        <a-form-item v-if="!formData.isPublic" label="权重">
          <a-input-number
            v-model="formData.weight"
            :max="10000"
            :min="0"
            :precision="0"
          />
        </a-form-item>
        <a-form-item v-if="formData.isPublic" label="固定中奖率">
          <a-space>
            <a-input-number
              v-model="formData.prob"
              :style="{ width: '320px' }"
              placeholder="万分之"
              allow-clear
              :precision="0"
              hide-button
            >
              <template #suffix> /10000 </template>
            </a-input-number>
            {{ formData.prob === undefined ? 0 : formData.prob / 100 }} %
          </a-space>
        </a-form-item>
        <a-form-item label="生效时间">
          <a-date-picker
            v-model="formData.startTime"
            style="width: 220px; margin: 0 24px 24px 0"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
          />
        </a-form-item>
        <a-form-item label="结束时间">
          <a-date-picker
            v-model="formData.endTime"
            style="width: 220px; margin: 0 24px 24px 0"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
          />
        </a-form-item>
        <a-form-item label="全服通知">
          <a-switch v-model="formData.notification">
            <template #checked-icon>
              <icon-check />
            </template>
            <template #unchecked-icon>
              <icon-close />
            </template>
          </a-switch>
        </a-form-item>
        <a-form-item label="备注">
          <a-input
            v-model="formData.comment"
            :max-length="255"
            show-word-limit
          />
        </a-form-item>
        <a-table
          v-if="tableData.length > 0"
          row-key="id"
          :loading="loading"
          :data="tableData"
          :pagination="false"
          :bordered="{ cell: true }"
        >
          <template #columns>
            <a-table-column
              title="ID"
              data-index="id"
              :width="80"
              align="center"
            />
            <a-table-column
              title="名称"
              data-index="name"
              align="center"
              :width="180"
            />
            <a-table-column title="公共池" align="center" :width="100">
              <template #cell="{ record }">
                <a-tag v-if="record.isPublic" color="red">公共池</a-tag>
                <a-tag v-else color="blue">非公共池</a-tag>
              </template>
            </a-table-column>
            <a-table-column
              title="权重"
              data-index="weight"
              align="center"
              :width="100"
            />
            <a-table-column title="真实概率" align="center" :width="90">
              <template #cell="{ record }">
                {{ record.realProb / 10000 }} %
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-form>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import { ref } from 'vue';
  import { GachaponPoolState } from '@/store/modules/gachapon/type';
  import {
    GachaponPoolSearchCondition,
    getPools,
    updatePool,
  } from '@/api/gachapon';
  import { Message } from '@arco-design/web-vue';

  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const formData = ref<GachaponPoolState>({});
  const title = ref<string>('创建奖池');

  const emit = defineEmits(['loadData']);
  const handleBeforeOk = async () => {
    setLoading(true);
    try {
      await updatePool(formData.value);
      visible.value = false;
      Message.success('奖池已保存');
      emit('loadData');
    } finally {
      setLoading(false);
    }
  };
  const handleCancel = () => {
    visible.value = false;
  };
  const condition = ref<GachaponPoolSearchCondition>({
    gachaponId: undefined,
    pageNo: 1,
    pageSize: 9999,
  });
  const tableData = ref<GachaponPoolState[]>([]);
  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getPools(condition.value);
      tableData.value = data.records.filter((_data: GachaponPoolState) => {
        return _data.id !== formData.value.id;
      });
    } finally {
      setLoading(false);
    }
  };

  const initForm = (data: GachaponPoolState) => {
    tableData.value = [];
    if (data) {
      formData.value = {
        id: data.id,
        name: data.name,
        gachaponId: data.gachaponId,
        weight: data.weight,
        isPublic: data.isPublic,
        prob: data.prob,
        startTime: data.startTime,
        endTime: data.endTime,
        notification: data.notification,
        realProb: data.realProb,
        comment: data.comment,
      };
      title.value = '编辑奖池';
      if (data.gachaponId !== -1) {
        condition.value.gachaponId = data.gachaponId;
        loadData();
      }
    } else {
      formData.value = {
        id: undefined,
        name: undefined,
        gachaponId: undefined,
        weight: undefined,
        isPublic: false,
        prob: undefined,
        startTime: undefined,
        endTime: undefined,
        notification: false,
        realProb: undefined,
        comment: undefined,
      };
      title.value = '创建奖池';
    }
    visible.value = true;
  };
  defineExpose({ initForm });

  const calcRealProb = (weight: number) => {
    let probTotal = 0;
    let totalWeight = 0;
    for (let i = 0; i < tableData.value.length; i += 1) {
      probTotal += tableData.value[i].prob || 0;
      totalWeight += tableData.value[i].weight || 0;
    }

    totalWeight += formData.value.weight || 0;

    const probPoint = 100 * probTotal;
    const weightPoint = 1000000 - probPoint;

    tableData.value.forEach((_data: GachaponPoolState) => {
      if (_data.isPublic) {
        _data.realProb = (_data.prob || 0) * 100;
      } else {
        _data.realProb = Math.round(
          (weightPoint * (_data.weight || 0)) / totalWeight
        );
      }
    });

    for (let i = 0; i < tableData.value.length; i += 1) {
      if (tableData.value[i].isPublic) {
        tableData.value[i].realProb = (tableData.value[i].prob || 0) * 100;
      } else {
        tableData.value[i].realProb = Math.round(
          (weightPoint * (tableData.value[i].weight || 0)) / totalWeight
        );
      }
    }

    return Math.round((weightPoint * weight) / totalWeight);
  };

  const formatter = (weight: number) => {
    const realProb = calcRealProb(weight);
    return `权重: ${weight} 概率 ${realProb / 10000} %`;
  };
</script>

<script lang="ts">
  export default {
    name: 'GachaponForm',
  };
</script>

<style lang="less" scoped></style>
