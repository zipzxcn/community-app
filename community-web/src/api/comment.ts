import client from '@/api/client'
import type { PageResponse } from '@/types/api'
import type { CommentItem, CommentQuery, CreateCommentPayload, ReplyCommentPayload } from '@/types/comment'

export function fetchPostComments(postId: number, params: CommentQuery) {
  return client.get<never, PageResponse<CommentItem>>(`/posts/${postId}/comments`, { params })
}

export function createComment(postId: number, payload: CreateCommentPayload) {
  return client.post<never, CommentItem>(`/posts/${postId}/comments`, payload)
}

export function replyComment(commentId: number, payload: ReplyCommentPayload) {
  return client.post<never, CommentItem>(`/comments/${commentId}/reply`, payload)
}

export function deleteComment(commentId: number) {
  return client.delete<never, void>(`/comments/${commentId}`)
}

export function likeComment(commentId: number) {
  return client.post<never, void>(`/comments/${commentId}/like`)
}

export function unlikeComment(commentId: number) {
  return client.delete<never, void>(`/comments/${commentId}/like`)
}
