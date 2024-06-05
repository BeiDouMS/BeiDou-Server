<template>
  <div class="container" :loading="true">
    <Breadcrumb />
    <a-card class="general-card" title="Cosmic Nap">
      <a-row>
        <a-col>
          Game Server 目前
          <a-tag v-if="serverStatus" color="green" bordered>运行中...</a-tag>
          <a-tag v-else color="gray" bordered>休息中</a-tag>
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
              启动服务
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              :disabled="!serverStatus"
              status="danger"
              @click="changeServerStatusClick('stop')"
            >
              停止服务
            </a-button>
            <a-button
              :loading="loading"
              type="primary"
              :disabled="!serverStatus"
              status="warning"
              @click="changeServerStatusClick('restart')"
            >
              重启服务
            </a-button>
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
    startServer,
    stopServer,
  } from '@/api/dashboard';
  import { Message } from '@arco-design/web-vue';
  import useLoading from '@/hooks/loading';

  const { loading, setLoading } = useLoading(false);
  const serverStatus = ref<boolean>(false);

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

  const changeServerStatusClick = async (
    type: 'start' | 'restart' | 'stop'
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
</script>

<script lang="ts">
  export default {
    name: 'Dashboard',
  };
</script>

<style lang="less" scoped></style>
