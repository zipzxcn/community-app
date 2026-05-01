/**
 * Axios 客户端封装：
 * - 统一 baseURL、超时与错误处理。
 * - 请求拦截器自动补 Bearer Token。
 * - 响应拦截器把后端 ApiResponse<T> 解包成真正业务数据。
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import type { ApiResponse } from '@/types/api'

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 10000,
})

// 请求拦截器解决的问题：业务代码无需在每个 API 调用点手工拼 Authorization 头。
client.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`
  }
  return config
})

// 响应拦截器把后端统一响应协议 { code, message, data } 变成前端更易用的数据结构。
client.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    if (body && typeof body.code === 'number') {
      if (body.code !== 0) {
        return Promise.reject(new Error(body.message || `请求失败：${body.code}`))
      }
      return body.data
    }
    return response.data
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default client
