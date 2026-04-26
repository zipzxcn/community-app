<template>
  <section class="post-detail">
    <a-spin :loading="loading">
      <article v-if="post" class="post-detail__article">
        <RouterLink class="post-detail__back" to="/">返回帖子流</RouterLink>
        <h1>{{ post.title }}</h1>
        <div class="post-detail__meta">
          <span>{{ post.author?.nickname || post.author?.username || '匿名用户' }}</span>
          <span>{{ formatDateTime(post.publishedAt) }}</span>
          <span>浏览 {{ post.viewCount || 0 }}</span>
          <span>评论 {{ post.commentCount || 0 }}</span>
          <a-tag v-if="post.status === 'HIDDEN'" color="orange">已下架</a-tag>
        </div>
        <img v-if="post.coverUrl" :src="resolveAssetUrl(post.coverUrl)" alt="" class="post-detail__cover" />
        <div v-if="post.tags?.length" class="post-detail__tags">
          <a-tag v-for="tag in post.tags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
        </div>
        <div class="markdown-body post-detail__content" v-html="contentHtml"></div>
        <div class="post-detail__actions">
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
      </article>
      <a-empty v-else description="帖子不存在或已下架" />
    </a-spin>

    <a-card v-if="post" class="post-detail__comments" :bordered="false">
      <template #title>评论互动 {{ post.commentCount || 0 }}</template>
      <a-alert v-if="post.allowComment === 0" type="warning" show-icon>
        <template #title>作者已关闭评论区。</template>
      </a-alert>
      <div v-else-if="authStore.isLoggedIn" class="post-detail__comment-form">
        <a-textarea v-model="commentContent" placeholder="写下你的评论" :auto-size="{ minRows: 3, maxRows: 6 }" />
        <a-button type="primary" :loading="submittingComment" @click="submitComment">发表评论</a-button>
      </div>
      <a-alert v-else type="info" show-icon>
        <template #title>登录后可以发表评论、回复和点赞评论。</template>
      </a-alert>

      <a-spin :loading="commentsLoading">
        <div v-if="comments.length" class="post-detail__comment-list">
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
        <a-empty v-else description="还没有评论" />
      </a-spin>

      <div v-if="commentPage.total > commentPage.size" class="post-detail__pager">
        <a-pagination
          :current="commentPage.page"
          :page-size="commentPage.size"
          :total="commentPage.total"
          @change="changeCommentPage"
        />
      </div>
    </a-card>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import CommentItem from '@/components/comment/CommentItem.vue'
import { createComment, deleteComment, fetchPostComments, likeComment, replyComment, unlikeComment } from '@/api/comment'
import { recordHistory } from '@/api/history'
import { deletePost, favoritePost, fetchPostDetail, hidePost, likePost, unfavoritePost, unlikePost } from '@/api/post'
import { useAuthStore } from '@/stores/auth'
import type { CommentItem as CommentItemType } from '@/types/comment'
import type { PostDetail } from '@/types/post'
import { formatDateTime, resolveAssetUrl } from '@/utils/format'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  postId: number
}>()

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const commentsLoading = ref(false)
const submittingComment = ref(false)
const likingPost = ref(false)
const favoritingPost = ref(false)
const commentLikeId = ref<number | null>(null)
const deletingCommentId = ref<number | null>(null)
const replyingCommentId = ref<number | null>(null)
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

function requireLogin() {
  if (authStore.isLoggedIn) {
    return true
  }
  Modal.confirm({
    title: '需要登录',
    content: '登录后才能继续互动。',
    onOk: () => router.push({ name: 'login', query: { redirect: `/posts/${props.postId}` } }),
  })
  return false
}

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

async function recordBrowseHistory() {
  if (!authStore.isLoggedIn || !post.value) {
    return
  }
  await recordHistory(post.value.id).catch(() => undefined)
}

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
  if (!currentUserId) {
    return false
  }
  return currentUserId === comment.user?.id || currentUserId === post.value?.author?.id
}

function changeCommentPage(current: number) {
  commentPage.page = current
  loadComments()
}

onMounted(async () => {
  await loadPost()
  await recordBrowseHistory()
  await loadComments()
})

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
.post-detail {
  display: grid;
  gap: 22px;
}

.post-detail__article,
.post-detail__comments {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.post-detail__article {
  padding: 30px;
}

.post-detail__back {
  color: #0f766e;
  font-weight: 700;
  text-decoration: none;
}

.post-detail h1 {
  margin: 14px 0;
  color: #172033;
  font-size: clamp(30px, 5vw, 52px);
  line-height: 1.08;
}

.post-detail__meta,
.post-detail__tags,
.post-detail__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.post-detail__meta {
  color: #64748b;
}

.post-detail__cover {
  width: 100%;
  max-height: 360px;
  margin: 22px 0;
  object-fit: cover;
  border-radius: 14px;
}

.post-detail__content {
  margin-top: 24px;
  color: #273449;
  font-size: 16px;
  line-height: 1.9;
}

.post-detail__actions {
  margin-top: 26px;
}

.post-detail__comment-form,
.post-detail__comment-list {
  display: grid;
  gap: 18px;
}

.post-detail__comment-form {
  margin-bottom: 22px;
}

.post-detail__comment-form .arco-btn {
  justify-self: end;
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
  color: #172033;
}

.markdown-body :deep(p),
.markdown-body :deep(ul) {
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

@media (max-width: 720px) {
  .post-detail__article {
    padding: 22px;
  }
}
</style>
