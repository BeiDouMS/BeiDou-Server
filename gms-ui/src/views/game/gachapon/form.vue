<template>
  <a-modal
    v-model:visible="visible"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    :width="600"
    @cancel="handleCancel"
  >
    <template #title> {{ $t(titleKey) }} </template>
    <div>
      <a-form :model="formData">
        <a-form-item :label="$t('gachapon.form.poolId')">
          {{ formData.id }}
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.isPublic')">
          <a-switch v-model="formData.isPublic">
            <template #checked-icon>
              <icon-check />
            </template>
            <template #unchecked-icon>
              <icon-close />
            </template>
          </a-switch>
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.name')">
          <a-input v-model="formData.name" />
        </a-form-item>
        <a-form-item
          v-if="!formData.isPublic"
          :label="$t('gachapon.form.gachaponId')"
        >
          <a-input-number v-model="formData.gachaponId" />
        </a-form-item>
        <a-form-item
          v-if="!formData.isPublic && tableData.length > 0"
          :label="$t('gachapon.form.prob')"
        >
          <a-slider
            v-model="formData.weight"
            :min="0"
            :max="10000"
            :format-tooltip="formatter"
          />
        </a-form-item>
        <a-form-item
          v-if="!formData.isPublic"
          :label="$t('gachapon.form.weight')"
        >
          <a-input-number
            v-model="formData.weight"
            :max="10000"
            :min="0"
            :precision="0"
          />
        </a-form-item>
        <a-form-item
          v-if="formData.isPublic"
          :label="$t('gachapon.form.fixedProb')"
        >
          <a-space>
            <a-input-number
              v-model="formData.prob"
              :style="{ width: '320px' }"
              :placeholder="$t('gachapon.form.probPlaceholder')"
              allow-clear
              :precision="0"
              hide-button
            >
              <template #suffix> /10000 </template>
            </a-input-number>
            {{ formData.prob === undefined ? 0 : formData.prob / 100 }} %
          </a-space>
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.startTime')">
          <a-date-picker
            v-model="formData.startTime"
            style="width: 220px; margin: 0 24px 24px 0"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
          />
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.endTime')">
          <a-date-picker
            v-model="formData.endTime"
            style="width: 220px; margin: 0 24px 24px 0"
            show-time
            format="YYYY-MM-DD HH:mm:ss"
          />
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.notification')">
          <a-switch v-model="formData.notification">
            <template #checked-icon>
              <icon-check />
            </template>
            <template #unchecked-icon>
              <icon-close />
            </template>
          </a-switch>
        </a-form-item>
        <a-form-item :label="$t('gachapon.form.comment')">
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
              :title="$t('gachapon.form.column.id')"
              data-index="id"
              :width="80"
              align="center"
            />
            <a-table-column
              :title="$t('gachapon.form.column.name')"
              data-index="name"
              align="center"
              :width="180"
            />
            <a-table-column
              :title="$t('gachapon.form.column.isPublic')"
              align="center"
              :width="100"
            >
              <template #cell="{ record }">
                <a-tag v-if="record.isPublic" color="red">
                  {{ $t('gachapon.isPublic.true.desc') }}
                </a-tag>
                <a-tag v-else color="blue">
                  {{ $t('gachapon.isPublic.false.desc') }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('gachapon.form.column.weight')"
              data-index="weight"
              align="center"
              :width="100"
            />
            <a-table-column
              :title="$t('gachapon.form.column.realProb')"
              align="center"
              :width="90"
            >
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
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const formData = ref<GachaponPoolState>({});
  const titleKey = ref<string>('gachapon.form.createTitle');

  const emit = defineEmits(['loadData']);
  const handleBeforeOk = async () => {
    setLoading(true);
    try {
      await updatePool(formData.value);
      visible.value = false;
      Message.success(t('gachapon.message.poolSaved'));
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
      titleKey.value = 'gachapon.form.editTitle';
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
      titleKey.value = 'gachapon.form.createTitle';
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
    return t('gachapon.form.tooltip', {
      weight,
      prob: realProb / 10000,
    });
  };
</script>

<script lang="ts">
  export default {
    name: 'GachaponForm',
  };
</script>

<style lang="less" scoped></style>
