import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { PostListItem } from '@/types/post'
import type { UpdateProfilePayload, UserProfile, UserSummary } from '@/types/user'

export function fetchUserProfile(userId: number) {
  return client.get<never, UserProfile>(`/users/${userId}`)
}

export function searchUsers(keyword: string, page = 1, size = 20) {
  return client.get<never, PageResponse<UserSummary>>('/users/search', {
    params: { keyword, page, size },
  })
}

export function updateMyProfile(payload: UpdateProfilePayload) {
  return client.put<never, UserProfile>('/users/me/profile', payload)
}

export function fetchUserPosts(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/posts`, {
    params: { page, size },
  })
}

export function fetchUserFavorites(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/favorites`, {
    params: { page, size },
  })
}

export function fetchUserLikes(userId: number, page = 1, size = 10) {
  return client.get<never, PageResponse<PostListItem>>(`/users/${userId}/likes`, {
    params: { page, size },
  })
}
