<template>
  <a-card class="general-card">
    <a-row>
      <a-col>
        <a-space>
          <a-button
            :disabled="condition.onSale === 1"
            type="primary"
            status="success"
            @click="changeOnSaleFilter(1)"
          >
            上架中
          </a-button>
          <a-button
            :disabled="condition.onSale === 0"
            type="primary"
            status="danger"
            @click="changeOnSaleFilter(0)"
          >
            待售
          </a-button>
          <a-button
            :disabled="condition.onSale === undefined"
            type="primary"
            @click="changeOnSaleFilter(undefined)"
          >
            全部
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
          title="SN"
          data-index="sn"
          align="center"
          :width="140"
        />
        <a-table-column title="物品" align="center" :width="100">
          <template #cell="{ record }">
            <img :src="getIconUrl('item', record.itemId)" />
          </template>
        </a-table-column>
        <a-table-column
          title="物品ID"
          data-index="itemId"
          align="center"
          :width="140"
        />
        <a-table-column
          title="数量"
          data-index="count"
          align="center"
          :width="80"
        />
        <a-table-column
          title="优先级"
          data-index="priority"
          align="center"
          :width="80"
        />
        <a-table-column
          title="售价"
          data-index="price"
          align="center"
          :width="120"
        />
        <!--        <a-table-column title="折扣" data-index="bonus" align="center" />-->
        <a-table-column
          title="有效期"
          data-index="period"
          align="center"
          :width="120"
        >
          <template #cell="{ record }"> {{ record.period }} 天 </template>
        </a-table-column>
        <!--        <a-table-column title="抵用券" data-index="maplePoint" align="center" />-->
        <!--        <a-table-column title="金币" data-index="meso" align="center" />-->
        <!--        <a-table-column-->
        <!--          title="会员专属"-->
        <!--          data-index="ForPremiumUser"-->
        <!--          align="center"-->
        <!--        />-->
        <!--        <a-table-column-->
        <!--          title="性别"-->
        <!--          data-index="CommodityGender"-->
        <!--          align="center"-->
        <!--        />-->
        <a-table-column
          title="上架"
          data-index="onSale"
          align="center"
          :width="120"
        >
          <template #cell="{ record }">
            <a-tag v-if="record.onSale" color="green">上架中</a-tag>
            <a-tag v-else color="red">待售</a-tag>
          </template>
        </a-table-column>
        <!--        <a-table-column title="标签" data-index="class" align="center" />-->
        <!--        <a-table-column title="limit" data-index="limit" align="center" />-->
        <!--        <a-table-column title="PbCash" data-index="PbCash" align="center" />-->
        <!--        <a-table-column title="PbPoint" data-index="PbPoint" align="center" />-->
        <!--        <a-table-column title="PbGift" data-index="PbGift" align="center" />-->
        <!--        <a-table-column title="礼包合集" data-index="package" align="center" />-->
        <a-table-column title="操作" fixed="right">
          <template #cell="{ record }">
            <a-button type="text" size="mini" @click="editClick(record)">
              编辑
            </a-button>
          </template>
        </a-table-column>
      </template>
    </a-table>
    <a-pagination
      style="margin-top: 20px"
      :total="total"
      :current="condition.pageNo"
      show-total
      show-jumper
      @change="pageChange"
    />
  </a-card>
  <cash-shop-form ref="cashShopFormRef" @load-data="loadData" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import { conditionState, getCommodityByCategory } from '@/api/cashShop';
  import CashShopForm from '@/views/dashboard/cashShop/form.vue';
  import { cashShopState } from '@/store/modules/cashShop/type';
  import { getIconUrl } from '@/utils/mapleStoryAPI';

  const { loading, setLoading } = useLoading(false);

  const props = defineProps<{
    topId: string | number;
    subId?: string | number;
  }>();
  const tableData = ref<cashShopState>([]);
  const total = ref<number>(0);
  const condition = ref<conditionState>({
    id: 1,
    subId: 0,
    onSale: 1,
    pageNo: 1,
  });

  const pageChange = (data: number) => {
    condition.value.pageNo = data;
    loadData();
  };

  const loadData = async () => {
    setLoading(true);
    try {
      const { data } = await getCommodityByCategory(condition.value);
      tableData.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };
  condition.value.id = props.topId as number;
  condition.value.subId = props.subId as number;
  loadData();

  const changeOnSaleFilter = (data: undefined | 0 | 1) => {
    condition.value.onSale = data;
    loadData();
  };

  const cashShopFormRef = ref();
  const editClick = (data: cashShopState) => {
    cashShopFormRef.value.initForm(data);
  };
</script>

<script lang="ts">
  export default {
    name: 'CashShopTable',
  };
</script>
