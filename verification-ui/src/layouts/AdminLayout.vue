<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useSessionActions } from '../utils/session'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { logout } = useSessionActions(router)

const menus = computed(() => [
  { label: '管理总览', index: '/admin/dashboard', icon: 'Monitor' },
  { label: '审计日志', index: '/admin/audit', icon: 'Tickets' },
  { label: '完整性校验', index: '/admin/integrity', icon: 'Shield' },
  { label: '系统参数', index: '/admin/settings', icon: 'Setting' },
])

const activeMenu = computed(() => route.path)
</script>

<template>
  <div class="shell-layout admin-shell">
    <aside class="shell-sidebar admin-sidebar">
      <div class="brand-card admin-brand-card">
        <div class="brand-chip">ADMIN CONSOLE</div>
        <h1>管理员控制端</h1>
        <p>面向系统管理员的审计日志、链完整性校验与全局运行观察台。</p>
      </div>

      <el-menu :default-active="activeMenu" class="nav-menu admin-menu" router>
        <el-menu-item v-for="menu in menus" :key="menu.index" :index="menu.index">
          <el-icon><component :is="menu.icon" /></el-icon>
          <span>{{ menu.label }}</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="shell-main">
      <header class="shell-header glass-card admin-header">
        <div>
          <div class="eyebrow">Admin Console</div>
          <div class="page-title">{{ route.meta.title || '管理员控制端' }}</div>
        </div>
        <div class="header-actions">
          <el-tag type="danger" effect="dark">{{ authStore.user?.roleCode || 'ADMIN' }}</el-tag>
          <div class="user-brief">
            <span class="user-name">{{ authStore.user?.realName || authStore.user?.username }}</span>
            <span class="user-sub">{{ authStore.user?.username }}</span>
          </div>
          <el-button type="danger" plain @click="logout">退出</el-button>
        </div>
      </header>

      <main class="shell-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
