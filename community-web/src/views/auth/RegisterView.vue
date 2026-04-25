<template>
  <div class="auth-page">
    <a-card class="auth-card" title="注册">
      <a-form :model="form" layout="vertical">
        <a-form-item field="username" label="用户名">
          <a-input v-model="form.username" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="nickname" label="昵称">
          <a-input v-model="form.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password v-model="form.password" placeholder="请输入密码" />
        </a-form-item>
        <a-button type="primary" long :loading="submitting" @click="handleRegister">注册</a-button>
      </a-form>
      <p class="auth-tip">已有账号？<RouterLink to="/login">去登录</RouterLink></p>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'

const router = useRouter()
const submitting = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  password: '',
})

async function handleRegister() {
  if (!form.username || !form.nickname || !form.password) {
    Message.warning('请完整填写注册信息')
    return
  }
  submitting.value = true
  try {
    await register(form)
    Message.success('注册成功，请登录')
    await router.push('/login')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '注册失败')
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
