<template>
  <section class="post-publish app-page" :class="`post-publish--${workspaceMode}`">
    <div class="post-publish__header app-hero">
      <div class="post-publish__header-main">
        <p class="post-publish__eyebrow">Compose Workspace</p>
        <h1>{{ isEditMode ? '编辑帖子' : currentDraftId ? '从草稿继续发布' : '发布新帖子' }}</h1>
        <p class="post-publish__header-desc">
          当前工作台支持 Markdown 编辑、标签选择、封面上传、正文附件、草稿保存和实时预览<!--，适合作为一期内容生产主入口。-->
        </p>

        <div class="app-stat-grid">
          <article class="app-stat-card">
            <strong>{{ form.title.trim().length }}</strong>
            <span>标题字数</span>
            <small>建议标题清晰、聚焦主题</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ form.tagIds.length }}</strong>
            <span>当前标签数</span>
            <small>最多可选择 5 个标签</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ attachmentFiles.length }}</strong>
            <span>正文附件数</span>
            <small>上传后会自动插入正文图片语法</small>
          </article>
        </div>
      </div>

      <div class="post-publish__header-side">
        <div class="app-panel post-publish__workspace-card">
          <p class="post-publish__eyebrow">Workspace Status</p>
          <h3>当前创作状态</h3>
          <div class="post-publish__status-list">
            <span class="app-chip">{{ saveStatus }}</span>
            <span class="app-chip">{{ form.allowComment ? '允许评论' : '评论关闭' }}</span>
            <span class="app-chip">{{ isEditMode ? '编辑模式' : currentDraftId ? '草稿续写' : '新建模式' }}</span>
          </div>
          <div class="post-publish__header-actions">
            <RouterLink to="/drafts">
              <a-button long>草稿箱</a-button>
            </RouterLink>
            <RouterLink v-if="isEditMode && currentPostId" :to="`/posts/${currentPostId}`">
              <a-button long>查看帖子</a-button>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <div class="post-publish__mode-bar app-panel">
      <div>
        <p class="post-publish__eyebrow">Workspace View</p>
        <strong>写作视图</strong>
        <span>默认双栏写作，也可以切换为专注编辑或完整预览。</span>
      </div>
      <div class="post-publish__mode-actions">
        <a-button size="small" :type="workspaceMode === 'split' ? 'primary' : 'secondary'" @click="workspaceMode = 'split'">左右双栏</a-button>
        <a-button size="small" :type="workspaceMode === 'edit' ? 'primary' : 'secondary'" @click="workspaceMode = 'edit'">专注编辑</a-button>
        <a-button size="small" :type="workspaceMode === 'preview' ? 'primary' : 'secondary'" @click="workspaceMode = 'preview'">全屏预览</a-button>
      </div>
    </div>

    <div class="post-publish__grid" :class="`post-publish__grid--${workspaceMode}`">
      <a-card v-if="showEditor" class="post-publish__editor app-panel" :bordered="false">
        <div class="app-section-head">
          <div class="app-section-head__main">
            <p class="app-section-head__eyebrow">Editor</p>
            <h2 class="app-section-head__title">内容编辑区</h2>
            <p class="app-section-head__desc">在这里完成标题、标签、封面、正文和附件管理。</p>
          </div>
        </div>

        <a-form :model="form" layout="vertical" class="post-publish__form">
          <a-form-item field="title" label="标题">
            <a-input v-model="form.title" placeholder="写一个清楚的标题" :max-length="120" show-word-limit />
          </a-form-item>

          <div class="post-publish__row">
            <a-form-item field="tags" label="标签" class="post-publish__field">
              <a-select
                v-model="form.tagIds"
                multiple
                allow-search
                allow-clear
                placeholder="选择最多 5 个标签"
                :max-tag-count="4"
              >
                <a-option v-for="tag in availableTags" :key="tag.id" :value="tag.id">{{ tag.name }}</a-option>
              </a-select>
            </a-form-item>

            <a-form-item field="allowComment" label="允许评论" class="post-publish__field post-publish__switch-field">
              <a-switch v-model="form.allowComment" />
            </a-form-item>
          </div>

          <div class="post-publish__media-grid">
            <section class="post-publish__media-card">
              <div class="post-publish__media-head">
                <div>
                  <p class="post-publish__eyebrow">Cover</p>
                  <h3>封面图</h3>
                </div>
              </div>
              <div class="post-publish__cover-field">
                <a-input v-model="form.coverUrl" placeholder="可手填 URL，或直接上传封面图" allow-clear />
                <label class="post-publish__upload-trigger">
                  <input type="file" accept="image/*" @change="uploadCover" />
                  <span>{{ uploadingCover ? '上传中' : '上传封面' }}</span>
                </label>
              </div>
              <img v-if="form.coverUrl" :src="resolveAssetUrl(form.coverUrl)" alt="" class="post-publish__cover-preview" />
              <div v-else class="post-publish__cover-placeholder">封面预览区</div>
            </section>

          </div>

          <a-form-item field="contentMd" label="正文 Markdown">
            <div class="post-publish__markdown-workbench">
              <aside class="post-publish__toolbar-panel">
                <p class="post-publish__toolbar-title">格式工具</p>
                <div class="post-publish__toolbar app-toolbar">
                  <a-button v-for="level in 6" :key="level" size="small" @click="applyHeading(level)">H{{ level }}</a-button>
                  <a-button size="small" @click="applyInlineFormat('**', '**', '加粗文本')">加粗</a-button>
                  <a-button size="small" @click="applyInlineFormat('*', '*', '斜体文本')">斜体</a-button>
                  <a-button size="small" @click="applyUnorderedList">无序列表</a-button>
                  <a-button size="small" @click="applyOrderedList">有序列表</a-button>
                  <a-button size="small" @click="applyTaskList">任务列表</a-button>
                  <a-button size="small" @click="applyCodeFormat">代码</a-button>
                  <a-button size="small" @click="applyBlockquote">引用</a-button>
                  <a-button size="small" @click="insertTableTemplate">表格</a-button>
                  <a-button size="small" @click="insertHorizontalRule">分割线</a-button>
                  <a-button size="small" @click="insertLinkTemplate">链接</a-button>
                </div>
                <div class="post-publish__floating-attachments">
                  <p class="post-publish__toolbar-title">正文图片</p>
                  <label class="post-publish__upload-trigger post-publish__upload-trigger--block">
                    <input type="file" accept="image/*" multiple @change="uploadAttachments" />
                    <span>{{ uploadingAttachments ? '上传中' : '上传图片' }}</span>
                  </label>
                  <span class="post-publish__hint">上传后插入当前光标处。</span>
                  <div v-if="attachmentFiles.length" class="post-publish__attachment-list post-publish__attachment-list--compact">
                    <article v-for="file in attachmentFiles" :key="file.id" class="post-publish__attachment-item post-publish__attachment-item--compact">
                      <img :src="resolveAssetUrl(file.accessUrl)" alt="" />
                      <div class="post-publish__attachment-copy">
                        <strong>{{ file.originalName }}</strong>
                        <p>{{ formatFileSize(file.sizeBytes) }}</p>
                      </div>
                      <a-button size="mini" status="danger" @click="removeAttachment(file.id)">移除</a-button>
                    </article>
                  </div>
                  <div v-else class="post-publish__attachment-empty">暂无正文图片。</div>
                </div>
              </aside>
              <a-textarea
                ref="editorRef"
                class="post-publish__textarea"
                v-model="form.contentMd"
                placeholder="支持完整 Markdown：标题、段落、列表、任务列表、表格、引用、代码块、图片和链接。"
                :auto-size="{ minRows: workspaceMode === 'edit' ? 34 : 28, maxRows: 56 }"
                @click="rememberEditorSelection"
                @keyup="rememberEditorSelection"
                @mouseup="rememberEditorSelection"
                @select="rememberEditorSelection"
              />
            </div>
          </a-form-item>
        </a-form>

        <div class="post-publish__actions">
          <span class="post-publish__save-text">{{ saveStatus }}</span>
          <div class="post-publish__action-buttons">
            <a-button v-if="!isEditMode" :loading="saving" @click="saveDraftManually">保存草稿</a-button>
            <a-button type="primary" :loading="publishing" @click="submitPost">
              {{ isEditMode ? '保存修改' : '发布帖子' }}
            </a-button>
          </div>
        </div>
      </a-card>

      <a-card v-if="showPreview" class="post-publish__preview app-panel" :bordered="false">
        <div class="app-section-head">
          <div class="app-section-head__main">
            <p class="app-section-head__eyebrow">Preview</p>
            <h2 class="app-section-head__title">实时预览</h2>
            <p class="app-section-head__desc">在发布前预览封面、标签和正文排版效果。</p>
          </div>
        </div>

        <div class="post-publish__preview-body">
          <img v-if="form.coverUrl" :src="resolveAssetUrl(form.coverUrl)" alt="" class="post-publish__cover" />
          <div v-else class="post-publish__cover post-publish__cover--placeholder">封面预览</div>
          <h2>{{ form.title || '未命名帖子' }}</h2>
          <div v-if="previewTags.length" class="post-publish__preview-tags">
            <a-tag v-for="tag in previewTags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
          </div>
          <div class="markdown-body post-publish__preview-content" v-html="previewHtml"></div>
        </div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * 发帖页：同时承载新建帖子、编辑帖子、草稿自动保存等复杂创作流程。
 */
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { createDraft, deleteDraft, fetchDraftDetail, updateDraft } from '@/api/draft'
import { deleteFile, uploadImageFile } from '@/api/file'
import { createPost, fetchPostDetail, fetchTags, updatePost } from '@/api/post'
import type { FileObject } from '@/types/file'
import type { Tag } from '@/types/post'
import { resolveAssetUrl } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  postId?: number
}>()

const route = useRoute()
const router = useRouter()
const editorRef = ref()
const currentDraftId = ref<number | null>(null)
const currentPostId = ref<number | null>(props.postId ?? null)
const saving = ref(false)
const publishing = ref(false)
const loading = ref(false)
const uploadingCover = ref(false)
const uploadingAttachments = ref(false)
const saveStatus = ref('尚未保存')
const availableTags = ref<Tag[]>([])
const attachmentFiles = ref<FileObject[]>([])
type WorkspaceMode = 'split' | 'edit' | 'preview'
const workspaceMode = ref<WorkspaceMode>('split')
// 教学点：表单状态集中放在一个 reactive 对象里，方便草稿保存、发布提交、预览渲染共用同一份数据源。
const form = reactive({
  title: '',
  coverUrl: '',
  contentMd: '',
  allowComment: true,
  tagIds: [] as number[],
})
const lastEditorSelection = ref({ start: 0, end: 0 })
let autosaveTimer: number | undefined

const isEditMode = computed(() => Boolean(props.postId))
const showEditor = computed(() => workspaceMode.value !== 'preview')
const showPreview = computed(() => workspaceMode.value !== 'edit')
// 教学点：预览区完全基于 form.contentMd 计算得到，做到“编辑区即数据源，预览区只是派生视图”。
const previewHtml = computed(() => renderMarkdown(form.contentMd))
const previewTags = computed(() => availableTags.value.filter((tag) => form.tagIds.includes(tag.id)))

function getDraftIdFromQuery() {
  const raw = route.query.draftId
  const value = Array.isArray(raw) ? raw[0] : raw
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
}

function validateForDraft() {
  return Boolean(form.title.trim())
}

function validateForPublish() {
  if (!form.title.trim()) {
    Message.warning('请填写标题')
    return false
  }
  if (!form.contentMd.trim()) {
    Message.warning('请填写正文')
    return false
  }
  if (form.tagIds.length > 5) {
    Message.warning('最多选择 5 个标签')
    return false
  }
  return true
}

function buildPostPayload() {
  return {
    title: form.title.trim(),
    contentMd: form.contentMd,
    coverUrl: form.coverUrl.trim() || undefined,
    allowComment: form.allowComment,
    tagIds: form.tagIds,
    attachmentFileIds: attachmentFiles.value.map((file) => file.id),
  }
}

// load* 系列方法负责从后端拉取页面初始化数据，统一控制 loading 和错误提示。
// 发帖页先加载标签，为分类选择和内容组织提供基础数据。
// 教学点：标签属于发帖辅助数据，页面初始化先加载，后续无需重复请求。
async function loadTags() {
  availableTags.value = await fetchTags()
}

async function loadDraft(draftId: number) {
  loading.value = true
  try {
    const draft = await fetchDraftDetail(draftId)
    currentDraftId.value = draft.id
    form.title = draft.title
    form.coverUrl = draft.coverUrl || ''
    form.contentMd = draft.contentMd || ''
    form.tagIds = draft.tagIds || draft.tags?.map((tag) => tag.id) || []
    attachmentFiles.value = draft.attachmentFiles || []
    saveStatus.value = `已加载草稿 #${draft.id}`
  } finally {
    loading.value = false
  }
}

async function loadPost(postId: number) {
  loading.value = true
  try {
    const post = await fetchPostDetail(postId)
    currentPostId.value = post.id
    form.title = post.title
    form.coverUrl = post.coverUrl || ''
    form.contentMd = post.contentMd || ''
    form.allowComment = Boolean(post.allowComment)
    form.tagIds = post.tags.map((tag) => tag.id)
    attachmentFiles.value = post.attachmentFiles || []
    saveStatus.value = `已加载帖子 #${post.id}`
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '帖子加载失败')
  } finally {
    loading.value = false
  }
}

async function saveDraft(autoSave = false) {
  if (loading.value || isEditMode.value || !validateForDraft()) return
  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      coverUrl: form.coverUrl.trim() || undefined,
      contentMd: form.contentMd,
      tagIds: form.tagIds,
      attachmentFileIds: attachmentFiles.value.map((file) => file.id),
      autoSave,
    }
    if (currentDraftId.value) {
      await updateDraft(currentDraftId.value, payload)
    } else {
      const result = await createDraft(payload)
      currentDraftId.value = result.draftId
    }
    saveStatus.value = autoSave ? '已自动保存草稿' : '草稿已保存'
    if (!autoSave) Message.success('草稿已保存')
  } catch (error) {
    saveStatus.value = '草稿保存失败'
    if (!autoSave) Message.error(error instanceof Error ? error.message : '草稿保存失败')
  } finally {
    saving.value = false
  }
}

function saveDraftManually() {
  saveDraft(false)
}

// 教学点：封面上传成功后只回填 coverUrl，真正落库仍要等发布或保存草稿时绑定到业务对象。
async function uploadCover(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  uploadingCover.value = true
  try {
    const uploaded = await uploadImageFile(file, 'POST')
    form.coverUrl = uploaded.accessUrl
    Message.success('封面已上传')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '封面上传失败')
  } finally {
    uploadingCover.value = false
  }
}

// 教学点：正文图片上传后不仅保存附件列表，还会自动插入 Markdown 图片语法，提高编辑效率。
async function uploadAttachments(event: Event) {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files || [])
  input.value = ''
  if (!files.length) return
  uploadingAttachments.value = true
  try {
    for (const file of files) {
      const uploaded = await uploadImageFile(file, 'POST')
      attachmentFiles.value = [...attachmentFiles.value, uploaded]
      appendAttachmentMarkdown(uploaded.accessUrl, uploaded.originalName)
    }
    Message.success('图片已上传')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '图片上传失败')
  } finally {
    uploadingAttachments.value = false
  }
}

async function removeAttachment(fileId: number) {
  const target = attachmentFiles.value.find((file) => file.id === fileId)
  attachmentFiles.value = attachmentFiles.value.filter((file) => file.id !== fileId)
  if (target) {
    removeAttachmentMarkdown(target.accessUrl, target.originalName)
    await deleteFile(fileId).catch(() => undefined)
  }
}

function appendAttachmentMarkdown(url: string, name: string) {
  const imageMarkdown = `![${escapeMarkdownText(name)}](${url})`
  insertBlockAtCursor(imageMarkdown)
}

function removeAttachmentMarkdown(url: string, name: string) {
  const escapedUrl = url.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const escapedName = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  form.contentMd = form.contentMd
    .replace(new RegExp(`\\n?!\\[${escapedName}\\]\\(${escapedUrl}\\)\\n?`, 'g'), '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function getEditorTextarea() {
  return editorRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement | undefined
}

function clampSelection(value: number) {
  return Math.max(0, Math.min(value, form.contentMd.length))
}

function rememberEditorSelection() {
  const textarea = getEditorTextarea()
  if (!textarea) return
  lastEditorSelection.value = {
    start: clampSelection(textarea.selectionStart),
    end: clampSelection(textarea.selectionEnd),
  }
}

function getEditorSelection() {
  const textarea = getEditorTextarea()
  if (textarea) {
    return {
      start: clampSelection(textarea.selectionStart),
      end: clampSelection(textarea.selectionEnd),
    }
  }
  return {
    start: clampSelection(lastEditorSelection.value.start),
    end: clampSelection(lastEditorSelection.value.end),
  }
}

function replaceEditorSelection(text: string, cursorStartOffset = text.length, cursorEndOffset = cursorStartOffset) {
  const { start, end } = getEditorSelection()
  form.contentMd = `${form.contentMd.slice(0, start)}${text}${form.contentMd.slice(end)}`
  const nextStart = start + cursorStartOffset
  const nextEnd = start + cursorEndOffset
  lastEditorSelection.value = { start: nextStart, end: nextEnd }
  nextTick(() => {
    const textarea = getEditorTextarea()
    if (!textarea) return
    textarea.focus()
    textarea.setSelectionRange(nextStart, nextEnd)
  })
}

function insertBlockAtCursor(block: string) {
  const { start, end } = getEditorSelection()
  const before = form.contentMd.slice(0, start)
  const after = form.contentMd.slice(end)
  const prefix = before && !before.endsWith('\n\n') ? before.endsWith('\n') ? '\n' : '\n\n' : ''
  const suffix = after && !after.startsWith('\n\n') ? after.startsWith('\n') ? '\n' : '\n\n' : ''
  const text = `${prefix}${block}${suffix}`
  replaceEditorSelection(text)
}

function escapeMarkdownText(value: string) {
  return value.replace(/([\\[\]])/g, '\\$1')
}

function selectedTextOrFallback(fallback: string) {
  const { start, end } = getEditorSelection()
  return form.contentMd.slice(start, end) || fallback
}

function applyInlineFormat(prefix: string, suffix: string, fallback: string) {
  const selected = selectedTextOrFallback(fallback)
  if (selected.includes('\n')) {
    const formatted = selected
      .split('\n')
      .map((line) => (line.trim() ? `${prefix}${line}${suffix}` : line))
      .join('\n')
    replaceEditorSelection(formatted, 0, formatted.length)
    return
  }
  const text = `${prefix}${selected}${suffix}`
  replaceEditorSelection(text, prefix.length, prefix.length + selected.length)
}

function getSelectedLineRange() {
  const { start, end } = getEditorSelection()
  const lineStart = form.contentMd.lastIndexOf('\n', Math.max(0, start - 1)) + 1
  const nextBreak = form.contentMd.indexOf('\n', end)
  const lineEnd = nextBreak === -1 ? form.contentMd.length : nextBreak
  return {
    start: lineStart,
    end: lineEnd,
    text: form.contentMd.slice(lineStart, lineEnd),
  }
}

function replaceLineRange(text: string) {
  const range = getSelectedLineRange()
  form.contentMd = `${form.contentMd.slice(0, range.start)}${text}${form.contentMd.slice(range.end)}`
  lastEditorSelection.value = { start: range.start, end: range.start + text.length }
  nextTick(() => {
    const textarea = getEditorTextarea()
    if (!textarea) return
    textarea.focus()
    textarea.setSelectionRange(range.start, range.start + text.length)
  })
}

function transformSelectedLines(transform: (line: string, index: number) => string) {
  const range = getSelectedLineRange()
  const lines = range.text ? range.text.split('\n') : ['']
  replaceLineRange(lines.map(transform).join('\n'))
}

function stripBlockPrefix(line: string) {
  return line
    .replace(/^\s{0,3}#{1,6}\s+/, '')
    .replace(/^\s{0,3}>\s?/, '')
    .replace(/^\s{0,3}[-*+]\s+/, '')
    .replace(/^\s{0,3}\d+\.\s+/, '')
    .replace(/^\s{0,3}[-*+]\s+\[[ xX]\]\s+/, '')
}

function applyHeading(level: number) {
  const prefix = `${'#'.repeat(level)} `
  transformSelectedLines((line) => (line.trim() ? `${prefix}${stripBlockPrefix(line)}` : line))
}

function applyUnorderedList() {
  transformSelectedLines((line) => (line.trim() ? `- ${stripBlockPrefix(line)}` : line))
}

function applyOrderedList() {
  let order = 1
  transformSelectedLines((line) => (line.trim() ? `${order++}. ${stripBlockPrefix(line)}` : line))
}

function applyTaskList() {
  transformSelectedLines((line) => (line.trim() ? `- [ ] ${stripBlockPrefix(line)}` : line))
}

function applyBlockquote() {
  transformSelectedLines((line) => (line.trim() ? `> ${line.replace(/^\s{0,3}>\s?/, '')}` : line))
}

function applyCodeFormat() {
  const selected = selectedTextOrFallback('code')
  if (selected.includes('\n')) {
    const text = `\`\`\`txt\n${selected.replace(/^```.*\n?|\n?```$/g, '')}\n\`\`\``
    replaceEditorSelection(text, 7, 7 + selected.length)
    return
  }
  const text = `\`${selected}\``
  replaceEditorSelection(text, 1, 1 + selected.length)
}

function insertTableTemplate() {
  const table = '| 列 1 | 列 2 | 列 3 |\n| --- | --- | --- |\n| 内容 | 内容 | 内容 |'
  insertBlockAtCursor(table)
}

function insertHorizontalRule() {
  insertBlockAtCursor('---')
}

function insertLinkTemplate() {
  const selected = selectedTextOrFallback('链接文字')
  const text = `[${selected}](https://example.com)`
  replaceEditorSelection(text, 1, 1 + selected.length)
}

// 提交时根据是否存在 postId 区分“新建帖子”和“编辑帖子”两条业务路径。
// 教学点：提交发布时，先做标题/正文等前端校验，再根据模式决定调用 create 还是 update。
async function submitPost() {
  if (!validateForPublish()) return
  publishing.value = true
  try {
    if (isEditMode.value && currentPostId.value) {
      await updatePost(currentPostId.value, buildPostPayload())
      Message.success('帖子已更新')
      await router.push(`/posts/${currentPostId.value}`)
      return
    }
    const result = await createPost(buildPostPayload())
    if (currentDraftId.value) {
      await deleteDraft(currentDraftId.value).catch(() => undefined)
    }
    Message.success('帖子已发布')
    await router.push(`/posts/${result.postId}`)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '提交失败')
  } finally {
    publishing.value = false
  }
}

function scheduleAutoSave() {
  if (isEditMode.value) return
  window.clearTimeout(autosaveTimer)
  if (!validateForDraft()) {
    saveStatus.value = '填写标题后自动保存草稿'
    return
  }
  saveStatus.value = '等待自动保存...'
  autosaveTimer = window.setTimeout(() => {
    saveDraft(true)
  }, 1200)
}

function formatFileSize(value?: number) {
  if (!value) return '0 B'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / 1024 / 1024).toFixed(1)} MB`
}

// 监听路由参数或局部状态变化，保证页面在切换对象后自动刷新。
// 教学点：监听内容变化做自动保存时，要配合节流/定时器，避免每敲一个字都打接口。
watch(
  () => [form.title, form.coverUrl, form.contentMd, form.tagIds.join(','), attachmentFiles.value.map((file) => file.id).join(',')],
  scheduleAutoSave,
)

onMounted(async () => {
  try {
    await loadTags()
    if (props.postId) {
      await loadPost(props.postId)
      return
    }
    const draftId = getDraftIdFromQuery()
    if (draftId) {
      await loadDraft(draftId)
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '编辑器初始化失败')
  }
})

onBeforeUnmount(() => {
  window.clearTimeout(autosaveTimer)
})
</script>

<style scoped lang="scss">
.post-publish__header {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(280px, 0.85fr);
  gap: 24px;
}

.post-publish {
  width: min(100%, 1680px);
  margin: 0 auto;
}

.post-publish__header-main {
  display: grid;
  gap: 18px;
}

.post-publish__eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.post-publish__header h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(32px, 5vw, 48px);
  line-height: 1.08;
}

.post-publish__header-desc {
  max-width: 720px;
  margin: 0;
  color: var(--app-text-3);
  line-height: 1.85;
}

.post-publish__workspace-card {
  display: grid;
  gap: 14px;
  align-content: start;
}

.post-publish__workspace-card h3 {
  margin: 0;
  color: var(--app-text-1);
  font-size: 22px;
}

.post-publish__status-list,
.post-publish__header-actions {
  display: grid;
  gap: 10px;
}

.post-publish__grid {
  display: grid;
  grid-template-columns: minmax(0, 3fr) minmax(460px, 2fr);
  gap: 22px;
  align-items: start;
}

.post-publish__grid--edit,
.post-publish__grid--preview {
  grid-template-columns: minmax(0, 1fr);
}

.post-publish--edit,
.post-publish--preview {
  max-width: 1320px;
}

.post-publish__editor :deep(.arco-card-body),
.post-publish__preview :deep(.arco-card-body) {
  padding: 24px;
}

.post-publish__form {
  display: grid;
  gap: 8px;
}

.post-publish__row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 180px;
  gap: 12px;
}

.post-publish__field {
  min-width: 0;
}

.post-publish__switch-field {
  align-self: end;
}

.post-publish__media-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
  margin: 4px 0 8px;
}

.post-publish__media-card {
  display: grid;
  gap: 12px;
  padding: 16px;
  background: rgba(248, 250, 252, 0.86);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
}

.post-publish__media-head h3,
.post-publish__media-head p {
  margin: 0;
}

.post-publish__media-head h3 {
  margin-top: 6px;
  color: var(--app-text-1);
  font-size: 18px;
}

.post-publish__cover-field,
.post-publish__attachment-tools {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
}

.post-publish__upload-trigger {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 96px;
  height: 36px;
  color: var(--app-primary);
  cursor: pointer;
  background: rgba(236, 253, 245, 0.95);
  border: 1px solid rgba(15, 118, 110, 0.24);
  border-radius: 10px;
}

.post-publish__upload-trigger input {
  position: absolute;
  inset: 0;
  width: 100%;
  opacity: 0;
}

.post-publish__upload-trigger--block {
  width: 100%;
  min-width: 0;
}

.post-publish__cover-preview,
.post-publish__cover {
  width: 100%;
  min-height: 220px;
  max-height: 280px;
  object-fit: cover;
  border-radius: 14px;
}

.post-publish__cover-placeholder,
.post-publish__cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--app-text-4);
  background: linear-gradient(135deg, rgba(236, 253, 245, 0.9), rgba(219, 234, 254, 0.9));
  border: 1px dashed var(--app-border-color-strong);
}

.post-publish__attachment-list {
  display: grid;
  gap: 10px;
}

.post-publish__attachment-list--compact {
  gap: 8px;
  margin-top: 10px;
}

.post-publish__attachment-item {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--app-border-color-soft);
  border-radius: 10px;
}

.post-publish__attachment-item img {
  width: 72px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
}

.post-publish__attachment-item--compact {
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 8px;
  padding: 8px;
}

.post-publish__attachment-item--compact img {
  width: 42px;
  height: 34px;
}

.post-publish__attachment-item--compact .arco-btn {
  grid-column: 1 / -1;
  justify-self: stretch;
}

.post-publish__attachment-item--compact .post-publish__attachment-copy {
  min-width: 0;
}

.post-publish__attachment-item--compact .post-publish__attachment-copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-publish__attachment-copy strong,
.post-publish__attachment-copy p {
  display: block;
  margin: 0;
}

.post-publish__attachment-copy p,
.post-publish__hint,
.post-publish__attachment-empty {
  color: var(--app-text-3);
  font-size: 13px;
  line-height: 1.7;
}

.post-publish__markdown-workbench {
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr);
  gap: 14px;
  width: 100%;
  align-items: start;
}

.post-publish__toolbar-panel {
  position: sticky;
  top: clamp(112px, 22vh, 220px);
  z-index: 6;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  max-height: calc(100vh - 150px);
  padding: 12px;
  overflow-y: auto;
  overscroll-behavior: contain;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--app-border-color-soft);
  border-radius: 14px;
  box-shadow: 0 18px 42px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(14px);
}

.post-publish__toolbar-title {
  margin: 0 0 10px;
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 700;
}

.post-publish__toolbar {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.post-publish__floating-attachments {
  display: grid;
  gap: 8px;
  padding-top: 14px;
  margin-top: 14px;
  border-top: 1px solid var(--app-border-color-soft);
}

.post-publish__toolbar .arco-btn {
  min-width: 0;
  padding-inline: 8px;
}

.post-publish--edit .post-publish__markdown-workbench {
  grid-template-columns: 190px minmax(0, 1fr);
}

.post-publish__textarea :deep(textarea) {
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.8;
}

.post-publish__textarea :deep(.arco-textarea-wrapper) {
  height: 100%;
}

.post-publish__mode-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
}

.post-publish__mode-bar strong,
.post-publish__mode-bar span {
  display: block;
}

.post-publish__mode-bar strong {
  margin-top: 4px;
  color: var(--app-text-1);
  font-size: 18px;
}

.post-publish__mode-bar span {
  margin-top: 4px;
  color: var(--app-text-3);
  font-size: 13px;
}

.post-publish__mode-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.post-publish__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 18px;
}

.post-publish__save-text {
  color: var(--app-text-3);
  font-size: 13px;
}

.post-publish__action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.post-publish__preview {
  position: sticky;
  top: 120px;
  min-width: 0;
  max-height: calc(100vh - 148px);
  overflow: auto;
}

.post-publish__grid--preview .post-publish__preview {
  position: static;
  max-height: none;
}

.post-publish__preview-body {
  display: grid;
  gap: 14px;
  min-width: 0;
}

.post-publish__editor :deep(.app-section-head),
.post-publish__preview :deep(.app-section-head) {
  margin-bottom: 18px;
}

.post-publish__preview h2 {
  margin: 0;
  color: var(--app-text-1);
  font-size: 28px;
  overflow-wrap: anywhere;
}

.post-publish__preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.post-publish__preview-content {
  min-width: 0;
}


@media (max-width: 1100px) {
  .post-publish__header,
  .post-publish__grid,
  .post-publish__media-grid {
    grid-template-columns: 1fr;
  }

  .post-publish__preview {
    position: static;
    top: auto;
  }
}

@media (max-width: 720px) {
  .post-publish__row,
  .post-publish__cover-field,
  .post-publish__attachment-tools,
  .post-publish__markdown-workbench,
  .post-publish__attachment-item,
  .post-publish__actions {
    grid-template-columns: 1fr;
  }

  .post-publish__toolbar-panel {
    justify-content: flex-start;
  }

  .post-publish__toolbar {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .post-publish__header h1 {
    font-size: clamp(28px, 8vw, 36px);
  }

  .post-publish__header-desc {
    line-height: 1.75;
  }

  .post-publish__actions {
    display: grid;
  }

  .post-publish__action-buttons {
    justify-content: flex-start;
  }

  .post-publish__action-buttons .arco-btn,
  .post-publish__header-actions .arco-btn {
    min-height: 38px;
  }
}
</style>
