<template>
  <div class="home">

    <!-- 学术宇宙探索区域 -->
    <section class="universe-section">
      <div class="universe-content">
        <div class="universe-text">
          <h2 class="universe-title">在学术的宇宙中</h2>
          <h3 class="universe-subtitle">发现属于自己的星星</h3>
          <p class="universe-desc">Discover Your Own Star in the Academic Universe</p>
        </div>
        <div class="universe-visual">
          <div class="network-graph">
            <div class="central-node">
              <div class="person-icon">👤</div>
              <span class="connection-count">213</span>
            </div>
            <div class="network-lines">
              <div class="line line-1"></div>
              <div class="line line-2"></div>
              <div class="line line-3"></div>
              <div class="line line-4"></div>
              <div class="line line-5"></div>
            </div>
            <div class="node node-1">
              <div class="person-icon small">👤</div>
            </div>
            <div class="node node-2">
              <div class="person-icon small">👤</div>
            </div>
            <div class="node node-3">
              <div class="person-icon small">👤</div>
            </div>
            <div class="node node-4">
              <div class="person-icon small">👤</div>
            </div>
            <div class="node node-5">
              <div class="person-icon small">👤</div>
            </div>
          </div>
        </div>
      </div>
      <div class="universe-bg"></div>
    </section>

    <!-- Word Cloud -->
    <section class="card word-cloud-section">
      <div class="section-header">
        <div class="title-wrap">
          <span class="icon">#</span>
          <h2>热点词云</h2>
          <small class="muted">高频作者/主题一目了然</small>
        </div>
        <div class="tools">
          <button class="tool" @click="fetchWordCloud" :disabled="loadingCloud">
            {{ loadingCloud ? '刷新中…' : '刷新' }}
          </button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loadingCloud" class="skeleton skeleton-cloud"></div>
        <div v-else class="cloud-wrap">
          <div ref="cloudContainer" class="word-cloud"></div>
          <div ref="cloudTooltip" class="cloud-tooltip" v-show="tooltip.visible" :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }">
            <div class="tt-title">{{ tooltip.text }}</div>
            <div class="tt-sub">权重：{{ formatScore(tooltip.value) }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Carousel -->
    <section class="card carousel-section">
      <div class="section-header">
        <div class="title-wrap">
          <span class="icon">★</span>
          <h2>精选推荐</h2>
          <small class="muted">为你精选的优质学者与机构</small>
        </div>
        <div class="tools">
          <button class="tool" @click="prevSlide">上一条</button>
          <button class="tool" @click="nextSlide">下一条</button>
        </div>
      </div>
      <div class="card-body">
        <div v-if="loadingCarousel" class="skeleton skeleton-carousel"></div>
        <div
          v-else
          class="carousel"
          @mouseenter="pauseAutoPlay"
          @mouseleave="startAutoPlay"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <div class="track" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
            <div
              v-for="(item, index) in carouselItems"
              :key="item.id"
              class="slide"
              @click="goTo(item.linkUrl)"
            >
              <div class="media" :style="{ backgroundImage: item.imageUrl ? `url(${item.imageUrl})` : gradientFor(index) }"></div>
              <div class="slide-content">
                <div class="title">{{ item.title }}</div>
                <div class="subtitle">{{ item.subtitle || '—' }}</div>
                <div class="meta">综合评分：{{ formatScore(item.score) }}</div>
              </div>
            </div>
          </div>
          <div class="dots">
            <span
              v-for="(it, idx) in carouselItems"
              :key="it.id + '-dot'"
              class="dot"
              :class="{ active: idx === currentIndex }"
              @click="currentIndex = idx"
            />
          </div>
        </div>
        <div v-if="!loadingCarousel && !carouselItems.length" class="empty">暂无推荐</div>
      </div>
    </section>

  </div>
  
</template>

<script>
import axios from 'axios'
import * as d3 from 'd3'
import cloud from 'd3-cloud'


export default {
  name: 'HomeView',

  data() {
    return {
      cloudWords: [],
      carouselItems: [],
      currentIndex: 0,
      intervalId: null,
      loadingCloud: false,
      loadingCarousel: false,
      tooltip: { visible: false, text: '', value: 0, x: 0, y: 0 },
      touchStartX: 0,
      touchDeltaX: 0
    }
  },
  mounted() {
    this.fetchWordCloud()
    this.fetchCarousel()
    window.addEventListener('resize', this.onResize, { passive: true })
  },
  beforeUnmount() {
    if (this.intervalId) clearInterval(this.intervalId)
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    async fetchWordCloud() {
      try {
        this.loadingCloud = true
        const { data } = await axios.get('/api/home/word-cloud')
        this.cloudWords = Array.isArray(data) ? data : []
        this.$nextTick(() => this.renderWordCloud())
      } catch (e) {
        console.error('加载词云失败', e)
      } finally {
        this.loadingCloud = false
      }
    },
    async fetchCarousel() {
      try {
        this.loadingCarousel = true
        const { data } = await axios.get('/api/home/carousel')
        this.carouselItems = Array.isArray(data) ? data : []
        this.startAutoPlay()
      } catch (e) {
        console.error('加载轮播失败', e)
      } finally {
        this.loadingCarousel = false
      }
    },
    renderWordCloud() {
      const container = this.$refs.cloudContainer
      if (!container || !this.cloudWords.length) return

      const width = container.clientWidth || 600
      const height = 360

      container.innerHTML = ''

      const values = this.cloudWords.map(w => w.value || 0)
      const maxVal = d3.max(values) || 1
      const minVal = d3.min(values) || 0

      const fontSize = d3.scaleSqrt().domain([minVal, maxVal]).range([14, 52])
      const color = d3.scaleOrdinal(d3.schemeTableau10)

      const layout = cloud()
        .size([width, height])
        .words(this.cloudWords.map(d => ({ text: d.text, size: fontSize(d.value || 0) })))
        .padding(4)
        .rotate(() => (Math.random() > 0.8 ? 90 : 0))
        .font('Impact')
        .fontSize(d => d.size)
        .on('end', draw)

      layout.start()

      const vm = this
      function draw(words) {
        const svg = d3
          .select(container)
          .append('svg')
          .attr('width', width)
          .attr('height', height)
          .append('g')
          .attr('transform', `translate(${width / 2},${height / 2})`)

        svg
          .selectAll('text')
          .data(words)
          .enter()
          .append('text')
          .style('font-family', 'Impact')
          .style('font-size', d => `${d.size}px`)
          .style('fill', (_, i) => color(i))
          .attr('text-anchor', 'middle')
          .attr('transform', d => `translate(${d.x},${d.y})rotate(${d.rotate})`)
          .style('cursor', 'pointer')
          .text(d => d.text)
          .on('click', (event, d) => {
            vm.$router.push({ name: 'AuthorSearch', query: { q: d.text } })
          })
          .on('mouseover', (event, d) => {
            vm.tooltip = { visible: true, text: d.text, value: d.size, x: event.offsetX + 12, y: event.offsetY + 12 }
          })
          .on('mousemove', (event, d) => {
            vm.tooltip.x = event.offsetX + 12
            vm.tooltip.y = event.offsetY + 12
          })
          .on('mouseout', () => {
            vm.tooltip.visible = false
          })
      }
    },
    onResize() {
      // 简单防抖
      clearTimeout(this._resizeTid)
      this._resizeTid = setTimeout(() => this.renderWordCloud(), 200)
    },
    startAutoPlay() {
      if (this.intervalId) clearInterval(this.intervalId)
      if (this.carouselItems.length <= 1) return
      this.intervalId = setInterval(this.nextSlide, 3500)
    },
    pauseAutoPlay() {
      if (this.intervalId) clearInterval(this.intervalId)
    },
    nextSlide() {
      this.currentIndex = (this.currentIndex + 1) % this.carouselItems.length
    },
    prevSlide() {
      this.currentIndex = (this.currentIndex - 1 + this.carouselItems.length) % this.carouselItems.length
    },
    goTo(path) {
      if (!path) return
      this.$router.push(path)
    },
    formatScore(s) {
      if (s == null) return '-'
      return Number(s).toFixed(2)
    },
    gradientFor(i) {
      const colors = [
        ['#6366F1', '#22D3EE'],
        ['#F59E0B', '#EF4444'],
        ['#10B981', '#06B6D4'],
        ['#8B5CF6', '#EC4899'],
        ['#0EA5E9', '#14B8A6']
      ]
      const [a, b] = colors[i % colors.length]
      return `linear-gradient(135deg, ${a}, ${b})`
    },
    onTouchStart(e) {
      this.touchStartX = e.touches[0].clientX
      this.touchDeltaX = 0
      this.pauseAutoPlay()
    },
    onTouchMove(e) {
      this.touchDeltaX = e.touches[0].clientX - this.touchStartX
    },
    onTouchEnd() {
      const threshold = 40
      if (this.touchDeltaX > threshold) this.prevSlide()
      if (this.touchDeltaX < -threshold) this.nextSlide()
      this.startAutoPlay()
    }
  }
}
</script>

<style scoped>
:root {
  --card-bg: #ffffff;
  --muted: #6b7280;
  --ring: rgba(99,102,241,.35);
}
.home {
  max-width: 1120px;
  margin: 0 auto;
  padding: 20px 16px 56px;
}



/* Card - 高级卡片样式 */
.card { 
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  margin-top: 24px;
  box-shadow: 
    0 4px 24px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.card:hover {
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.section-header { 
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.title-wrap { 
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-wrap h2 { 
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: -0.01em;
}

.title-wrap .muted { 
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  font-weight: 400;
}

.title-wrap .icon { 
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(168, 85, 247, 0.2));
  color: #a855f7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  border: 1px solid rgba(139, 92, 246, 0.3);
}

.tools { 
  display: flex;
  gap: 8px;
}

.tool { 
  height: 32px;
  padding: 0 16px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.tool:hover { 
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.25);
  color: #ffffff;
  transform: translateY(-1px);
}

.card-body { 
  padding: 24px;
}

/* Word Cloud */
.cloud-wrap { position: relative; }
.word-cloud { width: 100%; height: 360px; }
.cloud-tooltip { 
  position: absolute;
  pointer-events: none;
  background: rgba(17, 24, 39, 0.95);
  backdrop-filter: blur(12px);
  color: #e5e7eb;
  padding: 8px 12px;
  font-size: 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}
.cloud-tooltip .tt-title { font-weight: 600; }
.cloud-tooltip .tt-sub { color:#94a3b8; margin-top: 2px; }

/* Carousel */
.carousel { position: relative; overflow: hidden; }
.track { display: flex; transition: transform .45s ease; }
.slide { 
  min-width: 100%; 
  display: grid; 
  grid-template-columns: 280px 1fr; 
  gap: 24px; 
  align-items: center; 
  cursor: pointer;
  padding: 8px;
}

.slide:hover {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
}

.media { 
  width: 100%; 
  height: 160px; 
  border-radius: 12px; 
  background-size: cover; 
  background-position: center; 
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.slide-content .title { 
  font-size: 22px; 
  font-weight: 700; 
  color: #ffffff;
  margin-bottom: 8px;
  letter-spacing: -0.01em;
}

.slide-content .subtitle { 
  color: rgba(255, 255, 255, 0.7); 
  margin-bottom: 12px;
  font-size: 15px;
}

.slide-content .meta { 
  color: rgba(255, 255, 255, 0.6); 
  font-size: 14px;
  font-weight: 500;
}

.dots { 
  display: flex; 
  gap: 10px; 
  justify-content: center; 
  padding-top: 16px;
}

.dot { 
  width: 8px; 
  height: 8px; 
  background: rgba(255, 255, 255, 0.3); 
  border-radius: 999px; 
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.dot.active { 
  background: #a855f7; 
  width: 24px;
  box-shadow: 0 0 8px rgba(168, 85, 247, 0.4);
}

.dot:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* Empty & Skeleton */
.empty { 
  color: rgba(255, 255, 255, 0.5); 
  padding: 16px 0;
  text-align: center;
  font-size: 14px;
}

.skeleton { 
  position: relative; 
  overflow: hidden; 
  background: rgba(255, 255, 255, 0.05); 
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.skeleton::after { content: ''; position: absolute; inset: 0; transform: translateX(-100%); background: linear-gradient(90deg, transparent, rgba(255,255,255,.5), transparent); animation: shimmer 1.2s infinite; }
.skeleton-cloud { height: 360px; }
.skeleton-carousel { height: 200px; }
@keyframes shimmer { 100% { transform: translateX(100%); } }

@media (max-width: 820px) {
  .slide { grid-template-columns: 1fr; }
  .media { height: 140px; }
  
  .section-header {
    padding: 16px 20px;
  }
  
  .card-body {
    padding: 20px;
  }
}

/* 学术宇宙探索区域 */
.universe-section {
  position: relative;
  margin-top: 32px;
  padding: 0;
  overflow: visible;
}

.universe-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0;
  gap: 32px;
}

.universe-text {
  flex: 1;
  max-width: 480px;
}

.universe-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.universe-subtitle {
  font-size: 24px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 16px;
  letter-spacing: -0.01em;
}

.universe-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  font-weight: 400;
  letter-spacing: 0.02em;
}

.universe-visual {
  flex-shrink: 0;
  position: relative;
  width: 320px;
  height: 240px;
}

.network-graph {
  position: relative;
  width: 100%;
  height: 100%;
}

.central-node {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 3;
}

.person-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #22d3ee, #06b6d4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 
    0 0 20px rgba(34, 211, 238, 0.4),
    0 0 40px rgba(34, 211, 238, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

.person-icon.small {
  width: 32px;
  height: 32px;
  font-size: 16px;
  background: linear-gradient(135deg, rgba(34, 211, 238, 0.8), rgba(6, 182, 212, 0.8));
  box-shadow: 0 0 15px rgba(34, 211, 238, 0.3);
}

.connection-count {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.network-lines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.line {
  position: absolute;
  background: linear-gradient(90deg, rgba(34, 211, 238, 0.6), rgba(34, 211, 238, 0.2));
  height: 2px;
  border-radius: 1px;
  animation: glow 3s ease-in-out infinite alternate;
}

.line-1 {
  top: 30%;
  left: 20%;
  width: 60%;
  transform: rotate(15deg);
  animation-delay: 0s;
}

.line-2 {
  top: 45%;
  left: 15%;
  width: 70%;
  transform: rotate(-20deg);
  animation-delay: 0.5s;
}

.line-3 {
  top: 60%;
  left: 25%;
  width: 50%;
  transform: rotate(25deg);
  animation-delay: 1s;
}

.line-4 {
  top: 35%;
  right: 20%;
  width: 55%;
  transform: rotate(-15deg);
  animation-delay: 1.5s;
}

.line-5 {
  top: 55%;
  right: 25%;
  width: 45%;
  transform: rotate(30deg);
  animation-delay: 2s;
}

.node {
  position: absolute;
  z-index: 2;
  animation: float 4s ease-in-out infinite;
}

.node-1 {
  top: 20%;
  left: 15%;
  animation-delay: 0s;
}

.node-2 {
  top: 25%;
  right: 20%;
  animation-delay: 0.8s;
}

.node-3 {
  top: 60%;
  left: 10%;
  animation-delay: 1.6s;
}

.node-4 {
  top: 70%;
  right: 15%;
  animation-delay: 2.4s;
}

.node-5 {
  top: 45%;
  right: 10%;
  animation-delay: 3.2s;
}

.universe-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(800px 400px at 80% 20%, rgba(34, 211, 238, 0.08), transparent 70%),
    radial-gradient(600px 300px at 20% 80%, rgba(139, 92, 246, 0.06), transparent 70%);
  z-index: 1;
  pointer-events: none;
}

@keyframes pulse {
  0%, 100% { 
    transform: scale(1);
    box-shadow: 
      0 0 20px rgba(34, 211, 238, 0.4),
      0 0 40px rgba(34, 211, 238, 0.2);
  }
  50% { 
    transform: scale(1.05);
    box-shadow: 
      0 0 30px rgba(34, 211, 238, 0.6),
      0 0 60px rgba(34, 211, 238, 0.3);
  }
}

@keyframes glow {
  0% { opacity: 0.4; }
  100% { opacity: 0.8; }
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

@media (max-width: 820px) {
  .universe-content {
    flex-direction: column;
    text-align: center;
    padding: 0;
    gap: 24px;
  }
  
  .universe-title {
    font-size: 28px;
  }
  
  .universe-subtitle {
    font-size: 20px;
  }
  
  .universe-visual {
    width: 280px;
    height: 200px;
  }
}


</style>
