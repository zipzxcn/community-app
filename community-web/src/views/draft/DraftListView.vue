<template>
  <section class="draft-list">
    <div class="draft-list__header">
      <div>
        <p>Drafts</p>
        <h1>草稿箱</h1>
      </div>
      <RouterLink to="/posts/publish">
        <a-button type="primary">新建帖子</a-button>
      </RouterLink>
    </div>

    <a-spin :loading="loading">
      <div v-if="drafts.length" class="draft-list__items">
        <a-card v-for="draft in drafts" :key="draft.id" :bordered="false" class="draft-list__item">
          <div>
            <h2>{{ draft.title }}</h2>
            <p>更新时间：{{ formatDateTime(draft.updatedAt) }}</p>
            <p v-if="draft.autoSavedAt">自动保存：{{ formatDateTime(draft.autoSavedAt) }}</p>
          </div>
          <div class="draft-list__actions">
            <RouterLink :to="{ name: 'post-publish', query: { draftId: draft.id } }">
              <a-button>继续编辑</a-button>
            </RouterLink>
            <a-button type="primary" :loading="publishingId === draft.id" @click="publish(draft.id)">发布</a-button>
            <a-button status="danger" :loading="deletingId === draft.id" @click="remove(draft.id)">删除</a-button>
          </div>
        </a-card>
      </div>
      <a-empty v-else description="暂无草稿" />
    </a-spin>

    <div v-if="page.total > page.size" class="draft-list__pager">
      <a-pagination :current="page.page" :page-size="page.size" :total="page.total" @change="changePage" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import { deleteDraft, fetchDrafts, publishDraft } from '@/api/draft'
import type { DraftItem } from '@/types/draft'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const drafts = ref<DraftItem[]>([])
const deletingId = ref<number | null>(null)
const publishingId = ref<number | null>(null)
const page = reactive({
  page: 1,
  size: 10,
  total: 0,
})

async function loadDrafts() {
  loading.value = true
  try {
    const result = await fetchDrafts({ page: page.page, size: page.size })
    drafts.value = result.list
    page.page = result.page
    page.size = result.size
    page.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '草稿加载失败')
  } finally {
    loading.value = false
  }
}

function changePage(current: number) {
  page.page = current
  loadDrafts()
}

function remove(draftId: number) {
  Modal.confirm({
    title: '删除草稿',
    content: '删除后不可从草稿箱恢复，确认继续？',
    async onOk() {
      deletingId.value = draftId
      try {
        await deleteDraft(draftId)
        Message.success('草稿已删除')
        await loadDrafts()
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '草稿删除失败')
      } finally {
        deletingId.value = null
      }
    },
  })
}

async function publish(draftId: number) {
  publishingId.value = draftId
  try {
    const result = await publishDraft(draftId)
    Message.success('草稿已发布')
    await router.push(`/posts/${result.postId}`)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '草稿发布失败，请确认正文不为空')
  } finally {
    publishingId.value = null
  }
}

onMounted(loadDrafts)
</script>

<style scoped lang="scss">
.draft-list {
  display: grid;
  gap: 20px;
}

.draft-list__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 30px;
}

.draft-list__header p {
  margin: 0 0 8px;
  color: #0f766e;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.draft-list__header h1 {
  margin: 0;
  color: #172033;
  font-size: 42px;
}

.draft-list__items {
  display: grid;
  gap: 14px;
}

.draft-list__item {
  border-radius: 24px;
}

.draft-list__item :deep(.arco-card-body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.draft-list__item h2 {
  margin: 0 0 8px;
  color: #172033;
}

.draft-list__item p {
  margin: 4px 0;
  color: #64748b;
}

.draft-list__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.draft-list__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .draft-list__header,
  .draft-list__item :deep(.arco-card-body) {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
