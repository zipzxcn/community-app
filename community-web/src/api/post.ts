/**
 * 帖子接口封装：列表、详情、发布、编辑、删除、显隐、点赞与收藏。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { CreatePostPayload, PostDetail, PostListItem, PostQuery, Tag, UpdatePostPayload } from '@/types/post'

/**
 * fetchPosts：按函数名对应后端接口完成请求封装。
 */
export function fetchPosts(params: PostQuery) {
  return client.get<never, PageResponse<PostListItem>>('/posts', { params })
}

/**
 * fetchPostDetail：按函数名对应后端接口完成请求封装。
 */
export function fetchPostDetail(postId: number) {
  return client.get<never, PostDetail>(`/posts/${postId}`)
}

/**
 * createPost：按函数名对应后端接口完成请求封装。
 */
export function createPost(payload: CreatePostPayload) {
  return client.post<never, { postId: number }>('/posts', payload)
}

/**
 * updatePost：按函数名对应后端接口完成请求封装。
 */
export function updatePost(postId: number, payload: UpdatePostPayload) {
  return client.put<never, void>(`/posts/${postId}`, payload)
}

/**
 * deletePost：按函数名对应后端接口完成请求封装。
 */
export function deletePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}`)
}

/**
 * hidePost：按函数名对应后端接口完成请求封装。
 */
export function hidePost(postId: number, hidden: boolean) {
  return client.patch<never, void>(`/posts/${postId}/hide`, { hidden })
}

/**
 * fetchTags：按函数名对应后端接口完成请求封装。
 */
export function fetchTags() {
  return client.get<never, Tag[]>('/posts/tags')
}

/**
 * likePost：按函数名对应后端接口完成请求封装。
 */
export function likePost(postId: number) {
  return client.post<never, void>(`/posts/${postId}/like`)
}

/**
 * unlikePost：按函数名对应后端接口完成请求封装。
 */
export function unlikePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}/like`)
}

/**
 * favoritePost：按函数名对应后端接口完成请求封装。
 */
export function favoritePost(postId: number) {
  return client.post<never, void>(`/posts/${postId}/favorite`)
}

/**
 * unfavoritePost：按函数名对应后端接口完成请求封装。
 */
export function unfavoritePost(postId: number) {
  return client.delete<never, void>(`/posts/${postId}/favorite`)
}
