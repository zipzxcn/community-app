import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: DefaultLayout,
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
