<template>
  <section class="home-view">
    <a-card :bordered="false">
      <template #title>社区首页骨架</template>
      <p>这里放帖子流、标签筛选和搜索入口。</p>
    </a-card>
  </section>
</template>

<style scoped>
.home-view {
  display: grid;
  gap: 16px;
}
</style>
