/**
 * 评论接口封装：负责评论树查询、评论、回复、删除与点赞。
 */
import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { CommentItem, CommentQuery, CreateCommentPayload, ReplyCommentPayload } from '@/types/comment'

/**
 * fetchPostComments：按函数名对应后端接口完成请求封装。
 */
export function fetchPostComments(postId: number, params: CommentQuery) {
  return client.get<never, PageResponse<CommentItem>>(`/posts/${postId}/comments`, { params })
}

/**
 * createComment：按函数名对应后端接口完成请求封装。
 */
export function createComment(postId: number, payload: CreateCommentPayload) {
  return client.post<never, CommentItem>(`/posts/${postId}/comments`, payload)
}

/**
 * replyComment：按函数名对应后端接口完成请求封装。
 */
export function replyComment(commentId: number, payload: ReplyCommentPayload) {
  return client.post<never, CommentItem>(`/comments/${commentId}/reply`, payload)
}

/**
 * deleteComment：按函数名对应后端接口完成请求封装。
 */
export function deleteComment(commentId: number) {
  return client.delete<never, void>(`/comments/${commentId}`)
}

/**
 * likeComment：按函数名对应后端接口完成请求封装。
 */
export function likeComment(commentId: number) {
  return client.post<never, void>(`/comments/${commentId}/like`)
}

/**
 * unlikeComment：按函数名对应后端接口完成请求封装。
 */
export function unlikeComment(commentId: number) {
  return client.delete<never, void>(`/comments/${commentId}/like`)
}
