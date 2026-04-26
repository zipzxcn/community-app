<template>
  <article class="post-card">
    <RouterLink class="post-card__main" :to="`/posts/${post.id}`">
      <div class="post-card__content">
        <div class="post-card__head">
          <div class="post-card__author">
            <span class="post-card__author-mark">{{ (post.author?.nickname || post.author?.username || '匿').slice(0, 1).toUpperCase() }}</span>
            <div>
              <strong>{{ post.author?.nickname || post.author?.username || '匿名用户' }}</strong>
              <small>{{ formatDateTime(post.publishedAt) }}</small>
            </div>
          </div>
          <span class="post-card__category">{{ post.status === 'HIDDEN' ? '已下架' : '公开帖子' }}</span>
        </div>

        <div>
          <h2>{{ post.title }}</h2>
          <p>{{ post.excerpt || '作者暂未填写摘要，打开详情查看完整内容。' }}</p>
        </div>
      </div>
      <img v-if="post.coverUrl" :src="resolveAssetUrl(post.coverUrl)" alt="" class="post-card__cover" />
    </RouterLink>

    <div class="post-card__meta">
      <span>点赞 {{ compactNumber(post.likeCount) }}</span>
      <span>收藏 {{ compactNumber(post.favoriteCount) }}</span>
      <span>评论 {{ compactNumber(post.commentCount) }}</span>
      <span>浏览 {{ compactNumber(post.viewCount) }}</span>
    </div>

    <div v-if="post.tags?.length" class="post-card__tags">
      <a-tag v-for="tag in post.tags" :key="tag.id" color="green">{{ tag.name }}</a-tag>
    </div>
  </article>
</template>

<script setup lang="ts">
import type { PostListItem } from '@/types/post'
import { compactNumber, formatDateTime, resolveAssetUrl } from '@/utils/format'

defineProps<{
  post: PostListItem
}>()
</script>

<style scoped lang="scss">
.post-card {
  padding: 22px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(248, 250, 252, 0.92));
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  border-color: rgba(15, 118, 110, 0.14);
  box-shadow: 0 22px 68px rgba(15, 23, 42, 0.08);
}

.post-card__main {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  color: inherit;
  text-decoration: none;
}

.post-card__content {
  display: grid;
  gap: 16px;
}

.post-card__head,
.post-card__author,
.post-card__meta,
.post-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.post-card__head {
  justify-content: space-between;
}

.post-card__author-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  color: #0f766e;
  font-size: 14px;
  font-weight: 800;
  background: #ccfbf1;
  border-radius: 12px;
}

.post-card__author strong,
.post-card__author small {
  display: block;
}

.post-card__author strong {
  color: #172033;
  font-size: 14px;
}

.post-card__author small,
.post-card__category {
  color: #64748b;
  font-size: 12px;
}

.post-card__category {
  padding: 6px 10px;
  background: rgba(241, 245, 249, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
}

.post-card h2 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 22px;
  line-height: 1.3;
}

.post-card p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #64748b;
  line-height: 1.7;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.post-card__cover {
  width: 132px;
  height: 92px;
  object-fit: cover;
  border-radius: 18px;
}

.post-card__meta {
  margin-top: 14px;
  color: #64748b;
  font-size: 13px;
}

.post-card__meta span {
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 999px;
}

.post-card__tags {
  margin-top: 14px;
}

@media (max-width: 720px) {
  .post-card__main {
    grid-template-columns: 1fr;
  }

  .post-card__cover {
    width: 100%;
    height: 160px;
  }
}
</style>
