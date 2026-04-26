<template>
  <section class="user-center">
    <div class="user-center__hero">
      <div class="user-center__profile">
        <a-avatar :size="68" class="user-center__avatar">
          <img v-if="profileForm.avatarUrl" :src="resolveAssetUrl(profileForm.avatarUrl)" alt="" />
          <template v-else>{{ avatarText }}</template>
        </a-avatar>
        <div>
          <p class="user-center__eyebrow">Personal Hub</p>
          <h1>{{ displayName }}</h1>
          <p>{{ profileForm.bio || '完善资料后，其他用户能更快了解你。' }}</p>
        </div>
      </div>
      <div class="user-center__stats">
        <span>帖子 {{ profileStats.postCount }}</span>
        <span>关注 {{ profileStats.followingCount }}</span>
        <span>粉丝 {{ profileStats.followerCount }}</span>
      </div>
    </div>

    <a-card :bordered="false" class="user-center__panel">
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
        </a-form>

        <template v-else-if="isPostTab">
          <div class="user-center__section-head">
            <div>
              <p>{{ currentPostMeta.eyebrow }}</p>
              <h2>{{ currentPostMeta.title }}</h2>
              <span>{{ currentPostMeta.description }}</span>
            </div>
            <strong>{{ currentPage.total }}</strong>
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
              <div v-if="activeTab === 'posts'" class="user-center__post-actions">
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
              </div>
              <div v-else class="user-center__post-actions">
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
              </div>
            </article>
          </div>
          <a-empty v-else :description="currentPostMeta.emptyText" />
          <div v-if="currentPage.total > currentPage.size" class="user-center__pager">
            <a-pagination
              :current="currentPage.page"
              :page-size="currentPage.size"
              :total="currentPage.total"
              @change="changePostPage"
            />
          </div>
        </template>

        <template v-else>
          <div class="user-center__section-head">
            <div>
              <p>{{ currentUserMeta.eyebrow }}</p>
              <h2>{{ currentUserMeta.title }}</h2>
              <span>{{ currentUserMeta.description }}</span>
            </div>
            <strong>{{ currentUserPage.total }}</strong>
          </div>
          <div v-if="currentUsers.length" class="user-center__user-list">
            <article v-for="user in currentUsers" :key="user.id" class="user-center__user-item">
              <RouterLink :to="`/users/${user.id}`" class="user-center__user-main">
                <a-avatar :size="42">
                  <img v-if="user.avatarUrl" :src="resolveAssetUrl(user.avatarUrl)" alt="" />
                  <template v-else>{{ (user.nickname || user.username).slice(0, 1).toUpperCase() }}</template>
                </a-avatar>
                <div>
                  <h3>{{ user.nickname || user.username }}</h3>
                  <p>@{{ user.username }}</p>
                </div>
              </RouterLink>
              <a-tag v-if="user.mutualFollow" color="green">互关</a-tag>
              <a-tag v-else-if="user.followedByMe" color="arcoblue">已关注</a-tag>
              <a-button
                v-else-if="activeTab === 'followers'"
                size="small"
                type="primary"
                @click="followBack(user.id)"
              >
                回关
              </a-button>
            </article>
          </div>
          <a-empty v-else :description="emptyUserText" />
          <div v-if="currentUserPage.total > currentUserPage.size" class="user-center__pager">
            <a-pagination
              :current="currentUserPage.page"
              :page-size="currentUserPage.size"
              :total="currentUserPage.total"
              @change="changeUserPage"
            />
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
import { fetchFollowers, fetchFollowing, fetchMutualFollows, followUser } from '@/api/follow'
import { deletePost, hidePost, unlikePost, unfavoritePost } from '@/api/post'
import { fetchUserFavorites, fetchUserLikes, fetchUserPosts, fetchUserProfile, updateMyProfile } from '@/api/user'
import { uploadImageFile } from '@/api/file'
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
  if (tab === 'profile') {
    return
  }
  if (['following', 'followers', 'mutual'].includes(tab)) {
    await loadUsers(tab)
    return
  }
  await loadPosts(tab)
})
</script>

<style scoped lang="scss">
.user-center {
  display: grid;
  gap: 20px;
}

.user-center__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 28px;
  background:
    radial-gradient(circle at 0% 0%, rgba(15, 118, 110, 0.22), transparent 40%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.86));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 22px;
}

.user-center__profile {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-center__avatar {
  color: #0f766e;
  font-weight: 800;
  background: #ccfbf1;
}

.user-center__eyebrow {
  margin: 0 0 6px;
  color: #0f766e;
  font-weight: 800;
  letter-spacing: 0;
}

.user-center__hero h1 {
  margin: 0 0 8px;
  color: #172033;
  font-size: 32px;
}

.user-center__hero p:last-child {
  margin: 0;
  color: #64748b;
}

.user-center__stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #334155;
}

.user-center__stats span {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
}

.user-center__panel {
  border-radius: 8px;
  box-shadow: 0 14px 42px rgba(15, 23, 42, 0.06);
}

.user-center__form {
  max-width: 680px;
  margin-top: 12px;
}

.user-center__avatar-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.user-center__upload {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 96px;
  height: 32px;
  color: #0f766e;
  cursor: pointer;
  background: #ecfdf5;
  border: 1px solid rgba(15, 118, 110, 0.24);
  border-radius: 6px;
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

.user-center__post-list,
.user-center__user-list {
  display: grid;
  gap: 14px;
  margin-top: 14px;
}

.user-center__post-filter {
  margin-top: 14px;
}

.user-center__section-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-top: 14px;
  padding: 18px 20px;
  background: linear-gradient(135deg, rgba(15, 118, 110, 0.08), rgba(59, 130, 246, 0.06));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
}

.user-center__section-head p,
.user-center__section-head h2,
.user-center__section-head span,
.user-center__section-head strong {
  margin: 0;
}

.user-center__section-head p {
  color: #0f766e;
  font-weight: 800;
}

.user-center__section-head h2 {
  margin-top: 6px;
  color: #172033;
  font-size: 24px;
}

.user-center__section-head span {
  display: block;
  margin-top: 8px;
  color: #64748b;
}

.user-center__section-head strong {
  color: #172033;
  font-size: 30px;
  line-height: 1;
}

.user-center__post-item {
  display: grid;
  gap: 10px;
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
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
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

.user-center__user-item h3 {
  margin: 0 0 2px;
  color: #172033;
}

.user-center__user-item p {
  margin: 0;
  color: #64748b;
}

.user-center__pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

@media (max-width: 760px) {
  .user-center__hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .user-center__avatar-editor {
    grid-template-columns: 1fr;
  }

  .user-center__section-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
