<template>
  <section class="user-profile">
    <a-spin :loading="loading">
      <div v-if="profile" class="user-profile__hero">
        <div class="user-profile__identity">
          <a-avatar :size="72" class="user-profile__avatar">
            <img v-if="profile.avatarUrl" :src="resolveAssetUrl(profile.avatarUrl)" alt="" />
            <template v-else>{{ (profile.nickname || profile.username).slice(0, 1).toUpperCase() }}</template>
          </a-avatar>
          <div>
            <p class="user-profile__eyebrow">Profile</p>
            <h1>{{ profile.nickname || profile.username }}</h1>
            <p>@{{ profile.username }}</p>
          </div>
        </div>
        <div class="user-profile__actions">
          <a-tag v-if="profile.mutualFollow" color="green">互关</a-tag>
          <a-tag v-else-if="profile.followed" color="arcoblue">已关注</a-tag>
          <RouterLink v-if="canChat" :to="`/chat?userId=${profile.id}`">
            <a-button>发私信</a-button>
          </RouterLink>
          <a-button
            v-if="canFollow"
            :type="profile.followed ? 'secondary' : 'primary'"
            :loading="followLoading"
            @click="toggleFollow"
          >
            {{ profile.followed ? '取消关注' : '关注' }}
          </a-button>
        </div>
      </div>

      <div v-if="profile" class="user-profile__summary">
        <p>{{ profile.bio || '这个用户还没有填写简介。' }}</p>
        <div class="user-profile__stats">
          <span>帖子 {{ profile.postCount }}</span>
          <span>关注 {{ profile.followingCount }}</span>
          <span>粉丝 {{ profile.followerCount }}</span>
        </div>
      </div>

      <div v-if="posts.length" class="user-profile__posts">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
      </div>
      <a-empty v-else-if="!loading" description="暂无公开帖子" />

      <div v-if="pageState.total > pageState.size" class="user-profile__pager">
        <a-pagination
          :current="pageState.page"
          :page-size="pageState.size"
          :total="pageState.total"
          @change="changePage"
        />
      </div>
    </a-spin>
  </section>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import PostCard from '@/components/post/PostCard.vue'
import { followUser, unfollowUser } from '@/api/follow'
import { fetchUserPosts, fetchUserProfile } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { PostListItem } from '@/types/post'
import type { UserProfile } from '@/types/user'
import { resolveAssetUrl } from '@/utils/format'

const props = defineProps<{
  userId: number
}>()

const authStore = useAuthStore()
const loading = ref(false)
const followLoading = ref(false)
const profile = ref<UserProfile | null>(null)
const posts = ref<PostListItem[]>([])
const pageState = reactive({ page: 1, size: 10, total: 0 })
const canFollow = computed(() => authStore.isLoggedIn && authStore.userInfo?.id !== props.userId)
const canChat = computed(() => canFollow.value && profile.value?.mutualFollow)

async function loadProfile() {
  loading.value = true
  try {
    const [profileResult, postResult] = await Promise.all([
      fetchUserProfile(props.userId),
      fetchUserPosts(props.userId, pageState.page, pageState.size),
    ])
    profile.value = profileResult
    posts.value = postResult.list
    pageState.page = postResult.page
    pageState.size = postResult.size
    pageState.total = postResult.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '用户主页加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  if (!profile.value) return
  followLoading.value = true
  try {
    if (profile.value.followed) {
      await unfollowUser(profile.value.id)
      Message.success('已取消关注')
    } else {
      await followUser(profile.value.id)
      Message.success('已关注')
    }
    await loadProfile()
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '关注操作失败')
  } finally {
    followLoading.value = false
  }
}

function changePage(page: number) {
  pageState.page = page
  loadProfile()
}

watch(
  () => props.userId,
  () => {
    pageState.page = 1
    loadProfile()
  },
  { immediate: true },
)
</script>

<style scoped lang="scss">
.user-profile {
  display: grid;
  gap: 18px;
}

.user-profile__hero,
.user-profile__summary {
  padding: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.user-profile__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.user-profile__identity {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile__avatar {
  color: #0f766e;
  font-weight: 800;
  background: #ccfbf1;
}

.user-profile__eyebrow {
  margin: 0 0 6px;
  color: #0f766e;
  font-weight: 800;
}

.user-profile h1 {
  margin: 0 0 4px;
  color: #172033;
  font-size: 32px;
}

.user-profile p {
  margin: 0;
  color: #64748b;
}

.user-profile__actions,
.user-profile__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.user-profile__summary {
  display: grid;
  gap: 14px;
}

.user-profile__stats span {
  padding: 8px 12px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.user-profile__posts {
  display: grid;
  gap: 14px;
}

.user-profile__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 720px) {
  .user-profile__hero {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
