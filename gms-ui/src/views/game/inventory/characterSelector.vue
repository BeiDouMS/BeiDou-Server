<script setup lang="ts">
  import { ref } from 'vue';
  import { getCharacterList, InventoryCondition } from '@/api/inventory';

  const condition = ref<InventoryCondition>({
    characterId: undefined,
    characterName: undefined,
    pageNo: 1,
    pageSize: 20,
  });

  const ccId = ref<number | undefined>(undefined);
  const ccName = ref<string | undefined>(undefined);

  const visible = ref(false);
  const openSelector = () => {
    visible.value = true;
  };

  const tableData = ref<any>([]);

  const searchClick = async () => {
    const { data } = await getCharacterList(condition.value);
    tableData.value = data.records;
  };

  const emit = defineEmits(['useCharacter']);
  const selectClick = (cid: number, cName: string) => {
    ccId.value = cid;
    ccName.value = cName;
    emit('useCharacter', cid);
    visible.value = false;
  };
</script>

<script lang="ts">
  export default {
    name: 'CharacterSelector',
  };
</script>

<template>
  <a-space>
    <a-input-search
      :placeholder="
        ccId === undefined && ccName === undefined
          ? '选择玩家'
          : '[' + ccId + '] ' + ccName
      "
      :readonly="true"
      :search-button="true"
      size="large"
      @click="openSelector"
    />
  </a-space>
  <a-modal
    v-model:visible="visible"
    title="选择玩家"
    :width="750"
    :footer="false"
  >
    <a-form :model="condition" layout="inline">
      <a-form-item label="角色ID">
        <a-input-number v-model="condition.characterId" allow-clear />
      </a-form-item>
      <a-form-item label="角色名称">
        <a-input v-model="condition.characterName" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="searchClick">查询</a-button>
      </a-form-item>
    </a-form>
    <a-table :data="tableData" row-key="characterId" :pagination="false">
      <template #columns>
        <a-table-column
          title="角色ID"
          data-index="characterId"
          align="center"
        />
        <a-table-column
          title="角色"
          data-index="characterName"
          align="center"
        />
        <a-table-column title="操作">
          <template #cell="{ record }">
            <a-button
              type="primary"
              size="mini"
              @click="selectClick(record.characterId, record.characterName)"
            >
              选择
            </a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-modal>
</template>

<style scoped lang="less"></style>
