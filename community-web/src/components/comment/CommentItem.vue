<template>
  <div class="comment-item">
    <div class="comment-item__body">
      <a-avatar :size="36" class="comment-item__avatar">
        <img v-if="comment.user?.avatarUrl" :src="resolveAssetUrl(comment.user.avatarUrl)" alt="" />
        <template v-else>{{ displayName.slice(0, 1).toUpperCase() }}</template>
      </a-avatar>
      <div class="comment-item__content">
        <div class="comment-item__top">
          <strong>{{ displayName }}</strong>
          <span>{{ formatDateTime(comment.createdAt) }}</span>
          <span v-if="comment.replyToUser" class="comment-item__reply-target">
            回复 @{{ comment.replyToUser.nickname || comment.replyToUser.username }}
          </span>
        </div>
        <p>
          <template v-if="comment.replyToUser">@{{ comment.replyToUser.nickname || comment.replyToUser.username }} </template>
          {{ comment.content }}
        </p>
        <div class="comment-item__actions">
          <a-button size="mini" type="text" :loading="busyLike" @click="$emit('toggle-like', comment)">
            {{ comment.liked ? '取消赞' : '点赞' }} {{ comment.likeCount || 0 }}
          </a-button>
          <a-tooltip v-if="!canReply" content="登录后且评论区开启时才能回复">
            <a-button size="mini" type="text" disabled>回复</a-button>
          </a-tooltip>
          <a-button v-else size="mini" type="text" @click="replying = !replying">回复</a-button>
          <a-button
            v-if="canDelete"
            size="mini"
            type="text"
            status="danger"
            :loading="busyDelete"
            @click="$emit('delete', comment)"
          >
            删除
          </a-button>
          <a-tooltip v-else-if="showDeleteHint" content="仅评论作者或帖主可删除">
            <a-button size="mini" type="text" disabled>删除</a-button>
          </a-tooltip>
        </div>
        <div v-if="replying && canReply" class="comment-item__reply">
          <a-textarea
            v-model="replyContent"
            :placeholder="`回复 @${displayName}`"
            :auto-size="{ minRows: 2, maxRows: 4 }"
          />
          <div>
            <a-button size="small" @click="replying = false">取消</a-button>
            <a-button size="small" type="primary" :loading="busyReply" @click="submitReply">发送回复</a-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="comment.replies?.length" class="comment-item__children">
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :can-reply="canReply"
        :current-user-id="currentUserId"
        :post-author-id="postAuthorId"
        :show-delete-hint="showDeleteHint"
        :busy-like-id="busyLikeId"
        :busy-delete-id="busyDeleteId"
        :busy-reply-id="busyReplyId"
        @reply="$emit('reply', $event)"
        @delete="$emit('delete', $event)"
        @toggle-like="$emit('toggle-like', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CommentItem } from '@/types/comment'
import { formatDateTime, resolveAssetUrl } from '@/utils/format'

defineOptions({ name: 'CommentItem' })

const props = defineProps<{
  comment: CommentItem
  canReply: boolean
  currentUserId?: number | null
  postAuthorId?: number | null
  showDeleteHint?: boolean
  busyLikeId?: number | null
  busyDeleteId?: number | null
  busyReplyId?: number | null
}>()

const emit = defineEmits<{
  reply: [payload: { comment: CommentItem; content: string }]
  delete: [comment: CommentItem]
  'toggle-like': [comment: CommentItem]
}>()

const replying = ref(false)
const replyContent = ref('')
const displayName = computed(() => props.comment.user?.nickname || props.comment.user?.username || '匿名用户')
const canDelete = computed(() => {
  const currentUserId = props.currentUserId
  if (!currentUserId) {
    return false
  }
  return currentUserId === props.comment.user?.id || currentUserId === props.postAuthorId
})
const busyLike = computed(() => props.busyLikeId === props.comment.id)
const busyDelete = computed(() => props.busyDeleteId === props.comment.id)
const busyReply = computed(() => props.busyReplyId === props.comment.id)

function submitReply() {
  const content = replyContent.value.trim()
  if (!content) {
    return
  }
  emit('reply', { comment: props.comment, content })
  replyContent.value = ''
  replying.value = false
}
</script>

<style scoped lang="scss">
.comment-item {
  display: grid;
  gap: 12px;
}

.comment-item__body {
  display: flex;
  gap: 12px;
  padding: 14px 14px 12px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 18px;
}

.comment-item__avatar {
  flex: 0 0 36px;
  color: #0f766e;
  font-weight: 800;
  background: #ccfbf1;
}

.comment-item__content {
  flex: 1;
}

.comment-item__top {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.comment-item__top strong {
  color: #172033;
}

.comment-item__reply-target {
  color: #0f766e;
}

.comment-item p {
  margin: 8px 0;
  color: #334155;
  line-height: 1.7;
}

.comment-item__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.comment-item__reply {
  display: grid;
  gap: 8px;
  margin-top: 10px;
}

.comment-item__reply > div {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-item__children {
  display: grid;
  gap: 14px;
  margin-left: 48px;
  padding-left: 16px;
  border-left: 2px solid rgba(15, 118, 110, 0.12);
}

@media (max-width: 720px) {
  .comment-item__body {
    padding: 12px;
  }

  .comment-item__children {
    margin-left: 18px;
    padding-left: 12px;
  }
}
</style>
