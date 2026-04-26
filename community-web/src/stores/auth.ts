import { defineStore } from 'pinia'
import { fetchCurrentUser, login, logout as logoutApi } from '@/api/auth'
import type { CurrentUser } from '@/types/user'
import type { LoginPayload, LoginResult } from '@/types/auth'

const STORAGE_KEY = 'community-auth'

interface AuthState {
  accessToken: string
  refreshToken: string
  userInfo: CurrentUser | null
  initialized: boolean
}

function loadStoredAuth(): Pick<AuthState, 'accessToken' | 'refreshToken' | 'userInfo'> {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return { accessToken: '', refreshToken: '', userInfo: null }
  }
  try {
    const parsed = JSON.parse(raw) as Pick<AuthState, 'accessToken' | 'refreshToken' | 'userInfo'>
    return {
      accessToken: parsed.accessToken || '',
      refreshToken: parsed.refreshToken || '',
      userInfo: parsed.userInfo || null,
    }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return { accessToken: '', refreshToken: '', userInfo: null }
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    ...loadStoredAuth(),
    initialized: false,
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.accessToken),
  },
  actions: {
    persist() {
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          accessToken: this.accessToken,
          refreshToken: this.refreshToken,
          userInfo: this.userInfo,
        }),
      )
    },
    setLogin(data: LoginResult) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.userInfo = data.userInfo
      this.persist()
    },
    setUserInfo(userInfo: CurrentUser) {
      this.userInfo = userInfo
      this.persist()
    },
    async login(payload: LoginPayload) {
      const result = await login(payload)
      this.setLogin(result)
      return result
    },
    async restore() {
      if (this.initialized) {
        return
      }
      this.initialized = true
      if (!this.accessToken) {
        return
      }
      try {
        this.userInfo = await fetchCurrentUser()
        this.persist()
      } catch {
        this.clear()
      }
    },
    clear() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem(STORAGE_KEY)
    },
    async logout() {
      const token = this.refreshToken
      this.clear()
      if (token) {
        await logoutApi(token).catch(() => undefined)
      }
    },
  },
})
