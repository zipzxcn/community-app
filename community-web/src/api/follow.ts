/**
 * 关注关系接口封装：关注、取关、关注列表、粉丝列表、互关列表。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { FollowUser } from '@/types/follow'

/**
 * followUser：按函数名对应后端接口完成请求封装。
 */
export function followUser(targetUserId: number) {
  return client.post<never, void>(`/follows/${targetUserId}`)
}

/**
 * unfollowUser：按函数名对应后端接口完成请求封装。
 */
export function unfollowUser(targetUserId: number) {
  return client.delete<never, void>(`/follows/${targetUserId}`)
}

/**
 * fetchFollowing：按函数名对应后端接口完成请求封装。
 */
export function fetchFollowing(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/following', {
    params: { page, size },
  })
}

/**
 * fetchFollowers：按函数名对应后端接口完成请求封装。
 */
export function fetchFollowers(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/followers', {
    params: { page, size },
  })
}

/**
 * fetchMutualFollows：按函数名对应后端接口完成请求封装。
 */
export function fetchMutualFollows(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/mutual', {
    params: { page, size },
  })
}
