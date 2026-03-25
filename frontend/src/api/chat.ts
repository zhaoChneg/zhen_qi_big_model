import request from '@/utils/request'
import type { ChatSession, ChatMessage, FileAttachment } from '@/types'

export const chatApi = {
  // 获取会话列表
  getSessions(): Promise<ChatSession[]> {
    return request.get('/chat/sessions')
  },

  // 创建新会话
  createSession(title?: string): Promise<ChatSession> {
    return request.post('/chat/sessions', { title: title || '新对话' })
  },

  // 删除会话
  deleteSession(sessionId: string): Promise<void> {
    return request.delete(`/chat/sessions/${sessionId}`)
  },

  // 更新会话标题
  updateSession(sessionId: string, title: string): Promise<void> {
    return request.put(`/chat/sessions/${sessionId}`, { title })
  },

  // 获取会话消息列表
  getMessages(sessionId: string): Promise<ChatMessage[]> {
    return request.get(`/chat/sessions/${sessionId}/messages`)
  },

  // 上传文件
  uploadFile(file: File): Promise<FileAttachment> {
    const form = new FormData()
    form.append('file', file)
    return request.post('/chat/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// SSE 流式对话的 URL（不走axios）
export const CHAT_STREAM_URL = '/api/chat/completions'
