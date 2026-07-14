<template>
  <a-modal
    v-model:visible="visible"
    :title="modalTitle"
    :width="1000"
    :mask-closable="false"
    :esc-to-close="false"
    :footer="false"
  >
    <a-table
      row-key="id"
      :loading="loading"
      :data="tableData"
      :pagination="false"
      :bordered="{ cell: true }"
      size="mini"
    >
      <template #columns>
        <a-table-column
          :title="$t('account.list.charList.column.id')"
          data-index="id"
          :width="70"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.charList.column.name')"
          data-index="name"
          :width="140"
        />
        <a-table-column
          :title="$t('account.list.charList.column.job')"
          data-index="jobName"
          :width="120"
        />
        <a-table-column
          :title="$t('account.list.charList.column.world')"
          data-index="worldName"
          :width="100"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.charList.column.level')"
          data-index="level"
          :width="60"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.charList.column.gm')"
          data-index="gm"
          :width="60"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.charList.column.meso')"
          data-index="meso"
          :width="110"
          align="right"
        />
        <a-table-column
          :title="$t('account.list.charList.column.fame')"
          data-index="fame"
          :width="60"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.charList.column.status')"
          :width="80"
          align="center"
        >
          <template #cell="{ record }">
            <a-tag v-if="record.online" color="green">
              {{ $t('account.list.charList.column.status.online') }}
            </a-tag>
            <a-tag v-else color="gray">
              {{ $t('account.list.charList.column.status.offline') }}
            </a-tag>
          </template>
        </a-table-column>
        <a-table-column
          :title="$t('account.list.charList.column.createdate')"
          data-index="createdate"
          :width="150"
          align="center"
        />
        <a-table-column
          :title="$t('account.list.column.operate')"
          :width="90"
          align="center"
          fixed="right"
        >
          <template #cell="{ record }">
            <a-popconfirm
              type="error"
              :content="
                $t('account.list.charList.column.operate.delete.confirm')
              "
              @ok="deleteClick(record)"
            >
              <a-button type="text" size="mini" status="danger">
                {{ $t('account.list.column.operate.delete') }}
              </a-button>
            </a-popconfirm>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup lang="ts">
  import useLoading from '@/hooks/loading';
  import { computed, ref } from 'vue';
  import {
    CharacterListItem,
    deleteCharacter,
    getAccountCharacters,
  } from '@/api/character';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const visible = ref(false);
  const tableData = ref<CharacterListItem[]>([]);
  const accountId = ref(0);
  const accountName = ref('');

  const modalTitle = computed(
    () =>
      `${t('account.list.charList.title')} [${accountId.value}] ${
        accountName.value
      }`
  );

  const init = (id: number, name: string) => {
    accountId.value = id;
    accountName.value = name;
    visible.value = true;
    loadData();
  };
  defineExpose({ init });

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAccountCharacters(accountId.value);
      tableData.value = data ?? [];
    } finally {
      setLoading(false);
    }
  };

  const deleteClick = async (record: CharacterListItem) => {
    setLoading(true);
    try {
      await deleteCharacter(record.id);
      Message.success(t('message.success'));
      await loadData();
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'AccountCharList',
  };
</script>
