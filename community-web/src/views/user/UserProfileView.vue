<template>
  <section class="user-profile app-page">
    <a-spin :loading="loading">
      <template v-if="profile">
        <div class="user-profile__hero app-hero">
          <div class="user-profile__hero-main">
            <div class="user-profile__identity">
              <a-avatar :size="82" class="user-profile__avatar">
                <img v-if="profile.avatarUrl" :src="resolveAssetUrl(profile.avatarUrl)" alt="" />
                <template v-else>{{ displayInitial }}</template>
              </a-avatar>

              <div class="user-profile__identity-copy">
                <p class="user-profile__eyebrow">Public Profile</p>
                <h1>{{ profile.nickname || profile.username }}</h1>
                <p>@{{ profile.username }}</p>
                <span>{{ profile.bio || '这个用户还没有填写简介。' }}</span>
              </div>
            </div>

            <div class="app-stat-grid">
              <article class="app-stat-card">
                <strong>{{ profile.postCount }}</strong>
                <span>公开帖子</span>
                <small>当前可见内容数量</small>
              </article>
              <article class="app-stat-card">
                <strong>{{ profile.followingCount }}</strong>
                <span>TA 的关注</span>
                <small>正在持续关注的作者与用户</small>
              </article>
              <article class="app-stat-card">
                <strong>{{ profile.followerCount }}</strong>
                <span>TA 的粉丝</span>
                <small>已建立关注关系的用户数量</small>
              </article>
            </div>
          </div>

          <div class="user-profile__side">
            <div class="app-panel user-profile__action-card">
              <p class="user-profile__eyebrow">Relationship</p>
              <h3>与 TA 建立连接</h3>
              <div class="user-profile__status">
                <a-tag v-if="profile.mutualFollow" color="green">互关好友</a-tag>
                <a-tag v-else-if="profile.followed" color="arcoblue">已关注</a-tag>
                <a-tag v-else color="gray">暂未关注</a-tag>
              </div>

              <div class="user-profile__actions">
                <RouterLink v-if="canChat" :to="`/chat?userId=${profile.id}`">
                  <a-button long>发私信</a-button>
                </RouterLink>
                <a-button
                  v-if="canFollow"
                  :type="profile.followed ? 'secondary' : 'primary'"
                  long
                  :loading="followLoading"
                  @click="toggleFollow"
                >
                  {{ profile.followed ? '取消关注' : '关注 TA' }}
                </a-button>
                <RouterLink to="/users/search">
                  <a-button long>继续找人</a-button>
                </RouterLink>
              </div>
            </div>
          </div>
        </div>

        <div class="user-profile__summary-grid">
          <section class="app-panel user-profile__summary-card">
            <p class="user-profile__eyebrow">Profile Summary</p>
            <h3>个人简介</h3>
            <p>{{ profile.bio || '这个用户还没有填写简介。' }}</p>
          </section>

          <section class="app-panel user-profile__summary-card">
            <p class="user-profile__eyebrow">Quick Stats</p>
            <h3>关系概览</h3>
            <div class="user-profile__stats">
              <span class="app-chip">帖子 {{ profile.postCount }}</span>
              <span class="app-chip">关注 {{ profile.followingCount }}</span>
              <span class="app-chip">粉丝 {{ profile.followerCount }}</span>
            </div>
          </section>
        </div>

        <div class="app-section-head">
          <div class="app-section-head__main">
            <p class="app-section-head__eyebrow">Posts</p>
            <h2 class="app-section-head__title">TA 的公开内容</h2>
            <p class="app-section-head__desc">继续浏览 TA 已经公开发布的帖子，查看创作内容与互动热度。</p>
          </div>
          <strong class="app-section-head__value">{{ pageState.total }}</strong>
        </div>

        <div v-if="posts.length" class="user-profile__posts">
          <PostCard v-for="post in posts" :key="post.id" :post="post" />
        </div>
        <div v-else-if="!loading" class="app-empty-state">
          <p class="app-empty-state__eyebrow">Profile Posts</p>
          <h3 class="app-empty-state__title">这个用户还没有公开帖子</h3>
          <p class="app-empty-state__desc">你可以先关注对方、继续浏览其他用户，或者回到首页看看当前社区里还有哪些内容。</p>
          <div class="app-empty-state__actions">
            <RouterLink to="/">
              <a-button type="primary">回到首页</a-button>
            </RouterLink>
            <RouterLink to="/users/search">
              <a-button>继续找人</a-button>
            </RouterLink>
          </div>
        </div>

        <div v-if="pageState.total > pageState.size" class="user-profile__pager">
          <a-pagination :current="pageState.page" :page-size="pageState.size" :total="pageState.total" @change="changePage" />
        </div>
      </template>
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
const displayInitial = computed(() => (profile.value?.nickname || profile.value?.username || 'U').slice(0, 1).toUpperCase())

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
.user-profile__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.9fr);
  gap: 24px;
}

.user-profile__hero-main {
  display: grid;
  gap: 20px;
}

.user-profile__identity {
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

.user-profile__avatar {
  flex-shrink: 0;
  color: var(--app-primary);
  font-weight: 800;
  background: linear-gradient(135deg, rgba(204, 251, 241, 0.92), rgba(219, 234, 254, 0.92));
}

.user-profile__identity-copy {
  display: grid;
  gap: 8px;
}

.user-profile__eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.user-profile__identity-copy h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(30px, 5vw, 44px);
  line-height: 1.08;
}

.user-profile__identity-copy p,
.user-profile__identity-copy span {
  margin: 0;
  color: var(--app-text-3);
}

.user-profile__identity-copy span {
  line-height: 1.8;
}

.user-profile__action-card {
  display: grid;
  gap: 14px;
  align-content: start;
}

.user-profile__action-card h3,
.user-profile__action-card p {
  margin: 0;
}

.user-profile__action-card h3 {
  color: var(--app-text-1);
  font-size: 22px;
}

.user-profile__status {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.user-profile__actions {
  display: grid;
  gap: 10px;
}

.user-profile__summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.user-profile__summary-card {
  display: grid;
  gap: 10px;
}

.user-profile__summary-card h3,
.user-profile__summary-card p {
  margin: 0;
}

.user-profile__summary-card h3 {
  color: var(--app-text-1);
  font-size: 20px;
}

.user-profile__summary-card p:last-child {
  color: var(--app-text-3);
  line-height: 1.8;
}

.user-profile__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.user-profile__posts {
  display: grid;
  gap: 14px;
}

.user-profile__pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 1080px) {
  .user-profile__hero,
  .user-profile__summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .user-profile__identity {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-profile__identity-copy h1 {
    font-size: clamp(26px, 8vw, 34px);
  }

  .user-profile__actions .arco-btn {
    min-height: 38px;
  }
}
</style>
