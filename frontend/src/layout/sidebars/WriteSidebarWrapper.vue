<template>
  <aside class="sidebar sidebar--write">
    <div class="sidebar-title">AI 写作工具</div>
    <div class="tool-list">
      <div
        v-for="tool in writeTools"
        :key="tool.key"
        class="tool-item"
        :class="{ 'is-active': activeTool === tool.key }"
        @click="setActiveTool(tool.key)"
      >
        <div class="tool-icon-wrap" :style="{ background: tool.bgColor }">
          <el-icon :style="{ color: tool.color }"><component :is="tool.icon" /></el-icon>
        </div>
        <div class="tool-info">
          <span class="tool-name">{{ tool.name }}</span>
          <span class="tool-desc">{{ tool.desc }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { EditPen, MagicStick, Expand, Tickets, Brush } from '@element-plus/icons-vue'
import { useWriteStore } from '@/stores/writeStore'

const writeStore = useWriteStore()
const activeTool = ref('draft')

const writeTools = [
  { key: 'draft',   name: 'AI 拟文',   desc: '从模板智能生成文稿', icon: Tickets,   color: '#7C3AED', bgColor: '#F5F3FF' },
  { key: 'polish',  name: 'AI 润色',   desc: '优化措辞与表达',     icon: Brush,     color: '#2563EB', bgColor: '#EFF6FF' },
  { key: 'expand',  name: 'AI 扩写',   desc: '扩充内容与细节',     icon: Expand,    color: '#059669', bgColor: '#ECFDF5' },
  { key: 'magic',   name: 'AI 续写',   desc: '延续行文风格',       icon: MagicStick,color: '#D97706', bgColor: '#FFFBEB' },
  { key: 'rewrite', name: 'AI 改写',   desc: '调整语气与风格',     icon: EditPen,   color: '#DC2626', bgColor: '#FEF2F2' },
]

function setActiveTool(key: string) {
  activeTool.value = key
  writeStore.setActiveTool(key)
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
  overflow: hidden;
  padding: 16px 0;
}
.sidebar-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  padding: 0 16px 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.tool-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 8px;
}
.tool-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.tool-item:hover { background: var(--bg-hover); }
.tool-item.is-active { background: var(--bg-hover); }

.tool-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}
.tool-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  overflow: hidden;
}
.tool-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.tool-desc {
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
