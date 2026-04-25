<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { signApi, verifyApi, auditApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const signRecords = ref([])
const verifyLinks = ref([])
const verifyRecords = ref([])
const auditLogs = ref([])
const loading = ref(false)

const metrics = computed(() => [
  { label: '签名记录', value: signRecords.value.length, accent: 'azure' },
  { label: '验签链接', value: verifyLinks.value.length, accent: 'orange' },
  { label: '验签记录', value: verifyRecords.value.length, accent: 'green' },
  { label: '审计日志', value: auditLogs.value.length, accent: 'violet' },
])

onMounted(async () => {
  loading.value = true
  try {
    const [signRes, linkRes, recordRes, auditRes] = await Promise.all([
      signApi.list(),
      verifyApi.listLinks(),
      verifyApi.listRecords(),
      auditApi.logs(),
    ])
    signRecords.value = signRes.data || []
    verifyLinks.value = linkRes.data || []
    verifyRecords.value = recordRes.data || []
    auditLogs.value = auditRes.data || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-stack">
    <section class="hero-panel glass-card">
      <div>
        <div class="eyebrow">欢迎回来</div>
        <h2>{{ authStore.user?.realName || authStore.user?.username }}</h2>
        <p>
          在这里你可以完成文件摘要计算、SM2 签名记录创建、验签链接生成、公开验签与链式审计查看。
        </p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/sign')">立即签名</el-button>
        <el-button plain @click="router.push('/verify-links')">管理验签链接</el-button>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" class="metric-card glass-card" :data-accent="metric.accent">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
      </article>
    </section>

    <section class="dashboard-grid">
      <el-card shadow="never" class="glass-card panel-card">
        <template #header>
          <div class="panel-header">
            <span>最近签名</span>
            <el-button link @click="router.push('/sign')">查看全部</el-button>
          </div>
        </template>
        <el-skeleton :loading="loading" animated :rows="4">
          <div v-if="signRecords.length" class="timeline-list">
            <div v-for="item in signRecords.slice(0, 4)" :key="item.signNo" class="timeline-item">
              <strong>{{ item.fileName }}</strong>
              <span>{{ item.signNo }}</span>
              <small>{{ item.signTime }}</small>
            </div>
          </div>
          <el-empty v-else description="还没有签名记录" />
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="glass-card panel-card">
        <template #header>
          <div class="panel-header">
            <span>最近验签</span>
            <el-button link @click="router.push('/verify-records')">查看全部</el-button>
          </div>
        </template>
        <el-skeleton :loading="loading" animated :rows="4">
          <div v-if="verifyRecords.length" class="timeline-list">
            <div v-for="item in verifyRecords.slice(0, 4)" :key="item.verifyNo" class="timeline-item">
              <strong>{{ item.uploadFileName || '公开验签请求' }}</strong>
              <span>{{ item.verifyResult }}</span>
              <small>{{ item.verifyTime }}</small>
            </div>
          </div>
          <el-empty v-else description="还没有验签记录" />
        </el-skeleton>
      </el-card>
    </section>
  </div>
</template>
