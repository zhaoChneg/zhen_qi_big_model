<template>
  <div class="query-input-wrap">
    <div class="query-box">
      <el-icon class="search-icon"><Search /></el-icon>
      <input
        v-model="inputText"
        class="query-input"
        placeholder="用自然语言描述您想查询的数据，例如：本月各路段的通行量对比"
        @keydown.enter.exact.prevent="handleSubmit"
        :disabled="loading"
      />
      <div class="query-actions">
        <el-tooltip v-if="inputText" content="清空" placement="top">
          <el-icon class="clear-btn" @click="inputText = ''"><CircleClose /></el-icon>
        </el-tooltip>
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!inputText.trim()"
          class="submit-btn"
          @click="handleSubmit"
        >
          <el-icon v-if="!loading"><Promotion /></el-icon>
          查询
        </el-button>
      </div>
    </div>
    <p class="query-tip">支持自然语言查询 · AI自动生成SQL · 结果可视化展示</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search, CircleClose, Promotion } from '@element-plus/icons-vue'
import { useDataQueryStore } from '@/stores/dataQueryStore'

const emit = defineEmits<{ submit: [question: string] }>()
const store = useDataQueryStore()
const loading = computed(() => store.nl2sqlLoading || store.queryLoading)

const inputText = ref('')

function handleSubmit() {
  const text = inputText.value.trim()
  if (!text || loading.value) return
  emit('submit', text)
}
</script>

<style scoped>
.query-input-wrap {
  flex-shrink: 0;
  padding: 20px 24px 0;
  background: #F8FAFC;
}
.query-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--bg-white);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 10px 12px;
  transition: border-color var(--transition-fast);
  box-shadow: var(--shadow-sm);
}
.query-box:focus-within {
  border-color: #059669;
  box-shadow: 0 0 0 3px rgba(5, 150, 105, 0.1);
}
.search-icon {
  font-size: 18px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.query-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: var(--text-primary);
  background: transparent;
  font-family: var(--font-family);
}
.query-input::placeholder { color: var(--text-placeholder); }
.query-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}
.clear-btn {
  font-size: 18px;
  color: var(--text-secondary);
  cursor: pointer;
}
.clear-btn:hover { color: var(--text-primary); }
.submit-btn {
  background: #059669;
  border-color: #059669;
  border-radius: var(--radius-md);
  padding: 6px 16px;
}
.submit-btn:hover { background: #047857; border-color: #047857; }

.query-tip {
  text-align: center;
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 8px;
}
</style>
