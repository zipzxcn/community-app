import type { PageQuery } from './api'
import type { UserSummary } from './user'

export type NotificationType = 'POST_LIKE' | 'COMMENT' | 'FOLLOW' | 'CHAT' | 'SYSTEM'
export type NotificationTargetType = 'POST' | 'COMMENT' | 'USER' | 'THREAD'

/**
 * 通知模块类型：通知列表项、未读统计、筛选条件。
 */
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

/**
 * 未读统计：顶部导航与通知中心都会依赖这份聚合数据来显示红点。
 */
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
