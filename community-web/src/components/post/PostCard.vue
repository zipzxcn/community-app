<template>
  <article class="post-card">
    <RouterLink class="post-card__main" :to="`/posts/${post.id}`">
      <div>
        <h2>{{ post.title }}</h2>
        <p>{{ post.excerpt || '作者暂未填写摘要，打开详情查看完整内容。' }}</p>
      </div>
      <img v-if="post.coverUrl" :src="resolveAssetUrl(post.coverUrl)" alt="" class="post-card__cover" />
    </RouterLink>

    <div class="post-card__meta">
      <span>{{ post.author?.nickname || post.author?.username || '匿名用户' }}</span>
      <span>{{ formatDateTime(post.publishedAt) }}</span>
      <span>点赞 {{ compactNumber(post.likeCount) }}</span>
      <span>收藏 {{ compactNumber(post.favoriteCount) }}</span>
      <span>评论 {{ compactNumber(post.commentCount) }}</span>
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
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  box-shadow: 0 18px 60px rgba(15, 23, 42, 0.06);
}

.post-card__main {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  color: inherit;
  text-decoration: none;
}

.post-card h2 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 22px;
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

.post-card__meta,
.post-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
  color: #64748b;
  font-size: 13px;
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
