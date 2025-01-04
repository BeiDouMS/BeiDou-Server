<template>
  <div class="container" :loading="true">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.dashboard.workplace')">
      <a-row>
        <a-col>
          {{ $t('workplace.gameServer.status') }}
          <a-tag v-if="serverStatus" color="green" bordered>
            {{ $t('workplace.running') }}
          </a-tag>
          <a-tag v-else color="gray" bordered>
            {{ $t('workplace.stopped') }}
          </a-tag>
        </a-col>
      </a-row>
      <a-row>
        <a-col>
          <a-space>
            <a-button
              :loading="loading"
              type="primary"
              :disabled="serverStatus"
              status="success"
              @click="changeServerStatusClick('start')"
            >
              {{ $t('workplace.button.start') }}
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              :disabled="!serverStatus"
              status="danger"
              @click="changeServerStatusClick('stop')"
            >
              {{ $t('workplace.button.stop') }}
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              :disabled="!serverStatus"
              status="warning"
              @click="changeServerStatusClick('restart')"
            >
              {{ $t('workplace.button.restart') }}
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              status="danger"
              @click="changeServerStatusClick('shutdown')"
            >
              {{ $t('workplace.button.shutdown') }}
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              @click="handleReloadEvents"
            >
              重载事件
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              @click="handleReloadMaps"
            >
              重载地图
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              @click="handleReloadPortals"
            >
              重载传送点
            </a-button>
            <a-modal
              v-model:visible="visible"
              :width="450"
              draggable
              :ok-button-props="{ status: 'danger' }"
              @ok="handleOk"
            >
              <template #title>
                {{ $t('workplace.button.shutdown') }}
              </template>
              <div>{{ $t('workplace.button.shutdown.confirm') }}</div>
            </a-modal>
          </a-space>
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
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

  const { loading, setLoading } = useLoading(false);
  const serverStatus = ref<boolean>(false);
  const visible = ref(false);
  const router = useRouter();

  const loadSeverStatus = async () => {
    setLoading(true);
    try {
      const { data } = await getServerStatus();
      serverStatus.value = data;
    } finally {
      setLoading(false);
    }
  };

  loadSeverStatus();

  const handleOk = async () => {
    shutdown();
    await router.push({
      name: 'login',
      query: {
        ...router.currentRoute.value.query,
        redirect: router.currentRoute.value.path,
      },
    });
  };

  const changeServerStatusClick = async (
    type: 'start' | 'restart' | 'stop' | 'shutdown'
  ) => {
    setLoading(true);
    try {
      switch (type) {
        case 'start':
          await startServer();
          break;
        case 'stop':
          await stopServer();
          break;
        case 'restart':
          await restartServer();
          break;
        case 'shutdown':
          visible.value = true;
          return;
        default:
          break;
      }

      Message.success('操作成功');
    } catch (err) {
      window.console.log(err);
      Message.error('请求失败');
    } finally {
      await loadSeverStatus();
      setLoading(false);
    }
  };

  const handleReloadEvents = () => {
    reloadEventsByGMCommand()
      .then((response) => {
        Message.success('事件重载成功');
      })
      .catch((error) => {
        Message.error('事件重载失败');
      });
  };

  const handleReloadMaps = () => {
    reloadMapsByGMCommand()
      .then((response) => {
        Message.success('地图重载成功');
      })
      .catch((error) => {
        Message.error('地图重载失败');
      });
  };

  const handleReloadPortals = () => {
    reloadPortalsByGMCommand()
      .then((response) => {
        Message.success('传送点重载成功');
      })
      .catch((error) => {
        Message.error('传送点重载失败');
      });
  };
</script>

<script lang="ts">
  export default {
    name: 'Dashboard',
  };
</script>

<style lang="less" scoped></style>
