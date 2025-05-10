<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.config')">
      <a-space direction="vertical" align="start">
        <a-form-item :label="$t('config.search.type.label')">
          <a-radio-group
            v-for="type in types"
            :key="type"
            type="button"
            :model-value="condition.type"
            @change="typeChange"
          >
            <a-radio :value="type">
              {{ transI18nType(type, false) }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="$t('config.search.subType.label')">
          <a-radio-group
            v-for="subType in subTypes"
            :key="subType"
            type="button"
            :model-value="condition.subType"
            @change="subTypeChange"
          >
            <a-radio :value="subType">
              {{ transI18nType(subType, true) }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :hide-label="true">
          <a-col :offset="0">
            <a-input
              v-model="condition.filter"
              :placeholder="$t('config.placeholder.filter')"
            />
            <a-button type="primary" @click="searchData">
              {{ $t('button.search') }}
            </a-button>
            <a-button @click="resetSearch">
              {{ $t('button.reset') }}
            </a-button>
            <a-button
              type="primary"
              status="success"
              :disabled="selectedKeys.length > 0"
              @click="addClick"
            >
              {{ $t('button.add') }}
            </a-button>
            <a-button
              type="primary"
              status="danger"
              :disabled="selectedKeys.length === 0"
              @click="delClick"
            >
              {{ $t('button.delete') }}
            </a-button>
            <a-button type="primary" @click="importClick">
              {{ $t('config.extra.import') }}
            </a-button>
            <a-button type="primary" @click="exportClick">
              {{ $t('config.extra.export') }}
            </a-button>
          </a-col>
        </a-form-item>
      </a-space>
      <a-table
        v-model:selectedKeys="selectedKeys"
        row-key="id"
        :loading="loading"
        :data="configList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
        :row-selection="{
          type: 'checkbox',
          showCheckedAll: true,
          onlyCurrent: false,
        }"
      >
        <template #columns>
          <a-table-column
            :title="$t('config.column.type')"
            data-index="configType"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag color="orangered">
                {{ transI18nType(record.configType, false) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('config.column.subType')"
            data-index="configSubType"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag color="purple">
                {{ transI18nType(record.configSubType, true) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('config.column.clazz')"
            data-index="configClazz"
            :width="120"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag color="green">
                {{ transI18nClz(record.configClazz) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('config.column.code')"
            data-index="configCode"
            :width="200"
            align="center"
          />
          <a-table-column
            :title="$t('config.column.value')"
            data-index="configValue"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('config.column.desc')"
            data-index="configDesc"
            :width="400"
            align="center"
          />
          <a-table-column
            :title="$t('config.column.operate')"
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
        :title="editTitle"
        draggable
        :ok-text="$t('button.submit')"
        @ok="editOk"
      >
        <a-form :model="editData" :auto-label-width="true">
          <a-form-item
            field="configType"
            :label="$t('config.column.type')"
            :required="true"
            :disabled="editData.id != null && editData.id != 0"
          >
            <a-select v-model="editData.configType">
              <a-option v-for="type in oriTypes" :key="type" :value="type">
                {{ transI18nType(type, false) }}
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item
            field="configSubType"
            :label="$t('config.column.subType')"
            :required="true"
            :disabled="editData.id != null && editData.id != 0"
          >
            <a-select v-model="editData.configSubType">
              <a-option
                v-for="subType in oriSubTypes"
                :key="subType"
                :value="subType"
              >
                {{ transI18nType(subType, true) }}
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item
            field="configClazz"
            :label="$t('config.column.clazz')"
            :required="true"
            :disabled="editData.id != null && editData.id != 0"
          >
            <a-select v-model="editData.configClazz">
              <a-option
                v-for="clzType in editData.id != null && editData.id != 0
                  ? clzFull
                  : clzTypes"
                :key="clzType"
                :value="clzType"
              >
                {{ transI18nClz(clzType) }}
              </a-option>
            </a-select>
          </a-form-item>
          <a-form-item
            field="configCode"
            :label="$t('config.column.code')"
            :required="true"
            :disabled="editData.id != null && editData.id != 0"
          >
            <a-input v-model="editData.configCode" :max-length="64" />
          </a-form-item>
          <a-form-item
            field="configValue"
            :label="$t('config.column.value')"
            :required="true"
          >
            <!-- 小数也用字符串输入，避免进行小数点精确 -->
            <a-input
              v-if="getClzType(editData.configClazz) !== 'bool'"
              v-model="editData.configValue"
              :max-length="256"
            />
            <a-switch
              v-if="getClzType(editData.configClazz) === 'bool'"
              v-model="editData.configValue"
              checked-value="true"
              unchecked-value="false"
            />
          </a-form-item>
          <a-form-item field="configDesc" :label="$t('config.column.desc')">
            <a-textarea v-model="editData.configDesc" :max-length="500" />
          </a-form-item>
        </a-form>
      </a-modal>
      <a-modal
        v-model:visible="confirmVisible"
        :width="450"
        draggable
        @ok="confirmOk"
      >
        <template #title>
          {{ $t('button.delete') }}
        </template>
        <div>{{ $t('config.confirm.text') }}</div>
      </a-modal>
      <a-modal
        v-model:visible="importVisible"
        :width="450"
        draggable
        :ok-text="$t('button.upload')"
        @ok="importOk"
      >
        <template #title>
          {{ $t('config.extra.import') }}
        </template>
        <div>
          <p style="color: red; font-size: 16px">
            {{ $t('config.extra.import.warn') }}
          </p>
        </div>
        <a-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          action="/config/v1/importYml"
          :custom-request="customRequest"
          :file-list="fileList"
          @success="uploadSuccess"
        />
      </a-modal>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import {
    addConfig,
    ConfigResult,
    ConfigSearch,
    deleteConfigList,
    getConfigList,
    getConfigTypeList,
    updateConfig,
    importYml,
    exportYml,
  } from '@/api/config';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { FileItem, RequestOption } from '@arco-design/web-vue';

  const { t } = useI18n();
  const types = ref<string[]>([]);
  const oriTypes = ref<string[]>([]);
  const subTypes = ref<string[]>([]);
  const oriSubTypes = ref<string[]>([]);
  const condition = ref<ConfigSearch>({
    type: 'all',
    subType: 'All',
    filter: '',
    pageNo: 1,
    pageSize: 20,
  });
  const configList = ref<ConfigResult[]>([]);
  const total = ref<number>(0);
  const { loading, setLoading } = useLoading(false);
  const selectedKeys = ref<number[]>([]);
  const editVisible = ref<boolean>(false);
  const editTitle = ref<string>('');
  const editData = reactive<ConfigResult>({
    id: 0,
    configType: 'server',
    configSubType: 'Game Mechanics',
    configClazz: 'java.lang.String',
    configCode: '',
    configValue: '',
    configDesc: '',
  });
  const clzTypes = ref<string[]>([
    'java.lang.Integer',
    'java.lang.String',
    'java.lang.Float',
    'java.lang.Boolean',
  ]);
  const clzFull = ref<string[]>([
    ...clzTypes.value,
    'java.lang.Long',
    'java.lang.Byte',
    'java.lang.Short',
    'java.lang.Double',
    'java.util.Map',
  ]);
  const confirmVisible = ref<boolean>(false);
  const importVisible = ref<boolean>(false);
  const uploadRef = ref();
  const fileList = ref<FileItem[]>([]);

  const loadTypes = async () => {
    const { data } = await getConfigTypeList();
    oriTypes.value = data.types;
    types.value = ['all', ...data.types];
    oriSubTypes.value = data.subTypes;
    subTypes.value = ['All', ...data.subTypes];
  };

  const transI18nType = (type: string, isSub: boolean) => {
    type = type.replace(' ', '');
    let i18nType;
    if (isSub) {
      if (Number.isFinite(Number(type))) {
        i18nType = t('config.type.world') + type;
      } else {
        i18nType = t(`config.subType.${type}`);
      }
    } else {
      i18nType = t(`config.type.${type}`);
    }
    return i18nType == null ? type : i18nType;
  };

  const transI18nClz = (clz: string) => {
    const clzType = getClzType(clz);
    return t(`config.clz.${clzType}`);
  };

  const getClzType = (clz: string) => {
    let clzType;
    switch (clz) {
      case 'java.lang.Integer':
      case 'java.lang.Long':
      case 'java.lang.Byte':
      case 'java.lang.Short':
        clzType = 'int';
        break;
      case 'java.lang.Float':
      case 'java.lang.Double':
        clzType = 'float';
        break;
      case 'java.lang.Boolean':
        clzType = 'bool';
        break;
      default:
        clzType = 'string';
        break;
    }
    return clzType;
  };

  const loadConfigs = async () => {
    setLoading(true);
    try {
      const param = {
        ...condition.value,
        type: condition.value.type === 'all' ? '' : condition.value.type,
        subType:
          condition.value.subType === 'All' ? '' : condition.value.subType,
      };
      const { data } = await getConfigList(param);
      configList.value = data.records;
      total.value = data.totalRow;
      selectedKeys.value = [];
    } finally {
      setLoading(false);
    }
  };

  const pageChange = (data: number) => {
    condition.value.pageNo = data;
    loadConfigs();
  };

  const pageSizeChange = (data: number) => {
    condition.value.pageNo = 1;
    condition.value.pageSize = data;
    loadConfigs();
  };

  const searchData = async () => {
    await loadConfigs();
  };

  const resetSearch = () => {
    condition.value.type = 'all';
    condition.value.subType = 'All';
    condition.value.filter = '';
    condition.value.pageNo = 1;
    condition.value.pageSize = 20;
  };

  const typeChange = async (value: any) => {
    condition.value.type = String(value);
    await loadConfigs();
  };

  const subTypeChange = async (value: any) => {
    condition.value.subType = String(value);
    await loadConfigs();
  };

  const addClick = () => {
    resetEditData();
    editVisible.value = true;
    editTitle.value = t('button.add');
  };

  const delClick = async () => {
    confirmVisible.value = true;
  };

  const uptClick = (record: ConfigResult) => {
    editData.id = record.id;
    editData.configType = record.configType;
    editData.configSubType = record.configSubType;
    editData.configClazz = record.configClazz;
    editData.configCode = record.configCode;
    editData.configValue = record.configValue;
    editData.configDesc = record.configDesc;
    editVisible.value = true;
    editTitle.value = t('button.edit');
  };

  const editOk = async () => {
    if (editData.id) {
      await updateConfig(editData);
    } else {
      await addConfig(editData);
    }
    resetEditData();
    editVisible.value = false;
    await loadConfigs();
  };

  const resetEditData = () => {
    editData.id = 0;
    editData.configType = 'server';
    editData.configSubType = 'Game Mechanics';
    editData.configClazz = 'java.lang.String';
    editData.configCode = '';
    editData.configValue = '';
    editData.configDesc = '';
  };

  const confirmOk = async () => {
    await deleteConfigList(selectedKeys.value);
    await loadConfigs();
  };

  const importClick = async () => {
    importVisible.value = true;
  };

  const importOk = async () => {
    uploadRef.value.submit();
  };

  const customRequest = (option: RequestOption) => {
    setLoading(true);
    try {
      importYml(option);
    } finally {
      setLoading(false);
    }
    return undefined;
  };

  const uploadSuccess = async () => {
    importVisible.value = false;
    fileList.value = [];
    await loadConfigs();
  };

  const exportClick = async () => {
    setLoading(true);
    try {
      await exportYml();
    } finally {
      setLoading(false);
    }
  };

  loadTypes();
</script>

<script lang="ts">
  export default {
    name: 'Config',
  };
</script>

<style scoped lang="less">
  :deep(.arco-form-item-content-flex) {
    flex-wrap: wrap;
    align-items: center;
    justify-content: flex-start;
  }
  :deep(.arco-space-horizontal, .arco-col arco-col-24) {
    flex-wrap: wrap;
    align-items: center;
  }
  :deep(.arco-row-align-start > .arco-col) {
    flex-wrap: wrap;
  }
  :deep(.arco-card-body > .arco-space-vertical) {
    width: 100% !important;
  }
  :deep(.arco-space-item .arco-input-wrapper) {
    width: 100% !important;
    max-width: 400px !important;
  }
  :deep(.arco-space-item) {
    width: 100%;
  }
  :deep(.arco-form-item-content > div) {
    width: 100%;
  }
  :deep(.arco-form-item-content > div > *) {
    margin-right: 5px;
    margin-top: 5px;
  }
  :deep(.arco-table-th) {
    min-width: 30px;
  }
  :deep(.arco-table-th:nth-child(4)) {
    min-width: 70px;
  }
  :deep(.arco-table-th:nth-child(5)) {
    min-width: 100px;
  }
  :deep(.arco-table-th:nth-child(6)) {
    min-width: 100px;
  }
  :deep(.arco-table-th:nth-child(7)) {
    min-width: 250px;
  }
</style>
