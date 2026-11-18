<template>
  <a-modal
    v-model:visible="visible"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title> {{ $t('cashShop.form.title') }} </template>
    <div>
      <a-form :model="formData">
        <a-form-item label="sn">
          {{ formData.sn }}
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.item')">
          <a-space>
            {{ formData.itemId }}
            <img :src="getIconUrl('item', formData.itemId)" alt="" />
          </a-space>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.count')">
          <a-input-number v-model="formData.count" />
          <template v-if="tempData.defaultCount" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultCount })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.price')">
          <a-input-number v-model="formData.price" />
          <template v-if="tempData.defaultPrice" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultPrice })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.priority')">
          <a-input-number v-model="formData.priority" />
          <template v-if="tempData.defaultPriority" #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: tempData.defaultPriority,
              })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.period')">
          <a-input-number v-model="formData.period" />
          <template v-if="tempData.defaultPeriod" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultPeriod })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.status')">
          <a-switch
            v-model="formData.onSale"
            type="round"
            :checked-value="1"
            :unchecked-value="0"
          >
            <template #checked>
              {{ $t('cashShop.column.onSale.true') }}
            </template>
            <template #unchecked>
              {{ $t('cashShop.column.onSale.false') }}
            </template>
          </a-switch>
          <template #extra>
            {{
              tempData.defaultOnSale
                ? $t('cashShop.column.onSale.true')
                : $t('cashShop.column.onSale.false')
            }}
          </template>
        </a-form-item>
        <a-form-item label="Bonus">
          <a-input-number v-model="formData.bonus" />
          <template v-if="tempData.defaultBonus" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultBonus })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.maplePoint')">
          <a-input-number v-model="formData.maplePoint" />
          <template v-if="tempData.defaultMaplePoint" #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: tempData.defaultMaplePoint,
              })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.meso')">
          <a-input-number v-model="formData.meso" />
          <template v-if="tempData.defaultMeso" #extra>
            {{ $t('cashShop.form.wzDefault', { value: tempData.defaultMeso }) }}
          </template>
        </a-form-item>
        <a-form-item label="PremiumUser">
          <a-input-number v-model="formData.forPremiumUser" />
          <template v-if="tempData.defaultForPremiumUser" #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: tempData.defaultForPremiumUser,
              })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.gender')">
          <a-select v-model="formData.commodityGender">
            <a-option :value="0">
              {{ $t('cashShop.column.gender.male') }}
            </a-option>
            <a-option :value="1">
              {{ $t('cashShop.column.gender.female') }}
            </a-option>
            <a-option :value="2">
              {{ $t('cashShop.column.gender.common') }}
            </a-option>
          </a-select>
          <template #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: genderText(tempData.defaultGender),
              })
            }}
          </template>
        </a-form-item>
        <a-form-item :label="$t('cashShop.form.clz')">
          <a-select v-model="formData.clz" allow-clear>
            <a-option :value="0">NEW</a-option>
            <a-option :value="1">SALE</a-option>
            <a-option :value="2">HOT</a-option>
            <a-option :value="3">EVENT</a-option>
          </a-select>
          <template v-if="tempData.defaultClz" #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: clzText(tempData.defaultClz),
              })
            }}
          </template>
        </a-form-item>
        <a-form-item label="Limit">
          <a-input-number v-model="formData.limit" />
          <template v-if="tempData.defaultLimit" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultLimit })
            }}
          </template>
        </a-form-item>
        <a-form-item label="pbCash">
          <a-input-number v-model="formData.pbCash" />
          <template v-if="tempData.defaultPBCash" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultPBCash })
            }}
          </template>
        </a-form-item>
        <a-form-item label="pbPoint">
          <a-input-number v-model="formData.pbPoint" />
          <template v-if="tempData.defaultPBPoint" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultPBPoint })
            }}
          </template>
        </a-form-item>
        <a-form-item label="pbGift">
          <a-input-number v-model="formData.pbGift" />
          <template v-if="tempData.defaultPBGift" #extra>
            {{
              $t('cashShop.form.wzDefault', { value: tempData.defaultPBGift })
            }}
          </template>
        </a-form-item>
        <a-form-item label="packageSn">
          <a-input-number v-model="formData.packageSn" />
          <template v-if="tempData.defaultPackageSn" #extra>
            {{
              $t('cashShop.form.wzDefault', {
                value: tempData.defaultPackageSn,
              })
            }}
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
  import { useI18n } from 'vue-i18n';
  import { getIconUrl } from '@/utils/mapleStoryAPI';

  const { t } = useI18n();
  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const formData = ref<cashShopFormState>({ sn: -1, itemId: -1 });
  const tempData = ref<cashShopState>({ sn: -1, itemId: -1 });

  const genderText = (gender?: number) => {
    if (gender === 0) return t('cashShop.column.gender.male');
    if (gender === 1) return t('cashShop.column.gender.female');
    if (gender === 2) return t('cashShop.column.gender.common');
    return '';
  };
  const clzText = (clz?: number) => {
    if (clz === 0) return 'NEW';
    if (clz === 1) return 'SALE';
    if (clz === 2) return 'HOT';
    if (clz === 3) return 'EVENT';
    return '';
  };

  const emit = defineEmits(['loadData']);
  const handleBeforeOk = async () => {
    setLoading(true);
    try {
      if (formData.value.onSale) await onSale(formData.value);
      else await offSale(formData.value);
      visible.value = false;
      Message.success(t('cashShop.message.updateSuccess'));
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
