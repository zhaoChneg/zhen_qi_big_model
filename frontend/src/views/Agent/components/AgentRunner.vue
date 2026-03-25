<template>
  <div class="agent-runner">
    <!-- 头部 -->
    <div class="runner-header">
      <el-button :icon="ArrowLeft" text @click="emit('close')">返回</el-button>
      <div class="tool-info">
        <div class="tool-icon-sm" :style="{ background: bgColor }">
          <el-icon :style="{ color: tool.color }"><component :is="tool.icon" /></el-icon>
        </div>
        <span class="tool-name">{{ tool.name }}</span>
        <el-tag size="small" type="info">CHATFLOW</el-tag>
      </div>
      <div style="width: 60px" />
    </div>

    <!-- 内容区 -->
    <div class="runner-body">
      <!-- 输入面板 -->
      <div class="input-panel card">
        <h3 class="panel-title">输入参数</h3>
        <el-form :model="formData" label-position="top" size="default">
          <el-form-item
            v-for="input in tool.inputs"
            :key="input.key"
            :label="input.label"
            :required="input.required"
          >
            <el-input
              v-if="input.type === 'text'"
              v-model="formData[input.key]"
              :placeholder="input.placeholder"
            />
            <el-input
              v-else-if="input.type === 'textarea'"
              v-model="formData[input.key]"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 8 }"
              :placeholder="input.placeholder"
            />
            <el-select
              v-else-if="input.type === 'select'"
              v-model="formData[input.key]"
              :placeholder="input.placeholder || '请选择'"
              style="width: 100%"
            >
              <el-option
                v-for="opt in input.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <div v-else-if="input.type === 'file'" class="file-upload-area">
              <el-upload
                :auto-upload="false"
                :show-file-list="true"
                :on-change="(file: any) => handleFileChange(input.key, file)"
                drag
              >
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <div class="upload-text">拖拽文件至此或 <em>点击上传</em></div>
                <div class="upload-hint">支持 PDF、Word、TXT 格式</div>
              </el-upload>
            </div>
          </el-form-item>
        </el-form>

        <el-button
          type="primary"
          :loading="agentStore.isRunning"
          :style="{ background: tool.color, borderColor: tool.color, width: '100%' }"
          @click="handleRun"
        >
          {{ agentStore.isRunning ? '执行中...' : '开始执行' }}
        </el-button>
      </div>

      <!-- 结果面板 -->
      <div class="result-panel">
        <div v-if="!hasMessages" class="result-empty">
          <el-icon class="empty-icon"><ChatDotRound /></el-icon>
          <p>填写参数后点击执行，AI将在此展示结果</p>
        </div>
        <div v-else class="messages-wrap">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="msg.role"
          >
            <div v-if="msg.role === 'assistant'" class="ai-bubble">
              <div v-if="msg.isStreaming && !msg.content" class="thinking">
                <div class="thinking-dots"><span /><span /><span /></div>
                <span>AI执行中...</span>
              </div>
              <div
                v-else
                class="md-content"
                :class="{ 'stream-cursor': msg.isStreaming }"
                v-html="renderMarkdown(msg.content)"
              />
              <!-- 结果操作 -->
              <div v-if="!msg.isStreaming && msg.content" class="result-actions">
                <el-button :icon="CopyDocument" text size="small" @click="copyResult(msg.content)">
                  复制
                </el-button>
                <el-button :icon="Download" text size="small" @click="exportResult(msg.content)">
                  导出Word
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ArrowLeft, ChatDotRound, UploadFilled, CopyDocument, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import { useAgentStore } from '@/stores/agentStore'
import { exportWord } from '@/utils/export'
import type { AgentTool } from '@/types'

const props = defineProps<{ tool: AgentTool }>()
const emit = defineEmits<{ close: [] }>()
const agentStore = useAgentStore()

const formData = ref<Record<string, any>>({})
const messages = computed(() => agentStore.currentRun?.messages || [])
const hasMessages = computed(() => messages.value.length > 0)

const bgColor = computed(() => {
  const map: Record<string, string> = {
    '#DC2626': '#FEF2F2', '#059669': '#ECFDF5',
    '#2563EB': '#EFF6FF', '#D97706': '#FFFBEB',
  }
  return map[props.tool.color] || '#F3F4F6'
})

const md = new MarkdownIt({ html: false, breaks: true })
function renderMarkdown(content: string) { return md.render(content || '') }

function handleFileChange(key: string, file: any) {
  formData.value[key] = file.raw
}

async function handleRun() {
  const required = props.tool.inputs.filter(i => i.required)
  for (const inp of required) {
    if (!formData.value[inp.key]) {
      ElMessage.warning(`请填写 ${inp.label}`)
      return
    }
  }
  await agentStore.runTool(formData.value)
}

async function copyResult(content: string) {
  await navigator.clipboard.writeText(content)
  ElMessage.success('已复制')
}

function exportResult(content: string) {
  exportWord(md.render(content), `${props.tool.name}_${Date.now()}`)
}
</script>

<style scoped>
.agent-runner {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #F8FAFC;
}
.runner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: var(--bg-white);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}
.tool-info { display: flex; align-items: center; gap: 8px; }
.tool-icon-sm {
  width: 28px; height: 28px;
  border-radius: var(--radius-sm);
  display: flex; align-items: center; justify-content: center;
}
.tool-name { font-size: 15px; font-weight: 600; }

.runner-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 20px;
  overflow: hidden;
}

.input-panel {
  width: 320px;
  flex-shrink: 0;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; }

.file-upload-area :deep(.el-upload-dragger) {
  padding: 20px;
}
.upload-icon { font-size: 28px; color: var(--text-secondary); }
.upload-text { font-size: 13px; color: var(--text-secondary); }
.upload-text em { color: var(--primary-color); font-style: normal; }
.upload-hint { font-size: 11px; color: var(--text-placeholder); }

.result-panel {
  flex: 1;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.result-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-placeholder);
  font-size: 13px;
}
.empty-icon { font-size: 40px; opacity: 0.4; }

.messages-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
.ai-bubble {
  background: #F9FAFB;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
}
.thinking {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}
.result-actions {
  display: flex;
  gap: 4px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--border-light);
}
</style>
