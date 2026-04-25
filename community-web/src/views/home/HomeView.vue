<template>
  <section class="home-view">
    <div class="home-view__hero">
      <div>
        <p class="home-view__eyebrow">Community MVP</p>
        <h1>发现帖子，加入讨论</h1>
        <p>当前先接入一期后端已完成的帖子列表、搜索、排序、详情与互动接口。</p>
      </div>
      <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">
        <a-button type="primary" size="large">发布帖子</a-button>
      </RouterLink>
      <RouterLink v-else to="/login">
        <a-button type="primary" size="large">登录后发布</a-button>
      </RouterLink>
    </div>

    <a-card :bordered="false" class="home-view__filters">
      <a-input-search v-model="query.keyword" placeholder="搜索标题或摘要" allow-clear @search="reload" @press-enter="reload" />
      <a-select v-model="query.sort" class="home-view__sort" @change="reload">
        <a-option value="latest">最新发布</a-option>
        <a-option value="hot">热门优先</a-option>
      </a-select>
    </a-card>

    <a-spin :loading="loading">
      <div v-if="posts.length" class="home-view__list">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
      </div>
      <a-empty v-else description="暂无帖子，发布第一条内容吧" />
    </a-spin>

    <div v-if="page.total > page.size" class="home-view__pager">
      <a-pagination
        :current="page.page"
        :page-size="page.size"
        :total="page.total"
        @change="changePage"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import PostCard from '@/components/post/PostCard.vue'
import { fetchPosts } from '@/api/post'
import { useAuthStore } from '@/stores/auth'
import type { PostListItem, PostQuery } from '@/types/post'

const authStore = useAuthStore()
const loading = ref(false)
const posts = ref<PostListItem[]>([])
const query = reactive<PostQuery>({
  page: 1,
  size: 10,
  sort: 'latest',
  keyword: '',
})
const page = reactive({
  page: 1,
  size: 10,
  total: 0,
})

async function loadPosts() {
  loading.value = true
  try {
    const result = await fetchPosts({
      ...query,
      keyword: query.keyword?.trim() || undefined,
    })
    posts.value = result.list
    page.page = result.page
    page.size = result.size
    page.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '帖子加载失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  query.page = 1
  loadPosts()
}

function changePage(current: number) {
  query.page = current
  loadPosts()
}

onMounted(loadPosts)
</script>

<style scoped lang="scss">
.home-view {
  display: grid;
  gap: 20px;
}

.home-view__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 34px;
  color: #172033;
  background:
    linear-gradient(135deg, rgba(20, 184, 166, 0.14), rgba(251, 191, 36, 0.18)),
    rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 32px;
}

.home-view__eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.home-view__hero h1 {
  margin: 0 0 10px;
  font-size: clamp(32px, 6vw, 56px);
  line-height: 1;
}

.home-view__hero p:last-child {
  max-width: 560px;
  margin: 0;
  color: #64748b;
  line-height: 1.8;
}

.home-view__filters {
  :deep(.arco-card-body) {
    display: grid;
    grid-template-columns: 1fr 160px;
    gap: 12px;
  }
}

.home-view__list {
  display: grid;
  gap: 16px;
}

.home-view__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .home-view__hero {
    align-items: flex-start;
    flex-direction: column;
    padding: 24px;
  }

  .home-view__filters {
    :deep(.arco-card-body) {
      grid-template-columns: 1fr;
    }
  }
}
</style>
