<template>
  <div class="default-layout">
    <header class="default-layout__header">
      <div class="default-layout__header-inner">
        <div class="default-layout__brand-group">
          <RouterLink class="default-layout__brand" to="/">
            <span class="default-layout__brand-mark">C</span>
            <span class="default-layout__brand-copy">
              <strong>Community</strong>
              <small>内容社区 · 一期 MVP</small>
            </span>
          </RouterLink>
          <p class="default-layout__slogan"><!--围绕帖子、关系、通知与私信的社区闭环已经打通，当前进入 UI 统一与体验打磨阶段。--></p>
        </div>

        <div class="default-layout__header-controls">
          <div v-if="authStore.isLoggedIn" class="default-layout__quick-status">
            <span class="app-chip">
              <icon-message />
              聊天 {{ chatStore.connected ? '在线' : '离线' }}
            </span>
            <span class="app-chip">
              <icon-notification />
              未读 {{ unreadSummary }}
            </span>
          </div>

          <a-button class="default-layout__menu-button" shape="circle" @click="mobileMenuOpen = !mobileMenuOpen">
            <icon-menu />
          </a-button>
        </div>
      </div>

      <div :class="['default-layout__nav-shell', { 'default-layout__nav-shell--open': mobileMenuOpen }]">
        <div class="default-layout__nav-group">
          <span class="default-layout__nav-label">主导航</span>
          <nav class="default-layout__nav">
            <RouterLink v-for="item in primaryNav" :key="item.to" :to="item.to" class="default-layout__nav-link" @click="mobileMenuOpen = false">
              <component :is="item.icon" />
              <span>{{ item.label }}</span>
            </RouterLink>
          </nav>
        </div>

        <div v-if="authStore.isLoggedIn" class="default-layout__nav-group">
          <span class="default-layout__nav-label">功能区</span>
          <nav class="default-layout__nav">
            <RouterLink
              v-for="item in featureNav"
              :key="item.to"
              :to="item.to"
              class="default-layout__nav-link"
              @click="mobileMenuOpen = false"
            >
              <component :is="item.icon" />
              <span>{{ item.label }}</span>
              <span v-if="item.badge && item.badge > 0" class="default-layout__nav-badge">
                {{ item.badge > 99 ? '99+' : item.badge }}
              </span>
            </RouterLink>
          </nav>
        </div>

        <div class="default-layout__user-area">
          <template v-if="authStore.isLoggedIn">
            <div class="default-layout__user-card">
              <a-avatar :size="42" class="default-layout__user-avatar">
                <img v-if="authStore.userInfo?.avatarUrl" :src="resolveAssetUrl(authStore.userInfo.avatarUrl)" alt="" />
                <template v-else>{{ displayInitial }}</template>
              </a-avatar>
              <div class="default-layout__user-meta">
                <strong>{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</strong>
                <span>@{{ authStore.userInfo?.username }}</span>
              </div>
              <a-dropdown trigger="click">
                <a-button class="default-layout__user-trigger" shape="circle">
                  <icon-more />
                </a-button>
                <template #content>
                  <a-doption @click="goTo('/me')">个人中心</a-doption>
                  <a-doption @click="goTo('/posts/publish')">发布帖子</a-doption>
                  <a-doption @click="goTo('/notifications')">消息通知</a-doption>
                  <a-doption @click="handleLogout">退出登录</a-doption>
                </template>
              </a-dropdown>
            </div>
          </template>
          <template v-else>
            <div class="default-layout__guest-actions">
              <RouterLink to="/login" class="default-layout__ghost-entry" @click="mobileMenuOpen = false">登录</RouterLink>
              <RouterLink to="/register" @click="mobileMenuOpen = false">
                <a-button type="primary">立即加入</a-button>
              </RouterLink>
            </div>
          </template>
        </div>
      </div>
    </header>

    <main :class="['default-layout__main', { 'default-layout__main--compose': isComposeRoute }]">
      <RouterView />
    </main>

    <nav v-if="authStore.isLoggedIn" class="default-layout__mobile-tabbar">
      <RouterLink v-for="item in mobileTabbar" :key="item.to" :to="item.to" class="default-layout__mobile-tab" @click="mobileMenuOpen = false">
        <div class="default-layout__mobile-tab-icon">
          <component :is="item.icon" />
          <span v-if="item.badge && item.badge > 0" class="default-layout__mobile-tab-badge">
            {{ item.badge > 99 ? '99+' : item.badge }}
          </span>
        </div>
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>

    <footer class="default-layout__footer">
      <div class="default-layout__footer-inner">
        <div>
          <strong>Community MVP</strong>
          <p>聚焦内容发布、互动交流、用户关系与消息触达的一期社区产品。</p>
        </div>
        <div class="default-layout__footer-meta">
          <span>当前重心：UI 统一与核心页面体验优化</span>
          <span>下一阶段：首页、帖子卡片、个人中心精修</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
// 统一布局层：承载主导航、登录态信息、未读徽标、移动端底栏等跨页面公共能力。
import { computed, onMounted, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconCalendar,
  IconCompass,
  IconEdit,
  IconHistory,
  IconMenu,
  IconMessage,
  IconMore,
  IconNotification,
  IconPlus,
  IconSearch,
  IconUser,
} from '@arco-design/web-vue/es/icon'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useNotificationStore } from '@/stores/notification'
import { resolveAssetUrl } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()
const mobileMenuOpen = ref(false)

// 顶部未读摘要把通知未读与聊天未读合并展示，强化用户对“有新消息”的感知。
const unreadSummary = computed(() => {
  const unread = notificationStore.unread.total + notificationStore.unread.chatCount
  return unread > 99 ? '99+' : unread
})

const displayInitial = computed(() =>
  (authStore.userInfo?.nickname || authStore.userInfo?.username || 'C').slice(0, 1).toUpperCase(),
)

const isComposeRoute = computed(() => route.name === 'post-publish' || route.name === 'post-edit')

const primaryNav = computed(() => {
  const items = [
    { to: '/', label: '首页', icon: IconCompass },
    { to: '/users/search', label: '找人', icon: IconSearch },
  ]

  if (authStore.isLoggedIn) {
    items.push({ to: '/posts/publish', label: '发布', icon: IconPlus })
  }

  return items
})

const featureNav = computed(() => [
  { to: '/drafts', label: '草稿箱', icon: IconEdit, badge: 0 },
  { to: '/histories', label: '浏览历史', icon: IconHistory, badge: 0 },
  { to: '/chat', label: '聊天', icon: IconMessage, badge: notificationStore.unread.chatCount },
  { to: '/notifications', label: '通知', icon: IconNotification, badge: notificationStore.unread.total },
  { to: '/me', label: '我的主页', icon: IconUser, badge: 0 },
])

const mobileTabbar = computed(() => [
  { to: '/', label: '首页', icon: IconCompass, badge: 0 },
  { to: '/users/search', label: '找人', icon: IconSearch, badge: 0 },
  { to: '/posts/publish', label: '发布', icon: IconPlus, badge: 0 },
  { to: '/notifications', label: '通知', icon: IconNotification, badge: notificationStore.unread.total },
  { to: '/me', label: '我的', icon: IconUser, badge: 0 },
])

async function goTo(path: string) {
  mobileMenuOpen.value = false
  await router.push(path)
}

async function handleLogout() {
  mobileMenuOpen.value = false
  notificationStore.reset()
  await authStore.logout()
  Message.success('已退出登录')
  await router.push('/')
}

// 监听登录态：登录后建立聊天连接并刷新未读，退出时主动断开并清空状态。
watch(
  () => authStore.isLoggedIn,
  async (loggedIn) => {
    if (!loggedIn) {
      chatStore.disconnect()
      notificationStore.reset()
      return
    }
    chatStore.connect()
    await notificationStore.refreshUnread().catch(() => undefined)
  },
  { immediate: true },
)

watch(
  () => router.currentRoute.value.fullPath,
  () => {
    mobileMenuOpen.value = false
  },
)

onMounted(async () => {
  if (authStore.isLoggedIn) {
    chatStore.connect()
    await notificationStore.refreshUnread().catch(() => undefined)
  }
})
</script>

<style scoped lang="scss">
.default-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at 8% 0%, rgba(23, 162, 184, 0.18), transparent 32%),
    linear-gradient(135deg, #f7fbf8 0%, #eef4f7 52%, #fbf6ed 100%);
}

.default-layout__header {
  position: sticky;
  top: 0;
  z-index: 40;
  padding: 18px 24px 16px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(18px);
  border-bottom: 1px solid var(--app-border-color-soft);
  box-shadow: 0 10px 36px rgba(15, 23, 42, 0.04);
}

.default-layout__header-inner,
.default-layout__nav-shell,
.default-layout__footer-inner {
  max-width: var(--app-page-width-wide);
  margin: 0 auto;
}

.default-layout__header-inner {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.default-layout__brand-group {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.default-layout__brand {
  display: inline-flex;
  align-items: center;
  gap: 14px;
  color: var(--app-text-1);
  text-decoration: none;
}

.default-layout__brand-copy {
  display: grid;
}

.default-layout__brand-copy strong,
.default-layout__brand-copy small {
  display: block;
}

.default-layout__brand-copy strong {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.1;
}

.default-layout__brand-copy small {
  margin-top: 4px;
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 700;
}

.default-layout__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--app-primary), var(--app-secondary));
  border-radius: 16px;
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.2);
}

.default-layout__slogan {
  max-width: 680px;
  margin: 0;
  color: var(--app-text-3);
  font-size: 13px;
  line-height: 1.7;
}

.default-layout__header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.default-layout__quick-status {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.default-layout__menu-button {
  display: none;
  flex-shrink: 0;
}

.default-layout__nav-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 18px;
  margin-top: 18px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid var(--app-border-color);
  border-radius: 24px;
}

.default-layout__nav-group {
  display: grid;
  gap: 10px;
}

.default-layout__nav-label {
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.default-layout__nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.default-layout__nav-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  color: var(--app-text-2);
  text-decoration: none;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid var(--app-border-color);
  border-radius: 999px;
  transition:
    transform 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.default-layout__nav-link:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.16);
}

.default-layout__nav-link.router-link-active {
  color: var(--app-primary);
  font-weight: 700;
  background: rgba(236, 253, 245, 0.95);
  border-color: rgba(15, 118, 110, 0.18);
  box-shadow: 0 10px 24px rgba(15, 118, 110, 0.08);
}

.default-layout__nav-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  background: #ef4444;
  border-radius: 999px;
}

.default-layout__user-area {
  display: flex;
  align-items: stretch;
}

.default-layout__user-card,
.default-layout__guest-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.84);
  border: 1px solid var(--app-border-color);
  border-radius: 18px;
}

.default-layout__user-card {
  min-width: 260px;
}

.default-layout__user-avatar {
  flex-shrink: 0;
}

.default-layout__user-meta {
  display: grid;
  flex: 1;
  min-width: 0;
}

.default-layout__user-meta strong,
.default-layout__user-meta span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.default-layout__user-meta strong {
  color: var(--app-text-1);
  font-size: 14px;
}

.default-layout__user-meta span {
  color: var(--app-text-3);
  font-size: 12px;
}

.default-layout__user-trigger {
  flex-shrink: 0;
}

.default-layout__ghost-entry {
  color: var(--app-text-2);
  font-weight: 600;
  text-decoration: none;
}

.default-layout__main {
  width: 100%;
  max-width: var(--app-page-width);
  margin: 0 auto;
  padding: 28px 24px 20px;
}

.default-layout__main--compose {
  max-width: min(1760px, calc(100vw - 48px));
}

.default-layout__footer {
  padding: 0 24px 28px;
}

.default-layout__mobile-tabbar {
  display: none;
}

.default-layout__footer-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
  color: var(--app-text-3);
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--app-border-color);
  border-radius: 24px;
}

.default-layout__footer-inner strong {
  display: block;
  color: var(--app-text-1);
  font-size: 16px;
}

.default-layout__footer-inner p {
  margin: 8px 0 0;
  line-height: 1.7;
}

.default-layout__footer-meta {
  display: grid;
  gap: 8px;
  text-align: right;
  font-size: 13px;
}

@media (max-width: 1080px) {
  .default-layout__nav-shell {
    grid-template-columns: 1fr;
  }

  .default-layout__user-area {
    justify-content: flex-start;
  }
}

@media (max-width: 780px) {
  .default-layout__header {
    padding: 16px;
  }

  .default-layout__header-inner {
    align-items: center;
  }

  .default-layout__quick-status {
    display: none;
  }

  .default-layout__menu-button {
    display: inline-flex;
  }

  .default-layout__nav-shell {
    display: none;
    padding: 14px;
  }

  .default-layout__nav-shell--open {
    display: grid;
  }

  .default-layout__nav {
    flex-direction: column;
  }

  .default-layout__nav-link {
    justify-content: space-between;
    width: 100%;
    min-height: 42px;
  }

  .default-layout__user-card,
  .default-layout__guest-actions {
    width: 100%;
  }

  .default-layout__guest-actions {
    justify-content: space-between;
  }

  .default-layout__main {
    padding: 18px 16px 20px;
    padding-bottom: 88px;
  }

  .default-layout__footer {
    padding: 0 16px 24px;
  }

  .default-layout__footer-inner {
    align-items: flex-start;
    flex-direction: column;
    padding: 18px;
  }

  .default-layout__footer-meta {
    text-align: left;
  }

  .default-layout__mobile-tabbar {
    position: fixed;
    right: 12px;
    bottom: 12px;
    left: 12px;
    z-index: 45;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 6px;
    padding: 8px;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(18px);
    border: 1px solid var(--app-border-color);
    border-radius: 20px;
    box-shadow: var(--app-shadow-lg);
  }

  .default-layout__mobile-tab {
    display: grid;
    justify-items: center;
    gap: 4px;
    padding: 8px 4px;
    color: var(--app-text-3);
    font-size: 11px;
    font-weight: 700;
    text-decoration: none;
    border-radius: 14px;
  }

  .default-layout__mobile-tab.router-link-active {
    color: var(--app-primary);
    background: rgba(236, 253, 245, 0.95);
  }

  .default-layout__mobile-tab-icon {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    font-size: 16px;
  }

  .default-layout__mobile-tab-badge {
    position: absolute;
    top: -3px;
    right: -7px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    color: #fff;
    font-size: 10px;
    line-height: 16px;
    text-align: center;
    background: #ef4444;
    border-radius: 999px;
  }
}

@media (min-width: 781px) {
  .default-layout__nav-shell {
    display: grid !important;
  }
}
</style>
