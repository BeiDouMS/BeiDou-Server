<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.autoban')">
      <a-table
        :loading="loading"
        :data="configList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column
            :title="$t('autoban.column.type')"
            :width="180"
            align="center"
          >
            <template #cell="{ record }">
              <a-tooltip :content="record.type">
                <a-tag color="arcoblue">{{ record.name }}</a-tag>
              </a-tooltip>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.disabled')"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-switch v-model="record.disabled" />
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.defaultPoints')"
            data-index="defaultPoints"
            :width="120"
            align="center"
          />
          <a-table-column
            :title="$t('autoban.column.points')"
            :width="200"
            align="center"
          >
            <template #cell="{ record }">
              <a-space>
                <a-checkbox v-model="record.changePoints">
                  {{ $t('autoban.points.custom') }}
                </a-checkbox>
                <a-input-number
                  v-if="record.changePoints"
                  v-model="record.points"
                  :min="1"
                  :style="{ width: '80px' }"
                />
              </a-space>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.defaultExpireTime')"
            data-index="defaultExpireTimeSeconds"
            :width="160"
            align="center"
          >
            <template #cell="{ record }">
              {{
                record.defaultExpireTimeSeconds === -1
                  ? $t('autoban.neverExpire')
                  : record.defaultExpireTimeSeconds
              }}
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.expireTime')"
            :width="220"
            align="center"
          >
            <template #cell="{ record }">
              <a-space>
                <a-checkbox v-model="record.changeExpireTime">
                  {{ $t('autoban.expireTime.custom') }}
                </a-checkbox>
                <a-input-number
                  v-if="record.changeExpireTime"
                  v-model="record.expireTimeSeconds"
                  :min="-1"
                  :style="{ width: '100px' }"
                />
              </a-space>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.description')"
            data-index="description"
            :width="200"
            align="center"
          >
            <template #cell="{ record }">
              <a-input v-model="record.description" :max-length="128" />
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('autoban.column.operate')"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-button type="text" size="mini" @click="saveClick(record)">
                {{ $t('button.save') }}
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import {
    getAutobanConfigList,
    updateAutobanConfig,
    AutobanConfigResult,
  } from '@/api/autoban';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { Message } from '@arco-design/web-vue';

  const { t } = useI18n();
  const configList = ref<AutobanConfigResult[]>([]);
  const { loading, setLoading } = useLoading(false);

  const loadConfigs = async () => {
    setLoading(true);
    try {
      const { data } = await getAutobanConfigList();
      configList.value = data;
    } finally {
      setLoading(false);
    }
  };

  const saveClick = async (record: AutobanConfigResult) => {
    await updateAutobanConfig(record);
    Message.success(t('autoban.save.success'));
    await loadConfigs();
  };

  loadConfigs();
</script>

<script lang="ts">
  export default {
    name: 'Autoban',
  };
</script>

<style scoped lang="less">
  :deep(.arco-table-th) {
    min-width: 30px;
  }
</style>
