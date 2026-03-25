<template>
  <div class="sql-preview card">
    <div class="preview-header">
      <div class="header-left">
        <el-icon class="sql-icon"><Tickets /></el-icon>
        <span class="preview-title">生成的 SQL</span>
        <el-tag v-if="!loading" size="small" type="success">已生成</el-tag>
        <el-tag v-else size="small">生成中...</el-tag>
      </div>
      <div class="header-actions">
        <el-tooltip content="复制SQL" placement="top">
          <el-button :icon="CopyDocument" text size="small" @click="copySql" :disabled="loading" />
        </el-tooltip>
        <el-button
          type="primary"
          size="small"
          :loading="executing"
          :disabled="loading || !editableSql.trim()"
          @click="handleExecute"
          style="background: #059669; border-color: #059669;"
        >
          执行查询
        </el-button>
      </div>
    </div>

    <!-- SQL编辑器 -->
    <div class="sql-editor-wrap">
      <el-skeleton v-if="loading" :rows="3" animated />
      <textarea
        v-else
        v-model="editableSql"
        class="sql-editor"
        spellcheck="false"
        placeholder="SQL将在这里显示..."
      />
    </div>

    <!-- AI解释 -->
    <div v-if="explanation && !loading" class="sql-explanation">
      <el-icon><InfoFilled /></el-icon>
      <span>{{ explanation }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Tickets, CopyDocument, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useDataQueryStore } from '@/stores/dataQueryStore'

const props = defineProps<{
  sql: string
  explanation: string
  loading: boolean
}>()
const emit = defineEmits<{ execute: [sql: string] }>()
const store = useDataQueryStore()
const executing = computed(() => store.queryLoading)

const editableSql = ref(props.sql)
watch(() => props.sql, (val) => { editableSql.value = val })

async function copySql() {
  await navigator.clipboard.writeText(editableSql.value)
  ElMessage.success('SQL已复制')
}

function handleExecute() {
  emit('execute', editableSql.value)
}
</script>

<style scoped>
.sql-preview {
  padding: 16px;
  margin-bottom: 16px;
  max-width: 900px;
}
.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.sql-icon { font-size: 16px; color: #059669; }
.preview-title { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.header-actions { display: flex; align-items: center; gap: 8px; }

.sql-editor-wrap {
  background: #1E1E2E;
  border-radius: var(--radius-md);
  overflow: hidden;
}
.sql-editor {
  width: 100%;
  min-height: 80px;
  max-height: 200px;
  background: #1E1E2E;
  color: #CDD6F4;
  border: none;
  outline: none;
  padding: 14px 16px;
  font-family: 'Courier New', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: vertical;
}

.sql-explanation {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 10px;
  padding: 10px 12px;
  background: #ECFDF5;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: #065F46;
}
.sql-explanation .el-icon { flex-shrink: 0; margin-top: 1px; }
</style>
