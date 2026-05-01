/**
 * 通知 Store：集中维护未读汇总，供头部徽标、聊天入口、通知中心共享。
 */
import { defineStore } from 'pinia'
import { fetchUnreadNotificationSummary } from '@/api/notification'
import type { NotificationUnreadSummary } from '@/types/notification'

const EMPTY_SUMMARY: NotificationUnreadSummary = {
  total: 0,
  postLikeCount: 0,
  commentCount: 0,
  followCount: 0,
  chatCount: 0,
  systemCount: 0,
}

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unread: { ...EMPTY_SUMMARY } as NotificationUnreadSummary,
    loading: false,
  }),
  actions: {
    reset() {
      this.unread = { ...EMPTY_SUMMARY }
      this.loading = false
    },
    // 主动拉取未读汇总，避免每个页面各自重复请求。
    async refreshUnread() {
      this.loading = true
      try {
        this.unread = await fetchUnreadNotificationSummary()
      } finally {
        this.loading = false
      }
    },
  },
})
