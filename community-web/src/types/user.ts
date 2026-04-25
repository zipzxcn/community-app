export interface UserSummary {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
}

export interface CurrentUser extends UserSummary {}
