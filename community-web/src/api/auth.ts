/**
 * 认证接口封装：对应后端 /api/v1/auth 下的登录、注册、当前用户与登出能力。
 */
import client from '@/api/client'
import type { CurrentUser } from '@/types/user'
import type { AuthCaptcha, LoginPayload, LoginResult, RegisterPayload } from '@/types/auth'

/**
 * fetchCaptcha：按函数名对应后端接口完成请求封装。
 */
export function fetchCaptcha() {
  return client.get<never, AuthCaptcha>('/auth/captcha')
}

/**
 * login：按函数名对应后端接口完成请求封装。
 */
export function login(payload: LoginPayload) {
  return client.post<never, LoginResult>('/auth/login', payload)
}

/**
 * register：按函数名对应后端接口完成请求封装。
 */
export function register(payload: RegisterPayload) {
  return client.post<never, { userId: number }>('/auth/register', payload)
}

/**
 * fetchCurrentUser：按函数名对应后端接口完成请求封装。
 */
export function fetchCurrentUser() {
  return client.get<never, CurrentUser>('/auth/me')
}

/**
 * logout：按函数名对应后端接口完成请求封装。
 */
export function logout(refreshToken: string) {
  return client.post<never, void>('/auth/logout', { refreshToken })
}
