<script setup>
import { computed, onMounted, ref } from 'vue'
import { auditApi, signApi, verifyApi } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const auditLogs = ref([])
const signRecords = ref([])
const verifyRecords = ref([])
const integrity = ref(null)

const metrics = computed(() => [
  { label: '审计日志总数', value: auditLogs.value.length, accent: 'violet' },
  { label: '签名记录总数', value: signRecords.value.length, accent: 'azure' },
  { label: '验签记录总数', value: verifyRecords.value.length, accent: 'green' },
  { label: '完整性异常数', value: integrity.value?.issueCount || 0, accent: 'orange' },
])

onMounted(async () => {
  loading.value = true
  try {
    const [auditRes, signRes, verifyRes, integrityRes] = await Promise.all([
      auditApi.adminLogs(),
      signApi.adminList(),
      verifyApi.adminListRecords(),
      auditApi.integrity(),
    ])
    auditLogs.value = auditRes.data || []
    signRecords.value = signRes.data || []
    verifyRecords.value = verifyRes.data || []
    integrity.value = integrityRes.data || null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-stack">
    <section class="hero-panel glass-card admin-hero">
      <div>
        <div class="eyebrow">管理员视角</div>
        <h2>系统审计与完整性控制台</h2>
        <p>聚焦全局日志链、验签结果沉淀与平台运行可观察性。</p>
      </div>
      <div class="hero-actions">
        <el-button type="danger" @click="router.push('/admin/integrity')">查看完整性校验</el-button>
        <el-button plain @click="router.push('/admin/audit')">查看审计日志</el-button>
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
            <span>最近审计事件</span>
            <el-button link @click="router.push('/admin/audit')">查看全部</el-button>
          </div>
        </template>
        <el-skeleton :loading="loading" animated :rows="4">
          <div v-if="auditLogs.length" class="timeline-list">
            <div v-for="item in auditLogs.slice(0, 4)" :key="item.logNo" class="timeline-item">
              <strong>{{ item.operationType }}</strong>
              <span>{{ item.operationDesc }}</span>
              <small>{{ item.operationTime }}</small>
            </div>
          </div>
          <el-empty v-else description="暂无审计日志" />
        </el-skeleton>
      </el-card>

      <el-card shadow="never" class="glass-card panel-card">
        <template #header>
          <div class="panel-header">
            <span>完整性状态</span>
            <el-tag :type="integrity?.valid ? 'success' : 'danger'">{{ integrity?.valid ? '通过' : '异常' }}</el-tag>
          </div>
        </template>
        <el-skeleton :loading="loading" animated :rows="4">
          <div class="detail-grid" v-if="integrity">
            <div><span>日志总数</span><strong>{{ integrity.totalLogs || 0 }}</strong></div>
            <div><span>异常数量</span><strong>{{ integrity.issueCount || 0 }}</strong></div>
            <div class="detail-block"><span>最新链哈希</span><code>{{ integrity.latestHash || '暂无数据' }}</code></div>
          </div>
          <el-empty v-else description="暂无完整性结果" />
        </el-skeleton>
      </el-card>
    </section>
  </div>
</template>
