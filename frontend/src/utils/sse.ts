/**
 * SSE (Server-Sent Events) 封装
 * 支持流式输出、AbortController中断、指数退避重连
 */

export interface SSEOptions {
  url: string
  body: Record<string, unknown>
  onMessage: (chunk: string) => void
  onDone: (fullText: string) => void
  onError: (error: Error) => void
  maxRetries?: number
}

export class SSEClient {
  private abortController: AbortController | null = null
  private retryCount = 0
  private maxRetries: number

  constructor(maxRetries = 3) {
    this.maxRetries = maxRetries
  }

  async connect(options: SSEOptions): Promise<void> {
    this.abort()
    this.abortController = new AbortController()
    this.retryCount = 0
    await this.doConnect(options)
  }

  abort(): void {
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
    }
  }

  private async doConnect(options: SSEOptions): Promise<void> {
    const token = localStorage.getItem('jt_token')
    let fullText = ''

    try {
      const response = await fetch(options.url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: token ? `Bearer ${token}` : '',
          Accept: 'text/event-stream',
        },
        body: JSON.stringify(options.body),
        signal: this.abortController?.signal,
      })

      if (!response.ok) {
        throw new Error(`HTTP error: ${response.status}`)
      }

      const reader = response.body?.getReader()
      if (!reader) throw new Error('No readable stream')

      const decoder = new TextDecoder('utf-8')

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const text = decoder.decode(value, { stream: true })
        const lines = text.split('\n')

        for (const line of lines) {
          if (line.startsWith('data: ')) {
            const data = line.slice(6).trim()
            if (data === '[DONE]') {
              options.onDone(fullText)
              return
            }
            try {
              const parsed = JSON.parse(data)
              const chunk = parsed.choices?.[0]?.delta?.content
                || parsed.content
                || parsed.chunk
                || ''
              if (chunk) {
                fullText += chunk
                options.onMessage(chunk)
              }
            } catch {
              // 非JSON格式，直接作为文本
              if (data) {
                fullText += data
                options.onMessage(data)
              }
            }
          }
        }
      }

      options.onDone(fullText)
    } catch (error: any) {
      if (error.name === 'AbortError') return

      if (this.retryCount < this.maxRetries) {
        this.retryCount++
        const delay = Math.pow(2, this.retryCount) * 1000 // 指数退避
        await new Promise(r => setTimeout(r, delay))
        await this.doConnect(options)
      } else {
        options.onError(error)
      }
    }
  }
}

// 全局SSE客户端实例管理（每用户最多1个活跃连接）
const sseInstances = new Map<string, SSEClient>()

export function getSSEClient(key: string): SSEClient {
  if (!sseInstances.has(key)) {
    sseInstances.set(key, new SSEClient(3))
  }
  return sseInstances.get(key)!
}

export function abortSSE(key: string): void {
  sseInstances.get(key)?.abort()
}
