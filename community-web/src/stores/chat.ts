/**
 * 聊天连接 Store：
 * - 管理 WebSocket 连接状态、重连、事件广播。
 * - 业务设计上采用 REST + WebSocket 混合模式：消息收发走 REST，实时提醒走 WebSocket。
 */
import { defineStore } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import type { ChatWsEnvelope } from '@/types/chat'

type ConnectionState = 'idle' | 'connecting' | 'connected' | 'disconnected'
type ChatEventListener = (event: ChatWsEnvelope) => void

let socket: WebSocket | null = null
let reconnectTimer: number | null = null
const listeners = new Set<ChatEventListener>()

function clearReconnectTimer() {
  if (reconnectTimer !== null) {
    window.clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
}

// 兼容显式配置的 VITE_WS_BASE_URL 与基于当前站点自动推导的两种连接方式。
function resolveWebSocketUrl(token: string) {
  const explicit = import.meta.env.VITE_WS_BASE_URL
  if (explicit) {
    return `${explicit}${explicit.includes('?') ? '&' : '?'}token=${encodeURIComponent(token)}`
  }
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'
  if (/^https?:\/\//i.test(apiBaseUrl)) {
    const url = new URL(apiBaseUrl)
    url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:'
    url.pathname = '/ws/chat'
    url.search = `token=${encodeURIComponent(token)}`
    return url.toString()
  }
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${window.location.host}/ws/chat?token=${encodeURIComponent(token)}`
}

function broadcast(event: ChatWsEnvelope) {
  listeners.forEach((listener) => listener(event))
}

export const useChatStore = defineStore('chat', {
  state: () => ({
    connectionState: 'idle' as ConnectionState,
    lastError: '',
  }),
  getters: {
    connected: (state) => state.connectionState === 'connected',
  },
  actions: {
    subscribe(listener: ChatEventListener) {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    connect() {
      const authStore = useAuthStore()
      if (!authStore.accessToken) {
        this.disconnect()
        return
      }
      if (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
        return
      }

      clearReconnectTimer()
      this.connectionState = 'connecting'
      this.lastError = ''
      socket = new WebSocket(resolveWebSocketUrl(authStore.accessToken))

      socket.onopen = () => {
        this.connectionState = 'connected'
        this.lastError = ''
      }
      // 服务端推送 chat.message / chat.read 时，顺手刷新通知红点，保持顶部状态栏同步。
      socket.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data) as ChatWsEnvelope
          if (payload.type === 'chat.message' || payload.type === 'chat.read') {
            useNotificationStore().refreshUnread().catch(() => undefined)
          }
          broadcast(payload)
        } catch {
          this.lastError = '聊天推送消息解析失败'
        }
      }
      socket.onerror = () => {
        this.lastError = '聊天实时连接异常'
      }
      socket.onclose = () => {
        this.connectionState = 'disconnected'
        socket = null
        if (!useAuthStore().accessToken) {
          return
        }
        clearReconnectTimer()
        reconnectTimer = window.setTimeout(() => this.connect(), 3000)
      }
    },
    ping() {
      if (socket?.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify({ type: 'chat.ping' }))
      }
    },
    disconnect() {
      clearReconnectTimer()
      if (socket) {
        socket.onclose = null
        socket.close()
        socket = null
      }
      this.connectionState = 'idle'
      this.lastError = ''
    },
  },
})
