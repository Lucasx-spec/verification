<script setup>
import { onMounted, ref } from 'vue'
import { verifyApi } from '../api'

const loading = ref(false)
const records = ref([])

async function loadRecords() {
  loading.value = true
  try {
    const response = await verifyApi.listRecords()
    records.value = response.data || []
  } finally {
    loading.value = false
  }
}

onMounted(loadRecords)
</script>

<template>
  <div class="page-stack">
    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>验签记录</span>
          <el-button link @click="loadRecords">刷新</el-button>
        </div>
      </template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="verifyNo" label="验签编号" min-width="220" />
        <el-table-column prop="signNo" label="签名编号" min-width="220" />
        <el-table-column prop="uploadFileName" label="上传文件" min-width="160" />
        <el-table-column prop="verifyResult" label="验签结果" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.verifyResult === 'PASSED' ? 'success' : 'warning'">{{ scope.row.verifyResult }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultMessage" label="结果说明" min-width="240" />
        <el-table-column prop="verifyTime" label="验签时间" min-width="180" />
      </el-table>
    </el-card>
  </div>
</template>
