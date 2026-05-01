import type { PageQuery } from './api'
import type { UserSummary } from './user'

/**
 * 浏览历史模块类型：历史记录列表与分页参数。
 */
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
