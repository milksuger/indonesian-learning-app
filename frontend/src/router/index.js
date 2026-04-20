import { createRouter, createWebHistory } from 'vue-router'

/**
 * 前端路由配置
 * 定义所有页面路径与对应的视图组件
 */
const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
  { path: '/learn', name: 'learn', component: () => import('../views/LearnView.vue') },
  { path: '/review', name: 'review', component: () => import('../views/ReviewView.vue') },
  { path: '/wordbooks', name: 'wordbooks', component: () => import('../views/WordbooksView.vue') },
  { path: '/wordbooks/:id/words', name: 'wordbook-words', component: () => import('../views/WordbookWordsView.vue') },
  { path: '/favorites', name: 'favorites', component: () => import('../views/FavoritesView.vue') },
  { path: '/memorized', name: 'memorized', component: () => import('../views/MemorizedView.vue') },
  { path: '/mistakes', name: 'mistakes', component: () => import('../views/MistakesView.vue') },
  { path: '/settings', name: 'settings', component: () => import('../views/SettingsView.vue') },
  { path: '/profile', name: 'profile', component: () => import('../views/ProfileView.vue') },
  { path: '/today', name: 'today', component: () => import('../views/TodayView.vue') },
  { path: '/random', name: 'random', component: () => import('../views/RandomView.vue') },
  { path: '/search', name: 'search', component: () => import('../views/WordSearchView.vue') },
  { path: '/stats', name: 'stats', component: () => import('../views/StatsView.vue') },
  { path: '/checkin', name: 'checkin', component: () => import('../views/CheckinView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：未登录用户重定向到登录页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const publicPages = ['/login', '/register']
  if (!token && !publicPages.includes(to.path)) {
    return '/login'
  }
  return true
})

export default router
