<template>
  <section class="post-publish">
    <div class="post-publish__header">
      <div>
        <p class="post-publish__eyebrow">Compose</p>
        <h1>{{ isEditMode ? '编辑帖子' : currentDraftId ? '从草稿继续发布' : '发布新帖子' }}</h1>
        <p>支持 Markdown 编辑、标签选择、封面上传、多图附件、草稿保存与发布。</p>
      </div>
      <div class="post-publish__header-actions">
        <RouterLink to="/drafts">
          <a-button>草稿箱</a-button>
        </RouterLink>
        <RouterLink v-if="isEditMode && currentPostId" :to="`/posts/${currentPostId}`">
          <a-button>查看帖子</a-button>
        </RouterLink>
      </div>
    </div>

    <div class="post-publish__grid">
      <a-card class="post-publish__editor" :bordered="false">
        <a-form :model="form" layout="vertical">
          <a-form-item field="title" label="标题">
            <a-input v-model="form.title" placeholder="写一个清楚的标题" :max-length="120" show-word-limit />
          </a-form-item>

          <a-form-item field="tags" label="标签">
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

          <a-form-item field="coverUrl" label="封面">
            <div class="post-publish__cover-field">
              <a-input v-model="form.coverUrl" placeholder="可手填 URL，或直接上传封面图" allow-clear />
              <label class="post-publish__upload-trigger">
                <input type="file" accept="image/*" @change="uploadCover" />
                <span>{{ uploadingCover ? '上传中' : '上传封面' }}</span>
              </label>
            </div>
            <img v-if="form.coverUrl" :src="resolveAssetUrl(form.coverUrl)" alt="" class="post-publish__cover-preview" />
          </a-form-item>

          <a-form-item field="attachments" label="正文图片">
            <div class="post-publish__attachment-tools">
              <label class="post-publish__upload-trigger">
                <input type="file" accept="image/*" multiple @change="uploadAttachments" />
                <span>{{ uploadingAttachments ? '上传中' : '上传图片' }}</span>
              </label>
              <span class="post-publish__hint">上传后会自动插入正文；移除附件时会同步移除自动插入的图片语法。</span>
            </div>
            <div v-if="attachmentFiles.length" class="post-publish__attachment-list">
              <article v-for="file in attachmentFiles" :key="file.id" class="post-publish__attachment-item">
                <img :src="resolveAssetUrl(file.accessUrl)" alt="" />
                <div>
                  <strong>{{ file.originalName }}</strong>
                  <p>{{ formatFileSize(file.sizeBytes) }}</p>
                </div>
                <a-button size="mini" status="danger" @click="removeAttachment(file.id)">移除</a-button>
              </article>
            </div>
          </a-form-item>

          <a-form-item field="contentMd" label="正文 Markdown">
            <div class="post-publish__toolbar">
              <a-button size="small" @click="wrapSelection('**', '**')">加粗</a-button>
              <a-button size="small" @click="wrapSelection('*', '*')">斜体</a-button>
              <a-button size="small" @click="wrapSelection('## ', '')">标题</a-button>
              <a-button size="small" @click="wrapSelection('- ', '')">列表</a-button>
              <a-button size="small" @click="wrapSelection('`', '`')">代码</a-button>
            </div>
            <a-textarea
              ref="editorRef"
              v-model="form.contentMd"
              placeholder="支持基础 Markdown。"
              :auto-size="{ minRows: 18, maxRows: 30 }"
            />
          </a-form-item>

          <a-form-item field="allowComment" label="允许评论">
            <a-switch v-model="form.allowComment" />
          </a-form-item>
        </a-form>

        <div class="post-publish__actions">
          <span>{{ saveStatus }}</span>
          <a-button v-if="!isEditMode" :loading="saving" @click="saveDraftManually">保存草稿</a-button>
          <a-button type="primary" :loading="publishing" @click="submitPost">
            {{ isEditMode ? '保存修改' : '发布帖子' }}
          </a-button>
        </div>
      </a-card>

      <a-card class="post-publish__preview" :bordered="false">
        <template #title>预览</template>
        <img v-if="form.coverUrl" :src="resolveAssetUrl(form.coverUrl)" alt="" class="post-publish__cover" />
        <h2>{{ form.title || '未命名帖子' }}</h2>
        <div v-if="previewTags.length" class="post-publish__preview-tags">
          <a-tag v-for="tag in previewTags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
        </div>
        <div class="markdown-body" v-html="previewHtml"></div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
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
const form = reactive({
  title: '',
  coverUrl: '',
  contentMd: '',
  allowComment: true,
  tagIds: [] as number[],
})
let autosaveTimer: number | undefined

const isEditMode = computed(() => Boolean(props.postId))
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
  if (loading.value || isEditMode.value || !validateForDraft()) {
    return
  }
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
    if (!autoSave) {
      Message.success('草稿已保存')
    }
  } catch (error) {
    saveStatus.value = '草稿保存失败'
    if (!autoSave) {
      Message.error(error instanceof Error ? error.message : '草稿保存失败')
    }
  } finally {
    saving.value = false
  }
}

function saveDraftManually() {
  saveDraft(false)
}

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
  const block = `\n![${name}](${url})\n`
  form.contentMd = `${form.contentMd}${block}`.trimStart()
}

function removeAttachmentMarkdown(url: string, name: string) {
  const escapedUrl = url.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const escapedName = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  form.contentMd = form.contentMd
    .replace(new RegExp(`\\n?!\\[${escapedName}\\]\\(${escapedUrl}\\)\\n?`, 'g'), '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function wrapSelection(prefix: string, suffix: string) {
  const textarea = editorRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement | undefined
  if (!textarea) {
    form.contentMd += `${prefix}${suffix}`
    return
  }
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selected = form.contentMd.slice(start, end)
  form.contentMd = `${form.contentMd.slice(0, start)}${prefix}${selected}${suffix}${form.contentMd.slice(end)}`
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + prefix.length, start + prefix.length + selected.length)
  })
}

async function submitPost() {
  if (!validateForPublish()) {
    return
  }
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
  if (isEditMode.value) {
    return
  }
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
.post-publish {
  display: grid;
  gap: 22px;
}

.post-publish__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  background:
    linear-gradient(135deg, rgba(14, 165, 233, 0.12), rgba(245, 158, 11, 0.16)),
    rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
}

.post-publish__header-actions {
  display: flex;
  gap: 10px;
}

.post-publish__eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  font-weight: 800;
}

.post-publish__header h1 {
  margin: 0 0 10px;
  color: #172033;
  font-size: clamp(30px, 5vw, 44px);
}

.post-publish__header p:last-child {
  max-width: 680px;
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.post-publish__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(300px, 0.8fr);
  gap: 18px;
}

.post-publish__editor,
.post-publish__preview {
  border-radius: 8px;
  box-shadow: 0 14px 40px rgba(15, 23, 42, 0.06);
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
  height: 32px;
  color: #0f766e;
  cursor: pointer;
  background: #ecfdf5;
  border: 1px solid rgba(15, 118, 110, 0.24);
  border-radius: 6px;
}

.post-publish__upload-trigger input {
  position: absolute;
  inset: 0;
  width: 100%;
  opacity: 0;
}

.post-publish__cover-preview,
.post-publish__cover {
  width: 100%;
  max-height: 220px;
  margin-top: 10px;
  object-fit: cover;
  border-radius: 12px;
}

.post-publish__attachment-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.post-publish__attachment-item {
  display: grid;
  grid-template-columns: 72px 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.post-publish__attachment-item img {
  width: 72px;
  height: 56px;
  object-fit: cover;
  border-radius: 8px;
}

.post-publish__attachment-item strong,
.post-publish__attachment-item p {
  display: block;
  margin: 0;
}

.post-publish__attachment-item p,
.post-publish__hint {
  color: #64748b;
  font-size: 13px;
}

.post-publish__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.post-publish__actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  color: #64748b;
}

.post-publish__preview h2 {
  margin: 16px 0 12px;
  color: #172033;
}

.post-publish__preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.markdown-body {
  color: #273449;
  line-height: 1.8;
  word-break: break-word;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 16px 0 10px;
  color: #172033;
}

.markdown-body :deep(p),
.markdown-body :deep(ul) {
  margin: 0 0 12px;
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  font-family: Consolas, monospace;
  background: #f1f5f9;
  border-radius: 6px;
}

.markdown-body :deep(img) {
  display: block;
  max-width: 100%;
  margin: 14px 0;
  border-radius: 12px;
}

@media (max-width: 960px) {
  .post-publish__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .post-publish__grid,
  .post-publish__cover-field,
  .post-publish__attachment-tools,
  .post-publish__attachment-item {
    grid-template-columns: 1fr;
  }
}
</style>
