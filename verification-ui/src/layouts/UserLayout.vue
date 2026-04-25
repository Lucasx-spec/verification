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
  { label: '业务总览', index: '/workspace/dashboard', icon: 'DataAnalysis' },
  { label: '我的资料', index: '/workspace/profile', icon: 'User' },
  { label: '数字签名', index: '/workspace/sign', icon: 'EditPen' },
  { label: '验签链接', index: '/workspace/verify-links', icon: 'Link' },
  { label: '验签记录', index: '/workspace/verify-records', icon: 'DocumentChecked' },
])

const activeMenu = computed(() => route.path)
</script>

<template>
  <div class="shell-layout">
    <aside class="shell-sidebar">
      <div class="brand-card">
        <div class="brand-chip">USER WORKSPACE</div>
        <h1>用户业务端</h1>
        <p>面向普通业务用户的数字签名、验签链接、个人验证记录与个人审计日志工作台。</p>
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
          <div class="eyebrow">User Workspace</div>
          <div class="page-title">{{ route.meta.title || '用户业务端' }}</div>
        </div>
        <div class="header-actions">
          <el-tag type="info" effect="dark">{{ authStore.user?.roleCode || 'USER' }}</el-tag>
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
