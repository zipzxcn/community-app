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

export function compactNumber(value?: number) {
  const count = value || 0
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  }
  return String(count)
}

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
