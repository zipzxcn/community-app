<template>
  <section class="user-center app-page">
    <div class="user-center__hero app-hero">
      <div class="user-center__hero-main">
        <div class="user-center__identity">
          <a-avatar :size="76" class="user-center__avatar">
            <img v-if="profileForm.avatarUrl" :src="resolveAssetUrl(profileForm.avatarUrl)" alt="" />
            <template v-else>{{ avatarText }}</template>
          </a-avatar>

          <div class="user-center__identity-copy">
            <p class="user-center__eyebrow">Personal Console</p>
            <h1>{{ displayName }}</h1>
            <p>{{ profileForm.bio || '完善你的资料、内容和关系网络，让更多人更快认识你。' }}</p>

            <div class="user-center__identity-chips">
              <span class="app-chip">我的帖子 {{ profileStats.postCount }}</span>
              <span class="app-chip">我的关注 {{ profileStats.followingCount }}</span>
              <span class="app-chip">我的粉丝 {{ profileStats.followerCount }}</span>
            </div>
          </div>
        </div>

        <div class="app-stat-grid">
          <article class="app-stat-card">
            <strong>{{ profileStats.postCount }}</strong>
            <span>内容产出</span>
            <small>已发布与可管理的帖子数量</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ profileStats.followingCount }}</strong>
            <span>主动关注</span>
            <small>你正在持续关注的用户</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ profileStats.followerCount }}</strong>
            <span>被关注量</span>
            <small>对你内容感兴趣的用户</small>
          </article>
        </div>
      </div>

      <div class="user-center__hero-side">
        <div class="app-panel user-center__quick-card">
          <p class="user-center__eyebrow">Quick Actions</p>
          <h3>快速管理你的社区空间</h3>
          <div class="user-center__quick-actions">
            <RouterLink to="/posts/publish">
              <a-button type="primary" long>发布新帖子</a-button>
            </RouterLink>
            <RouterLink to="/drafts">
              <a-button long>查看草稿箱</a-button>
            </RouterLink>
            <RouterLink to="/histories">
              <a-button long>查看浏览历史</a-button>
            </RouterLink>
          </div>
        </div>
      </div>
    </div>

    <a-card :bordered="false" class="user-center__panel app-panel">
      <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
        <a-tab-pane key="profile" title="资料设置" />
        <a-tab-pane key="posts" title="我的帖子" />
        <a-tab-pane key="favorites" title="我的收藏" />
        <a-tab-pane key="likes" title="我的点赞" />
        <a-tab-pane key="following" title="我的关注" />
        <a-tab-pane key="followers" title="我的粉丝" />
        <a-tab-pane key="mutual" title="互关好友" />
      </a-tabs>

      <a-spin :loading="loading">
        <a-form v-if="activeTab === 'profile'" :model="profileForm" layout="vertical" class="user-center__form">
          <div class="app-section-head">
            <div class="app-section-head__main">
              <p class="app-section-head__eyebrow">Profile Settings</p>
              <h2 class="app-section-head__title">维护你的昵称、头像和个人简介</h2>
              <p class="app-section-head__desc">资料页是别人了解你的第一入口，建议保持清晰、完整、有辨识度。</p>
            </div>
          </div>

          <div class="user-center__form-grid">
            <div class="user-center__profile-preview app-panel">
              <a-avatar :size="84" class="user-center__avatar">
                <img v-if="profileForm.avatarUrl" :src="resolveAssetUrl(profileForm.avatarUrl)" alt="" />
                <template v-else>{{ avatarText }}</template>
              </a-avatar>
              <div>
                <strong>{{ displayName }}</strong>
                <p>@{{ authStore.userInfo?.username }}</p>
                <span>{{ profileForm.bio || '完善个人简介后，这里会展示你的自我介绍。' }}</span>
              </div>
            </div>

            <div class="user-center__form-fields">
              <a-form-item field="nickname" label="昵称">
                <a-input v-model="profileForm.nickname" :max-length="32" show-word-limit />
              </a-form-item>
              <a-form-item field="avatarUrl" label="头像">
                <div class="user-center__avatar-editor">
                  <a-input v-model="profileForm.avatarUrl" placeholder="头像 URL" allow-clear />
                  <label class="user-center__upload">
                    <input type="file" accept="image/*" @change="handleAvatarSelected" />
                    <span>{{ uploadingAvatar ? '上传中' : '上传图片' }}</span>
                  </label>
                </div>
              </a-form-item>
              <a-form-item field="bio" label="个人简介">
                <a-textarea v-model="profileForm.bio" :max-length="255" show-word-limit :auto-size="{ minRows: 4, maxRows: 6 }" />
              </a-form-item>
              <div class="user-center__actions">
                <a-button type="primary" :loading="savingProfile" @click="saveProfile">保存资料</a-button>
              </div>
            </div>
          </div>
        </a-form>

        <template v-else-if="isPostTab">
          <div class="app-section-head">
            <div class="app-section-head__main">
              <p class="app-section-head__eyebrow">{{ currentPostMeta.eyebrow }}</p>
              <h2 class="app-section-head__title">{{ currentPostMeta.title }}</h2>
              <p class="app-section-head__desc">{{ currentPostMeta.description }}</p>
            </div>
            <strong class="app-section-head__value">{{ currentPage.total }}</strong>
          </div>

          <div v-if="activeTab === 'posts'" class="user-center__post-filter">
            <a-radio-group v-model="postStatusFilter" type="button">
              <a-radio value="ALL">全部</a-radio>
              <a-radio value="PUBLISHED">已发布</a-radio>
              <a-radio value="HIDDEN">已下架</a-radio>
            </a-radio-group>
          </div>

          <div v-if="currentPosts.length" class="user-center__post-list">
            <article v-for="post in currentPosts" :key="post.id" class="user-center__post-item">
              <PostCard :post="post" />
              <div class="user-center__post-actions">
                <template v-if="activeTab === 'posts'">
                  <a-tag :color="post.status === 'HIDDEN' ? 'orange' : 'green'">
                    {{ post.status === 'HIDDEN' ? '已下架' : '已发布' }}
                  </a-tag>
                  <RouterLink v-if="post.status === 'PUBLISHED'" :to="`/posts/${post.id}/edit`">
                    <a-button size="small">编辑</a-button>
                  </RouterLink>
                  <a-button size="small" @click="togglePostHidden(post)">
                    {{ post.status === 'HIDDEN' ? '恢复上架' : '下架' }}
                  </a-button>
                  <a-button size="small" status="danger" @click="removePost(post.id)">删除</a-button>
                </template>
                <template v-else>
                  <RouterLink :to="`/posts/${post.id}`">
                    <a-button size="small">查看详情</a-button>
                  </RouterLink>
                  <a-button
                    v-if="activeTab === 'favorites'"
                    size="small"
                    status="danger"
                    :loading="postActionId === post.id"
                    @click="toggleFavorite(post)"
                  >
                    取消收藏
                  </a-button>
                  <a-button
                    v-if="activeTab === 'likes'"
                    size="small"
                    status="danger"
                    :loading="postActionId === post.id"
                    @click="toggleLike(post)"
                  >
                    取消点赞
                  </a-button>
                </template>
              </div>
            </article>
          </div>
          <div v-else class="app-empty-state">
            <p class="app-empty-state__eyebrow">{{ currentPostMeta.eyebrow }}</p>
            <h3 class="app-empty-state__title">{{ currentPostMeta.title }}</h3>
            <p class="app-empty-state__desc">{{ currentPostMeta.emptyText }}</p>
            <div class="app-empty-state__actions">
              <RouterLink v-if="activeTab === 'posts'" to="/posts/publish">
                <a-button type="primary">去发布</a-button>
              </RouterLink>
              <RouterLink v-else to="/">
                <a-button>回到首页</a-button>
              </RouterLink>
            </div>
          </div>
          <div v-if="currentPage.total > currentPage.size" class="user-center__pager">
            <a-pagination :current="currentPage.page" :page-size="currentPage.size" :total="currentPage.total" @change="changePostPage" />
          </div>
        </template>

        <template v-else>
          <div class="app-section-head">
            <div class="app-section-head__main">
              <p class="app-section-head__eyebrow">{{ currentUserMeta.eyebrow }}</p>
              <h2 class="app-section-head__title">{{ currentUserMeta.title }}</h2>
              <p class="app-section-head__desc">{{ currentUserMeta.description }}</p>
            </div>
            <strong class="app-section-head__value">{{ currentUserPage.total }}</strong>
          </div>

          <div v-if="currentUsers.length" class="user-center__user-list">
            <article v-for="user in currentUsers" :key="user.id" class="user-center__user-item">
              <RouterLink :to="`/users/${user.id}`" class="user-center__user-main">
                <a-avatar :size="48">
                  <img v-if="user.avatarUrl" :src="resolveAssetUrl(user.avatarUrl)" alt="" />
                  <template v-else>{{ (user.nickname || user.username).slice(0, 1).toUpperCase() }}</template>
                </a-avatar>
                <div class="user-center__user-copy">
                  <h3>{{ user.nickname || user.username }}</h3>
                  <p>@{{ user.username }}</p>
                </div>
              </RouterLink>
              <div class="user-center__user-actions">
                <a-tag v-if="user.mutualFollow" color="green">互关</a-tag>
                <a-tag v-else-if="user.followedByMe" color="arcoblue">已关注</a-tag>
                <RouterLink v-if="user.mutualFollow" :to="`/chat?userId=${user.id}`">
                  <a-button size="small">发私信</a-button>
                </RouterLink>
                <a-button v-else-if="activeTab === 'followers'" size="small" type="primary" @click="followBack(user.id)">回关</a-button>
              </div>
            </article>
          </div>
          <div v-else class="app-empty-state">
            <p class="app-empty-state__eyebrow">{{ currentUserMeta.eyebrow }}</p>
            <h3 class="app-empty-state__title">{{ currentUserMeta.title }}</h3>
            <p class="app-empty-state__desc">{{ emptyUserText }}</p>
            <div class="app-empty-state__actions">
              <RouterLink to="/users/search">
                <a-button type="primary">去找人</a-button>
              </RouterLink>
            </div>
          </div>
          <div v-if="currentUserPage.total > currentUserPage.size" class="user-center__pager">
            <a-pagination :current="currentUserPage.page" :page-size="currentUserPage.size" :total="currentUserPage.total" @change="changeUserPage" />
          </div>
        </template>
      </a-spin>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { useRoute, useRouter } from 'vue-router'
import PostCard from '@/components/post/PostCard.vue'
import { uploadImageFile } from '@/api/file'
import { fetchFollowers, fetchFollowing, fetchMutualFollows, followUser } from '@/api/follow'
import { deletePost, hidePost, unlikePost, unfavoritePost } from '@/api/post'
import { fetchUserFavorites, fetchUserLikes, fetchUserPosts, fetchUserProfile, updateMyProfile } from '@/api/user'
import { useAuthStore } from '@/stores/auth'
import type { FollowUser } from '@/types/follow'
import type { PostListItem } from '@/types/post'
import { resolveAssetUrl } from '@/utils/format'

type CenterTab = 'profile' | 'posts' | 'favorites' | 'likes' | 'following' | 'followers' | 'mutual'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const activeTab = ref<CenterTab>('profile')
const loading = ref(false)
const savingProfile = ref(false)
const uploadingAvatar = ref(false)
const postActionId = ref<number | null>(null)
const postStatusFilter = ref<'ALL' | 'PUBLISHED' | 'HIDDEN'>('ALL')
const myPosts = ref<PostListItem[]>([])
const myFavorites = ref<PostListItem[]>([])
const myLikes = ref<PostListItem[]>([])
const followingUsers = ref<FollowUser[]>([])
const followerUsers = ref<FollowUser[]>([])
const mutualUsers = ref<FollowUser[]>([])
const postPage = reactive({ page: 1, size: 10, total: 0 })
const favoritePage = reactive({ page: 1, size: 10, total: 0 })
const likePage = reactive({ page: 1, size: 10, total: 0 })
const followingPage = reactive({ page: 1, size: 20, total: 0 })
const followerPage = reactive({ page: 1, size: 20, total: 0 })
const mutualPage = reactive({ page: 1, size: 20, total: 0 })
const profileForm = reactive({
  nickname: authStore.userInfo?.nickname || '',
  avatarUrl: authStore.userInfo?.avatarUrl || '',
  bio: authStore.userInfo?.bio || '',
})
const profileStats = reactive({
  postCount: 0,
  followerCount: 0,
  followingCount: 0,
})

const displayName = computed(() => profileForm.nickname || authStore.userInfo?.username || '我的主页')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const isPostTab = computed(() => ['posts', 'favorites', 'likes'].includes(activeTab.value))
const currentPosts = computed(() => {
  if (activeTab.value === 'favorites') return myFavorites.value
  if (activeTab.value === 'likes') return myLikes.value
  if (activeTab.value !== 'posts') return myPosts.value
  if (postStatusFilter.value === 'ALL') return myPosts.value
  return myPosts.value.filter((post) => post.status === postStatusFilter.value)
})
const currentPage = computed(() => {
  if (activeTab.value === 'favorites') return favoritePage
  if (activeTab.value === 'likes') return likePage
  return postPage
})
const currentUsers = computed(() => {
  if (activeTab.value === 'followers') return followerUsers.value
  if (activeTab.value === 'mutual') return mutualUsers.value
  return followingUsers.value
})
const currentUserPage = computed(() => {
  if (activeTab.value === 'followers') return followerPage
  if (activeTab.value === 'mutual') return mutualPage
  return followingPage
})
const currentPostMeta = computed(() => {
  if (activeTab.value === 'favorites') {
    return {
      eyebrow: 'Favorites',
      title: '我的收藏',
      description: '收藏过的帖子会集中在这里，方便回看和继续互动。',
      emptyText: '你还没有收藏任何帖子',
    }
  }
  if (activeTab.value === 'likes') {
    return {
      eyebrow: 'Likes',
      title: '我的点赞',
      description: '这里汇总你点过赞的帖子，方便快速回访。',
      emptyText: '你还没有点赞任何帖子',
    }
  }
  return {
    eyebrow: 'Posts',
    title: '我的帖子',
    description: '管理自己发布的帖子，支持编辑、下架和删除。',
    emptyText: '你还没有发布任何帖子',
  }
})
const currentUserMeta = computed(() => {
  if (activeTab.value === 'followers') {
    return {
      eyebrow: 'Followers',
      title: '我的粉丝',
      description: '关注你的人会显示在这里。',
    }
  }
  if (activeTab.value === 'mutual') {
    return {
      eyebrow: 'Mutual',
      title: '互关好友',
      description: '双方互相关注的用户，适合继续互动和私聊。',
    }
  }
  return {
    eyebrow: 'Following',
    title: '我的关注',
    description: '你主动关注的用户列表。',
  }
})
const emptyUserText = computed(() => {
  if (activeTab.value === 'followers') return '暂无粉丝'
  if (activeTab.value === 'mutual') return '暂无互关好友'
  return '暂无关注用户'
})

function getCurrentUserId() {
  const id = authStore.userInfo?.id
  if (!id) {
    throw new Error('未获取到用户信息，请重新登录')
  }
  return id
}

async function saveProfile() {
  savingProfile.value = true
  try {
    const profile = await updateMyProfile({
      nickname: profileForm.nickname.trim(),
      avatarUrl: profileForm.avatarUrl.trim(),
      bio: profileForm.bio.trim(),
    })
    profileStats.postCount = profile.postCount
    profileStats.followerCount = profile.followerCount
    profileStats.followingCount = profile.followingCount
    authStore.setUserInfo({
      id: profile.id,
      username: profile.username,
      nickname: profile.nickname,
      avatarUrl: profile.avatarUrl,
      bio: profile.bio,
    })
    Message.success('资料已保存')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '资料保存失败')
  } finally {
    savingProfile.value = false
  }
}

async function handleAvatarSelected(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) {
    Message.warning('请选择图片文件')
    return
  }
  uploadingAvatar.value = true
  try {
    const uploaded = await uploadImageFile(file, 'AVATAR')
    profileForm.avatarUrl = uploaded.accessUrl
    Message.success('头像已上传')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function loadPosts(tab: CenterTab) {
  loading.value = true
  try {
    const userId = getCurrentUserId()
    if (tab === 'posts') {
      const result = await fetchUserPosts(userId, postPage.page, postPage.size)
      myPosts.value = result.list
      postPage.page = result.page
      postPage.size = result.size
      postPage.total = result.total
      profileStats.postCount = result.total
    } else if (tab === 'favorites') {
      const result = await fetchUserFavorites(userId, favoritePage.page, favoritePage.size)
      myFavorites.value = result.list
      favoritePage.page = result.page
      favoritePage.size = result.size
      favoritePage.total = result.total
    } else if (tab === 'likes') {
      const result = await fetchUserLikes(userId, likePage.page, likePage.size)
      myLikes.value = result.list
      likePage.page = result.page
      likePage.size = result.size
      likePage.total = result.total
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadOwnProfile() {
  try {
    const profile = await fetchUserProfile(getCurrentUserId())
    profileForm.nickname = profile.nickname
    profileForm.avatarUrl = profile.avatarUrl || ''
    profileForm.bio = profile.bio || ''
    profileStats.postCount = profile.postCount
    profileStats.followerCount = profile.followerCount
    profileStats.followingCount = profile.followingCount
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '个人资料加载失败')
  }
}

async function loadUsers(tab: CenterTab) {
  loading.value = true
  try {
    if (tab === 'following') {
      const result = await fetchFollowing(followingPage.page, followingPage.size)
      followingUsers.value = result.list
      followingPage.page = result.page
      followingPage.size = result.size
      followingPage.total = result.total
      profileStats.followingCount = result.total
    } else if (tab === 'followers') {
      const result = await fetchFollowers(followerPage.page, followerPage.size)
      followerUsers.value = result.list
      followerPage.page = result.page
      followerPage.size = result.size
      followerPage.total = result.total
      profileStats.followerCount = result.total
    } else if (tab === 'mutual') {
      const result = await fetchMutualFollows(mutualPage.page, mutualPage.size)
      mutualUsers.value = result.list
      mutualPage.page = result.page
      mutualPage.size = result.size
      mutualPage.total = result.total
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

async function followBack(userId: number) {
  try {
    await followUser(userId)
    Message.success('已关注')
    await loadUsers(activeTab.value)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '关注失败')
  }
}

async function toggleFavorite(post: PostListItem) {
  postActionId.value = post.id
  try {
    await unfavoritePost(post.id)
    Message.success('已取消收藏')
    await loadPosts('favorites')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '取消收藏失败')
  } finally {
    postActionId.value = null
  }
}

async function toggleLike(post: PostListItem) {
  postActionId.value = post.id
  try {
    await unlikePost(post.id)
    Message.success('已取消点赞')
    await loadPosts('likes')
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '取消点赞失败')
  } finally {
    postActionId.value = null
  }
}

function togglePostHidden(post: PostListItem) {
  const nextHidden = post.status !== 'HIDDEN'
  Modal.confirm({
    title: nextHidden ? '下架帖子' : '恢复上架',
    content: nextHidden ? '下架后公开列表将不可见。' : '恢复上架后帖子会重新公开。',
    async onOk() {
      try {
        await hidePost(post.id, nextHidden)
        Message.success(nextHidden ? '帖子已下架' : '帖子已恢复上架')
        await loadPosts('posts')
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '帖子状态更新失败')
      }
    },
  })
}

function removePost(postId: number) {
  Modal.confirm({
    title: '删除帖子',
    content: '删除后帖子将从列表移除。',
    async onOk() {
      try {
        await deletePost(postId)
        Message.success('帖子已删除')
        await loadPosts('posts')
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '删除失败')
      }
    },
  })
}

async function handleTabChange(key: string | number) {
  const tab = String(key) as CenterTab
  activeTab.value = tab
  await router.replace({
    query: tab === 'profile' ? {} : { tab },
  })
  if (tab === 'profile') return
  if (['following', 'followers', 'mutual'].includes(tab)) {
    await loadUsers(tab)
    return
  }
  await loadPosts(tab)
}

function changePostPage(page: number) {
  const tab = activeTab.value
  if (tab === 'posts') postPage.page = page
  if (tab === 'favorites') favoritePage.page = page
  if (tab === 'likes') likePage.page = page
  loadPosts(tab)
}

function getInitialTab() {
  const raw = route.query.tab
  const value = Array.isArray(raw) ? raw[0] : raw
  const allowed: CenterTab[] = ['profile', 'posts', 'favorites', 'likes', 'following', 'followers', 'mutual']
  return allowed.includes(value as CenterTab) ? (value as CenterTab) : 'posts'
}

function changeUserPage(page: number) {
  const tab = activeTab.value
  if (tab === 'following') followingPage.page = page
  if (tab === 'followers') followerPage.page = page
  if (tab === 'mutual') mutualPage.page = page
  loadUsers(tab)
}

onMounted(async () => {
  await loadOwnProfile()
  const tab = getInitialTab()
  activeTab.value = tab
  if (tab === 'profile') return
  if (['following', 'followers', 'mutual'].includes(tab)) {
    await loadUsers(tab)
    return
  }
  await loadPosts(tab)
})
</script>

<style scoped lang="scss">
.user-center {
  width: 100%;
  max-width: 100%;
  overflow-x: clip;
}

.user-center__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.9fr);
  gap: 24px;
  width: 100%;
  max-width: 100%;
}

.user-center__hero-main {
  display: grid;
  gap: 20px;
  min-width: 0;
}

.user-center__identity {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  min-width: 0;
}

.user-center__avatar {
  flex-shrink: 0;
  color: var(--app-primary);
  font-weight: 800;
  background: linear-gradient(135deg, rgba(204, 251, 241, 0.92), rgba(219, 234, 254, 0.92));
}

.user-center__identity-copy {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.user-center__eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.user-center__identity-copy h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(30px, 5vw, 44px);
  line-height: 1.08;
}

.user-center__identity-copy > p:last-of-type {
  margin: 0;
  color: var(--app-text-3);
  line-height: 1.8;
}

.user-center__identity-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-width: 0;
}

.user-center__hero-side {
  display: grid;
  min-width: 0;
}

.user-center__quick-card {
  align-content: start;
  gap: 14px;
  min-width: 0;
}

.user-center__quick-card h3,
.user-center__quick-card p {
  margin: 0;
}

.user-center__quick-card h3 {
  color: var(--app-text-1);
  font-size: 22px;
}

.user-center__quick-card p:last-of-type {
  color: var(--app-text-3);
}

.user-center__quick-actions {
  display: grid;
  gap: 10px;
}

.user-center__panel {
  padding-top: 18px;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.user-center__panel :deep(.arco-card-body) {
  padding: 0;
  min-width: 0;
  overflow: hidden;
}

.user-center__panel :deep(.arco-tabs) {
  min-width: 0;
}

.user-center__panel :deep(.arco-tabs-nav) {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 6px;
  scrollbar-width: thin;
}

.user-center__panel :deep(.arco-tabs-nav-tab-list) {
  flex-wrap: nowrap;
  width: auto;
  min-width: max-content;
}

.user-center__panel :deep(.arco-tabs-content) {
  min-width: 0;
  max-width: 100%;
  overflow-x: hidden;
}

.user-center__panel :deep(.arco-tabs-content-list),
.user-center__panel :deep(.arco-tabs-content-item) {
  min-width: 0;
  max-width: 100%;
}

.user-center__form {
  display: grid;
  gap: 18px;
  margin-top: 12px;
}

.user-center__form-grid {
  display: grid;
  grid-template-columns: minmax(240px, 320px) minmax(0, 1fr);
  gap: 18px;
  min-width: 0;
}

.user-center__profile-preview {
  display: grid;
  gap: 14px;
  align-content: start;
  min-width: 0;
}

.user-center__profile-preview strong,
.user-center__profile-preview p,
.user-center__profile-preview span {
  display: block;
}

.user-center__profile-preview strong {
  color: var(--app-text-1);
  font-size: 18px;
}

.user-center__profile-preview p {
  margin: 4px 0 0;
  color: var(--app-text-3);
}

.user-center__profile-preview span {
  margin-top: 8px;
  color: var(--app-text-3);
  line-height: 1.75;
}

.user-center__form-fields {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.user-center__avatar-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  min-width: 0;
}

.user-center__upload {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 108px;
  height: 36px;
  color: var(--app-primary);
  cursor: pointer;
  background: rgba(236, 253, 245, 0.95);
  border: 1px solid rgba(15, 118, 110, 0.24);
  border-radius: 10px;
}

.user-center__upload input {
  position: absolute;
  inset: 0;
  width: 100%;
  opacity: 0;
}

.user-center__actions {
  display: flex;
  justify-content: flex-end;
}

.user-center__post-filter,
.user-center__post-list,
.user-center__user-list {
  margin-top: 16px;
  min-width: 0;
}

.user-center__post-list,
.user-center__user-list {
  display: grid;
  gap: 14px;
}

.user-center__post-item {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.user-center__post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.user-center__user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
  box-shadow: var(--app-shadow-xs);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.user-center__user-item:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.14);
  box-shadow: var(--app-shadow-sm);
}

.user-center__user-main {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 12px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.user-center__user-copy {
  min-width: 0;
}

.user-center__user-copy h3,
.user-center__user-copy p {
  margin: 0;
}

.user-center__user-copy h3 {
  color: var(--app-text-1);
  font-size: 16px;
}

.user-center__user-copy p {
  margin-top: 4px;
  color: var(--app-text-3);
}

.user-center__user-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.user-center__pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

@media (max-width: 1080px) {
  .user-center__hero,
  .user-center__form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .user-center__identity,
  .user-center__user-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-center__hero {
    gap: 18px;
  }

  .user-center__identity {
    gap: 14px;
  }

  .user-center__avatar-editor {
    grid-template-columns: 1fr;
  }

  .user-center__panel :deep(.arco-tabs-nav) {
    margin-inline: -4px;
    padding-inline: 4px;
  }

  .user-center__panel :deep(.arco-tabs-tab) {
    white-space: nowrap;
  }

  .user-center__post-filter :deep(.arco-radio-group) {
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
    max-width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 4px;
    scrollbar-width: thin;
  }

  .user-center__post-filter :deep(.arco-radio) {
    flex-shrink: 0;
  }

  .user-center__actions,
  .user-center__user-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .user-center__identity-copy h1 {
    font-size: clamp(26px, 8vw, 34px);
  }

  .user-center__identity-copy > p:last-of-type {
    line-height: 1.7;
  }

  .user-center__identity-chips {
    gap: 10px;
  }

  .user-center__identity-chips .app-chip {
    max-width: 100%;
    padding-inline: 11px;
    font-size: 12px;
  }

  .user-center__quick-card h3 {
    font-size: 18px;
    line-height: 1.3;
  }

  .user-center__quick-actions :deep(.arco-btn) {
    min-height: 40px;
  }

  .user-center__post-actions .arco-btn,
  .user-center__user-actions .arco-btn {
    min-height: 36px;
  }

  .user-center__post-actions,
  .user-center__user-actions {
    gap: 10px;
  }

  .user-center__panel {
    padding-top: 14px;
  }

  .user-center__panel :deep(.arco-tabs-nav-tab) {
    flex-shrink: 0;
  }
}
</style>
