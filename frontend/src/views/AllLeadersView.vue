<template>
  <div class="full-ranking-container">
    <div class="glass-overlay"></div>
    
    <div class="content-wrapper">
      <header class="page-header">
        <h1 class="title">领军人才<span>完整榜单</span></h1>
        <router-link to="/rankings" class="back-link">
          <i class="fa fa-arrow-left"></i> 返回概览
        </router-link>
      </header>

      <div class="controls-card">
        <div class="search-engine">
          <div class="input-wrapper">
            <i class="fa fa-search"></i>
            <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索学者姓名或研究领域..."
              @keyup.enter="searchAuthors"
            >
          </div>
          <button class="btn-glow" @click="searchAuthors">执行检索</button>
          <button class="btn-outline" @click="clearSearch" v-if="searchQuery">重置</button>
        </div>
        
        <div class="page-indicator">
          <span class="dot"></span>
          Page {{ page + 1 }} / {{ totalPages }}
        </div>
      </div>

      <div class="main-table-container">
        <div class="ranking-header">
          <div class="col-rank">RANK</div>
          <div class="col-name">学者姓名</div>
          <div class="col-score">学术总分</div>
          <div class="col-org">所属机构</div>
          <div class="col-papers">论文数</div>
        </div>

        <div v-if="loading" class="state-info">
          <div class="loader"></div>
          <p>正在检索学术大数据...</p>
        </div>
        
        <div v-else class="ranking-list">
          <div
            v-for="(author, index) in leaders"
            :key="author.id"
            class="ranking-row"
            @click="goToAuthorDetail(author)" 
            style="cursor: pointer;"
            
          >
            <div class="col-rank">
              <span class="rank-num" :class="{ 'top-rank': page === 0 && index < 3 }">
                {{ (page * size) + index + 1 }}
              </span>
            </div>
            <div class="col-name" v-html="highlightMatch(author.name)"></div>
            <div class="col-score">
              <span class="score-tag">{{ formatScore(author.totalScore) }}</span>
            </div>
            <div class="col-org" :title="author.org">
              {{ truncateOrg(processOrgName(author.org)) }}
            </div>
            <div class="col-papers">
              <i class="fa fa-file-text-o"></i> {{ author.paperCount || 0 }}
            </div>
          </div>
        </div>
      </div>

      <footer class="pagination-footer">
        <button @click="prevPage" :disabled="page === 0" class="nav-btn">
          <i class="fa fa-chevron-left"></i> 上一页
        </button>
        <div class="stats-text">
          Showing <b>{{ (page * size) + 1 }}-{{ Math.min((page + 1) * size, totalElements) }}</b> of {{ totalElements }} Scholars
        </div>
        <button @click="nextPage" :disabled="page >= totalPages - 1" class="nav-btn">
          下一页 <i class="fa fa-chevron-right"></i>
        </button>
      </footer>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

export default {
  setup() {
    const leaders = ref([])
    const loading = ref(true)
    const page = ref(0)
    const size = ref(10)
    const totalPages = ref(0)
    const totalElements = ref(0)
    const searchQuery = ref('')
    const route = useRoute()
    const router = useRouter()
    const apiError = ref(null)
    const isSearchResult = ref(false)

    // 机构名称截断
    const truncateOrg = (org) => {
      return org?.length > 40 ? `${org.substring(0, 40)}...` : org || '未知机构'
    }
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

    const fetchLeaders = async () => {
      loading.value = true
      apiError.value = null

      try {
        let url = `/api/rankings/leaders?page=${page.value}&size=${size.value}`
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

        leaders.value = response.data.content
        totalPages.value = response.data.totalPages || Math.ceil(response.data.totalElements / size.value)
        totalElements.value = response.data.totalElements

        console.log('成功获取数据:', {
          count: leaders.value.length,
          firstItem: leaders.value[0]
        })
      } catch (error) {
        console.error('API请求失败:', error)
        apiError.value = error.response?.data?.message ||
            error.message ||
            '请求失败，请检查网络'
        leaders.value = []
      } finally {
        loading.value = false
      }
    }

    const resolveAuthorId = async (author) => {
      const directId = author?.id || author?.scholarId || author?.authorId
      if (directId) return String(directId)

      const name = String(author?.name || '').trim()
      if (!name) return ''

      try {
        const resp = await axios.get('http://localhost:8080/api/rankings/search', {
          params: { name, page: 1, size: 5 }
        })
        const list = Array.isArray(resp.data) ? resp.data : []
        const exact = list.find((item) => String(item?.name || '').trim().toLowerCase() === name.toLowerCase())
        return String(exact?.id || list[0]?.id || '')
      } catch (e) {
        return ''
      }
    }

    const goToAuthorDetail = async (author) => {
      const id = await resolveAuthorId(author)
      if (!id) return
      router.push({
        name: 'AuthorDetail',
        params: { id }
      })
    }

    const searchAuthors = () => {
      page.value = 0
      fetchLeaders()
    }

    const clearSearch = () => {
      searchQuery.value = ''
      page.value = 0
      fetchLeaders()
    }

    const nextPage = () => {
      if (page.value < totalPages.value - 1) {
        page.value++
        fetchLeaders()
      }
    }

    const prevPage = () => {
      if (page.value > 0) {
        page.value--
        fetchLeaders()
      }
    }

    // 初始加载和路由监听
    onMounted(fetchLeaders)
    watch(() => route.query, fetchLeaders)

    return {
      leaders,
      loading,
      page,
      size,
      totalPages,
      totalElements,
      searchQuery,
      apiError,
      searchAuthors,
      clearSearch,
      nextPage,
      prevPage,
      truncateOrg,
      formatScore,
      highlightMatch,
      isSearchResult,
      processOrgName,
      goToAuthorDetail
    }
  }
}
</script>

<style scoped>
/* 核心背景与布局 */
.full-ranking-container {
  min-height: 100vh;
  background: #0f0f23;
  background-image: 
    radial-gradient(at 0% 0%, rgba(31, 27, 75, 0.5) 0, transparent 50%),
    radial-gradient(at 100% 100%, rgba(49, 46, 129, 0.3) 0, transparent 50%);
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

/* 标题样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 40px;
}

.title {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -1px;
  margin: 0;
}

.title span {
  display: block;
  font-size: 16px;
  color: #8b5cf6;
  text-transform: uppercase;
  letter-spacing: 4px;
  margin-top: 5px;
}

.back-link {
  color: rgba(255,255,255,0.6);
  text-decoration: none;
  font-size: 14px;
  transition: 0.3s;
  border-bottom: 1px solid transparent;
}

.back-link:hover {
  color: #fff;
  border-color: #8b5cf6;
}

/* 搜索控制区 */
.controls-card {
  background: rgba(255, 255, 255, 0.03);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.search-engine {
  display: flex;
  gap: 12px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-wrapper i {
  position: absolute;
  left: 15px;
  color: rgba(255,255,255,0.4);
}

.input-wrapper input {
  background: rgba(0,0,0,0.2);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 8px;
  padding: 12px 15px 12px 40px;
  color: #fff;
  width: 320px;
  transition: 0.3s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #8b5cf6;
  box-shadow: 0 0 15px rgba(139, 92, 246, 0.2);
}

.btn-glow {
  background: #8b5cf6;
  color: white;
  border: none;
  padding: 0 25px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.3s;
}

.btn-glow:hover {
  background: #7c3aed;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.4);
}

/* 榜单表格系统 */
.main-table-container {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.ranking-header {
  display: grid;
  grid-template-columns: 100px 1.5fr 150px 2fr 120px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 13px;
  font-weight: 700;
  color: rgba(255,255,255,0.5);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.ranking-row {
  display: grid;
  grid-template-columns: 100px 1.5fr 150px 2fr 120px;
  padding: 25px 20px; /* 增加高度，每行更舒展 */
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
  transition: 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  animation: slideIn 0.5s ease forwards;
  animation-delay: calc(var(--delay) * 0.05s);
  opacity: 0;
}

.ranking-row:hover {
  background: rgba(139, 92, 246, 0.05);
  transform: translateX(10px);
  border-left: 4px solid #8b5cf6;
}

/* 单元格细节 */
.rank-num {
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  background: rgba(255,255,255,0.05);
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
}

.top-rank {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: #fff;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.3);
}

.col-name {
  font-size: 17px;
  font-weight: 600;
}

.score-tag {
  color: #fbbf24;
  font-weight: 800;
  font-size: 18px;
  text-shadow: 0 0 10px rgba(251, 191, 36, 0.2);
}

.col-org {
  color: rgba(255,255,255,0.7);
  font-size: 14px;
}

.col-papers {
  color: rgba(255,255,255,0.5);
  font-size: 14px;
}

/* 分页控制 */
.pagination-footer {
  margin-top: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}

.nav-btn {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  padding: 10px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.3s;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(139, 92, 246, 0.2);
  border-color: #8b5cf6;
}

.nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

/* 动画与高亮 */
@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

:deep(.match) {
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.1);
  padding: 0 2px;
  border-radius: 2px;
  text-decoration: underline;
}

.loader {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(139, 92, 246, 0.1);
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 15px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
