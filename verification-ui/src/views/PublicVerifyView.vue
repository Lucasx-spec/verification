<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { systemApi, verifyApi } from '../api'

const route = useRoute()
const verifyLoading = ref(false)
const detailLoading = ref(false)
const uploadFile = ref(null)
const result = ref(null)
const detail = ref(null)
const uploadMaxFileSizeMb = ref(50)

const verifyToken = computed(() => route.params.token || '')

function onUploadChange(file) {
  const rawFile = file.raw
  const maxFileSizeBytes = uploadMaxFileSizeMb.value * 1024 * 1024
  if ((rawFile?.size || 0) > maxFileSizeBytes) {
    uploadFile.value = null
    ElMessage.warning(`上传文件超过大小限制，当前最大允许 ${uploadMaxFileSizeMb.value}MB`)
    return
  }
  uploadFile.value = rawFile
}

async function loadUploadLimit() {
  try {
    const response = await systemApi.getSettings()
    const limit = Number(response?.data?.uploadMaxFileSizeMb)
    if (Number.isFinite(limit) && limit > 0) {
      uploadMaxFileSizeMb.value = limit
    }
  } catch {
    uploadMaxFileSizeMb.value = 50
  }
}

async function loadPublicDetail() {
  if (!verifyToken.value) {
    return
  }
  detailLoading.value = true
  try {
    const response = await verifyApi.linkDetail(verifyToken.value)
    detail.value = response.data
  } catch {
    // message handled globally
  } finally {
    detailLoading.value = false
  }
}

async function submitVerify() {
  if (!verifyToken.value) {
    ElMessage.warning('未检测到验签令牌')
    return
  }
  if (!uploadFile.value) {
    ElMessage.warning('请先选择待验签文件')
    return
  }

  verifyLoading.value = true
  try {
    const formData = new FormData()
    formData.append('verifyToken', verifyToken.value)
    formData.append('file', uploadFile.value)
    const response = await verifyApi.verifyFile(formData)
    result.value = response.data
    ElMessage.success(response.message || '验签完成')
  } catch {
    // message handled globally
  } finally {
    verifyLoading.value = false
  }
}

onMounted(() => {
  loadUploadLimit()
  loadPublicDetail()
})
</script>

<template>
  <div class="auth-page public-verify-page">
    <section class="public-verify-shell glass-card">
      <div class="public-verify-head">
        <div>
          <div class="eyebrow">Public Verify Portal</div>
          <h1>公开在线验签</h1>
          <p>页面已自动读取验签令牌，你只需要上传待验证文件即可。</p>
        </div>
        <el-tag type="success" effect="dark">{{ verifyToken }}</el-tag>
      </div>

      <section class="two-column-layout verify-layout">
        <el-card shadow="never" class="glass-card panel-card">
          <template #header>
            <div class="panel-header">
              <span>上传文件验签</span>
              <el-tag type="info">自动带入 Token</el-tag>
            </div>
          </template>

          <el-form label-position="top" class="form-stack">
            <el-alert :title="`当前文件上传大小限制：${uploadMaxFileSizeMb}MB`" type="info" show-icon :closable="false" />
            <el-form-item label="验签令牌">
              <el-input :model-value="verifyToken" readonly />
            </el-form-item>
            <el-form-item label="选择待验签文件">
              <el-upload :auto-upload="false" :limit="1" :on-change="onUploadChange">
                <el-button type="primary" plain>选择文件</el-button>
              </el-upload>
            </el-form-item>
            <el-button type="success" :loading="verifyLoading" @click="submitVerify">开始验签</el-button>
          </el-form>
        </el-card>

        <el-card shadow="never" class="glass-card panel-card code-panel">
          <template #header>
            <div class="panel-header">
              <span>验签链接信息</span>
              <el-tag v-if="detail" :type="detail.status === 'ACTIVE' ? 'success' : 'info'">{{ detail.status }}</el-tag>
            </div>
          </template>

          <el-skeleton :loading="detailLoading" animated :rows="5">
            <el-empty v-if="!detail" description="未获取到验签链接详情，可能令牌无效或无权查看详情。" />
            <div v-else class="detail-grid">
              <div><span>签名编号</span><strong>{{ detail.signNo }}</strong></div>
              <div><span>文件名</span><strong>{{ detail.fileName }}</strong></div>
              <div><span>签名人</span><strong>{{ detail.signerName }}</strong></div>
              <div><span>访问次数</span><strong>{{ detail.currentAccessCount }}</strong></div>
            </div>
          </el-skeleton>
        </el-card>
      </section>

      <el-card shadow="never" class="glass-card panel-card" v-if="result">
        <template #header>
          <div class="panel-header">
            <span>验签结果</span>
            <el-tag :type="result.verifyResult === 'PASSED' ? 'success' : 'danger'">{{ result.verifyResult }}</el-tag>
          </div>
        </template>
        <div class="detail-grid">
          <div><span>签名编号</span><strong>{{ result.signNo }}</strong></div>
          <div><span>签名人</span><strong>{{ result.signerName }}</strong></div>
          <div><span>摘要算法</span><strong>{{ result.digestAlgorithm }}</strong></div>
          <div><span>签名算法</span><strong>{{ result.signatureAlgorithm }}</strong></div>
          <div class="detail-block"><span>结果说明</span><code>{{ result.resultMessage }}</code></div>
          <div class="detail-block"><span>上传文件摘要</span><code>{{ result.fileDigest }}</code></div>
        </div>
      </el-card>
    </section>
  </div>
</template>
