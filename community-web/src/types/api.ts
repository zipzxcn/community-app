export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  requestId?: string
}

export interface PageResponse<T> {
  list: T[]
  page: number
  size: number
  total: number
  hasMore: boolean
}

export interface PageQuery {
  page?: number
  size?: number
}
