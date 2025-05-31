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
              block-node="true"
              :v-model:expanded-keys="expandedKeys"
              :data="treeData"
              :load-more="onSelectDiretory"
              :virtual-list-props="{ buffer: 100 }"
              @select="onSelectFile"
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
  import { TreeInstance, TreeNodeData } from '@arco-design/web-vue/es/tree';
  import { IconDown } from '@arco-design/web-vue/es/icon';
  import dts from './types/beidoums-scripts.d.ts.txt?raw';

  const treeData = ref([]);
  const treeRef = ref<TreeInstance>();
  const editingTreeNode = ref<TreeNodeData>();

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

  function registerCodeCompletion(monaco: MonacoEditor) {
    monaco.languages.typescript.javascriptDefaults.addExtraLib(
      dts,
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

  async function onSelectFile(newSelectedKeys: Array<string>, event: any) {
    const node = event.node as TreeNodeData;
    const selectKey = newSelectedKeys[0];
    if (!node.isLeaf) {
      const expanded = treeRef.value
        ?.getExpandedNodes()
        ?.some((it) => it?.key === selectKey);
      treeRef.value?.expandNode(node?.key ?? '', !expanded);
      onSelectDiretory(event.selectedNodes[0]);
      return;
    }
    editingTreeNode.value = node;
    const result = await readFile({
      currentKey: selectKey,
      title: node.title ?? '',
    });
    editorContent.value = result.data;
    const ext = node.title?.split('.')?.pop()?.toLowerCase();

    if (ext)
      editorLangeguage.value = languageMap[ext as LanguageMapKey] ?? 'txt';
  }

  async function onSelectDiretory(nodeData: TreeNodeData) {
    const result = await treeFile({ currentKey: `${nodeData.key}` });
    nodeData.children = result.data;
  }

  const debounceSaveFile = useDebounceFn(
    async () => {
      await writeFile({
        currentKey: `${editingTreeNode.value?.key}` ?? '',
        title: editingTreeNode.value?.title ?? '',
        content: editorContent.value ?? '',
      });
    },
    1000, // vscode auto save default delay
    { maxWait: 10_000 }
  );

  function onEditorTextChange(value: string | undefined, event: any) {
    debounceSaveFile();
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
    height: 710px !important;
  }
  :deep(.arco-tree-node-title-text) {
    color: #b6b6b6;
  }
  /*  arco-tree-node-title-block */
  /*  arco-tree-node-title */
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
