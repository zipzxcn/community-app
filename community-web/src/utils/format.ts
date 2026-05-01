/**
 * 时间格式化：
 * - 面向中文界面输出月/日 时:分。
 * - 若后端返回异常时间字符串，则原样回显，避免直接显示 Invalid Date。
 */
export function formatDateTime(value?: string) {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * 大数字紧凑显示：例如 13200 -> 1.3万。
 * 解决首页卡片统计值过长挤压布局的问题。
 */
export function compactNumber(value?: number) {
  const count = value || 0
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  }
  return String(count)
}

/**
 * 资源地址解析：
 * - 兼容 http/data/blob 等完整地址直接返回。
 * - 兼容后端返回相对路径时，自动拼接当前站点或 API 基础地址。
 */
export function resolveAssetUrl(value?: string) {
  if (!value) {
    return ''
  }
  if (/^https?:\/\//i.test(value) || value.startsWith('data:') || value.startsWith('blob:')) {
    return value
  }
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'
  if (/^https?:\/\//i.test(apiBaseUrl)) {
    return new URL(value, apiBaseUrl).toString()
  }
  return new URL(value, window.location.origin).toString()
}
