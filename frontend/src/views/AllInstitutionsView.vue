<template>
  <div class="full-ranking">
    <h1 class="title">机构排名榜单</h1>

    <div class="controls">
      <div class="search-box">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="搜索机构..."
            @keyup.enter="searchInstitutions"
        >
        <button @click="searchInstitutions">搜索</button>
        <button @click="clearSearch" v-if="searchQuery">清除</button>
      </div>
      <div class="pagination-info">
        {{ searchQuery ? '搜索结果' : '第' }} {{ page + 1 }} 页 / 共 {{ totalPages }} 页
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="apiError" class="error-message">错误: {{ apiError }}</div>
    <div v-else-if="institutions.length === 0" class="empty-message">
      {{ searchQuery ? '未找到匹配的机构' : '暂无数据' }}
    </div>
    <div v-else>
      <div class="ranking-header">
        <div class="header-rank">排名</div>
        <div class="header-name">机构名称</div>
        <div class="header-score">平均分</div>
        <div class="header-papers">论文总数</div>

      </div>

      <div class="ranking-list">
        <div
            v-for="(institution, index) in institutions"
            :key="institution.id || institution.name"
            class="ranking-item"
            :class="{ 'highlight': isSearchResult && institution.name.toLowerCase().includes(searchQuery.toLowerCase()) }"
        >
          <div class="rank">{{ (page * size) + index + 1 }}</div>
          <div class="name" v-html="highlightMatch(processOrgName(institution.name))"></div>
          <div class="score">{{ formatScore(institution.averageScore) }}</div>
          <div class="papers">{{ institution.paperCount || 0 }}</div>

        </div>
      </div>

      <div class="pagination">
        <button @click="prevPage" :disabled="page === 0">上一页</button>
        <span>
          显示 {{ (page * size) + 1 }}-{{ Math.min((page + 1) * size, totalElements) }} 条，
          共 {{ totalElements }} 条
        </span>
        <button @click="nextPage" :disabled="page >= totalPages - 1">下一页</button>
      </div>
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
/* 保持与领军人才榜单相同的样式 */
.full-ranking {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.title {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
  font-size: 28px;
}

.controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.search-box {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-box input {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
  font-size: 14px;
}

.search-box button {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.search-box button:hover {
  background: #2980b9;
}

.ranking-header {
  display: grid;
  grid-template-columns: 60px 2fr 100px 100px 100px;
  padding: 12px 15px;
  background: #34495e;
  color: white;
  font-weight: bold;
  border-radius: 4px 4px 0 0;
  align-items: center;
}

.ranking-list {
  border: 1px solid #eee;
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.ranking-item {
  display: grid;
  grid-template-columns: 60px 2fr 100px 100px 100px;
  align-items: center;
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  transition: background 0.2s;
}

.ranking-item:hover {
  background-color: #f8f9fa;
}

.ranking-item.highlight {
  background-color: #f0f8ff;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank {
  text-align: center;
  font-weight: bold;
  color: #7f8c8d;
}

.name {
  font-weight: 500;
  color: #2c3e50;
  padding: 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.score {
  text-align: center;
  font-weight: bold;
  color: #e74c3c;
}

.papers, .authors {
  text-align: center;
  color: #555;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.pagination button {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.pagination button:hover:not(:disabled) {
  background: #2980b9;
}

.pagination button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.loading, .error-message, .empty-message {
  text-align: center;
  padding: 50px;
  font-size: 18px;
}

.loading {
  color: #7f8c8d;
}

.error-message {
  color: #e74c3c;
}

.empty-message {
  color: #95a5a6;
}

.match {
  background-color: #fff3cd;
  font-weight: bold;
}
</style>