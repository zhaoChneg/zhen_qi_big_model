import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AgentTool, AgentRun, ChatMessage } from '@/types'
import { agentApi, getAgentStreamUrl } from '@/api/agent'
import { getSSEClient } from '@/utils/sse'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

// 内置工具列表（可覆盖后端数据）
const BUILTIN_TOOLS: AgentTool[] = [
  {
    id: 'file-compare',
    name: '文件比对',
    description: '比较两个文件内容差异，找出不同之处。',
    category: 'document',
    icon: 'DocumentCopy',
    color: '#DC2626',
    type: 'chatflow',
    inputs: [
      { key: 'file1', label: '文件一', type: 'file', required: true, placeholder: '上传第一份文件' },
      { key: 'file2', label: '文件二', type: 'file', required: true, placeholder: '上传第二份文件' },
    ],
  },
  {
    id: 'file-review',
    name: '文件校审',
    description: '擅长在措辞精确字，优化适句逻辑，规范标点使用。',
    category: 'document',
    icon: 'Finished',
    color: '#DC2626',
    type: 'chatflow',
    inputs: [
      { key: 'file', label: '待校审文件', type: 'file', required: true, placeholder: '上传待审核文件' },
      { key: 'focus', label: '重点关注', type: 'select', required: false,
        options: [
          { label: '措辞规范', value: 'wording' },
          { label: '逻辑结构', value: 'logic' },
          { label: '标点符号', value: 'punctuation' },
          { label: '全面审查', value: 'all' },
        ],
      },
    ],
  },
  {
    id: 'compliance-check',
    name: '合规检查',
    description: '根据最新政策法规，对文件进行合规性分析与风险提示。',
    category: 'document',
    icon: 'CircleCheck',
    color: '#059669',
    type: 'chatflow',
    inputs: [
      { key: 'content', label: '文件内容', type: 'textarea', required: true, placeholder: '粘贴需要合规检查的文件内容' },
      { key: 'domain', label: '业务领域', type: 'select', required: true,
        options: [
          { label: '交通运输', value: 'transport' },
          { label: '工程建设', value: 'construction' },
          { label: '财务管理', value: 'finance' },
          { label: '人力资源', value: 'hr' },
        ],
      },
    ],
  },
  {
    id: 'meeting-minutes',
    name: '会议纪要',
    description: '将会议录音或文字记录，自动整理为结构化会议纪要。',
    category: 'meeting',
    icon: 'ChatDotRound',
    color: '#2563EB',
    type: 'chatflow',
    inputs: [
      { key: 'transcript', label: '会议记录', type: 'textarea', required: true, placeholder: '粘贴会议文字记录或语音转文字内容' },
      { key: 'meetingName', label: '会议名称', type: 'text', required: false, placeholder: '例：2025年一季度经营分析会' },
    ],
  },
  {
    id: 'data-report',
    name: '数据报告',
    description: '输入数据或描述，自动生成专业的数据分析报告。',
    category: 'data',
    icon: 'DataAnalysis',
    color: '#059669',
    type: 'chatflow',
    inputs: [
      { key: 'data', label: '数据内容', type: 'textarea', required: true, placeholder: '粘贴CSV数据或描述数据情况' },
      { key: 'reportType', label: '报告类型', type: 'select', required: true,
        options: [
          { label: '月度分析报告', value: 'monthly' },
          { label: '专项分析报告', value: 'special' },
          { label: '对比分析报告', value: 'compare' },
        ],
      },
    ],
  },
]

export const useAgentStore = defineStore('agent', () => {
  const tools = ref<AgentTool[]>(BUILTIN_TOOLS)
  const activeCategory = ref('all')
  const currentToolId = ref<string>('')
  const currentRun = ref<AgentRun | null>(null)
  const isRunning = ref(false)

  const filteredTools = computed(() => {
    if (activeCategory.value === 'all') return tools.value
    return tools.value.filter(t => t.category === activeCategory.value)
  })

  const currentTool = computed(() =>
    tools.value.find(t => t.id === currentToolId.value) || null
  )

  function setCategory(cat: string) {
    activeCategory.value = cat
  }

  function openTool(id: string) {
    currentToolId.value = id
    currentRun.value = {
      id: `run_${Date.now()}`,
      toolId: id,
      inputs: {},
      messages: [],
      status: 'running',
      createdAt: dayjs().toISOString(),
    }
  }

  function closeTool() {
    getSSEClient('agent').abort()
    currentToolId.value = ''
    currentRun.value = null
    isRunning.value = false
  }

  async function runTool(inputs: Record<string, unknown>) {
    if (!currentRun.value) return
    isRunning.value = true
    currentRun.value.inputs = inputs
    currentRun.value.messages = []

    // 添加用户消息（展示输入摘要）
    const userMsg: ChatMessage = {
      id: `u_${Date.now()}`,
      sessionId: currentRun.value.id,
      role: 'user',
      content: Object.entries(inputs)
        .filter(([, v]) => typeof v === 'string' && v)
        .map(([k, v]) => `${k}: ${String(v).slice(0, 50)}`)
        .join('\n'),
      createdAt: dayjs().toISOString(),
    }
    const aiMsg: ChatMessage = {
      id: `a_${Date.now()}`,
      sessionId: currentRun.value.id,
      role: 'assistant',
      content: '',
      createdAt: dayjs().toISOString(),
      isStreaming: true,
    }
    currentRun.value.messages.push(userMsg, aiMsg)

    const client = getSSEClient('agent')
    await client.connect({
      url: getAgentStreamUrl(currentToolId.value),
      body: { inputs },
      onMessage: (chunk) => { aiMsg.content += chunk },
      onDone: (full) => {
        aiMsg.content = full
        aiMsg.isStreaming = false
        isRunning.value = false
        if (currentRun.value) currentRun.value.status = 'done'
      },
      onError: () => {
        aiMsg.content = '工具执行失败，请稍后重试。'
        aiMsg.isStreaming = false
        isRunning.value = false
        ElMessage.error('工具执行失败')
      },
    })
  }

  async function loadTools() {
    try {
      const remote = await agentApi.getTools()
      if (remote.length > 0) tools.value = remote
    } catch {}
  }

  return {
    tools, activeCategory, currentToolId, currentRun, isRunning,
    filteredTools, currentTool,
    setCategory, openTool, closeTool, runTool, loadTools,
  }
})
