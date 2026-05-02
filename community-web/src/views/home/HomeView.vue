<template>
  <section class="home-view app-page">
    <div class="home-view__hero app-hero">
      <div class="home-view__hero-main">
        <p class="home-view__eyebrow">Community Home</p>
        <h1>发现新内容，加入真实讨论</h1>
        <p class="home-view__hero-desc">
<!--          在这里浏览最新帖子、关注高热话题、进入作者主页继续互动。当前首页聚焦内容发现、关系建立与发帖入口三类核心动作。-->
        </p>

        <div class="app-stat-grid home-view__hero-stats">
          <article class="app-stat-card">
            <strong>{{ page.total }}</strong>
            <span>当前帖子量</span>
            <small>已接入最新/热门排序浏览</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ query.sort === 'hot' ? '热门优先' : '最新优先' }}</strong>
            <span>当前内容流</span>
            <small>{{ query.keyword?.trim() ? `关键词：${query.keyword.trim()}` : '未启用关键词筛选' }}</small>
          </article>
          <article class="app-stat-card">
            <strong>{{ authStore.isLoggedIn ? '已登录' : '游客模式' }}</strong>
            <span>当前身份</span>
            <small>{{ authStore.isLoggedIn ? '可直接发布、互动、私信' : '登录后解锁完整互动能力' }}</small>
          </article>
        </div>
      </div>

      <div class="home-view__hero-actions">
        <div class="home-view__hero-cta">
          <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">
            <a-button type="primary" size="large">发布帖子</a-button>
          </RouterLink>
          <RouterLink v-else to="/login">
            <a-button type="primary" size="large">登录后发布</a-button>
          </RouterLink>
          <RouterLink to="/users/search">
            <a-button size="large">去找人</a-button>
          </RouterLink>
        </div>

        <div class="home-view__quick-links">
          <RouterLink class="app-chip" to="/users/search">
            <icon-search />
            内容作者
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" class="app-chip" to="/chat">
            <icon-message />
            私信聊天
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" class="app-chip" to="/notifications">
            <icon-notification />
            消息提醒
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" class="app-chip" to="/me">
            <icon-user />
            个人中心
          </RouterLink>
        </div>
      </div>
    </div>

    <div class="home-view__grid">
      <div class="home-view__content">
        <a-card :bordered="false" class="home-view__filters app-panel">
          <div class="home-view__filters-main">
            <div>
              <p class="home-view__section-eyebrow">内容筛选</p>
              <h2>按关键词和排序快速浏览帖子</h2>
            </div>

            <div class="home-view__filter-controls">
              <a-input-search
                v-model="query.keyword"
                placeholder="搜索标题或摘要"
                allow-clear
                @search="reload"
                @press-enter="reload"
              />
              <a-select v-model="query.sort" class="home-view__sort" @change="reload">
                <a-option value="latest">最新发布</a-option>
                <a-option value="hot">热门优先</a-option>
              </a-select>
            </div>

            <div class="home-view__filter-summary">
              <span class="app-chip">
                <icon-calendar />
                排序：{{ query.sort === 'hot' ? '热门优先' : '最新优先' }}
              </span>
              <span class="app-chip">
                <icon-file />
                当前页：第 {{ page.page }} 页
              </span>
              <span class="app-chip">
                <icon-fire />
                匹配内容：{{ page.total }}
              </span>
            </div>
          </div>
        </a-card>

        <a-spin :loading="loading">
          <div v-if="posts.length" class="home-view__list">
            <PostCard v-for="post in posts" :key="post.id" :post="post" />
          </div>
          <div v-else class="app-empty-state app-empty-state--center">
            <p class="app-empty-state__eyebrow">Empty Feed</p>
            <h3 class="app-empty-state__title">暂时还没有帖子内容</h3>
            <p class="app-empty-state__desc">你可以先发第一条帖子，或者切换搜索关键词与排序条件，再看看是否有内容出现。</p>
            <div class="app-empty-state__actions">
              <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">
                <a-button type="primary">发布第一条帖子</a-button>
              </RouterLink>
              <a-button @click="reload">重新加载</a-button>
            </div>
          </div>
        </a-spin>

        <div v-if="page.total > page.size" class="home-view__pager">
          <a-pagination :current="page.page" :page-size="page.size" :total="page.total" @change="changePage" />
        </div>
      </div>

      <aside class="home-view__sidebar">
        <section class="app-panel home-view__side-card">
          <div class="home-view__side-head">
            <div>
              <p class="home-view__section-eyebrow">为你推荐</p>
              <h3>{{ authStore.isLoggedIn ? '基于你的浏览与互动' : '当前热门内容' }}</h3>
            </div>
            <span>{{ recommendPosts.length }}</span>
          </div>
          <div v-if="recommendLoading" class="home-view__recommend-list">
            <div v-for="index in 3" :key="index" class="home-view__recommend-skeleton">
              <div class="app-skeleton app-skeleton--title"></div>
              <div class="app-skeleton app-skeleton--text-short"></div>
              <div class="app-skeleton app-skeleton--text"></div>
            </div>
          </div>
          <div v-else-if="recommendPosts.length" class="home-view__recommend-list">
            <RouterLink
              v-for="item in recommendPosts"
              :key="item.id"
              :to="{
                name: 'post-detail',
                params: { postId: item.id },
                query: buildFeedRouteQuery({ ...query, page: page.page, size: page.size }),
              }"
              class="home-view__recommend-item"
            >
              <strong>{{ item.title }}</strong>
              <span>{{ item.recommendReason || (authStore.isLoggedIn ? '根据你的兴趣推荐' : '当前热门内容') }}</span>
              <small>
                {{ item.author?.nickname || item.author?.username || '匿名用户' }} ·
                {{ item.likeCount || 0 }} 赞 · {{ item.commentCount || 0 }} 评
              </small>
            </RouterLink>
          </div>
          <p v-else class="home-view__side-empty">推荐内容生成中，稍后再看看。</p>
        </section>

        <section class="app-panel home-view__side-card home-view__quickstart-card">
          <p class="home-view__section-eyebrow">快速开始</p>
          <h3>让你的内容被看见</h3>
          <p>优先完成发帖、找人、进入个人中心这三件事，就能最快体验当前社区主流程。</p>
          <div class="home-view__side-actions">
            <RouterLink v-if="authStore.isLoggedIn" to="/posts/publish">
              <a-button type="primary" long>去发帖</a-button>
            </RouterLink>
            <RouterLink v-else to="/login">
              <a-button type="primary" long>先登录</a-button>
            </RouterLink>
            <RouterLink to="/users/search">
              <a-button long>去找人</a-button>
            </RouterLink>
          </div>
        </section>

        <section class="app-panel home-view__side-card">
          <div class="home-view__side-head">
            <div>
              <p class="home-view__section-eyebrow">热门标签</p>
              <h3>从主题切入内容流</h3>
            </div>
            <span>{{ tagList.length }}</span>
          </div>
          <div v-if="tagList.length" class="home-view__tag-cloud">
            <button
              v-for="tag in tagList"
              :key="tag.id"
              type="button"
              :class="['home-view__tag-chip', { 'home-view__tag-chip--active': query.tagId === tag.id }]"
              @click="selectTag(tag.id)"
            >
              # {{ tag.name }}
            </button>
          </div>
          <p v-else class="home-view__side-empty">标签加载中或暂未配置。</p>
        </section>

        <section class="app-panel home-view__side-card">
          <div class="home-view__side-head">
            <div>
              <p class="home-view__section-eyebrow">本页摘要</p>
              <h3>当前浏览状态</h3>
            </div>
          </div>
          <ul class="home-view__summary-list">
            <li>
              <strong>{{ featuredTitle }}</strong>
              <span>当前列表首条内容标题</span>
            </li>
            <li>
              <strong>{{ coverCount }}</strong>
              <span>本页带封面内容数</span>
            </li>
            <li>
              <strong>{{ totalCommentCount }}</strong>
              <span>本页累计评论量</span>
            </li>
          </ul>
        </section>

        <section class="app-panel home-view__side-card">
          <div class="home-view__side-head">
            <div>
              <p class="home-view__section-eyebrow">社区说明</p>
              <h3>当前首页定位</h3>
            </div>
          </div>
          <p class="home-view__side-copy">
            当前首页以“内容发现”为主，围绕帖子流、作者入口与发帖入口建立主路径。下一轮可继续叠加推荐作者、活跃榜单、热帖榜与移动端底部导航。
          </p>
        </section>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
/**
 * 首页：社区内容流入口，承载列表筛选、内容发现与快捷互动。
 */
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconCalendar,
  IconFile,
  IconFire,
  IconMessage,
  IconNotification,
  IconSearch,
  IconUser,
} from '@arco-design/web-vue/es/icon'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import PostCard from '@/components/post/PostCard.vue'
import { fetchPosts, fetchRecommendPosts, fetchTags } from '@/api/post'
import { useAuthStore } from '@/stores/auth'
import type { PostListItem, PostQuery, Tag } from '@/types/post'
import { buildFeedRouteQuery, clearFeedScroll, loadFeedScroll, parseFeedRouteQuery, saveFeedScroll } from '@/utils/feed'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const recommendLoading = ref(false)
const posts = ref<PostListItem[]>([])
const recommendPosts = ref<PostListItem[]>([])
const tagList = ref<Tag[]>([])
const query = reactive<PostQuery>({
  page: 1,
  size: 10,
  sort: 'latest',
  keyword: '',
  tagId: undefined,
})
const page = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const featuredTitle = computed(() => posts.value[0]?.title || '等待内容加载')
const coverCount = computed(() => posts.value.filter((post) => Boolean(post.coverUrl)).length)
const totalCommentCount = computed(() => posts.value.reduce((sum, post) => sum + (post.commentCount || 0), 0))

// load* 系列方法负责从后端拉取页面初始化数据，统一控制 loading 和错误提示。
// 首页是内容发现主入口，筛选条件变化后都复用这一个查询方法。
async function loadPosts() {
  loading.value = true
  try {
    const result = await fetchPosts({
      ...query,
      keyword: query.keyword?.trim() || undefined,
    })
    posts.value = result.list
    page.page = result.page
    page.size = result.size
    page.total = result.total
  } catch (error) {
    Message.error(error instanceof Error ? error.message : '帖子加载失败')
  } finally {
    loading.value = false
  }
}

async function loadRecommend() {
  recommendLoading.value = true
  try {
    recommendPosts.value = await fetchRecommendPosts(6)
  } catch (error) {
    recommendPosts.value = []
    Message.error(error instanceof Error ? error.message : '推荐内容加载失败')
  } finally {
    recommendLoading.value = false
  }
}

async function loadTags() {
  try {
    tagList.value = await fetchTags()
  } catch {
    tagList.value = []
  }
}

function reload() {
  updateFeedRoute({
    ...query,
    page: 1,
  })
}

// 分页切换时只修改页码，再复用同一套加载逻辑，避免数据请求分散。
function changePage(current: number) {
  updateFeedRoute({
    ...query,
    page: current,
  })
}

function selectTag(tagId: number) {
  updateFeedRoute({
    ...query,
    page: 1,
    tagId: query.tagId === tagId ? undefined : tagId,
  })
}

function syncQueryFromRoute() {
  const parsed = parseFeedRouteQuery(route.query)
  query.page = parsed.page
  query.size = parsed.size
  query.sort = parsed.sort
  query.keyword = parsed.keyword
  query.tagId = parsed.tagId
}

function updateFeedRoute(nextQuery: Partial<PostQuery>, options: { restoreFeed?: boolean } = {}) {
  router.replace({
    name: 'home',
    query: buildFeedRouteQuery({
      page: nextQuery.page ?? 1,
      size: nextQuery.size ?? query.size ?? 10,
      sort: nextQuery.sort ?? query.sort ?? 'latest',
      keyword: nextQuery.keyword ?? query.keyword ?? '',
      tagId: nextQuery.tagId,
      restoreFeed: options.restoreFeed,
    }),
  })
}

async function restoreFeedScrollIfNeeded() {
  const restoreFeed = (Array.isArray(route.query.restoreFeed) ? route.query.restoreFeed[0] : route.query.restoreFeed) === '1'
  if (!restoreFeed) {
    return
  }
  const scrollY = loadFeedScroll(parseFeedRouteQuery(route.query))
  if (scrollY === null) {
    return
  }
  await nextTick()
  window.scrollTo({ top: scrollY, behavior: 'auto' })
  clearFeedScroll()
}

onBeforeRouteLeave((to) => {
  if (to.name === 'post-detail') {
    saveFeedScroll(parseFeedRouteQuery(route.query), window.scrollY)
  }
})

watch(
  () => route.fullPath,
  async () => {
    syncQueryFromRoute()
    await Promise.all([loadPosts(), loadRecommend()])
    await restoreFeedScrollIfNeeded()
  },
  { immediate: true },
)

watch(
  () => authStore.isLoggedIn,
  async () => {
    await loadRecommend()
  },
)

onMounted(async () => {
  await loadTags()
})
</script>

<style scoped lang="scss">
.home-view__hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 0.9fr);
  gap: 24px;
}

.home-view__hero-main {
  display: grid;
  gap: 18px;
}

.home-view__eyebrow,
.home-view__section-eyebrow {
  margin: 0;
  color: var(--app-primary);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.home-view__hero h1 {
  margin: 0;
  color: var(--app-text-1);
  font-size: clamp(34px, 6vw, 60px);
  line-height: 1.04;
}

.home-view__hero-desc {
  max-width: 640px;
  margin: 0;
  color: var(--app-text-3);
  line-height: 1.9;
}

.home-view__hero-stats {
  max-width: 760px;
}

.home-view__hero-actions {
  display: grid;
  align-content: center;
  gap: 14px;
}

.home-view__hero-cta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.home-view__quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.home-view__grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  align-items: start;
}

.home-view__content {
  display: grid;
  gap: 18px;
}

.home-view__filters {
  :deep(.arco-card-body) {
    padding: 0;
  }
}

.home-view__filters-main {
  display: grid;
  gap: 18px;
}

.home-view__filters-main h2 {
  margin: 6px 0 0;
  color: var(--app-text-1);
  font-size: var(--app-font-title-3);
}

.home-view__filter-controls {
  display: grid;
  grid-template-columns: 1fr 180px;
  gap: 12px;
}

.home-view__filter-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.home-view__list {
  display: grid;
  gap: 16px;
}

.home-view__pager {
  display: flex;
  justify-content: center;
}

.home-view__sidebar {
  display: grid;
  gap: 16px;
  position: sticky;
  top: 132px;
}

.home-view__side-card {
  display: grid;
  gap: 14px;
}

.home-view__side-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.home-view__side-head h3 {
  margin: 6px 0 0;
  color: var(--app-text-1);
  font-size: 20px;
}

.home-view__side-head span {
  color: var(--app-text-3);
  font-size: 13px;
  font-weight: 700;
}

.home-view__side-card p {
  margin: 0;
  color: var(--app-text-3);
  line-height: 1.75;
}

.home-view__side-actions {
  display: grid;
  gap: 10px;
}

.home-view__quickstart-card {
  gap: 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.92));
}

.home-view__quickstart-card h3 {
  margin: 4px 0 0;
  color: var(--app-text-1);
  font-size: 26px;
  line-height: 1.2;
}

.home-view__quickstart-card p {
  line-height: 1.85;
}

.home-view__quickstart-card .home-view__side-actions {
  gap: 12px;
  margin-top: 4px;
}

.home-view__quickstart-card .home-view__side-actions :deep(.arco-btn) {
  min-height: 42px;
  font-weight: 700;
}

.home-view__quickstart-card .home-view__side-actions :deep(.arco-btn-primary) {
  box-shadow: 0 14px 30px rgba(37, 99, 235, 0.18);
}

.home-view__recommend-list {
  display: grid;
  gap: 10px;
}

.home-view__recommend-item,
.home-view__recommend-skeleton {
  display: grid;
  gap: 8px;
  padding: 14px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid var(--app-border-color-soft);
  border-radius: var(--app-radius-md);
}

.home-view__recommend-item {
  color: inherit;
  text-decoration: none;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.home-view__recommend-item:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.18);
  box-shadow: var(--app-shadow-xs);
}

.home-view__recommend-item strong {
  color: var(--app-text-1);
  line-height: 1.5;
}

.home-view__recommend-item span,
.home-view__recommend-item small {
  color: var(--app-text-3);
  line-height: 1.6;
}

.home-view__recommend-item span {
  font-size: 12px;
  font-weight: 700;
  color: var(--app-primary);
}

.home-view__recommend-item small {
  font-size: 12px;
}

.home-view__tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.home-view__tag-chip {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  padding: 0 12px;
  color: var(--app-text-2);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid var(--app-border-color);
  border-radius: var(--app-radius-pill);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    color 0.18s ease,
    background-color 0.18s ease;
}

.home-view__tag-chip:hover {
  transform: translateY(-1px);
  border-color: rgba(15, 118, 110, 0.18);
}

.home-view__tag-chip--active {
  color: var(--app-primary);
  background: rgba(236, 253, 245, 0.95);
  border-color: rgba(15, 118, 110, 0.22);
}

.home-view__summary-list {
  display: grid;
  gap: 12px;
  padding: 0;
  margin: 0;
  list-style: none;
}

.home-view__summary-list li {
  display: grid;
  gap: 6px;
  padding: 14px;
  background: rgba(248, 250, 252, 0.88);
  border: 1px solid var(--app-border-color-soft);
  border-radius: var(--app-radius-md);
}

.home-view__summary-list strong {
  color: var(--app-text-1);
  font-size: 14px;
}

.home-view__summary-list span,
.home-view__side-empty,
.home-view__side-copy {
  color: var(--app-text-3);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .home-view__grid {
    grid-template-columns: 1fr;
  }

  .home-view__sidebar {
    position: static;
    top: auto;
  }
}

@media (max-width: 860px) {
  .home-view__hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .home-view__hero {
    gap: 18px;
  }

  .home-view__hero h1 {
    font-size: clamp(28px, 8vw, 38px);
  }

  .home-view__hero-desc {
    line-height: 1.75;
  }

  .home-view__filter-controls {
    grid-template-columns: 1fr;
  }

  .home-view__hero-cta {
    display: grid;
    grid-template-columns: 1fr;
  }

  .home-view__quick-links {
    width: 100%;
  }

  .home-view__quick-links :deep(.app-chip),
  .home-view__filter-summary :deep(.app-chip) {
    min-height: 36px;
    padding-inline: 10px;
    font-size: 12px;
  }

  .home-view__side-card {
    gap: 12px;
  }
}
</style>
