import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { FollowUser } from '@/types/follow'

export function followUser(targetUserId: number) {
  return client.post<never, void>(`/follows/${targetUserId}`)
}

export function unfollowUser(targetUserId: number) {
  return client.delete<never, void>(`/follows/${targetUserId}`)
}

export function fetchFollowing(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/following', {
    params: { page, size },
  })
}

export function fetchFollowers(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/followers', {
    params: { page, size },
  })
}

export function fetchMutualFollows(page = 1, size = 20) {
  return client.get<never, PageResponse<FollowUser>>('/follows/mutual', {
    params: { page, size },
  })
}
