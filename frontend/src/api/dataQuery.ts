import request from '@/utils/request'
import type { DataSource, DataQueryRecord, QueryResult } from '@/types'

export const dataQueryApi = {
  // 获取可用数据源列表
  getDataSources(): Promise<DataSource[]> {
    return request.get('/data/sources')
  },

  // 自然语言转SQL
  nl2sql(params: {
    question: string
    sourceId: string
  }): Promise<{ sql: string; explanation: string }> {
    return request.post('/data/nl2sql', params)
  },

  // 执行SQL查询
  executeQuery(params: {
    sql: string
    sourceId: string
    page?: number
    pageSize?: number
  }): Promise<QueryResult> {
    return request.post('/data/execute', params)
  },

  // AI生成数据解读
  generateSummary(params: {
    question: string
    data: QueryResult
  }): Promise<{ summary: string }> {
    return request.post('/data/summary', params)
  },

  // 获取查询历史
  getHistory(page = 1, pageSize = 20): Promise<{
    list: DataQueryRecord[]
    total: number
  }> {
    return request.get('/data/history', { params: { page, pageSize } })
  },

  // 删除历史记录
  deleteHistory(id: string): Promise<void> {
    return request.delete(`/data/history/${id}`)
  },
}
