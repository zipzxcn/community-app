/**
 * 用户接口封装：主页、搜索、资料修改、帖子/收藏/点赞列表。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { PostListItem } from '@/types/post'
import type { UpdateProfilePayload, UserProfile, UserSummary } from '@/types/user'

/**
 * fetchUserProfile：按函数名对应后端接口完成请求封装。
 */
export function fetchUserProfile(userId: number) {
  return client.get<never, UserProfile>(`/users/${userId}`)
}

/**
 * searchUsers：按函数名对应后端接口完成请求封装。
 */
export function searchUsers(keyword: string, page = 1, size = 20) {
  return client.get<never, PageResponse<UserSummary>>('/users/search', {
    params: { keyword, page, size },
  })
}

/**
 * updateMyProfile：按函数名对应后端接口完成请求封装。
 */
export function updateMyProfile(payload: UpdateProfilePayload) {
  return client.put<never, UserProfile>('/users/me/profile', payload)
}

/**
 * fetchUserPosts：按函数名对应后端接口完成请求封装。
 */
export function fetchUserPosts(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/posts`, {
    params: { page, size },
  })
}

/**
 * fetchUserFavorites：按函数名对应后端接口完成请求封装。
 */
export function fetchUserFavorites(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/favorites`, {
    params: { page, size },
  })
}

/**
 * fetchUserLikes：按函数名对应后端接口完成请求封装。
 */
export function fetchUserLikes(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/likes`, {
    params: { page, size },
  })
}
