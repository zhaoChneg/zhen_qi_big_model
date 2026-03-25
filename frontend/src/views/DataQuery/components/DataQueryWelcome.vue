<template>
  <div class="dq-welcome">
    <div class="welcome-icon">
      <el-icon><DataAnalysis /></el-icon>
    </div>
    <h2 class="welcome-title">智能问数</h2>
    <p class="welcome-sub">用自然语言查询数据，无需编写SQL</p>

    <div class="suggest-list">
      <div
        v-for="item in suggests"
        :key="item.text"
        class="suggest-item"
        @click="emit('select', item.text)"
      >
        <div class="suggest-icon" :style="{ background: item.bg }">
          <el-icon :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
        </div>
        <div class="suggest-info">
          <span class="suggest-label">{{ item.label }}</span>
          <span class="suggest-text">{{ item.text }}</span>
        </div>
        <el-icon class="arrow-right"><ArrowRight /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { DataAnalysis, TrendCharts, Calendar, Money, List, ArrowRight } from '@element-plus/icons-vue'

const emit = defineEmits<{ select: [text: string] }>()

const suggests = [
  {
    label: '流量分析',
    text: '查询本月各路段的日均通行量，按流量从高到低排序',
    icon: TrendCharts, color: '#2563EB', bg: '#EFF6FF',
  },
  {
    label: '收费统计',
    text: '统计今年一季度各收费站的收费总额和环比增幅',
    icon: Money, color: '#059669', bg: '#ECFDF5',
  },
  {
    label: '时段分析',
    text: '分析最近7天高峰期（7-9点、17-19点）的车流量分布',
    icon: Calendar, color: '#D97706', bg: '#FFFBEB',
  },
  {
    label: '明细查询',
    text: '查询上周发生故障的收费设备列表及处理状态',
    icon: List, color: '#DC2626', bg: '#FEF2F2',
  },
]
</script>

<style scoped>
.dq-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0 0;
  max-width: 680px;
  margin: 0 auto;
  width: 100%;
}
.welcome-icon {
  width: 60px;
  height: 60px;
  background: #ECFDF5;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: #059669;
}
.welcome-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 16px 0 6px;
}
.welcome-sub {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}
.suggest-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.suggest-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.suggest-item:hover {
  border-color: #059669;
  box-shadow: 0 2px 8px rgba(5, 150, 105, 0.1);
}
.suggest-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}
.suggest-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  overflow: hidden;
}
.suggest-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
}
.suggest-text {
  font-size: 13px;
  color: var(--text-regular);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.arrow-right {
  font-size: 16px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
</style>
