<template>
  <article class="post-card">
    <RouterLink class="post-card__main" :to="`/posts/${post.id}`">
      <div class="post-card__content">
        <div class="post-card__top">
          <div class="post-card__author">
            <span class="post-card__author-mark">{{ displayInitial }}</span>
            <div class="post-card__author-copy">
              <strong>{{ post.author?.nickname || post.author?.username || '匿名用户' }}</strong>
              <small>{{ formatDateTime(post.publishedAt) }}</small>
            </div>
          </div>

          <div class="post-card__status-group">
            <span v-if="post.tags?.[0]" class="post-card__eyebrow">{{ post.tags[0].name }}</span>
            <span :class="['post-card__status', { 'post-card__status--hidden': post.status === 'HIDDEN' }]">
              {{ post.status === 'HIDDEN' ? '已下架' : '公开帖子' }}
            </span>
          </div>
        </div>

        <div class="post-card__body">
          <h2>{{ post.title }}</h2>
          <p>{{ post.excerpt || '作者暂未填写摘要，打开详情查看完整内容。' }}</p>
        </div>

        <div v-if="post.tags?.length" class="post-card__tags">
          <a-tag v-for="tag in post.tags.slice(0, 4)" :key="tag.id" color="green">{{ tag.name }}</a-tag>
          <span v-if="post.tags.length > 4" class="post-card__tags-more">+{{ post.tags.length - 4 }}</span>
        </div>
      </div>

      <div class="post-card__side">
        <img v-if="post.coverUrl" :src="resolveAssetUrl(post.coverUrl)" alt="" class="post-card__cover" />
        <div v-else class="post-card__cover post-card__cover--placeholder">
          <span>{{ displayInitial }}</span>
        </div>

        <div class="post-card__meta">
          <span><icon-thumb-up />{{ compactNumber(post.likeCount) }}</span>
          <span><icon-star />{{ compactNumber(post.favoriteCount) }}</span>
          <span><icon-message />{{ compactNumber(post.commentCount) }}</span>
          <span><icon-eye />{{ compactNumber(post.viewCount) }}</span>
        </div>
      </div>
    </RouterLink>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { IconEye, IconMessage, IconStar, IconThumbUp } from '@arco-design/web-vue/es/icon'
import type { PostListItem } from '@/types/post'
import { compactNumber, formatDateTime, resolveAssetUrl } from '@/utils/format'

const props = defineProps<{
  post: PostListItem
}>()

const displayInitial = computed(() => (props.post.author?.nickname || props.post.author?.username || '匿').slice(0, 1).toUpperCase())
</script>

<style scoped lang="scss">
.post-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92));
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-lg);
  box-shadow: var(--app-shadow-md);
  overflow: hidden;
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;
}

.post-card:hover {
  transform: translateY(-3px);
  border-color: rgba(15, 118, 110, 0.18);
  box-shadow: var(--app-shadow-lg);
}

.post-card__main {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 248px;
  gap: 20px;
  padding: 22px;
  color: inherit;
  text-decoration: none;
}

.post-card__content {
  display: grid;
  gap: 16px;
  min-width: 0;
}

.post-card__top,
.post-card__author,
.post-card__status-group,
.post-card__tags,
.post-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.post-card__top {
  justify-content: space-between;
}

.post-card__author-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  color: var(--app-primary);
  font-size: 15px;
  font-weight: 800;
  background: linear-gradient(135deg, rgba(204, 251, 241, 0.92), rgba(219, 234, 254, 0.92));
  border-radius: 14px;
  box-shadow: 0 10px 26px rgba(15, 118, 110, 0.12);
}

.post-card__author-copy {
  min-width: 0;
}

.post-card__author-copy strong,
.post-card__author-copy small {
  display: block;
}

.post-card__author-copy strong {
  color: var(--app-text-1);
  font-size: 14px;
}

.post-card__author-copy small {
  margin-top: 4px;
  color: var(--app-text-3);
  font-size: 12px;
}

.post-card__status-group {
  justify-content: flex-end;
}

.post-card__eyebrow {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  background: rgba(236, 253, 245, 0.9);
  border-radius: var(--app-radius-pill);
}

.post-card__status {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 700;
  background: rgba(241, 245, 249, 0.94);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-pill);
}

.post-card__status--hidden {
  color: #9a3412;
  background: rgba(255, 237, 213, 0.9);
  border-color: rgba(249, 115, 22, 0.18);
}

.post-card__body h2 {
  margin: 0 0 12px;
  color: var(--app-text-1);
  font-size: clamp(22px, 3vw, 28px);
  line-height: 1.25;
}

.post-card__body p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--app-text-3);
  line-height: 1.8;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.post-card__tags {
  gap: 8px;
}

.post-card__tags-more {
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 700;
}

.post-card__side {
  display: grid;
  align-content: space-between;
  gap: 14px;
}

.post-card__cover {
  width: 100%;
  height: 172px;
  object-fit: cover;
  border-radius: 18px;
}

.post-card__cover--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.92);
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--app-primary), var(--app-secondary));
}

.post-card__meta {
  gap: 8px;
}

.post-card__meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 0 10px;
  color: var(--app-text-3);
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid var(--app-border-color-soft);
  border-radius: var(--app-radius-pill);
}

@media (max-width: 860px) {
  .post-card__main {
    grid-template-columns: 1fr;
  }

  .post-card__side {
    gap: 12px;
  }

  .post-card__cover {
    height: 190px;
  }
}

@media (max-width: 720px) {
  .post-card__main {
    gap: 14px;
    padding: 16px;
  }

  .post-card__top {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-card__status-group {
    justify-content: flex-start;
  }

  .post-card__cover {
    height: 148px;
  }

  .post-card__body h2 {
    font-size: 20px;
  }

  .post-card__meta {
    gap: 6px;
  }

  .post-card__meta span {
    min-height: 30px;
    padding: 0 8px;
    font-size: 11px;
  }
}
</style>
