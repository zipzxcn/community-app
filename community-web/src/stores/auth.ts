import { defineStore } from 'pinia'

interface UserInfo {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
}

interface AuthState {
  accessToken: string
  refreshToken: string
  userInfo: UserInfo | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    accessToken: '',
    refreshToken: '',
    userInfo: null,
  }),
  actions: {
    setLogin(data: { accessToken: string; refreshToken: string; userInfo: UserInfo }) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
    },
    logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = null
    },
  },
})
