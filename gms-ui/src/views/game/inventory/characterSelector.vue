<script setup lang="ts">
  import { ref } from 'vue';
  import { getCharacterList, InventoryCondition } from '@/api/inventory';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();

  const condition = ref<InventoryCondition>({
    characterId: undefined,
    characterName: undefined,
    pageNo: 1,
    pageSize: 20,
  });

  const ccId = ref<number | undefined>(undefined);
  const ccName = ref<string | undefined>(undefined);
  const cOnlineStatus = ref<boolean | undefined>(false);

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
  const selectClick = (cid: number, cName: string, onlineStatus: boolean) => {
    ccId.value = cid;
    ccName.value = cName;
    cOnlineStatus.value = onlineStatus;
    emit('useCharacter', cid, cName, onlineStatus);
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
          ? t('characterSelector.placeholder')
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
    :title="t('characterSelector.title')"
    :width="750"
    :footer="false"
  >
    <a-form :model="condition" layout="inline">
      <a-form-item
        :label="t('characterSelector.column.id')"
        style="width: 200px"
      >
        <a-input-number v-model="condition.characterId" allow-clear />
      </a-form-item>
      <a-form-item
        :label="t('characterSelector.column.name')"
        style="width: 200px"
      >
        <a-input v-model="condition.characterName" allow-clear />
      </a-form-item>
      <a-form-item style="width: 200px">
        <a-button type="primary" @click="searchClick"
          >{{ t('characterSelector.searchButton') }}
        </a-button>
      </a-form-item>
    </a-form>
    <a-table :data="tableData" row-key="characterId" :pagination="false">
      <template #columns>
        <a-table-column
          :title="t('characterSelector.column.id')"
          data-index="characterId"
          align="center"
        />
        <a-table-column
          :title="t('characterSelector.column.name')"
          data-index="characterName"
          align="center"
        />
        <a-table-column
          :title="$t('characterSelector.column.onlineStatus')"
          data-index="online"
          align="center"
        >
          <template #cell="{ record }">
            <a-tag v-if="record.onlineStatus" color="green"
              >{{ $t('inventoryList.column.online') }}
            </a-tag>
            <a-tag v-else color="gray"
              >{{ $t('inventoryList.column.offline') }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column :title="t('characterSelector.column.operation')">
          <template #cell="{ record }">
            <a-button
              type="primary"
              size="mini"
              @click="
                selectClick(
                  record.characterId,
                  record.characterName,
                  record.onlineStatus
                )
              "
            >
              {{ t('characterSelector.selectButton') }}
            </a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-modal>
</template>

<style scoped lang="less"></style>
