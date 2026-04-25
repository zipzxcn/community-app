<template>
  <div class="default-layout">
    <header class="default-layout__header">
      <div class="default-layout__brand">Community</div>
      <nav class="default-layout__nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/login">登录</RouterLink>
        <RouterLink to="/register">注册</RouterLink>
      </nav>
    </header>

    <main class="default-layout__main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped lang="scss">
.default-layout {
  min-height: 100vh;
  background: #f4f7fb;
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
}

.default-layout__nav {
  display: flex;
  gap: 16px;
}

.default-layout__nav a {
  color: #334155;
  text-decoration: none;
}

.default-layout__main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}
</style>
