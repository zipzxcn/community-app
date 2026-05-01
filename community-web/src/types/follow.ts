/**
 * 关注模块类型：关注列表中的用户关系信息。
 */
export interface FollowUser {
  id: number
  username: string
  nickname: string
  avatarUrl?: string
  followedByMe: boolean
  mutualFollow: boolean
}
