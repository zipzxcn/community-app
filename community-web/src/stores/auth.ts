/**
 * 登录态 Store：
 * - 负责 accessToken/refreshToken/当前用户信息持久化。
 * - 解决刷新页面后登录态丢失的问题。
 * - 供路由守卫、Axios 拦截器、页面头部统一读取。
 */
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

// 从 localStorage 恢复登录快照；如果 JSON 损坏则自动清理，避免应用启动即报错。
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
    // 把登录态持久化到浏览器本地，解决 F5 刷新后状态丢失问题。
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
    // restore 会在路由守卫中触发：若本地还有 token，则调用 /auth/me 校验会话是否仍有效。
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
