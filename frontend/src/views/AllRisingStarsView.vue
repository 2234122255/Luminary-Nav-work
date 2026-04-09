<template>
  <div class="stars-view-container">
    <div class="nebula-bg"></div>
    
    <div class="content-wrapper">
      <header class="page-header">
        <h1 class="title">潜力新星<span>学术未来势力榜</span></h1>
        <router-link to="/rankings" class="back-link">返回概览</router-link>
      </header>

      <div class="search-panel">
        <div class="search-input-group">
          <i class="fa fa-magic"></i>
          <input v-model="searchQuery" placeholder="搜索极具潜力的学者..." @keyup.enter="fetchStars">
          <button class="search-btn" @click="fetchStars">探测</button>
        </div>
        <div class="info-tag">筛选标准：论文数 (5-20) 且 学术分领先</div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="orbit-spinner"></div>
        <p>正在分析学术增长曲线...</p>
      </div>

      <div v-else class="stars-grid">
        <div v-for="(star, index) in stars" :key="star.id" class="star-card" :style="{'--delay': index}">
          <div class="card-glow"></div>
          <div class="rank-tag">#{{ (page * size) + index + 1 }}</div>
          
          <div class="user-profile">
            <div class="avatar-placeholder">{{ star.name.charAt(0) }}</div>
            <div class="name-area">
              <h3 class="name">{{ star.name }}</h3>
              <p class="org">{{ processOrgName(star.org) }}</p>
            </div>
          </div>

          <div class="growth-stats">
            <div class="stat-item">
              <span class="stat-label">学术潜力值</span>
              <span class="stat-value highlight">{{ formatScore(star.totalScore) }}</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-label">当前论文数</span>
              <span class="stat-value">{{ star.paperCount }}</span>
            </div>
          </div>
        </div>
      </div>

      <footer class="pagination">
        <button :disabled="page === 0" @click="changePage(-1)">PREV</button>
        <span class="page-num">PAGE {{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page >= totalPages - 1" @click="changePage(1)">NEXT</button>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const stars = ref([])
const loading = ref(true)
const page = ref(0)
const size = ref(12) // 每页12个，适合网格布局
const totalPages = ref(0)
const totalElements = ref(0)
const searchQuery = ref('')

const fetchStars = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/rankings/stars`, {
      params: { page: page.value, size: size.value, name: searchQuery.value }
    })
    stars.value = res.data.content
    totalElements.value = res.data.totalElements
    totalPages.value = res.data.totalPages
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
}

const changePage = (delta) => {
  page.value += delta
  fetchStars()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const formatScore = (s) => (s || 0).toFixed(4)
const processOrgName = (org) => org?.replace(/["']/g, '') || '未知机构'

onMounted(fetchStars)
</script>

<style scoped>
.stars-view-container {
  min-height: 100vh;
  background: #050510;
  color: #fff;
  padding: 60px 40px;
  position: relative;
  overflow: hidden;
}

/* 背景星云效果 */
.nebula-bg {
  position: absolute;
  top: -20%; right: -10%;
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.15) 0%, transparent 70%);
  filter: blur(50px);
}

.content-wrapper { position: relative; z-index: 2; max-width: 1400px; margin: 0 auto; }

.title { font-size: 38px; font-weight: 800; }
.title span { display: block; font-size: 16px; color: #a78bfa; letter-spacing: 5px; margin-top: 8px; }

.search-panel { margin: 40px 0; display: flex; align-items: center; gap: 20px; }

.search-input-group {
  display: flex; align-items: center; background: rgba(255,255,255,0.05);
  border: 1px solid rgba(255,255,255,0.1); border-radius: 15px; padding: 5px 20px;
}

.search-input-group input {
  background: transparent; border: none; color: #fff; padding: 12px; width: 300px; outline: none;
}

.search-btn {
  background: #8b5cf6; color: white; border: none; padding: 8px 20px; border-radius: 10px; cursor: pointer;
}

.stars-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 25px;
}

/* 卡片样式 */
.star-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 30px;
  position: relative;
  transition: 0.4s;
  animation: fadeIn 0.5s ease forwards;
  animation-delay: calc(var(--delay) * 0.05s);
  opacity: 0;
}

.star-card:hover {
  transform: translateY(-10px);
  background: rgba(255, 255, 255, 0.06);
  border-color: #8b5cf6;
  box-shadow: 0 15px 35px rgba(139, 92, 246, 0.2);
}

.rank-tag {
  position: absolute; top: 20px; right: 20px;
  font-family: 'JetBrains Mono'; color: rgba(255,255,255,0.2); font-size: 24px; font-weight: 800;
}

.user-profile { display: flex; align-items: center; gap: 15px; margin-bottom: 25px; }

.avatar-placeholder {
  width: 50px; height: 50px; background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold;
}

.name { font-size: 20px; margin: 0; }
.org { font-size: 13px; color: rgba(255,255,255,0.5); margin: 5px 0 0; }

.growth-stats {
  display: flex; justify-content: space-between; align-items: center;
  background: rgba(0,0,0,0.2); border-radius: 15px; padding: 15px;
}

.stat-label { font-size: 11px; color: rgba(255,255,255,0.4); text-transform: uppercase; display: block; margin-bottom: 5px; }
.stat-value { font-size: 18px; font-weight: 700; }
.stat-value.highlight { color: #fbbf24; }

@keyframes fadeIn { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>