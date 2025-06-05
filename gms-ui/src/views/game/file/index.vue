<template>
  <div class="container">
    <Breadcrumb />
    <a-card class="general-card" :title="$t('menu.game.file')">
      <a-layout>
        <a-layout>
          <a-layout-sider>
            <a-tree
              ref="treeRef"
              theme="dark"
              size="mini"
              :block-node="treeBlockMode"
              :data="treeData"
              :load-more="onTreeSelectDiretory"
              :virtual-list-props="{ buffer: 100 }"
              @select="onTreeSelectFile"
            >
              <template #switcher-icon>
                <IconDown />
              </template>
            </a-tree>
          </a-layout-sider>
          <a-layout-content>
            <vue-monaco-editor
              v-model:value="editorContent"
              :language="editorLangeguage"
              default-language="javascript"
              theme="vs-dark"
              :options="editorOptions"
              @mount="onEditorMount"
              @change="onEditorTextChange"
          /></a-layout-content>
        </a-layout>
      </a-layout>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref, shallowRef, onUnmounted } from 'vue';
  import {
    Editor,
    MonacoEditor,
    VueMonacoEditor,
  } from '@guolao/vue-monaco-editor';
  import { treeFile, readFile, writeFile } from '@/api/fileTree';
  import { useDebounceFn } from '@vueuse/core';
  import type { TreeNodeData } from '@arco-design/web-vue/es/tree/interface';
  import { Tree } from '@arco-design/web-vue';
  import { IconDown } from '@arco-design/web-vue/es/icon';
  import localDts from './types/beidoums-scripts.d.ts.txt?raw';

  const treeData = ref([]);
  const treeRef = ref<typeof Tree>();
  const treeBlockMode = ref(true);
  const treeEditingNode = ref<TreeNodeData>();

  const editor = shallowRef<typeof Editor>();
  const editorContent = ref('');
  const editorCompletionProvider = shallowRef();
  const editorLangeguage = ref('');
  const editorOptions = {
    automaticLayout: true,
    formatOnType: true,
    formatOnPaste: true,
  };

  const languageMap = {
    js: 'javascript',
    html: 'html',
    xml: 'xml',
    json: 'json',
    java: 'java',
    md: 'markdown',
    sh: 'shell',
    bat: 'bat',
    yml: 'yaml',
    yaml: 'yaml',
    properties: 'properties',
    sql: 'sql',
  };
  type LanguageMapKey = keyof typeof languageMap;

  onUnmounted(() => {
    if (editorCompletionProvider.value)
      editorCompletionProvider.value.dispose();
    if (editor.value) editor.value.dispose();
  });

  function onEditorMount(editorInstance: any, monacoInstance: MonacoEditor) {
    editor.value = editorInstance;
    registerCodeCompletion(monacoInstance);
  }

  /**
   * 添加代码提示, 优先访问网络文件（浏览器缓存）
   */
  async function registerCodeCompletion(monaco: MonacoEditor) {
    let usingDts = '';
    try {
      const response = await fetch(
        `https://cdn.jsdelivr.net/gh/shinobi9/beidoums-scripts-snippets/types/beidoums-scripts.d.ts`
      );
      usingDts = response.ok ? await response.text() : usingDts;
    } catch (e) {
      // 请求不到
      usingDts = localDts;
    }

    monaco.languages.typescript.javascriptDefaults.addExtraLib(
      usingDts,
      'beidoums-scripts-dts'
    );
    monaco.languages.typescript.javascriptDefaults.setCompilerOptions({
      allowJs: true,
      target: monaco.languages.typescript.ScriptTarget.ES6,
      allowNonTsExtensions: true,
      noNonAsciiIdentifier: false,
      noLib: true,
    });
  }

  async function onTreeSelectFile(
    newSelectedKeys: (string | number)[],
    event: any
  ) {
    const node = event.node as TreeNodeData;
    const selectKey = String(newSelectedKeys[0]);
    // 当选择文件夹时，默认什么也不做（展开在点击图标上），这里使其展开
    if (!node.isLeaf) {
      const expanded = treeRef.value
        ?.getExpandedNodes()
        ?.some((it: TreeNodeData) => String(it.key) === selectKey);
      treeRef.value?.expandNode(String(node.key), !expanded);
      onTreeSelectDiretory(event.selectedNodes[0]);
      return;
    }
    // 如果是 文件，则保存状态（正在修改什么文件）
    treeEditingNode.value = node;
    const result = await readFile({
      currentKey: selectKey,
      title: node.title ?? '',
    });
    editorContent.value = result.data;
    // 根据文件扩展名，设置文件高亮
    const ext = node.title?.split('.')?.pop()?.toLowerCase();
    if (ext)
      editorLangeguage.value = languageMap[ext as LanguageMapKey] ?? 'txt';
  }

  async function onTreeSelectDiretory(nodeData: TreeNodeData) {
    const result = await treeFile({ currentKey: String(nodeData.key) });
    nodeData.children = result.data;
  }

  /**
   * 修改文件时自动保存，防抖
   */
  const debounceSaveFile = useDebounceFn(
    async () => {
      if (!treeEditingNode.value) return;
      await writeFile({
        currentKey: String(treeEditingNode.value.key),
        title: treeEditingNode.value.title ?? '',
        content: editorContent.value ?? '',
      });
    },
    1000, // vscode auto save default delay
    { maxWait: 10_000 }
  );

  /**
   *  如果是初始化状态 或者 意外状况没有正在修改的文件，则不保存文件
   */
  function onEditorTextChange(value: string | undefined, event: any) {
    if (treeEditingNode.value) debounceSaveFile();
  }

  async function treeRoot() {
    const result = await treeFile({ currentKey: '' });
    treeData.value = result.data;
  }

  treeRoot();
</script>

<script lang="ts">
  export default {
    name: 'ScriptFileManage',
    components: { VueMonacoEditor, IconDown },
  };
</script>

<style scoped>
  :deep(.arco-layout-sider) {
    background-color: #2c2c2c !important;
    width: 360px;
  }
  :deep(.arco-virtual-list) {
    height: calc(100vh - 270px) !important;
  }
  :deep(.arco-tree-node-title-text) {
    color: #b6b6b6;
  }
  :deep(.arco-tree-node-title):hover {
    background-color: #181818;
  }

  :deep(.arco-virtual-list) {
    color: #d4d4d4;
    scrollbar-width: thin;
    scrollbar-color: #181818 #383838;
  }
  :deep(.arco-tree-node-plus-icon),
  :deep(.arco-tree-node-minus-icon) {
    background-color: #000;
  }
</style>
