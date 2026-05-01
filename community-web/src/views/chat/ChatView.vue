<template>
  <section class="chat-view">
    <div class="chat-view__hero">
      <div>
        <p>Realtime Inbox</p>
        <h1>私信聊天</h1>
        <span>一期采用 REST 拉取历史与发送消息，WebSocket 负责新消息与已读回执推送。</span>
      </div>
      <div class="chat-view__hero-actions">
        <a-tag :color="chatStore.connected ? 'green' : 'orange'">
          {{ chatStore.connected ? '实时已连接' : '实时重连中' }}
        </a-tag>
        <a-button :loading="refreshing" @click="refreshCurrent">手动刷新</a-button>
      </div>
    </div>

    <a-alert
      v-if="!chatStore.connected"
      class="chat-view__status-alert"
      type="warning"
      show-icon
    >
      当前实时通道未连接，页面仍可通过 REST 刷新继续聊天；连接恢复后会自动同步新消息。
    </a-alert>

    <div class="chat-view__body">
      <a-card :bordered="false" class="chat-view__threads">
        <div class="chat-view__panel-head">
          <div>
            <h2>会话列表</h2>
            <span>展示已有会话与尚未开聊的互关好友</span>
          </div>
          <strong>{{ threads.length }}</strong>
        </div>
        <a-spin :loading="false">
          <div v-if="threadLoading && !threads.length" class="app-loading-list">
            <div v-for="index in 4" :key="index" class="app-loading-card">
              <div class="chat-view__thread-skeleton">
                <div class="app-skeleton app-skeleton--avatar"></div>
                <div class="chat-view__thread-skeleton-copy">
                  <div class="app-skeleton app-skeleton--title"></div>
                  <div class="app-skeleton app-skeleton--text"></div>
                  <div class="app-skeleton app-skeleton--text-short"></div>
                </div>
              </div>
            </div>
          </div>
          <div v-else-if="threads.length" class="chat-view__thread-list">
            <button
              v-for="item in threads"
              :key="`${item.peerUser.id}-${item.threadId ?? 'new'}`"
              type="button"
              :class="['chat-view__thread-item', { 'is-active': activePeerId === item.peerUser.id }]"
              @click="selectThread(item)"
            >
              <a-avatar :size="44" class="chat-view__avatar">
                <img v-if="item.peerUser.avatarUrl" :src="resolveAssetUrl(item.peerUser.avatarUrl)" alt="" />
                <template v-else>{{ (item.peerUser.nickname || item.peerUser.username).slice(0, 1).toUpperCase() }}</template>
              </a-avatar>
              <div class="chat-view__thread-main">
                <div class="chat-view__thread-head">
                  <strong>{{ item.peerUser.nickname || item.peerUser.username }}</strong>
                  <span>{{ formatDateTime(item.lastMessageAt) }}</span>
                </div>
                <small>@{{ item.peerUser.username }}</small>
                <p>{{ item.lastMessagePreview || '还没有聊天，先打个招呼吧。' }}</p>
              </div>
              <div class="chat-view__thread-side">
                <a-tag size="small" :color="item.status === 'ACTIVE' ? 'green' : 'orange'">
                  {{ item.status === 'ACTIVE' ? '可聊天' : '只读' }}
                </a-tag>
                <span v-if="item.unreadCount > 0" class="chat-view__thread-badge">
                  {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
                </span>
              </div>
            </button>
          </div>
          <div v-else class="app-empty-state app-empty-state--center">
            <p class="app-empty-state__eyebrow">Empty Inbox</p>
            <h3 class="app-empty-state__title">你还没有任何会话</h3>
            <p class="app-empty-state__desc">去用户主页找到互关好友，就可以直接发起聊天。实时通道恢复后，新消息会自动同步到这里。</p>
            <div class="app-empty-state__actions">
              <RouterLink to="/users/search">
                <a-button type="primary">去找人</a-button>
              </RouterLink>
              <a-button @click="refreshCurrent">手动刷新</a-button>
            </div>
          </div>
        </a-spin>
      </a-card>

      <a-card :bordered="false" class="chat-view__messages">
        <template v-if="activeThread">
          <div class="chat-view__panel-head">
            <div>
              <h2>{{ activeThread.peerUser.nickname || activeThread.peerUser.username }}</h2>
              <span>
                {{ activeThread.status === 'ACTIVE' ? '互关状态，可正常聊天' : '当前已非互关，仅保留历史消息只读查看' }}
              </span>
            </div>
            <div class="chat-view__message-head-tools">
              <a-tag :color="chatStore.connected ? 'green' : 'gray'">
                {{ chatStore.connected ? '实时同步' : 'REST 兜底' }}
              </a-tag>
              <span v-if="chatStore.lastError">{{ chatStore.lastError }}</span>
            </div>
          </div>

          <div class="chat-view__history-tools">
            <a-button v-if="messagePage.hasMore" size="small" :loading="loadingMore" @click="loadMoreMessages">
              加载更早消息
            </a-button>
          </div>

          <a-spin :loading="false">
            <div v-if="messageLoading && !messages.length" class="app-loading-list chat-view__message-loading">
              <div v-for="index in 4" :key="index" class="app-loading-card">
                <div class="app-skeleton app-skeleton--text-short"></div>
                <div class="app-skeleton app-skeleton--text"></div>
              </div>
            </div>
            <div v-else ref="messageListRef" class="chat-view__message-list">
              <article
                v-for="message in messages"
                :key="message.id"
                :class="['chat-view__message-item', { 'is-self': message.senderId === currentUserId }]"
              >
                <div class="chat-view__message-bubble">
                  <p>{{ message.content }}</p>
                  <div class="chat-view__message-meta">
                    <span>{{ formatDateTime(message.createdAt) }}</span>
                    <span v-if="message.senderId === currentUserId">{{ message.read ? '已读' : '未读' }}</span>
                  </div>
                </div>
              </article>
            </div>
          </a-spin>

          <div class="chat-view__composer">
            <a-textarea
              v-model="draft"
              :disabled="activeThread.status !== 'ACTIVE' || sending"
              :auto-size="{ minRows: 3, maxRows: 5 }"
              placeholder="输入消息，按 Enter 发送，Shift + Enter 换行"
              @keydown.enter.exact.prevent="handleEnterSend"
            />
            <div class="chat-view__composer-actions">
              <span>
                <template v-if="activeThread.status !== 'ACTIVE'">已非互关，当前会话只读</template>
                <template v-else>{{ draft.trim().length }}/5000</template>
              </span>
              <a-button
                type="primary"
                :disabled="activeThread.status !== 'ACTIVE' || !draft.trim()"
                :loading="sending"
                @click="submitMessage"
              >
                发送消息
              </a-button>
            </div>
          </div>
        </template>

        <div v-else class="app-empty-state app-empty-state--center chat-view__empty-panel">
          <p class="app-empty-state__eyebrow">Select Thread</p>
          <h3 class="app-empty-state__title">请选择一个会话开始查看消息</h3>
          <p class="app-empty-state__desc">如果左侧没有会话，可以先从用户主页发起私信，或等待新的聊天通知进入列表。</p>
        </div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * 聊天页：会话列表、消息历史、发送消息、已读同步与实时推送统一收口在本页。
 */
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchChatMessages, fetchChatThreads, markChatThreadRead, openChatThread, sendChatMessage } from '@/api/chat'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useNotificationStore } from '@/stores/notification'
import type { ChatMessageItem, ChatReadReceipt, ChatThreadItem, ChatWsEnvelope } from '@/types/chat'
import { formatDateTime, resolveAssetUrl } from '@/utils/format'

const router = useRouter()
// 教学点：聊天页支持从别的页面带 query 进来，比如从用户主页点击“发私信”后自动定位到目标用户。
const route = useRoute()
const authStore = useAuthStore()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()

const currentUserId = computed(() => authStore.userInfo?.id || 0)
const threadLoading = ref(false)
const messageLoading = ref(false)
const loadingMore = ref(false)
const refreshing = ref(false)
const sending = ref(false)
const threads = ref<ChatThreadItem[]>([])
const activeThread = ref<ChatThreadItem | null>(null)
const messages = ref<ChatMessageItem[]>([])
const draft = ref('')
const messageListRef = ref<HTMLElement | null>(null)
const messagePage = reactive({
  hasMore: false,
})

let pollTimer: number | null = null
let pingTimer: number | null = null
let unsubscribe: (() => void) | null = null

const activePeerId = computed(() => activeThread.value?.peerUser.id || 0)

function scrollToBottom() {
  nextTick(() => {
    const element = messageListRef.value
    if (!element) return
    element.scrollTop = element.scrollHeight
  })
}

function upsertThread(item: ChatThreadItem) {
  const index = threads.value.findIndex((thread) => thread.peerUser.id === item.peerUser.id)
  if (index >= 0) {
    threads.value.splice(index, 1, item)
  } else {
    threads.value.unshift(item)
  }
}

// load* 系列方法负责从后端拉取页面初始化数据，统一控制 loading 和错误提示。
async function loadThreads(options: { silent?: boolean } = {}) {
  if (!options.silent) {
    threadLoading.value = true
  }
  try {
    const result = await fetchChatThreads()
    threads.value = result.list
    if (activeThread.value) {
      const matched = result.list.find((item) => item.peerUser.id === activeThread.value?.peerUser.id)
      if (matched) {
        activeThread.value = matched
      }
    }
  } catch (error) {
    if (!options.silent) {
      Message.error(error instanceof Error ? error.message : '会话列表加载失败')
    }
  } finally {
    threadLoading.value = false
  }
}

async function markCurrentThreadRead() {
  const threadId = activeThread.value?.threadId
  const lastMessage = [...messages.value].reverse().find((item) => item.receiverId === currentUserId.value)
  if (!threadId || !lastMessage || currentUserId.value === 0) {
    return
  }
  try {
    await markChatThreadRead(threadId, { lastReadMessageId: lastMessage.id })
    messages.value = messages.value.map((item) =>
      item.receiverId === currentUserId.value && item.id <= lastMessage.id
        ? { ...item, read: true, readAt: new Date().toISOString() }
        : item,
    )
    if (activeThread.value) {
      activeThread.value = { ...activeThread.value, unreadCount: 0 }
      upsertThread(activeThread.value)
    }
    await notificationStore.refreshUnread().catch(() => undefined)
  } catch {
    // 已读同步失败时保留静默，用户仍可继续聊天。
  }
}

// 消息历史通过 cursor/分页向上翻，避免一次性拉全量记录。
async function loadMessages(threadId: number, options: { cursor?: number; prepend?: boolean } = {}) {
  if (options.prepend) {
    loadingMore.value = true
  } else {
    messageLoading.value = true
  }
  try {
    const result = await fetchChatMessages(threadId, {
      cursor: options.cursor,
      size: 20,
    })
    messagePage.hasMore = result.hasMore
    messages.value = options.prepend ? [...result.list, ...messages.value] : result.list
    if (!options.prepend) {
      scrollToBottom()
      await markCurrentThreadRead()
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '消息加载失败')
  } finally {
    messageLoading.value = false
    loadingMore.value = false
  }
}

async function ensureThread(item: ChatThreadItem) {
  if (item.threadId) {
    return item
  }
  const opened = await openChatThread(item.peerUser.id)
  upsertThread(opened)
  return opened
}

// 教学点：选中会话时，如果 threadId 为空，说明这是“互关但未开聊”的占位项，需要先创建真实会话。
async function selectThread(item: ChatThreadItem) {
  try {
    const resolved = await ensureThread(item)
    activeThread.value = resolved
    messages.value = []
    await router.replace({
      query: { threadId: String(resolved.threadId) },
    })
    await loadMessages(resolved.threadId as number)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '打开会话失败')
  }
}

async function loadMoreMessages() {
  if (!activeThread.value?.threadId || !messages.value.length) {
    return
  }
  await loadMessages(activeThread.value.threadId, {
    cursor: messages.value[0].id,
    prepend: true,
  })
}

function createClientMessageId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `chat-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

async function submitMessage() {
  const threadId = activeThread.value?.threadId
  const content = draft.value.trim()
  if (!threadId || !content) {
    return
  }
  sending.value = true
  try {
    const result = await sendChatMessage(threadId, {
      clientMsgId: createClientMessageId(),
      messageType: 'TEXT',
      content,
    })
    messages.value = [...messages.value, result]
    draft.value = ''
    if (activeThread.value) {
      activeThread.value = {
        ...activeThread.value,
        lastMessagePreview: result.content,
        lastMessageAt: result.createdAt,
      }
      upsertThread(activeThread.value)
    }
    scrollToBottom()
    await loadThreads({ silent: true })
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '消息发送失败')
  } finally {
    sending.value = false
  }
}

// handle* 系列方法通常对应用户点击动作，例如登录、发布、提交、删除。
async function handleEnterSend() {
  if (sending.value || activeThread.value?.status !== 'ACTIVE') {
    return
  }
  await submitMessage()
}

async function refreshCurrent() {
  refreshing.value = true
  try {
    await loadThreads({ silent: true })
    if (activeThread.value?.threadId) {
      const matched = threads.value.find((item) => item.peerUser.id === activeThread.value?.peerUser.id)
      if (matched) {
        activeThread.value = matched
      }
      const threadId = activeThread.value?.threadId
      if (threadId) {
        await loadMessages(threadId)
      }
    }
    await notificationStore.refreshUnread().catch(() => undefined)
  } finally {
    refreshing.value = false
  }
}

async function applyRouteIntent() {
  const threadIdQuery = Array.isArray(route.query.threadId) ? route.query.threadId[0] : route.query.threadId
  const userIdQuery = Array.isArray(route.query.userId) ? route.query.userId[0] : route.query.userId
  if (userIdQuery) {
    const targetUserId = Number(userIdQuery)
    if (Number.isFinite(targetUserId) && targetUserId > 0) {
      try {
        const opened = await openChatThread(targetUserId)
        upsertThread(opened)
        activeThread.value = opened
        await router.replace({
          query: { threadId: String(opened.threadId) },
        })
        await loadMessages(opened.threadId as number)
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '发起私信失败')
      }
      return
    }
  }
  if (threadIdQuery) {
    const threadId = Number(threadIdQuery)
    const matched = threads.value.find((item) => item.threadId === threadId)
    if (matched) {
      await selectThread(matched)
      return
    }
  }
  if (!activeThread.value && threads.value.length) {
    await selectThread(threads.value[0])
  }
}

function handleWsEvent(event: ChatWsEnvelope) {
  if (event.type === 'chat.message') {
    const payload = event.data as ChatMessageItem
    if (activeThread.value?.threadId === payload.threadId) {
      if (!messages.value.some((item) => item.id === payload.id)) {
        messages.value = [...messages.value, payload]
        scrollToBottom()
      }
      markCurrentThreadRead().catch(() => undefined)
    }
    loadThreads({ silent: true }).catch(() => undefined)
    return
  }

  if (event.type === 'chat.read') {
    const payload = event.data as ChatReadReceipt
    if (activeThread.value?.threadId === payload.threadId) {
      messages.value = messages.value.map((item) =>
        item.senderId === currentUserId.value && item.id <= payload.lastReadMessageId
          ? { ...item, read: true, readAt: payload.readAt }
          : item,
      )
    }
  }
}

function startBackgroundTasks() {
  pollTimer = window.setInterval(() => {
    refreshCurrent().catch(() => undefined)
  }, 15000)
  pingTimer = window.setInterval(() => {
    chatStore.ping()
  }, 20000)
}

function stopBackgroundTasks() {
  if (pollTimer !== null) {
    window.clearInterval(pollTimer)
    pollTimer = null
  }
  if (pingTimer !== null) {
    window.clearInterval(pingTimer)
    pingTimer = null
  }
}

// 监听路由参数或局部状态变化，保证页面在切换对象后自动刷新。
watch(
  () => route.fullPath,
  async () => {
    if (threads.value.length) {
      await applyRouteIntent()
    }
  },
)

onMounted(async () => {
  chatStore.connect()
  unsubscribe = chatStore.subscribe(handleWsEvent)
  await loadThreads()
  await applyRouteIntent()
  startBackgroundTasks()
})

onUnmounted(() => {
  unsubscribe?.()
  unsubscribe = null
  stopBackgroundTasks()
})
</script>

<style scoped lang="scss">
.chat-view {
  display: grid;
  gap: 20px;
}

.chat-view__status-alert {
  border-radius: 16px;
}

.chat-view__hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
  background:
    radial-gradient(circle at 0% 0%, rgba(59, 130, 246, 0.16), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 255, 0.92) 55%, rgba(248, 250, 252, 0.88));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
}

.chat-view__hero p,
.chat-view__hero h1,
.chat-view__hero span,
.chat-view__hero strong {
  margin: 0;
}

.chat-view__hero p {
  color: #0f766e;
  font-weight: 800;
}

.chat-view__hero h1 {
  margin-top: 8px;
  color: #172033;
  font-size: clamp(30px, 5vw, 42px);
}

.chat-view__hero span {
  display: block;
  margin-top: 10px;
  color: #64748b;
}

.chat-view__hero-actions,
.chat-view__panel-head,
.chat-view__thread-head,
.chat-view__thread-side,
.chat-view__message-meta,
.chat-view__composer-actions,
.chat-view__message-head-tools {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-view__hero-actions,
.chat-view__message-head-tools {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.chat-view__body {
  display: grid;
  grid-template-columns: minmax(240px, 300px) minmax(0, 1.45fr);
  gap: 18px;
  align-items: stretch;
}

.chat-view__threads,
.chat-view__messages {
  min-height: 680px;
  border-radius: 22px;
  box-shadow: 0 16px 48px rgba(15, 23, 42, 0.06);
}

.chat-view__messages {
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr) auto;
}

.chat-view__messages :deep(.arco-card-body) {
  display: grid;
  grid-template-rows: inherit;
  min-height: 100%;
  min-width: 0;
}

.chat-view__threads :deep(.arco-card-body) {
  display: grid;
  align-content: start;
  min-width: 0;
}

.chat-view__empty-panel {
  min-height: 420px;
  align-content: center;
}

.chat-view__panel-head {
  justify-content: space-between;
  margin-bottom: 16px;
}

.chat-view__panel-head h2,
.chat-view__panel-head span,
.chat-view__panel-head strong {
  margin: 0;
}

.chat-view__panel-head h2 {
  color: #172033;
  font-size: 22px;
}

.chat-view__panel-head span {
  display: block;
  margin-top: 6px;
  color: #64748b;
}

.chat-view__panel-head strong {
  color: #172033;
  font-size: 30px;
}

.chat-view__thread-list {
  display: grid;
  gap: 10px;
}

.chat-view__thread-skeleton {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-view__thread-skeleton-copy {
  display: grid;
  flex: 1;
  gap: 10px;
}

.chat-view__thread-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 12px;
  width: 100%;
  padding: 14px;
  text-align: left;
  cursor: pointer;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
}

.chat-view__thread-item.is-active {
  background: rgba(236, 253, 245, 0.92);
  border-color: rgba(15, 118, 110, 0.22);
  box-shadow: 0 16px 40px rgba(15, 118, 110, 0.08);
}

.chat-view__avatar {
  color: #0f766e;
  font-weight: 800;
  background: #ccfbf1;
}

.chat-view__thread-main {
  min-width: 0;
}

.chat-view__thread-head {
  justify-content: space-between;
}

.chat-view__thread-head strong,
.chat-view__thread-main p {
  margin: 0;
}

.chat-view__thread-main small {
  display: block;
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.chat-view__thread-head strong {
  color: #172033;
}

.chat-view__thread-head span,
.chat-view__thread-main p,
.chat-view__composer-actions span,
.chat-view__message-head-tools span {
  color: #64748b;
}

.chat-view__thread-main p {
  margin-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-view__thread-side {
  flex-direction: column;
  justify-content: center;
}

.chat-view__thread-badge {
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  color: #fff;
  font-size: 12px;
  line-height: 22px;
  text-align: center;
  background: #ef4444;
  border-radius: 999px;
}

.chat-view__history-tools {
  margin-bottom: 12px;
}

.chat-view__message-list {
  display: grid;
  gap: 12px;
  min-height: 520px;
  height: 100%;
  padding: 8px 4px;
  overflow-y: auto;
  scrollbar-width: thin;
}

.chat-view__message-loading {
  min-height: 240px;
  align-content: start;
}

.chat-view__message-item {
  display: flex;
  justify-content: flex-start;
}

.chat-view__message-item.is-self {
  justify-content: flex-end;
}

.chat-view__message-bubble {
  min-width: 124px;
  max-width: min(86%, 760px);
  padding: 14px 16px;
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px 18px 18px 6px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.05);
}

.chat-view__message-item.is-self .chat-view__message-bubble {
  background: linear-gradient(135deg, #0f766e, #0ea5a3);
  border-color: transparent;
  border-radius: 18px 18px 6px 18px;
}

.chat-view__message-bubble p,
.chat-view__message-meta span {
  margin: 0;
}

.chat-view__message-bubble p {
  color: #172033;
  line-height: 1.7;
  white-space: pre-wrap;
}

.chat-view__message-item.is-self .chat-view__message-bubble p,
.chat-view__message-item.is-self .chat-view__message-meta span {
  color: #f8fafc;
}

.chat-view__message-meta {
  justify-content: flex-end;
  margin-top: 8px;
  font-size: 12px;
}

.chat-view__composer {
  display: grid;
  gap: 10px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(15, 23, 42, 0.08);
}

.chat-view__composer-actions {
  justify-content: space-between;
}

@media (max-width: 980px) {
  .chat-view__body {
    grid-template-columns: 1fr;
  }

  .chat-view__threads,
  .chat-view__messages {
    min-height: auto;
  }

  .chat-view__message-list {
    height: 360px;
    min-height: 360px;
  }
}

@media (max-width: 720px) {
  .chat-view__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .chat-view__thread-item {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .chat-view__thread-side {
    grid-column: 2;
    flex-direction: row;
    justify-content: flex-start;
  }

  .chat-view__composer-actions,
  .chat-view__panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .chat-view__messages {
    grid-template-rows: auto auto minmax(300px, 1fr) auto;
  }

  .chat-view__message-list {
    padding-inline: 0;
  }

  .chat-view__message-bubble {
    min-width: 0;
    max-width: 90%;
  }
}
</style>
