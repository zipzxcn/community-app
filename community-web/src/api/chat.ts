/**
 * 聊天接口封装：REST 负责会话列表、历史消息、发送消息、已读同步。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type {
  ChatMessageItem,
  ChatMessageQuery,
  ChatThreadItem,
  MarkChatReadPayload,
  SendChatMessagePayload,
} from '@/types/chat'

/**
 * fetchChatThreads：按函数名对应后端接口完成请求封装。
 */
export function fetchChatThreads(page = 1, size = 50) {
  return client.get<never, PageResponse<ChatThreadItem>>('/chat/threads', {
    params: { page, size },
  })
}

/**
 * openChatThread：按函数名对应后端接口完成请求封装。
 */
export function openChatThread(targetUserId: number) {
  return client.post<never, ChatThreadItem>(`/chat/threads/with/${targetUserId}`)
}

/**
 * fetchChatMessages：按函数名对应后端接口完成请求封装。
 */
export function fetchChatMessages(threadId: number, params: ChatMessageQuery = {}) {
  return client.get<never, PageResponse<ChatMessageItem>>(`/chat/threads/${threadId}/messages`, {
    params,
  })
}

/**
 * sendChatMessage：按函数名对应后端接口完成请求封装。
 */
export function sendChatMessage(threadId: number, payload: SendChatMessagePayload) {
  return client.post<SendChatMessagePayload, ChatMessageItem>(`/chat/threads/${threadId}/messages`, payload)
}

/**
 * markChatThreadRead：按函数名对应后端接口完成请求封装。
 */
export function markChatThreadRead(threadId: number, payload: MarkChatReadPayload) {
  return client.post<MarkChatReadPayload, void>(`/chat/threads/${threadId}/read`, payload)
}
