<template>
  <div class="container">
    <Breadcrumb />
    <a-card
      class="general-card"
      :title="$t('menu.dashboard.informationSearch')"
    >
      <a-row>
        <a-select
          v-model="condition.types"
          :placeholder="$t('informationSearch.placeholder.type')"
          :readonly="true"
          multiple
          :max-tag-count="3"
          allow-clear
          class="a-space-son"
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
          class="a-space-son"
          @keydown.enter="searchData"
        />
        <a-button type="primary" @click="searchData">
          {{ $t('button.search') }}
        </a-button>
        <a-button @click="resetSearch">
          {{ $t('button.reset') }}
        </a-button>
      </a-row>
      <a-table
        row-key="id"
        :loading="loading"
        :data="informationList"
        column-resizable
        :pagination="false"
        :bordered="{ wrapper: true, cell: true }"
      >
        <template #columns>
          <a-table-column
            :title="$t('informationSearch.column.type')"
            data-index="type"
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
            align="center"
          />
          <a-table-column
            :title="$t('informationSearch.column.name')"
            data-index="name"
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
            align="center"
            :width="400"
            :style="{ minWidth: '400px' }"
          />
          <a-table-column
            :title="$t('informationSearch.column.operation')"
            align="center"
            :width="120"
          >
            <template #cell="{ record }">
              <a-button
                v-if="['consume', 'eqp', 'etc', 'ins'].includes(record.type)"
                type="primary"
                size="mini"
                @click="handleDistribute(record)"
              >
                {{ $t('informationSearch.button.distribute') }}
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 选择玩家弹窗 -->
    <a-modal
      v-model:visible="selectorVisible"
      :title="$t('informationSearch.characterSelector.title')"
      :width="750"
      :footer="false"
    >
      <a-form :model="selectorCondition">
        <a-form-item
          :label="$t('informationSearch.characterSelector.column.id')"
        >
          <a-input-number v-model="selectorCondition.id" allow-clear />
        </a-form-item>
        <a-form-item
          :label="$t('informationSearch.characterSelector.column.name')"
        >
          <a-input v-model="selectorCondition.name" allow-clear />
        </a-form-item>
        <a-space class="a-form-item-btn">
          <a-button type="primary" @click="searchCharacter">
            {{ $t('informationSearch.characterSelector.searchButton') }}
          </a-button>
        </a-space>
      </a-form>
      <a-table :data="characterList" row-key="characterId" :pagination="false">
        <template #columns>
          <a-table-column
            :title="$t('informationSearch.characterSelector.column.id')"
            data-index="id"
            align="center"
          />
          <a-table-column
            :title="$t('informationSearch.characterSelector.column.name')"
            data-index="name"
            align="center"
          />
          <a-table-column
            :title="$t('informationSearch.characterSelector.column.operation')"
          >
            <template #cell="{ record }">
              <a-button
                type="primary"
                size="mini"
                @click="selectCharacter(record)"
              >
                {{ $t('informationSearch.characterSelector.selectButton') }}
              </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-modal>

    <!-- 下发弹窗（普通道具） -->
    <a-modal
      v-model:visible="itemFormVisible"
      :title="itemFormTitle"
      :ok-loading="loading"
      :mask-closable="false"
      :esc-to-close="false"
      :ok-text="$t('informationSearch.button.confirm')"
      :on-before-ok="submitItem"
    >
      <a-form :model="itemForm" layout="horizontal">
        <a-form-item :label="$t('informationSearch.form.player')">
          <a-space>
            <a-tag color="red">{{ itemForm.playerId }}</a-tag>
            <a-tag color="blue">{{ itemForm.player }}</a-tag>
          </a-space>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.itemId')">
          <a-tag color="arcoblue">{{ itemForm.id }}</a-tag>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.itemName')">
          <span>{{ currentItemName }}</span>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.quantity')">
          <a-input-number v-model="itemForm.quantity" :min="1" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 下发弹窗（装备） -->
    <a-modal
      v-model:visible="equipFormVisible"
      :title="equipFormTitle"
      :ok-loading="loading"
      :mask-closable="false"
      :esc-to-close="false"
      :ok-text="$t('informationSearch.button.confirm')"
      :on-before-ok="submitEquip"
    >
      <a-form :model="equipForm" layout="horizontal">
        <a-form-item :label="$t('informationSearch.form.player')">
          <a-space>
            <a-tag color="red">{{ equipForm.playerId }}</a-tag>
            <a-tag color="blue">{{ equipForm.player }}</a-tag>
          </a-space>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.itemId')">
          <a-tag color="arcoblue">{{ equipForm.id }}</a-tag>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.itemName')">
          <span>{{ currentItemName }}</span>
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.str')">
          <a-input-number v-model="equipForm.str" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.dex')">
          <a-input-number v-model="equipForm.dex" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.int')">
          <a-input-number v-model="equipForm.int" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.luk')">
          <a-input-number v-model="equipForm.luk" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.hp')">
          <a-input-number v-model="equipForm.hp" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.mp')">
          <a-input-number v-model="equipForm.mp" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.pAtk')">
          <a-input-number v-model="equipForm.pAtk" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.mAtk')">
          <a-input-number v-model="equipForm.mAtk" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.pDef')">
          <a-input-number v-model="equipForm.pDef" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.mDef')">
          <a-input-number v-model="equipForm.mDef" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.acc')">
          <a-input-number v-model="equipForm.acc" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.avoid')">
          <a-input-number v-model="equipForm.avoid" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.hands')">
          <a-input-number v-model="equipForm.hands" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.speed')">
          <a-input-number v-model="equipForm.speed" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.jump')">
          <a-input-number v-model="equipForm.jump" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.upgradeSlot')">
          <a-input-number v-model="equipForm.upgradeSlot" />
        </a-form-item>
        <a-form-item :label="$t('informationSearch.form.expire')">
          <a-input-number
            v-model="equipForm.expire"
            :placeholder="$t('informationSearch.form.expire.placeholder')"
          />
        </a-form-item>
      </a-form>
    </a-modal>
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
  import {
    getPlayerList,
    GiveForm,
    givePlayerSrc,
    getEquInitialInfo,
  } from '@/api/player';

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

  // ==================== 下发逻辑 ====================
  const selectorVisible = ref(false);
  const characterList = ref<any[]>([]);
  const selectorCondition = ref<{
    id?: number;
    name?: string;
    pageNo: number;
    pageSize: number;
  }>({
    id: undefined,
    name: undefined,
    pageNo: 1,
    pageSize: 20,
  });
  const currentItem = ref<InformationResult | null>(null);
  const currentItemName = ref('');

  const itemFormVisible = ref(false);
  const itemFormTitle = ref('');
  const itemForm = ref<GiveForm>({
    type: 5,
    worldId: undefined,
    playerId: undefined,
    player: undefined,
    id: undefined,
    quantity: 1,
  });

  const equipFormVisible = ref(false);
  const equipFormTitle = ref('');
  const equipForm = ref<GiveForm>({
    type: 6,
    worldId: undefined,
    playerId: undefined,
    player: undefined,
    id: undefined,
    quantity: 1,
    str: undefined,
    dex: undefined,
    int: undefined,
    luk: undefined,
    hp: undefined,
    mp: undefined,
    pAtk: undefined,
    mAtk: undefined,
    pDef: undefined,
    mDef: undefined,
    acc: undefined,
    avoid: undefined,
    hands: undefined,
    speed: undefined,
    jump: undefined,
    upgradeSlot: undefined,
    expire: undefined,
  });

  const handleDistribute = (record: InformationResult) => {
    currentItem.value = record;
    currentItemName.value = record.name;
    selectorCondition.value = {
      characterId: undefined,
      characterName: undefined,
      pageNo: 1,
      pageSize: 20,
    };
    characterList.value = [];
    selectorVisible.value = true;
  };

  const searchCharacter = async () => {
    setLoading(true);
    try {
      const { data } = await getPlayerList(
        selectorCondition.value.pageNo,
        selectorCondition.value.pageSize,
        selectorCondition.value.id,
        selectorCondition.value.name
      );
      if (!data.records || data.records.length === 0) {
        Message.warning(t('informationSearch.characterSelector.notFound'));
      }
      characterList.value = data.records;
    } finally {
      setLoading(false);
    }
  };

  const selectCharacter = async (record: any) => {
    const playerId = record.id;
    const playerName = record.name;

    if (currentItem.value?.type === 'eqp') {
      // 装备类型
      equipForm.value = {
        type: 6,
        worldId: record.world,
        playerId,
        player: playerName,
        id: currentItem.value?.id,
        quantity: 1,
        str: undefined,
        dex: undefined,
        int: undefined,
        luk: undefined,
        hp: undefined,
        mp: undefined,
        pAtk: undefined,
        mAtk: undefined,
        pDef: undefined,
        mDef: undefined,
        acc: undefined,
        avoid: undefined,
        hands: undefined,
        speed: undefined,
        jump: undefined,
        upgradeSlot: undefined,
        expire: undefined,
      };
      equipFormTitle.value = `${t('informationSearch.button.distribute')} - ${
        currentItem.value?.name
      }`;
      equipFormVisible.value = true;
      // 自动获取装备初始属性
      await loadEquipInitialInfo(currentItem.value?.id as number);
    } else {
      // 普通道具类型
      itemForm.value = {
        type: 5,
        worldId: record.world,
        playerId,
        player: playerName,
        id: currentItem.value?.id,
        quantity: 1,
      };
      itemFormTitle.value = `${t('informationSearch.button.distribute')} - ${
        currentItem.value?.name
      }`;
      itemFormVisible.value = true;
    }
    selectorVisible.value = false;
  };

  const loadEquipInitialInfo = async (id: number) => {
    if (!id) return;
    setLoading(true);
    try {
      const { data } = await getEquInitialInfo(id);
      equipForm.value.str = data.str;
      equipForm.value.dex = data.dex;
      equipForm.value.int = data.int;
      equipForm.value.luk = data.luk;
      equipForm.value.hp = data.hp;
      equipForm.value.mp = data.mp;
      equipForm.value.pAtk = data.patk;
      equipForm.value.mAtk = data.matk;
      equipForm.value.pDef = data.pdef;
      equipForm.value.mDef = data.mdef;
      equipForm.value.acc = data.acc;
      equipForm.value.avoid = data.avoid;
      equipForm.value.hands = data.hands;
      equipForm.value.speed = data.speed;
      equipForm.value.jump = data.jump;
      equipForm.value.upgradeSlot = data.upgradeSlot;
      equipForm.value.expire = data.expire;
    } finally {
      setLoading(false);
    }
  };

  const submitItem = async () => {
    setLoading(true);
    try {
      await givePlayerSrc(itemForm.value);
      Message.success(t('message.success'));
      itemFormVisible.value = false;
    } finally {
      setLoading(false);
    }
  };

  const submitEquip = async () => {
    setLoading(true);
    try {
      await givePlayerSrc(equipForm.value);
      Message.success(t('message.success'));
      equipFormVisible.value = false;
    } finally {
      setLoading(false);
    }
  };
</script>

<script lang="ts">
  export default {
    name: 'InformationSearch',
  };
</script>

<style lang="less" scoped>
  .arco-card-body > .arco-row > {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  :deep(.a-space-son) {
    width: 400px;
    max-width: 100%;
  }
  :deep(.arco-table-th:nth-child(1)) {
    min-width: 70px;
  }
  :deep(.arco-table-th:nth-child(2)) {
    min-width: 100px;
  }
  :deep(.arco-table-th:nth-child(3)) {
    min-width: 50px;
    max-width: 150px;
  }
  :deep(.arco-table-th:nth-child(4)) {
    min-width: 400px;
  }
  :deep(.arco-table-th:nth-child(5)) {
    min-width: 100px;
  }

  .a-form-item-btn {
    margin-left: 0px;
    margin-bottom: 15px;
    width: 100%;
    display: flex;
    justify-content: right;
    align-items: center;
  }
  @media (min-width: @screen-xs) {
    .a-form-item-btn {
      margin-left: 10px;
      margin-bottom: 0px;
      width: auto;
      display: flex;
      justify-content: center;
      align-items: start;
    }
  }
</style>
