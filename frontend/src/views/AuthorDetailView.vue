<template>
  <Transition name="fade-scale">
    <div v-if="visible" class="modal-overlay" @click.self="close">
      <div class="modal-content glass-morphism">
        <button class="close-btn" @click="close">&times;</button>

        <div class="modal-body">
          <div class="info-side">
            <div class="profile-header">
              <div class="avatar-glow">{{ scholar.name.charAt(0) }}</div>
              <div>
                <h2 class="name-text">{{ scholar.name }}</h2>
                <p class="org-text">{{ scholar.org }}</p>
              </div>
            </div>
            
            <div class="stats-grid">
              <div class="stat-box">
                <span class="label">学术总分</span>
                <span class="value gold">{{ scholar.totalScore.toFixed(4) }}</span>
              </div>
              <div class="stat-box">
                <span class="label">论文总量</span>
                <span class="value">{{ scholar.paperCount }}</span>
              </div>
            </div>
            
            <div class="bio-section">
              <h4>核心研究领域</h4>
              <div class="tags">
                <span v-for="tag in ['Computer Science', 'Data Mining', 'AI']" :key="tag" class="tag">{{ tag }}</span>
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
import { ref, watch, nextTick, onUnmounted } from 'vue';
import * as echarts from 'echarts';
const props = defineProps(['visible', 'scholar']);
const emit = defineEmits(['close']);
const radarChart = ref(null);
let chartInstance = null;

const close = () => emit('close');

// 初始化雷达图
const initChart = () => {
  if (!radarChart.value) return;
  if (chartInstance) chartInstance.dispose();
  
  chartInstance = echarts.init(radarChart.value);
  const option = {
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
        value: props.scholar.metrics || [0.8, 0.7, 0.9, 0.6, 0.8, 0.7],
        name: '能力值',
        itemStyle: { color: '#8b5cf6' },
        areaStyle: { color: 'rgba(139, 92, 246, 0.3)' },
        lineStyle: { width: 2, color: '#8b5cf6' }
      }]
    }]
  };
  chartInstance.setOption(option);
};

watch(() => props.visible, (newVal) => {
  if (newVal) {
    nextTick(() => initChart());
  }
});
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(8px);
  display: flex; justify-content: center; align-items: center; z-index: 9999;
}

.glass-morphism {
  background: rgba(20, 20, 35, 0.85);
  border: 1px solid rgba(139, 92, 246, 0.3);
  box-shadow: 0 0 50px rgba(139, 92, 246, 0.2);
  border-radius: 24px;
}

.modal-content {
  width: 900px; height: 550px; position: relative; padding: 40px;
  display: flex; flex-direction: column;
}

.modal-body { display: flex; gap: 40px; height: 100%; }
.info-side { flex: 1; }
.chart-side { flex: 1.2; display: flex; flex-direction: column; }

.avatar-glow {
  width: 60px; height: 60px; border-radius: 15px;
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: bold; margin-bottom: 20px;
}

.name-text { font-size: 28px; margin: 0; color: #fff; }
.org-text { color: rgba(255,255,255,0.5); font-size: 14px; }

.stat-box { margin-top: 25px; display: inline-block; margin-right: 30px; }
.stat-box .label { display: block; font-size: 12px; color: #8b5cf6; text-transform: uppercase; }
.stat-box .value { font-size: 24px; font-weight: 800; }
.stat-box .value.gold { color: #fbbf24; }

.radar-canvas { flex: 1; width: 100%; min-height: 350px; }

/* 动画效果 */
.fade-scale-enter-active, .fade-scale-leave-active { transition: all 0.3s ease; }
.fade-scale-enter-from, .fade-scale-leave-to { opacity: 0; transform: scale(0.9); }
</style>