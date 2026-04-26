import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type {
  ChatMessageItem,
  ChatMessageQuery,
  ChatThreadItem,
  MarkChatReadPayload,
  SendChatMessagePayload,
} from '@/types/chat'

export function fetchChatThreads(page = 1, size = 50) {
  return client.get<never, PageResponse<ChatThreadItem>>('/chat/threads', {
    params: { page, size },
  })
}

export function openChatThread(targetUserId: number) {
  return client.post<never, ChatThreadItem>(`/chat/threads/with/${targetUserId}`)
}

export function fetchChatMessages(threadId: number, params: ChatMessageQuery = {}) {
  return client.get<never, PageResponse<ChatMessageItem>>(`/chat/threads/${threadId}/messages`, {
    params,
  })
}

export function sendChatMessage(threadId: number, payload: SendChatMessagePayload) {
  return client.post<SendChatMessagePayload, ChatMessageItem>(`/chat/threads/${threadId}/messages`, payload)
}

export function markChatThreadRead(threadId: number, payload: MarkChatReadPayload) {
  return client.post<MarkChatReadPayload, void>(`/chat/threads/${threadId}/read`, payload)
}
