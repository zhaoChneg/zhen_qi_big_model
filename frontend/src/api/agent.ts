import request from '@/utils/request'
import type { AgentTool, AgentRun } from '@/types'

export const agentApi = {
  // 获取工具列表
  getTools(): Promise<AgentTool[]> {
    return request.get('/agents')
  },

  // 获取运行历史
  getRuns(toolId?: string): Promise<AgentRun[]> {
    return request.get('/agents/runs', { params: { toolId } })
  },

  // 删除运行记录
  deleteRun(runId: string): Promise<void> {
    return request.delete(`/agents/runs/${runId}`)
  },

  // 上传工具输入文件
  uploadFile(file: File): Promise<{ id: string; name: string; url: string }> {
    const form = new FormData()
    form.append('file', file)
    return request.post('/agents/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// SSE 工具执行 URL
export function getAgentStreamUrl(toolId: string) {
  return `/api/agents/${toolId}/run`
}
