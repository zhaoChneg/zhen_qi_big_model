import axios from 'axios'
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 - 注入Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jt_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const { data } = response
    if (data.code === 200 || data.code === 0) {
      return data.data as any
    }
    if (data.code === 401) {
      localStorage.removeItem('jt_token')
      window.location.href = '/login'
      return Promise.reject(new Error('登录已过期'))
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message))
  },
  (error) => {
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else if (error.response?.status === 503) {
      ElMessage.error('服务繁忙，请稍后重试')
    } else {
      ElMessage.error(error.response?.data?.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request

// 防抖请求工厂（问数模块使用）
export function createDebouncedRequest<T>(fn: (...args: any[]) => Promise<T>, delay = 500) {
  let timer: ReturnType<typeof setTimeout> | null = null
  let abortController: AbortController | null = null

  return function (...args: any[]): Promise<T> {
    if (timer) clearTimeout(timer)
    if (abortController) abortController.abort()
    abortController = new AbortController()

    return new Promise((resolve, reject) => {
      timer = setTimeout(async () => {
        try {
          const result = await fn(...args)
          resolve(result)
        } catch (e) {
          reject(e)
        }
      }, delay)
    })
  }
}
