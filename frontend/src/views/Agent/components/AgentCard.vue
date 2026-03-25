<template>
  <div class="agent-card" @click="emit('click')">
    <div class="card-icon" :style="{ background: bgColor }">
      <el-icon :style="{ color: tool.color, fontSize: '22px' }">
        <component :is="tool.icon" />
      </el-icon>
    </div>
    <div class="card-body">
      <div class="card-name">{{ tool.name }}</div>
      <div class="card-type">CHATFLOW</div>
      <p class="card-desc">{{ tool.description }}</p>
    </div>
    <div class="card-arrow">
      <el-icon><ArrowRight /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import type { AgentTool } from '@/types'

const props = defineProps<{ tool: AgentTool }>()
const emit = defineEmits<{ click: [] }>()

const bgColor = computed(() => {
  const colorMap: Record<string, string> = {
    '#DC2626': '#FEF2F2',
    '#059669': '#ECFDF5',
    '#2563EB': '#EFF6FF',
    '#D97706': '#FFFBEB',
  }
  return colorMap[props.tool.color] || '#F3F4F6'
})
</script>

<style scoped>
.agent-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
}
.agent-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.card-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-body { flex: 1; }
.card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}
.card-type {
  font-size: 10px;
  font-weight: 700;
  color: var(--text-placeholder);
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}
.card-desc {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-arrow {
  position: absolute;
  top: 16px;
  right: 16px;
  color: var(--text-secondary);
  font-size: 14px;
  opacity: 0;
  transition: opacity var(--transition-fast);
}
.agent-card:hover .card-arrow { opacity: 1; }
</style>
