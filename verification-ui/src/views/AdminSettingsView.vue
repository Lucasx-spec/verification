<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const form = reactive({
  sm3ChunkSizeKb: 1024,
  verifyLinkExpireHours: 24,
  verifyLinkMaxAccessCount: 20,
  uploadMaxFileSizeMb: 50,
})

const summaryCards = computed(() => [
  {
    label: 'SM3 分块大小',
    value: `${form.sm3ChunkSizeKb} KB`,
    accent: 'azure',
    description: '前端执行大文件摘要时的单次读取块大小',
  },
  {
    label: '链接有效期',
    value: `${form.verifyLinkExpireHours} 小时`,
    accent: 'violet',
    description: '新生成公开验签链接的默认存活时间',
  },
  {
    label: '最大访问次数',
    value: `${form.verifyLinkMaxAccessCount} 次`,
    accent: 'green',
    description: '单条公开验签链接允许的总访问上限',
  },
  {
    label: '上传大小限制',
    value: `${form.uploadMaxFileSizeMb} MB`,
    accent: 'azure',
    description: '前后端允许上传文件的最大体积',
  },
])

function adjustValue(key, delta, min, max) {
  const nextValue = Number(form[key] || 0) + delta
  form[key] = Math.min(max, Math.max(min, nextValue))
}

async function loadSettings() {
  loading.value = true
  try {
    const response = await systemApi.getSettings()
    Object.assign(form, response.data || {})
  } finally {
    loading.value = false
  }
}

async function saveSettings() {
  saving.value = true
  try {
    await systemApi.updateSettings(form)
    ElMessage.success('系统参数已保存')
    await loadSettings()
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<template>
  <div class="page-stack">
    <section class="hero-panel glass-card admin-hero settings-hero">
      <div>
        <div class="eyebrow">SYSTEM SETTINGS</div>
        <h2>国密算法与验签参数维护</h2>
        <p>统一维护摘要分块策略、公开验签链接生命周期、访问阈值以及文件上传大小限制，让签名与验签行为在全平台保持一致。</p>
      </div>
      <div class="hero-actions settings-hero-actions">
        <el-button plain class="settings-secondary-btn" @click="loadSettings">刷新配置</el-button>
        <el-button type="primary" class="settings-primary-btn" :loading="saving" @click="saveSettings">保存当前参数</el-button>
      </div>
    </section>

    <section class="metric-grid settings-metrics">
      <article v-for="item in summaryCards" :key="item.label" class="metric-card glass-card" :data-accent="item.accent">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.description }}</small>
      </article>
    </section>

    <section class="settings-grid" v-loading="loading">
      <el-card shadow="never" class="glass-card panel-card settings-card">
        <template #header>
          <div class="panel-header">
            <span>参数配置面板</span>
            <el-tag type="primary" effect="plain">管理员可维护</el-tag>
          </div>
        </template>

        <div class="settings-section-head">
          <div>
            <h3>核心运行参数</h3>
            <p>修改后将影响新一轮签名任务、公开验签访问控制以及上传文件拦截规则。</p>
          </div>
          <el-alert
            title="保存后即刻生效：签名前会重新读取 SM3 分块大小，验签链接会按最新有效期与次数限制执行，上传文件也会按最新大小限制拦截。"
            type="info"
            :closable="false"
            show-icon
          />
        </div>

        <el-form label-position="top" class="settings-form">
          <div class="settings-field-grid">
            <div class="setting-item-card">
              <div class="setting-item-head">
                <div>
                  <span class="setting-chip">SM3</span>
                  <h4>SM3 分块大小</h4>
                </div>
                <strong>{{ form.sm3ChunkSizeKb }} KB</strong>
              </div>
              <p>用于前端处理大文件时的摘要分块大小。块越大，请求次数越少；块越小，单次内存压力越低。</p>
              <div class="setting-value-panel">
                <span>当前值</span>
                <div class="setting-value-hero">{{ form.sm3ChunkSizeKb }} <small>KB</small></div>
                <em>推荐范围：1024 - 4096 KB</em>
              </div>
              <el-form-item label="分块大小（KB）" class="setting-form-item">
                <div class="setting-adjuster-row setting-adjuster-row--buttons-only">
                  <el-button class="setting-adjust-btn setting-adjust-btn--minus" @click="adjustValue('sm3ChunkSizeKb', -256, 256, 16384)">减小</el-button>
                  <el-button class="setting-adjust-btn setting-adjust-btn--plus" @click="adjustValue('sm3ChunkSizeKb', 256, 256, 16384)">增大</el-button>
                </div>
              </el-form-item>
            </div>

            <div class="setting-item-card">
              <div class="setting-item-head">
                <div>
                  <span class="setting-chip violet">LINK</span>
                  <h4>验签链接有效期</h4>
                </div>
                <strong>{{ form.verifyLinkExpireHours }} 小时</strong>
              </div>
              <p>决定新生成公开验签链接的默认有效时长。超过该时间后，链接将不再允许继续访问。</p>
              <div class="setting-value-panel violet-panel">
                <span>当前值</span>
                <div class="setting-value-hero">{{ form.verifyLinkExpireHours }} <small>小时</small></div>
                <em>推荐范围：1 - 24 小时</em>
              </div>
              <el-form-item label="有效期（小时）" class="setting-form-item">
                <div class="setting-adjuster-row setting-adjuster-row--buttons-only">
                  <el-button class="setting-adjust-btn setting-adjust-btn--minus" @click="adjustValue('verifyLinkExpireHours', -1, 1, 720)">减小</el-button>
                  <el-button class="setting-adjust-btn setting-adjust-btn--plus" @click="adjustValue('verifyLinkExpireHours', 1, 1, 720)">增大</el-button>
                </div>
              </el-form-item>
            </div>

            <div class="setting-item-card span-full">
              <div class="setting-item-head">
                <div>
                  <span class="setting-chip green">LIMIT</span>
                  <h4>单链接最大访问次数</h4>
                </div>
                <strong>{{ form.verifyLinkMaxAccessCount }} 次</strong>
              </div>
              <p>用于限制单个公开验签链接被重复访问的次数。到达阈值后，系统将拒绝继续验签，降低滥用风险。</p>
              <div class="setting-value-panel green-panel">
                <span>当前值</span>
                <div class="setting-value-hero">{{ form.verifyLinkMaxAccessCount }} <small>次</small></div>
                <em>推荐范围：1 - 20 次</em>
              </div>
              <el-form-item label="最大访问次数" class="setting-form-item">
                <div class="setting-adjuster-row setting-adjuster-row--buttons-only">
                  <el-button class="setting-adjust-btn setting-adjust-btn--minus" @click="adjustValue('verifyLinkMaxAccessCount', -1, 1, 100000)">减小</el-button>
                  <el-button class="setting-adjust-btn setting-adjust-btn--plus" @click="adjustValue('verifyLinkMaxAccessCount', 1, 1, 100000)">增大</el-button>
                </div>
              </el-form-item>
            </div>

            <div class="setting-item-card span-full">
              <div class="setting-item-head">
                <div>
                  <span class="setting-chip">UPLOAD</span>
                  <h4>文件上传大小限制</h4>
                </div>
                <strong>{{ form.uploadMaxFileSizeMb }} MB</strong>
              </div>
              <p>统一限制签名上传与公开验签上传的文件大小，超出后前端会提前拦截，后端也会拒绝处理。</p>
              <div class="setting-value-panel">
                <span>当前值</span>
                <div class="setting-value-hero">{{ form.uploadMaxFileSizeMb }} <small>MB</small></div>
                <em>推荐范围：10 - 100 MB</em>
              </div>
              <el-form-item label="上传大小限制（MB）" class="setting-form-item">
                <div class="setting-adjuster-row setting-adjuster-row--buttons-only">
                  <el-button class="setting-adjust-btn setting-adjust-btn--minus" @click="adjustValue('uploadMaxFileSizeMb', -1, 1, 500)">减小</el-button>
                  <el-button class="setting-adjust-btn setting-adjust-btn--plus" @click="adjustValue('uploadMaxFileSizeMb', 1, 1, 500)">增大</el-button>
                </div>
              </el-form-item>
            </div>
          </div>

          <div class="settings-actions">
            <div class="settings-note">
              <strong>生效说明</strong>
              <span>参数会持久化保存，刷新后台页面后仍保持最新值。</span>
            </div>
            <div class="inline-actions wrap-actions settings-action-buttons">
              <el-button class="settings-secondary-btn" @click="loadSettings">重新加载</el-button>
              <el-button type="primary" class="settings-primary-btn" :loading="saving" @click="saveSettings">保存参数</el-button>
            </div>
          </div>
        </el-form>
      </el-card>

      <el-card shadow="never" class="glass-card panel-card guide-card">
        <template #header>
          <div class="panel-header">
            <span>配置建议</span>
            <el-tag type="success" effect="plain">最佳实践</el-tag>
          </div>
        </template>

        <div class="guide-list">
          <article class="guide-item">
            <h4>大文件优先关注分块大小</h4>
            <p>如果签名对象多为大报告、压缩包或数据集，建议维持在 1024KB 到 4096KB 区间，以平衡速度和浏览器内存占用。</p>
          </article>
          <article class="guide-item">
            <h4>公开验签优先收紧有效期</h4>
            <p>若链接用于一次性材料核验，可将有效期控制在 24 小时内，减少长期暴露风险。</p>
          </article>
          <article class="guide-item">
            <h4>敏感材料建议降低访问次数</h4>
            <p>对于只允许有限人群访问的验签链接，可将最大访问次数设置为 1 到 5 次，避免过度传播。</p>
          </article>
          <article class="guide-item">
            <h4>上传限制建议留出安全余量</h4>
            <p>如果平台主要处理普通文档，建议控制在 20MB 到 50MB；如需支持大报告或压缩包，可按业务场景逐步上调。</p>
          </article>
        </div>
      </el-card>
    </section>
  </div>
</template>

<style scoped>
.settings-hero{align-items:flex-start}.settings-hero-actions,.settings-action-buttons{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.settings-primary-btn{min-width:148px;height:44px;border:none;border-radius:14px;background:linear-gradient(135deg,#2563eb,#1d4ed8);box-shadow:0 12px 24px rgba(37,99,235,.26);font-weight:700}.settings-primary-btn:hover,.settings-primary-btn:focus{background:linear-gradient(135deg,#1d4ed8,#1e40af);box-shadow:0 16px 28px rgba(29,78,216,.3)}.settings-secondary-btn{min-width:128px;height:44px;border-radius:14px;border:1px solid #cbd5e1;background:#fff;color:#0f172a;font-weight:700;box-shadow:0 6px 18px rgba(15,23,42,.06)}.settings-secondary-btn:hover,.settings-secondary-btn:focus{border-color:#93c5fd;color:#1d4ed8;background:#eff6ff}.settings-metrics .metric-card{min-height:132px}.settings-metrics .metric-card small{display:block;margin-top:10px;color:#64748b;line-height:1.6}.settings-grid{display:grid;grid-template-columns:minmax(0,1.7fr) minmax(320px,.9fr);gap:24px}.settings-card,.guide-card{height:100%}.settings-section-head{display:grid;gap:16px;margin-bottom:22px}.settings-section-head h3{margin:0 0 6px;color:#0f172a;font-size:22px}.settings-section-head p{margin:0;color:#475569;line-height:1.7}.settings-form{display:grid;gap:22px}.settings-field-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:18px}.setting-item-card{padding:20px;border-radius:22px;border:1px solid #dbe5f0;background:linear-gradient(180deg,#ffffff,#f8fbff);box-shadow:0 14px 30px rgba(15,23,42,.06)}.setting-item-card.span-full{grid-column:1 / -1}.setting-item-head{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;margin-bottom:12px}.setting-item-head h4{margin:8px 0 0;color:#0f172a;font-size:18px}.setting-item-head strong{color:#1d4ed8;font-size:18px;white-space:nowrap}.setting-item-card p{margin:0 0 16px;color:#475569;line-height:1.7}.setting-chip{display:inline-flex;align-items:center;padding:6px 10px;border-radius:999px;background:rgba(37,99,235,.1);color:#1d4ed8;font-size:12px;font-weight:700;letter-spacing:.04em}.setting-chip.violet{background:rgba(124,58,237,.1);color:#7c3aed}.setting-chip.green{background:rgba(5,150,105,.12);color:#059669}.setting-value-panel{display:grid;gap:6px;padding:16px 18px;margin-bottom:16px;border-radius:18px;background:linear-gradient(135deg,#eff6ff,#dbeafe);border:1px solid #bfdbfe}.setting-value-panel span{font-size:12px;font-weight:700;letter-spacing:.04em;color:#1d4ed8;text-transform:uppercase}.setting-value-hero{color:#1d4ed8;font-size:34px;font-weight:800;line-height:1.1}.setting-value-hero small{font-size:15px;font-weight:700;color:#2563eb}.setting-value-panel em{font-style:normal;color:#475569;font-size:13px}.violet-panel{background:linear-gradient(135deg,#f5f3ff,#ede9fe);border-color:#ddd6fe}.violet-panel span,.violet-panel .setting-value-hero,.violet-panel .setting-value-hero small{color:#7c3aed}.green-panel{background:linear-gradient(135deg,#ecfdf5,#d1fae5);border-color:#a7f3d0}.green-panel span,.green-panel .setting-value-hero,.green-panel .setting-value-hero small{color:#059669}.setting-form-item{margin-bottom:0}.setting-item-card :deep(.el-form-item__label){padding-bottom:10px;color:#0f172a;font-weight:700}.setting-adjuster-row{display:flex;align-items:center;gap:12px;flex-wrap:nowrap}.setting-adjuster-row--buttons-only{justify-content:flex-start}.setting-adjust-btn{min-width:120px;height:50px;border:none;border-radius:16px;font-weight:800;letter-spacing:.02em}.setting-adjust-btn--minus{background:linear-gradient(135deg,#93c5fd,#60a5fa);color:#0f172a;box-shadow:0 10px 20px rgba(96,165,250,.24)}.setting-adjust-btn--plus{background:linear-gradient(135deg,#2563eb,#1d4ed8);color:#fff;box-shadow:0 12px 24px rgba(37,99,235,.3)}.setting-adjust-btn:hover,.setting-adjust-btn:focus{filter:brightness(1.05)}.settings-actions{display:flex;align-items:center;justify-content:space-between;gap:16px;flex-wrap:wrap;padding:18px 20px;border-radius:18px;background:#f8fafc;border:1px solid #e2e8f0}.settings-note{display:grid;gap:4px}.settings-note strong{color:#0f172a}.settings-note span{color:#64748b;line-height:1.6}.guide-list{display:grid;gap:16px}.guide-item{padding:18px;border-radius:18px;border:1px solid #e2e8f0;background:linear-gradient(180deg,#ffffff,#f8fbff)}.guide-item h4{margin:0 0 10px;color:#0f172a;font-size:16px}.guide-item p{margin:0;color:#475569;line-height:1.75}.guide-card :deep(.el-card__body),.settings-card :deep(.el-card__body){padding:22px}@media (max-width:1100px){.settings-grid{grid-template-columns:1fr}.settings-field-grid{grid-template-columns:1fr}}@media (max-width:768px){.setting-item-head,.settings-actions{flex-direction:column;align-items:flex-start}.setting-adjuster-row{flex-wrap:wrap}.setting-value-hero{font-size:28px}.settings-card :deep(.el-card__body),.guide-card :deep(.el-card__body){padding:18px}}
</style>
