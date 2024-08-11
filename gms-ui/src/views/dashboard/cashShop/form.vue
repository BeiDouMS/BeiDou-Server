<template>
  <a-modal
    v-model:visible="visible"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title> 编辑商品 </template>
    <div>
      <a-form :model="formData">
        <a-form-item label="sn">
          {{ formData.sn }}
        </a-form-item>
        <a-form-item label="物品">
          <a-space>
            {{ formData.itemId }}
            <img :src="getIconUrl('item', formData.itemId)" />
          </a-space>
        </a-form-item>
        <a-form-item label="数量">
          <a-input-number v-model="formData.count" />
          <template #extra> wz默认值 {{ tempData.defaultCount }} </template>
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="formData.price" />
          <template #extra> wz默认值 {{ tempData.defaultPrice }} </template>
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model="formData.priority" />
          <template #extra> wz默认值 {{ tempData.defaultPriority }} </template>
        </a-form-item>
        <a-form-item label="有效期">
          <a-input-number v-model="formData.period" />
          <template #extra> wz默认值 {{ tempData.defaultPeriod }} </template>
        </a-form-item>
        <a-form-item label="状态">
          <a-switch
            v-model="formData.onSale"
            type="round"
            :checked-value="1"
            :unchecked-value="0"
          >
            <template #checked> 上架中 </template>
            <template #unchecked> 待售 </template>
          </a-switch>
          <template #extra>
            wz默认值 {{ tempData.defaultOnSale ? '上架中' : '待售' }}
          </template>
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { cashShopState } from '@/store/modules/cashShop/type';
  import { cashShopFormState, offSale, onSale } from '@/api/cashShop';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';
  import { getIconUrl } from '@/utils/mapleStoryAPI';

  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const formData = ref<cashShopFormState>({ sn: -1, itemId: -1 });
  const tempData = ref<cashShopState>({ sn: -1, itemId: -1 });

  const emit = defineEmits(['loadData']);
  const handleBeforeOk = async () => {
    setLoading(true);
    try {
      if (formData.value.onSale) await onSale(formData.value);
      else await offSale(formData.value);
      visible.value = false;
      Message.success('更新成功！');
      emit('loadData');
    } finally {
      setLoading(false);
    }
  };
  const handleCancel = () => {
    visible.value = false;
  };

  const initForm = (data: cashShopState) => {
    tempData.value = data;
    formData.value = {
      sn: data.sn,
      itemId: data.itemId,
      count: data.count,
      price: data.price,
      // bonus: data.bonus,
      priority: data.priority,
      period: data.period,
      // maplePoint: data.maplePoint,
      // meso: data.meso,
      // forPremiumUser: data.forPremiumUser,
      // commodityGender: data.commodityGender,
      onSale: data.onSale ? 1 : 0,
      // clz: data.clz,
      // limit: data.limit,
      // pbCash: data.pbCash,
      // pbPoint: data.pbPoint,
      // pbGift: data.pbGift,
      // packageSn: data.packageSn,
    };
    visible.value = true;
  };
  defineExpose({ initForm });
</script>

<script lang="ts">
  export default {
    name: 'CashShopForm',
  };
</script>
