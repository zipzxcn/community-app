import type { PageQuery } from './api'
import type { FileObject } from './file'
import type { Tag } from './post'

/**
 * 草稿模块类型：草稿列表、详情、保存载荷。
 */
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
  tagIds?: number[]
  tags?: Tag[]
  attachmentFileIds?: number[]
  attachmentFiles?: FileObject[]
  status: string
  publishedPostId?: number
}

export interface DraftQuery extends PageQuery {}

export interface SaveDraftPayload {
  title: string
  contentMd?: string
  coverUrl?: string
  tagIds?: number[]
  attachmentFileIds?: number[]
  autoSave?: boolean
}
