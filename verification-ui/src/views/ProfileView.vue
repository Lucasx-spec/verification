<script setup>
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { generateKeyPairHex } from '../utils/crypto'
import { loadKeyPair, saveKeyPair } from '../utils/keypair'

const authStore = useAuthStore()
const resetting = ref(false)

const profileRows = computed(() => [
  { label: '用户ID', value: authStore.user?.userId },
  { label: '用户名', value: authStore.user?.username },
  { label: '真实姓名', value: authStore.user?.realName || '未设置' },
  { label: '昵称', value: authStore.user?.nickname || '未设置' },
  { label: '邮箱', value: authStore.user?.email || '未设置' },
  { label: '手机号', value: authStore.user?.phone || '未设置' },
  { label: '角色', value: authStore.user?.roleCode || 'USER' },
  { label: '签名功能', value: authStore.user?.signEnabled ? '已启用' : '未启用' },
])

const localPrivateKey = computed(() => loadKeyPair()?.privateKey || '')

async function resetKeyPair() {
  await ElMessageBox.confirm(
    '重置后将为当前账户绑定新的公钥/私钥对。本地旧私钥会被覆盖，新签名记录将使用新密钥对生成。是否继续？',
    '确认重置密钥对',
    {
      confirmButtonText: '确认重置',
      cancelButtonText: '取消',
      type: 'warning',
    },
  )

  resetting.value = true
  try {
    const { publicKey, privateKey } = generateKeyPairHex()
    saveKeyPair({ publicKey, privateKey })
    await authStore.updateKeyPair({
      publicKey,
      publicKeyFormat: 'HEX_UNCOMPRESSED',
    })
    ElMessage.success('新密钥对已生成并绑定到当前账户')
  } finally {
    resetting.value = false
  }
}
</script>

<template>
  <div class="page-stack two-column-layout">
    <el-card shadow="never" class="glass-card panel-card">
      <template #header>
        <div class="panel-header">
          <span>账号资料</span>
          <el-tag effect="dark">{{ authStore.user?.roleCode }}</el-tag>
        </div>
      </template>

      <div class="profile-grid">
        <div v-for="row in profileRows" :key="row.label" class="profile-item">
          <span>{{ row.label }}</span>
          <strong>{{ row.value }}</strong>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="glass-card panel-card code-panel">
      <template #header>
        <div class="panel-header">
          <span>密钥对管理</span>
          <el-tag type="info">SM2 / HEX</el-tag>
        </div>
      </template>
      <div class="page-stack">
        <div>
          <p class="muted-tip">当前账户绑定公钥</p>
          <pre>{{ authStore.user?.publicKey }}</pre>
        </div>
        <div>
          <p class="muted-tip">本地保存私钥</p>
          <pre>{{ localPrivateKey || '当前浏览器尚未检测到本地私钥' }}</pre>
        </div>
        <el-alert
          title="如果本地私钥丢失，可在这里直接重置并绑定新密钥对，无需重新注册。历史签名记录仍使用其原公钥快照进行验签。"
          type="success"
          show-icon
          :closable="false"
        />
        <el-button type="primary" :loading="resetting" @click="resetKeyPair">重置并绑定新密钥对</el-button>
      </div>
    </el-card>
  </div>
</template>
