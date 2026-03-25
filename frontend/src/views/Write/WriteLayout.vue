<template>
  <div class="write-layout">
    <!-- 左侧：公文类型选择 + 表单 -->
    <div class="write-form-panel">
      <!-- 深度思考开关 -->
      <div class="deep-think-row">
        <span class="dt-label">深度思考</span>
        <el-switch v-model="writeStore.deepThink" />
      </div>

      <div class="section-label">以下内容将由AI生成</div>

      <!-- 公文类型选择 -->
      <DocTypeSelector />

      <!-- 标题 -->
      <div class="form-section">
        <div class="field-label">
          * 标题
          <span class="field-tip">{{ writeStore.docTitle.length }}/100</span>
        </div>
        <el-input
          v-model="writeStore.docTitle"
          placeholder="请输入标题"
          maxlength="100"
          class="title-input"
        />
      </div>

      <!-- 补充信息 -->
      <div class="form-section">
        <div class="field-label">
          补充信息
          <span class="field-sub">补充更多信息，AI生成效果更好！</span>
        </div>
        <el-input
          v-model="writeStore.supplement"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="请输入补充信息"
          class="supplement-input"
        />
      </div>

      <!-- 报告事项 -->
      <div class="form-section">
        <div class="field-label">报告事项</div>
        <el-input
          v-model="writeStore.matters"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 4 }"
          placeholder="请输入报告事项"
        />
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button
          :loading="writeStore.summaryLoading"
          class="summary-btn"
          @click="writeStore.generateSummary()"
        >
          生成摘要
        </el-button>
        <el-button
          type="primary"
          :loading="writeStore.generating"
          :disabled="!writeStore.summary"
          class="write-btn"
          @click="writeStore.generateDoc()"
        >
          根据摘要写作
        </el-button>
      </div>

      <!-- 摘要展示 -->
      <div v-if="writeStore.summary" class="summary-preview">
        <div class="summary-label">摘要预览</div>
        <p class="summary-text">{{ writeStore.summary }}</p>
      </div>
    </div>

    <!-- 右侧：富文本编辑器 -->
    <div class="write-editor-panel">
      <RichTextEditor />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import DocTypeSelector from './components/DocTypeSelector.vue'
import RichTextEditor from './components/RichTextEditor.vue'
import { useWriteStore } from '@/stores/writeStore'

const writeStore = useWriteStore()

onMounted(() => {
  writeStore.loadDocs()
})
</script>

<style scoped>
.write-layout {
  display: flex;
  height: 100%;
  overflow: hidden;
  background: #F8FAFC;
}

/* 左侧表单 */
.write-form-panel {
  width: 300px;
  flex-shrink: 0;
  background: var(--bg-white);
  border-right: 1px solid var(--border-color);
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.deep-think-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid var(--border-light);
}
.dt-label { font-size: 13px; font-weight: 500; color: var(--text-primary); }

.section-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-section { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.field-tip { font-size: 11px; color: var(--text-placeholder); font-weight: normal; }
.field-sub { font-size: 11px; color: var(--text-secondary); font-weight: normal; }

.form-actions {
  display: flex;
  gap: 8px;
}
.summary-btn { flex: 1; }
.write-btn { flex: 1; background: #7C3AED; border-color: #7C3AED; }
.write-btn:hover { background: #6D28D9; border-color: #6D28D9; }

.summary-preview {
  background: #F5F3FF;
  border: 1px solid #DDD6FE;
  border-radius: var(--radius-md);
  padding: 12px;
}
.summary-label {
  font-size: 11px;
  font-weight: 600;
  color: #7C3AED;
  margin-bottom: 6px;
}
.summary-text {
  font-size: 12px;
  color: var(--text-regular);
  line-height: 1.6;
}

/* 右侧编辑器 */
.write-editor-panel {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
