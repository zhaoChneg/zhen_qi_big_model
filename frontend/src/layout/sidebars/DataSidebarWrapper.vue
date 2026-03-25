<template>
  <aside class="sidebar sidebar--data">
    <div class="sidebar-header">
      <div class="sidebar-title">数据查询</div>
    </div>

    <!-- 数据源选择 -->
    <div class="section">
      <div class="section-label">数据源</div>
      <el-select
        v-model="selectedSourceId"
        placeholder="选择数据源"
        size="small"
        style="width: 100%"
        @change="handleSourceChange"
      >
        <el-option
          v-for="source in dataSources"
          :key="source.id"
          :label="source.name"
          :value="source.id"
        >
          <div class="source-option">
            <el-icon><Coin /></el-icon>
            <span>{{ source.name }}</span>
          </div>
        </el-option>
      </el-select>
    </div>

    <!-- 查询历史 -->
    <div class="section-label" style="margin-top: 16px">历史查询</div>
    <div class="history-list">
      <div
        v-for="item in queryHistory"
        :key="item.id"
        class="history-item"
        :class="{ 'is-active': currentQueryId === item.id }"
        @click="selectHistory(item)"
      >
        <el-icon class="history-icon" :class="getStatusClass(item.status)">
          <component :is="getStatusIcon(item.status)" />
        </el-icon>
        <span class="history-question ellipsis">{{ item.question }}</span>
        <el-icon class="delete-icon" @click.stop="deleteHistory(item.id)"><Delete /></el-icon>
      </div>
      <div v-if="queryHistory.length === 0" class="empty-history">
        暂无查询记录
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { Coin, Delete, CircleCheck, CircleClose, Loading, Clock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useDataQueryStore } from '@/stores/dataQueryStore'
import { storeToRefs } from 'pinia'
import type { DataQueryRecord } from '@/types'

const store = useDataQueryStore()
const { dataSources, queryHistory, selectedSourceId, currentQueryId } = storeToRefs(store)

function handleSourceChange(id: string) {
  store.selectSource(id)
}

function selectHistory(item: DataQueryRecord) {
  store.loadHistoryRecord(item)
}

async function deleteHistory(id: string) {
  await store.deleteHistory(id)
  ElMessage.success('已删除')
}

function getStatusIcon(status: string) {
  const map: Record<string, any> = {
    success: CircleCheck,
    error: CircleClose,
    running: Loading,
    pending: Clock,
  }
  return map[status] || Clock
}

function getStatusClass(status: string) {
  return {
    'status-success': status === 'success',
    'status-error': status === 'error',
    'status-running': status === 'running',
  }
}

onMounted(() => {
  store.loadDataSources()
  store.loadHistory()
})
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--bg-white);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px 0;
}
.sidebar-header { padding: 0 16px 12px; }
.sidebar-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
.section { padding: 0 12px; }
.section-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 16px 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.source-option {
  display: flex;
  align-items: center;
  gap: 6px;
}
.history-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
  position: relative;
}
.history-item:hover { background: var(--bg-hover); }
.history-item.is-active { background: #ECFDF5; }
.history-icon { font-size: 14px; flex-shrink: 0; color: var(--text-secondary); }
.status-success { color: var(--success-color); }
.status-error   { color: var(--danger-color); }
.status-running { color: var(--primary-color); animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.history-question {
  flex: 1;
  font-size: 12px;
  color: var(--text-regular);
}
.delete-icon {
  opacity: 0;
  font-size: 13px;
  color: var(--text-secondary);
  transition: opacity var(--transition-fast);
}
.history-item:hover .delete-icon { opacity: 1; }
.empty-history {
  text-align: center;
  padding: 32px 0;
  font-size: 12px;
  color: var(--text-placeholder);
}
</style>
