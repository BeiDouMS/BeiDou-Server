<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.autobanLog')">
      <a-form :model="filterForm" class="a-from-keyword">
        <a-form-item :label="$t('autobanLog.filter.timeRange')">
          <a-range-picker
            v-model="filterForm.timeRange"
            show-time
            value-format="YYYY-MM-DD HH:mm:ss"
            :allow-clear="false"
            style="width: 380px"
            @change="rangeTouched = true"
          />
        </a-form-item>
        <a-form-item :label="$t('autobanLog.filter.characterName')">
          <a-input
            v-model="filterForm.characterName"
            allow-clear
            @input="clearCharacterId"
            @clear="clearCharacterId"
            @keydown.enter="searchClick"
          />
        </a-form-item>
        <a-form-item :label="$t('autobanLog.filter.accountId')">
          <a-input-number
            v-model="filterForm.accountId"
            :min="1"
            @keydown.enter="searchClick"
          />
        </a-form-item>
        <a-form-item :label="$t('autobanLog.filter.type')">
          <a-select
            v-model="filterForm.type"
            :options="typeOptions"
            allow-clear
            allow-search
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item :label="$t('autobanLog.filter.action')">
          <a-select
            v-model="filterForm.action"
            :options="actionOptions"
            allow-clear
            style="width: 150px"
          />
        </a-form-item>
        <a-form-item :label="$t('autobanLog.filter.minPoints')">
          <a-input-number
            v-model="filterForm.minPoints"
            :min="1"
            @keydown.enter="searchClick"
          />
        </a-form-item>
      </a-form>
      <a-space class="a-space-btn">
        <a-button type="primary" @click="searchClick">
          <template #icon>
            <icon-search />
          </template>
          {{ $t('button.search') }}
        </a-button>
        <a-button @click="resetClick">
          <template #icon>
            <icon-refresh />
          </template>
          {{ $t('button.reset') }}
        </a-button>
      </a-space>
      <a-divider />
      <a-card
        :title="$t('autobanLog.summary.title')"
        size="small"
        style="margin-bottom: 16px"
      >
        <template #extra>
          <span class="summary-hint">{{ $t('autobanLog.summary.hint') }}</span>
        </template>
        <a-table
          row-key="key"
          :loading="summaryLoading"
          :data="summaryList"
          :pagination="false"
          :bordered="{ cell: true }"
          size="small"
          class="summary-table"
          @row-click="summaryRowClick"
        >
          <template #columns>
            <a-table-column
              :title="$t('autobanLog.summary.column.character')"
              :width="160"
            >
              <template #cell="{ record }">
                {{ idName(record.characterId, record.characterName) }}
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('autobanLog.summary.column.account')"
              :width="160"
            >
              <template #cell="{ record }">
                {{ idName(record.accountId, record.accountName) }}
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('autobanLog.summary.column.total')"
              data-index="total"
              :width="90"
              align="center"
            />
            <a-table-column
              :title="$t('autobanLog.summary.column.alert')"
              data-index="alertCount"
              :width="80"
              align="center"
            />
            <a-table-column
              :title="$t('autobanLog.summary.column.point')"
              data-index="pointCount"
              :width="80"
              align="center"
            />
            <a-table-column
              :title="$t('autobanLog.summary.column.autoban')"
              data-index="autobanCount"
              :width="100"
              align="center"
            />
            <a-table-column
              :title="$t('autobanLog.summary.column.disconnect')"
              data-index="disconnectCount"
              :width="80"
              align="center"
            />
            <a-table-column :title="$t('autobanLog.summary.column.types')">
              <template #cell="{ record }">
                <a-space wrap size="mini">
                  <a-tooltip
                    v-for="item in splitTypes(record.types)"
                    :key="item"
                    :content="item"
                  >
                    <a-tag size="small" color="arcoblue">
                      {{ typeLabel(item) }}
                    </a-tag>
                  </a-tooltip>
                </a-space>
              </template>
            </a-table-column>
            <a-table-column
              :title="$t('autobanLog.summary.column.lastTime')"
              data-index="lastTime"
              :width="170"
              align="center"
            />
            <a-table-column
              :title="$t('autobanLog.summary.column.lastIp')"
              data-index="lastIp"
              :width="140"
              align="center"
            />
          </template>
        </a-table>
      </a-card>
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
            :title="$t('autobanLog.column.time')"
            data-index="createTime"
            :width="170"
            align="center"
          />
          <a-table-column
            :title="$t('autobanLog.column.worldChannel')"
            :width="90"
            align="center"
          >
            <template #cell="{ record }">
              {{ record.world }}/{{ record.channel }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('autobanLog.column.account')" :width="150">
            <template #cell="{ record }">
              {{ idName(record.accountId, record.accountName) }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autobanLog.column.character')"
            :width="150"
          >
            <template #cell="{ record }">
              {{ idName(record.characterId, record.characterName) }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autobanLog.column.map')"
            data-index="mapId"
            :width="110"
            align="center"
          />
          <a-table-column
            :title="$t('autobanLog.column.ip')"
            data-index="ip"
            :width="140"
            align="center"
          />
          <a-table-column
            :title="$t('autobanLog.column.type')"
            :width="150"
            align="center"
          >
            <template #cell="{ record }">
              <a-tooltip :content="record.type">
                <a-tag color="arcoblue">{{ typeLabel(record.type) }}</a-tag>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autobanLog.column.action')"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag :color="actionColor(record.action)">
                {{ actionLabel(record.action) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autobanLog.column.points')"
            :width="90"
            align="center"
          >
            <template #cell="{ record }">
              {{ pointsText(record.points, record.threshold) }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autobanLog.column.reason')"
            data-index="reason"
            ellipsis
            tooltip
          />
          <a-table-column
            :title="$t('autobanLog.column.status')"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag v-if="record.autoBanEnabled" color="red">
                {{ $t('autobanLog.status.enforced') }}
              </a-tag>
              <a-tag v-else color="gray">
                {{ $t('autobanLog.status.logOnly') }}
              </a-tag>
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
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Message } from '@arco-design/web-vue';
  import type { TableData } from '@arco-design/web-vue';
  import dayjs from 'dayjs';
  import useLoading from '@/hooks/loading';
  import {
    AUTOBAN_LOG_ACTIONS,
    AutobanLogRecord,
    AutobanLogSummary,
    AutobanLogTypeOption,
    getAutobanLogList,
    getAutobanLogSummary,
    getAutobanLogTypeOptions,
  } from '@/api/autobanLog';

  const TIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';
  const SUMMARY_LIMIT = 20;
  const ACTION_COLORS: Record<string, string> = {
    ALERT: 'orange',
    POINT: 'arcoblue',
    AUTOBAN: 'red',
    DISCONNECT: 'gray',
  };

  type RangeValue = (Date | string | number)[] | undefined;

  interface FilterForm {
    timeRange: RangeValue;
    characterName?: string;
    /** 点击 TOP 行时按角色 ID 精确筛选；手动改角色名后清空 */
    characterId?: number;
    accountId?: number;
    type?: string;
    action?: string;
    minPoints?: number;
  }

  type SummaryRow = AutobanLogSummary & { key: string };

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const summaryLoading = ref(false);

  const defaultRange = (): string[] => [
    dayjs().subtract(24, 'hour').format(TIME_FORMAT),
    dayjs().format(TIME_FORMAT),
  ];

  const filterForm = ref<FilterForm>({ timeRange: defaultRange() });
  /** 用户是否手动改过时间范围；未改过时每次查询都把「最近 24 小时」滑动到当前时刻 */
  const rangeTouched = ref(false);
  const tableData = ref<AutobanLogRecord[]>([]);
  const total = ref(0);
  const page = ref(1);
  const size = ref(20);
  const summaryList = ref<SummaryRow[]>([]);
  const typeOptions = ref<AutobanLogTypeOption[]>([]);

  const typeLabelMap = computed(() => {
    const map: Record<string, string> = {};
    typeOptions.value.forEach((item) => {
      map[item.value] = item.label;
    });
    return map;
  });
  const actionOptions = computed(() =>
    AUTOBAN_LOG_ACTIONS.map((action) => ({
      value: action,
      label: t(`autobanLog.action.${action}`),
    }))
  );

  const typeLabel = (type: string) => typeLabelMap.value[type] ?? type;
  const actionLabel = (action: string) => t(`autobanLog.action.${action}`);
  const actionColor = (action: string) => ACTION_COLORS[action] ?? 'gray';
  const splitTypes = (types?: string | null) =>
    types ? types.split(',').filter((item) => item) : [];
  const idName = (id?: number | null, name?: string | null) => {
    if (id == null && !name) return '-';
    return `[${id ?? '-'}] ${name ?? ''}`.trim();
  };
  const pointsText = (points?: number | null, threshold?: number | null) => {
    if (points == null && threshold == null) return '-';
    return `${points ?? '-'}/${threshold ?? '-'}`;
  };
  const trimText = (value?: string) => {
    const text = value?.trim();
    return text || undefined;
  };
  const toTime = (value: Date | string | number | undefined) => {
    if (value === undefined || value === null || value === '') return undefined;
    return dayjs(value).format(TIME_FORMAT);
  };
  const timeParams = () => {
    const range = filterForm.value.timeRange ?? [];
    return { startTime: toTime(range[0]), endTime: toTime(range[1]) };
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getAutobanLogList({
        pageNo: page.value,
        pageSize: size.value,
        characterName: trimText(filterForm.value.characterName),
        characterId: filterForm.value.characterId,
        accountId: filterForm.value.accountId,
        type: trimText(filterForm.value.type),
        action: trimText(filterForm.value.action),
        minPoints: filterForm.value.minPoints,
        ...timeParams(),
      });
      tableData.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };

  const loadSummary = async () => {
    summaryLoading.value = true;
    try {
      const { data } = await getAutobanLogSummary({
        ...timeParams(),
        limit: SUMMARY_LIMIT,
      });
      summaryList.value = data.map((item) => ({
        ...item,
        key: `${item.characterId ?? ''}-${item.accountId ?? ''}-${
          item.characterName ?? ''
        }`,
      }));
    } finally {
      summaryLoading.value = false;
    }
  };

  const loadTypeOptions = async () => {
    const { data } = await getAutobanLogTypeOptions();
    typeOptions.value = data;
  };

  const clearCharacterId = () => {
    filterForm.value.characterId = undefined;
  };

  const searchClick = () => {
    if (!rangeTouched.value) {
      filterForm.value.timeRange = defaultRange();
    }
    page.value = 1;
    loadData();
    loadSummary();
  };

  const resetClick = () => {
    filterForm.value = { timeRange: defaultRange() };
    rangeTouched.value = false;
    page.value = 1;
    loadData();
    loadSummary();
  };

  const pageChange = (current: number) => {
    page.value = current;
    loadData();
  };

  const pageSizeChange = (pageSize: number) => {
    page.value = 1;
    size.value = pageSize;
    loadData();
  };

  const summaryRowClick = (record: TableData) => {
    const row = record as SummaryRow;
    if (row.characterName) {
      filterForm.value.characterName = row.characterName;
      filterForm.value.characterId = row.characterId ?? undefined;
      filterForm.value.accountId = undefined;
    } else if (row.accountId != null) {
      filterForm.value.accountId = row.accountId;
      filterForm.value.characterName = undefined;
      filterForm.value.characterId = undefined;
    } else {
      return;
    }
    page.value = 1;
    Message.info(
      t('autobanLog.summary.applied', {
        name: row.characterName ?? String(row.accountId),
      })
    );
    loadData();
  };

  loadTypeOptions();
  loadData();
  loadSummary();
</script>

<script lang="ts">
  export default {
    name: 'AutobanLog',
  };
</script>

<style scoped lang="less">
  :deep(.arco-table-th) {
    min-width: 30px;
  }

  .summary-hint {
    color: var(--color-text-3);
    font-size: 12px;
  }

  .summary-table :deep(.arco-table-tr) {
    cursor: pointer;
  }
</style>

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
