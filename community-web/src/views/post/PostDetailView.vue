<template>
  <section class="post-detail app-page">
    <a-spin :loading="false">
      <article v-if="post" class="post-detail__article app-panel">
        <div class="post-detail__topbar">
          <RouterLink class="post-detail__back" :to="backToFeed">
            <icon-left />
            <span>返回帖子流</span>
          </RouterLink>
          <div class="post-detail__topbar-actions">
            <a-tag v-if="post.status === 'HIDDEN'" color="orange">已下架</a-tag>
            <a-tag v-if="post.allowComment === 0" color="red">评论已关闭</a-tag>
          </div>
        </div>

        <div class="post-detail__hero">
          <div class="post-detail__hero-main">
            <p class="post-detail__eyebrow">Post Detail</p>
            <h1>{{ post.title }}</h1>

            <div class="post-detail__author-card">
              <div class="post-detail__author-main">
                <a-avatar :size="52" class="post-detail__avatar">
                  <img v-if="post.author?.avatarUrl" :src="resolveAssetUrl(post.author.avatarUrl)" alt="" />
                  <template v-else>{{ displayInitial }}</template>
                </a-avatar>
                <div>
                  <strong>{{ post.author?.nickname || post.author?.username || '匿名用户' }}</strong>
                  <p>@{{ post.author?.username || 'unknown' }}</p>
                </div>
              </div>
              <div class="post-detail__meta">
                <span class="app-chip">{{ formatDateTime(post.publishedAt) }}</span>
                <span class="app-chip">浏览 {{ post.viewCount || 0 }}</span>
                <span class="app-chip">评论 {{ post.commentCount || 0 }}</span>
              </div>
            </div>

            <div v-if="post.tags?.length" class="post-detail__tags">
              <a-tag v-for="tag in post.tags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
            </div>
          </div>

          <div class="post-detail__hero-side">
            <article class="app-stat-card">
              <strong>{{ post.likeCount || 0 }}</strong>
              <span>点赞</span>
              <small>表达你对内容的认可</small>
            </article>
            <article class="app-stat-card">
              <strong>{{ post.favoriteCount || 0 }}</strong>
              <span>收藏</span>
              <small>方便后续再次回看</small>
            </article>
            <article class="app-stat-card">
              <strong>{{ post.commentCount || 0 }}</strong>
              <span>评论</span>
              <small>参与这场讨论</small>
            </article>
          </div>
        </div>

        <img v-if="post.coverUrl" :src="resolveAssetUrl(post.coverUrl)" alt="" class="post-detail__cover" />

        <div class="post-detail__action-bar">
          <a-button :type="post.liked ? 'primary' : 'outline'" :loading="likingPost" @click="togglePostLike">
            {{ post.liked ? '已点赞' : '点赞' }} {{ post.likeCount || 0 }}
          </a-button>
          <a-button :type="post.favorited ? 'primary' : 'outline'" :loading="favoritingPost" @click="togglePostFavorite">
            {{ post.favorited ? '已收藏' : '收藏' }} {{ post.favoriteCount || 0 }}
          </a-button>
          <template v-if="isOwner">
            <RouterLink :to="`/posts/${post.id}/edit`">
              <a-button>编辑</a-button>
            </RouterLink>
            <a-button @click="togglePostHidden">
              {{ post.status === 'HIDDEN' ? '恢复上架' : '下架帖子' }}
            </a-button>
            <a-button status="danger" @click="removePost">删除帖子</a-button>
          </template>
        </div>

        <div class="markdown-body post-detail__content" v-html="contentHtml"></div>
      </article>
      <div v-else-if="loading" class="app-loading-list">
        <div class="app-loading-card post-detail__skeleton-card">
          <div class="app-skeleton app-skeleton--text-short"></div>
          <div class="app-skeleton app-skeleton--title"></div>
          <div class="app-skeleton app-skeleton--text"></div>
          <div class="app-skeleton app-skeleton--text"></div>
          <div class="app-skeleton post-detail__skeleton-cover"></div>
          <div class="app-skeleton app-skeleton--text"></div>
          <div class="app-skeleton app-skeleton--text-short"></div>
        </div>
      </div>
      <div v-else class="app-empty-state app-empty-state--center">
        <p class="app-empty-state__eyebrow">Post Detail</p>
        <h3 class="app-empty-state__title">帖子不存在或暂时不可见</h3>
        <p class="app-empty-state__desc">这篇内容可能已删除、下架，或者你当前没有查看权限。你可以返回首页继续浏览其他帖子。</p>
        <div class="app-empty-state__actions">
          <RouterLink to="/">
            <a-button type="primary">返回首页</a-button>
          </RouterLink>
        </div>
      </div>
    </a-spin>

    <a-card v-if="post" class="post-detail__comments app-panel" :bordered="false">
      <div class="app-section-head">
        <div class="app-section-head__main">
          <p class="app-section-head__eyebrow">Comments</p>
          <h2 class="app-section-head__title">评论互动</h2>
          <p class="app-section-head__desc">继续讨论、回复他人观点，或者为优质评论点赞。</p>
        </div>
        <strong class="app-section-head__value">{{ post.commentCount || 0 }}</strong>
      </div>

      <a-alert v-if="post.allowComment === 0" type="warning" show-icon class="post-detail__comment-alert">
        <template #title>作者已关闭评论区。</template>
      </a-alert>
      <div v-else-if="authStore.isLoggedIn" class="post-detail__comment-form">
        <div class="post-detail__comment-head">
          <div>
            <p>发表评论</p>
            <h3>说点什么，参与这场讨论</h3>
          </div>
          <span>{{ commentContent.trim().length }}/1000</span>
        </div>
        <a-textarea
          ref="commentInputRef"
          v-model="commentContent"
          placeholder="写下你的评论"
          :auto-size="{ minRows: 3, maxRows: 6 }"
        />
        <a-button type="primary" :disabled="!commentContent.trim()" :loading="submittingComment" @click="submitComment">发表评论</a-button>
      </div>
      <a-alert v-else type="info" show-icon class="post-detail__comment-alert">
        <template #title>登录后可以发表评论、回复和点赞评论。</template>
      </a-alert>

      <a-spin :loading="false">
        <div v-if="commentsLoading && !comments.length" class="app-loading-list">
          <div v-for="index in 3" :key="index" class="app-loading-card">
            <div class="app-skeleton app-skeleton--title"></div>
            <div class="app-skeleton app-skeleton--text"></div>
            <div class="app-skeleton app-skeleton--text-short"></div>
          </div>
        </div>
        <div v-else-if="comments.length" class="post-detail__comment-list">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            :can-reply="canReplyComment"
            :current-user-id="authStore.userInfo?.id"
            :post-author-id="post.author?.id"
            :show-delete-hint="authStore.isLoggedIn"
            :busy-like-id="commentLikeId"
            :busy-delete-id="deletingCommentId"
            :busy-reply-id="replyingCommentId"
            @reply="submitReply"
            @delete="removeComment"
            @toggle-like="toggleCommentLike"
          />
        </div>
        <div v-else class="app-empty-state">
          <p class="app-empty-state__eyebrow">Comments</p>
          <h3 class="app-empty-state__title">还没有评论</h3>
          <p class="app-empty-state__desc">如果你已经登录，可以先发表第一条评论，帮助这篇内容开启讨论。</p>
          <div v-if="authStore.isLoggedIn && post?.allowComment !== 0" class="app-empty-state__actions">
            <a-button type="primary" @click="focusCommentInput">我来发第一条</a-button>
          </div>
        </div>
      </a-spin>

      <div v-if="commentPage.total > commentPage.size" class="post-detail__pager">
        <a-pagination :current="commentPage.page" :page-size="commentPage.size" :total="commentPage.total" @change="changeCommentPage" />
      </div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
/**
 * 帖子详情页：展示正文、评论树、附件、互动状态与浏览记录。
 */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { IconLeft } from '@arco-design/web-vue/es/icon'
import { useRoute, useRouter } from 'vue-router'
import CommentItem from '@/components/comment/CommentItem.vue'
import { createComment, deleteComment, fetchPostComments, likeComment, replyComment, unlikeComment } from '@/api/comment'
import { recordHistory } from '@/api/history'
import { deletePost, favoritePost, fetchPostDetail, hidePost, likePost, unfavoritePost, unlikePost } from '@/api/post'
import { useAuthStore } from '@/stores/auth'
import type { CommentItem as CommentItemType } from '@/types/comment'
import type { PostDetail } from '@/types/post'
import { buildFeedRouteQuery, parseFeedRouteQuery } from '@/utils/feed'
import { formatDateTime, resolveAssetUrl } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  postId: number
}>()

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const commentsLoading = ref(false)
const submittingComment = ref(false)
const likingPost = ref(false)
const favoritingPost = ref(false)
const commentLikeId = ref<number | null>(null)
const deletingCommentId = ref<number | null>(null)
const replyingCommentId = ref<number | null>(null)
const commentInputRef = ref()
const post = ref<PostDetail | null>(null)
const comments = ref<CommentItemType[]>([])
const commentContent = ref('')
const commentPage = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const isOwner = computed(() => Boolean(post.value && authStore.userInfo?.id === post.value.author?.id))
const canReplyComment = computed(() => authStore.isLoggedIn && post.value?.allowComment !== 0)
const contentHtml = computed(() => renderMarkdown(post.value?.contentMd))
const displayInitial = computed(() => (post.value?.author?.nickname || post.value?.author?.username || '匿').slice(0, 1).toUpperCase())
const backToFeed = computed(() => ({
  name: 'home',
  query: buildFeedRouteQuery({
    ...parseFeedRouteQuery(route.query),
    restoreFeed: true,
  }),
}))

// 教学点：把“未登录时弹确认框并跳登录页”的逻辑抽成公共函数，避免每个互动按钮重复写一遍。
function requireLogin() {
  if (authStore.isLoggedIn) return true
  Modal.confirm({
    title: '需要登录',
    content: '登录后才能继续互动。',
    onOk: () => router.push({ name: 'login', query: { redirect: `/posts/${props.postId}` } }),
  })
  return false
}

// load* 系列方法负责从后端拉取页面初始化数据，统一控制 loading 和错误提示。
// 教学点：详情主数据单独加载，便于点赞/收藏后只刷新局部，不必每次都把评论一起重拉。
async function loadPost() {
  loading.value = true
  try {
    post.value = await fetchPostDetail(props.postId)
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '帖子详情加载失败')
    post.value = null
  } finally {
    loading.value = false
  }
}

// 教学点：浏览历史是“静默副作用”，失败不阻塞主流程，所以这里故意吞掉错误。
async function recordBrowseHistory() {
  if (!authStore.isLoggedIn || !post.value) return
  await recordHistory(post.value.id).catch(() => undefined)
}

// 教学点：评论和帖子详情拆开请求，可以让评论分页独立工作，也便于后续懒加载。
async function loadComments() {
  commentsLoading.value = true
  try {
    const result = await fetchPostComments(props.postId, {
      page: commentPage.page,
      size: commentPage.size,
    })
    comments.value = result.list
    commentPage.page = result.page
    commentPage.size = result.size
    commentPage.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '评论加载失败')
  } finally {
    commentsLoading.value = false
  }
}

async function togglePostLike() {
  if (!post.value || !requireLogin()) return
  likingPost.value = true
  try {
    // 教学点：这里采用本地乐观更新计数，用户不需要等重新请求详情就能立刻看到 UI 反馈。
    if (post.value.liked) {
      await unlikePost(post.value.id)
      post.value.liked = false
      post.value.likeCount = Math.max((post.value.likeCount || 0) - 1, 0)
    } else {
      await likePost(post.value.id)
      post.value.liked = true
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '点赞操作失败')
  } finally {
    likingPost.value = false
  }
}

async function togglePostFavorite() {
  if (!post.value || !requireLogin()) return
  favoritingPost.value = true
  try {
    // 收藏与点赞同理，也是先本地改状态，再由接口结果兜底。
    if (post.value.favorited) {
      await unfavoritePost(post.value.id)
      post.value.favorited = false
      post.value.favoriteCount = Math.max((post.value.favoriteCount || 0) - 1, 0)
    } else {
      await favoritePost(post.value.id)
      post.value.favorited = true
      post.value.favoriteCount = (post.value.favoriteCount || 0) + 1
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '收藏操作失败')
  } finally {
    favoritingPost.value = false
  }
}

function togglePostHidden() {
  if (!post.value) return
  const nextHidden = post.value.status !== 'HIDDEN'
  Modal.confirm({
    title: nextHidden ? '下架帖子' : '恢复上架',
    content: nextHidden ? '下架后公开列表将不可见。' : '恢复上架后帖子会重新出现在公开列表。',
    async onOk() {
      try {
        await hidePost(post.value!.id, nextHidden)
        Message.success(nextHidden ? '帖子已下架' : '帖子已恢复上架')
        if (nextHidden) {
          await router.push('/me')
        } else {
          await loadPost()
        }
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '帖子状态更新失败')
      }
    },
  })
}

function removePost() {
  if (!post.value) return
  Modal.confirm({
    title: '删除帖子',
    content: '删除后帖子将从公开列表移除。',
    async onOk() {
      try {
        await deletePost(post.value!.id)
        Message.success('帖子已删除')
        await router.push('/me')
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '删除失败')
      }
    },
  })
}

// 评论提交通常会触发帖子评论数和通知数的联动刷新。
async function submitComment() {
  if (post.value?.allowComment === 0) {
    Message.warning('当前帖子已关闭评论')
    return
  }
  if (!requireLogin()) return
  const content = commentContent.value.trim()
  if (!content) {
    Message.warning('请输入评论内容')
    return
  }
  submittingComment.value = true
  try {
    // 教学点：评论成功后这里选择“重新拉帖子 + 重新拉评论”，代码更稳，代价是一次额外请求。
    await createComment(props.postId, { content })
    commentContent.value = ''
    Message.success('评论已发布')
    await loadPost()
    await loadComments()
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '评论发布失败')
  } finally {
    submittingComment.value = false
  }
}

async function submitReply(payload: { comment: CommentItemType; content: string }) {
  if (post.value?.allowComment === 0) {
    Message.warning('当前帖子已关闭评论')
    return
  }
  if (!requireLogin()) return
  replyingCommentId.value = payload.comment.id
  try {
    // 教学点：回复接口路径中带 commentId，请求体里再带 parentId，是为了同时兼顾路由语义和服务端校验。
    await replyComment(payload.comment.id, {
      parentId: payload.comment.id,
      content: payload.content,
    })
    Message.success('回复已发送')
    await loadPost()
    await loadComments()
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '回复失败')
  } finally {
    replyingCommentId.value = null
  }
}

function removeComment(comment: CommentItemType) {
  if (!requireLogin()) return
  Modal.confirm({
    title: '删除评论',
    content: canDeleteComment(comment) ? '确认删除这条评论？' : '仅评论作者或帖主可删除这条评论。',
    async onOk() {
      if (!canDeleteComment(comment)) {
        Message.warning('仅评论作者或帖主可删除评论')
        return
      }
      deletingCommentId.value = comment.id
      try {
        await deleteComment(comment.id)
        Message.success('评论已删除')
        await loadPost()
        await loadComments()
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '删除失败')
      } finally {
        deletingCommentId.value = null
      }
    },
  })
}

async function toggleCommentLike(comment: CommentItemType) {
  if (!requireLogin()) return
  commentLikeId.value = comment.id
  try {
    // 评论点赞直接在当前评论对象上原地修改，不必整页重刷，交互更丝滑。
    if (comment.liked) {
      await unlikeComment(comment.id)
      comment.liked = false
      comment.likeCount = Math.max((comment.likeCount || 0) - 1, 0)
    } else {
      await likeComment(comment.id)
      comment.liked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '评论点赞失败')
  } finally {
    commentLikeId.value = null
  }
}

function canDeleteComment(comment: CommentItemType) {
  const currentUserId = authStore.userInfo?.id
  if (!currentUserId) return false
  return currentUserId === comment.user?.id || currentUserId === post.value?.author?.id
}

function focusCommentInput() {
  commentInputRef.value?.focus?.()
}

function changeCommentPage(current: number) {
  commentPage.page = current
  loadComments()
}

// 教学点：首次进入详情页时，按“详情 -> 历史 -> 评论”的顺序跑，主内容优先可见。
onMounted(async () => {
  await loadPost()
  await recordBrowseHistory()
  await loadComments()
})

// 监听路由参数或局部状态变化，保证页面在切换对象后自动刷新。
watch(
  () => props.postId,
  async () => {
    commentPage.page = 1
    await loadPost()
    await recordBrowseHistory()
    await loadComments()
  },
)
</script>

<style scoped lang="scss">
.post-detail__article :deep(.arco-card-body),
.post-detail__comments :deep(.arco-card-body) {
  padding: 20px;
}

.post-detail__comments :deep(.arco-card-body),
.post-detail__comments :deep(.arco-spin),
.post-detail__comments :deep(.arco-spin-children) {
  display: block;
  width: 100%;
}

.post-detail__topbar,
.post-detail__topbar-actions,
.post-detail__meta,
.post-detail__tags,
.post-detail__action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.post-detail__topbar {
  justify-content: space-between;
  margin-bottom: 16px;
}

.post-detail__back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 0 16px;
  color: var(--app-primary);
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  background: rgba(236, 253, 245, 0.96);
  border: 1px solid rgba(15, 118, 110, 0.14);
  border-radius: var(--app-radius-pill);
  box-shadow: 0 10px 24px rgba(15, 118, 110, 0.08);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.post-detail__back:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.22);
  box-shadow: 0 14px 28px rgba(15, 118, 110, 0.12);
}

.post-detail__comments :deep(.app-section-head) {
  margin-bottom: 18px;
}

.post-detail__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(220px, 0.55fr);
  gap: 22px;
  margin-top: 18px;
}

.post-detail__eyebrow {
  margin: 0 0 8px;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.post-detail__hero h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(34px, 5vw, 56px);
  line-height: 1.05;
}

.post-detail__author-card {
  display: grid;
  gap: 14px;
  margin-top: 18px;
  padding: 18px;
  background: rgba(248, 250, 252, 0.84);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-md);
}

.post-detail__author-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-detail__avatar {
  flex-shrink: 0;
  color: var(--app-primary);
  font-weight: 800;
  background: linear-gradient(135deg, rgba(204, 251, 241, 0.92), rgba(219, 234, 254, 0.92));
}

.post-detail__author-main strong,
.post-detail__author-main p {
  display: block;
  margin: 0;
}

.post-detail__author-main strong {
  color: var(--app-text-1);
}

.post-detail__author-main p {
  margin-top: 4px;
  color: var(--app-text-3);
}

.post-detail__hero-side {
  display: grid;
  gap: 12px;
}

.post-detail__tags {
  margin-top: 14px;
  gap: 8px;
}

.post-detail__cover {
  width: 100%;
  max-height: 420px;
  margin: 22px 0;
  object-fit: cover;
  border-radius: 18px;
}

.post-detail__action-bar {
  margin-bottom: 22px;
}

.post-detail__content {
  color: #273449;
  font-size: 16px;
  line-height: 1.95;
}

.post-detail__comment-alert,
.post-detail__comment-form {
  margin-top: 18px;
}

.post-detail__comment-form,
.post-detail__comment-list {
  display: grid;
  gap: 18px;
}

.post-detail__comment-form {
  padding: 18px;
  background: rgba(248, 250, 252, 0.62);
  border: 1px solid var(--app-border-color-soft);
  border-radius: var(--app-radius-md);
}

.post-detail__comment-list {
  grid-template-columns: 1fr;
  width: 100%;
  margin-top: 10px;
}

.post-detail__comment-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
}

.post-detail__comment-head p,
.post-detail__comment-head h3,
.post-detail__comment-head span {
  margin: 0;
}

.post-detail__comment-head p {
  color: var(--app-primary);
  font-weight: 800;
}

.post-detail__comment-head h3 {
  margin-top: 6px;
  color: var(--app-text-1);
  font-size: 22px;
}

.post-detail__comment-head span {
  color: var(--app-text-3);
  font-size: 13px;
}

.post-detail__comment-form .arco-btn {
  justify-self: end;
}

.post-detail__skeleton-card {
  min-height: 420px;
}

.post-detail__skeleton-cover {
  width: 100%;
  height: 220px;
}

.post-detail__pager {
  display: flex;
  justify-content: center;
  margin-top: 22px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 16px 0 10px;
  color: var(--app-text-1);
}

.markdown-body :deep(p),
.markdown-body :deep(ul),
.markdown-body :deep(ol),
.markdown-body :deep(blockquote) {
  margin: 0 0 12px;
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  font-family: Consolas, monospace;
  background: #f1f5f9;
  border-radius: 6px;
}

.markdown-body :deep(img) {
  display: block;
  max-width: 100%;
  margin: 14px 0;
  border-radius: 12px;
}

@media (max-width: 960px) {
  .post-detail__hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .post-detail__topbar,
  .post-detail__comment-head,
  .post-detail__author-main {
    align-items: flex-start;
    flex-direction: column;
  }

  .post-detail__hero h1 {
    font-size: clamp(28px, 8vw, 38px);
  }

  .post-detail__action-bar .arco-btn,
  .post-detail__comment-form .arco-btn {
    min-height: 38px;
  }
}
</style>
