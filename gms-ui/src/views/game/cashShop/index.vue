<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.cashShop')">
      <a-tabs
        lazy-load
        destroy-on-hide
        type="card-gutter"
        :default-active-key="1"
        :active-key="topTab"
        @change="topCategoryChange"
      >
        <a-tab-pane
          v-for="topCategory in topCategoryList"
          :key="topCategory.id"
        >
          <template #title>{{ topCategory.name }}</template>
          <a-tabs
            lazy-load
            destroy-on-hide
            type="text"
            :default-active-key="0"
            :active-key="subTab"
            @change="subCategoryChange"
          >
            <a-tab-pane
              v-for="subCategory in subCategoryList"
              :key="subCategory.subId"
            >
              <template #title>{{ subCategory.subName }}</template>
              <cash-shop-table :top-id="topTab" :sub-id="subTab" />
            </a-tab-pane>
          </a-tabs>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import CashShopTable from '@/views/game/cashShop/table.vue';
  import { ref } from 'vue';
  import { getAllCategoryList } from '@/api/cashShop';
  import { categoryState } from '@/store/modules/cashShop/type';

  const topCategoryList = ref<categoryState[]>([]);
  const subCategoryList = ref<categoryState[]>([]);
  const allCategoryList = ref<categoryState[]>([]);
  const topTab = ref<string | number>(1);
  const subTab = ref<string | number>(0);

  const topCategoryChange = (tab: string | number) => {
    topTab.value = tab;
    subCategoryList.value = allCategoryList.value.filter((_data) => {
      return _data.id === tab;
    });
    subTab.value = 0;
  };
  const subCategoryChange = (tab: string | number) => {
    subTab.value = tab;
  };

  const loadCategories = async () => {
    const { data } = await getAllCategoryList();
    allCategoryList.value = data; // 暂存全部数据

    const tc = topCategoryList.value;
    data.forEach((_data: any) => {
      if (_data.id === 8) return;
      // 检查一级分类存在
      let exist = false;
      for (let tci = 0; tci < tc.length; tci += 1) {
        if (tc[tci].id === _data.id) {
          exist = true;
          break;
        }
      }
      // 插入一级分类
      if (!exist) {
        tc.push(_data);
      }

      // 初始化二级分类
      if (_data.id === 1) {
        subCategoryList.value.push(_data);
      }
    });
  };

  loadCategories();
</script>

<script lang="ts">
  export default {
    name: 'CashShop',
  };
</script>

<style lang="less" scoped></style>
