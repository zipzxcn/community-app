import type { PageQuery } from './api'
import type { UserSummary } from './user'

export interface HistoryItem {
  id: number
  postId?: number
  postTitle?: string
  postCoverUrl?: string
  viewCount: number
  lastViewedAt: string
  author?: UserSummary
}

export interface HistoryQuery extends PageQuery {}
