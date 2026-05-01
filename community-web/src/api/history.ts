/**
 * 浏览历史接口封装：列表查询、单条删除、全部清空与访问记录。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { HistoryItem, HistoryQuery } from '@/types/history'

/**
 * fetchHistories：按函数名对应后端接口完成请求封装。
 */
export function fetchHistories(params: HistoryQuery) {
  return client.get<never, PageResponse<HistoryItem>>('/histories', { params })
}

/**
 * recordHistory：按函数名对应后端接口完成请求封装。
 */
export function recordHistory(postId: number) {
  return client.post<never, void>(`/histories/${postId}`)
}

/**
 * deleteHistory：按函数名对应后端接口完成请求封装。
 */
export function deleteHistory(historyId: number) {
  return client.delete<never, void>(`/histories/${historyId}`)
}

/**
 * clearHistories：按函数名对应后端接口完成请求封装。
 */
export function clearHistories() {
  return client.delete<never, void>('/histories')
}
