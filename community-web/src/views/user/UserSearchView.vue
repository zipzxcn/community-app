<template>
  <section class="user-search">
    <div class="user-search__header">
      <div>
        <p class="user-search__eyebrow">Discover</p>
        <h1>查找用户</h1>
      </div>
      <a-input-search
        v-model="keyword"
        class="user-search__input"
        placeholder="输入用户名或昵称"
        search-button
        @search="searchFirstPage"
      />
    </div>

    <a-spin :loading="loading">
      <div v-if="users.length" class="user-search__list">
        <article v-for="user in users" :key="user.id" class="user-search__item">
          <RouterLink :to="`/users/${user.id}`" class="user-search__main">
            <a-avatar :size="44">
              <img v-if="user.avatarUrl" :src="resolveAssetUrl(user.avatarUrl)" alt="" />
              <template v-else>{{ (user.nickname || user.username).slice(0, 1).toUpperCase() }}</template>
            </a-avatar>
            <div>
              <h2>{{ user.nickname || user.username }}</h2>
              <p>@{{ user.username }}</p>
            </div>
          </RouterLink>
          <RouterLink :to="`/users/${user.id}`">
            <a-button>主页</a-button>
          </RouterLink>
        </article>
      </div>
      <a-empty v-else description="暂无用户" />
    </a-spin>

    <div v-if="pageState.total > pageState.size" class="user-search__pager">
      <a-pagination
        :current="pageState.page"
        :page-size="pageState.size"
        :total="pageState.total"
        @change="changePage"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Message } from '@arco-design/web-vue'
import { searchUsers } from '@/api/user'
import type { UserSummary } from '@/types/user'
import { resolveAssetUrl } from '@/utils/format'

const keyword = ref('')
const loading = ref(false)
const users = ref<UserSummary[]>([])
const pageState = reactive({ page: 1, size: 20, total: 0 })

async function loadUsers() {
  loading.value = true
  try {
    const result = await searchUsers(keyword.value.trim(), pageState.page, pageState.size)
    users.value = result.list
    pageState.page = result.page
    pageState.size = result.size
    pageState.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '用户搜索失败')
  } finally {
    loading.value = false
  }
}

function searchFirstPage() {
  pageState.page = 1
  loadUsers()
}

function changePage(page: number) {
  pageState.page = page
  loadUsers()
}

loadUsers()
</script>

<style scoped lang="scss">
.user-search {
  display: grid;
  gap: 18px;
}

.user-search__header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 18px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.user-search__eyebrow {
  margin: 0 0 6px;
  color: #0f766e;
  font-weight: 800;
}

.user-search h1 {
  margin: 0;
  color: #172033;
  font-size: 30px;
}

.user-search__input {
  max-width: 420px;
}

.user-search__list {
  display: grid;
  gap: 12px;
}

.user-search__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.user-search__main {
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-decoration: none;
}

.user-search__item h2 {
  margin: 0 0 2px;
  color: #172033;
  font-size: 18px;
}

.user-search__item p {
  margin: 0;
  color: #64748b;
}

.user-search__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .user-search__header,
  .user-search__item {
    align-items: stretch;
    flex-direction: column;
  }

  .user-search__input {
    max-width: none;
  }
}
</style>
