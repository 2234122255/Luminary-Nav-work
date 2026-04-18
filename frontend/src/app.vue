<template>
   <div id="app">
    <div class="tech-background">
      <div class="grid-pattern"></div>
      <div class="floating-icons">
        <div class="icon key-icon">🔑</div>
        <div class="icon house-icon">🏠</div>
        <div class="icon dollar-icon">$</div>
        <div class="icon network-icon">🌐</div>
      </div>
      
      <div class="fingerprint-section">
        <div class="fingerprint-panel">
          <div class="fingerprint-graphic">
            <div class="fingerprint-svg">
              <svg viewBox="0 0 200 200" class="fingerprint-icon">
                <path d="M100 20 Q120 40 140 60 Q160 80 180 100 Q160 120 140 140 Q120 160 100 180 Q80 160 60 140 Q40 120 20 100 Q40 80 60 60 Q80 40 100 20" 
                      fill="none" stroke="rgba(255,255,255,0.9)" stroke-width="3" stroke-linecap="round"/>
                <path d="M100 30 Q115 45 130 60 Q145 75 160 90 Q145 105 130 120 Q115 135 100 150 Q85 135 70 120 Q55 105 40 90 Q55 75 70 60 Q85 45 100 30" 
                      fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linecap="round"/>
                <circle cx="100" cy="100" r="4" fill="rgba(255,255,255,1)" class="fingerprint-dot"/>
                <circle cx="80" cy="80" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="120" cy="80" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="80" cy="120" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <circle cx="120" cy="120" r="3" fill="rgba(255,255,255,0.8)" class="fingerprint-dot"/>
                <path d="M60 100 Q80 110 100 100 Q120 90 140 100" fill="none" stroke="rgba(255,255,255,0.6)" stroke-width="1.5" stroke-linecap="round"/>
              </svg>
            </div>
          </div>
          <div class="pointing-hand">👆</div>
        </div>
      </div>
    </div>

     <header class="header">
      <div class="header-container">
        <div class="logo-section">
          <div class="logo-icon">
            <img src="@/assets/logo.jpg" alt="Logo" class="logo-image">
          </div>
          <div class="brand-line">
            <h1 class="brand-welcome">欢迎来到 <span class="brand-name-en">Luminary Nav</span></h1>
          </div>
        </div>

        <nav class="nav-menu">
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/rankings" class="nav-link">榜单</router-link>
          <router-link to="/network" class="nav-link">合著网络</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link">后台管理</router-link>
        </nav>

        <div class="search-section">
          <div class="search-box">
            <i class="search-icon">🔍</i>
            <input 
              type="text" 
              class="search-input" 
              placeholder="搜索学者/论文" 
              v-model="searchQuery"
              @input="onSearchInput"
              @keyup.enter="executeSearch"
              @focus="showSuggestions = true"
              @blur="handleBlur"
            />
            <button class="clear-btn" @click="clearSearch" v-if="searchQuery">✕</button>
            <button class="search-btn" @click="executeSearch">搜索</button>
          </div>
          <!-- 补全推荐下拉框 -->
          <div v-if="showSuggestions && (suggestions.length > 0 || suggestionLoading)" class="suggestions-dropdown">
            <div v-if="suggestionLoading" class="suggestion-loading">加载中...</div>
            <div v-else>
              <div 
                v-for="author in suggestions" 
                :key="author.id"
                class="suggestion-item"
                @click="selectAuthor(author)"
              >
                <div class="suggestion-name">{{ author.name }}</div>
                <div class="suggestion-org">{{ truncateOrg(processOrgName(author.org)) }}</div>
              </div>
              <div v-if="suggestions.length === 0" class="suggestion-empty">未找到相关学者</div>
            </div>
          </div>
           <!-- AI Chat Icon -->
          <div class="ai-chat-icon" @click="toggleAiChat" title="AI学术助手">
            <span class="icon">🤖</span>
          </div>
          <router-link
            v-if="!isLoggedIn && $route.path !== '/login' && $route.path !== '/register'"
            to="/login"
            class="auth-btn"
          >
            立即登录
          </router-link>
          <div
            v-else-if="isLoggedIn && $route.path !== '/login' && $route.path !== '/register'"
            class="user-menu-wrap"
            ref="userMenuWrap"
          >
            <div class="user-avatar" title="User" role="button" tabindex="0" @click.stop="toggleUserMenu">
              U
            </div>
            <div v-if="userMenuOpen" class="user-menu" @click.stop>
              <button v-if="isAdmin" class="user-menu-item" type="button" @click="goToAdmin">后台管理</button>
              <button class="user-menu-item" type="button" @click="logout">退出登录</button>
            </div>
          </div>
        </div>
      </div>
    </header>


    <div class="carousel-section" v-if="$route.path === '/'">

            <div class="carousel-container">
        <div class="carousel-wrapper" :style="{ 
          transform: `translateX(-${currentSlide * 100}%)`,
          transition: `transform ${getTransitionDuration()}s cubic-bezier(0.25, 0.8, 0.25, 1)`
        }">
          <div class="carousel-slide" v-for="(slide, index) in carouselSlides" :key="index" @click="openLink(slide.link)" style="cursor: pointer;">
            <div class="slide-content hotspot-style">
              <div class="slide-bg-image" :style="{ backgroundImage: `url(${slide.image})` }"></div>
              <div class="slide-text-overlay">
                <h3 class="slide-title">{{ slide.title }}</h3>
                <p class="slide-description">{{ slide.description }}</p>
              </div>
            </div>
          </div>
        </div>
        <div class="carousel-indicators">
          <button v-for="(slide, index) in carouselSlides" :key="index" class="indicator" :class="{ active: currentSlide === index }" @click="goToSlide(index)"></button>
        </div>
        <button class="carousel-btn prev-btn" @click="prevSlide"><span>‹</span></button>
        <button class="carousel-btn next-btn" @click="nextSlide"><span>›</span></button>
      </div>
    </div>

    <main class="main-content">
      <router-view @show-detail="openModal" />
    </main>

    <ScholarDetailModal 
      :visible="isModalVisible" 
      :scholar="selectedScholar" 
      @close="isModalVisible = false" 
    />

    <AiChatDialog :visible="isAiChatVisible" @close="isAiChatVisible = false" />

    <div class="footer-wave">
      <svg viewBox="0 0 1200 120" xmlns="http://www.w3.org/2000/svg">
        <path d="M0,0V46.29c47.79,22.2,103.59,32.17,158,28,70.36-5.37,136.33-33.31,206.8-37.5C438.64,32.43,512.34,53.67,583,72.05c69.27,18,138.3,24.88,209.4,13.08,36.15-6,69.85-17.84,104.45-29.34C989.49,25,1113-14.29,1200,52.47V0Z" opacity=".25" fill="#667eea"/>
        <path d="M0,0V15.81C13,36.92,27.64,56.86,47.69,72.05,99.41,111.27,165,111,224.58,91.58c31.15-10.15,60.09-26.07,89.67-39.8,40.92-19,84.73-46,130.83-49.67,36.26-2.85,70.9,9.42,98.6,31.56,31.77,25.39,62,62,103.63,73,40.44,10.79,81.35-6.69,119.13-24.28s75.16-39,116.92-43.05c59.73-5.85,113.28,22.88,168.9,38.84,30.2,8.66,59,6.17,87.09-7.5,22.43-10.89,48-26.93,65.6-42.79C1132.92,88.14,1200,43.56,1200,43.56V0Z" opacity=".5" fill="#667eea"/>
      </svg>
    </div>
    <footer class="footer">
      <div class="footer-content">
        <button class="contact-btn">联系我们</button>
        <div class="social-icons">
          <div class="social-icon">📧</div><div class="social-icon">💼</div><div class="social-icon">🔗</div>
        </div>
        <div class="copyright">© 2026 Luminary Nav. All Rights Reserved</div>
      </div>
    </footer>
  </div>
</template>

<script>

import axios from 'axios'  // 重要：导入 axios
import ScholarDetailModal from './views/AuthorDetailView.vue'

import AiChatDialog from './components/AiChatDialog.vue';


export default {
  name: 'App',
  components: {
    ScholarDetailModal,
    AiChatDialog
  },
  data() {
    return {
      isModalVisible: false,
      isAiChatVisible: false,
      selectedScholar: null,
      auth: null,
      userMenuOpen: false,
      searchQuery: '',
      // 补全推荐所需的数据
      suggestions: [],
      showSuggestions: false,
      suggestionLoading: false,
      searchDebounceTimer: null,   // 防抖定时器
      currentSlide: 0,
      previousSlide: 0,
      carouselSlides: [
        { 
          title: '【茶思AI每周摘要】Meta超级智能团队首个大模型', 
          description: 'Anthropic 官宣史上最强模型 Mythos；智谱新开源GLM-5.1，支持独立持续工作8小时的长程任务；Meta推出全新自研模型 Muse Spark。', 
          image: 'https://images.unsplash.com/photo-1620712943543-bcc4688e7485?q=80&w=1200&auto=format&fit=crop',
          link: 'https://www.chaspark.com/#/hotspots/1261188204806799360'
        },
        { 
          title: '由Claude Code源码泄露，到Agent怎么使用才安全？', 
          description: '探讨在企业级 AI 应用中，如何通过沙箱技术（Sandboxing）提升 AI Agent 及其执行单元 Skill（技能）的安全性和稳定性。', 
          image: 'https://images.unsplash.com/photo-1555949963-aa79dcee981c?q=80&w=1200&auto=format&fit=crop',
          link: 'https://www.chaspark.com/#/hotspots/1260043906599194624'
        },
        { 
          title: '把 Claude Code 源码蒸馏成 Agent Skill', 
          description: '从 Claude Code 的实现中提炼了六个 harness 层的设计原则，产出一个可以直接安装的 Agent Skill，指导 harness 开发与设计的最佳实践。', 
          image: 'https://images.unsplash.com/photo-1550439062-609e1531270e?q=80&w=1200&auto=format&fit=crop',
          link: 'https://www.chaspark.com/#/hotspots/1260673729431998464'
        }
      ]
    }
  },
  mounted() {
    this.syncAuthFromStorage()
    this._authListener = () => this.syncAuthFromStorage()
    window.addEventListener('luminary-auth-change', this._authListener)
    this._docClickListener = (event) => {
      const wrap = this.$refs.userMenuWrap
      if (wrap && !wrap.contains(event.target)) this.userMenuOpen = false
    }
    document.addEventListener('click', this._docClickListener)
    this.startAutoPlay()
  },
  beforeUnmount() {
    this.stopAutoPlay()
    if (this.searchDebounceTimer) clearTimeout(this.searchDebounceTimer)
    if (this._authListener) window.removeEventListener('luminary-auth-change', this._authListener)
    if (this._docClickListener) document.removeEventListener('click', this._docClickListener)
  },
  computed: {
    isLoggedIn() {
      return this.auth?.role === 'user' || this.auth?.role === 'admin'
    },
    isAdmin() {
      return this.auth?.role === 'admin'
    }
  },
  methods: {
    syncAuthFromStorage() {
      try {
        const auth = JSON.parse(localStorage.getItem('luminaryAuth') || 'null')
        this.auth = auth && typeof auth === 'object' ? auth : null
      } catch {
        this.auth = null
        localStorage.removeItem('luminaryAuth')
      }
    },
    toggleUserMenu() {
      this.userMenuOpen = !this.userMenuOpen
    },
    logout() {
      localStorage.removeItem('luminaryAuth')
      window.dispatchEvent(new Event('luminary-auth-change'))
      this.userMenuOpen = false
      if (this.$route.path !== '/') this.$router.push('/')
    },
    goToAdmin() {
      this.userMenuOpen = false
      if (this.$route.path !== '/admin') this.$router.push('/admin')
    },
    toggleAiChat() {
      this.isAiChatVisible = !this.isAiChatVisible;
    },
    openModal(scholar) {
      this.selectedScholar = scholar
      this.isModalVisible = true
    },
    // 实时输入处理（防抖）
    onSearchInput() {
      if (this.searchDebounceTimer) clearTimeout(this.searchDebounceTimer)
      const query = this.searchQuery.trim()
      if (!query) {
        this.suggestions = []
        this.showSuggestions = false
        return
      }
      this.searchDebounceTimer = setTimeout(() => {
        this.fetchSuggestions(query)
      }, 300)
    },
    // 获取补全推荐
    async fetchSuggestions(keyword) {
      if (!keyword) return
      this.suggestionLoading = true
      try {
        const response = await axios.get('http://localhost:8080/api/rankings/search', {
          params: { name: keyword, page: 1, size: 5 }
        })
        this.suggestions = response.data || []
        this.showSuggestions = this.suggestions.length > 0
      } catch (err) {
        console.error('获取补全推荐失败', err)
        this.suggestions = []
        this.showSuggestions = false
      } finally {
        this.suggestionLoading = false
      }
    },
    // 选择推荐项
    selectAuthor(author) {
      this.searchQuery = author.name
      this.showSuggestions = false
      this.openModal(author)
    },
    // 执行搜索（点击搜索按钮或回车）
    async executeSearch() {
      const query = this.searchQuery.trim()
      if (!query) return
      this.showSuggestions = false
      // 尝试精确匹配或取第一个搜索结果
      try {
        const response = await axios.get('http://localhost:8080/api/rankings/search', {
          params: { name: query, size: 1 }
        })
        const authors = response.data
        if (authors && authors.length > 0) {
          this.openModal(authors[0])
        } else {
          // 无结果提示（可简单处理）
          alert(`未找到学者“${query}”`)
        }
      } catch (err) {
        console.error('搜索失败', err)
        alert('搜索失败，请检查网络或后端服务')
      }
    },
    clearSearch() {
      this.searchQuery = ''
      this.suggestions = []
      this.showSuggestions = false
    },
    handleBlur() {
      // 延迟关闭，以允许点击推荐项
      setTimeout(() => {
        this.showSuggestions = false
      }, 200)
    },
    // 辅助方法
    processOrgName(org) {
      if (!org) return '未知机构'
      return org.replace(/^["']|["']$/g, '').trim()
    },
    truncateOrg(org, maxLength = 30) {
      if (!org) return '未知机构'
      return org.length > maxLength ? org.slice(0, maxLength) + '...' : org
    },
    // 轮播图方法
    nextSlide() {
      this.previousSlide = this.currentSlide
      this.currentSlide = (this.currentSlide + 1) % this.carouselSlides.length
    },
    prevSlide() {
      this.previousSlide = this.currentSlide
      this.currentSlide = this.currentSlide === 0 ? this.carouselSlides.length - 1 : this.currentSlide - 1
    },
    goToSlide(index) {
      this.previousSlide = this.currentSlide
      this.currentSlide = index
    },
    getTransitionDuration() {
      const isBoundaryTransition = 
        (this.currentSlide === 0 && this.previousSlide === this.carouselSlides.length - 1) ||
        (this.currentSlide === this.carouselSlides.length - 1 && this.previousSlide === 0)
      return isBoundaryTransition ? 0.3 : 0.6
    },
    openLink(link) {
      if (link) {
        window.open(link, '_blank');
      }
    },
    startAutoPlay() {
      this.autoPlayInterval = setInterval(() => { this.nextSlide() }, 4000)
    },
    stopAutoPlay() {
      if (this.autoPlayInterval) clearInterval(this.autoPlayInterval)
    }
  }
}
</script>
<style scoped>
/* 原有样式保持不变，额外添加搜索补全下拉框样式 */
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

.nav-link:hover,
.nav-link.router-link-exact-active {
  color: #667eea;
}

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

/* 搜索区域 - 增加搜索按钮和下拉推荐 */
.search-section {
  display: flex;
  align-items: center;
  position: relative;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 30px;
  padding: 6px 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.search-box:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 20px rgba(100, 150, 255, 0.3);
}

.search-icon {
  margin-right: 8px;
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
  padding: 6px 0;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.clear-btn {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  padding: 0 6px;
  border-radius: 50%;
  transition: all 0.3s ease;
  font-size: 14px;
}

.clear-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}


.search-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  color: white;
  padding: 6px 16px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  margin-left: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
}

/* 补全下拉框 */
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  background: rgba(20, 20, 50, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(100, 150, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.3);
  z-index: 1100;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-loading,
.suggestion-empty {
  padding: 12px 16px;
  text-align: center;
  color: rgba(255,255,255,0.6);
  font-size: 13px;
}

.suggestion-item {
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.suggestion-item:hover {
  background: rgba(100, 150, 255, 0.2);
}

.suggestion-name {
  font-weight: 500;
  color: white;
  font-size: 14px;
  margin-bottom: 4px;
}

.suggestion-org {
  font-size: 12px;
  color: rgba(255,255,255,0.6);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}  /* ← 这里之前缺少了闭合大括号 */

/* 下面的 .ai-chat-icon 样式现在独立，不再被错误嵌套 */
.ai-chat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 4px 10px rgba(102, 126, 234, 0.4);
  margin-left: 15px;
}

.auth-btn {
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-radius: 999px;
  margin-left: 12px;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  font-weight: 600;
  border: 1px solid rgba(100, 150, 255, 0.35);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.55), rgba(118, 75, 162, 0.5));
  box-shadow: 0 10px 22px rgba(102, 126, 234, 0.22);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
}

.auth-btn:hover {
  transform: translateY(-1px);
  border-color: rgba(102, 126, 234, 0.75);
  box-shadow: 0 14px 26px rgba(102, 126, 234, 0.32);
}

.user-avatar {
  width: 40px;
  height: 40px;
  margin-left: 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(100, 150, 255, 0.35);
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 22px rgba(0, 0, 0, 0.2);
  color: rgba(209, 213, 219, 0.9);
  font-weight: 800;
  letter-spacing: 0.04em;
  user-select: none;
}

.user-menu-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
}

.user-avatar {
  cursor: pointer;
}

.user-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  min-width: 140px;
  padding: 8px;
  border-radius: 12px;
  background: rgba(15, 12, 41, 0.92);
  border: 1px solid rgba(100, 150, 255, 0.22);
  box-shadow: 0 18px 46px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(18px);
  z-index: 50;
}

.user-menu-item {
  width: 100%;
  border: none;
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.92);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s, border-color 0.2s;
  border: 1px solid rgba(100, 150, 255, 0.18);
}

.user-menu-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.35), rgba(118, 75, 162, 0.32));
  transform: translateY(-1px);
  border-color: rgba(102, 126, 234, 0.55);
}

.ai-chat-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 15px rgba(102, 126, 234, 0.6);
}

.ai-chat-icon .icon {
  font-size: 20px;
  position: static;
  width: auto;
  height: auto;
  background: none;
  border: none;
  box-shadow: none;
  animation: none;
  backdrop-filter: none;
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
  height: 400px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.carousel-wrapper {
  display: flex;
  height: 100%;
  will-change: transform;
}

.carousel-slide {
  flex: 0 0 100%;
  height: 100%;
  padding: 0 10px;
  box-sizing: border-box;
}

.slide-content.hotspot-style {
  position: relative;
  height: 100%;
  width: 100%;
  overflow: hidden;
  border-radius: 20px;
}
.slide-bg {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.slide-bg-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: transform 0.5s ease;
}



.slide-text-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 30px 40px 40px 40px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, rgba(0, 0, 0, 0.6) 60%, transparent 100%);
  color: white;
  box-sizing: border-box;
}

.slide-title {
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 600;
  line-height: 1.3;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.slide-description {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

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

.main-content {
  margin-top: 40px;
  min-height: calc(100vh - 100px);
  position: relative;
  z-index: 2;
}

.footer-wave {
  position: relative;
  margin-top: 100px;
}

.footer-wave svg {
  width: 100%;
  height: 120px;
  display: block;
}

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

/* 响应式 */
@media (max-width: 1024px) {
  .header-container {
    flex-wrap: wrap;
    gap: 15px;
  }
  .suggestions-dropdown {
    width: 280px;
    right: auto;
    left: 0;
  }
}
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
  .suggestions-dropdown {
    width: 260px;
  }
  .fingerprint-panel {
    width: 250px;
    height: 250px;
  }
  .fingerprint-graphic {
    width: 150px;
    height: 150px;
  }
  .carousel-section {
    padding: 20px 0;
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
  .suggestions-dropdown {
    width: 240px;
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
