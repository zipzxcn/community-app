import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { NotificationItem, NotificationQuery, NotificationType, NotificationUnreadSummary } from '@/types/notification'

export function fetchNotifications(params: NotificationQuery) {
  return client.get<never, PageResponse<NotificationItem>>('/notifications', { params })
}

export function fetchUnreadNotificationSummary() {
  return client.get<never, NotificationUnreadSummary>('/notifications/unread-count')
}

export function markNotificationRead(notificationId: number) {
  return client.patch<never, void>(`/notifications/${notificationId}/read`)
}

export function markAllNotificationsRead(type?: NotificationType) {
  return client.patch<never, void>('/notifications/read-all', type ? { type } : {})
}
