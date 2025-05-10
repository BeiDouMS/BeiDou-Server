<template>
  <div class="container" :loading="true">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.npcShop')">
      <a-row>
        <a-col>
          <a-input-number v-model="shopFilter.shopId" placeholder="商店 ID" />
          <a-input-number v-model="shopFilter.npcId" placeholder="NPC ID" />
          <a-input v-model="shopFilter.npcName" placeholder="NPC 名称" />
          <a-input-number v-model="shopFilter.itemId" placeholder="物品 ID" />
          <a-input v-model="shopFilter.itemName" placeholder="物品" />
          <a-space>
            <a-button type="primary" status="success" @click="loadClick">
              搜索
            </a-button>
            <a-button @click="resetClick">重置</a-button>
            <a-divider
              v-if="shopId > 0"
              style="height: 24px"
              direction="vertical"
            />
            <a-button v-if="shopId > 0" type="primary" @click="insertItemClick">
              新增
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-table
        v-show="shopId <= 0"
        row-key="id"
        :loading="loading"
        :data="shopList"
        column-resizable
        :pagination="false"
        :bordered="{ cell: true }"
      >
        <template #columns>
          <a-table-column
            title="商店ID"
            data-index="shopId"
            :width="100"
            align="center"
          />
          <a-table-column
            title="NPC ID"
            data-index="npcId"
            :width="100"
            align="center"
          />
          <a-table-column
            title="NPC"
            data-index="npcName"
            :width="200"
            align="center"
          />
          <a-table-column
            title="NPC图片"
            data-index="npcId"
            :width="100"
            align="center"
          >
            <template #cell="{ record }">
              <img :src="getIconUrl('npc', record.npcId)" />
            </template>
          </a-table-column>
          <a-table-column
            title="操作"
            data-index="edit"
            :width="80"
            fixed="right"
            align="center"
          >
            <template #cell="{ record }">
              <a-button
                type="text"
                size="mini"
                @click="showShopItemClick(record.shopId)"
              >
                查看
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-table
        v-if="shopId > 0"
        row-key="id"
        :loading="loading"
        :data="shopItemList"
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
          />
          <a-table-column
            title="商店ID"
            data-index="shopId"
            :width="100"
            align="center"
          />
          <a-table-column title="物品图片" align="center" :width="100">
            <template #cell="{ record }">
              <img :src="getIconUrl('item', record.itemId)" />
            </template>
          </a-table-column>
          <a-table-column title="物品ID" :width="100" align="center">
            <template #cell="{ record }">
              <span v-if="record.id === undefined || editMode === record.id">
                <a-input-number v-model="record.itemId" />
              </span>
              <span v-else>{{ record.itemId }}</span>
            </template>
          </a-table-column>
          <a-table-column
            title="物品"
            data-index="itemName"
            :width="120"
            align="center"
          />
          <a-table-column title="价格" :width="100" align="center">
            <template #cell="{ record }">
              <span v-if="record.id === undefined || editMode === record.id">
                <a-input-number v-model="record.price" />
              </span>
              <span v-else>{{ record.price }}</span>
            </template>
          </a-table-column>
          <a-table-column title="音符" :width="100" align="center">
            <template #cell="{ record }">
              <span v-if="record.id === undefined || editMode === record.id">
                <a-input-number v-model="record.pitch" />
              </span>
              <span v-else>{{ record.pitch }}</span>
            </template>
          </a-table-column>
          <a-table-column title="位置" :width="100" align="center">
            <template #cell="{ record }">
              <span v-if="record.id === undefined || editMode === record.id">
                <a-input-number v-model="record.position" />
              </span>
              <span v-else>{{ record.position }}</span>
            </template>
          </a-table-column>
          <a-table-column title="描述" :width="250" data-index="itemDesc" />
          <a-table-column title="操作">
            <template #cell="{ record }">
              <a-space :size="0">
                <a-button
                  v-if="record.id !== undefined && editMode !== record.id"
                  type="text"
                  size="mini"
                  status="normal"
                  @click="editMode = record.id"
                >
                  编辑
                </a-button>
                <a-popconfirm
                  v-if="record.id !== undefined && editMode !== record.id"
                  content="确定要删除吗？"
                  position="top"
                  @ok="deleteClick(record.id)"
                >
                  <a-button type="text" size="mini" status="danger">
                    删除
                  </a-button>
                </a-popconfirm>
                <a-button
                  v-if="record.id === undefined || editMode === record.id"
                  type="text"
                  size="mini"
                  status="success"
                  @click="saveClick(record)"
                >
                  保存
                </a-button>
                <a-button
                  v-if="record.id !== undefined && editMode === record.id"
                  type="text"
                  size="mini"
                  status="normal"
                  @click="rollbackClick(record)"
                >
                  返回
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 20px"
        :total="total"
        :page-size="shopFilter.pageSize"
        :current="shopFilter.pageNo"
        show-total
        show-jumper
        show-page-size
        :page-size-options="[7, 14, 35, 70]"
        @change="pageChange"
        @page-size-change="pageSizeChange"
      />
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import {
    addShopItem,
    deleteShopItem,
    getShopFilter,
    getShopItemList,
    getShopList,
    updateShopItem,
  } from '@/api/npcShop';
  import { NpcShopItemState, NpcShopState } from '@/store/modules/npcShop/type';
  import { Message } from '@arco-design/web-vue';
  import { getIconUrl } from '@/utils/mapleStoryAPI';

  const { loading, setLoading } = useLoading(false);

  const total = ref<number>(0);
  const pageChange = (data: number) => {
    shopFilter.value.pageNo = data;
    if (shopId.value === -1) {
      loadShopList();
    } else {
      loadShopItemList();
    }
  };

  const pageSizeChange = (data: number) => {
    shopFilter.value.pageNo = 1;
    shopFilter.value.pageSize = data;
    if (shopId.value === -1) {
      loadShopList();
    } else {
      loadShopItemList();
    }
  };

  const shopId = ref<number>(-1);

  const shopFilter = ref<getShopFilter>({
    pageNo: 1,
    pageSize: 20,
    onlyTotal: false,
    notPage: false,
    shopId: undefined,
    npcId: undefined,
    npcName: undefined,
    itemId: undefined,
    itemName: undefined,
  });

  const loadClick = () => {
    shopFilter.value.pageNo = 1;
    loadShopList();
  };

  const resetClick = () => {
    shopFilter.value = {
      pageNo: 1,
      pageSize: 20,
      onlyTotal: false,
      notPage: false,
      shopId: undefined,
      npcId: undefined,
      npcName: undefined,
      itemId: undefined,
      itemName: undefined,
    };
    loadShopList();
  };

  const shopList = ref<NpcShopState[]>();
  const loadShopList = async () => {
    setLoading(true);
    try {
      shopId.value = -1;
      const { data } = await getShopList(shopFilter.value);
      shopList.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };
  loadShopList();

  const showShopItemClick = (sid: number) => {
    shopId.value = sid;
    shopFilter.value.shopId = sid;
    loadShopItemList();
  };

  const shopItemList = ref<NpcShopItemState[]>();
  const loadShopItemList = async () => {
    setLoading(true);
    try {
      const { data } = await getShopItemList(shopFilter.value);
      shopItemList.value = data.records;
      total.value = data.totalRow;
    } finally {
      setLoading(false);
    }
  };

  const editMode = ref<number>(-1);

  const insertItemClick = () => {
    shopItemList.value?.unshift({
      id: -1,
      shopId: shopId.value,
      itemId: undefined,
      price: 0,
      pitch: 0,
      position: 1,
      itemName: undefined,
      itemDesc: undefined,
    });
  };

  const saveClick = async (data: NpcShopItemState) => {
    setLoading(true);
    try {
      if (data.id === -1) {
        await addShopItem(data);
        Message.success('新增商品成功');
      } else {
        await updateShopItem(data);
        Message.success('更新商品成功');
      }
    } finally {
      editMode.value = -1;
      setLoading(false);
      await loadShopItemList();
    }
  };

  const deleteClick = async (id: number) => {
    setLoading(true);
    try {
      await deleteShopItem(id);
      Message.success('商品已删除');
    } finally {
      setLoading(false);
      await loadShopItemList();
    }
  };

  const rollbackClick = async (record: NpcShopItemState) => {
    setLoading(true);
    if (record.id === -1) {
      const index = shopItemList.value?.findIndex((item) => item === record);
      if (index != null && index > -1) {
        shopItemList.value?.splice(index, 1);
      }
    }
    editMode.value = -1;
    setLoading(false);
  };
</script>

<script lang="ts">
  export default {
    name: 'NpcShop',
  };
</script>

<style lang="less" scoped>
  :deep(.arco-card-body, .arco-row) {
    width: 100%;
  }
  .arco-input-wrapper {
    margin-right: 0;
    margin-bottom: 5px;
    width: 100%;
  }
  @media (min-width: 500px) {
    .arco-input-wrapper {
      margin-right: 8px;
      width: 140px;
    }
  }
</style>
