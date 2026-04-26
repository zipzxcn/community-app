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
