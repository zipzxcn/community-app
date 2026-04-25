<template>
  <div class="auth-page">
    <a-card class="auth-card" title="登录">
      <a-form layout="vertical">
        <a-form-item field="username" label="用户名">
          <a-input placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item field="password" label="密码">
          <a-input-password placeholder="请输入密码" />
        </a-form-item>
        <a-button type="primary" long>登录</a-button>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped lang="scss">
.auth-page {
  display: flex;
  justify-content: center;
  padding-top: 48px;
}

.auth-card {
  width: 420px;
}
</style>
