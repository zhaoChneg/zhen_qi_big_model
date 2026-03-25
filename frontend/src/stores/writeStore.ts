import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { WriteDoc } from '@/types'
import { writeApi, WRITE_STREAM_URL } from '@/api/write'
import { getSSEClient } from '@/utils/sse'
import { ElMessage } from 'element-plus'

export const useWriteStore = defineStore('write', () => {
  const activeTool = ref('draft')
  const docType = ref('')
  const docCategory = ref<'legal' | 'business'>('legal')
  const docTitle = ref('')
  const supplement = ref('')
  const matters = ref('')
  const summary = ref('')
  const editorContent = ref('')
  const currentDocId = ref('')
  const docList = ref<WriteDoc[]>([])
  const summaryLoading = ref(false)
  const generating = ref(false)
  const deepThink = ref(false)

  function setActiveTool(key: string) {
    activeTool.value = key
  }

  async function generateSummary() {
    if (!docType.value || !docTitle.value) {
      ElMessage.warning('请先选择公文类型并填写标题')
      return
    }
    summaryLoading.value = true
    try {
      const res = await writeApi.generateSummary({
        docType: docType.value,
        title: docTitle.value,
        supplement: supplement.value,
        matters: matters.value,
      })
      summary.value = res.summary
    } catch {
      ElMessage.error('摘要生成失败')
    } finally {
      summaryLoading.value = false
    }
  }

  async function generateDoc() {
    if (!summary.value) {
      ElMessage.warning('请先生成摘要')
      return
    }
    generating.value = true
    editorContent.value = ''

    const client = getSSEClient('write')
    await client.connect({
      url: WRITE_STREAM_URL,
      body: {
        docType: docType.value,
        title: docTitle.value,
        summary: summary.value,
        deepThink: deepThink.value,
      },
      onMessage: (chunk) => {
        editorContent.value += chunk
      },
      onDone: (full) => {
        editorContent.value = full
        generating.value = false
      },
      onError: () => {
        generating.value = false
        ElMessage.error('文稿生成失败')
      },
    })
  }

  async function polishSelection(text: string): Promise<string> {
    const res = await writeApi.polishText(text)
    return res.result
  }

  async function loadDocs() {
    try {
      docList.value = await writeApi.getDocs()
    } catch {}
  }

  async function saveDoc() {
    const doc = await writeApi.saveDoc({
      id: currentDocId.value || undefined,
      docType: docType.value,
      title: docTitle.value,
      content: editorContent.value,
      summary: summary.value,
    })
    currentDocId.value = doc.id
    const idx = docList.value.findIndex(d => d.id === doc.id)
    if (idx >= 0) docList.value[idx] = doc
    else docList.value.unshift(doc)
    ElMessage.success('已保存')
  }

  async function deleteDoc(id: string) {
    await writeApi.deleteDoc(id)
    docList.value = docList.value.filter(d => d.id !== id)
    if (currentDocId.value === id) resetForm()
  }

  function resetForm() {
    docType.value = ''
    docTitle.value = ''
    supplement.value = ''
    matters.value = ''
    summary.value = ''
    editorContent.value = ''
    currentDocId.value = ''
  }

  return {
    activeTool, docType, docCategory, docTitle, supplement, matters,
    summary, editorContent, currentDocId, docList,
    summaryLoading, generating, deepThink,
    setActiveTool, generateSummary, generateDoc, polishSelection,
    loadDocs, saveDoc, deleteDoc, resetForm,
  }
})
