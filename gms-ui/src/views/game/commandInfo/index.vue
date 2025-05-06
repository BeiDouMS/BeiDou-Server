<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.command')">
      <a-space direction="vertical" align="start">
        <a-form-item :label="$t('command.search.type.label')">
          <a-radio-group
            v-for="level in levels"
            :key="level"
            type="button"
            :model-value="condition.level"
            @change="levelChange"
          >
            <a-radio :value="level">
              {{
                level == -1
                  ? $t('command.level.all')
                  : $t('command.level.text') + level
              }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :hide-label="true">
          <a-row :wrap="false">
            <a-col>
              <a-space>
                <a-input
                  v-model="condition.syntax"
                  :placeholder="$t('command.placeholder.syntax')"
                />
                <a-button type="primary" @click="searchData">
                  {{ $t('button.search') }}
                </a-button>
                <a-button @click="resetSearch">
                  {{ $t('button.reset') }}
                </a-button>
              </a-space>
            </a-col>
          </a-row>
        </a-form-item>
      </a-space>
      <a-table
        row-key="id"
        :loading="loading"
        :data="commandList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column
            :title="$t('command.column.syntax')"
            data-index="syntax"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('command.column.clazz')"
            data-index="clazz"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('command.column.description')"
            data-index="description"
            :width="200"
            align="center"
          />
          <a-table-column
            :title="$t('command.column.defaultLevel')"
            data-index="defaultLevel"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('command.column.level')"
            data-index="level"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('command.column.enabled')"
            data-index="enabled"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-switch v-model="record.enabled" :disabled="true" />
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('command.column.operate')"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-button type="text" size="mini" @click="uptClick(record)">
                {{ $t('button.edit') }}
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total"
        :page-size="condition.pageSize"
        :current="condition.pageNo"
        show-total
        show-jumper
        show-page-size
        :page-size-options="[10, 20, 40, 80, 100]"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
      <a-modal
        v-model:visible="editVisible"
        :width="450"
        :title="$t('button.edit')"
        draggable
        :ok-text="$t('button.submit')"
        @ok="editOk"
      >
        <a-form :model="editData" :auto-label-width="true">
          <a-form-item
            field="syntax"
            :label="$t('command.column.syntax')"
            :disabled="true"
          >
            <a-input v-model="editData.syntax" />
          </a-form-item>
          <a-form-item
            field="clazz"
            :label="$t('command.column.clazz')"
            :disabled="true"
          >
            <a-input v-model="editData.clazz" />
          </a-form-item>
          <a-form-item field="level" :label="$t('command.column.defaultLevel')">
            <a-input-number
              v-model="editData.defaultLevel"
              :min="0"
              :max="6"
              :disabled="true"
            />
          </a-form-item>
          <a-form-item field="level" :label="$t('command.column.level')">
            <a-input-number v-model="editData.level" :min="0" :max="6" />
          </a-form-item>
          <a-form-item field="enabled" :label="$t('command.column.enabled')">
            <a-switch v-model="editData.enabled" />
          </a-form-item>
          <a-form-item
            field="description"
            :label="$t('command.column.description')"
          >
            <a-textarea
              v-model="editData.description"
              :max-length="500"
              :disabled="true"
            />
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { CommandReq, getCommandList, updateCommand } from '@/api/command';
  import useLoading from '@/hooks/loading';

  const condition = ref({
    level: -1,
    syntax: '',
    pageNo: 1,
    pageSize: 20,
  });
  const commandList = ref<CommandReq[]>([]);
  const total = ref<number>(0);
  const { loading, setLoading } = useLoading(false);
  const editVisible = ref<boolean>(false);
  const editData = ref<CommandReq>({});
  const levels = ref<number[]>([-1, 1, 2, 3, 4, 5, 6]);

  const loadCommands = async () => {
    setLoading(true);
    try {
      const param = {
        ...condition.value,
        levelList:
          condition.value.level === -1 ? undefined : [condition.value.level],
      };
      const { data } = await getCommandList(param);
      commandList.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };

  const pageChange = (data: number) => {
    condition.value.pageNo = data;
    loadCommands();
  };

  const pageSizeChange = (data: number) => {
    condition.value.pageNo = 1;
    condition.value.pageSize = data;
    loadCommands();
  };

  const searchData = async () => {
    await loadCommands();
  };

  const resetSearch = () => {
    condition.value.syntax = '';
    condition.value.level = -1;
  };

  const levelChange = async (value: any) => {
    condition.value.level = Number(value);
    await loadCommands();
  };

  const uptClick = (record: CommandReq) => {
    editData.value.id = record.id;
    editData.value.level = record.level;
    editData.value.syntax = record.syntax;
    editData.value.defaultLevel = record.defaultLevel;
    editData.value.clazz = record.clazz;
    editData.value.description = record.description;
    editData.value.enabled = record.enabled;
    editVisible.value = true;
  };

  const editOk = async () => {
    await updateCommand(editData.value);
    resetEditData();
    editVisible.value = false;
    await loadCommands();
  };

  const resetEditData = () => {
    editData.value = {};
  };
</script>

<script lang="ts">
  export default {
    name: 'CommandInfo',
  };
</script>

<style lang="less" scoped>
  :deep(.arco-form-item-content-flex) {
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-start;
  }
</style>
