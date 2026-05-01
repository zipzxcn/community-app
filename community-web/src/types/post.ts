import type { FileObject } from './file'
import type { PageQuery } from './api'
import type { UserSummary } from './user'

/**
 * 帖子模块类型：标签、帖子列表、帖子详情、发帖查询与载荷。
 */
export interface Tag {
  id: number
  name: string
}

export interface PostListItem {
  id: number
  title: string
  excerpt: string
  coverUrl?: string
  status: 'PUBLISHED' | 'HIDDEN'
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  publishedAt: string
  author?: UserSummary
  tags: Tag[]
  attachmentFiles: FileObject[]
  liked: boolean
  favorited: boolean
}

// 详情页比列表项多正文、附件 ID、评论开关等字段。
export interface PostDetail extends Omit<PostListItem, 'excerpt'> {
  contentMd: string
  contentHtml?: string
  allowComment: number
  attachmentFileIds: number[]
  attachmentFiles: FileObject[]
}

export interface PostQuery extends PageQuery {
  sort?: 'latest' | 'hot'
  tagId?: number
  authorId?: number
  keyword?: string
}

export interface CreatePostPayload {
  title: string
  contentMd: string
  coverUrl?: string
  allowComment?: boolean
  tagIds?: number[]
  attachmentFileIds?: number[]
}

export interface UpdatePostPayload extends CreatePostPayload {}
