import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/verify/:token',
      name: 'public-verify',
      component: () => import('../views/PublicVerifyView.vue'),
      meta: { title: '公开在线验签' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { guestOnly: true, title: '登录 / 注册' },
    },
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/workspace',
      component: () => import('../layouts/UserLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/workspace/dashboard' },
        { path: 'dashboard', name: 'user-dashboard', component: () => import('../views/UserDashboardView.vue'), meta: { title: '用户业务总览' } },
        { path: 'profile', name: 'profile', component: () => import('../views/ProfileView.vue'), meta: { title: '我的资料' } },
        { path: 'sign', name: 'sign', component: () => import('../views/SignView.vue'), meta: { title: '数字签名' } },
        { path: 'verify-links', name: 'verify-links', component: () => import('../views/VerifyLinksView.vue'), meta: { title: '验签链接' } },
        { path: 'verify-records', name: 'verify-records', component: () => import('../views/VerifyRecordsView.vue'), meta: { title: '验签记录' } },
      ],
    },
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/dashboard' },
        { path: 'dashboard', name: 'admin-dashboard', component: () => import('../views/AdminDashboardView.vue'), meta: { title: '管理员控制台' } },
        { path: 'audit', name: 'audit', component: () => import('../views/AuditView.vue'), meta: { title: '审计日志', requiresAdmin: true } },
        { path: 'integrity', name: 'admin-integrity', component: () => import('../views/AdminIntegrityView.vue'), meta: { title: '日志完整性校验', requiresAdmin: true } },
        { path: 'settings', name: 'admin-settings', component: () => import('../views/AdminSettingsView.vue'), meta: { title: '系统参数维护', requiresAdmin: true } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  authStore.init()

  if (to.path.startsWith('/verify/')) {
    return true
  }

  if (authStore.token && !authStore.user) {
    try {
      await authStore.fetchCurrentUser()
    } catch (error) {
      authStore.logout()
    }
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return true
  }

  if (to.path.startsWith('/admin') && !authStore.isAdmin) {
    return { name: 'user-dashboard' }
  }

  if (to.path.startsWith('/workspace') && authStore.isAdmin) {
    return { name: 'admin-dashboard' }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'user-dashboard' }
  }

  return true
})

export default router
