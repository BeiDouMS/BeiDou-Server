<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.gachapon')">
      <a-row
        ><a-col>
          <a-space>
            <a-input-number
              v-model="condition.gachaponId"
              placeholder="百宝箱ID"
            />
            <a-button type="primary" @click="loadData">搜索</a-button>
            <a-button @click="resetClick">重置</a-button>
            <a-button type="primary" status="success" @click="insertClick">
              创建
            </a-button>
          </a-space>
        </a-col></a-row
      >
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
            title="ID"
            data-index="id"
            :width="80"
            align="center"
            fixed="left"
          />
          <a-table-column
            title="名称"
            data-index="name"
            align="center"
            :width="200"
            fixed="left"
          />
          <a-table-column title="百宝箱ID" align="center" :width="120">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="gachaponIdClick(record.gachaponId)"
              >
                {{ record.gachaponId }}
              </a-button>
            </template>
          </a-table-column>
          <a-table-column title="百宝箱名称" align="center" :width="120">
            <template #cell="{ record }">
              {{ record.gachaponName }}
            </template>
          </a-table-column>
          <a-table-column title="百宝箱图片" align="center" :width="120">
            <template #cell="{ record }">
              <img :src="getIconUrl('npc', record.gachaponId)" />
            </template>
          </a-table-column>
          <!--          <a-table-column-->
          <!--            title="权重"-->
          <!--            data-index="weight"-->
          <!--            align="center"-->
          <!--            :width="120"-->
          <!--          />-->
          <a-table-column title="公共池" align="center" :width="120">
            <template #cell="{ record }">
              <a-tag v-if="record.isPublic" color="red">公共池</a-tag>
              <a-tag v-else color="blue">非公共池</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="中奖率" align="center" :width="120">
            <template #cell="{ record }">
              <span
                v-if="filterGachaponId !== undefined && filterGachaponId !== -1"
              >
                {{ record.realProb / 10000 }} %
              </span>
            </template>
          </a-table-column>
          <!--          <a-table-column-->
          <!--            title="固定概率"-->
          <!--            data-index="prob"-->
          <!--            align="center"-->
          <!--            :width="120"-->
          <!--          />-->
          <a-table-column title="生效时间" align="center" :width="230">
            <template #cell="{ record }">
              {{
                record.startTime != null
                  ? timestampToChineseTime(record.startTime)
                  : '已开始'
              }}
            </template>
          </a-table-column>
          <a-table-column title="结束时间" align="center" :width="230">
            <template #cell="{ record }">
              {{
                record.endTime != null
                  ? timestampToChineseTime(record.endTime)
                  : '永久'
              }}
            </template>
          </a-table-column>
          <a-table-column
            title="全服广播"
            data-index="notification"
            align="center"
            :width="120"
          >
            <template #cell="{ record }">
              <a-tag v-if="record.notification" color="red">是</a-tag>
              <a-tag v-else color="green">否</a-tag>
            </template>
          </a-table-column>
          <a-table-column
            title="备注"
            data-index="comment"
            :width="400"
            align="center"
          />
          <a-table-column
            title="操作"
            :width="200"
            align="center"
            fixed="right"
          >
            <template #cell="{ record }">
              <div v-if="filterGachaponId != undefined">
                <a-button size="mini" type="text" @click="editClick(record)">
                  编辑
                </a-button>
                <a-button
                  size="mini"
                  type="text"
                  @click="showRewardFormClick(record)"
                >
                  奖品
                </a-button>
                <a-popconfirm
                  type="error"
                  content="你确定要删除这个奖池吗？"
                  @ok="deleteClick(record)"
                >
                  <a-button size="mini" status="danger" type="text">
                    删除
                  </a-button>
                </a-popconfirm>
              </div>
              <a-button
                v-else
                size="mini"
                type="text"
                @click="gachaponIdClick(record.gachaponId)"
              >
                显示操作按钮
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
        :page-size-options="[20, 40, 60, 100]"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
    <GachaponForm ref="gachaponForm" @load-data="loadData" />
    <GachaponRewardForm ref="gachaponRewardForm" />
  </div>
</template>

<script lang="ts" setup>
  import useLoading from '@/hooks/loading';
  import {
    deletePool,
    GachaponPoolSearchCondition,
    getPools,
  } from '@/api/gachapon';
  import { ref } from 'vue';
  import { GachaponPoolState } from '@/store/modules/gachapon/type';
  import { timestampToChineseTime } from '@/utils/stringUtils';
  import GachaponForm from '@/views/game/gachapon/form.vue';
  import { getIconUrl } from '@/utils/mapleStoryAPI';
  import GachaponRewardForm from '@/views/game/gachapon/reward.vue';
  import { Message } from '@arco-design/web-vue';

  const { loading, setLoading } = useLoading(false);
  const tableData = ref<GachaponPoolState[]>([]);
  const condition = ref<GachaponPoolSearchCondition>({
    gachaponId: undefined,
    pageNo: 1,
    pageSize: 20,
  });
  const total = ref<number>(0);
  const filterGachaponId = ref<number | undefined>(undefined);
  const editGachaponId = ref<number | undefined>(undefined);
  const pageChange = (data: number) => {
    condition.value.pageNo = data;
    loadData();
  };

  const pageSizeChange = (data: number) => {
    condition.value.pageNo = 1;
    condition.value.pageSize = data;
    loadData();
  };
  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getPools(condition.value);
      tableData.value = data.records;
      total.value = data.totalRow;
      filterGachaponId.value = condition.value.gachaponId;
      editGachaponId.value = undefined;
    } finally {
      setLoading(false);
    }
  };
  loadData();

  const resetClick = () => {
    condition.value = {
      gachaponId: undefined,
      pageNo: 1,
      pageSize: 20,
    };
    loadData();
  };

  const gachaponIdClick = (id: number) => {
    condition.value = {
      gachaponId: id,
      pageNo: 1,
      pageSize: 20,
    };
    loadData();
  };

  const deleteClick = async (record: GachaponPoolState) => {
    setLoading(true);
    try {
      await deletePool(record);
      Message.success('奖池已删除');
      await loadData();
    } finally {
      setLoading(false);
    }
  };

  const gachaponForm = ref();
  const insertClick = () => {
    gachaponForm.value.initForm();
  };
  const editClick = (record: GachaponPoolState) => {
    gachaponForm.value.initForm(record);
  };

  const gachaponRewardForm = ref();
  const showRewardFormClick = (record: GachaponPoolState) => {
    gachaponRewardForm.value.initForm(record);
  };
</script>

<script lang="ts">
  export default {
    name: 'Gachapon',
  };
</script>

<style lang="less" scoped></style>
