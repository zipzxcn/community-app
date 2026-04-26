import type { PageQuery } from './api'
import type { UserSummary } from './user'

export type ChatThreadStatus = 'ACTIVE' | 'READ_ONLY'
export type ChatMessageType = 'TEXT' | 'IMAGE'

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
