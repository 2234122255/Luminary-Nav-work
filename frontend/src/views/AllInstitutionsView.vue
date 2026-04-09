<template>
  <div class="full-ranking-container">
    <div class="glass-overlay"></div>
    
    <div class="content-wrapper">
      <header class="page-header">
        <h1 class="title">全球机构<span>学术排名榜单</span></h1>
        <router-link to="/rankings" class="back-link">
          <i class="fa fa-arrow-left"></i> 返回概览
        </router-link>
      </header>

      <div class="controls-card">
        <div class="search-engine">
          <div class="input-wrapper">
            <i class="fa fa-university"></i>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索机构名称（如：Xi'an Jiaotong University）..."
              @keyup.enter="searchInstitutions"
            >
          </div>
          <button class="btn-glow" @click="searchInstitutions">分析数据</button>
          <button class="btn-outline" @click="clearSearch" v-if="searchQuery">重置</button>
        </div>
        
        <div class="page-indicator">
          <span class="dot pulse"></span>
          Page {{ page + 1 }} / {{ totalPages }}
        </div>
      </div>

      <div class="main-table-container">
        <div class="ranking-header">
          <div class="col-rank">RANK</div>
          <div class="col-name">机构名称</div>
          <div class="col-score">平均学术分</div>
          <div class="col-papers">论文总数</div>
          <div class="col-authors">学者规模</div>
        </div>

        <div v-if="loading" class="state-info">
          <div class="loader"></div>
          <p>正在聚合全球机构学术大数据...</p>
        </div>
        
        <div v-else-if="institutions.length === 0" class="state-info empty">
          <i class="fa fa-folder-open-o"></i>
          <p>未发现匹配的科研机构</p>
        </div>

        <div v-else class="ranking-list">
          <div
            v-for="(inst, index) in institutions"
            :key="inst.name"
            class="ranking-row"
            :style="{ '--delay': index }"
          >
            <div class="col-rank">
              <span class="rank-num" :class="{ 'top-rank': page === 0 && index < 3 }">
                {{ (page * size) + index + 1 }}
              </span>
            </div>
            <div class="col-name" v-html="highlightMatch(processOrgName(inst.name))"></div>
            <div class="col-score">
              <span class="score-tag">{{ formatScore(inst.averageScore) }}</span>
            </div>
            <div class="col-papers">
              <span class="data-pill"><i class="fa fa-file-code-o"></i> {{ inst.paperCount || 0 }}</span>
            </div>
            <div class="col-authors">
              <i class="fa fa-users"></i> {{ inst.authorCount || 'N/A' }}
            </div>
          </div>
        </div>
      </div>

      <footer class="pagination-footer">
        <button @click="prevPage" :disabled="page === 0" class="nav-btn">
          <i class="fa fa-chevron-left"></i> 上一页
        </button>
        <div class="stats-text">
          Showing <b>{{ (page * size) + 1 }}-{{ Math.min((page + 1) * size, totalElements) }}</b> of {{ totalElements }} Institutions
        </div>
        <button @click="nextPage" :disabled="page >= totalPages - 1" class="nav-btn">
          下一页 <i class="fa fa-chevron-right"></i>
        </button>
      </footer>
    </div>
  </div>
</template>

<script>
import {ref, onMounted, watch} from 'vue'
import {useRoute} from 'vue-router'
import axios from 'axios'


export default {

  setup() {
    const institutions = ref([])
    const loading = ref(true)
    const page = ref(0)
    const size = ref(10)
    const totalPages = ref(0)
    const totalElements = ref(0)
    const searchQuery = ref('')
    const route = useRoute()
    const apiError = ref(null)
    const isSearchResult = ref(false)

    // 处理机构名：去除引号
    const processOrgName = (org) => {
      if (!org) return '未知机构'
      // 去除前后引号（单引号和双引号都处理）
      return org.replace(/^["']|["']$/g, '').trim()
    }

    // 分数格式化
    const formatScore = (score) => {
      return (score || 0).toFixed(2)
    }

    // 高亮搜索匹配
    const highlightMatch = (text) => {
      if (!searchQuery.value) return text

      const regex = new RegExp(`(${searchQuery.value})`, 'gi')
      return text.replace(regex, '<span class="match">$1</span>')
    }

    const fetchInstitutions = async () => {
      loading.value = true
      apiError.value = null

      try {
        let url = `/api/rankings/institutions?page=${page.value}&size=${size.value}`
        if (searchQuery.value) {
          url += `&name=${encodeURIComponent(searchQuery.value)}`
          isSearchResult.value = true
        } else {
          isSearchResult.value = false
        }

        const response = await axios.get(url)

        // 严格校验响应结构
        if (!response.data ||
            !Array.isArray(response.data.content) ||
            response.data.totalElements === undefined) {
          throw new Error('API返回数据结构异常')
        }

        // 校验每条数据包含必要字段
        institutions.value = response.data.content.map(item => ({
          ...item,
          name: item.name || '未知机构',
          averageScore: item.averageScore !== undefined ? item.averageScore : 0,
          paperCount: item.paperCount !== undefined ? item.paperCount : 0,
          authorCount: item.authorCount !== undefined ? item.authorCount : 0
        }))

        totalPages.value = response.data.totalPages || Math.ceil(response.data.totalElements / size.value)
        totalElements.value = response.data.totalElements

        console.log('成功获取机构数据:', {
          count: institutions.value.length,
          firstItem: institutions.value[0]
        })
      } catch (error) {
        console.error('API请求失败:', error)
        apiError.value = error.response?.data?.message ||
            error.message ||
            '请求失败，请检查网络'
        institutions.value = []
      } finally {
        loading.value = false
      }
    }

    const searchInstitutions = () => {
      page.value = 0
      fetchInstitutions()
    }

    const clearSearch = () => {
      searchQuery.value = ''
      page.value = 0
      fetchInstitutions()
    }

    const nextPage = () => {
      if (page.value < totalPages.value - 1) {
        page.value++
        fetchInstitutions()
      }
    }

    const prevPage = () => {
      if (page.value > 0) {
        page.value--
        fetchInstitutions()
      }
    }

    // 初始加载和路由监听
    onMounted(fetchInstitutions)
    watch(() => route.query, fetchInstitutions)

    return {
      institutions,
      loading,
      page,
      size,
      totalPages,
      totalElements,
      searchQuery,
      apiError,
      searchInstitutions,
      clearSearch,
      nextPage,
      prevPage,
      processOrgName,
      formatScore,
      highlightMatch,
      isSearchResult
    }
  }
}
</script>

<style scoped>
/* 保持与领军人才一致的背景与基础布局 */
.full-ranking-container {
  min-height: 100vh;
  background: #0f0f23;
  background-image: 
    radial-gradient(at 0% 0%, rgba(31, 27, 75, 0.5) 0, transparent 50%),
    radial-gradient(at 100% 100%, rgba(139, 92, 246, 0.1) 0, transparent 50%);
  color: #fff;
  padding: 40px 20px;
  position: relative;
  overflow-x: hidden;
}

.content-wrapper {
  max-width: 1300px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 标题与 Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
}

.title { font-size: 32px; font-weight: 800; letter-spacing: -1px; }
.title span {
  display: block; font-size: 16px; color: #8b5cf6;
  text-transform: uppercase; letter-spacing: 4px; margin-top: 5px;
}

.back-link {
  color: rgba(255,255,255,0.6); text-decoration: none; font-size: 14px;
  transition: 0.3s; border-bottom: 1px solid transparent;
}
.back-link:hover { color: #fff; border-color: #8b5cf6; }

/* 搜索控制区 - 玻璃拟态 */
.controls-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.search-engine { display: flex; gap: 15px; }

.input-wrapper { position: relative; display: flex; align-items: center; }
.input-wrapper i { position: absolute; left: 15px; color: #8b5cf6; }
.input-wrapper input {
  background: rgba(0,0,0,0.3);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 12px;
  padding: 12px 15px 12px 45px;
  color: #fff;
  width: 380px;
  transition: 0.3s;
}
.input-wrapper input:focus {
  outline: none; border-color: #8b5cf6;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.2);
}

.btn-glow {
  background: linear-gradient(135deg, #8b5cf6, #6d28d9);
  color: white; border: none; padding: 0 30px;
  border-radius: 12px; font-weight: 600; cursor: pointer; transition: 0.3s;
}
.btn-glow:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(139, 92, 246, 0.4);
}

/* 表格系统 */
.main-table-container {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.ranking-header {
  display: grid;
  grid-template-columns: 80px 2fr 150px 150px 150px;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 15px; font-weight: 700; color: rgba(255,255,255,0.4);
  text-transform: uppercase; letter-spacing: 2px;
}

.ranking-row {
  display: grid;
  grid-template-columns: 80px 2fr 150px 150px 150px;
  padding: 30px 30px; /* 拉长单行高度 */
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  transition: 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  animation: slideUp 0.6s ease forwards;
  animation-delay: calc(var(--delay) * 0.05s);
  opacity: 0;
}

.ranking-row:hover {
  background: rgba(139, 92, 246, 0.08);
  transform: scale(1.01) translateX(5px);
}

/* 装饰细节 */
.rank-num {
  width: 36px; height: 36px; display: flex; justify-content: center;
  align-items: center; border-radius: 10px; background: rgba(255,255,255,0.05);
  font-weight: 800; font-family: 'JetBrains Mono', monospace;
}

.top-rank {
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  color: #fff; box-shadow: 0 4px 15px rgba(59, 130, 246, 0.4);
}

.col-name { font-size: 18px; font-weight: 600; color: #fff; }

.score-tag {
  color: #fbbf24; font-weight: 800; font-size: 20px;
  text-shadow: 0 0 15px rgba(251, 191, 36, 0.3);
}

.data-pill {
  background: rgba(255,255,255,0.05); padding: 6px 12px;
  border-radius: 20px; font-size: 14px; border: 1px solid rgba(255,255,255,0.1);
}

/* 分页控制区 */
.pagination-footer {
  margin-top: 40px; display: flex; justify-content: space-between; align-items: center;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1);
  color: white; padding: 12px 25px; border-radius: 14px; cursor: pointer; transition: 0.3s;
}
.nav-btn:hover:not(:disabled) {
  background: rgba(139, 92, 246, 0.2); border-color: #8b5cf6;
}

/* 动画 */
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.loader {
  width: 40px; height: 40px; border: 3px solid rgba(139, 92, 246, 0.1);
  border-top-color: #8b5cf6; border-radius: 50%; animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin { to { transform: rotate(360deg); } }

:deep(.match) {
  color: #8b5cf6; background: rgba(139, 92, 246, 0.1);
  padding: 2px 4px; border-radius: 4px; font-weight: bold;
}
</style>