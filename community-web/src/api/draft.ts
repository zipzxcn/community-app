import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { DraftDetail, DraftItem, DraftQuery, SaveDraftPayload } from '@/types/draft'

export function fetchDrafts(params: DraftQuery) {
  return client.get<never, PageResponse<DraftItem>>('/drafts', { params })
}

export function fetchDraftDetail(draftId: number) {
  return client.get<never, DraftDetail>(`/drafts/${draftId}`)
}

export function createDraft(payload: SaveDraftPayload) {
  return client.post<never, { draftId: number }>('/drafts', payload)
}

export function updateDraft(draftId: number, payload: SaveDraftPayload) {
  return client.put<never, DraftDetail>(`/drafts/${draftId}`, payload)
}

export function deleteDraft(draftId: number) {
  return client.delete<never, void>(`/drafts/${draftId}`)
}

export function publishDraft(draftId: number) {
  return client.post<never, { postId: number }>(`/drafts/${draftId}/publish`)
}
