<template>
  <div class="rankings-view">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-main">
          <h1 class="hero-title">学术榜单</h1>
          <p class="hero-subtitle">发现卓越科研力量，展现学术成就风采</p>
        </div>
        
        <!-- 右侧筛选设置栏 -->
        <div class="hero-filter-bar">
          <!-- 年份筛选 -->
          <div class="filter-group">
            <div class="year-range">
              <div class="year-slider">
                <!-- 时间轴标签 -->
                <div class="year-labels">
                  <span class="year-label">{{ startYear }}</span>
                  <span class="year-label">{{ new Date().getFullYear() }}</span>
                </div>
                
                <div class="year-slider-track" @mousedown="startTrackDragging">
                  <div class="year-slider-fill" :style="{ left: startYearPercent + '%', right: (100 - endYearPercent) + '%' }"></div>
                  <div 
                    class="year-slider-thumb start" 
                    :style="{ left: startYearPercent + '%' }"
                    @mousedown.stop="startDragging('start')"
                    @keydown="handleKeyDown('start', $event)"
                    tabindex="0"
                    role="slider"
                    :aria-valuenow="startYear"
                    :aria-valuemin="minYear"
                    :aria-valuemax="endYear - 1"
                    aria-label="起始年份"
                  ></div>
                  <div 
                    class="year-slider-thumb end" 
                    :style="{ left: endYearPercent + '%' }"
                    @mousedown.stop="startDragging('end')"
                    @keydown="handleKeyDown('end', $event)"
                    tabindex="0"
                    role="slider"
                    :aria-valuenow="endYear"
                    :aria-valuemin="startYear + 1"
                    :aria-valuemax="maxYear"
                    aria-label="结束年份"
                  ></div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 领域设置 -->
          <div class="filter-group">
            <div class="select-wrapper">
              <select class="filter-select" v-model="selectedField">
                <option value="">全部领域</option>
                <option value="computer-science">计算机科学</option>
                <option value="physics">物理学</option>
                <option value="chemistry">化学</option>
                <option value="biology">生物学</option>
                <option value="mathematics">数学</option>
                <option value="engineering">工程学</option>
                <option value="medicine">医学</option>
                <option value="economics">经济学</option>
              </select>
            </div>
          </div>
          
          <!-- 地域范围设置 -->
          <div class="filter-group">
            <div class="select-wrapper">
              <select class="filter-select" v-model="selectedRegion">
                <option value="">全部地域</option>
                <option value="china">中国</option>
                <option value="north-america">北美</option>
                <option value="europe">欧洲</option>
                <option value="asia-pacific">亚太</option>
                <option value="other">其他</option>
              </select>
            </div>
          </div>
          
          <!-- 数据点总数显示 -->
          <div class="data-count">
            <span class="count-number">{{ filteredCount }}</span>
          </div>
          
          <!-- 筛选按钮 -->
          <button class="filter-btn" @click="applyFilters">
            筛选
          </button>
        </div>
      </div>
    </section>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 榜单网格 -->
      <div class="rankings-grid">
        <!-- 领军人才榜单 -->
        <div class="ranking-card leaders-card">
          <h2 class="card-title">
            领军人才榜单
            <router-link to="/rankings/leaders" class="view-all">查看全部</router-link>
          </h2>

          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="error" class="error-message">{{ error }}</div>
          <div v-else class="ranking-list">
            <div
                v-for="(author, index) in leaders"
                :key="author.id"
                class="ranking-item"
                :class="{ 'top-three': index < 3 }"
            >
              <div class="rank-badge">{{ index + 1 }}</div>
              <div class="author-info">
                <div class="name" :title="author.name">{{ author.name || '未知作者' }}</div>
                <div class="org" :title="processOrgName(author.org)">
                  {{ truncateOrg(processOrgName(author.org)) }}
                </div>
              </div>
              <div class="stats">
                <div class="papers">
                  <span class="label">论文数:</span>
                  <i class="fa fa-file-text-o"></i>
                  <span class="value">{{ author.paperCount || 0 }}</span>
                </div>
                <div class="score">
                  <span class="label">总分:</span>
                  <i class="fa fa-trophy"></i>
                  <span class="value">{{ formatScore(author.totalScore) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 机构排名榜单 -->
        <div class="ranking-card">
          <h2 class="card-title">
            机构排名
            <router-link to="/rankings/institutions" class="view-all">查看全部</router-link>
          </h2>

          <div v-if="institutionsLoading" class="loading">加载中...</div>
          <div v-else-if="institutionsError" class="error-message">{{ institutionsError }}</div>
          <div v-else class="ranking-list">
            <div
                v-for="(institution, index) in institutions"
                :key="institution.id || institution.name"
                class="ranking-item"
                :class="{ 'top-three': index < 3 }"
            >
              <div class="rank-badge">{{ index + 1 }}</div>
              <div class="author-info">
                <div class="name" :title="processOrgName(institution.name)">
                  {{ truncateOrg(processOrgName(institution.name), 28) }}
                </div>
              </div>
              <div class="stats">
                <div class="papers">
                  <span class="label">论文数:</span>
                  <i class="fa fa-file-text-o"></i>
                  <span class="value">{{ institution.paperCount || 0 }}</span>
                </div>
                <div class="score">
                  <span class="label">平均分:</span>
                  <i class="fa fa-trophy"></i>
                  <span class="value">{{ formatScore(institution.averageScore) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 潜力新星榜单 -->
        <div class="ranking-card">
          <h2 class="card-title">
            潜力新星榜单
            <router-link to="/rankings/rising-stars" class="view-all">查看全部</router-link>
          </h2>

          <div class="ranking-list placeholder-list">
            <div v-for="i in 10" :key="`star-${i}`" class="ranking-item placeholder-item">
              <div class="rank-badge">{{ i }}</div>
              <div class="author-info">
                <div class="name">——</div>
                <div class="org">——</div>
              </div>
              <div class="stats">
                <div class="papers">
                  <span class="label">论文数:</span>
                  <i class="fa fa-file-text-o"></i>
                  <span class="value">——</span>
                </div>
                <div class="score">
                  <span class="label">总分:</span>
                  <i class="fa fa-trophy"></i>
                  <span class="value">——</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  setup() {
    // 领军人才数据
    const leaders = ref([])
    const loading = ref(true)
    const error = ref(null)

    // 机构排名数据
    const institutions = ref([])
    const institutionsLoading = ref(true)
    const institutionsError = ref(null)

    // 筛选相关数据
    const startYear = ref(2000)
    const endYear = ref(new Date().getFullYear())
    const selectedField = ref('')
    const selectedRegion = ref('')
    const filteredCount = ref(213)
    
    // 滑块拖拽相关
    const isDragging = ref(false)
    const dragType = ref('')
    
    // 年份范围
    const minYear = 2000
    const maxYear = new Date().getFullYear()
    
    // 计算年份百分比
    const startYearPercent = ref(0) // 2000年对应0%
    const endYearPercent = ref(100) // 当前年份对应100%

    // 格式化分数
    const formatScore = (score) => {
      return score !== null && score !== undefined
          ? score.toFixed(2)
          : '0.00'
    }

    // 处理机构名：去除引号
    const processOrgName = (org) => {
      if (!org) return '未知机构'
      // 去除前后引号（单引号和双引号都处理）
      return org.replace(/^["']|["']$/g, '').trim()
    }

    // 截断机构名，不显示完全
    const truncateOrg = (org, maxLength = 24) => {
      if (!org) return '未知机构'
      return org.length > maxLength ? org.slice(0, maxLength) + '...' : org
    }

    // 获取领军人才数据
    const fetchLeaders = async () => {
      try {
        const response = await axios.get('/api/rankings/top10')
        leaders.value = response.data.content || []
      } catch (err) {
        console.error('获取领军人才数据失败:', err)
        error.value = '数据加载失败'
      } finally {
        loading.value = false
      }
    }

    // 获取机构排名数据
    const fetchInstitutions = async () => {
      try {
        const response = await axios.get('/api/rankings/institutions/top10')
        institutions.value = response.data.content || []
      } catch (err) {
        console.error('获取机构数据失败:', err)
        institutionsError.value = '数据加载失败'
      } finally {
        institutionsLoading.value = false
      }
    }

    // 应用筛选方法
    const applyFilters = () => {
      console.log('应用筛选:', {
        startYear: startYear.value,
        endYear: endYear.value,
        field: selectedField.value,
        region: selectedRegion.value
      })
      
      // 模拟筛选后的数量变化
      const baseCount = 213
      let filteredCountValue = baseCount
      
      // 根据年份范围筛选
      const yearRange = endYear.value - startYear.value
      if (yearRange < 5) {
        filteredCountValue = Math.floor(filteredCountValue * 0.6)
      } else if (yearRange < 10) {
        filteredCountValue = Math.floor(filteredCountValue * 0.8)
      }
      
      if (selectedField.value) filteredCountValue = Math.floor(filteredCountValue * 0.8)
      if (selectedRegion.value) filteredCountValue = Math.floor(filteredCountValue * 0.7)
      
      filteredCount.value = filteredCountValue
    }
    
    // 开始拖拽滑块
    const startDragging = (type) => {
      isDragging.value = true
      dragType.value = type
      
      const handleMouseMove = (e) => {
        if (!isDragging.value) return
        
        const slider = e.currentTarget.closest('.year-slider')
        if (!slider) return
        
        const rect = slider.getBoundingClientRect()
        const x = e.clientX - rect.left
        const percent = Math.max(0, Math.min(100, (x / rect.width) * 100))
        
        if (dragType.value === 'start') {
          // 允许起始滑块移动到结束滑块位置，但年份值保持最小间隔
          const yearRange = maxYear - minYear
          const targetYear = minYear + Math.round((percent / 100) * yearRange)
          const newStartYear = Math.max(minYear, targetYear)
          
          // 如果目标年份大于等于结束年份，则调整结束年份
          if (newStartYear >= endYear.value) {
            endYear.value = Math.min(maxYear, newStartYear + 1)
            endYearPercent.value = ((endYear.value - minYear) / yearRange) * 100
          }
          
          startYear.value = newStartYear
          startYearPercent.value = ((startYear.value - minYear) / yearRange) * 100
        } else if (dragType.value === 'end') {
          // 允许结束滑块移动到起始滑块位置，但年份值保持最小间隔
          const yearRange = maxYear - minYear
          const targetYear = minYear + Math.round((percent / 100) * yearRange)
          const newEndYear = Math.min(maxYear, targetYear)
          
          // 如果目标年份小于等于起始年份，则调整起始年份
          if (newEndYear <= startYear.value) {
            startYear.value = Math.max(minYear, newEndYear - 1)
            startYearPercent.value = ((startYear.value - minYear) / yearRange) * 100
          }
          
          endYear.value = newEndYear
          endYearPercent.value = ((endYear.value - minYear) / yearRange) * 100
        }
      }
      
      const handleMouseUp = () => {
        isDragging.value = false
        dragType.value = ''
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
      }
      
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }
    
    // 处理键盘事件，以1年为单位调整
    const handleKeyDown = (type, event) => {
      event.preventDefault()
      
      if (type === 'start') {
        if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
          // 减少起始年份
          if (startYear.value > minYear) {
            startYear.value--
            // 如果起始年份等于结束年份，则调整结束年份
            if (startYear.value === endYear.value) {
              endYear.value = Math.min(maxYear, startYear.value + 1)
            }
            updateYearPercentages()
          }
        } else if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
          // 增加起始年份
          if (startYear.value < endYear.value) {
            startYear.value++
            updateYearPercentages()
          }
        }
      } else if (type === 'end') {
        if (event.key === 'ArrowLeft' || event.key === 'ArrowDown') {
          // 减少结束年份
          if (endYear.value > startYear.value) {
            endYear.value--
            updateYearPercentages()
          }
        } else if (event.key === 'ArrowRight' || event.key === 'ArrowUp') {
          // 增加结束年份
          if (endYear.value < maxYear) {
            endYear.value++
            updateYearPercentages()
          }
        }
      }
    }
    
    // 轨道拖拽，移动整个年份范围
    const startTrackDragging = (event) => {
      const slider = event.currentTarget
      const rect = slider.getBoundingClientRect()
      const x = event.clientX - rect.left
      const percent = Math.max(0, Math.min(100, (x / rect.width) * 100))
      
      const yearRange = maxYear - minYear
      const targetYear = minYear + Math.round((percent / 100) * yearRange)
      const currentRange = endYear.value - startYear.value
      
      // 计算新的起始和结束年份，保持当前范围
      let newStartYear = Math.max(minYear, targetYear - Math.floor(currentRange / 2))
      let newEndYear = newStartYear + currentRange
      
      // 确保不超出边界
      if (newEndYear > maxYear) {
        newEndYear = maxYear
        newStartYear = Math.max(minYear, newEndYear - currentRange)
      }
      
      startYear.value = newStartYear
      endYear.value = newEndYear
      updateYearPercentages()
    }
    
    // 更新年份百分比
    const updateYearPercentages = () => {
      const yearRange = maxYear - minYear
      startYearPercent.value = ((startYear.value - minYear) / yearRange) * 100
      endYearPercent.value = ((endYear.value - minYear) / yearRange) * 100
    }

    onMounted(() => {
      // 初始化年份百分比
      updateYearPercentages()
      
      fetchLeaders()
      fetchInstitutions()
    })

    return {
      leaders,
      loading,
      error,
      institutions,
      institutionsLoading,
      institutionsError,
      startYear,
      endYear,
      selectedField,
      selectedRegion,
      filteredCount,
      startYearPercent,
      endYearPercent,
      formatScore,
      processOrgName,
      truncateOrg,
      applyFilters,
      startDragging,
      handleKeyDown,
      startTrackDragging,
      updateYearPercentages
    }
  }
}
</script>

<style scoped>
.rankings-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0f23 0%, #1e1b4b 50%, #312e81 100%);
  padding: 20px 16px 56px;
  color: #ffffff;
}

/* 英雄区域 */
.hero-section {
  padding: 40px 0;
  position: relative;
}

.hero-content {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;
}

.hero-main {
  flex: 1;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

/* 主要内容区域 */
.main-content {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0;
  margin-top: 32px;
}

/* 英雄区域筛选栏 */
.hero-filter-bar {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
  min-width: 600px;
  padding: 16px;
  position: relative;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
  padding: 0 16px;
  position: relative;
}

/* 年份滑块样式 */
.year-range {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.year-slider {
  position: relative;
  width: 200px;
}

/* 时间轴标签 */
.year-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.year-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  font-weight: 500;
}

.year-slider-track {
  position: relative;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  cursor: grab;
  transition: background 0.2s ease;
}

.year-slider-track:hover {
  background: rgba(255, 255, 255, 0.3);
}

.year-slider-track:active {
  cursor: grabbing;
}

.year-slider-fill {
  position: absolute;
  height: 100%;
  background: rgba(139, 92, 246, 0.6);
  border-radius: 2px;
  transition: all 0.3s ease;
}

.year-slider-thumb {
  position: absolute;
  top: 50%;
  width: 16px;
  height: 16px;
  background: #ffffff;
  border: 2px solid #8b5cf6;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.2s ease;
  outline: none;
}

.year-slider-thumb:hover {
  transform: translate(-50%, -50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.year-slider-thumb:focus {
  outline: 2px solid rgba(139, 92, 246, 0.8);
  outline-offset: 2px;
  transform: translate(-50%, -50%) scale(1.1);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.6);
}

/* 选择框样式 */
.select-wrapper {
  position: relative;
}

.filter-select {
  width: 140px;
  height: 32px;
  padding: 0 12px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  text-align: center;
  cursor: pointer;
  appearance: none;
  transition: all 0.2s ease;
}

.filter-select:hover {
  color: #ffffff;
}

.filter-select:focus {
  outline: none;
  color: #ffffff;
}

.filter-select option {
  background: #1e1b4b;
  color: #ffffff;
  padding: 8px;
}

/* 筛选按钮 */
.filter-btn {
  height: 36px;
  padding: 0 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-left: 8px;
}

.filter-btn:hover {
  color: #ffffff;
  transform: translateY(-1px);
}

/* 数据点总数显示 */
.data-count {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  transition: all 0.2s ease;
  margin: 0 16px;
  position: relative;
}

.data-count:hover {
  transform: scale(1.05);
}

.count-number {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  text-shadow: 0 0 8px rgba(139, 92, 246, 0.5);
}

/* 榜单网格 */
.rankings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.ranking-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 28px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.ranking-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
}

.leaders-card {
  border-top: 3px solid rgba(139, 92, 246, 0.6);
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
}

.view-all {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: color 0.3s;
}

.view-all:hover {
  color: #ffffff;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: grid;
  grid-template-columns: 50px 1fr 180px;
  align-items: flex-start;
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.02);
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid transparent;
}

.ranking-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.1), transparent);
  transition: left 0.8s ease;
}

.ranking-item:hover {
  transform: translateX(5px);
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.1);
}

.ranking-item:hover::before {
  left: 100%;
}

.top-three .rank-badge {
  color: white;
}

.top-three:nth-child(1) .rank-badge {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
}

.top-three:nth-child(2) .rank-badge {
  background: linear-gradient(135deg, #9ca3af, #6b7280);
}

.top-three:nth-child(3) .rank-badge {
  background: linear-gradient(135deg, #fb923c, #ea580c);
}

.rank-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  font-weight: bold;
  font-size: 15px;
  transition: all 0.3s;
  margin-top: 2px;
}

.ranking-item:hover .rank-badge {
  transform: scale(1.1);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-right: 10px;
}

.name {
  color: #ffffff;
  font-weight: 500;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  max-width: 100%;
}

.stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  margin-top: 2px;
}

.papers, .score {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.03);
  transition: all 0.3s;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.ranking-item:hover .papers,
.ranking-item:hover .score {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.1);
}

.papers i, .score i {
  color: rgba(139, 92, 246, 0.8);
  font-size: 14px;
}

.label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 12px;
  white-space: nowrap;
}

.value {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
}

.score .value {
  color: #fbbf24;
}

.placeholder-list {
  opacity: 0.5;
}

.placeholder-item {
  background: rgba(255, 255, 255, 0.02);
}

.placeholder-item .name,
.placeholder-item .org,
.placeholder-item .label,
.placeholder-item .value {
  color: rgba(255, 255, 255, 0.3);
}

.placeholder-item .papers i,
.placeholder-item .score i {
  color: rgba(255, 255, 255, 0.3);
}

.loading, .error-message {
  text-align: center;
  padding: 40px 0;
  color: rgba(255, 255, 255, 0.7);
  font-size: 15px;
}

.error-message {
  color: #f87171;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .rankings-grid {
    grid-template-columns: 1fr 1fr;
  }
  
  .hero-content {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }
  
  .hero-main {
    text-align: center;
  }
  
  .hero-title {
    font-size: 28px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .hero-filter-bar {
    min-width: 100%;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    padding: 12px;
  }
  
  .filter-group {
    padding: 0 12px;
  }
  
  .year-slider {
    width: 100%;
  }
  
  .year-labels {
    font-size: 10px;
  }
  
  .filter-select {
    width: 120px;
  }
  
  .filter-btn {
    margin-left: 0;
    margin-top: 8px;
  }
}

@media (max-width: 768px) {
  .rankings-grid {
    grid-template-columns: 1fr;
  }

  .stats {
    gap: 8px;
  }

  .papers, .score {
    padding: 4px 5px;
  }

  .label {
    font-size: 11px;
  }
}
</style>