// ==================== 用户 ====================
export interface UserInfo {
  id: string
  username: string
  displayName: string
  avatar?: string
  department?: string
  role: 'admin' | 'user'
}

// ==================== 对话（得心） ====================
export interface ChatSession {
  id: string
  title: string
  createdAt: string
  updatedAt: string
  messageCount: number
}

export interface ChatMessage {
  id: string
  sessionId: string
  role: 'user' | 'assistant' | 'system'
  content: string
  createdAt: string
  isStreaming?: boolean
  attachments?: FileAttachment[]
}

export interface FileAttachment {
  id: string
  name: string
  size: number
  type: string
  url: string
}

// ==================== 公文写作（应手） ====================
export type DocCategory = 'legal' | 'business'

export interface DocType {
  value: string
  label: string
  category: DocCategory
}

export interface WriteDoc {
  id: string
  title: string
  docType: string
  content: string
  summary: string
  createdAt: string
  updatedAt: string
}

// ==================== 问数 ====================
export interface DataSource {
  id: string
  name: string
  description: string
  type: 'mysql' | 'postgresql' | 'oracle'
  database: string
  tables: TableInfo[]
}

export interface TableInfo {
  name: string
  comment: string
  columns: ColumnInfo[]
}

export interface ColumnInfo {
  name: string
  type: string
  comment: string
}

export interface DataQueryRecord {
  id: string
  question: string
  sql: string
  status: 'pending' | 'running' | 'success' | 'error'
  result?: QueryResult
  errorMsg?: string
  createdAt: string
  duration?: number
}

export interface QueryResult {
  columns: string[]
  rows: Record<string, unknown>[]
  total: number
  summary?: string
}

// ==================== 知事工具 ====================
export interface AgentTool {
  id: string
  name: string
  description: string
  category: string
  icon: string
  color: string
  type: 'chatflow' | 'workflow'
  inputs: AgentInput[]
}

export interface AgentInput {
  key: string
  label: string
  type: 'text' | 'file' | 'select' | 'textarea'
  required: boolean
  placeholder?: string
  options?: { label: string; value: string }[]
}

export interface AgentRun {
  id: string
  toolId: string
  inputs: Record<string, unknown>
  messages: ChatMessage[]
  status: 'running' | 'done' | 'error'
  createdAt: string
}

// ==================== 通用 ====================
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}
