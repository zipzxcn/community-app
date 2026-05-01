import type { PageQuery } from './api'
import type { UserSummary } from './user'

// ACTIVE 表示双方仍互关可继续聊天；READ_ONLY 表示历史会话可见但当前关系不允许继续发送。
export type ChatThreadStatus = 'ACTIVE' | 'READ_ONLY'
export type ChatMessageType = 'TEXT' | 'IMAGE'

/**
 * 聊天模块类型：会话、消息、WebSocket 事件与已读回执。
 */
export interface ChatThreadItem {
  threadId: number | null
  status: ChatThreadStatus
  lastMessagePreview?: string
  lastMessageAt?: string
  unreadCount: number
  peerUser: UserSummary
}

export interface ChatMessageItem {
  id: number
  threadId: number
  senderId: number
  receiverId: number
  clientMsgId: string
  messageType: ChatMessageType
  content: string
  read: boolean
  readAt?: string
  createdAt: string
}

export interface ChatMessageQuery extends PageQuery {
  cursor?: number
}

export interface SendChatMessagePayload {
  clientMsgId: string
  messageType: ChatMessageType
  content: string
}

export interface MarkChatReadPayload {
  lastReadMessageId: number
}

/**
 * WebSocket 事件信封：服务端统一按 { type, data } 推送，前端据此分发不同实时事件。
 */
export interface ChatWsEnvelope<T = unknown> {
  type: 'chat.connected' | 'chat.pong' | 'chat.message' | 'chat.read' | 'chat.error'
  data: T
}

export interface ChatReadReceipt {
  threadId: number
  lastReadMessageId: number
  readerId: number
  readAt: string
}
