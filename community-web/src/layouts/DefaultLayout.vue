<template>
  <div class="default-layout">
    <header class="default-layout__header">
      <div class="default-layout__brand-group">
        <RouterLink class="default-layout__brand" to="/">
          <span class="default-layout__brand-mark">C</span>
          <span>
            <strong>Community</strong>
            <small>一期 MVP</small>
          </span>
        </RouterLink>
        <p class="default-layout__slogan">登录、发帖、互动、通知、私信的一期闭环已打通。</p>
      </div>

      <div class="default-layout__actions">
        <nav class="default-layout__nav">
          <RouterLink to="/">首页</RouterLink>
          <RouterLink to="/users/search">找人</RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">发布</RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/drafts">草稿箱</RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/histories">历史</RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/chat" class="default-layout__notice-link">
            聊天
            <span v-if="notificationStore.unread.chatCount > 0" class="default-layout__notice-badge">
              {{ notificationStore.unread.chatCount > 99 ? '99+' : notificationStore.unread.chatCount }}
            </span>
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/notifications" class="default-layout__notice-link">
            通知
            <span v-if="notificationStore.unread.total > 0" class="default-layout__notice-badge">
              {{ notificationStore.unread.total > 99 ? '99+' : notificationStore.unread.total }}
            </span>
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" to="/me">我的</RouterLink>
        </nav>

        <div class="default-layout__user-box">
          <template v-if="authStore.isLoggedIn">
            <div class="default-layout__user-meta">
              <span class="default-layout__user-name">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</span>
              <small>{{ chatStore.connected ? '实时连接正常' : '实时连接中断' }}</small>
            </div>
            <a-button size="mini" @click="handleLogout">退出</a-button>
          </template>
          <template v-else>
            <RouterLink to="/login">登录</RouterLink>
            <RouterLink to="/register">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>

    <main class="default-layout__main">
      <RouterView />
    </main>

    <footer class="default-layout__footer">
      <span>当前阶段：一期功能收尾</span>
      <span>下一阶段：全局 UI 统一、移动端细节、演示与验收材料</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useNotificationStore } from '@/stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const chatStore = useChatStore()
const notificationStore = useNotificationStore()

async function handleLogout() {
  notificationStore.reset()
  await authStore.logout()
  Message.success('已退出登录')
  await router.push('/')
}

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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 32px;
  position: sticky;
  top: 0;
  z-index: 30;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 10px 36px rgba(15, 23, 42, 0.04);
}

.default-layout__brand-group {
  display: grid;
  gap: 6px;
}

.default-layout__brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  color: #172033;
  text-decoration: none;
}

.default-layout__brand strong,
.default-layout__brand small {
  display: block;
}

.default-layout__brand strong {
  font-size: 18px;
  font-weight: 800;
}

.default-layout__brand small {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.default-layout__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  background: linear-gradient(135deg, #0f766e, #2563eb);
  border-radius: 14px;
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.18);
}

.default-layout__slogan {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.default-layout__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.default-layout__nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.default-layout__nav a {
  position: relative;
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 14px;
  color: #334155;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  transition:
    color 0.2s ease,
    border-color 0.2s ease,
    background 0.2s ease,
    transform 0.2s ease;
}

.default-layout__notice-link {
  position: relative;
}

.default-layout__notice-badge {
  position: absolute;
  top: -8px;
  right: -14px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  background: #ef4444;
  border-radius: 999px;
}

.default-layout__nav a.router-link-active {
  color: #0f766e;
  font-weight: 700;
  background: rgba(236, 253, 245, 0.95);
  border-color: rgba(15, 118, 110, 0.18);
  box-shadow: 0 10px 24px rgba(15, 118, 110, 0.08);
}

.default-layout__nav a:hover {
  transform: translateY(-1px);
}

.default-layout__user-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.default-layout__user-meta {
  display: grid;
  text-align: right;
}

.default-layout__user-name {
  color: #172033;
  font-weight: 700;
}

.default-layout__user-meta small {
  color: #64748b;
  font-size: 12px;
}

.default-layout__main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px 20px;
}

.default-layout__footer {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px 20px;
  padding: 0 24px 28px;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 720px) {
  .default-layout__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 16px 20px;
  }

  .default-layout__actions,
  .default-layout__user-box,
  .default-layout__user-meta {
    align-items: flex-start;
    text-align: left;
  }

  .default-layout__actions {
    width: 100%;
    flex-direction: column;
  }

  .default-layout__nav {
    width: 100%;
  }

  .default-layout__nav a {
    height: 34px;
    padding: 0 12px;
  }

  .default-layout__main {
    padding: 16px;
  }
}
</style>
