import client from '@/api/client'

export interface LoginPayload {
  username: string
  password: string
}

export interface RegisterPayload {
  username: string
  password: string
  nickname: string
}

export function login(payload: LoginPayload) {
  return client.post('/auth/login', payload)
}

export function register(payload: RegisterPayload) {
  return client.post('/auth/register', payload)
}

export function fetchCurrentUser() {
  return client.get('/auth/me')
}
