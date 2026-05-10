/**
 * Markdown 渲染工具：
 * - 使用 markdown-it 覆盖 Typora 常见 Markdown：h1-h6、段落、嵌套列表、表格、引用、代码块、分割线、链接、图片。
 * - 关闭原始 HTML 渲染，避免用户正文通过 v-html 注入脚本或危险标签。
 */
import MarkdownIt from 'markdown-it'
import taskLists from 'markdown-it-task-lists'
import { resolveAssetUrl } from '@/utils/format'

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  breaks: false,
})
  .enable(['table', 'strikethrough'])
  .use(taskLists, { enabled: false, label: true, labelAfter: true })

const defaultLinkOpen = markdown.renderer.rules.link_open
markdown.renderer.rules.link_open = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  token.attrSet('target', '_blank')
  token.attrSet('rel', 'noreferrer noopener')
  return defaultLinkOpen ? defaultLinkOpen(tokens, idx, options, env, self) : self.renderToken(tokens, idx, options)
}

const defaultImage = markdown.renderer.rules.image
markdown.renderer.rules.image = (tokens, idx, options, env, self) => {
  const token = tokens[idx]
  const src = token.attrGet('src')
  if (src) {
    token.attrSet('src', resolveAssetUrl(src))
  }
  return defaultImage ? defaultImage(tokens, idx, options, env, self) : self.renderToken(tokens, idx, options)
}

/**
 * 把 Markdown 文本渲染成 HTML 字符串，供 v-html 使用。
 */
export function renderMarkdown(value?: string) {
  const source = value || ''
  if (!source.trim()) {
    return '<p>暂无内容</p>'
  }
  return markdown.render(source)
}
