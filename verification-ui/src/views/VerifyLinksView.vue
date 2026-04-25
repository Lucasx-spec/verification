<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { verifyApi, signApi } from '../api'

const links = ref([])
const signRecords = ref([])
const loading = ref(false)
const creatingLink = ref(false)
const detail = ref(null)

const linkForm = reactive({
  signNo: '',
})

const publicVerifyUrl = computed(() => {
  if (!detail.value?.verifyToken) {
    return ''
  }
  return `${window.location.origin}/verify/${detail.value.verifyToken}`
})

async function loadAll() {
  loading.value = true
  try {
    const [linkRes, signRes] = await Promise.all([verifyApi.listLinks(), signApi.list()])
    links.value = linkRes.data || []
    signRecords.value = signRes.data || []
  } finally {
    loading.value = false
  }
}

async function createLink() {
  if (!linkForm.signNo) {
    ElMessage.warning('请先选择一个签名记录')
    return
  }

  creatingLink.value = true
  try {
    const response = await verifyApi.createLink({ signNo: linkForm.signNo })
    ElMessage.success('验签链接已创建')
    await loadAll()
    await openDetail(response.data.verifyToken)
  } catch (error) {
    // message handled globally
  } finally {
    creatingLink.value = false
  }
}

async function openDetail(token) {
  try {
    const response = await verifyApi.linkDetail(token)
    detail.value = response.data
  } catch (error) {
    // message handled globally
  }
}

async function disableLink(token) {
  try {
    await verifyApi.disableLink(token)
    ElMessage.success('验签链接已禁用')
    await loadAll()
    if (detail.value?.verifyToken === token) {
      await openDetail(token)
    }
  } catch (error) {
    // message handled globally
  }
}

async function copyPublicUrl() {
  if (!publicVerifyUrl.value) {
    ElMessage.warning('请先生成或打开一条验签链接详情')
    return
  }
  await navigator.clipboard.writeText(publicVerifyUrl.value)
  ElMessage.success('公开验签链接已复制')
}

function openPublicVerifyPage(token) {
  window.open(`${window.location.origin}/verify/${token}`, '_blank')
}

onMounted(loadAll)
</script>

<template>
  <div class="page-stack">
    <section class="two-column-layout verify-layout">
      <el-card shadow="never" class="glass-card panel-card">
        <template #header>
          <div class="panel-header">
            <span>创建验签链接</span>
            <el-button link @click="loadAll">刷新签名记录</el-button>
          </div>
        </template>
        <el-form label-position="top" class="form-stack">
          <el-form-item label="选择签名编号">
            <el-select v-model="linkForm.signNo" placeholder="请选择签名记录" filterable clearable>
              <el-option
                v-for="item in signRecords"
                :key="item.signNo"
                :label="`${item.fileName} · ${item.signNo}`"
                :value="item.signNo"
              />
            </el-select>
          </el-form-item>
          <el-alert
            v-if="!signRecords.length"
            title="当前没有可用于创建验签链接的签名记录，请先到“数字签名”页面生成签名记录。"
            type="info"
            show-icon
            :closable="false"
          />
          <el-button type="primary" :disabled="!signRecords.length" :loading="creatingLink" @click="createLink">生成验签链接</el-button>
        </el-form>

        <el-divider />


      </el-card>

      <el-card shadow="never" class="glass-card panel-card code-panel">
        <template #header>
          <div class="panel-header">
            <span>链接详情</span>
            <el-tag v-if="detail" :type="detail.status === 'ACTIVE' ? 'success' : 'info'">{{ detail.status }}</el-tag>
          </div>
        </template>
        <el-empty v-if="!detail" description="点击列表中的查看详情或创建新的验签链接" />
        <div v-else class="detail-grid">
          <div><span>签名编号</span><strong>{{ detail.signNo }}</strong></div>
          <div><span>文件名</span><strong>{{ detail.fileName }}</strong></div>
          <div><span>签名人</span><strong>{{ detail.signerName }}</strong></div>
          <div><span>访问次数</span><strong>{{ detail.currentAccessCount }} / {{ detail.maxAccessCount }}</strong></div>
          <div class="detail-block"><span>验签 Token</span><code>{{ detail.verifyToken }}</code></div>
          <div class="detail-block"><span>公开链接</span><code>{{ publicVerifyUrl }}</code></div>
          <div class="detail-block action-row">
            <el-button type="primary" @click="copyPublicUrl">复制公开验签链接</el-button>
            <el-button plain @click="openPublicVerifyPage(detail.verifyToken)">打开公开验签页</el-button>
          </div>
        </div>
      </el-card>
    </section>

    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>我的验签链接</span>
          <el-button link @click="loadAll">刷新</el-button>
        </div>
      </template>
      <el-table :data="links" v-loading="loading" stripe>
        <el-table-column prop="signNo" label="签名编号" min-width="220" />
        <el-table-column prop="verifyToken" label="验签令牌" min-width="220" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="currentAccessCount" label="访问次数" width="140">
          <template #default="scope">
            {{ scope.row.currentAccessCount }} / {{ scope.row.maxAccessCount }}
          </template>
        </el-table-column>
        <el-table-column prop="expiresAt" label="过期时间" min-width="180" />
        <el-table-column label="操作" width="240">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row.verifyToken)">详情</el-button>
            <el-button link type="success" @click="openPublicVerifyPage(scope.row.verifyToken)">公开验签</el-button>
            <el-button link type="danger" @click="disableLink(scope.row.verifyToken)">禁用</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
