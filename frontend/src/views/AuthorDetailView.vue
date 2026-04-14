<template>
  <Transition name="fade-scale">
    <div v-if="isVisible" class="modal-overlay" @click.self="onMaskClick">
      <div class="modal-content glass-morphism">
        <button class="close-btn" @click="close">×</button>

        <div class="modal-body" v-if="currentScholar">
          <div class="info-side">
            <div class="profile-header">
              <div class="avatar-glow">{{ currentScholar.name?.charAt(0) || '?' }}</div>
              <div>
                <h2 class="name-text">{{ currentScholar.name || 'Unknown Scholar' }}</h2>
                <p class="org-text">{{ currentScholar.org || currentScholar.institution || 'Unknown Institution' }}</p>
              </div>
            </div>

            <div class="stats-grid">
              <div class="stat-box">
                <span class="label">学术总分</span>
                <span class="value gold">{{ Number(currentScholar.totalScore || currentScholar.score || 0).toFixed(4) }}</span>
              </div>
              <div class="stat-box">
                <span class="label">论文总量</span>
                <span class="value">{{ currentScholar.paperCount || 0 }}</span>
              </div>
            </div>

            <div class="bio-section">
              <h4>核心研究领域</h4>
              <div class="tags">
                <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
              </div>
            </div>

            <!-- 新增：导出画报按钮区域 -->
            <div class="export-area">
              <div class="dropdown">
                <button class="export-btn" @click="toggleDropdown">📎 导出画报</button>
                <div v-if="dropdownOpen" class="dropdown-menu">
                  <div class="dropdown-item" @click="exportAsImage">📸 导出为图片</div>
                  <div class="dropdown-item" @click="exportAsPDF">📄 导出为 PDF</div>
                </div>
              </div>
            </div>
            <!-- 导出按钮结束 -->
          </div>

          <div class="chart-side">
            <h4 class="chart-title">学术能力画像 (Academic Portrait)</h4>
            <div ref="radarChart" class="radar-canvas"></div>
          </div>
        </div>
      </div>
    </div>
  </Transition>

  <!-- Login required dialog (frontend-only auth gate) -->
  <Transition name="fade-scale">
    <div v-if="authDialogOpen" class="auth-overlay" @click.self="authDialogOpen = false">
      <div class="auth-dialog glass-morphism">
        <div class="auth-title">无法下载</div>
        <div class="auth-desc">未登录，需先登录后才能下载。</div>
        <div class="auth-actions">
          <button class="auth-btn secondary" type="button" @click="authDialogOpen = false">取消</button>
          <button class="auth-btn primary" type="button" @click="goToLogin">去登录</button>
        </div>
      </div>
    </div>
  </Transition>

  <!-- 隐藏的画板容器，用于生成导出内容 -->
  <div ref="posterContainer" style="position: fixed; left: -9999px; top: 0; z-index: -1;"></div>
</template>

<script setup>
import axios from 'axios'
import * as echarts from 'echarts'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  visible: { type: Boolean, default: undefined },
  scholar: { type: Object, default: null },
  id: { type: String, default: '' }
})

const emit = defineEmits(['close'])
const router = useRouter()
const radarChart = ref(null)
const fetchedScholar = ref(null)
let chartInstance = null

// 导出相关
const posterContainer = ref(null)
let posterChartInstance = null
const dropdownOpen = ref(false)
const authDialogOpen = ref(false)

const isRouteMode = computed(() => props.visible === undefined)
const isVisible = computed(() => (props.visible === undefined ? true : props.visible))
const currentScholar = computed(() => props.scholar || fetchedScholar.value)

const tags = computed(() => {
  const org = String(currentScholar.value?.org || '').toLowerCase()
  if (org.includes('software')) return ['Software Engineering', 'Systems', 'Testing']
  if (org.includes('theory') || org.includes('algorithm')) return ['Theory', 'Algorithms', 'Complexity']
  if (org.includes('ai') || org.includes('intelligence') || org.includes('learning')) return ['AI', 'Machine Learning', 'Data Mining']
  return ['Computer Science', 'Data Mining', 'AI']
})

const clamp01 = (v) => Math.max(0, Math.min(1, v))

const normalizeMetricValue = (value, fallback = 0.35) => {
  const n = Number(value)
  if (!Number.isFinite(n) || n < 0) return fallback
  if (n <= 1) return clamp01(n)
  if (n <= 100) return clamp01(n / 100)
  return clamp01(Math.log10(n + 1) / 3)
}

const normalizeRadar = () => {
  const s = currentScholar.value || {}
  const portrait = s.portrait || {}
  const fallbackValues = [
    normalizeMetricValue(portrait.influence, 0.45),
    normalizeMetricValue(portrait.activity, 0.38),
    normalizeMetricValue(portrait.citationVolume ?? s.citationCount, 0.42),
    normalizeMetricValue(portrait.collaborationFreq ?? s.collaborationCount, 0.35),
    normalizeMetricValue(portrait.innovation, 0.4),
    normalizeMetricValue(portrait.outputRatio, 0.37)
  ]

  if (Array.isArray(s.metrics) && s.metrics.length >= 6) {
    return s.metrics
      .slice(0, 6)
      .map((v, i) => normalizeMetricValue(v, fallbackValues[i]))
      .map((v) => Math.max(0.08, v))
  }

  const score = Number(s.totalScore ?? s.score ?? 0)
  const paper = Number(s.paperCount || 0)
  const cite = Number(s.citationCount || 0)
  const collab = Number(s.collaborationCount || 0)
  const base = Math.max(0.15, Math.min(0.95, score || 0.35))
  const computed = [
    Math.min(1, base + 0.15),
    Math.min(1, 0.2 + paper / 250),
    Math.min(1, 0.2 + cite / 8000),
    Math.min(1, 0.2 + collab / 150),
    Math.min(1, base + 0.1),
    Math.min(1, base + 0.05)
  ]
  return computed.map((v, i) => Math.max(v, fallbackValues[i], 0.08))
}

const initChart = () => {
  if (!radarChart.value || !currentScholar.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(radarChart.value)
  chartInstance.setOption({
    backgroundColor: 'transparent',
    radar: {
      indicator: [
        { name: '影响力', max: 1 },
        { name: '活跃度', max: 1 },
        { name: '引用量', max: 1 },
        { name: '合作频率', max: 1 },
        { name: '创新性', max: 1 },
        { name: '产出比', max: 1 }
      ],
      shape: 'circle',
      splitNumber: 4,
      axisName: { color: '#8b5cf6' },
      splitLine: { lineStyle: { color: 'rgba(139, 92, 246, 0.2)' } },
      splitArea: { show: false }
    },
    series: [{
      type: 'radar',
      data: [{
        value: normalizeRadar(),
        name: '能力值',
        itemStyle: { color: '#8b5cf6' },
        areaStyle: { color: 'rgba(139, 92, 246, 0.3)' },
        lineStyle: { width: 2, color: '#8b5cf6' }
      }]
    }]
  })
}

const loadScholarById = async () => {
  if (!isRouteMode.value || !props.id) return
  try {
    const resp = await axios.get(`http://localhost:8080/api/rankings/detail/${props.id}`)
    fetchedScholar.value = resp.data || null
  } catch (e) {
    fetchedScholar.value = {
      id: props.id,
      name: `Scholar ${props.id.slice(0, 6)}`,
      org: 'Unknown Institution',
      totalScore: 0,
      paperCount: 0
    }
  }
}

const close = () => {
  if (isRouteMode.value) {
    router.back()
    return
  }
  emit('close')
}

const onMaskClick = () => {
  if (!isRouteMode.value) close()
}

const isLoggedIn = () => {
  try {
    const auth = JSON.parse(localStorage.getItem('luminaryAuth') || 'null')
    return !!auth && (auth.role === 'user' || auth.role === 'admin')
  } catch {
    localStorage.removeItem('luminaryAuth')
    return false
  }
}

const requireLogin = () => {
  if (isLoggedIn()) return true
  authDialogOpen.value = true
  return false
}

const goToLogin = () => {
  authDialogOpen.value = false
  dropdownOpen.value = false
  router.push('/login')
}

// ==================== 导出功能开始 ====================
const buildPosterHTML = () => {
  const scholar = currentScholar.value
  if (!scholar) return ''
  const name = scholar.name || 'Unknown Scholar'
  const org = scholar.org || scholar.institution || 'Unknown Institution'
  const totalScore = Number(scholar.totalScore || scholar.score || 0).toFixed(4)
  const paperCount = scholar.paperCount || 0
  const radarValues = normalizeRadar()
  const radarIndicators = ['影响力', '活跃度', '引用量', '合作频率', '创新性', '产出比']
  // 研究领域标签（使用原有的 tags 计算属性）
  const fieldTags = tags.value.map(tag => `<span style="background:rgba(139,92,246,0.2); padding:5px 12px; border-radius:20px; font-size:12px; margin:4px;">${tag}</span>`).join('')
  return `
    <div id="scholar-poster" style="width:800px; background:linear-gradient(135deg, #0f0c29 0%, #302b63 100%); border-radius:24px; padding:40px; font-family:'Helvetica Neue',sans-serif; color:white; box-shadow:0 20px 40px rgba(0,0,0,0.4);">
      <div style="display:flex; align-items:center; gap:20px; margin-bottom:30px;">
        <div style="width:80px; height:80px; border-radius:20px; background:linear-gradient(135deg,#8b5cf6,#3b82f6); display:flex; align-items:center; justify-content:center; font-size:36px; font-weight:bold;">${name.charAt(0)}</div>
        <div>
          <h1 style="margin:0; font-size:32px;">${name}</h1>
          <p style="color:rgba(255,255,255,0.7); margin:5px 0 0;">${org}</p>
        </div>
      </div>
      <div style="display:flex; gap:40px; margin-bottom:30px;">
        <div><span style="color:#8b5cf6; text-transform:uppercase; font-size:12px;">学术总分</span><div style="font-size:28px; font-weight:bold; color:#fbbf24;">${totalScore}</div></div>
        <div><span style="color:#8b5cf6; text-transform:uppercase; font-size:12px;">论文总量</span><div style="font-size:28px; font-weight:bold;">${paperCount}</div></div>
      </div>
      <div style="margin-bottom:30px;">
        <h4 style="margin:0 0 12px; color:#8b5cf6;">核心研究领域</h4>
        <div style="display:flex; flex-wrap:wrap; gap:10px;">${fieldTags}</div>
      </div>
      <div style="margin-top:20px;">
        <h4 style="margin:0 0 12px; color:#8b5cf6;">学术能力画像</h4>
        <div id="poster-radar" style="width:100%; height:300px;"></div>
      </div>
      <div style="margin-top:30px; text-align:center; font-size:12px; color:rgba(255,255,255,0.5); border-top:1px solid rgba(255,255,255,0.2); padding-top:20px;">
        Luminary Nav · 学者画报
      </div>
    </div>
  `
}

const renderPosterRadar = () => {
  return new Promise((resolve) => {
    const container = posterContainer.value
    if (!container) return resolve()

    const radarDiv = container.querySelector('#poster-radar')
    if (!radarDiv) return resolve()

    if (posterChartInstance) posterChartInstance.dispose()
    posterChartInstance = echarts.init(radarDiv)

    const radarValues = normalizeRadar()
    posterChartInstance.setOption({
      animation: false,
      backgroundColor: 'transparent',
      radar: {
        center: ['50%', '54%'],
        radius: '65%',
        indicator: [
          { name: '影响力', max: 1 },
          { name: '活跃度', max: 1 },
          { name: '引用量', max: 1 },
          { name: '合作频率', max: 1 },
          { name: '创新性', max: 1 },
          { name: '产出比', max: 1 }
        ],
        shape: 'circle',
        splitNumber: 4,
        axisName: { color: '#ddd' },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.2)' } },
        axisLine: { lineStyle: { color: 'rgba(255,255,255,0.45)' } }
      },
      series: [{
        type: 'radar',
        symbol: 'circle',
        symbolSize: 6,
        data: [{
          value: radarValues,
          areaStyle: { color: 'rgba(139,92,246,0.3)' },
          lineStyle: { color: '#8b5cf6', width: 2 },
          itemStyle: { color: '#8b5cf6' }
        }]
      }]
    }, true)

    posterChartInstance.resize()
    requestAnimationFrame(() => requestAnimationFrame(resolve))
  })
}

const exportAsImage = async () => {
  if (!requireLogin()) return
  dropdownOpen.value = false
  const container = posterContainer.value
  if (!container) return
  container.innerHTML = buildPosterHTML()
  await renderPosterRadar()
  const posterDiv = container.firstElementChild
  if (!posterDiv) return
  try {
    const canvas = await html2canvas(posterDiv, { scale: 2, backgroundColor: null })
    const link = document.createElement('a')
    link.download = `${currentScholar.value?.name || 'scholar'}_学术画报.png`
    link.href = canvas.toDataURL()
    link.click()
  } catch (err) {
    console.error('导出图片失败', err)
    alert('导出失败，请重试')
  } finally {
    container.innerHTML = ''
  }
}

const exportAsPDF = async () => {
  if (!requireLogin()) return
  dropdownOpen.value = false
  const container = posterContainer.value
  if (!container) return
  container.innerHTML = buildPosterHTML()
  await renderPosterRadar()
  const posterDiv = container.firstElementChild
  if (!posterDiv) return
  try {
    const canvas = await html2canvas(posterDiv, { scale: 2, backgroundColor: null })
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'px',
      format: [canvas.width, canvas.height]
    })
    pdf.addImage(imgData, 'PNG', 0, 0, canvas.width, canvas.height)
    pdf.save(`${currentScholar.value?.name || 'scholar'}_学术画报.pdf`)
  } catch (err) {
    console.error('导出PDF失败', err)
    alert('导出失败，请重试')
  } finally {
    container.innerHTML = ''
  }
}

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}
// ==================== 导出功能结束 ====================

watch([isVisible, currentScholar], async ([visible]) => {
  if (!visible) return
  await nextTick()
  initChart()
}, { immediate: true })

onMounted(async () => {
  await loadScholarById()
  await nextTick()
  initChart()
  window.addEventListener('resize', initChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', initChart)
  if (chartInstance) chartInstance.dispose()
  if (posterChartInstance) posterChartInstance.dispose()
})
</script>

<style scoped>
/* 原有样式保持不变 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.glass-morphism {
  background: rgba(20, 20, 35, 0.85);
  border: 1px solid rgba(139, 92, 246, 0.3);
  box-shadow: 0 0 50px rgba(139, 92, 246, 0.2);
  border-radius: 24px;
}

.modal-content {
  width: min(900px, 92vw);
  height: min(560px, 88vh);
  position: relative;
  padding: 36px;
  display: flex;
  flex-direction: column;
}

.close-btn {
  position: absolute;
  right: 14px;
  top: 12px;
  border: none;
  background: transparent;
  color: #d7d7ff;
  font-size: 26px;
  cursor: pointer;
}

.modal-body {
  display: flex;
  gap: 32px;
  height: 100%;
}

.info-side {
  flex: 1;
}

.chart-side {
  flex: 1.2;
  display: flex;
  flex-direction: column;
}

.profile-header {
  display: flex;
  gap: 14px;
  align-items: center;
}

.avatar-glow {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
}

.name-text {
  font-size: 28px;
  margin: 0;
  color: #fff;
}

.org-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

.stat-box {
  margin-top: 25px;
  display: inline-block;
  margin-right: 30px;
}

.stat-box .label {
  display: block;
  font-size: 12px;
  color: #8b5cf6;
  text-transform: uppercase;
}

.stat-box .value {
  font-size: 24px;
  font-weight: 800;
}

.stat-box .value.gold {
  color: #fbbf24;
}

.bio-section {
  margin-top: 24px;
}

.bio-section h4 {
  margin: 0 0 12px;
  color: #c7b7ff;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: rgba(139, 92, 246, 0.18);
  border: 1px solid rgba(139, 92, 246, 0.35);
  color: #d9ccff;
}

.chart-title {
  margin: 0 0 10px;
  color: #ddd6ff;
}

.radar-canvas {
  flex: 1;
  width: 100%;
  min-height: 350px;
}

/* 新增导出按钮样式 */
.export-area {
  margin-top: 30px;
  position: relative;
}
.export-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  padding: 8px 20px;
  border-radius: 30px;
  color: white;
  cursor: pointer;
  font-weight: 500;
  transition: 0.2s;
}
.export-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  background: rgba(30,30,50,0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  overflow: hidden;
  z-index: 100;
  margin-top: 8px;
  border: 1px solid rgba(139,92,246,0.3);
}
.dropdown-item {
  padding: 10px 20px;
  cursor: pointer;
  transition: 0.2s;
  color: white;
  font-size: 14px;
}
.dropdown-item:hover {
  background: rgba(139,92,246,0.3);
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

.auth-overlay {
  position: fixed;
  inset: 0;
  z-index: 10050;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(0, 0, 0, 0.45);
}

.auth-dialog {
  width: min(420px, 100%);
  padding: 18px 18px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(100, 150, 255, 0.22);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(18px);
  color: #fff;
}

.auth-title {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.01em;
}

.auth-desc {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.6;
  font-size: 14px;
}

.auth-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.auth-btn {
  height: 38px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid rgba(100, 150, 255, 0.22);
  cursor: pointer;
  font-weight: 700;
  color: #fff;
  background: rgba(255, 255, 255, 0.06);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s, background 0.2s;
}

.auth-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(102, 126, 234, 0.55);
}

.auth-btn.primary {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.7), rgba(118, 75, 162, 0.62));
  box-shadow: 0 10px 22px rgba(102, 126, 234, 0.22);
  border-color: rgba(102, 126, 234, 0.5);
}

.auth-btn.primary:hover {
  box-shadow: 0 14px 28px rgba(102, 126, 234, 0.32);
}

@media (max-width: 900px) {
  .modal-body {
    flex-direction: column;
  }
}
</style>
