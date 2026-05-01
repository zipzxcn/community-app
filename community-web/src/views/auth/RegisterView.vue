<template>
  <section class="auth-page">
    <div class="auth-page__shell">
      <div class="auth-page__hero">
        <p class="auth-page__eyebrow">Join Community</p>
        <h1>创建账号，开始你的第一条内容</h1>
        <p class="auth-page__desc">
          注册后即可发布帖子、参与评论、建立关注关系，并在个人主页中管理自己的内容、互动记录与消息提醒。
        </p>

        <div class="auth-page__feature-list">
          <article class="auth-page__feature-card">
            <strong>发布内容</strong>
            <span>支持标题、正文、封面、标签、附件与草稿流程</span>
          </article>
          <article class="auth-page__feature-card">
            <strong>建立关系</strong>
            <span>搜索用户、关注作者、查看粉丝与互关好友</span>
          </article>
          <article class="auth-page__feature-card">
            <strong>接收提醒</strong>
            <span>查看通知中心、聊天消息与浏览历史</span>
          </article>
        </div>

        <div class="auth-page__tips">
          <span class="app-chip">发帖创作</span>
          <span class="app-chip">关注互动</span>
          <span class="app-chip">个人主页</span>
        </div>
      </div>

      <a-card class="auth-card" :bordered="false">
        <div class="auth-card__head">
          <p class="auth-page__eyebrow">Create Account</p>
          <h2>开始注册</h2>
          <span>填写基础资料，注册后即可登录进入社区。</span>
        </div>

        <a-form :model="form" layout="vertical" class="auth-card__form">
          <a-form-item field="username" label="用户名">
            <a-input v-model="form.username" placeholder="请输入用户名" allow-clear />
          </a-form-item>
          <a-form-item field="nickname" label="昵称">
            <a-input v-model="form.nickname" placeholder="请输入昵称" allow-clear />
          </a-form-item>
          <a-form-item field="password" label="密码">
            <a-input-password v-model="form.password" placeholder="请输入密码" allow-clear />
          </a-form-item>
          <a-form-item field="captchaCode" label="图形验证码">
            <div class="auth-card__captcha-row">
              <a-input v-model="form.captchaCode" placeholder="请输入验证码" allow-clear />
              <button class="auth-card__captcha-image" type="button" @click="loadCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
              </button>
              <a-button type="outline" @click="loadCaptcha">换一张</a-button>
            </div>
          </a-form-item>

          <div class="auth-card__submit">
            <a-button type="primary" long size="large" :loading="submitting" @click="handleRegister">注册账号</a-button>
          </div>
        </a-form>

        <div class="auth-card__footer">
          <span>已有账号？</span>
          <RouterLink to="/login">立即登录</RouterLink>
        </div>
      </a-card>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * 注册页：创建账号前先做人机校验，减少批量注册风险。
 */
 import { onMounted, reactive, ref } from 'vue'
  import { Message } from '@arco-design/web-vue'
  import { useRouter } from 'vue-router'
 import { fetchCaptcha, register } from '@/api/auth'

const router = useRouter()
const submitting = ref(false)
const captchaImage = ref('')
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  captchaId: '',
  captchaCode: '',
})

// load* 系列方法负责从后端拉取页面初始化数据，统一控制 loading 和错误提示。
// 注册页同样使用图形验证码，降低脚本批量注册接口的成功率。
async function loadCaptcha() {
  try {
    const result = await fetchCaptcha()
    form.captchaId = result.captchaId
    form.captchaCode = ''
    captchaImage.value = result.captchaImageBase64
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '验证码加载失败')
  }
}

// handle* 系列方法通常对应用户点击动作，例如登录、发布、提交、删除。
async function handleRegister() {
  if (!form.username || !form.nickname || !form.password || !form.captchaId || !form.captchaCode) {
    Message.warning('请完整填写注册信息和验证码')
    return
  }
  submitting.value = true
  try {
    await register(form)
    Message.success('注册成功，请登录')
    await router.push('/login')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '注册失败')
    await loadCaptcha()
  } finally {
    submitting.value = false
  }
}

// 首次进入页面时执行初始化逻辑。
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
    radial-gradient(circle at 8% 12%, rgba(15, 118, 110, 0.14), transparent 28%),
    radial-gradient(circle at 85% 15%, rgba(245, 158, 11, 0.12), transparent 24%),
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

.auth-card__captcha-image img {
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
