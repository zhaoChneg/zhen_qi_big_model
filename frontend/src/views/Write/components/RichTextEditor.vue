<template>
  <div class="editor-wrap">
    <!-- 编辑器工具栏 -->
    <div class="editor-header">
      <div class="doc-title-row">
        <el-input
          v-model="writeStore.docTitle"
          placeholder="新建文档"
          class="doc-title-input"
          variant="borderless"
        />
        <el-tag v-if="writeStore.generating" type="warning" size="small" effect="light">
          生成中...
        </el-tag>
      </div>
      <div class="editor-actions">
        <el-button size="small" @click="handleExport">下载</el-button>
        <el-button type="primary" size="small" @click="writeStore.saveDoc()">保存</el-button>
      </div>
    </div>

    <!-- WangEditor 富文本 -->
    <div class="editor-container">
      <Toolbar
        :editor="editorRef"
        :defaultConfig="toolbarConfig"
        class="editor-toolbar"
      />
      <Editor
        v-model="editorContent"
        :defaultConfig="editorConfig"
        class="editor-body"
        @onCreated="handleCreated"
      />
    </div>

    <!-- 字数统计 -->
    <div class="editor-footer">
      <span class="word-count">字数：{{ wordCount }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, shallowRef, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'
import { exportWord } from '@/utils/export'
import { useWriteStore } from '@/stores/writeStore'

const writeStore = useWriteStore()
const editorRef = shallowRef<IDomEditor>()

const editorContent = computed({
  get: () => writeStore.editorContent,
  set: (val) => { writeStore.editorContent = val },
})

const wordCount = computed(() => {
  const text = editorContent.value.replace(/<[^>]*>/g, '')
  return text.length
})

const toolbarConfig: Partial<IToolbarConfig> = {
  excludeKeys: ['uploadImage', 'uploadVideo', 'insertVideo'],
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '公文内容将在此生成，您也可以直接编辑...',
  autoFocus: false,
}

// 生成中自动滚动到底部
watch(() => writeStore.editorContent, async () => {
  if (writeStore.generating) {
    const editor = editorRef.value
    if (editor) {
      const editorEl = editor.getEditableContainer()
      editorEl.scrollTop = editorEl.scrollHeight
    }
  }
})

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
}

function handleExport() {
  exportWord(editorContent.value, writeStore.docTitle || 'document')
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped>
.editor-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-white);
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}
.doc-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}
.doc-title-input {
  font-size: 15px;
  font-weight: 500;
}
:deep(.doc-title-input .el-input__inner) {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}
.editor-actions { display: flex; gap: 8px; flex-shrink: 0; }

.editor-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-toolbar {
  flex-shrink: 0;
  border-bottom: 1px solid var(--border-color) !important;
}

.editor-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 40px;
}

:deep(.w-e-text-container) {
  font-family: var(--font-family);
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-primary);
}

.editor-footer {
  flex-shrink: 0;
  padding: 6px 16px;
  border-top: 1px solid var(--border-light);
  background: var(--bg-page);
}
.word-count { font-size: 11px; color: var(--text-placeholder); }
</style>
