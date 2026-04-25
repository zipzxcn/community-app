import type { PageQuery } from './api'
import type { UserSummary } from './user'

export interface CommentItem {
  id: number
  postId: number
  parentId: number
  rootId: number
  content: string
  likeCount: number
  replyCount: number
  status: string
  liked: boolean
  createdAt: string
  user?: UserSummary
  replyToUser?: UserSummary
  replies: CommentItem[]
}

export interface CommentQuery extends PageQuery {}

export interface CreateCommentPayload {
  content: string
}

export interface ReplyCommentPayload {
  parentId: number
  content: string
}
