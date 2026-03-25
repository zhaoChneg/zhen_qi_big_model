import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatSession, ChatMessage, FileAttachment } from '@/types'
import { chatApi, CHAT_STREAM_URL } from '@/api/chat'
import { getSSEClient, abortSSE } from '@/utils/sse'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const currentSessionId = ref<string>('')
  const messagesMap = ref<Record<string, ChatMessage[]>>({})
  const streamingMap = ref<Record<string, boolean>>({})

  const currentMessages = computed(() =>
    messagesMap.value[currentSessionId.value] || []
  )
  const isStreaming = computed(() =>
    streamingMap.value[currentSessionId.value] || false
  )

  async function loadSessions() {
    try {
      sessions.value = await chatApi.getSessions()
      if (sessions.value.length > 0 && !currentSessionId.value) {
        await selectSession(sessions.value[0].id)
      }
    } catch {}
  }

  async function createSession() {
    const session = await chatApi.createSession()
    sessions.value.unshift(session)
    await selectSession(session.id)
  }

  async function selectSession(id: string) {
    abortSSE('chat')
    currentSessionId.value = id
    if (!messagesMap.value[id]) {
      try {
        messagesMap.value[id] = await chatApi.getMessages(id)
      } catch {
        messagesMap.value[id] = []
      }
    }
  }

  async function deleteSession(id: string) {
    await chatApi.deleteSession(id)
    sessions.value = sessions.value.filter(s => s.id !== id)
    delete messagesMap.value[id]
    if (currentSessionId.value === id) {
      currentSessionId.value = sessions.value[0]?.id || ''
    }
  }

  async function renameSession(id: string, title: string) {
    await chatApi.updateSession(id, title)
    const session = sessions.value.find(s => s.id === id)
    if (session) session.title = title
  }

  async function sendMessage(content: string, attachments?: FileAttachment[]) {
    if (!currentSessionId.value) {
      await createSession()
    }
    const sessionId = currentSessionId.value
    if (!messagesMap.value[sessionId]) {
      messagesMap.value[sessionId] = []
    }

    // 添加用户消息
    const userMsg: ChatMessage = {
      id: `user_${Date.now()}`,
      sessionId,
      role: 'user',
      content,
      createdAt: dayjs().toISOString(),
      attachments,
    }
    messagesMap.value[sessionId].push(userMsg)

    // 更新会话标题（首次发送时）
    const session = sessions.value.find(s => s.id === sessionId)
    if (session && session.title === '新对话') {
      session.title = content.slice(0, 20) + (content.length > 20 ? '...' : '')
    }

    // 添加 AI 消息占位
    const aiMsg: ChatMessage = {
      id: `ai_${Date.now()}`,
      sessionId,
      role: 'assistant',
      content: '',
      createdAt: dayjs().toISOString(),
      isStreaming: true,
    }
    messagesMap.value[sessionId].push(aiMsg)
    streamingMap.value[sessionId] = true

    const client = getSSEClient('chat')
    await client.connect({
      url: CHAT_STREAM_URL,
      body: {
        sessionId,
        message: content,
        attachmentIds: attachments?.map(a => a.id),
      },
      onMessage: (chunk) => {
        aiMsg.content += chunk
      },
      onDone: (fullText) => {
        aiMsg.content = fullText
        aiMsg.isStreaming = false
        streamingMap.value[sessionId] = false
        if (session) session.updatedAt = dayjs().toISOString()
      },
      onError: () => {
        aiMsg.content = '请求失败，请稍后重试。'
        aiMsg.isStreaming = false
        streamingMap.value[sessionId] = false
        ElMessage.error('对话请求失败')
      },
    })
  }

  function stopStreaming() {
    abortSSE('chat')
    const sessionId = currentSessionId.value
    streamingMap.value[sessionId] = false
    const msgs = messagesMap.value[sessionId]
    if (msgs) {
      const last = msgs[msgs.length - 1]
      if (last?.isStreaming) last.isStreaming = false
    }
  }

  return {
    sessions,
    currentSessionId,
    currentMessages,
    isStreaming,
    loadSessions,
    createSession,
    selectSession,
    deleteSession,
    renameSession,
    sendMessage,
    stopStreaming,
  }
})
