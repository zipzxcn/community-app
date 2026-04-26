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
