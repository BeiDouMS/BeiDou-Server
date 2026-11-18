<template>
  <a-list :bordered="false">
    <a-list-item
      v-for="item in renderList"
      :key="item.id"
      action-layout="vertical"
      :style="{
        opacity: item.status ? 0.5 : 1,
      }"
    >
      <template #extra>
        <a-tag v-if="item.messageType === 0" color="gray">
          {{ $t('component.messageBox.status.notStarted') }}
        </a-tag>
        <a-tag v-else-if="item.messageType === 1" color="green">
          {{ $t('component.messageBox.status.opened') }}
        </a-tag>
        <a-tag v-else-if="item.messageType === 2" color="blue">
          {{ $t('component.messageBox.status.inProgress') }}
        </a-tag>
        <a-tag v-else-if="item.messageType === 3" color="red">
          {{ $t('component.messageBox.status.expiring') }}
        </a-tag>
      </template>
      <div class="item-wrap" @click="onItemClick(item)">
        <a-list-item-meta>
          <template v-if="item.avatar" #avatar>
            <a-avatar shape="circle">
              <img v-if="item.avatar" :src="item.avatar" />
              <icon-desktop v-else />
            </a-avatar>
          </template>
          <template #title>
            <a-space :size="4">
              <span>{{ item.title }}</span>
              <a-typography-text type="secondary">
                {{ item.subTitle }}
              </a-typography-text>
            </a-space>
          </template>
          <template #description>
            <div>
              <a-typography-paragraph
                :ellipsis="{
                  rows: 1,
                }"
                >{{ item.content }}</a-typography-paragraph
              >
              <a-typography-text
                v-if="item.type === 'message'"
                class="time-text"
              >
                {{ item.time }}
              </a-typography-text>
            </div>
          </template>
        </a-list-item-meta>
      </div>
    </a-list-item>
    <template #footer>
      <a-space
        fill
        :size="0"
        :class="{ 'add-border-top': renderList.length < showMax }"
      >
        <div class="footer-wrap">
          <a-link @click="allRead">{{
            $t('component.messageBox.allRead')
          }}</a-link>
        </div>
        <div class="footer-wrap">
          <a-link>{{ $t('component.messageBox.viewMore') }}</a-link>
        </div>
      </a-space>
    </template>
    <div
      v-if="renderList.length && renderList.length < 3"
      :style="{ height: (showMax - renderList.length) * 86 + 'px' }"
    ></div>
  </a-list>
</template>

<script lang="ts" setup>
  import { PropType } from 'vue';
  import { MessageRecord, MessageListType } from '@/api/message';

  const props = defineProps({
    renderList: {
      type: Array as PropType<MessageListType>,
      required: true,
    },
    unreadCount: {
      type: Number,
      default: 0,
    },
  });
  const emit = defineEmits(['itemClick']);
  const allRead = () => {
    emit('itemClick', [...props.renderList]);
  };

  const onItemClick = (item: MessageRecord) => {
    if (!item.status) {
      emit('itemClick', [item]);
    }
  };
  const showMax = 3;
</script>

<style scoped lang="less">
  :deep(.arco-list) {
    .arco-list-item {
      min-height: 86px;
      border-bottom: 1px solid rgb(var(--gray-3));
    }
    .arco-list-item-extra {
      position: absolute;
      right: 20px;
    }
    .arco-list-item-meta-content {
      flex: 1;
    }
    .item-wrap {
      cursor: pointer;
    }
    .time-text {
      font-size: 12px;
      color: rgb(var(--gray-6));
    }
    .arco-empty {
      display: none;
    }
    .arco-list-footer {
      padding: 0;
      height: 50px;
      line-height: 50px;
      border-top: none;
      .arco-space-item {
        width: 100%;
        border-right: 1px solid rgb(var(--gray-3));
        &:last-child {
          border-right: none;
        }
      }
      .add-border-top {
        border-top: 1px solid rgb(var(--gray-3));
      }
    }
    .footer-wrap {
      text-align: center;
    }
    .arco-typography {
      margin-bottom: 0;
    }
    .add-border {
      border-top: 1px solid rgb(var(--gray-3));
    }
  }
</style>
