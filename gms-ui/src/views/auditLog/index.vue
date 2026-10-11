<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.account.auditLog')">
      <a-form :model="filterForm" class="a-from-keyword">
        <a-form-item :label="$t('auditLog.filter.operatorName')">
          <a-input
            v-model="filterForm.operatorName"
            @keydown.enter="loadData"
          />
        </a-form-item>
        <a-form-item :label="$t('auditLog.filter.uri')">
          <a-input v-model="filterForm.uri" @keydown.enter="loadData" />
        </a-form-item>
        <a-form-item :label="$t('auditLog.filter.operation')">
          <a-select
            v-model="filterForm.operation"
            style="width: 100%"
            allow-search
            allow-clear
            :placeholder="$t('auditLog.filter.operation.placeholder')"
          >
            <a-option
              v-for="item in operationOptions"
              :key="item"
              :value="item"
            >
              {{ item }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('auditLog.filter.result')">
          <a-select v-model="filterForm.result" style="width: 100%" allow-clear>
            <a-option :value="1">{{
              $t('auditLog.column.result.success')
            }}</a-option>
            <a-option :value="0">{{
              $t('auditLog.column.result.fail')
            }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="$t('auditLog.filter.startTime')">
          <a-date-picker
            v-model="filterForm.startTime"
            show-time
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item :label="$t('auditLog.filter.endTime')">
          <a-date-picker
            v-model="filterForm.endTime"
            show-time
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
            :title="$t('auditLog.column.id')"
            data-index="id"
            :width="90"
          />
          <a-table-column
            :title="$t('auditLog.column.operatorName')"
            data-index="operatorName"
            :width="120"
          />
          <a-table-column
            :title="$t('auditLog.column.operation')"
            data-index="operation"
            :width="140"
            ellipsis
          />
          <a-table-column
            :title="$t('auditLog.column.httpMethod')"
            data-index="httpMethod"
            :width="80"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag :color="methodColor(record.httpMethod)">
                {{ record.httpMethod }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('auditLog.column.uri')"
            data-index="uri"
            :width="220"
            ellipsis
          />
          <a-table-column
            :title="$t('auditLog.column.ip')"
            data-index="ip"
            :width="130"
          />
          <a-table-column
            :title="$t('auditLog.column.params')"
            data-index="params"
            :width="260"
            ellipsis
          />
          <a-table-column
            :title="$t('auditLog.column.result')"
            :width="80"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag v-if="record.result === 1" color="green">
                {{ $t('auditLog.column.result.success') }}
              </a-tag>
              <a-tag v-else color="red">
                {{ $t('auditLog.column.result.fail') }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('auditLog.column.errorMsg')"
            data-index="errorMsg"
            :width="180"
            ellipsis
          />
          <a-table-column
            :title="$t('auditLog.column.costMs')"
            data-index="costMs"
            :width="90"
            align="center"
          />
          <a-table-column
            :title="$t('auditLog.column.createdAt')"
            data-index="createdAt"
            :width="170"
            align="center"
          />
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
  </div>
</template>

<script setup lang="ts">
  import useLoading from '@/hooks/loading';
  import { onMounted, ref } from 'vue';
  import {
    AuditLogResult,
    getAuditLogList,
    getAuditLogOperationOptions,
  } from '@/api/auditLog';

  const { loading, setLoading } = useLoading(false);
  const tableData = ref<AuditLogResult[]>([]);
  const operationOptions = ref<string[]>([]);
  const total = ref(0);
  const page = ref(1);
  const size = ref(14);
  const filterForm = ref<{
    operatorName?: string;
    uri?: string;
    operation?: string;
    result?: number;
    startTime?: string;
    endTime?: string;
  }>({
    operatorName: undefined,
    uri: undefined,
    operation: undefined,
    result: undefined,
    startTime: undefined,
    endTime: undefined,
  });

  const loadOperationOptions = async () => {
    const { data } = await getAuditLogOperationOptions();
    operationOptions.value = data;
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAuditLogList({
        pageNo: page.value,
        pageSize: size.value,
        operatorName: filterForm.value.operatorName,
        uri: filterForm.value.uri,
        operation: filterForm.value.operation,
        result: filterForm.value.result,
        startTime: filterForm.value.startTime,
        endTime: filterForm.value.endTime,
      });
      tableData.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };

  onMounted(async () => {
    await loadOperationOptions();
    await loadData();
  });

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
    filterForm.value.operatorName = undefined;
    filterForm.value.uri = undefined;
    filterForm.value.operation = undefined;
    filterForm.value.result = undefined;
    filterForm.value.startTime = undefined;
    filterForm.value.endTime = undefined;
    page.value = 1;
    loadData();
  };

  const methodColor = (method: string) => {
    switch (method) {
      case 'GET':
        return 'blue';
      case 'POST':
        return 'green';
      case 'PUT':
        return 'orangered';
      case 'DELETE':
        return 'red';
      case 'PATCH':
        return 'purple';
      default:
        return 'gray';
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'AuditLog',
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
