<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock, Promotion, User } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { generateKeyPairHex } from '../utils/crypto'
import { saveKeyPair } from '../utils/keypair'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const tab = ref('login')
const privateKeyPreview = ref('')
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', publicKey: '', publicKeyFormat: 'HEX_UNCOMPRESSED', realName: '', nickname: '', email: '', phone: '' })
const currentTabMeta = computed(() => tab.value === 'register' ? { title: '注册账户', description: '绑定公钥后即可进入用户业务端。', badge: 'Register' } : { title: '账号登录', description: '登录后自动进入对应工作区。', badge: 'Login' })

function redirectByRole() {
  router.push(authStore.isAdmin ? '/admin/dashboard' : '/workspace/dashboard')
}

function fillGeneratedKeyPair() {
  const { publicKey, privateKey } = generateKeyPairHex()
  registerForm.publicKey = publicKey
  privateKeyPreview.value = privateKey
  saveKeyPair({ publicKey, privateKey })
  ElMessage.success('已生成并保存一组 SM2 密钥对：公钥用于注册，私钥将自动给签名页复用。')
}

async function copyText(value, label) {
  if (!value.trim()) {
    ElMessage.warning(`暂无可复制的${label}`)
    return
  }
  try {
    await navigator.clipboard.writeText(value)
    ElMessage.success(`${label}已复制`)
  } catch {
    ElMessage.error(`复制${label}失败，请手动复制`)
  }
}

function isValidHexPublicKey(publicKey) {
  const normalized = publicKey.trim()
  if (!/^[0-9a-fA-F]+$/.test(normalized)) {
    return false
  }
  if (normalized.length !== 130) {
    return false
  }
  return normalized.startsWith('04')
}

async function submitLogin() {
  if (!loginForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!loginForm.password.trim()) {
    ElMessage.warning('请输入密码')
    return
  }
  loading.value = true
  try {
    await authStore.login(loginForm)
    ElMessage.success('登录成功')
    redirectByRole()
  } finally {
    loading.value = false
  }
}

async function submitRegister() {
  if (!registerForm.username.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!registerForm.password.trim()) {
    ElMessage.warning('请输入登录密码')
    return
  }
  if (registerForm.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email.trim())) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }
  if (registerForm.phone.trim() && !/^1\d{10}$/.test(registerForm.phone.trim())) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  if (!registerForm.publicKey.trim()) {
    ElMessage.warning('请先生成或填写注册公钥')
    return
  }
  if (!isValidHexPublicKey(registerForm.publicKey)) {
    ElMessage.warning('请输入合法的 HEX 公钥，且应为 04 开头的未压缩公钥')
    return
  }
  loading.value = true
  try {
    await authStore.register(registerForm)
    ElMessage.success('注册并登录成功')
    redirectByRole()
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-orb orb-a"></div>
    <div class="auth-orb orb-b"></div>
    <section class="auth-grid">
      <div class="brand-panel">
        <div>
          <div class="brand-badge">SM2 / SM3</div>
          <h1>数字签名与在线验证平台</h1>
          <p class="brand-desc">数字签名、公开验签与审计追踪的一体化平台。</p>
        </div>
        <div class="feature-list">
          <article class="feature-card"><div class="feature-icon purple"><el-icon><User /></el-icon></div><div><h3>角色分区</h3><p>用户端与管理端分离。</p></div></article>
          <article class="feature-card"><div class="feature-icon blue"><el-icon><Lock /></el-icon></div><div><h3>本地密钥</h3><p>私钥仅保存在当前浏览器。</p></div></article>
          <article class="feature-card"><div class="feature-icon green"><el-icon><Promotion /></el-icon></div><div><h3>公开验签</h3><p>分享链接即可在线验证。</p></div></article>
        </div>
        <div class="brand-metrics"><div><strong>SM2</strong><span>签名算法</span></div><div><strong>SM3</strong><span>摘要算法</span></div><div><strong>Audit</strong><span>审计追踪</span></div></div>
      </div>

      <div class="form-wrap">
        <div class="form-head"><span class="mini-badge">{{ currentTabMeta.badge }}</span><h2>{{ currentTabMeta.title }}</h2><p>{{ currentTabMeta.description }}</p></div>
        <el-card shadow="never" class="auth-card">
          <el-tabs v-model="tab" stretch>
            <el-tab-pane label="登录" name="login">
              <el-form label-position="top" class="auth-form" @submit.prevent="submitLogin">
                <el-form-item label="用户名"><el-input v-model="loginForm.username" size="large" placeholder="请输入用户名" clearable autocomplete="off" /></el-form-item>
                <el-form-item label="密码"><el-input v-model="loginForm.password" size="large" type="password" show-password placeholder="请输入密码" clearable autocomplete="new-password" /></el-form-item>
                <el-button type="primary" size="large" class="block-btn hero-btn" :loading="loading" @click="submitLogin">登录并进入对应工作区</el-button>
                <div class="switch-tip">还没有账号？<button type="button" class="text-switch" @click="tab = 'register'">立即注册</button></div>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="注册" name="register">
              <el-form label-position="top" class="auth-form" @submit.prevent="submitRegister">
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12"><el-form-item label="用户名（必填）"><el-input v-model="registerForm.username" size="large" placeholder="请输入用户名" clearable autocomplete="off" name="register-username" /></el-form-item></el-col>
                  <el-col :xs="24" :sm="12"><el-form-item label="登录密码（必填）"><el-input v-model="registerForm.password" size="large" type="password" show-password placeholder="请输入密码" clearable autocomplete="new-password" name="register-password" /></el-form-item></el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12"><el-form-item label="真实姓名"><el-input v-model="registerForm.realName" size="large" placeholder="请输入真实姓名" clearable autocomplete="off" name="register-real-name" /></el-form-item></el-col>
                  <el-col :xs="24" :sm="12"><el-form-item label="昵称"><el-input v-model="registerForm.nickname" size="large" placeholder="请输入昵称" clearable autocomplete="off" name="register-nickname" /></el-form-item></el-col>
                </el-row>
                <el-row :gutter="16">
                  <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model="registerForm.email" size="large" placeholder="请输入邮箱" clearable autocomplete="off" name="register-email" /></el-form-item></el-col>
                  <el-col :xs="24" :sm="12"><el-form-item label="手机号"><el-input v-model="registerForm.phone" size="large" placeholder="请输入手机号" clearable autocomplete="off" name="register-phone" /></el-form-item></el-col>
                </el-row>
                <div class="key-card">
                  <div class="key-head"><div><div class="key-chip">密钥管理</div><h3>密钥对配置</h3><p>生成一组 SM2 密钥后，使用公钥完成注册，私钥将自动供签名页复用。</p></div><el-tag type="info" effect="plain" class="format-tag">{{ registerForm.publicKeyFormat }}</el-tag></div>
                  <div class="key-summary"><div class="key-summary-item"><span>公钥用途</span><strong>注册绑定身份</strong></div><div class="key-summary-item"><span>私钥保存位置</span><strong>当前浏览器本地</strong></div></div>
                  <div class="key-preview-grid"><el-form-item label="签名公钥（必填）" class="key-form-item"><div class="key-preview-block" :class="{ empty: !registerForm.publicKey }"><div class="key-preview-bar"><div><div class="key-preview-label">公钥预览</div><div class="key-preview-sub">用于注册绑定</div></div><el-button text class="copy-key-btn" @click="copyText(registerForm.publicKey, '公钥')">复制公钥</el-button></div><pre>{{ registerForm.publicKey || '点击下方按钮生成后，将在这里展示公钥。' }}</pre></div></el-form-item>
                    <el-form-item label="本地保存的配套私钥" class="key-form-item"><div class="key-preview-block" :class="{ empty: !privateKeyPreview }"><div class="key-preview-bar"><div><div class="key-preview-label">私钥预览</div><div class="key-preview-sub">仅保存在当前浏览器</div></div><el-button text class="copy-key-btn" @click="copyText(privateKeyPreview, '私钥')">复制私钥</el-button></div><pre>{{ privateKeyPreview || '点击下方按钮生成后，将在这里展示私钥。' }}</pre></div></el-form-item></div>
                  <div class="key-actions"><el-button type="primary" size="large" class="keygen-btn" @click="fillGeneratedKeyPair">生成并保存配套密钥对</el-button><span class="hint-text">生成后可直接复制公钥与私钥</span></div>
                </div>
                <el-alert title="使用这里生成的公钥完成注册，签名页会自动读取同一套私钥。" type="success" show-icon :closable="false" />
                <el-button type="primary" size="large" class="block-btn hero-btn register-btn" :loading="loading" @click="submitRegister">注册并进入用户业务端</el-button>
                <div class="switch-tip">已有账号？<button type="button" class="text-switch" @click="tab = 'login'">返回登录</button></div>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page{position:relative;min-height:100vh;padding:28px;overflow:hidden;background:linear-gradient(135deg,#0b1220,#111c34 55%,#172554)}.auth-grid{position:relative;z-index:1;display:grid;grid-template-columns:1fr 1.05fr;gap:28px;max-width:1360px;margin:0 auto}.auth-orb{position:absolute;border-radius:999px;filter:blur(12px);opacity:.22}.orb-a{top:70px;right:11%;width:220px;height:220px;background:radial-gradient(circle,rgba(59,130,246,.8),rgba(59,130,246,.03))}.orb-b{left:8%;bottom:50px;width:260px;height:260px;background:radial-gradient(circle,rgba(168,85,247,.55),rgba(168,85,247,.03))}
.brand-panel,.form-wrap{border:1px solid rgba(255,255,255,.1);border-radius:24px;backdrop-filter:blur(14px);box-shadow:0 18px 48px rgba(0,0,0,.22)}.brand-panel{display:flex;flex-direction:column;justify-content:space-between;gap:24px;padding:34px;background:rgba(15,23,42,.92);color:#f8fafc}.brand-badge,.mini-badge{display:inline-flex;width:fit-content;padding:8px 14px;border-radius:999px;border:1px solid rgba(148,163,184,.28);background:rgba(30,41,59,.92);color:#f8fafc;font-size:12px;letter-spacing:.08em;text-transform:uppercase}.brand-panel h1{margin:18px 0 10px;color:#fff;font-size:clamp(34px,4vw,48px);line-height:1.15}.brand-desc{margin:0;color:#e2e8f0;line-height:1.75;font-size:15px}
.feature-list{display:grid;gap:14px}.feature-card{display:grid;grid-template-columns:52px 1fr;gap:14px;padding:18px;border-radius:18px;background:#172033;border:1px solid rgba(148,163,184,.18)}.feature-card h3{margin:2px 0 6px;color:#fff;font-size:16px}.feature-card p{margin:0;color:#e2e8f0;line-height:1.6}.feature-icon{display:grid;place-items:center;width:52px;height:52px;border-radius:14px;color:#fff;font-size:22px}.purple{background:linear-gradient(135deg,#7c3aed,#4f46e5)}.blue{background:linear-gradient(135deg,#0284c7,#2563eb)}.green{background:linear-gradient(135deg,#059669,#0891b2)}
.brand-metrics{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}.brand-metrics div{padding:16px;border-radius:16px;background:#172033;border:1px solid rgba(148,163,184,.18)}.brand-metrics strong{display:block;margin-bottom:6px;color:#fff;font-size:20px}.brand-metrics span{color:#e2e8f0;font-size:13px}
.form-wrap{padding:24px;background:rgba(15,23,42,.88)}.form-head{margin-bottom:18px;padding:6px 4px 0}.form-head h2{margin:14px 0 8px;color:#fff;font-size:30px}.form-head p{margin:0;color:#e2e8f0;line-height:1.7}.auth-card{border:none;border-radius:22px;background:#fff;box-shadow:0 14px 36px rgba(15,23,42,.18)}:deep(.auth-card .el-card__body){padding:18px 22px 24px}:deep(.el-tabs__header){margin-bottom:20px}:deep(.el-tabs__item){height:48px;font-size:15px;font-weight:700;color:#334155}:deep(.el-tabs__item.is-active){color:#1d4ed8}:deep(.el-form-item){margin-bottom:18px}:deep(.el-form-item__label){padding-bottom:8px;color:#0f172a;font-weight:700}:deep(.el-input__wrapper),:deep(.el-textarea__inner){border-radius:14px;box-shadow:0 0 0 1px rgba(148,163,184,.25) inset}:deep(.el-input__wrapper){min-height:48px;background:#f8fafc}:deep(.el-textarea__inner){padding:14px 16px;background:#f8fafc;color:#0f172a}
.key-card{margin-bottom:20px;padding:24px;border-radius:24px;border:1px solid #d7e1ee;background:linear-gradient(180deg,#ffffff,#f8fbff);box-shadow:0 18px 40px rgba(15,23,42,.06)}.key-head{display:flex;justify-content:space-between;align-items:flex-start;gap:16px;margin-bottom:16px}.key-head h3{margin:0 0 6px;font-size:20px;color:#0f172a}.key-head p{margin:0;max-width:640px;color:#475569;line-height:1.7}.key-chip{display:inline-flex;align-items:center;padding:6px 10px;margin-bottom:10px;border-radius:999px;background:rgba(37,99,235,.08);color:#1d4ed8;font-size:12px;font-weight:700;letter-spacing:.04em}.format-tag{margin-top:2px}.key-summary{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px;margin-bottom:16px}.key-summary-item{padding:14px 16px;border-radius:16px;border:1px solid #e2e8f0;background:#fff;box-shadow:0 4px 14px rgba(15,23,42,.04)}.key-summary-item span{display:block;margin-bottom:6px;color:#64748b;font-size:12px}.key-summary-item strong{color:#0f172a;font-size:14px}.key-preview-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px;margin-bottom:16px}.key-form-item{margin-bottom:0}.key-form-item :deep(.el-form-item__label){font-size:13px}.key-preview-block{height:100%;padding:14px 16px;border-radius:18px;border:1px solid #dbe4f0;background:linear-gradient(180deg,#f8fbff,#eef4fb);box-shadow:inset 0 1px 0 rgba(255,255,255,.85)}.key-preview-bar{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:10px}.key-preview-label{color:#0f172a;font-size:13px;font-weight:700;letter-spacing:.02em}.key-preview-sub{margin-top:2px;color:#64748b;font-size:12px}.copy-key-btn{padding:6px 10px;border-radius:10px;color:#2563eb;font-weight:700;background:rgba(37,99,235,.08)}.copy-key-btn:hover{color:#1d4ed8;background:rgba(37,99,235,.14)}.key-preview-block pre{margin:0;min-height:172px;padding:12px 14px;border-radius:14px;background:#111827;color:#f8fafc;font-family:Consolas,"Courier New",monospace;font-size:12px;line-height:1.7;white-space:pre-wrap;word-break:break-all;box-shadow:inset 0 0 0 1px rgba(51,65,85,.7)}.key-preview-block.empty pre{color:#94a3b8}.inline-actions{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.key-actions{display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;padding-top:14px;border-top:1px solid #e2e8f0}.hint-text{font-size:13px;color:#475569}.block-btn{width:100%}.hero-btn{height:50px;border:none;border-radius:14px;background:linear-gradient(135deg,#2563eb,#1d4ed8);box-shadow:0 10px 24px rgba(37,99,235,.24);font-weight:700}.keygen-btn{min-width:260px;border:none;border-radius:14px;background:linear-gradient(135deg,#0f766e,#0ea5e9);box-shadow:0 10px 24px rgba(14,165,233,.24);font-weight:700}.register-btn{margin-top:16px}.switch-tip{margin-top:14px;text-align:center;color:#475569;font-size:14px}.text-switch{padding:0;border:none;background:transparent;color:#1d4ed8;font-weight:700;cursor:pointer}.text-switch:hover{color:#1e3a8a}
@media (max-width:1100px){.auth-page{padding:18px}.auth-grid{grid-template-columns:1fr}.brand-panel,.form-wrap{border-radius:24px}.key-summary,.key-preview-grid{grid-template-columns:1fr}}@media (max-width:768px){.auth-page{padding:14px}.brand-panel,.form-wrap{padding:18px}.brand-metrics{grid-template-columns:1fr}.key-head,.key-preview-bar,.key-actions{flex-direction:column;align-items:flex-start}.key-card{padding:18px}.key-preview-block pre{min-height:140px}:deep(.auth-card .el-card__body){padding:16px}}
</style>
