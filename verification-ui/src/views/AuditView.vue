<script setup>
import { onMounted, ref } from 'vue'
import { auditApi } from '../api'

const loading = ref(false)
const logs = ref([])

async function loadLogs() {
  loading.value = true
  try {
    const response = await auditApi.adminLogs()
    logs.value = response.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadLogs)
</script>

<template>
  <div class="page-stack">
    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>全平台审计日志</span>
          <el-button link @click="loadLogs">刷新</el-button>
        </div>
      </template>
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="logNo" label="日志编号" min-width="220" />
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" min-width="180" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="180" />
        <el-table-column prop="operationResult" label="结果" width="110">
          <template #default="scope">
            <el-tag :type="scope.row.operationResult === 'SUCCESS' ? 'success' : 'danger'">{{ scope.row.operationResult }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultMessage" label="结果说明" min-width="220" />
        <el-table-column prop="chainIndex" label="链序号" width="100" />
        <el-table-column prop="operationTime" label="操作时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>
