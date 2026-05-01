/**
 * 草稿接口封装：支持草稿列表、详情、保存、删除与发布。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { DraftDetail, DraftItem, DraftQuery, SaveDraftPayload } from '@/types/draft'

/**
 * fetchDrafts：按函数名对应后端接口完成请求封装。
 */
export function fetchDrafts(params: DraftQuery) {
  return client.get<never, PageResponse<DraftItem>>('/drafts', { params })
}

/**
 * fetchDraftDetail：按函数名对应后端接口完成请求封装。
 */
export function fetchDraftDetail(draftId: number) {
  return client.get<never, DraftDetail>(`/drafts/${draftId}`)
}

/**
 * createDraft：按函数名对应后端接口完成请求封装。
 */
export function createDraft(payload: SaveDraftPayload) {
  return client.post<never, { draftId: number }>('/drafts', payload)
}

/**
 * updateDraft：按函数名对应后端接口完成请求封装。
 */
export function updateDraft(draftId: number, payload: SaveDraftPayload) {
  return client.put<never, DraftDetail>(`/drafts/${draftId}`, payload)
}

/**
 * deleteDraft：按函数名对应后端接口完成请求封装。
 */
export function deleteDraft(draftId: number) {
  return client.delete<never, void>(`/drafts/${draftId}`)
}

/**
 * publishDraft：按函数名对应后端接口完成请求封装。
 */
export function publishDraft(draftId: number) {
  return client.post<never, { postId: number }>(`/drafts/${draftId}/publish`)
}
