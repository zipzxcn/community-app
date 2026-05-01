import type { CurrentUser } from './user'

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
