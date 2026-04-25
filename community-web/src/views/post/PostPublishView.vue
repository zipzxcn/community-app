<template>
  <section class="post-publish">
    <div class="post-publish__header">
      <div>
        <p class="post-publish__eyebrow">Write</p>
        <h1>{{ currentDraftId ? '继续编辑草稿' : '发布新帖子' }}</h1>
        <p>一期先接入 Markdown 原文、封面 URL、草稿自动保存和发布链路；标签和图片直传等后端补齐后再增强。</p>
      </div>
      <RouterLink to="/drafts">
        <a-button>查看草稿箱</a-button>
      </RouterLink>
    </div>

    <div class="post-publish__grid">
      <a-card class="post-publish__editor" :bordered="false">
        <a-form :model="form" layout="vertical">
          <a-form-item field="title" label="标题">
            <a-input v-model="form.title" placeholder="写一个清楚的标题" :max-length="120" show-word-limit />
          </a-form-item>
          <a-form-item field="coverUrl" label="封面 URL（可选）">
            <a-input v-model="form.coverUrl" placeholder="https://example.com/cover.png" allow-clear />
          </a-form-item>
          <a-form-item field="contentMd" label="正文 Markdown">
            <a-textarea
              v-model="form.contentMd"
              placeholder="支持 Markdown 原文，先把内容写清楚。"
              :auto-size="{ minRows: 16, maxRows: 28 }"
            />
          </a-form-item>
          <a-form-item field="allowComment" label="允许评论">
            <a-switch v-model="form.allowComment" />
          </a-form-item>
        </a-form>
        <div class="post-publish__actions">
          <span>{{ saveStatus }}</span>
          <a-button :loading="saving" @click="saveDraftManually">保存草稿</a-button>
          <a-button type="primary" :loading="publishing" @click="publish">发布</a-button>
        </div>
      </a-card>

      <a-card class="post-publish__preview" :bordered="false">
        <template #title>预览</template>
        <img v-if="form.coverUrl" :src="form.coverUrl" alt="" class="post-publish__cover" />
        <h2>{{ form.title || '未命名帖子' }}</h2>
        <div class="post-publish__content">{{ form.contentMd || '正文预览会显示在这里。' }}</div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { createDraft, fetchDraftDetail, publishDraft, updateDraft } from '@/api/draft'
import { createPost } from '@/api/post'

const route = useRoute()
const router = useRouter()
const currentDraftId = ref<number | null>(null)
const saving = ref(false)
const publishing = ref(false)
const loadingDraft = ref(false)
const saveStatus = ref('尚未保存')
const form = reactive({
  title: '',
  coverUrl: '',
  contentMd: '',
  allowComment: true,
})
let autosaveTimer: number | undefined

function getDraftIdFromQuery() {
  const raw = route.query.draftId
  const value = Array.isArray(raw) ? raw[0] : raw
  const id = Number(value)
  return Number.isFinite(id) && id > 0 ? id : null
}

function validateForDraft() {
  if (!form.title.trim()) {
    Message.warning('保存草稿至少需要填写标题')
    return false
  }
  return true
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
  return true
}

async function loadDraft(draftId: number) {
  loadingDraft.value = true
  try {
    const draft = await fetchDraftDetail(draftId)
    currentDraftId.value = draft.id
    form.title = draft.title
    form.coverUrl = draft.coverUrl || ''
    form.contentMd = draft.contentMd || ''
    saveStatus.value = `已加载草稿 #${draft.id}`
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '草稿加载失败')
  } finally {
    loadingDraft.value = false
  }
}

async function saveDraft(autoSave = false) {
  if (loadingDraft.value || !validateForDraft()) {
    return
  }
  saving.value = true
  try {
    const payload = {
      title: form.title.trim(),
      coverUrl: form.coverUrl.trim() || undefined,
      contentMd: form.contentMd,
      autoSave,
    }
    if (currentDraftId.value) {
      await updateDraft(currentDraftId.value, payload)
    } else {
      const result = await createDraft(payload)
      currentDraftId.value = result.draftId
    }
    saveStatus.value = autoSave ? '已自动保存' : '草稿已保存'
    if (!autoSave) {
      Message.success('草稿已保存')
    }
  } catch (error) {
    saveStatus.value = '保存失败'
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

async function publish() {
  if (!validateForPublish()) {
    return
  }
  publishing.value = true
  try {
    let postId: number
    if (currentDraftId.value) {
      await saveDraft(false)
      const result = await publishDraft(currentDraftId.value)
      postId = result.postId
    } else {
      const result = await createPost({
        title: form.title.trim(),
        contentMd: form.contentMd,
        coverUrl: form.coverUrl.trim() || undefined,
        allowComment: form.allowComment,
        tagIds: [],
        attachmentFileIds: [],
      })
      postId = result.postId
    }
    Message.success('帖子已发布')
    await router.push(`/posts/${postId}`)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '发布失败')
  } finally {
    publishing.value = false
  }
}

function scheduleAutoSave() {
  window.clearTimeout(autosaveTimer)
  if (!form.title.trim()) {
    saveStatus.value = '填写标题后自动保存'
    return
  }
  saveStatus.value = '等待自动保存...'
  autosaveTimer = window.setTimeout(() => {
    saveDraft(true)
  }, 1200)
}

watch(
  () => [form.title, form.coverUrl, form.contentMd],
  scheduleAutoSave,
)

onMounted(() => {
  const draftId = getDraftIdFromQuery()
  if (draftId) {
    loadDraft(draftId)
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
  padding: 30px;
  background:
    linear-gradient(135deg, rgba(14, 165, 233, 0.12), rgba(245, 158, 11, 0.16)),
    rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 30px;
}

.post-publish__eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.post-publish__header h1 {
  margin: 0 0 10px;
  color: #172033;
  font-size: clamp(30px, 5vw, 48px);
}

.post-publish__header p:last-child {
  max-width: 680px;
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}

.post-publish__grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: 18px;
}

.post-publish__editor,
.post-publish__preview {
  border-radius: 26px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.post-publish__actions {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  color: #64748b;
}

.post-publish__cover {
  width: 100%;
  max-height: 220px;
  object-fit: cover;
  border-radius: 20px;
}

.post-publish__preview h2 {
  margin: 18px 0 12px;
  color: #172033;
}

.post-publish__content {
  color: #334155;
  line-height: 1.85;
  white-space: pre-wrap;
}

@media (max-width: 960px) {
  .post-publish__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .post-publish__grid {
    grid-template-columns: 1fr;
  }
}
</style>
