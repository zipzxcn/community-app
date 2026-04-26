<template>
  <section class="notification-center">
    <div class="notification-center__hero">
      <div>
        <p>Inbox</p>
        <h1>通知中心</h1>
        <span>统一查看点赞、评论、关注、系统消息，并支持单条或批量已读。</span>
      </div>
      <div class="notification-center__hero-total">
        <strong>{{ unread.total }}</strong>
        <span>未读</span>
      </div>
    </div>

    <div class="notification-center__stats">
      <article v-for="item in summaryCards" :key="item.key" class="notification-center__stat">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </article>
    </div>

    <a-card :bordered="false" class="notification-center__panel">
      <div class="notification-center__filters">
        <a-radio-group v-model="typeFilter" type="button" @change="handleFilterChange">
          <a-radio value="ALL">全部</a-radio>
          <a-radio value="POST_LIKE">点赞</a-radio>
          <a-radio value="COMMENT">评论</a-radio>
          <a-radio value="FOLLOW">关注</a-radio>
          <a-radio value="CHAT">聊天</a-radio>
          <a-radio value="SYSTEM">系统</a-radio>
        </a-radio-group>
        <a-radio-group v-model="readFilter" type="button" @change="handleFilterChange">
          <a-radio value="ALL">全部状态</a-radio>
          <a-radio value="UNREAD">未读</a-radio>
          <a-radio value="READ">已读</a-radio>
        </a-radio-group>
        <div class="notification-center__tools">
          <a-input-search
            v-model="keyword"
            placeholder="搜索通知标题或内容"
            allow-clear
            @search="handleKeywordSearch"
            @clear="handleKeywordSearch"
          />
          <a-button
            type="primary"
            :disabled="currentFilterUnreadCount === 0 || readFilter === 'READ'"
            :loading="markingAll"
            @click="markCurrentFilterRead"
          >
            {{ typeFilter === 'ALL' ? '全部标已读' : '当前分类标已读' }}
          </a-button>
        </div>
      </div>

      <a-spin :loading="loading">
        <div v-if="notifications.length" class="notification-center__list">
          <article
            v-for="item in notifications"
            :key="item.id"
            :class="['notification-center__item', { 'is-unread': !item.read }]"
          >
            <div class="notification-center__item-main" @click="openTarget(item)">
              <div class="notification-center__item-head">
                <div class="notification-center__item-title">
                  <span v-if="!item.read" class="notification-center__dot"></span>
                  <h2>{{ item.title }}</h2>
                </div>
                <a-tag :color="tagColorMap[item.type]">{{ typeLabelMap[item.type] }}</a-tag>
              </div>
              <p class="notification-center__item-content">{{ item.content || '暂无附加内容' }}</p>
              <div class="notification-center__item-meta">
                <span>{{ item.actor?.nickname || item.actor?.username || '系统' }}</span>
                <span>{{ formatDateTime(item.createdAt) }}</span>
                <span v-if="item.readAt">已读于 {{ formatDateTime(item.readAt) }}</span>
              </div>
            </div>
            <div class="notification-center__item-actions">
              <a-button
                v-if="!item.read"
                size="small"
                :loading="markingOneId === item.id"
                @click="markOneRead(item)"
              >
                标记已读
              </a-button>
              <a-button v-if="resolveTargetPath(item)" size="small" type="outline" @click="openTarget(item)">
                查看详情
              </a-button>
            </div>
          </article>
        </div>
        <a-empty v-else description="当前筛选条件下暂无通知" />
      </a-spin>

      <div v-if="page.total > page.size" class="notification-center__pager">
        <a-pagination
          :current="page.page"
          :page-size="page.size"
          :total="page.total"
          @change="changePage"
        />
      </div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'
import type { NotificationItem, NotificationType } from '@/types/notification'
import { formatDateTime } from '@/utils/format'

type TypeFilter = 'ALL' | NotificationType
type ReadFilter = 'ALL' | 'UNREAD' | 'READ'

const route = useRoute()
const router = useRouter()
const notificationStore = useNotificationStore()
const loading = ref(false)
const markingAll = ref(false)
const markingOneId = ref<number | null>(null)
const typeFilter = ref<TypeFilter>('ALL')
const readFilter = ref<ReadFilter>('ALL')
const keyword = ref('')
const notifications = ref<NotificationItem[]>([])
const page = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const unread = computed(() => notificationStore.unread)
const currentFilterUnreadCount = computed(() => {
  if (typeFilter.value === 'POST_LIKE') return unread.value.postLikeCount
  if (typeFilter.value === 'COMMENT') return unread.value.commentCount
  if (typeFilter.value === 'FOLLOW') return unread.value.followCount
  if (typeFilter.value === 'CHAT') return unread.value.chatCount
  if (typeFilter.value === 'SYSTEM') return unread.value.systemCount
  return unread.value.total
})
const summaryCards = computed(() => [
  { key: 'postLike', label: '点赞', value: unread.value.postLikeCount },
  { key: 'comment', label: '评论', value: unread.value.commentCount },
  { key: 'follow', label: '关注', value: unread.value.followCount },
  { key: 'chat', label: '聊天', value: unread.value.chatCount },
  { key: 'system', label: '系统', value: unread.value.systemCount },
])

const typeLabelMap: Record<NotificationType, string> = {
  POST_LIKE: '点赞',
  COMMENT: '评论',
  FOLLOW: '关注',
  CHAT: '聊天',
  SYSTEM: '系统',
}

const tagColorMap: Record<NotificationType, string> = {
  POST_LIKE: 'orangered',
  COMMENT: 'arcoblue',
  FOLLOW: 'green',
  CHAT: 'purple',
  SYSTEM: 'gray',
}

function syncFiltersFromRoute() {
  const queryType = Array.isArray(route.query.type) ? route.query.type[0] : route.query.type
  const queryRead = Array.isArray(route.query.read) ? route.query.read[0] : route.query.read
  const queryKeyword = Array.isArray(route.query.keyword) ? route.query.keyword[0] : route.query.keyword
  const queryPage = Number(Array.isArray(route.query.page) ? route.query.page[0] : route.query.page)
  typeFilter.value = ['POST_LIKE', 'COMMENT', 'FOLLOW', 'CHAT', 'SYSTEM'].includes(String(queryType))
    ? (queryType as NotificationType)
    : 'ALL'
  readFilter.value = queryRead === 'UNREAD' || queryRead === 'READ' ? (queryRead as ReadFilter) : 'ALL'
  keyword.value = typeof queryKeyword === 'string' ? queryKeyword : ''
  page.page = Number.isFinite(queryPage) && queryPage > 0 ? queryPage : 1
}

async function syncRoute() {
  await router.replace({
    query: {
      ...(typeFilter.value !== 'ALL' ? { type: typeFilter.value } : {}),
      ...(readFilter.value !== 'ALL' ? { read: readFilter.value } : {}),
      ...(keyword.value.trim() ? { keyword: keyword.value.trim() } : {}),
      ...(page.page > 1 ? { page: String(page.page) } : {}),
    },
  })
}

async function loadNotifications() {
  loading.value = true
  try {
    const result = await fetchNotifications({
      page: page.page,
      size: page.size,
      type: typeFilter.value === 'ALL' ? undefined : typeFilter.value,
      isRead: readFilter.value === 'ALL' ? undefined : readFilter.value === 'READ',
      keyword: keyword.value.trim() || undefined,
    })
    notifications.value = result.list
    page.page = result.page
    page.size = result.size
    page.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '通知加载失败')
  } finally {
    loading.value = false
  }
}

async function markOneRead(item: NotificationItem, silent = false) {
  if (item.read) {
    return
  }
  markingOneId.value = item.id
  try {
    await markNotificationRead(item.id)
    item.read = true
    item.readAt = new Date().toISOString()
    await notificationStore.refreshUnread()
    if (!silent) {
      Message.success('已标记为已读')
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '标记已读失败')
  } finally {
    markingOneId.value = null
  }
}

async function markCurrentFilterRead() {
  markingAll.value = true
  try {
    await markAllNotificationsRead(typeFilter.value === 'ALL' ? undefined : typeFilter.value)
    await Promise.all([notificationStore.refreshUnread(), loadNotifications()])
    Message.success(typeFilter.value === 'ALL' ? '全部通知已标记为已读' : '当前分类通知已标记为已读')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '批量已读失败')
  } finally {
    markingAll.value = false
  }
}

function resolveTargetPath(item: NotificationItem) {
  if (item.targetType === 'POST' && item.targetId) {
    return `/posts/${item.targetId}`
  }
  if (item.targetType === 'USER' && item.targetId) {
    return `/users/${item.targetId}`
  }
  return ''
}

async function openTarget(item: NotificationItem) {
  const targetPath = resolveTargetPath(item)
  if (!item.read) {
    await markOneRead(item, true)
  }
  if (targetPath) {
    await router.push(targetPath)
  }
}

async function handleFilterChange() {
  page.page = 1
  await syncRoute()
  await loadNotifications()
}

async function handleKeywordSearch() {
  page.page = 1
  await syncRoute()
  await loadNotifications()
}

async function changePage(current: number) {
  page.page = current
  await syncRoute()
  await loadNotifications()
}

onMounted(async () => {
  syncFiltersFromRoute()
  await Promise.all([notificationStore.refreshUnread(), loadNotifications()])
})
</script>

<style scoped lang="scss">
.notification-center {
  display: grid;
  gap: 20px;
}

.notification-center__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  background:
    radial-gradient(circle at 0% 0%, rgba(59, 130, 246, 0.18), transparent 35%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(248, 250, 252, 0.88));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
}

.notification-center__hero p,
.notification-center__hero h1,
.notification-center__hero span,
.notification-center__hero strong {
  margin: 0;
}

.notification-center__hero p {
  color: #0f766e;
  font-weight: 800;
}

.notification-center__hero h1 {
  margin-top: 8px;
  color: #172033;
  font-size: clamp(30px, 5vw, 44px);
}

.notification-center__hero span {
  display: block;
  margin-top: 10px;
  color: #64748b;
}

.notification-center__hero-total {
  text-align: right;
}

.notification-center__hero-total strong {
  color: #172033;
  font-size: 44px;
  line-height: 1;
}

.notification-center__hero-total span {
  margin-top: 6px;
}

.notification-center__stats {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 14px;
}

.notification-center__stat,
.notification-center__panel,
.notification-center__item {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.06);
}

.notification-center__stat {
  padding: 18px;
  border-radius: 16px;
}

.notification-center__stat span,
.notification-center__stat strong {
  display: block;
}

.notification-center__stat span {
  color: #64748b;
}

.notification-center__stat strong {
  margin-top: 8px;
  color: #172033;
  font-size: 26px;
}

.notification-center__panel {
  border-radius: 20px;
}

.notification-center__filters {
  display: grid;
  gap: 14px;
  margin-bottom: 18px;
}

.notification-center__tools {
  display: grid;
  grid-template-columns: minmax(0, 320px) auto;
  gap: 12px;
  justify-content: space-between;
}

.notification-center__list {
  display: grid;
  gap: 14px;
}

.notification-center__item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
}

.notification-center__item.is-unread {
  border-color: rgba(15, 118, 110, 0.24);
  box-shadow: 0 18px 50px rgba(15, 118, 110, 0.08);
}

.notification-center__item-main {
  cursor: pointer;
}

.notification-center__item-head,
.notification-center__item-meta,
.notification-center__item-actions,
.notification-center__item-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notification-center__item-head {
  justify-content: space-between;
}

.notification-center__item-title h2,
.notification-center__item-content {
  margin: 0;
}

.notification-center__item-title h2 {
  color: #172033;
  font-size: 18px;
}

.notification-center__dot {
  width: 10px;
  height: 10px;
  background: #ef4444;
  border-radius: 50%;
}

.notification-center__item-content {
  margin-top: 10px;
  color: #475569;
  line-height: 1.7;
}

.notification-center__item-meta {
  flex-wrap: wrap;
  margin-top: 12px;
  color: #64748b;
  font-size: 13px;
}

.notification-center__item-actions {
  justify-content: flex-end;
  align-self: center;
}

.notification-center__pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

@media (max-width: 900px) {
  .notification-center__stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .notification-center__item,
  .notification-center__tools,
  .notification-center__hero {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .notification-center__item-actions {
    justify-content: flex-start;
  }

  .notification-center__hero-total {
    text-align: left;
  }
}
</style>
