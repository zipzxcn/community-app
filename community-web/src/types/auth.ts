import type { CurrentUser } from './user'

/**
 * 认证模块类型：登录、注册、验证码、登录返回结构。
 */
export interface LoginPayload {
  username: string
  password: string
  captchaId: string
  captchaCode: string
}

export interface RegisterPayload {
  username: string
  password: string
  nickname: string
  captchaId: string
  captchaCode: string
}

export interface AuthCaptcha {
  captchaId: string
  captchaImageBase64: string
  expireInSeconds: number
}

export interface LoginResult {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: CurrentUser
}
