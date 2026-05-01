/**
 * 前后端通用协议类型：统一描述 ApiResponse、分页响应和分页查询参数。
 */
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
