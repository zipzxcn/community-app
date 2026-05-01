/**
 * 用户模块类型：摘要信息、主页信息、当前登录用户与资料更新载荷。
 */
export interface UserSummary {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
}

export interface UserProfile extends UserSummary {
  bio?: string
  postCount: number
  followerCount: number
  followingCount: number
  followed: boolean
  mutualFollow: boolean
}

export interface CurrentUser extends UserSummary {
  bio?: string
}

export interface UpdateProfilePayload {
  nickname?: string
  avatarUrl?: string
  bio?: string
}
