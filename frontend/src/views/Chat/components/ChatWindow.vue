<template>
  <div class="chat-window" ref="containerRef">
    <div class="messages-wrap">
      <TransitionGroup name="message-fade">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-row"
          :class="msg.role"
        >
          <!-- 用户消息 -->
          <template v-if="msg.role === 'user'">
            <div class="message-bubble user-bubble">
              <p class="msg-text">{{ msg.content }}</p>
              <!-- 附件 -->
              <div v-if="msg.attachments?.length" class="attachments">
                <div v-for="att in msg.attachments" :key="att.id" class="att-chip">
                  <el-icon><Document /></el-icon>
                  <span>{{ att.name }}</span>
                </div>
              </div>
            </div>
            <el-avatar :size="32" class="user-avatar">
              {{ userInitial }}
            </el-avatar>
          </template>

          <!-- AI消息 -->
          <template v-else-if="msg.role === 'assistant'">
            <div class="ai-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="message-bubble ai-bubble">
              <!-- 思考中 -->
              <div v-if="msg.isStreaming && !msg.content" class="thinking">
                <div class="thinking-dots">
                  <span /><span /><span />
                </div>
                <span class="thinking-text">正在思考中...</span>
              </div>
              <!-- Markdown内容 -->
              <div
                v-else
                class="md-content"
                :class="{ 'stream-cursor': msg.isStreaming }"
                v-html="renderMarkdown(msg.content)"
              />
              <!-- 消息操作 -->
              <div v-if="!msg.isStreaming && msg.content" class="msg-actions">
                <el-tooltip content="复制" placement="top">
                  <el-button :icon="CopyDocument" text size="small" @click="copyContent(msg.content)" />
                </el-tooltip>
              </div>
            </div>
          </template>
        </div>
      </TransitionGroup>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { ChatDotRound, Document, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import { useChatStore } from '@/stores/chatStore'
import { useUserStore } from '@/stores/userStore'

const chatStore = useChatStore()
const userStore = useUserStore()
const containerRef = ref<HTMLElement>()
const messages = computed(() => chatStore.currentMessages)
const userInitial = computed(() => userStore.userInfo?.displayName?.charAt(0) || 'U')

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true,
  highlight(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return `<pre><code class="hljs">${hljs.highlight(code, { language: lang }).value}</code></pre>`
    }
    return `<pre><code class="hljs">${md.utils.escapeHtml(code)}</code></pre>`
  },
})

function renderMarkdown(content: string) {
  return md.render(content || '')
}

async function copyContent(content: string) {
  await navigator.clipboard.writeText(content)
  ElMessage.success('已复制')
}

// 自动滚动到底部
watch(
  () => messages.value[messages.value.length - 1]?.content,
  async () => {
    await nextTick()
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight
    }
  }
)
</script>

<style scoped>
.chat-window {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.messages-wrap {
  max-width: 840px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.message-row.user {
  flex-direction: row-reverse;
}

/* 头像 */
.user-avatar {
  flex-shrink: 0;
  background: var(--primary-color);
  color: white;
  font-size: 13px;
  font-weight: 600;
}
.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: var(--primary-color);
  flex-shrink: 0;
}

/* 气泡 */
.message-bubble {
  max-width: 72%;
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  position: relative;
}

.user-bubble {
  background: var(--primary-color);
  color: white;
  border-bottom-right-radius: 4px;
}
.user-bubble .msg-text {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-bubble {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-bottom-left-radius: 4px;
  box-shadow: var(--shadow-sm);
}

/* 附件 */
.attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
.att-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  background: rgba(255,255,255,0.2);
  padding: 3px 8px;
  border-radius: var(--radius-sm);
}

/* 思考中 */
.thinking {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}
.thinking-text {
  font-size: 13px;
  color: var(--text-secondary);
}

/* 消息操作 */
.msg-actions {
  display: flex;
  gap: 2px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-light);
}

/* 动画 */
.message-fade-enter-active { transition: all 0.3s ease; }
.message-fade-enter-from { opacity: 0; transform: translateY(12px); }
</style>
