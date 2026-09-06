import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/shares' },
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('@/views/Register.vue'), meta: { public: true } },
  { path: '/shares', component: () => import('@/views/Shares.vue'), meta: { public: true } },
  { path: '/activities', component: () => import('@/views/Activities.vue'), meta: { public: true } },
  { path: '/activities/:id', component: () => import('@/views/ActivityDetail.vue'), meta: { public: true } },
  { path: '/notes', component: () => import('@/views/MyNotes.vue'), meta: { requiresAuth: true } },
  { path: '/admin', component: () => import('@/views/Admin.vue'), meta: { requiresAuth: true, requiresAdmin: true } },
  { path: '/:pathMatch(.*)*', redirect: '/shares' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('cn_token')
  const user = JSON.parse(localStorage.getItem('cn_user') || 'null')
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !(user && user.cn_user_role === 'admin')) {
    return '/shares'
  }
  return true
})

export default router
