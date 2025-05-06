<template>
  <div class="container" :loading="true">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.account.list')">
      <a-form :model="filterForm" class="a-from-keyword">
        <a-form-item :label="$t('account.list.filter.id')">
          <a-input-number v-model="filterForm.id" />
        </a-form-item>
        <a-form-item :label="$t('account.list.filter.name')">
          <a-input v-model="filterForm.name" />
        </a-form-item>
        <a-form-item :label="$t('account.list.filter.lastLoginStart')">
          <a-date-picker
            v-model="filterForm.lastLoginStart"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item :label="$t('account.list.filter.lastLoginEnd')">
          <a-date-picker
            v-model="filterForm.lastLoginEnd"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item :label="$t('account.list.filter.createdAtStart')">
          <a-date-picker
            v-model="filterForm.createdAtStart"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item :label="$t('account.list.filter.createdAtEnd')">
          <a-date-picker
            v-model="filterForm.createdAtEnd"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
      <a-space class="a-space-btn">
        <a-button type="primary" @click="loadData()">
          <template #icon>
            <icon-search />
          </template>
          {{ $t('button.load') }}
        </a-button>
        <a-button @click="resetClick">
          <template #icon>
            <icon-refresh />
          </template>
          {{ $t('button.reset') }}
        </a-button>
      </a-space>
      <a-divider />
      <a-row style="margin-bottom: 16px">
        <a-col>
          <a-space>
            <a-button type="primary" @click="addClick">
              <template #icon>
                <icon-plus />
              </template>
              {{ $t('button.create') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-table
        row-key="id"
        :loading="loading"
        :data="tableData"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column
            :title="$t('account.list.column.id')"
            data-index="id"
            :width="100"
          />
          <a-table-column
            :title="$t('account.list.column.name')"
            data-index="name"
            :width="200"
          />
          <a-table-column
            :title="$t('account.list.column.loggedin')"
            :width="120"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag v-if="record.loggedin" color="blue">
                {{ $t('account.list.column.loggedin.true') }}
              </a-tag>
              <a-tag v-else color="gray">
                {{ $t('account.list.column.loggedin.false') }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('account.list.column.banned')"
            :width="120"
            align="center"
          >
            <template #cell="{ record }">
              <a-tooltip v-if="record.banned" :content="record.banreason">
                <a-tag color="red">
                  {{ $t('account.list.column.banned.true') }}
                </a-tag>
              </a-tooltip>
              <a-tag v-else color="green">
                {{ $t('account.list.column.banned.false') }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('account.list.column.gender')"
            :width="60"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag v-if="record.gender === 0" color="blue">
                {{ $t('account.list.column.gender.male') }}
              </a-tag>
              <a-tag v-else-if="record.gender === 1" color="red">
                {{ $t('account.list.column.gender.female') }}
              </a-tag>
              <a-tag v-else color="gray">
                {{ $t('account.list.column.gender.other') }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('account.list.column.lastLoginAt')"
            data-index="lastlogin"
            :width="120"
            align="center"
          />
          <a-table-column
            :title="$t('account.list.column.registerAt')"
            data-index="createdat"
            :width="120"
            align="center"
          />
          <a-table-column
            :title="$t('account.list.column.operate')"
            :width="150"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                type="text"
                size="mini"
                :disabled="record.loggedin === 2"
                @click="editClick(record)"
              >
                {{ $t('button.edit') }}
              </a-button>
              <a-button
                type="text"
                size="mini"
                @click="restLoggedInClick(record)"
              >
                {{ $t('account.list.column.operate.restLoggedIn') }}
              </a-button>
              <a-popconfirm
                type="warning"
                :content="$t('account.list.column.operate.unban.confirm')"
                @ok="unbanClick(record)"
              >
                <a-button
                  v-if="record.banned"
                  type="text"
                  size="mini"
                  status="warning"
                >
                  {{ $t('account.list.column.operate.unban') }}
                </a-button>
              </a-popconfirm>
              <a-button
                v-if="!record.banned"
                type="text"
                size="mini"
                status="danger"
                @click="banClick(record)"
              >
                {{ $t('account.list.column.operate.ban') }}
              </a-button>
              <a-popconfirm
                type="error"
                :content="$t('account.list.column.operate.delete.confirm')"
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
      <a-pagination
        style="margin-top: 20px"
        :total="total"
        :page-size="size"
        :current="page"
        show-total
        show-jumper
        show-page-size
        :page-size-options="[10, 20, 50, 100]"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
    <account-add-form ref="accountAddFormRef" @reload="loadData" />
    <account-update-form ref="accountUpdateFormRef" @reload="loadData" />
    <a-modal
      v-model:visible="reasonVisible"
      :title="reasonTitle"
      :ok-loading="loading"
      :mask-closable="false"
      :esc-to-close="false"
      :ok-text="$t('account.list.column.operate.ban')"
      :on-before-ok="submitBanClick"
    >
      <a-form :model="{ reason }">
        <a-form-item
          :label="$t('account.list.column.operate.ban.reason')"
          validate-trigger="blur"
        >
          <a-input v-model="reason" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import useLoading from '@/hooks/loading';
  import { ref } from 'vue';
  import { AccountState } from '@/store/modules/account/types';
  import {
    banAccount,
    deleteAccount,
    getAccountList,
    resetLoggedIn,
    unbanAccount,
  } from '@/api/account';
  import AccountAddForm from '@/views/account/list/addForm.vue';
  import AccountUpdateForm from '@/views/account/list/updateForm.vue';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const tableData = ref<AccountState[]>([]);
  const total = ref(0);
  const page = ref(1);
  const size = ref(14);
  const filterForm = ref<{
    id?: number;
    name?: string;
    lastLoginStart?: string;
    lastLoginEnd?: string;
    createdAtStart?: string;
    createdAtEnd?: string;
  }>({
    id: undefined,
    name: undefined,
    lastLoginStart: undefined,
    lastLoginEnd: undefined,
    createdAtStart: undefined,
    createdAtEnd: undefined,
  });
  const reasonVisible = ref(false);
  const reasonTitle = ref('');
  const reason = ref('');
  const banAccountIdReady = ref(0);

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAccountList(
        page.value,
        size.value,
        filterForm.value.id,
        filterForm.value.name,
        filterForm.value.lastLoginStart,
        filterForm.value.lastLoginEnd,
        filterForm.value.createdAtStart,
        filterForm.value.createdAtEnd
      );
      tableData.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };
  loadData();

  const pageChange = (data: number) => {
    page.value = data;
    loadData();
  };

  const pageSizeChange = (data: number) => {
    page.value = 1;
    size.value = data;
    loadData();
  };

  const resetClick = () => {
    filterForm.value.id = undefined;
    filterForm.value.name = undefined;
    filterForm.value.lastLoginStart = undefined;
    filterForm.value.lastLoginEnd = undefined;
    filterForm.value.createdAtStart = undefined;
    filterForm.value.createdAtEnd = undefined;
    page.value = 1;
    loadData();
  };

  const accountAddFormRef = ref();
  const addClick = () => {
    accountAddFormRef.value.init();
  };

  const accountUpdateFormRef = ref();
  const editClick = (data: AccountState) => {
    accountUpdateFormRef.value.init(data);
  };

  const restLoggedInClick = async (data: AccountState) => {
    setLoading(true);
    try {
      await resetLoggedIn(data.id);
      Message.success(t('message.success'));
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const banClick = async (data: AccountState) => {
    reasonTitle.value = `${t(
      'account.list.column.operate.ban.reason.title'
    )} [${data.id}] ${data.name}`;
    banAccountIdReady.value = data.id;
    reasonVisible.value = true;
    reason.value = '';
  };

  const unbanClick = async (data: AccountState) => {
    setLoading(true);
    try {
      await unbanAccount(data.id);
      Message.success(t('message.success'));
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const submitBanClick = async () => {
    setLoading(true);
    try {
      await banAccount(banAccountIdReady.value, reason.value);
      Message.success(t('message.success'));
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const deleteClick = async (data: AccountState) => {
    setLoading(true);
    try {
      await deleteAccount(data.id);
      Message.success(t('message.success'));
      await loadData();
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'AccountList',
  };
</script>

<style lang="less">
  .a-from-keyword {
    @media (min-width: @screen-sm) {
      display: flex;
      flex-direction: initial;
      flex-wrap: wrap;
      width: 100%;
      div {
        margin-right: 5px;
      }
      .arco-row {
        width: max-content;
        display: flex;
      }
      .arco-col {
        flex: max-content;
        width: 100%;
      }
      .arco-form-item-label-col,
      .arco-form-item-label {
        min-width: auto;
        text-align: right;
      }
    }
    @media (max-width: @screen-sm) {
      display: block;
      flex-direction: column;
      .arco-row {
        flex-flow: row wrap;
        width: 100%;
      }

      .arco-col {
        flex: 0 0 100%;
        width: 100%;
      }

      .arco-form-item-label-col {
        display: contents;
      }
    }
  }
</style>
