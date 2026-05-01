import client from '@/api/client'
import type { CurrentUser } from '@/types/user'
import type { AuthCaptcha, LoginPayload, LoginResult, RegisterPayload } from '@/types/auth'

export function fetchCaptcha() {
  return client.get<never, AuthCaptcha>('/auth/captcha')
}

export function login(payload: LoginPayload) {
  return client.post<never, LoginResult>('/auth/login', payload)
}

export function register(payload: RegisterPayload) {
  return client.post<never, { userId: number }>('/auth/register', payload)
}

export function fetchCurrentUser() {
  return client.get<never, CurrentUser>('/auth/me')
}

export function logout(refreshToken: string) {
  return client.post<never, void>('/auth/logout', { refreshToken })
}
