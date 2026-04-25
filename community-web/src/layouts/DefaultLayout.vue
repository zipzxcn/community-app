<template>
  <div class="default-layout">
    <header class="default-layout__header">
      <RouterLink class="default-layout__brand" to="/">Community</RouterLink>
      <nav class="default-layout__nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">发布</RouterLink>
        <RouterLink v-if="authStore.isLoggedIn" to="/drafts">草稿箱</RouterLink>
        <template v-if="authStore.isLoggedIn">
          <span class="default-layout__user">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</span>
          <a-button size="mini" @click="handleLogout">退出</a-button>
        </template>
        <template v-else>
          <RouterLink to="/login">登录</RouterLink>
          <RouterLink to="/register">注册</RouterLink>
        </template>
      </nav>
    </header>

    <main class="default-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  Message.success('已退出登录')
  await router.push('/')
}
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
  padding: 16px 32px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.default-layout__brand {
  font-size: 20px;
  font-weight: 700;
  color: #172033;
  text-decoration: none;
}

.default-layout__nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.default-layout__nav a {
  color: #334155;
  text-decoration: none;
}

.default-layout__nav a.router-link-active {
  color: #0f766e;
  font-weight: 700;
}

.default-layout__user {
  color: #64748b;
}

.default-layout__main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 720px) {
  .default-layout__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 12px;
    padding: 16px 20px;
  }

  .default-layout__nav {
    flex-wrap: wrap;
  }

  .default-layout__main {
    padding: 16px;
  }
}
</style>
