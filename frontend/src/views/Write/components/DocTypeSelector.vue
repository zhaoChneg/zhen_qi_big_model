<template>
  <div class="doc-type-selector">
    <!-- 分类Tab -->
    <div class="category-tabs">
      <div
        class="cat-tab"
        :class="{ 'is-active': category === 'legal' }"
        @click="category = 'legal'"
      >法定公文</div>
      <div
        class="cat-tab"
        :class="{ 'is-active': category === 'business' }"
        @click="category = 'business'"
      >事务性公文</div>
    </div>

    <!-- 公文类型网格 -->
    <div class="type-grid">
      <div
        v-for="type in currentTypes"
        :key="type.value"
        class="type-chip"
        :class="{ 'is-selected': writeStore.docType === type.value }"
        @click="selectType(type.value)"
      >
        {{ type.label }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useWriteStore } from '@/stores/writeStore'

const writeStore = useWriteStore()
const category = ref<'legal' | 'business'>('legal')

const legalTypes = [
  { value: 'report', label: '报告' },
  { value: 'notice', label: '通知' },
  { value: 'motion', label: '议案' },
  { value: 'instruction', label: '通知' },
  { value: 'resolution', label: '决议' },
  { value: 'request', label: '请示' },
  { value: 'announcement', label: '公庭' },
  { value: 'reply', label: '批复' },
  { value: 'minutes', label: '纪要' },
  { value: 'circular', label: '通报' },
  { value: 'order', label: '命令(令)' },
  { value: 'letter', label: '函' },
  { value: 'opinion', label: '意见' },
  { value: 'decision', label: '决定' },
]

const businessTypes = [
  { value: 'work_report', label: '工作报告' },
  { value: 'summary', label: '工作总结' },
  { value: 'plan', label: '工作计划' },
  { value: 'proposal', label: '提案' },
  { value: 'investigation', label: '调研报告' },
  { value: 'speech', label: '讲话稿' },
  { value: 'contract', label: '合同协议' },
  { value: 'briefing', label: '情况简报' },
]

const currentTypes = computed(() =>
  category.value === 'legal' ? legalTypes : businessTypes
)

function selectType(value: string) {
  writeStore.docType = value
}
</script>

<style scoped>
.doc-type-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.category-tabs {
  display: flex;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  padding: 2px;
}
.cat-tab {
  flex: 1;
  text-align: center;
  padding: 5px 0;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  border-radius: 6px;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.cat-tab.is-active {
  background: var(--bg-white);
  color: #7C3AED;
  box-shadow: var(--shadow-sm);
  font-weight: 600;
}
.type-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.type-chip {
  padding: 5px 10px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  font-size: 12px;
  color: var(--text-regular);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--bg-white);
}
.type-chip:hover {
  border-color: #7C3AED;
  color: #7C3AED;
  background: #F5F3FF;
}
.type-chip.is-selected {
  background: #7C3AED;
  color: white;
  border-color: #7C3AED;
  font-weight: 500;
}
</style>
