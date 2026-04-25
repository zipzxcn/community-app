<template>
  <div class="auth-page">
    <a-card class="auth-card" title="登录">
      <a-form :model="form" layout="vertical">
        <a-form-item field="username" label="用户名">
          <a-input v-model="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password v-model="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-button type="primary" long :loading="submitting" @click="handleLogin">登录</a-button>
      </a-form>
      <p class="auth-tip">还没有账号？<RouterLink to="/register">去注册</RouterLink></p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const submitting = ref(false)
const form = reactive({
  username: '',
  password: '',
})

async function handleLogin() {
  if (!form.username || !form.password) {
    Message.warning('请输入用户名和密码')
    return
  }
  submitting.value = true
  try {
    await authStore.login(form)
    Message.success('登录成功')
    await router.push((route.query.redirect as string) || '/')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '登录失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 48px;
}

.auth-card {
  width: 420px;
}

.auth-tip {
  margin: 16px 0 0;
  color: #64748b;
  text-align: center;
}
</style>
