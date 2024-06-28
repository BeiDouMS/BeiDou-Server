<template>
  <div class="container" :loading="true">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.account.list')">
      <a-row style="margin-bottom: 16px">
        <a-col :flex="1">
          <a-form
            :model="filterForm"
            :label-col-props="{ span: 8 }"
            :wrapper-col-props="{ span: 16 }"
          >
            <a-row :gutter="16">
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.id')">
                  <a-input-number v-model="filterForm.id" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.name')">
                  <a-input v-model="filterForm.name" />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.lastLoginStart')">
                  <a-date-picker
                    v-model="filterForm.lastLoginStart"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.lastLoginEnd')">
                  <a-date-picker
                    v-model="filterForm.lastLoginEnd"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.createdAtStart')">
                  <a-date-picker
                    v-model="filterForm.createdAtStart"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="6">
                <a-form-item :label="$t('account.list.filter.createdAtEnd')">
                  <a-date-picker
                    v-model="filterForm.createdAtEnd"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-col>
        <a-divider style="height: 84px" direction="vertical" />
        <a-col :flex="'86px'" style="text-align: right">
          <a-space direction="vertical" :size="18">
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
        </a-col>
      </a-row>
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
        :scroll="{ y: 'calc(100vh - 502px)' }"
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
              <a-tag v-if="record.banned" color="red">
                {{ $t('account.list.column.banned.true') }}
              </a-tag>
              <a-tag v-else color="green">
                {{ $t('account.list.column.banned.false') }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('account.list.column.gender')"
            :width="80"
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
          />
          <a-table-column
            :title="$t('account.list.column.registerAt')"
            data-index="createdat"
          />
          <a-table-column :title="$t('account.list.column.operate')">
            <template #cell="{ record }">
              <a-button
                type="text"
                size="mini"
                :disabled="record.loggedin === 2"
                @click="editClick(record)"
              >
                {{ $t('button.edit') }}
              </a-button>
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
        :page-size-options="[7, 14, 35, 70]"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
    <account-add-form ref="accountAddFormRef" @reload="loadData" />
    <account-update-form ref="accountUpdateFormRef" @reload="loadData" />
  </div>
</template>

<script setup lang="ts">
  import useLoading from '@/hooks/loading';
  import { ref } from 'vue';
  import { AccountState } from '@/store/modules/account/types';
  import { getAccountList } from '@/api/account';
  import AccountAddForm from '@/views/account/list/addForm.vue';
  import AccountUpdateForm from '@/views/account/list/updateForm.vue';

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
</script>

<script lang="ts">
  export default {
    name: 'AccountList',
  };
</script>
