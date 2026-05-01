/**
 * 轻量 Markdown 渲染工具：
 * - 当前只支持标题、列表、图片、链接、粗体、斜体、行内代码等基础能力。
 * - 适合 MVP 阶段避免额外引入大型 Markdown 依赖。
 */
import { resolveAssetUrl } from '@/utils/format'

// 先做 HTML 转义，避免用户输入原始 HTML 时直接注入到页面。
function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function applyInlineMarkdown(value: string) {
  return value
    .replace(/!\[([^\]]*)\]\(([^)\s]+)\)/g, (_match, alt: string, url: string) => {
      const src = escapeHtml(resolveAssetUrl(url))
      return `<img src="${src}" alt="${alt}" />`
    })
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/(^|[^*])\*([^*]+)\*/g, '$1<em>$2</em>')
    .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, (_match, label: string, url: string) => {
      const href = escapeHtml(resolveAssetUrl(url))
      return `<a href="${href}" target="_blank" rel="noreferrer">${label}</a>`
    })
}

/**
 * 把 Markdown 文本渲染成 HTML 字符串，供 v-html 使用。
 */
export function renderMarkdown(value?: string) {
  const source = escapeHtml(value || '')
  if (!source.trim()) {
    return '<p>暂无内容</p>'
  }
  const lines = source.split('\n')
  const blocks: string[] = []
  let inList = false

  for (const rawLine of lines) {
    const line = rawLine.trimEnd()
    const trimmed = line.trim()
    if (!trimmed) {
      if (inList) {
        blocks.push('</ul>')
        inList = false
      }
      continue
    }
    const listMatch = /^[-*]\s+(.+)$/.exec(trimmed)
    if (listMatch) {
      if (!inList) {
        blocks.push('<ul>')
        inList = true
      }
      blocks.push(`<li>${applyInlineMarkdown(listMatch[1])}</li>`)
      continue
    }
    if (inList) {
      blocks.push('</ul>')
      inList = false
    }
    const headingMatch = /^(#{1,3})\s+(.+)$/.exec(trimmed)
    if (headingMatch) {
      const level = headingMatch[1].length
      blocks.push(`<h${level}>${applyInlineMarkdown(headingMatch[2])}</h${level}>`)
      continue
    }
    blocks.push(`<p>${applyInlineMarkdown(trimmed)}</p>`)
  }
  if (inList) {
    blocks.push('</ul>')
  }
  return blocks.join('')
}
