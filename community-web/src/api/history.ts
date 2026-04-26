import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { HistoryItem, HistoryQuery } from '@/types/history'

export function fetchHistories(params: HistoryQuery) {
  return client.get<never, PageResponse<HistoryItem>>('/histories', { params })
}

export function recordHistory(postId: number) {
  return client.post<never, void>(`/histories/${postId}`)
}

export function deleteHistory(historyId: number) {
  return client.delete<never, void>(`/histories/${historyId}`)
}

export function clearHistories() {
  return client.delete<never, void>('/histories')
}
