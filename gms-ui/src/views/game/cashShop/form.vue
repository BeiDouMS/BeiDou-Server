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
            <img :src="getIconUrl('item', formData.itemId)" alt="" />
          </a-space>
        </a-form-item>
        <a-form-item label="数量">
          <a-input-number v-model="formData.count" />
          <template v-if="tempData.defaultCount" #extra>
            wz默认值 {{ tempData.defaultCount }}
          </template>
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="formData.price" />
          <template v-if="tempData.defaultPrice" #extra>
            wz默认值 {{ tempData.defaultPrice }}
          </template>
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model="formData.priority" />
          <template v-if="tempData.defaultPriority" #extra>
            wz默认值 {{ tempData.defaultPriority }}
          </template>
        </a-form-item>
        <a-form-item label="有效期">
          <a-input-number v-model="formData.period" />
          <template v-if="tempData.defaultPeriod" #extra>
            wz默认值 {{ tempData.defaultPeriod }}
          </template>
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
        <a-form-item label="Bonus">
          <a-input-number v-model="formData.bonus" />
          <template v-if="tempData.defaultBonus" #extra>
            wz默认值 {{ tempData.defaultBonus }}
          </template>
        </a-form-item>
        <a-form-item label="抵用券">
          <a-input-number v-model="formData.maplePoint" />
          <template v-if="tempData.defaultMaplePoint" #extra>
            wz默认值 {{ tempData.defaultMaplePoint }}
          </template>
        </a-form-item>
        <a-form-item label="金币">
          <a-input-number v-model="formData.meso" />
          <template v-if="tempData.defaultMeso" #extra>
            wz默认值 {{ tempData.defaultMeso }}
          </template>
        </a-form-item>
        <a-form-item label="PremiumUser">
          <a-input-number v-model="formData.forPremiumUser" />
          <template v-if="tempData.defaultForPremiumUser" #extra>
            wz默认值 {{ tempData.defaultForPremiumUser }}
          </template>
        </a-form-item>
        <a-form-item label="性别">
          <a-select v-model="formData.commodityGender">
            <a-option :value="0">男</a-option>
            <a-option :value="1">女</a-option>
            <a-option :value="2">通用</a-option>
          </a-select>
          <template #extra>
            wz默认值
            {{
              tempData.defaultGender === 0
                ? '男'
                : tempData.defaultGender === 1
                ? '女'
                : tempData.defaultGender === 2
                ? '通用'
                : ''
            }}
          </template>
        </a-form-item>
        <a-form-item label="标签">
          <a-select v-model="formData.clz" allow-clear>
            <a-option :value="0">NEW</a-option>
            <a-option :value="1">SALE</a-option>
            <a-option :value="2">HOT</a-option>
            <a-option :value="3">EVENT</a-option>
          </a-select>
          <template v-if="tempData.defaultClz" #extra>
            wz默认值
            {{
              tempData.defaultClz === 0
                ? 'NEW'
                : tempData.defaultClz === 1
                ? 'SALE'
                : tempData.defaultClz === 2
                ? 'HOT'
                : tempData.defaultClz === 3
                ? 'EVENT'
                : ''
            }}
          </template>
        </a-form-item>
        <a-form-item label="Limit">
          <a-input-number v-model="formData.limit" />
          <template v-if="tempData.defaultLimit" #extra>
            wz默认值 {{ tempData.defaultLimit }}
          </template>
        </a-form-item>
        <a-form-item label="pbCash">
          <a-input-number v-model="formData.pbCash" />
          <template v-if="tempData.defaultPBCash" #extra>
            wz默认值 {{ tempData.defaultPBCash }}
          </template>
        </a-form-item>
        <a-form-item label="pbPoint">
          <a-input-number v-model="formData.pbPoint" />
          <template v-if="tempData.defaultPBPoint" #extra>
            wz默认值 {{ tempData.defaultPBPoint }}
          </template>
        </a-form-item>
        <a-form-item label="pbGift">
          <a-input-number v-model="formData.pbGift" />
          <template v-if="tempData.defaultPBGift" #extra>
            wz默认值 {{ tempData.defaultPBGift }}
          </template>
        </a-form-item>
        <a-form-item label="packageSn">
          <a-input-number v-model="formData.packageSn" />
          <template v-if="tempData.defaultPackageSn" #extra>
            wz默认值 {{ tempData.defaultPackageSn }}
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
      bonus: data.bonus,
      priority: data.priority,
      period: data.period,
      maplePoint: data.maplePoint,
      meso: data.meso,
      forPremiumUser: data.forPremiumUser,
      commodityGender: data.gender,
      onSale: data.onSale ? 1 : 0,
      clz: data.clz,
      limit: data.limit,
      pbCash: data.pbCash,
      pbPoint: data.pbPoint,
      pbGift: data.pbGift,
      packageSn: data.packageSn,
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
