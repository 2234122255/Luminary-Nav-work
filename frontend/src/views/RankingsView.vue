<template>
  <div class="rankings-container">
    <h1 class="page-title">综合榜单</h1>

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

      <!-- 潜力新星榜单（预留位置） -->
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

    onMounted(() => {
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
      formatScore,
      processOrgName,
      truncateOrg
    }
  }
}
</script>

<style scoped>
.rankings-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.page-title {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
  font-size: 28px;
  font-weight: 600;
}

.rankings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 25px;
}

.ranking-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.ranking-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.leaders-card {
  border-top: 4px solid #3498db;
}

.card-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  margin-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  color: #3498db;
  font-size: 18px;
  font-weight: 600;
}

.view-all {
  font-size: 13px;
  color: #3498db;
  text-decoration: none;
  transition: color 0.3s;
}

.view-all:hover {
  text-decoration: underline;
  color: #2980b9;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ranking-item {
  display: grid;
  grid-template-columns: 50px 1fr 180px;
  align-items: flex-start; /* 改为顶部对齐，适应换行 */
  padding: 12px 15px;
  border-radius: 8px;
  background: #f9f9f9;
  font-size: 14px;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  overflow: hidden;
}

.ranking-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(52, 152, 219, 0.1), transparent);
  transition: left 0.8s ease;
}

.ranking-item:hover {
  transform: translateX(5px);
  background: #f5f5f5;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.ranking-item:hover::before {
  left: 100%;
}

.top-three .rank-badge {
  color: white;
}

.top-three:nth-child(1) .rank-badge {
  background: #f39c12;
}

.top-three:nth-child(2) .rank-badge {
  background: #bdc3c7;
}

.top-three:nth-child(3) .rank-badge {
  background: #e67e22;
}

.rank-badge {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #ecf0f1;
  color: #7f8c8d;
  font-weight: bold;
  font-size: 15px;
  transition: all 0.3s;
  margin-top: 2px; /* 微调位置，适应顶部对齐 */
}

.ranking-item:hover .rank-badge {
  transform: scale(1.1);
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding-right: 10px; /* 增加右侧间距 */
}

.name {
  color: #2c3e50;
  font-weight: 500;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 新增：机构名容器样式，允许换行 */
.org-wrap {
  max-width: 100%;
  word-wrap: break-word; /* 允许单词内换行 */
  word-break: break-all; /* 强制换行 */
  line-height: 1.4; /* 增加行高，提高可读性 */
}

.org {
  color: #7f8c8d;
  font-size: 13px;
  max-width: 100%; /* 占满可用宽度 */
}

.stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 15px;
  margin-top: 2px; /* 微调位置，适应顶部对齐 */
}

.papers, .score {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 8px;
  border-radius: 4px;
  background: rgba(52, 152, 219, 0.05);
  transition: all 0.3s;
}

.ranking-item:hover .papers,
.ranking-item:hover .score {
  background: rgba(52, 152, 219, 0.1);
}

.papers i, .score i {
  color: #3498db;
  font-size: 14px;
}

.label {
  color: #7f8c8d;
  font-size: 12px;
  white-space: nowrap;
}

.value {
  font-weight: 500;
  color: #34495e;
}

.score .value {
  color: #e74c3c;
}

.placeholder-list {
  opacity: 0.7;
}

.placeholder-item {
  background: #f5f5f5;
}

.placeholder-item .name,
.placeholder-item .org,
.placeholder-item .label,
.placeholder-item .value {
  color: #bdc3c7;
}

.placeholder-item .papers i,
.placeholder-item .score i {
  color: #bdc3c7;
}

.loading, .error-message {
  text-align: center;
  padding: 40px 0;
  color: #7f8c8d;
  font-size: 15px;
}

.error-message {
  color: #e74c3c;
}

@media (max-width: 1024px) {
  .rankings-grid {
    grid-template-columns: 1fr 1fr;
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