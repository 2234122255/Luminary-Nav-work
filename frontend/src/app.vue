<template>
  <div id="app">
    <!-- 科技感背景 -->
    <div class="tech-background">
      <div class="grid-pattern"></div>
      <div class="floating-icons">
        <div class="icon key-icon">🔑</div>
        <div class="icon house-icon">🏠</div>
        <div class="icon dollar-icon">$</div>
        <div class="icon network-icon">🌐</div>
      </div>
      
      <!-- 中央指纹区域 -->
      <div class="fingerprint-section">
        <div class="fingerprint-panel">
          <div class="fingerprint-graphic">
            <div class="fingerprint-svg">
              <svg viewBox="0 0 200 200" class="fingerprint-icon">
                <!-- 外圈指纹 -->
                <path d="M100 20 Q120 40 140 60 Q160 80 180 100 Q160 120 140 140 Q120 160 100 180 Q80 160 60 140 Q40 120 20 100 Q40 80 60 60 Q80 40 100 20" 
                      fill="none" stroke="rgba(255,255,255,0.9)" stroke-width="3" stroke-linecap="round"/>
                <!-- 内圈指纹 -->
                <path d="M100 30 Q115 45 130 60 Q145 75 160 90 Q145 105 130 120 Q115 135 100 150 Q85 135 70 120 Q55 105 40 90 Q55 75 70 60 Q85 45 100 30" 
                      fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linecap="round"/>
                <!-- 指纹细节点 -->
                <circle cx="100" cy="100" r="4" fill="rgba(255,255,255,1)" class="fingerprint-dot"/>
                <circle cx="80" cy="80" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="120" cy="80" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="80" cy="120" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="120" cy="120" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <!-- 额外的指纹线条 -->
                <path d="M60 100 Q80 110 100 100 Q120 90 140 100" 
                      fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5" stroke-linecap="round"/>
                <path d="M70 70 Q90 80 110 70 Q130 60 150 70" 
                      fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1" stroke-linecap="round"/>
                <path d="M70 130 Q90 120 110 130 Q130 140 150 130" 
                      fill="none" stroke="rgba(255,255,255,0.5)" stroke-width="1" stroke-linecap="round"/>
              </svg>
            </div>
          </div>
          <!-- 指向指纹的手 -->
          <div class="pointing-hand">👆</div>
        </div>
      </div>
    </div>

    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- 品牌栏：Logo + 欢迎文案（合并为一行，更加简约） -->
        <div class="logo-section">
          <div class="logo-icon">
            <img src="@/assets/logo.jpg" alt="Logo" class="logo-image">
          </div>
          <div class="brand-line">
            <h1 class="brand-welcome">欢迎来到 <span class="brand-name-en">Luminary Nav</span></h1>
          </div>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/rankings" class="nav-link">榜单</router-link>
          <router-link to="/heatmap" class="nav-link">热力地图</router-link>
          <router-link to="/network" class="nav-link">合著网络</router-link>
        </nav>

        <!-- 搜索框 -->
        <div class="search-section">
          <div class="search-box">
            <i class="search-icon">🔍</i>
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索学者/论文"
              v-model="searchQuery"
              @keyup.enter="searchAuthors"
            >
            <button class="clear-btn" @click="clearSearch" v-if="searchQuery">✕</button>
          </div>
        </div>
      </div>
    </header>

    <!-- 轮播图区域 -->
    <div class="carousel-section">
      <div class="carousel-container">
        <div class="carousel-wrapper" :style="{ 
          transform: `translateX(-${currentSlide * 100}%)`,
          transition: `transform ${getTransitionDuration()}s cubic-bezier(0.25, 0.8, 0.25, 1)`
        }">
          <div class="carousel-slide" v-for="(slide, index) in carouselSlides" :key="index">
            <div class="slide-content">
              <div class="slide-image">
                <div class="slide-bg" :style="{ background: slide.gradient }"></div>
                <div class="slide-icon">{{ slide.icon }}</div>
              </div>
              <div class="slide-text">
                <h3 class="slide-title">{{ slide.title }}</h3>
                <p class="slide-description">{{ slide.description }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 轮播图指示器 -->
        <div class="carousel-indicators">
          <button 
            v-for="(slide, index) in carouselSlides" 
            :key="index"
            class="indicator"
            :class="{ active: currentSlide === index }"
            @click="goToSlide(index)"
          ></button>
        </div>
        
        <!-- 轮播图控制按钮 -->
        <button class="carousel-btn prev-btn" @click="prevSlide">
          <span>‹</span>
        </button>
        <button class="carousel-btn next-btn" @click="nextSlide">
          <span>›</span>
        </button>
      </div>
    </div>

    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view/>
    </main>

    <!-- 底部波浪效果 -->
    <div class="footer-wave">
      <svg viewBox="0 0 1200 120" xmlns="http://www.w3.org/2000/svg">
        <path d="M0,0V46.29c47.79,22.2,103.59,32.17,158,28,70.36-5.37,136.33-33.31,206.8-37.5C438.64,32.43,512.34,53.67,583,72.05c69.27,18,138.3,24.88,209.4,13.08,36.15-6,69.85-17.84,104.45-29.34C989.49,25,1113-14.29,1200,52.47V0Z" 
              opacity=".25" fill="#667eea"/>
        <path d="M0,0V15.81C13,36.92,27.64,56.86,47.69,72.05,99.41,111.27,165,111,224.58,91.58c31.15-10.15,60.09-26.07,89.67-39.8,40.92-19,84.73-46,130.83-49.67,36.26-2.85,70.9,9.42,98.6,31.56,31.77,25.39,62,62,103.63,73,40.44,10.79,81.35-6.69,119.13-24.28s75.16-39,116.92-43.05c59.73-5.85,113.28,22.88,168.9,38.84,30.2,8.66,59,6.17,87.09-7.5,22.43-10.89,48-26.93,65.6-42.79C1132.92,88.14,1200,43.56,1200,43.56V0Z" 
              opacity=".5" fill="#667eea"/>
        <path d="M0,0V5.63C149.93,59,314.09,71.32,475.83,42.57c43-7.64,84.23-20.12,127.61-26.46,59-8.63,112.48,12.24,165.56,35.4C827.93,77.22,886,95.24,951.2,90c86.53-7,172.46-45.71,248.8-84.81V0Z" 
              fill="#667eea"/>
      </svg>
    </div>

    <!-- 底部联系信息 -->
    <footer class="footer">
      <div class="footer-content">
        <button class="contact-btn">联系我们</button>
        <div class="social-icons">
          <div class="social-icon">📧</div>
          <div class="social-icon">💼</div>
          <div class="social-icon">🐦</div>
          <div class="social-icon">📱</div>
          <div class="social-icon">🔗</div>
        </div>
        <div class="copyright">© 2021 All Rights Reserved</div>
      </div>
    </footer>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchQuery: '',
      currentSlide: 0,
      previousSlide: 0,
      carouselSlides: [
        {
          icon: '🚀',
          title: '科技创新',
          description: '探索前沿科技，引领未来发展',
          gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        },
        {
          icon: '🔬',
          title: '学术研究',
          description: '深度学术分析，发现研究热点',
          gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
        },
        {
          icon: '🌐',
          title: '全球网络',
          description: '连接世界学者，构建合作桥梁',
          gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
        },
        {
          icon: '📊',
          title: '数据分析',
          description: '智能数据挖掘，洞察学术趋势',
          gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
        }
      ]
    }
  },
  mounted() {
    this.startAutoPlay()
  },
  beforeUnmount() {
    this.stopAutoPlay()
  },
  methods: {
    searchAuthors() {
      if (this.searchQuery.trim()) {
        this.$router.push(`/author/search?q=${encodeURIComponent(this.searchQuery)}`)
      }
    },
    clearSearch() {
      this.searchQuery = ''
    },
    nextSlide() {
      this.previousSlide = this.currentSlide
      this.currentSlide = (this.currentSlide + 1) % this.carouselSlides.length
    },
    prevSlide() {
      this.previousSlide = this.currentSlide
      this.currentSlide = this.currentSlide === 0 
        ? this.carouselSlides.length - 1 
        : this.currentSlide - 1
    },
    goToSlide(index) {
      this.previousSlide = this.currentSlide
      this.currentSlide = index
    },
    getTransitionDuration() {
      // 如果是边界切换（最后一张到第一张，或第一张到最后一张），使用更短的过渡时间
      const isBoundaryTransition = 
        (this.currentSlide === 0 && this.previousSlide === this.carouselSlides.length - 1) ||
        (this.currentSlide === this.carouselSlides.length - 1 && this.previousSlide === 0)
      
      return isBoundaryTransition ? 0.3 : 0.6
    },
    startAutoPlay() {
      this.autoPlayInterval = setInterval(() => {
        this.previousSlide = this.currentSlide
        this.nextSlide()
      }, 4000)
    },
    stopAutoPlay() {
      if (this.autoPlayInterval) {
        clearInterval(this.autoPlayInterval)
      }
    }
  }
}
</script>

<style scoped>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  color: white;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  position: relative;
  overflow-x: hidden;
}

/* 科技感背景 */
.tech-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}

.grid-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(100, 150, 255, 0.15) 1px, transparent 1px),
    linear-gradient(90deg, rgba(100, 150, 255, 0.15) 1px, transparent 1px),
    radial-gradient(circle at 25% 25%, rgba(100, 150, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(100, 150, 255, 0.1) 0%, transparent 50%);
  background-size: 50px 50px, 50px 50px, 200px 200px, 200px 200px;
  background-position: 0 0, 0 0, 0 0, 0 0;
  background-repeat: repeat, repeat, no-repeat, no-repeat;
  animation: gridMove 20s linear infinite;
}

@keyframes gridMove {
  0% { transform: translate(0, 0); }
  100% { transform: translate(50px, 50px); }
}

/* 浮动图标 */
.floating-icons {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.icon {
  position: absolute;
  width: 50px;
  height: 50px;
  background: rgba(100, 150, 255, 0.15);
  border: 2px solid rgba(100, 150, 255, 0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  animation: float 6s ease-in-out infinite;
  backdrop-filter: blur(15px);
  box-shadow: 
    0 0 20px rgba(100, 150, 255, 0.3),
    inset 0 0 20px rgba(100, 150, 255, 0.1);
  transition: all 0.3s ease;
}

.icon:hover {
  transform: scale(1.1);
  box-shadow: 
    0 0 30px rgba(100, 150, 255, 0.5),
    inset 0 0 30px rgba(100, 150, 255, 0.2);
}

.key-icon {
  top: 20%;
  left: 15%;
  animation-delay: 0s;
}

.house-icon {
  top: 35%;
  left: 10%;
  animation-delay: 1s;
}

.dollar-icon {
  top: 25%;
  right: 20%;
  animation-delay: 2s;
}

.network-icon {
  top: 50%;
  right: 15%;
  animation-delay: 3s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 指纹区域 */
.fingerprint-section {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
}

.fingerprint-panel {
  position: relative;
  width: 300px;
  height: 300px;
  background: rgba(100, 150, 255, 0.1);
  border: 2px solid rgba(100, 150, 255, 0.6);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 0 30px rgba(100, 150, 255, 0.3),
    inset 0 0 30px rgba(100, 150, 255, 0.1);
  animation: glow 3s ease-in-out infinite alternate;
}

@keyframes glow {
  0% { box-shadow: 0 0 30px rgba(100, 150, 255, 0.3), inset 0 0 30px rgba(100, 150, 255, 0.1); }
  100% { box-shadow: 0 0 50px rgba(100, 150, 255, 0.5), inset 0 0 50px rgba(100, 150, 255, 0.2); }
}

.fingerprint-graphic {
  width: 200px;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fingerprint-svg {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.fingerprint-icon {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
}

.fingerprint-dot {
  animation: pulse 2s ease-in-out infinite;
}

.fingerprint-dot:nth-child(3) { animation-delay: 0s; }
.fingerprint-dot:nth-child(4) { animation-delay: 0.4s; }
.fingerprint-dot:nth-child(5) { animation-delay: 0.8s; }
.fingerprint-dot:nth-child(6) { animation-delay: 1.2s; }
.fingerprint-dot:nth-child(7) { animation-delay: 1.6s; }

@keyframes pulse {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}

.pointing-hand {
  position: absolute;
  bottom: -60px;
  right: -40px;
  font-size: 40px;
  animation: point 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
}

@keyframes point {
  0%, 100% { transform: translateX(0px) rotate(0deg); }
  50% { transform: translateX(10px) rotate(5deg); }
}

/* 顶部导航栏 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background: rgba(15, 12, 41, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(100, 150, 255, 0.3);
  z-index: 1000;
  padding: 15px 0;
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  position: relative;
  width: 52px;
  height: 52px;
  background: radial-gradient(60% 60% at 50% 45%, rgba(255,255,255,0.25) 0%, rgba(102,126,234,0.2) 35%, rgba(118,75,162,0.22) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 18px rgba(102, 126, 234, 0.55), inset 0 0 10px rgba(118, 75, 162, 0.35);
  overflow: visible;
}

/* 旋转光环 */
.logo-icon::before {
  content: '';
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, rgba(102,126,234,0.0) 0%, rgba(102,126,234,0.75) 25%, rgba(118,75,162,0.0) 50%, rgba(118,75,162,0.75) 75%, rgba(102,126,234,0.0) 100%);
  filter: blur(0.6px);
  animation: spin 6s linear infinite;
  z-index: -1;
}

/* 内部柔光 */
.logo-icon::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: 50%;
  box-shadow: 0 0 18px rgba(102,126,234,0.55), 0 0 30px rgba(118,75,162,0.45);
  pointer-events: none;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.logo-image {
  width: 84%;
  height: 84%;
  object-fit: cover;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.65);
}

/* 品牌文案 */
/* 品牌合并行样式 */
.brand-line { display: flex; align-items: center; gap: 12px; }
.brand-welcome {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255,255,255,0.85);
  margin: 0;
}
.brand-name-en {
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #9bb8ff 0%, #ffffff 35%, #a68bdc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 10px rgba(102,126,234,0.35);
}

/* logo与文字之间的分隔线 */
/* 删除中间分割线，整体更简洁 */

.nav-menu {
  display: flex;
  gap: 40px;
}

.nav-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  padding: 10px 0;
  position: relative;
  transition: all 0.3s ease;
}

/* 悬浮与激活态颜色 */
.nav-link:hover,
.nav-link.router-link-exact-active {
  color: #667eea;
}

/* 下划线发亮效果 */
.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.nav-link:hover::after,
.nav-link.router-link-exact-active::after {
  width: 100%;
}

/* 搜索框 */
.search-section {
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 25px;
  padding: 8px 15px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(100, 150, 255, 0.3);
}

.search-icon {
  margin-right: 10px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.search-input {
  background: transparent;
  border: none;
  outline: none;
  color: white;
  font-size: 14px;
  width: 200px;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.clear-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

/* 轮播图区域 */
.carousel-section {
  margin-top: 100px;
  padding: 40px 0;
  position: relative;
  z-index: 2;
}

.carousel-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.carousel-wrapper {
  display: flex;
}

.carousel-slide {
  min-width: 100%;
  height: 300px;
  position: relative;
}

.slide-content {
  display: flex;
  height: 100%;
  align-items: center;
  padding: 0 60px;
}

.slide-image {
  flex: 0 0 200px;
  height: 200px;
  position: relative;
  margin-right: 40px;
}

.slide-bg {
  width: 100%;
  height: 100%;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

.slide-bg::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: inherit;
  filter: blur(20px);
  transform: scale(1.2);
}

.slide-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 80px;
  filter: drop-shadow(0 0 20px rgba(255, 255, 255, 0.5));
  z-index: 2;
}

.slide-text {
  flex: 1;
  color: white;
}

.slide-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.slide-description {
  font-size: 18px;
  line-height: 1.6;
  opacity: 0.9;
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.3);
}

/* 轮播图指示器 */
.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 10;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background: white;
  transform: scale(1.2);
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.7);
}

/* 轮播图控制按钮 */
.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  color: white;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;
}

.carousel-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.1);
}

.prev-btn {
  left: 20px;
}

.next-btn {
  right: 20px;
}

/* 主内容区域 */
.main-content {
  margin-top: 40px;
  min-height: calc(100vh - 100px);
  position: relative;
  z-index: 2;
}

/* 底部波浪 */
.footer-wave {
  position: relative;
  margin-top: 100px;
}

.footer-wave svg {
  width: 100%;
  height: 120px;
  display: block;
}

/* 底部 */
.footer {
  background: rgba(15, 12, 41, 0.95);
  backdrop-filter: blur(20px);
  padding: 40px 0 20px;
  text-align: center;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
}

.contact-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  padding: 15px 40px;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 30px;
  box-shadow: 0 5px 20px rgba(100, 150, 255, 0.3);
}

.contact-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(100, 150, 255, 0.4);
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.social-icon {
  width: 40px;
  height: 40px;
  background: rgba(100, 150, 255, 0.2);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.social-icon:hover {
  background: rgba(100, 150, 255, 0.4);
  transform: scale(1.1);
}

.copyright {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    gap: 20px;
    padding: 0 20px;
  }
  
  .nav-menu {
    gap: 20px;
  }
  
  .search-input {
    width: 150px;
  }
  
  .fingerprint-panel {
    width: 250px;
    height: 250px;
  }
  
  .fingerprint-graphic {
    width: 150px;
    height: 150px;
  }
  
  /* 轮播图响应式 */
  .carousel-section {
    padding: 20px 0;
  }
  
  .carousel-container {
    margin: 0 20px;
  }
  
  .carousel-slide {
    height: 250px;
  }
  
  .slide-content {
    padding: 0 30px;
    flex-direction: column;
    text-align: center;
  }
  
  .slide-image {
    flex: 0 0 120px;
    height: 120px;
    margin-right: 0;
    margin-bottom: 20px;
  }
  
  .slide-title {
    font-size: 28px;
  }
  
  .slide-description {
    font-size: 16px;
  }
  
  .carousel-btn {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .prev-btn {
    left: 10px;
  }
  
  .next-btn {
    right: 10px;
  }
}

@media (max-width: 480px) {
  .nav-menu {
    flex-wrap: wrap;
    justify-content: center;
    gap: 15px;
  }
  
  .search-input {
    width: 120px;
  }
  
  .fingerprint-panel {
    width: 200px;
    height: 200px;
  }
  
  .fingerprint-graphic {
    width: 120px;
    height: 120px;
  }
}
</style>