<template>
  <div class="container">
    <Breadcrumb />
    <a-card
      class="general-card"
      :title="$t('menu.dashboard.informationSearch')"
    >
      <a-row>
        <a-col>
          <a-space>
            <a-select
              v-model="condition.types"
              :placeholder="$t('informationSearch.placeholder.type')"
              :scrollbar="true"
              :style="{ width: '200px' }"
              multiple
              :max-tag-count="1"
              allow-clear
            >
              <a-option value="cash">
                {{ $t('informationSearch.type.cash') }}
              </a-option>
              <a-option value="consume">
                {{ $t('informationSearch.type.consume') }}
              </a-option>
              <a-option value="eqp">
                {{ $t('informationSearch.type.eqp') }}
              </a-option>
              <a-option value="etc">
                {{ $t('informationSearch.type.etc') }}
              </a-option>
              <a-option value="ins">
                {{ $t('informationSearch.type.ins') }}
              </a-option>
              <a-option value="map">
                {{ $t('informationSearch.type.map') }}
              </a-option>
              <a-option value="mob">
                {{ $t('informationSearch.type.mob') }}
              </a-option>
              <a-option value="npc">
                {{ $t('informationSearch.type.npc') }}
              </a-option>
              <a-option value="pet">
                {{ $t('informationSearch.type.pet') }}
              </a-option>
              <a-option value="skill">
                {{ $t('informationSearch.type.skill') }}
              </a-option>
            </a-select>
            <a-input
              v-model="condition.filter"
              :placeholder="$t('informationSearch.placeholder.filter')"
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
      <a-table
        row-key="id"
        :loading="loading"
        :data="informationList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column
            :title="$t('informationSearch.column.type')"
            data-index="type"
            :width="120"
            align="center"
          >
            <template #cell="{ record }">
              <a-tag color="arcoblue">
                {{ getTag(record.type) }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('informationSearch.column.id')"
            data-index="id"
            :width="100"
            align="center"
          />
          <a-table-column
            :title="$t('informationSearch.column.name')"
            data-index="name"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <a-popover>
                <a-button type="text" size="mini">
                  {{ record.name }}
                </a-button>
                <template #content>
                  <img :src="getImg(record.type, record.id)" alt="" />
                </template>
              </a-popover>
            </template>
          </a-table-column>
          <a-table-column
            :title="$t('informationSearch.column.desc')"
            data-index="desc"
            :width="200"
            align="center"
          />
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { Message } from '@arco-design/web-vue';
  import useLoading from '@/hooks/loading';
  import { getIconUrl } from '@/utils/mapleStoryAPI';
  import {
    InformationSearch,
    InformationResult,
    informationSearch,
  } from '@/api/information';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const informationList = ref<InformationResult[]>([]);
  const condition = ref<InformationSearch>({
    types: [],
    filter: '',
  });

  const getImg = (type: string, id: number) => {
    let imgType = type.toLowerCase();
    if (['cash', 'consume', 'eqp', 'etc', 'ins', 'pet'].includes(type)) {
      imgType = 'item';
    }
    return getIconUrl(imgType, id);
  };

  const searchData = async () => {
    if (!condition.value.filter) {
      Message.error({
        content: t('informationSearch.check.filter'),
        duration: 3 * 1000,
      });
      return;
    }
    setLoading(true);
    try {
      const { data } = await informationSearch(condition.value);
      informationList.value = data;
    } finally {
      setLoading(false);
    }
  };

  const resetSearch = () => {
    condition.value.types = [];
    condition.value.filter = '';
  };

  const getTag = (type: string) => {
    let tag;
    switch (type) {
      case 'cash':
      case 'consume':
      case 'eqp':
      case 'etc':
      case 'ins':
      case 'map':
      case 'mob':
      case 'npc':
      case 'pet':
      case 'skill':
        tag = t(`informationSearch.type.${type}`);
        break;
      default:
        tag = type;
        break;
    }
    return tag;
  };
</script>

<script lang="ts">
  export default {
    name: 'InformationSearch',
  };
</script>

<style lang="less" scoped></style>
