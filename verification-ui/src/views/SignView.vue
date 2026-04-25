<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { signApi, systemApi } from '../api'
import { digestFileHexByChunk, signDigestHex } from '../utils/crypto'
import { loadKeyPair } from '../utils/keypair'

const authStore = useAuthStore()
const records = ref([])
const loading = ref(false)
const signing = ref(false)
const generatedPrivateKey = ref('')
const storedPublicKey = ref('')
const fileRef = ref(null)
const selectedFiles = ref([])
const latestDetail = ref(null)
const keySource = ref('')
const batchResults = ref([])
const uploadMaxFileSizeMb = ref(50)

const form = reactive({
  signatureFormat: 'RS_HEX',
  signRemark: '',
})

const selectedFileCount = computed(() => selectedFiles.value.length)
const totalSelectedSizeText = computed(() => formatFileSize(selectedFiles.value.reduce((total, file) => total + (file.size || 0), 0)))
const completedCount = computed(() => selectedFiles.value.filter((file) => ['success', 'error'].includes(file.status)).length)
const successCount = computed(() => selectedFiles.value.filter((file) => file.status === 'success').length)
const errorCount = computed(() => selectedFiles.value.filter((file) => file.status === 'error').length)
const signingCount = computed(() => selectedFiles.value.filter((file) => file.status === 'signing').length)
const failedFiles = computed(() => selectedFiles.value.filter((file) => file.status === 'error'))
const batchProgressPercent = computed(() => {
  if (!selectedFiles.value.length) {
    return 0
  }
  return Math.round((completedCount.value / selectedFiles.value.length) * 100)
})
const batchProgressStatus = computed(() => {
  if (!selectedFiles.value.length || signing.value || errorCount.value === 0) {
    return ''
  }
  return errorCount.value > 0 ? 'exception' : 'success'
})
const accountPublicKey = computed(() => authStore.user?.publicKey || '')
const keyPairConsistent = computed(() => {
  if (!storedPublicKey.value || !accountPublicKey.value) {
    return false
  }
  return storedPublicKey.value === accountPublicKey.value
})
const keyPairStatusText = computed(() => {
  if (!generatedPrivateKey.value || !storedPublicKey.value) {
    return '请先到注册页生成并保存配套密钥对'
  }
  if (!accountPublicKey.value) {
    return '当前账户未检测到绑定公钥'
  }
  if (!keyPairConsistent.value) {
    return '本地密钥对与当前账户绑定公钥不一致，请先到“我的资料”重置并绑定新密钥对'
  }
  return keySource.value || '已加载可用密钥对'
})

function resolveStatusText(status) {
  const statusMap = {
    pending: '待处理',
    signing: '签名中',
    success: '成功',
    error: '失败',
  }
  return statusMap[status] || '待处理'
}

function resolveStatusClass(status) {
  const classMap = {
    pending: 'pending',
    signing: 'signing',
    success: 'success',
    error: 'error',
  }
  return classMap[status] || 'pending'
}

function resolveStatusTagType(status) {
  const typeMap = {
    pending: 'info',
    signing: 'warning',
    success: 'success',
    error: 'danger',
  }
  return typeMap[status] || 'info'
}

function formatFileSize(size) {
  if (!size) {
    return '0 B'
  }
  if (size < 1024) {
    return `${size} B`
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`
  }
  if (size < 1024 * 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)} MB`
  }
  return `${(size / (1024 * 1024 * 1024)).toFixed(1)} GB`
}

function resolveMimeText(type) {
  if (!type) {
    return '未知文件类型'
  }
  const mimeMap = {
    'text/plain': '纯文本文档',
    'text/csv': 'CSV 表格文件',
    'application/pdf': 'PDF 文档',
    'application/json': 'JSON 数据文件',
    'application/zip': 'ZIP 压缩包',
    'application/x-zip-compressed': 'ZIP 压缩包',
    'image/png': 'PNG 图片',
    'image/jpeg': 'JPEG 图片',
    'image/jpg': 'JPG 图片',
    'image/gif': 'GIF 图片',
    'application/msword': 'Word 文档',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word 文档',
    'application/vnd.ms-excel': 'Excel 表格',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel 表格',
    'application/vnd.ms-powerpoint': 'PPT 演示文稿',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PPT 演示文稿',
  }
  return mimeMap[type] || type
}

function resolveFileKindLabel(file) {
  const extension = file.name?.includes('.') ? file.name.split('.').pop()?.toUpperCase() : ''
  if (extension) {
    return extension
  }
  if (file.type?.startsWith('image/')) {
    return 'IMAGE'
  }
  if (file.type?.startsWith('video/')) {
    return 'VIDEO'
  }
  if (file.type?.startsWith('audio/')) {
    return 'AUDIO'
  }
  if (file.type?.includes('pdf')) {
    return 'PDF'
  }
  return 'FILE'
}

function fileSizeLevel(size) {
  if (size >= 1024 * 1024 * 10) {
    return 'large'
  }
  if (size >= 1024 * 1024) {
    return 'medium'
  }
  return 'small'
}

function removeSelectedFile(uid) {
  selectedFiles.value = selectedFiles.value.filter((file) => file.uid !== uid)
}

function loadStoredPrivateKey() {
  const keypair = loadKeyPair()
  if (keypair?.privateKey && keypair?.publicKey) {
    generatedPrivateKey.value = keypair.privateKey
    storedPublicKey.value = keypair.publicKey
    keySource.value = '当前浏览器已加载本地密钥对'
  } else {
    generatedPrivateKey.value = ''
    storedPublicKey.value = ''
    keySource.value = ''
  }
}

function mapUploadFile(uploadFile) {
  return {
    uid: uploadFile.uid,
    name: uploadFile.name,
    size: uploadFile.size,
    type: uploadFile.raw?.type || 'application/octet-stream',
    raw: uploadFile.raw,
    status: 'pending',
    message: '',
  }
}

function onFileChange(uploadFile) {
  const maxFileSizeBytes = uploadMaxFileSizeMb.value * 1024 * 1024
  if ((uploadFile.size || 0) > maxFileSizeBytes) {
    ElMessage.warning(`文件“${uploadFile.name}”超过上传大小限制，当前最大允许 ${uploadMaxFileSizeMb.value}MB`)
    fileRef.value?.clearFiles()
    return
  }

  const preservedFiles = selectedFiles.value.filter((file) => file.status !== 'success')
  const nextFile = mapUploadFile(uploadFile)
  const exists = preservedFiles.some((file) => file.name === nextFile.name && file.size === nextFile.size)

  if (!exists) {
    selectedFiles.value = [...preservedFiles, nextFile]
  } else {
    selectedFiles.value = preservedFiles
  }

  fileRef.value?.clearFiles()
}

function onFileRemove() {
  fileRef.value?.clearFiles()
}

function updateSelectedFileStatus(uid, status, message = '') {
  selectedFiles.value = selectedFiles.value.map((file) => file.uid === uid ? { ...file, status, message } : file)
}

function clearSelectedFiles() {
  selectedFiles.value = []
  batchResults.value = []
  fileRef.value?.clearFiles()
}

async function createSingleSignRecord(file) {
  const digest = await digestFileHexByChunk(file.raw)
  const signature = signDigestHex(generatedPrivateKey.value, storedPublicKey.value, digest)
  const payload = {
    fileName: file.name,
    fileType: file.name.split('.').pop() || 'unknown',
    fileSize: file.size,
    mimeType: file.type,
    fileDigest: digest,
    signatureValue: signature,
    signatureFormat: form.signatureFormat,
    signRemark: form.signRemark,
  }
  const response = await signApi.create(payload)
  return response.data
}

async function runBatchForFiles(files, { resetAll = false } = {}) {
  signing.value = true
  batchResults.value = []

  if (resetAll) {
    selectedFiles.value = selectedFiles.value.map((file) => ({ ...file, status: 'pending', message: '' }))
  } else {
    const retryIds = new Set(files.map((file) => file.uid))
    selectedFiles.value = selectedFiles.value.map((file) => retryIds.has(file.uid) ? { ...file, status: 'pending', message: '' } : file)
  }

  let successCounter = 0
  try {
    for (const file of files) {
      updateSelectedFileStatus(file.uid, 'signing')
      try {
        const detail = await createSingleSignRecord(file)
        latestDetail.value = detail
        updateSelectedFileStatus(file.uid, 'success', '签名成功')
        batchResults.value.push({
          fileName: file.name,
          status: 'success',
          signNo: detail.signNo,
          message: '签名成功',
        })
        successCounter += 1
      } catch (error) {
        const errorMessage = error?.response?.data?.message || error?.message || '签名失败'
        updateSelectedFileStatus(file.uid, 'error', errorMessage)
        batchResults.value.push({
          fileName: file.name,
          status: 'error',
          signNo: '',
          message: errorMessage,
        })
      }
    }

    await loadRecords()
    return successCounter
  } finally {
    signing.value = false
  }
}

async function submitBatchSign() {
  if (!selectedFiles.value.length) {
    ElMessage.warning('请先选择要签名的文件')
    return
  }
  if (!generatedPrivateKey.value || !storedPublicKey.value) {
    ElMessage.warning('未检测到注册时保存的配套密钥对，请先到注册页生成并保存密钥对')
    return
  }
  if (!accountPublicKey.value) {
    ElMessage.warning('当前账户未检测到绑定公钥，请先检查账号资料')
    return
  }
  if (!keyPairConsistent.value) {
    ElMessage.warning('本地密钥对与当前账户绑定公钥不一致，请先到“我的资料”重置并绑定新密钥对')
    return
  }

  const successCounter = await runBatchForFiles([...selectedFiles.value], { resetAll: true })

  if (successCounter === selectedFiles.value.length) {
    ElMessage.success(`批量签名完成，共成功 ${successCounter} 个文件`)
  } else if (successCounter > 0) {
    ElMessage.warning(`批量签名完成，成功 ${successCounter} 个，失败 ${selectedFileCount.value - successCounter} 个`)
  }
}

async function retryFailedFiles() {
  if (!failedFiles.value.length) {
    ElMessage.info('当前没有可重试的失败文件')
    return
  }
  if (!generatedPrivateKey.value || !storedPublicKey.value || !accountPublicKey.value || !keyPairConsistent.value) {
    ElMessage.warning('当前密钥状态不可用，请先检查并确保密钥一致')
    return
  }

  const retryTargets = failedFiles.value.map((file) => ({ ...file }))
  const successCounter = await runBatchForFiles(retryTargets)

  if (successCounter === retryTargets.length) {
    ElMessage.success(`失败文件重试完成，已成功重试 ${successCounter} 个文件`)
  } else if (successCounter > 0) {
    ElMessage.warning(`失败文件重试完成，成功 ${successCounter} 个，仍失败 ${retryTargets.length - successCounter} 个`)
  }
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

async function loadRecords() {
  loading.value = true
  try {
    const response = await signApi.list()
    records.value = response.data || []
  } finally {
    loading.value = false
  }
}

async function openDetail(signNo) {
  const response = await signApi.detail(signNo)
  latestDetail.value = response.data
}

onMounted(async () => {
  authStore.init()
  if (!authStore.user) {
    try {
      await authStore.fetchCurrentUser()
    } catch {
      // ignore and rely on page prompts
    }
  }
  loadStoredPrivateKey()
  loadUploadLimit()
  loadRecords()
})
</script>

<template>
  <div class="page-stack">
    <section class="two-column-layout">
      <el-card shadow="never" class="glass-card panel-card">
        <template #header>
          <div class="panel-header">
            <span>批量 SM2 签名</span>
            <el-tag :type="keyPairConsistent ? 'success' : 'warning'">
              {{ keyPairConsistent ? '密钥状态正常' : '请检查密钥一致性' }}
            </el-tag>
          </div>
        </template>

        <el-alert title="系统会自动使用当前账户已绑定的本地密钥对，按顺序对多个文件逐个完成 SM2 签名并创建签名记录。" type="success" show-icon :closable="false" />
        <el-alert :title="`当前文件上传大小限制：${uploadMaxFileSizeMb}MB`" type="info" show-icon :closable="false" class="upload-limit-alert" />

        <el-form label-position="top" class="form-stack">
          <el-form-item label="当前签名私钥（自动读取本地保存）">
            <el-input v-model="generatedPrivateKey" type="textarea" :rows="4" readonly placeholder="请先在注册页生成并保存配套密钥对" />
          </el-form-item>
          <el-form-item label="当前本地公钥（与私钥配套）">
            <el-input v-model="storedPublicKey" type="textarea" :rows="3" readonly placeholder="注册页生成的公钥会一起保存在本地" />
          </el-form-item>
          <el-form-item label="当前账户绑定公钥（后端验签使用）">
            <el-input :model-value="accountPublicKey" type="textarea" :rows="3" readonly placeholder="当前账户资料中的绑定公钥" />
          </el-form-item>
          <div class="inline-actions wrap-actions">
            <el-tag :type="keyPairConsistent ? 'success' : 'danger'">{{ keyPairStatusText }}</el-tag>
            <el-tag type="primary">已选文件 {{ selectedFileCount }} 个</el-tag>
          </div>
          <el-alert
            v-if="generatedPrivateKey && storedPublicKey && accountPublicKey && !keyPairConsistent"
            title="检测到本地密钥对与当前账户绑定公钥不一致。继续签名会导致后续验签出现“SM3 摘要匹配，但 SM2 验签失败”。请先到“我的资料”重置并绑定新密钥对。"
            type="error"
            show-icon
            :closable="false"
          />
          <el-form-item label="选择待签文件（支持批量）">
            <el-upload
              ref="fileRef"
              :auto-upload="false"
              :show-file-list="false"
              multiple
              :on-change="onFileChange"
              :on-remove="onFileRemove"
            >
              <el-button type="primary" plain>选择多个文件</el-button>
            </el-upload>
          </el-form-item>
          <el-form-item label="批量签名备注">
            <el-input v-model="form.signRemark" placeholder="请输入备注，例如：课程设计提交材料" />
          </el-form-item>

          <div v-if="selectedFiles.length" class="selected-files-panel">
            <div class="panel-header compact">
              <div>
                <span>待签文件列表</span>
                <p class="selected-summary">共 {{ selectedFileCount }} 个文件，合计 {{ totalSelectedSizeText }}</p>
              </div>
              <div class="panel-actions">
                <el-button link :disabled="!failedFiles.length || signing" @click="retryFailedFiles">重试失败文件</el-button>
                <el-button link @click="clearSelectedFiles">清空</el-button>
              </div>
            </div>
            <div class="progress-overview">
              <div class="progress-stats">
                <div class="progress-stat">
                  <strong>{{ selectedFileCount }}</strong>
                  <span>总文件</span>
                </div>
                <div class="progress-stat">
                  <strong>{{ completedCount }}</strong>
                  <span>已完成</span>
                </div>
                <div class="progress-stat success">
                  <strong>{{ successCount }}</strong>
                  <span>成功</span>
                </div>
                <div class="progress-stat danger">
                  <strong>{{ errorCount }}</strong>
                  <span>失败</span>
                </div>
                <div class="progress-stat warning" v-if="signingCount">
                  <strong>{{ signingCount }}</strong>
                  <span>签名中</span>
                </div>
              </div>
              <div class="progress-bar-wrap">
                <div class="progress-text">批量进度 {{ completedCount }} / {{ selectedFileCount }}</div>
                <el-progress :percentage="batchProgressPercent" :status="batchProgressStatus" :stroke-width="10" />
              </div>
            </div>
            <el-table :data="selectedFiles" size="small" stripe class="selected-files-table">
              <el-table-column label="文件名" min-width="300">
                <template #default="scope">
                  <div class="file-row-main">
                    <span class="file-kind-chip">{{ resolveFileKindLabel(scope.row) }}</span>
                    <div class="file-name-cell">
                      <strong>{{ scope.row.name }}</strong>
                      <span>{{ resolveMimeText(scope.row.type) }}</span>
                      <small v-if="scope.row.message" :class="['file-status-note', scope.row.status]">{{ scope.row.message }}</small>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="大小" width="150" align="center">
                <template #default="scope">
                  <span class="file-size-badge" :class="fileSizeLevel(scope.row.size)">{{ formatFileSize(scope.row.size) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120" align="center">
                <template #default="scope">
                  <span class="status-pill" :class="resolveStatusClass(scope.row.status)">{{ resolveStatusText(scope.row.status) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="110" align="center">
                <template #default="scope">
                  <el-button link class="remove-file-btn" @click="removeSelectedFile(scope.row.uid)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-button type="primary" :loading="signing" :disabled="!keyPairConsistent" @click="submitBatchSign">
            {{ selectedFileCount > 1 ? '开始批量签名' : '生成签名记录' }}
          </el-button>
        </el-form>
      </el-card>

      <el-card shadow="never" class="glass-card panel-card code-panel">
        <template #header>
          <div class="panel-header">
            <span>批量签名结果</span>
            <el-tag type="success" v-if="batchResults.length">已执行</el-tag>
          </div>
        </template>
        <el-empty v-if="!batchResults.length" description="批量签名完成后将在这里显示结果" />
        <div v-else class="batch-result-list">
          <div v-for="item in batchResults" :key="`${item.fileName}-${item.signNo || item.status}`" class="batch-result-item" :class="item.status">
            <div>
              <strong>{{ item.fileName }}</strong>
              <p>{{ item.message }}</p>
            </div>
            <span class="status-pill" :class="resolveStatusClass(item.status)">
              {{ item.status === 'success' ? '成功' : '失败' }}
            </span>
          </div>
        </div>

        <div class="detail-divider"></div>

        <div class="panel-header compact">
          <span>最近一次签名详情</span>
          <el-tag type="success" v-if="latestDetail">已生成</el-tag>
        </div>
        <el-empty v-if="!latestDetail" description="签名完成后将在这里显示详情" />
        <div v-else class="detail-grid">
          <div><span>签名编号</span><strong>{{ latestDetail.signNo }}</strong></div>
          <div><span>文件名</span><strong>{{ latestDetail.fileName }}</strong></div>
          <div><span>摘要算法</span><strong>{{ latestDetail.digestAlgorithm }}</strong></div>
          <div><span>签名算法</span><strong>{{ latestDetail.signatureAlgorithm }}</strong></div>
          <div class="detail-block"><span>文件摘要</span><code>{{ latestDetail.fileDigest }}</code></div>
          <div class="detail-block"><span>签名值</span><code>{{ latestDetail.signatureValue }}</code></div>
        </div>
      </el-card>
    </section>

    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>我的签名记录</span>
          <el-button link @click="loadRecords">刷新</el-button>
        </div>
      </template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="signNo" label="签名编号" min-width="220" />
        <el-table-column prop="fileName" label="文件名" min-width="180" />
        <el-table-column prop="fileType" label="类型" width="100" />
        <el-table-column prop="signerName" label="签名人" width="120" />
        <el-table-column prop="signTime" label="签名时间" min-width="180" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.signNo)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.wrap-actions{flex-wrap:wrap}.selected-files-panel{margin:8px 0 18px;padding:16px;border:1px solid #dbe4f0;border-radius:18px;background:linear-gradient(180deg,#fbfdff,#f3f7fc);box-shadow:inset 0 1px 0 rgba(255,255,255,.9)}.selected-summary{margin:4px 0 0;color:#475569;font-size:12px;line-height:1.5}.panel-actions{display:flex;align-items:center;gap:8px;flex-wrap:wrap}.progress-overview{display:grid;gap:14px;margin:12px 0 14px;padding:14px;border-radius:16px;background:rgba(255,255,255,.78);border:1px solid #e2e8f0}.progress-stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(88px,1fr));gap:10px}.progress-stat{padding:10px 12px;border-radius:14px;background:#f8fafc;border:1px solid #e2e8f0}.progress-stat strong{display:block;color:#0f172a;font-size:18px;line-height:1.2}.progress-stat span{display:block;margin-top:4px;color:#64748b;font-size:12px}.progress-stat.success{background:#f0fdf4;border-color:#bbf7d0}.progress-stat.success strong{color:#15803d}.progress-stat.danger{background:#fef2f2;border-color:#fecaca}.progress-stat.danger strong{color:#dc2626}.progress-stat.warning{background:#fffbeb;border-color:#fde68a}.progress-stat.warning strong{color:#d97706}.progress-bar-wrap{display:grid;gap:8px}.progress-text{color:#334155;font-size:13px;font-weight:700}.selected-files-table{margin-top:10px}.file-row-main{display:flex;align-items:flex-start;gap:12px}.file-kind-chip{display:inline-flex;align-items:center;justify-content:center;min-width:56px;height:28px;padding:0 10px;border-radius:999px;background:linear-gradient(135deg,#dbeafe,#bfdbfe);color:#1d4ed8;font-size:11px;font-weight:800;letter-spacing:.04em;box-shadow:inset 0 0 0 1px rgba(59,130,246,.18)}.file-name-cell{display:flex;flex-direction:column;gap:4px;min-width:0}.file-name-cell strong{color:#0f172a;font-size:14px;line-height:1.5;word-break:break-all}.file-name-cell span{color:#64748b;font-size:12px;line-height:1.4;word-break:break-all}.file-status-note{font-size:12px;line-height:1.4}.file-status-note.signing{color:#b45309;font-weight:700}.file-status-note.success{color:#047857;font-weight:700}.file-status-note.error{color:#b91c1c;font-weight:700}.file-size-badge{display:inline-flex;align-items:center;justify-content:center;min-width:86px;padding:6px 10px;border-radius:999px;font-size:12px;font-weight:700;box-shadow:inset 0 0 0 1px rgba(37,99,235,.12)}.file-size-badge.small{background:rgba(16,185,129,.12);color:#047857;box-shadow:inset 0 0 0 1px rgba(16,185,129,.16)}.file-size-badge.medium{background:rgba(245,158,11,.12);color:#b45309;box-shadow:inset 0 0 0 1px rgba(245,158,11,.18)}.file-size-badge.large{background:rgba(239,68,68,.12);color:#b91c1c;box-shadow:inset 0 0 0 1px rgba(239,68,68,.16)}.status-pill{display:inline-flex;align-items:center;justify-content:center;min-width:72px;padding:6px 12px;border-radius:999px;font-size:12px;font-weight:800;letter-spacing:.02em}.status-pill.pending{background:#e2e8f0;color:#334155}.status-pill.signing{background:#fef3c7;color:#92400e}.status-pill.success{background:#dcfce7;color:#166534}.status-pill.error{background:#fee2e2;color:#991b1b}.remove-file-btn{padding:6px 10px;border-radius:10px;color:#dc2626;font-weight:700;background:rgba(239,68,68,.08)}.remove-file-btn:hover{color:#b91c1c;background:rgba(239,68,68,.14)}:deep(.selected-files-table .el-table){--el-table-border-color:#e2e8f0;--el-table-header-bg-color:#eef4fb;--el-table-tr-bg-color:transparent;--el-table-row-hover-bg-color:#edf4ff;color:#0f172a;border-radius:14px;overflow:hidden}:deep(.selected-files-table th.el-table__cell){color:#334155;font-weight:700;background:#eef4fb}:deep(.selected-files-table td.el-table__cell){background:rgba(255,255,255,.72)}.panel-header.compact{margin-bottom:10px}.batch-result-list{display:grid;gap:10px}.batch-result-item{display:flex;align-items:flex-start;justify-content:space-between;gap:12px;padding:14px 16px;border-radius:14px;border:1px solid #e2e8f0;background:#f8fafc}.batch-result-item.success{border-color:#bbf7d0;background:#f0fdf4}.batch-result-item.error{border-color:#fecaca;background:#fef2f2}.batch-result-item strong{display:block;color:#0f172a;margin-bottom:4px}.batch-result-item p{margin:0;color:#475569;font-size:13px;line-height:1.6}.detail-divider{height:1px;margin:18px 0;background:#e2e8f0}
</style>
