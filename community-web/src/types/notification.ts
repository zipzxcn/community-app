import type { PageQuery } from './api'
import type { UserSummary } from './user'

export type NotificationType = 'POST_LIKE' | 'COMMENT' | 'FOLLOW' | 'CHAT' | 'SYSTEM'
export type NotificationTargetType = 'POST' | 'COMMENT' | 'USER' | 'THREAD'

export interface NotificationItem {
  id: number
  actorId?: number
  type: NotificationType
  targetType?: NotificationTargetType
  targetId?: number
  title: string
  content?: string
  read: boolean
  readAt?: string
  createdAt: string
  actor?: UserSummary
}

export interface NotificationUnreadSummary {
  total: number
  postLikeCount: number
  commentCount: number
  followCount: number
  chatCount: number
  systemCount: number
}

export interface NotificationQuery extends PageQuery {
  type?: NotificationType
  targetType?: NotificationTargetType
  isRead?: boolean
  keyword?: string
}
