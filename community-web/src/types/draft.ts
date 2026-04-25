import type { PageQuery } from './api'

export interface DraftItem {
  id: number
  title: string
  coverUrl?: string
  autoSavedAt?: string
  updatedAt: string
  createdAt: string
}

export interface DraftDetail extends DraftItem {
  contentMd?: string
  status: string
  publishedPostId?: number
}

export interface DraftQuery extends PageQuery {}

export interface SaveDraftPayload {
  title: string
  contentMd?: string
  coverUrl?: string
  autoSave?: boolean
}
