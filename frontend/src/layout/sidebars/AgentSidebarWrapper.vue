<template>
  <aside class="sidebar sidebar--agent">
    <div class="sidebar-title">智能工具</div>
    <div class="category-list">
      <div
        v-for="cat in categories"
        :key="cat.key"
        class="category-item"
        :class="{ 'is-active': activeCategory === cat.key }"
        @click="setCategory(cat.key)"
      >
        <el-icon><component :is="cat.icon" /></el-icon>
        <span>{{ cat.label }}</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Grid, Document, DataAnalysis, ChatDotRound } from '@element-plus/icons-vue'
import { useAgentStore } from '@/stores/agentStore'

const agentStore = useAgentStore()
const activeCategory = ref('all')

const categories = [
  { key: 'all',      label: '全部工具', icon: Grid },
  { key: 'document', label: '文档处理', icon: Document },
  { key: 'data',     label: '数据分析', icon: DataAnalysis },
  { key: 'meeting',  label: '会议辅助', icon: ChatDotRound },
]

function setCategory(key: string) {
  activeCategory.value = key
  agentStore.setCategory(key)
}
</script>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--bg-white);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  padding: 16px 0;
  overflow: hidden;
}
.sidebar-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 16px 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.category-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 8px;
}
.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-regular);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.category-item:hover { background: var(--bg-hover); }
.category-item.is-active {
  background: #FEF2F2;
  color: #DC2626;
  font-weight: 500;
}
</style>
