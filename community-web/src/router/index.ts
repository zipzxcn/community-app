/**
 * 前端路由表：
 * - 通过 createWebHistory 使用 HTML5 History 模式。
 * - 业务上把首页、发帖、用户中心、通知、聊天等模块挂到统一布局下。
 * - requiresAuth 元信息用于登录拦截。
 */
import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
      // 所有一期 MVP 页面都复用 DefaultLayout，统一导航、未读徽标与移动端底栏。
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/home/HomeView.vue'),
        },
        {
          path: 'posts/publish',
          name: 'post-publish',
          component: () => import('@/views/post/PostPublishView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'posts/:postId/edit',
          name: 'post-edit',
          component: () => import('@/views/post/PostPublishView.vue'),
          props: (route) => ({ postId: Number(route.params.postId) }),
          meta: { requiresAuth: true },
        },
        {
          path: 'posts/:postId',
          name: 'post-detail',
          component: () => import('@/views/post/PostDetailView.vue'),
          props: (route) => ({ postId: Number(route.params.postId) }),
        },
        {
          path: 'drafts',
          name: 'drafts',
          component: () => import('@/views/draft/DraftListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'histories',
          name: 'histories',
          component: () => import('@/views/history/HistoryListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'users/search',
          name: 'user-search',
          component: () => import('@/views/user/UserSearchView.vue'),
        },
        {
          path: 'users/:userId',
          name: 'user-profile',
          component: () => import('@/views/user/UserProfileView.vue'),
          props: (route) => ({ userId: Number(route.params.userId) }),
        },
        {
          path: 'me',
          name: 'me-center',
          component: () => import('@/views/user/UserCenterView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'notifications',
          name: 'notifications',
          component: () => import('@/views/notification/NotificationCenterView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/chat/ChatView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('@/views/auth/LoginView.vue'),
        },
        {
          path: 'register',
          name: 'register',
          component: () => import('@/views/auth/RegisterView.vue'),
        },
      ],
    },
  ],
})

/**
 * 全局前置守卫：
 * 1) 先尝试从 localStorage 恢复登录态。
 * 2) 未登录访问受保护页面时跳转登录页。
 * 3) 已登录用户访问登录/注册页时重定向回首页。
 */
router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  await authStore.restore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && authStore.isLoggedIn) {
    return { name: 'home' }
  }
  return true
})

export default router
