<template>
  <div class="full-ranking">
    <h1 class="title">领军人才榜单</h1>

    <div class="controls">
      <div class="search-box">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="搜索作者..."
            @keyup.enter="searchAuthors"
        >
        <button @click="searchAuthors">搜索</button>
        <button @click="clearSearch" v-if="searchQuery">清除</button>
      </div>
      <div class="pagination-info">
        {{ searchQuery ? '搜索结果' : '第' }} {{ page + 1 }} 页 / 共 {{ totalPages }} 页
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="apiError" class="error-message">错误: {{ apiError }}</div>
    <div v-else-if="leaders.length === 0" class="empty-message">
      {{ searchQuery ? '未找到匹配的作者' : '暂无数据' }}
    </div>
    <div v-else>
      <div class="ranking-header">
        <div class="header-rank">排名</div>
        <div class="header-name">作者姓名</div>
        <div class="header-score">总分</div>
        <div class="header-org">所属机构</div>
        <div class="header-papers">论文数</div>
      </div>

      <div class="ranking-list">
        <div
            v-for="(author, index) in leaders"
            :key="author.id"
            class="ranking-item"
            :class="{ 'highlight': isSearchResult && author.name.toLowerCase().includes(searchQuery.toLowerCase()) }"
        >
          <div class="rank">{{ (page * size) + index + 1 }}</div>
          <!-- 使用 v-html 渲染高亮的 HTML -->
          <div class="name" v-html="highlightMatch(author.name)"></div>
          <div class="score">{{ formatScore(author.totalScore) }}</div>
          <div class="org" :title="author.org">{{ truncateOrg(author.org) }}</div>
          <div class="papers">{{ author.paperCount || 0 }}</div>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
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
      processOrgName
    }
  }
}
</script>

<style scoped>
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
  grid-template-columns: 60px 1fr 100px 2fr 80px;
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
  grid-template-columns: 60px 1fr 100px 2fr 80px;
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
}

.score {
  text-align: center;
  font-weight: bold;
  color: #e74c3c;
}

.org {
  padding: 0 10px;
  color: #555;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.papers {
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