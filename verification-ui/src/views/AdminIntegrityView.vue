<script setup>
import { onMounted, ref } from 'vue'
import { auditApi } from '../api'

const loading = ref(false)
const integrity = ref(null)

async function loadIntegrity() {
  loading.value = true
  try {
    const response = await auditApi.integrity()
    integrity.value = response.data
  } finally {
    loading.value = false
  }
}

onMounted(loadIntegrity)
</script>

<template>
  <div class="page-stack">
    <section class="metric-grid">
      <article class="metric-card glass-card" data-accent="green">
        <span>完整性状态</span>
        <strong>{{ integrity?.valid ? '通过' : '异常' }}</strong>
      </article>
      <article class="metric-card glass-card" data-accent="azure">
        <span>日志总数</span>
        <strong>{{ integrity?.totalLogs || 0 }}</strong>
      </article>
      <article class="metric-card glass-card" data-accent="orange">
        <span>异常数量</span>
        <strong>{{ integrity?.issueCount || 0 }}</strong>
      </article>
    </section>

    <el-card shadow="never" class="glass-card panel-card code-panel">
      <template #header>
        <div class="panel-header">
          <span>最新链哈希</span>
          <el-button link @click="loadIntegrity">重新校验</el-button>
        </div>
      </template>
      <el-skeleton :loading="loading" animated :rows="4">
        <code class="hash-line">{{ integrity?.latestHash || '暂无数据' }}</code>
      </el-skeleton>
    </el-card>

    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>异常明细</span>
          <el-tag :type="integrity?.issueCount ? 'danger' : 'success'">{{ integrity?.issueCount || 0 }} 条</el-tag>
        </div>
      </template>
      <el-table :data="integrity?.issues || []" v-loading="loading" stripe>
        <el-table-column prop="chainIndex" label="链序号" width="100" />
        <el-table-column prop="logNo" label="日志编号" min-width="220" />
        <el-table-column prop="issueType" label="异常类型" min-width="180" />
        <el-table-column prop="message" label="说明" min-width="240" />
      </el-table>
      <el-empty v-if="integrity && !integrity.issueCount" description="当前审计链完整，未发现异常。" />
    </el-card>
  </div>
</template>
