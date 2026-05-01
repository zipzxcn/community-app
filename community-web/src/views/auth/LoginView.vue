<template>
  <section class="auth-page">
    <div class="auth-page__shell">
      <div class="auth-page__hero">
        <p class="auth-page__eyebrow">Welcome Back</p>
        <h1>登录后继续你的社区节奏</h1>
        <p class="auth-page__desc">
          回到帖子流、处理通知消息、继续私信聊天，并在个人中心查看自己的内容、收藏、点赞与关注关系。
        </p>

        <div class="auth-page__feature-list">
          <article class="auth-page__feature-card">
            <strong>内容发现</strong>
            <span>查看最新帖子、热门内容和作者主页</span>
          </article>
          <article class="auth-page__feature-card">
            <strong>互动关系</strong>
            <span>评论、点赞、收藏、关注与互关沟通</span>
          </article>
          <article class="auth-page__feature-card">
            <strong>个人空间</strong>
            <span>管理资料、帖子、草稿、历史与通知</span>
          </article>
        </div>

        <div class="auth-page__tips">
          <span class="app-chip">社区内容流</span>
          <span class="app-chip">消息触达</span>
          <span class="app-chip">创作发布</span>
        </div>
      </div>

      <a-card class="auth-card" :bordered="false">
        <div class="auth-card__head">
          <p class="auth-page__eyebrow">Account Login</p>
          <h2>欢迎回来</h2>
          <span>输入你的账号信息，继续浏览和互动。</span>
        </div>

        <a-form :model="form" layout="vertical" class="auth-card__form">
          <a-form-item field="username" label="用户名">
            <a-input v-model="form.username" placeholder="请输入用户名" allow-clear />
          </a-form-item>
          <a-form-item field="password" label="密码">
            <a-input-password v-model="form.password" placeholder="请输入密码" allow-clear />
          </a-form-item>
          <a-form-item field="captchaCode" label="图形验证码">
            <div class="auth-card__captcha-row">
              <a-input v-model="form.captchaCode" placeholder="请输入验证码" allow-clear />
              <button class="auth-card__captcha-image" type="button" @click="loadCaptcha" v-html="captchaSvg"></button>
              <a-button type="outline" @click="loadCaptcha">换一张</a-button>
            </div>
          </a-form-item>

          <div class="auth-card__submit">
            <a-button type="primary" long size="large" :loading="submitting" @click="handleLogin">登录</a-button>
          </div>
        </a-form>

        <div class="auth-card__footer">
          <span>还没有账号？</span>
          <RouterLink to="/register">立即注册</RouterLink>
        </div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
 import { onMounted, reactive, ref } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { useRoute, useRouter } from 'vue-router'
 import { fetchCaptcha } from '@/api/auth'
  import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)
const captchaSvg = ref('')
const form = reactive({
  username: '',
  password: '',
  captchaId: '',
  captchaCode: '',
})

async function loadCaptcha() {
  try {
    const result = await fetchCaptcha()
    form.captchaId = result.captchaId
    form.captchaCode = ''
    captchaSvg.value = result.captchaSvg
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '验证码加载失败')
  }
}

async function handleLogin() {
  if (!form.username || !form.password || !form.captchaId || !form.captchaCode) {
    Message.warning('请输入用户名、密码和验证码')
    return
  }
  submitting.value = true
  try {
    await authStore.login(form)
    Message.success('登录成功')
    await router.push((route.query.redirect as string) || '/')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '登录失败')
    await loadCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped lang="scss">
.auth-page {
  min-height: calc(100vh - 220px);
  display: grid;
  align-items: center;
}

.auth-page__shell {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(380px, 420px);
  gap: 24px;
  overflow: hidden;
  padding: 24px;
  background:
    radial-gradient(circle at 10% 10%, rgba(15, 118, 110, 0.14), transparent 28%),
    radial-gradient(circle at 85% 15%, rgba(37, 99, 235, 0.12), transparent 22%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.9));
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-xl);
  box-shadow: var(--app-shadow-lg);
}

.auth-page__hero,
.auth-card {
  position: relative;
  z-index: 1;
}

.auth-page__hero {
  display: grid;
  align-content: center;
  gap: 18px;
  padding: 10px 6px 10px 6px;
}

.auth-page__eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.auth-page__hero h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(34px, 5vw, 56px);
  line-height: 1.06;
}

.auth-page__desc {
  max-width: 620px;
  margin: 0;
  color: var(--app-text-3);
  line-height: 1.85;
}

.auth-page__feature-list {
  display: grid;
  gap: 12px;
  max-width: 620px;
}

.auth-page__feature-card {
  display: grid;
  gap: 6px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  box-shadow: var(--app-shadow-xs);
}

.auth-page__feature-card strong {
  color: var(--app-text-1);
  font-size: 15px;
}

.auth-page__feature-card span {
  color: var(--app-text-3);
  font-size: 13px;
  line-height: 1.7;
}

.auth-page__tips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.auth-card {
  align-self: center;
  padding: 10px;
}

.auth-card__head {
  display: grid;
  gap: 6px;
}

.auth-card__head h2,
.auth-card__head span {
  margin: 0;
}

.auth-card__head h2 {
  color: var(--app-text-1);
  font-size: 28px;
}

.auth-card__head span {
  color: var(--app-text-3);
  line-height: 1.7;
}

.auth-card__form {
  margin-top: 20px;
}

.auth-card__submit {
  margin-top: 6px;
}

.auth-card__captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px auto;
  gap: 10px;
  align-items: center;
}

.auth-card__captcha-image {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0;
  cursor: pointer;
  background: #fff;
  border: 1px solid var(--app-border-color);
  border-radius: 10px;
}

.auth-card__captcha-image :deep(svg) {
  width: 132px;
  height: 44px;
  display: block;
}

.auth-card__footer {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 18px;
  color: var(--app-text-3);
  font-size: 14px;
}

.auth-card__footer a {
  color: var(--app-primary);
  font-weight: 700;
  text-decoration: none;
}

@media (max-width: 920px) {
  .auth-page__shell {
    grid-template-columns: 1fr;
  }

  .auth-page__hero {
    padding: 4px 0;
  }
}

@media (max-width: 720px) {
  .auth-page {
    min-height: auto;
  }

  .auth-page__shell {
    padding: 18px;
    border-radius: var(--app-radius-lg);
  }

  .auth-page__hero h1 {
    font-size: clamp(28px, 8vw, 38px);
  }

  .auth-card__captcha-row {
    grid-template-columns: 1fr;
  }
}
</style>
