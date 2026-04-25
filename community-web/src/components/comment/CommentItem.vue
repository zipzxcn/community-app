<template>
  <div class="comment-item">
    <div class="comment-item__body">
      <div class="comment-item__avatar">{{ displayName.slice(0, 1).toUpperCase() }}</div>
      <div class="comment-item__content">
        <div class="comment-item__top">
          <strong>{{ displayName }}</strong>
          <span>{{ formatDateTime(comment.createdAt) }}</span>
        </div>
        <p>
          <template v-if="comment.replyToUser">@{{ comment.replyToUser.nickname || comment.replyToUser.username }} </template>
          {{ comment.content }}
        </p>
        <div class="comment-item__actions">
          <a-button size="mini" type="text" @click="$emit('toggle-like', comment)">
            {{ comment.liked ? '取消赞' : '点赞' }} {{ comment.likeCount || 0 }}
          </a-button>
          <a-button size="mini" type="text" @click="replying = !replying">回复</a-button>
          <a-button size="mini" type="text" status="danger" @click="$emit('delete', comment)">删除</a-button>
        </div>
        <div v-if="replying" class="comment-item__reply">
          <a-textarea v-model="replyContent" placeholder="写下你的回复" :auto-size="{ minRows: 2, maxRows: 4 }" />
          <div>
            <a-button size="small" @click="replying = false">取消</a-button>
            <a-button size="small" type="primary" @click="submitReply">发送回复</a-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="comment.replies?.length" class="comment-item__children">
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
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
import { formatDateTime } from '@/utils/format'

defineOptions({ name: 'CommentItem' })

const props = defineProps<{
  comment: CommentItem
}>()

const emit = defineEmits<{
  reply: [payload: { comment: CommentItem; content: string }]
  delete: [comment: CommentItem]
  'toggle-like': [comment: CommentItem]
}>()

const replying = ref(false)
const replyContent = ref('')
const displayName = computed(() => props.comment.user?.nickname || props.comment.user?.username || '匿名用户')

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
}

.comment-item__avatar {
  display: grid;
  flex: 0 0 36px;
  width: 36px;
  height: 36px;
  color: #0f766e;
  font-weight: 800;
  background: #ccfbf1;
  border-radius: 50%;
  place-items: center;
}

.comment-item__content {
  flex: 1;
}

.comment-item__top {
  display: flex;
  gap: 10px;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.comment-item__top strong {
  color: #172033;
}

.comment-item p {
  margin: 8px 0;
  color: #334155;
  line-height: 1.7;
}

.comment-item__actions {
  display: flex;
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
</style>
