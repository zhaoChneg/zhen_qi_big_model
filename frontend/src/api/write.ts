import request from '@/utils/request'
import type { WriteDoc } from '@/types'

export const writeApi = {
  // 获取公文模板列表
  getTemplates(): Promise<{ category: string; types: { value: string; label: string }[] }[]> {
    return request.get('/write/templates')
  },

  // 生成摘要
  generateSummary(params: {
    docType: string
    title: string
    supplement?: string
    matters?: string
  }): Promise<{ summary: string }> {
    return request.post('/write/summary', params)
  },

  // 获取文稿列表
  getDocs(): Promise<WriteDoc[]> {
    return request.get('/write/docs')
  },

  // 保存文稿
  saveDoc(doc: Partial<WriteDoc>): Promise<WriteDoc> {
    return request.post('/write/docs', doc)
  },

  // 删除文稿
  deleteDoc(docId: string): Promise<void> {
    return request.delete(`/write/docs/${docId}`)
  },

  // AI 润色
  polishText(text: string): Promise<{ result: string }> {
    return request.post('/write/polish', { text })
  },
}

// SSE 写作生成 URL
export const WRITE_STREAM_URL = '/api/write/generate'
