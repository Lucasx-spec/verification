<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useSessionActions } from '../utils/session'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { logout } = useSessionActions(router)

const menus = computed(() => {
  const baseMenus = [
    { label: '总览', index: '/dashboard', icon: 'DataAnalysis' },
    { label: '我的资料', index: '/profile', icon: 'User' },
    { label: '数字签名', index: '/sign', icon: 'EditPen' },
    { label: '验签链接', index: '/verify-links', icon: 'Link' },
    { label: '验签记录', index: '/verify-records', icon: 'DocumentChecked' },
    { label: '审计日志', index: '/audit', icon: 'Tickets' },
  ]

  if (authStore.isAdmin) {
    baseMenus.push({ label: '完整性校验', index: '/admin/integrity', icon: 'Shield' })
  }

  return baseMenus
})

const activeMenu = computed(() => route.path)
</script>

<template>
  <div class="shell-layout">
    <aside class="shell-sidebar">
      <div class="brand-card">
        <div class="brand-chip">SM2 / SM3</div>
        <h1>数字签名与在线验证平台</h1>
        <p>围绕签名、验签、审计与链完整性校验构建的一体化控制台。</p>
      </div>

      <el-menu :default-active="activeMenu" class="nav-menu" router>
        <el-menu-item v-for="menu in menus" :key="menu.index" :index="menu.index">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="shell-main">
      <header class="shell-header glass-card">
        <div>
          <div class="eyebrow">Verification Console</div>
          <div class="page-title">{{ route.meta.title || '业务控制台' }}</div>
        </div>
        <div class="header-actions">
          <el-tag :type="authStore.isAdmin ? 'danger' : 'info'" effect="dark">
            {{ authStore.user?.roleCode || 'GUEST' }}
          </el-tag>
          <div class="user-brief">
            <span class="user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
            <span class="user-sub">{{ authStore.user?.username }}</span>
          </div>
          <el-button type="primary" plain @click="logout">退出</el-button>
        </div>
      </header>

      <main class="shell-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
