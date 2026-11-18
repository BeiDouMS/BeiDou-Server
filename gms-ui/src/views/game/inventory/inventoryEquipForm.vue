<template>
  <a-modal
    v-model:visible="visible"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    @cancel="handleCancel"
  >
    <template #title> {{ $t('inventoryEquipForm.title') }} </template>
    <div>
      <a-form :model="formData">
        <a-form-item :label="$t('inventoryEquipForm.inventoryEquipmentId')">
          {{ formData.inventoryEquipment.id }}
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.inventoryItemId')">
          {{ formData.inventoryEquipment.inventoryItemId }}
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.upgradeSlots')">
          <a-input-number v-model="formData.inventoryEquipment.upgradeSlots" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.level')">
          <a-input-number v-model="formData.inventoryEquipment.level" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.attStr')">
          <a-input-number v-model="formData.inventoryEquipment.attStr" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.attDex')">
          <a-input-number v-model="formData.inventoryEquipment.attDex" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.attInt')">
          <a-input-number v-model="formData.inventoryEquipment.attInt" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.attLuk')">
          <a-input-number v-model="formData.inventoryEquipment.attLuk" />
        </a-form-item>
        <a-form-item label="HP">
          <a-input-number v-model="formData.inventoryEquipment.hp" />
        </a-form-item>
        <a-form-item label="MP">
          <a-input-number v-model="formData.inventoryEquipment.mp" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.patk')">
          <a-input-number v-model="formData.inventoryEquipment.patk" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.matk')">
          <a-input-number v-model="formData.inventoryEquipment.matk" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.pdef')">
          <a-input-number v-model="formData.inventoryEquipment.pdef" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.mdef')">
          <a-input-number v-model="formData.inventoryEquipment.mdef" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.acc')">
          <a-input-number v-model="formData.inventoryEquipment.acc" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.avoid')">
          <a-input-number v-model="formData.inventoryEquipment.avoid" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.hands')">
          <a-input-number v-model="formData.inventoryEquipment.hands" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.speed')">
          <a-input-number v-model="formData.inventoryEquipment.speed" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.jump')">
          <a-input-number v-model="formData.inventoryEquipment.jump" />
        </a-form-item>
        <a-form-item label="locked">
          <a-input-number v-model="formData.inventoryEquipment.locked" />
        </a-form-item>
        <a-form-item label="vicious">
          <a-input-number v-model="formData.inventoryEquipment.vicious" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.itemLevel')">
          <a-input-number v-model="formData.inventoryEquipment.itemLevel" />
        </a-form-item>
        <a-form-item :label="$t('inventoryEquipForm.itemExp')">
          <a-input-number v-model="formData.inventoryEquipment.itemExp" />
        </a-form-item>
        <a-form-item label="ringId">
          <a-input-number v-model="formData.inventoryEquipment.ringId" />
        </a-form-item>
      </a-form>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';
  import { InventoryState } from '@/store/modules/inventory/type';
  import { updateInventory } from '@/api/inventory';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { setLoading, loading } = useLoading(false);
  const visible = ref<boolean>(false);
  const formData = ref<InventoryState>({
    id: undefined,
    characterId: undefined,
    itemId: undefined,
    itemType: undefined,
    inventoryType: undefined,
    position: undefined,
    quantity: undefined,
    owner: undefined,
    petId: undefined,
    flag: undefined,
    expiration: undefined,
    giftFrom: undefined,
    online: undefined,
    equipment: undefined,
    inventoryEquipment: {
      id: -1,
      inventoryItemId: 0,
      upgradeSlots: 0,
      level: 0,
      attStr: 0,
      attDex: 0,
      attInt: 0,
      attLuk: 0,
      hp: 0,
      mp: 0,
      patk: 0,
      matk: 0,
      pdef: 0,
      mdef: 0,
      acc: 0,
      avoid: 0,
      hands: 0,
      speed: 0,
      jump: 0,
      locked: 0,
      vicious: 0,
      itemLevel: 0,
      itemExp: 0,
      ringId: 0,
    },
  });

  const emit = defineEmits(['loadData']);
  const handleBeforeOk = async () => {
    setLoading(true);
    try {
      await updateInventory(formData.value);
      visible.value = false;
      Message.success(t('inventory.message.updateSuccess'));
      emit('loadData');
    } finally {
      setLoading(false);
    }
  };
  const handleCancel = () => {
    visible.value = false;
  };

  const initForm = (data: InventoryState) => {
    formData.value = data;
    visible.value = true;
  };
  defineExpose({ initForm });
</script>

<script lang="ts">
  export default {
    name: 'InventoryEquipForm',
  };
</script>
