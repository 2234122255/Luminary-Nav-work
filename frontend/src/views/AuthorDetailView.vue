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
          </div>

          <div class="chart-side">
            <h4 class="chart-title">学术能力画像 (Academic Portrait)</h4>
            <div ref="radarChart" class="radar-canvas"></div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import axios from 'axios'
import * as echarts from 'echarts'
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
})
</script>

<style scoped>
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

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.3s ease;
}

.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

@media (max-width: 900px) {
  .modal-body {
    flex-direction: column;
  }
}
</style>
