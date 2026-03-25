<template>
  <div class="input-area">
    <!-- 附件预览 -->
    <div v-if="attachments.length" class="attachments-bar">
      <div v-for="att in attachments" :key="att.id" class="att-chip">
        <el-icon><Document /></el-icon>
        <span class="att-name">{{ att.name }}</span>
        <el-icon class="att-remove" @click="removeAttachment(att.id)"><Close /></el-icon>
      </div>
    </div>

    <div class="input-box">
      <!-- 上传按钮 -->
      <el-tooltip content="上传文档（PDF/Word）" placement="top">
        <label class="upload-btn" for="chat-file-input">
          <el-icon><Paperclip /></el-icon>
        </label>
      </el-tooltip>
      <input
        id="chat-file-input"
        type="file"
        accept=".pdf,.doc,.docx,.txt"
        style="display:none"
        @change="handleFileUpload"
      />

      <!-- 文本输入 -->
      <el-input
        v-model="inputText"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        :placeholder="isStreaming ? 'AI 正在回复中...' : '输入问题，按 Enter 发送，Shift+Enter 换行'"
        :disabled="isStreaming"
        class="msg-textarea"
        resize="none"
        @keydown.enter.exact.prevent="handleSend"
      />

      <!-- 发送/停止按钮 -->
      <el-tooltip :content="isStreaming ? '停止生成' : '发送'" placement="top">
        <el-button
          :type="isStreaming ? 'danger' : 'primary'"
          :icon="isStreaming ? VideoPause : Promotion"
          circle
          class="send-btn"
          :disabled="!isStreaming && !inputText.trim()"
          @click="isStreaming ? handleStop() : handleSend()"
        />
      </el-tooltip>
    </div>

    <p class="input-tip">内容由AI生成，仅供参考</p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Paperclip, Document, Close, Promotion, VideoPause } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chatStore'
import { chatApi } from '@/api/chat'
import type { FileAttachment } from '@/types'

const emit = defineEmits<{
  send: [payload: { content: string; attachments?: FileAttachment[] }]
}>()

const chatStore = useChatStore()
const inputText = ref('')
const attachments = ref<FileAttachment[]>([])
const uploading = ref(false)
const isStreaming = computed(() => chatStore.isStreaming)

async function handleFileUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const maxSize = 20 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.warning('文件大小不能超过20MB')
    return
  }
  uploading.value = true
  try {
    const att = await chatApi.uploadFile(file)
    attachments.value.push(att)
  } catch {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
    ;(e.target as HTMLInputElement).value = ''
  }
}

function removeAttachment(id: string) {
  attachments.value = attachments.value.filter(a => a.id !== id)
}

function handleSend() {
  const text = inputText.value.trim()
  if (!text || isStreaming.value) return
  emit('send', { content: text, attachments: attachments.value })
  inputText.value = ''
  attachments.value = []
}

function handleStop() {
  chatStore.stopStreaming()
}
</script>

<style scoped>
.input-area {
  flex-shrink: 0;
  padding: 12px 20px 16px;
  background: #F8FAFC;
  border-top: 1px solid var(--border-color);
}

.attachments-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-width: 840px;
  margin: 0 auto 8px;
}
.att-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  color: var(--text-regular);
}
.att-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.att-remove {
  cursor: pointer;
  color: var(--text-secondary);
  font-size: 12px;
}
.att-remove:hover { color: var(--danger-color); }

.input-box {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 840px;
  margin: 0 auto;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 8px 10px 8px 12px;
  transition: border-color var(--transition-fast);
}
.input-box:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px var(--primary-light);
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.upload-btn:hover {
  background: var(--bg-hover);
  color: var(--primary-color);
}

.msg-textarea {
  flex: 1;
}
:deep(.msg-textarea .el-textarea__inner) {
  border: none !important;
  box-shadow: none !important;
  padding: 4px 0;
  font-size: 14px;
  line-height: 1.6;
  background: transparent;
  resize: none;
}

.send-btn {
  flex-shrink: 0;
}

.input-tip {
  text-align: center;
  font-size: 11px;
  color: var(--text-placeholder);
  margin-top: 8px;
}
</style>
