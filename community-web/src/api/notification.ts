/**
 * 通知中心接口封装：通知列表、未读汇总、单条已读、批量已读。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { NotificationItem, NotificationQuery, NotificationType, NotificationUnreadSummary } from '@/types/notification'

/**
 * fetchNotifications：按函数名对应后端接口完成请求封装。
 */
export function fetchNotifications(params: NotificationQuery) {
  return client.get<never, PageResponse<NotificationItem>>('/notifications', { params })
}

/**
 * fetchUnreadNotificationSummary：按函数名对应后端接口完成请求封装。
 */
export function fetchUnreadNotificationSummary() {
  return client.get<never, NotificationUnreadSummary>('/notifications/unread-count')
}

/**
 * markNotificationRead：按函数名对应后端接口完成请求封装。
 */
export function markNotificationRead(notificationId: number) {
  return client.patch<never, void>(`/notifications/${notificationId}/read`)
}

/**
 * markAllNotificationsRead：按函数名对应后端接口完成请求封装。
 */
export function markAllNotificationsRead(type?: NotificationType) {
  return client.patch<never, void>('/notifications/read-all', type ? { type } : {})
}
