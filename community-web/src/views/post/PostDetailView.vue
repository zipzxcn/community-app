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
        </div>
        <img v-if="post.coverUrl" :src="post.coverUrl" alt="" class="post-detail__cover" />
        <div v-if="post.tags?.length" class="post-detail__tags">
          <a-tag v-for="tag in post.tags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
        </div>
        <div class="post-detail__content">{{ post.contentMd }}</div>
        <div class="post-detail__actions">
          <a-button :type="post.liked ? 'primary' : 'outline'" @click="togglePostLike">
            {{ post.liked ? '已点赞' : '点赞' }} {{ post.likeCount || 0 }}
          </a-button>
          <a-button :type="post.favorited ? 'primary' : 'outline'" @click="togglePostFavorite">
            {{ post.favorited ? '已收藏' : '收藏' }} {{ post.favoriteCount || 0 }}
          </a-button>
        </div>
      </article>
      <a-empty v-else description="帖子不存在或已下架" />
    </a-spin>

    <a-card v-if="post" class="post-detail__comments" :bordered="false">
      <template #title>评论互动</template>
      <div v-if="authStore.isLoggedIn" class="post-detail__comment-form">
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
import { onMounted, reactive, ref, watch } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'
import CommentItem from '@/components/comment/CommentItem.vue'
import { createComment, deleteComment, fetchPostComments, likeComment, replyComment, unlikeComment } from '@/api/comment'
import { favoritePost, fetchPostDetail, likePost, unfavoritePost, unlikePost } from '@/api/post'
import { useAuthStore } from '@/stores/auth'
import type { CommentItem as CommentItemType } from '@/types/comment'
import type { PostDetail } from '@/types/post'
import { formatDateTime } from '@/utils/format'

const props = defineProps<{
  postId: number
}>()

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const commentsLoading = ref(false)
const submittingComment = ref(false)
const post = ref<PostDetail | null>(null)
const comments = ref<CommentItemType[]>([])
const commentContent = ref('')
const commentPage = reactive({
  page: 1,
  size: 10,
  total: 0,
})

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
  if (!post.value || !requireLogin()) {
    return
  }
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
  }
}

async function togglePostFavorite() {
  if (!post.value || !requireLogin()) {
    return
  }
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
  }
}

async function submitComment() {
  if (!requireLogin()) {
    return
  }
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
  if (!requireLogin()) {
    return
  }
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
  }
}

function removeComment(comment: CommentItemType) {
  if (!requireLogin()) {
    return
  }
  Modal.confirm({
    title: '删除评论',
    content: '确认删除这条评论？',
    async onOk() {
      try {
        await deleteComment(comment.id)
        Message.success('评论已删除')
        await loadPost()
        await loadComments()
      } catch (error) {
        Message.error(error instanceof Error ? error.message : '删除失败')
      }
    },
  })
}

async function toggleCommentLike(comment: CommentItemType) {
  if (!requireLogin()) {
    return
  }
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
  }
}

function changeCommentPage(current: number) {
  commentPage.page = current
  loadComments()
}

onMounted(async () => {
  await loadPost()
  await loadComments()
})

watch(
  () => props.postId,
  async () => {
    commentPage.page = 1
    await loadPost()
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
  border-radius: 28px;
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
  border-radius: 24px;
}

.post-detail__content {
  margin-top: 24px;
  color: #273449;
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-wrap;
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

@media (max-width: 720px) {
  .post-detail__article {
    padding: 22px;
  }
}
</style>
