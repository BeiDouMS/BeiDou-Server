<template>
  <div class="container" :loading="loading">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.dashboard.workplace')">
      <a-card
        class="status-card"
        :title="$t('workplace.gameServer.status')"
        :bordered="false"
      >
        <a-row>
          <a-col>
            {{ $t('workplace.gameServer.currently') }}
            <a-tag v-if="serverStatus === 'running'" color="green" bordered>
              {{ $t('workplace.running') }}
            </a-tag>
            <a-tag v-else color="gray" bordered>
              {{ $t('workplace.stopped') }}
            </a-tag>
          </a-col>
        </a-row>
      </a-card>

      <a-card
        class="control-card"
        :title="$t('workplace.gameServer.serverControl')"
        :bordered="false"
      >
        <a-space class="button-group" :size="16">
          <a-button
            v-for="(btn, index) in serverControlButtons"
            :key="index"
            :loading="loading && btn.action !== 'stop'"
            type="primary"
            :disabled="btn.disabled(serverStatus)"
            :status="btn.status"
            @click="handleButtonClick(btn.action)"
          >
            <template #icon>
              <component :is="btn.icon" />
            </template>
            {{ $t(`workplace.button.${btn.label}`) }}
          </a-button>
        </a-space>
      </a-card>

      <a-card
        class="reload-card"
        :title="$t('workplace.dataReload')"
        :bordered="false"
      >
        <a-space class="button-group" :size="16">
          <a-button
            v-for="(btn, index) in dataReloadButtons"
            :key="index + 'reload'"
            :loading="loading"
            type="primary"
            @click="handleButtonClick(btn.action)"
          >
            <template #icon>
              <component :is="btn.icon" />
            </template>
            {{ $t(`workplace.button.${btn.label}`) }}
          </a-button>
        </a-space>
      </a-card>

      <!-- 完全停服并退出BAT的确认框 -->
      <a-modal
        v-model:visible="shutdownConfirmVisible"
        class="arco-modal-auto"
        draggable
        @ok="handleShutdownConfirm"
        @cancel="handleShutdownCancel"
      >
        <template #title>
          {{ $t('workplace.button.shutdown') }}
        </template>
        <p>{{ $t('workplace.button.shutdown.confirm') }}</p>
      </a-modal>

      <!-- 重启服务端的确认框 -->
      <a-modal
        v-model:visible="restartConfirmVisible"
        modal-class="arco-modal-auto"
        draggable
        @ok="handleRestartConfirm"
        @cancel="handleRestartCancel"
      >
        <template #title>
          {{ $t('workplace.button.restart') }}
        </template>
        <p>{{ $t('workplace.button.restart.confirm') }}</p>
      </a-modal>

      <!-- 停服倒计时配置框 -->
      <a-modal
        v-model:visible="stopConfigVisible"
        modal-class="arco-modal-auto"
        draggable
        @ok="handleStopConfigOk"
        @cancel="handleStopConfigCancel"
      >
        <template #title>
          {{ $t('workplace.button.stop.config') }}
        </template>
        <a-form :model="stopConfigData" layout="vertical">
          <a-card
            :title="$t('workplace.stop.minutes')"
            :bordered="false"
            style="margin-bottom: 16px"
          >
            <a-row :gutter="[16, 16]">
              <a-col :span="18">
                <a-input-number v-model="stopConfigData.minutes" :min="0" />
              </a-col>
              <a-col :span="6">
                <span style="line-height: 32px; text-align: right">{{
                  $t('workplace.unit.minutes')
                }}</span>
              </a-col>
            </a-row>
          </a-card>

          <a-card :bordered="false" style="margin-bottom: 16px">
            <template #title>
              <div style="display: flex; align-items: center">
                <span>{{ $t('workplace.stop.shutdownMsg') }}</span>
                <a-tooltip :content="$t('workplace.stop.shutdownMsgDefault')">
                  <icon-info-circle style="margin-left: 8px" />
                </a-tooltip>
              </div>
            </template>

            <a-row :gutter="[16, 16]">
              <a-col :span="24">
                <a-textarea v-model="stopConfigData.shutdownMsg" />
              </a-col>
            </a-row>
          </a-card>

          <a-card :title="$t('workplace.stop.messageTypes')" :bordered="false">
            <a-space class="button-group" :size="16">
              <a-checkbox v-model="stopConfigData.showServerMsg">
                {{ $t('workplace.stop.showServerMsg') }}
              </a-checkbox>
              <a-checkbox v-model="stopConfigData.showCenterMsg">
                {{ $t('workplace.stop.showCenterMsg') }}
              </a-checkbox>
              <a-checkbox v-model="stopConfigData.showChatMsg">
                {{ $t('workplace.stop.showChatMsg') }}
              </a-checkbox>
            </a-space>
          </a-card>
        </a-form>
      </a-modal>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, reactive, ref } from 'vue';
  import {
    getServerStatus,
    restartServer,
    shutdown,
    startServer,
    stopServer,
  } from '@/api/dashboard';
  import { Message } from '@arco-design/web-vue';
  import useLoading from '@/hooks/loading';
  import { useRouter } from 'vue-router';
  import {
    reloadEventsByGMCommand,
    reloadMapsByGMCommand,
    reloadPortalsByGMCommand,
  } from '@/api/command';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(false);
  const serverStatus = ref<'resting' | 'running'>('resting');
  const stopConfigVisible = ref(false);
  const shutdownConfirmVisible = ref(false); // 新增用于确认关机的模态框可见性控制
  const restartConfirmVisible = ref(false); // 新增用于确认重启的模态框可见性控制
  const router = useRouter();
  const stopConfigData = reactive({
    minutes: 0,
    shutdownMsg: '',
    showServerMsg: false,
    showCenterMsg: false,
    showChatMsg: false,
  });

  const serverControlButtons = [
    {
      label: 'start',
      action: 'start',
      disabled: (status: 'resting' | 'running') => status === 'running',
      status: 'success' as const,
      icon: 'icon-play-arrow-fill',
    },
    {
      label: 'stop',
      action: 'stop',
      disabled: (status: 'resting' | 'running') => status === 'resting',
      status: 'danger' as const,
      icon: 'icon-stop',
    },
    {
      label: 'restart',
      action: 'restart',
      disabled: (status: 'resting' | 'running') => status === 'resting',
      status: 'warning' as const,
      icon: 'icon-refresh',
    },
    {
      label: 'shutdown',
      action: 'shutdown',
      disabled: () => false,
      status: 'danger' as const,
      icon: 'icon-poweroff',
    },
  ];

  const dataReloadButtons = [
    { label: 'dataReloadEvents', action: 'reloadEvents', icon: 'icon-compass' },
    {
      label: 'dataReloadMaps',
      action: 'reloadMaps',
      icon: 'icon-mind-mapping',
    },
    {
      label: 'dataReloadPortals',
      action: 'reloadPortals',
      icon: 'icon-common',
    },
  ];

  const loadSeverStatus = async () => {
    setLoading(true);
    try {
      const { data } = await getServerStatus();
      serverStatus.value = data ? 'running' : 'resting';
    } finally {
      setLoading(false);
    }
  };

  onMounted(() => {
    loadSeverStatus();
  });

  const handleButtonClick = async (action: string) => {
    if (action === 'shutdown') {
      shutdownConfirmVisible.value = true;
      return;
    }
    if (action === 'restart') {
      restartConfirmVisible.value = true;
      return;
    }

    setLoading(true);
    try {
      switch (action) {
        case 'start':
          await startServer();
          break;
        case 'stop':
          stopConfigVisible.value = true;
          setLoading(false);
          return;
        case 'restart':
          await restartServer();
          break;
        case 'reloadEvents':
          await reloadEventsByGMCommand();
          break;
        case 'reloadMaps':
          await reloadMapsByGMCommand();
          break;
        case 'reloadPortals':
          await reloadPortalsByGMCommand();
          break;
        default:
          break;
      }

      Message.success(t('common.operationSuccess'));
    } catch (err) {
      console.error(err);
      Message.error(t('common.requestFailed'));
    } finally {
      await loadSeverStatus();
      setLoading(false);
    }
  };

  const handleShutdownConfirm = async () => {
    try {
      setLoading(true);
      await shutdown();
      Message.success(t('workplace.button.shutdown.success'));
      // 立即尝试更新服务器状态
      await loadSeverStatus();
    } catch (err) {
      console.error(err);
      Message.error(t('common.requestFailed'));
    } finally {
      shutdownConfirmVisible.value = false;
      setLoading(false);
    }
  };

  const handleShutdownCancel = () => {
    shutdownConfirmVisible.value = false;
  };

  const handleRestartConfirm = async () => {
    try {
      setLoading(true);
      await restartServer();
      Message.success(t('common.operationSuccess'));
    } catch (err) {
      console.error(err);
      Message.error(t('common.requestFailed'));
    } finally {
      restartConfirmVisible.value = false;
      setLoading(false);
    }
  };

  const handleRestartCancel = () => {
    restartConfirmVisible.value = false;
  };
  const handleStopConfigOk = async () => {
    try {
      setLoading(true);
      const stopConfigParams = {
        minutes: stopConfigData.minutes,
        shutdownMsg: stopConfigData.shutdownMsg,
        showServerMsg: stopConfigData.showServerMsg,
        showCenterMsg: stopConfigData.showCenterMsg,
        showChatMsg: stopConfigData.showChatMsg,
      };

      await stopServer(stopConfigParams);
      Message.success(t('workplace.stop.shutdownInProgress'));

      // 如果设置了延迟时间，则启动一个定时器，在延迟时间结束后更新服务器状态
      if (stopConfigData.minutes > 0) {
        setTimeout(async () => {
          await loadSeverStatus();
        }, stopConfigData.minutes * 60 * 1000);
      } else {
        // 如果没有设置延迟时间，立即更新服务器状态
        await loadSeverStatus();
      }

      stopConfigVisible.value = false;
    } catch (err) {
      console.error(err);
      Message.error(t('common.requestFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleStopConfigCancel = () => {
    Object.assign(stopConfigData, {
      minutes: 0,
      shutdownMsg: '',
      showServerMsg: false,
      showCenterMsg: false,
      showChatMsg: false,
    });
    stopConfigVisible.value = false;
  };
</script>

<script lang="ts">
  export default {
    name: 'Dashboard',
  };
</script>

<style lang="less" scoped>
  .button-group {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
</style>
