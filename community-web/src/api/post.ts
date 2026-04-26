import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { CreatePostPayload, PostDetail, PostListItem, PostQuery, Tag, UpdatePostPayload } from '@/types/post'

export function fetchPosts(params: PostQuery) {
  return client.get<never, PageResponse<PostListItem>>('/posts', { params })
}

export function fetchPostDetail(postId: number) {
  return client.get<never, PostDetail>(`/posts/${postId}`)
}

export function createPost(payload: CreatePostPayload) {
  return client.post<never, { postId: number }>('/posts', payload)
}

export function updatePost(postId: number, payload: UpdatePostPayload) {
  return client.put<never, void>(`/posts/${postId}`, payload)
}

export function deletePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}`)
}

export function hidePost(postId: number, hidden: boolean) {
  return client.patch<never, void>(`/posts/${postId}/hide`, { hidden })
}

export function fetchTags() {
  return client.get<never, Tag[]>('/posts/tags')
}

export function likePost(postId: number) {
  return client.post<never, void>(`/posts/${postId}/like`)
}

export function unlikePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}/like`)
}

export function favoritePost(postId: number) {
  return client.post<never, void>(`/posts/${postId}/favorite`)
}

export function unfavoritePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}/favorite`)
}
