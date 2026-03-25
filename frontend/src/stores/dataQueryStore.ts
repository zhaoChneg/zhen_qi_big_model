import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DataSource, DataQueryRecord, QueryResult } from '@/types'
import { dataQueryApi } from '@/api/dataQuery'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

export const useDataQueryStore = defineStore('dataQuery', () => {
  const dataSources = ref<DataSource[]>([])
  const selectedSourceId = ref<string>('')
  const queryHistory = ref<DataQueryRecord[]>([])
  const currentRecord = ref<DataQueryRecord | null>(null)
  const currentQueryId = ref<string>('')

  // 当前输入状态
  const question = ref<string>('')
  const generatedSQL = ref<string>('')
  const sqlExplanation = ref<string>('')
  const queryResult = ref<QueryResult | null>(null)
  const aiSummary = ref<string>('')

  // 加载状态
  const nl2sqlLoading = ref(false)
  const queryLoading = ref(false)
  const summaryLoading = ref(false)

  async function loadDataSources() {
    try {
      dataSources.value = await dataQueryApi.getDataSources()
      if (dataSources.value.length > 0 && !selectedSourceId.value) {
        selectedSourceId.value = dataSources.value[0].id
      }
    } catch {}
  }

  function selectSource(id: string) {
    selectedSourceId.value = id
    resetQuery()
  }

  function resetQuery() {
    question.value = ''
    generatedSQL.value = ''
    sqlExplanation.value = ''
    queryResult.value = null
    aiSummary.value = ''
    currentQueryId.value = ''
  }

  // 步骤1：自然语言转SQL
  async function generateSQL(q: string) {
    if (!selectedSourceId.value) {
      ElMessage.warning('请先选择数据源')
      return
    }
    question.value = q
    generatedSQL.value = ''
    sqlExplanation.value = ''
    queryResult.value = null
    aiSummary.value = ''
    nl2sqlLoading.value = true

    try {
      const res = await dataQueryApi.nl2sql({ question: q, sourceId: selectedSourceId.value })
      generatedSQL.value = res.sql
      sqlExplanation.value = res.explanation
    } catch {
      ElMessage.error('SQL生成失败，请重新描述您的问题')
    } finally {
      nl2sqlLoading.value = false
    }
  }

  // 步骤2：执行查询
  async function executeQuery(sql?: string) {
    const execSQL = sql || generatedSQL.value
    if (!execSQL.trim()) {
      ElMessage.warning('请先生成查询SQL')
      return
    }
    queryLoading.value = true
    queryResult.value = null
    aiSummary.value = ''

    // 添加历史记录（pending状态）
    const record: DataQueryRecord = {
      id: `qr_${Date.now()}`,
      question: question.value,
      sql: execSQL,
      status: 'running',
      createdAt: dayjs().toISOString(),
    }
    queryHistory.value.unshift(record)
    currentQueryId.value = record.id

    const startTime = Date.now()
    try {
      const result = await dataQueryApi.executeQuery({
        sql: execSQL,
        sourceId: selectedSourceId.value,
      })
      queryResult.value = result
      record.status = 'success'
      record.result = result
      record.duration = Date.now() - startTime

      // 异步生成AI解读
      generateSummary(result)
    } catch (e: any) {
      record.status = 'error'
      record.errorMsg = e?.message || '查询失败'
      ElMessage.error(record.errorMsg)
    } finally {
      queryLoading.value = false
    }
  }

  // 步骤3：AI生成数据解读
  async function generateSummary(result: QueryResult) {
    summaryLoading.value = true
    try {
      const res = await dataQueryApi.generateSummary({
        question: question.value,
        data: result,
      })
      aiSummary.value = res.summary
    } catch {} finally {
      summaryLoading.value = false
    }
  }

  function loadHistoryRecord(record: DataQueryRecord) {
    currentQueryId.value = record.id
    question.value = record.question
    generatedSQL.value = record.sql
    queryResult.value = record.result || null
    aiSummary.value = record.result?.summary || ''
  }

  async function loadHistory() {
    try {
      const res = await dataQueryApi.getHistory()
      queryHistory.value = res.list
    } catch {}
  }

  async function deleteHistory(id: string) {
    await dataQueryApi.deleteHistory(id)
    queryHistory.value = queryHistory.value.filter(r => r.id !== id)
    if (currentQueryId.value === id) resetQuery()
  }

  return {
    dataSources,
    selectedSourceId,
    queryHistory,
    currentQueryId,
    question,
    generatedSQL,
    sqlExplanation,
    queryResult,
    aiSummary,
    nl2sqlLoading,
    queryLoading,
    summaryLoading,
    loadDataSources,
    selectSource,
    resetQuery,
    generateSQL,
    executeQuery,
    loadHistoryRecord,
    loadHistory,
    deleteHistory,
  }
})
