import client from '@/api/client'
import type { CurrentUser } from '@/types/user'
import type { LoginPayload, LoginResult, RegisterPayload } from '@/types/auth'

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
