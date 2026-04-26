export interface FollowUser {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
  followedByMe: boolean
  mutualFollow: boolean
}
