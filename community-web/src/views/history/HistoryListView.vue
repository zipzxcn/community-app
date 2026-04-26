<template>
  <section class="history-list">
    <div class="history-list__header">
      <div>
        <p>History</p>
        <h1>浏览历史</h1>
        <span>按最近浏览时间排序，支持删除单条记录或一键清空。</span>
      </div>
      <div class="history-list__header-actions">
        <RouterLink to="/">
          <a-button>回到首页</a-button>
        </RouterLink>
        <a-button type="primary" status="danger" :disabled="!histories.length" :loading="clearing" @click="clearAll">
          清空历史
        </a-button>
      </div>
    </div>

    <a-spin :loading="loading">
      <div v-if="histories.length" class="history-list__items">
        <a-card v-for="item in histories" :key="item.id" :bordered="false" class="history-list__item">
          <div class="history-list__item-main">
            <div class="history-list__item-copy">
              <h2>{{ item.postTitle || '帖子已不可见' }}</h2>
              <p>{{ item.author?.nickname || item.author?.username || '匿名用户' }}</p>
              <p>最近浏览：{{ formatDateTime(item.lastViewedAt) }}</p>
              <p>累计浏览：{{ item.viewCount }} 次</p>
            </div>
            <img v-if="item.postCoverUrl" :src="resolveAssetUrl(item.postCoverUrl)" alt="" class="history-list__cover" />
          </div>
          <div class="history-list__actions">
            <RouterLink v-if="item.postId" :to="`/posts/${item.postId}`">
              <a-button type="primary">再次查看</a-button>
            </RouterLink>
            <a-button
              status="danger"
              :loading="deletingId === item.id"
              @click="removeOne(item.id)"
            >
              删除记录
            </a-button>
          </div>
        </a-card>
      </div>
      <a-empty v-else description="还没有浏览历史" />
    </a-spin>

    <div v-if="page.total > page.size" class="history-list__pager">
      <a-pagination :current="page.page" :page-size="page.size" :total="page.total" @change="changePage" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { clearHistories, deleteHistory, fetchHistories } from '@/api/history'
import type { HistoryItem } from '@/types/history'
import { formatDateTime, resolveAssetUrl } from '@/utils/format'

const loading = ref(false)
const clearing = ref(false)
const deletingId = ref<number | null>(null)
const histories = ref<HistoryItem[]>([])
const page = reactive({
  page: 1,
  size: 10,
  total: 0,
})

async function loadHistories() {
  loading.value = true
  try {
    const result = await fetchHistories({ page: page.page, size: page.size })
    histories.value = result.list
    page.page = result.page
    page.size = result.size
    page.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '浏览历史加载失败')
  } finally {
    loading.value = false
  }
}

function changePage(current: number) {
  page.page = current
  loadHistories()
}

function removeOne(historyId: number) {
  Modal.confirm({
    title: '删除历史记录',
    content: '确认删除这条浏览历史？',
    async onOk() {
      deletingId.value = historyId
      try {
        await deleteHistory(historyId)
        Message.success('历史记录已删除')
        await loadHistories()
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '删除历史失败')
      } finally {
        deletingId.value = null
      }
    },
  })
}

function clearAll() {
  Modal.confirm({
    title: '清空浏览历史',
    content: '确认清空全部浏览历史？该操作不可恢复。',
    async onOk() {
      clearing.value = true
      try {
        await clearHistories()
        Message.success('浏览历史已清空')
        page.page = 1
        await loadHistories()
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '清空历史失败')
      } finally {
        clearing.value = false
      }
    },
  })
}

onMounted(loadHistories)
</script>

<style scoped lang="scss">
.history-list {
  display: grid;
  gap: 20px;
}

.history-list__header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  background:
    radial-gradient(circle at 0% 0%, rgba(15, 118, 110, 0.18), transparent 36%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.88));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
}

.history-list__header p,
.history-list__header h1,
.history-list__header span {
  margin: 0;
}

.history-list__header p {
  color: #0f766e;
  font-weight: 800;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.history-list__header h1 {
  margin-top: 8px;
  color: #172033;
  font-size: clamp(32px, 5vw, 44px);
}

.history-list__header span {
  display: block;
  margin-top: 10px;
  color: #64748b;
}

.history-list__header-actions,
.history-list__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.history-list__items {
  display: grid;
  gap: 14px;
}

.history-list__item {
  border-radius: 20px;
}

.history-list__item :deep(.arco-card-body) {
  display: grid;
  gap: 16px;
}

.history-list__item-main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  align-items: center;
}

.history-list__item-copy h2 {
  margin: 0 0 10px;
  color: #172033;
}

.history-list__item-copy p {
  margin: 4px 0;
  color: #64748b;
}

.history-list__cover {
  width: 140px;
  height: 96px;
  object-fit: cover;
  border-radius: 16px;
}

.history-list__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .history-list__header,
  .history-list__item-main {
    align-items: flex-start;
    flex-direction: column;
    grid-template-columns: 1fr;
  }
}
</style>
