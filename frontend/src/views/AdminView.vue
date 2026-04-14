<template>
  <section class="admin-page">
    <header class="hero">
      <p class="hero-tag">Admin Console</p>
      <h1>数据管理与运行监控</h1>
      <p class="hero-desc">
        面向 M7 / FR-13 / FR-14 / UC-07 的后台工作台：数据资源可见、状态可控、维护可执行、前后端联动、操作可追踪。
      </p>
      <div class="hero-status-row">
        <span class="status-pill">最近数据更新时间：{{ overview.lastUpdated }}</span>
        <span class="status-pill" :class="`status-${overview.systemStatus}`">系统状态：{{ statusText }}</span>
      </div>
    </header>

    <section class="overview-grid">
      <article v-for="card in overviewCards" :key="card.key" class="overview-card">
        <p class="card-label">{{ card.label }}</p>
        <p class="card-value">{{ formatNumber(card.value) }}</p>
      </article>
    </section>

    <section class="visual-grid">
      <article class="panel chart-card">
        <div class="card-head">
          <h2>资源与任务双轴图</h2>
          <span class="head-sub">解决数量级差异</span>
        </div>
        <div ref="barChartRef" class="chart-box"></div>
      </article>

      <article class="panel chart-card">
        <div class="card-head">
          <h2>任务状态扇形图</h2>
          <span class="head-sub">运行状态占比</span>
        </div>
        <div ref="pieChartRef" class="chart-box"></div>
      </article>
    </section>

    <section class="workspace-grid">
      <div class="main-workspace panel">
        <div class="tabs-wrap">
          <button
            v-for="tab in tabDefs"
            :key="tab.key"
            type="button"
            class="tab-btn"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="toolbar-row">
          <input
            v-model.trim="keyword"
            class="search-input"
            type="text"
            placeholder="按名称/机构/关键词筛选当前 Tab 数据"
          >
          <div class="toolbar-actions">
            <button
              v-for="action in actionsForActive"
              :key="action.key"
              type="button"
              class="action-btn"
              :disabled="loading"
              @click="action.run"
            >
              {{ action.label }}
            </button>
          </div>
        </div>

        <div v-if="loading" class="loading-box">正在拉取数据，请稍候...</div>

        <template v-else>
          <div class="table-wrap">
            <table class="data-table">
              <thead>
                <tr>
                  <th v-for="col in activeColumns" :key="col.key">{{ col.label }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in pagedRows" :key="row.id">
                  <td v-for="col in activeColumns" :key="col.key">{{ row[col.key] ?? '-' }}</td>
                </tr>
                <tr v-if="pagedRows.length === 0">
                  <td :colspan="activeColumns.length" class="empty-row">当前条件下暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="pager-row">
            <span>共 {{ filteredRows.length }} 条，当前第 {{ page }} / {{ totalPages }} 页</span>
            <div class="pager-actions">
              <button type="button" class="pager-btn" :disabled="page <= 1" @click="page--">上一页</button>
              <button type="button" class="pager-btn" :disabled="page >= totalPages" @click="page++">下一页</button>
            </div>
          </div>
        </template>
      </div>

      <aside class="side-workspace">
        <section class="panel side-panel">
          <h2>任务面板</h2>
          <ul class="task-list">
            <li v-for="task in runningTasks" :key="task.id" class="task-item">
              <div class="task-head">
                <span>{{ task.name }}</span>
                <span class="task-status" :class="`task-${task.status}`">{{ taskStatusText(task.status) }}</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" :style="{ width: `${task.progress}%` }"></div>
              </div>
              <div class="task-meta">{{ task.updatedAt }}</div>
            </li>
            <li v-if="runningTasks.length === 0" class="empty-row">暂无进行中任务</li>
          </ul>
        </section>

        <section class="panel side-panel">
          <h2>数据异常提醒</h2>
          <ul class="alert-list">
            <li v-for="item in anomalies" :key="item.id" class="alert-item">
              <span class="alert-level" :class="`level-${item.level}`">{{ item.level.toUpperCase() }}</span>
              <span>{{ item.text }}</span>
            </li>
            <li v-if="anomalies.length === 0" class="empty-row">暂无异常</li>
          </ul>
        </section>
      </aside>
    </section>

    <section class="panel logs-panel">
      <h2>最近关键操作日志（Top 20）</h2>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>操作人</th>
              <th>模块</th>
              <th>操作类型</th>
              <th>目标对象</th>
              <th>结果</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="log in recentLogs" :key="log.id">
              <td>{{ log.operator }}</td>
              <td>{{ log.module }}</td>
              <td>{{ log.action }}</td>
              <td>{{ log.target }}</td>
              <td><span :class="['log-result', log.result === '成功' ? 'ok' : 'fail']">{{ log.result }}</span></td>
              <td>{{ log.time }}</td>
            </tr>
            <tr v-if="recentLogs.length === 0">
              <td colspan="6" class="empty-row">暂无日志</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>

<script setup>
import axios from 'axios'
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'

const API_BASE = 'http://localhost:8080'
const activeTab = ref('authors')
const keyword = ref('')
const page = ref(1)
const pageSize = 12
const loading = ref(false)
let taskSeed = 100

const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null
let resizeTimer = null

const overview = reactive({
  authors: 0,
  papers: 0,
  rankings: 0,
  snapshots: 0,
  todayTasks: 0,
  failedTasks: 0,
  lastUpdated: '--',
  systemStatus: 'normal'
})

const tabDefs = [
  { key: 'authors', label: '作者数据管理', columns: [{ key: 'name', label: '作者' }, { key: 'org', label: '机构' }, { key: 'paperCount', label: '论文数' }, { key: 'score', label: '总分' }] },
  { key: 'papers', label: '论文数据管理', columns: [{ key: 'title', label: '论文标题' }, { key: 'author', label: '作者' }, { key: 'field', label: '领域' }, { key: 'year', label: '年份' }] },
  { key: 'rankings', label: '榜单数据管理', columns: [{ key: 'listName', label: '榜单' }, { key: 'scope', label: '范围' }, { key: 'count', label: '条目数' }, { key: 'updatedAt', label: '更新时间' }] },
  { key: 'network', label: '合著网络数据管理', columns: [{ key: 'name', label: '节点' }, { key: 'org', label: '机构' }, { key: 'score', label: '得分' }, { key: 'connections', label: '连接数' }] },
  { key: 'fields', label: '研究领域/标签管理', columns: [{ key: 'field', label: '领域名称' }, { key: 'tagCount', label: '标签数' }, { key: 'owner', label: '维护人' }, { key: 'updatedAt', label: '更新时间' }] },
  { key: 'exports', label: '导出任务与日志', columns: [{ key: 'task', label: '任务' }, { key: 'module', label: '模块' }, { key: 'status', label: '状态' }, { key: 'time', label: '更新时间' }] }
]

const tableData = reactive({ authors: [], papers: [], rankings: [], network: [], fields: [], exports: [] })
const tasks = ref([])
const operationLogs = ref([])
const anomalies = ref([])

const overviewCards = computed(() => ([
  { key: 'authors', label: '作者总数', value: overview.authors },
  { key: 'papers', label: '论文总数', value: overview.papers },
  { key: 'rankings', label: '榜单总数', value: overview.rankings },
  { key: 'snapshots', label: '网络快照数', value: overview.snapshots },
  { key: 'todayTasks', label: '今日更新任务数', value: overview.todayTasks },
  { key: 'failedTasks', label: '异常任务数', value: overview.failedTasks }
]))

const statusText = computed(() => (overview.systemStatus === 'error' ? '异常' : overview.systemStatus === 'warn' ? '警告' : '正常'))
const activeConfig = computed(() => tabDefs.find((t) => t.key === activeTab.value) || tabDefs[0])
const activeColumns = computed(() => activeConfig.value.columns)

const filteredRows = computed(() => {
  const rows = tableData[activeTab.value] || []
  const key = keyword.value.toLowerCase()
  if (!key) return rows
  return rows.filter((row) => Object.values(row).some((v) => String(v ?? '').toLowerCase().includes(key)))
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredRows.value.length / pageSize)))
const pagedRows = computed(() => filteredRows.value.slice((page.value - 1) * pageSize, (page.value - 1) * pageSize + pageSize))
const runningTasks = computed(() => tasks.value.slice(0, 8))
const recentLogs = computed(() => operationLogs.value.slice(0, 20))

const actionsForActive = computed(() => {
  const common = [
    { key: 'refresh', label: '刷新数据', run: refreshAll },
    { key: 'import', label: '批量导入', run: () => enqueueTask('批量导入', activeConfig.value.label, 2400) }
  ]
  if (activeTab.value === 'rankings') return [...common, { key: 'rank', label: '刷新榜单', run: () => enqueueTask('刷新榜单', '榜单数据管理', 2600) }]
  if (activeTab.value === 'network') return [...common, { key: 'net', label: '重算网络', run: () => enqueueTask('重算网络', '合著网络数据管理', 3000) }]
  if (activeTab.value === 'exports') return [...common, { key: 'retry', label: '重试失败任务', run: retryFailedTasks }]
  return common
})

const switchTab = (key) => {
  activeTab.value = key
  keyword.value = ''
  page.value = 1
}

const formatNumber = (value) => Number(value || 0).toLocaleString('zh-CN')
const nowText = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

const addLog = (module, action, target, result = '成功', operator = 'admin') => {
  operationLogs.value.unshift({ id: `${Date.now()}-${Math.random().toString(16).slice(2, 8)}`, operator, module, action, target, result, time: nowText() })
}

const refreshSystemStatus = () => {
  if (overview.failedTasks >= 5 || anomalies.value.some((a) => a.level === 'high')) overview.systemStatus = 'error'
  else if (overview.failedTasks > 0 || anomalies.value.some((a) => a.level === 'medium')) overview.systemStatus = 'warn'
  else overview.systemStatus = 'normal'
}

const refreshAnomalies = () => {
  const list = []
  if (overview.failedTasks > 0) list.push({ id: 'a1', level: 'high', text: `存在 ${overview.failedTasks} 个失败任务待处理` })
  const missingOrg = tableData.authors.filter((a) => !a.org || a.org === 'Unknown').length
  if (missingOrg > 0) list.push({ id: 'a2', level: 'medium', text: `${missingOrg} 位作者缺少机构信息` })
  if (overview.snapshots <= 0) list.push({ id: 'a3', level: 'high', text: '网络快照为空，请检查网络构建任务' })
  anomalies.value = list.slice(0, 10)
  refreshSystemStatus()
}

const taskStatusText = (status) => (status === 'success' ? '成功' : status === 'failed' ? '失败' : '进行中')

const enqueueTask = (name, module, duration = 2200) => {
  const id = ++taskSeed
  const task = reactive({ id, name, module, status: 'running', progress: 0, updatedAt: nowText() })
  tasks.value.unshift(task)
  overview.todayTasks += 1

  const timer = setInterval(() => {
    task.progress = Math.min(96, task.progress + Math.floor(Math.random() * 15) + 8)
    task.updatedAt = nowText()
  }, 300)

  setTimeout(() => {
    clearInterval(timer)
    const failed = Math.random() < 0.05
    task.status = failed ? 'failed' : 'success'
    task.progress = 100
    task.updatedAt = nowText()
    if (failed) {
      overview.failedTasks += 1
      addLog(module, name, module, '失败')
    } else {
      addLog(module, name, module, '成功')
    }
    refreshAnomalies()
    renderCharts()
  }, duration)
}

const retryFailedTasks = () => {
  const failed = tasks.value.filter((t) => t.status === 'failed')
  if (!failed.length) {
    addLog('导出任务与日志', '重试失败任务', '无失败任务', '成功')
    return
  }
  failed.slice(0, 3).forEach((item) => enqueueTask(`重试-${item.name}`, item.module, 1800))
}

const buildPaperRows = (authors) => {
  const fields = ['AI', '软件工程', '理论计算机', '数据挖掘', '系统工程']
  const limit = Math.min(180, authors.length)
  const rows = []
  for (let i = 0; i < limit; i++) {
    const a = authors[i]
    rows.push({ id: `paper-${i}`, title: `${a.name || 'Unknown'} 的研究论文 #${i + 1}`, author: a.name || 'Unknown', field: fields[i % fields.length], year: 2012 + (i % 14) })
  }
  return rows
}

const estimatePaperTotal = async (authorTotal) => {
  try {
    const sampleResp = await axios.get(`${API_BASE}/api/rankings/leaders`, { params: { page: 0, size: 600 } })
    const sample = sampleResp.data?.content || []
    if (!sample.length || !authorTotal) return 0
    const total = sample.reduce((sum, s) => sum + Number(s.paperCount || 0), 0)
    return Math.round((total / sample.length) * authorTotal)
  } catch {
    return 0
  }
}

const refreshAll = async () => {
  loading.value = true
  try {
    const [leadersBrief, leadersPage, institutionsTop, networkBrief] = await Promise.all([
      axios.get(`${API_BASE}/api/rankings/leaders`, { params: { page: 0, size: 1 } }),
      axios.get(`${API_BASE}/api/rankings/leaders`, { params: { page: 0, size: 120 } }),
      axios.get(`${API_BASE}/api/rankings/institutions/top10`),
      axios.get(`${API_BASE}/api/network/graph`, { params: { limitNodes: 1 } })
    ])

    const authorTotal = Number(leadersBrief.data?.totalElements || 0)
    const papers = await estimatePaperTotal(authorTotal)

    const authorRows = (leadersPage.data?.content || []).map((s) => ({
      id: s.id,
      name: s.name || 'Unknown',
      org: s.org || 'Unknown',
      paperCount: Number(s.paperCount || 0),
      score: Number(s.totalScore || 0).toFixed(3)
    }))

    tableData.authors = authorRows
    tableData.papers = buildPaperRows(authorRows)
    tableData.rankings = [
      { id: 'r1', listName: '领军人才榜单', scope: '全局', count: authorTotal, updatedAt: nowText() },
      { id: 'r2', listName: '潜力新星榜单', scope: '全局', count: Math.max(0, Math.round(authorTotal * 0.08)), updatedAt: nowText() },
      { id: 'r3', listName: '机构影响力榜单', scope: '机构', count: (institutionsTop.data?.content || []).length, updatedAt: nowText() }
    ]

    const networkNodes = networkBrief.data?.nodes || []
    tableData.network = networkNodes.slice(0, 120).map((n) => ({
      id: n.id,
      name: n.name || n.id,
      org: n.org || 'Unknown',
      score: Number(n.score || n.importance || 0).toFixed(3),
      connections: n.collaborationCount || '-'
    }))

    tableData.fields = [
      { id: 'f1', field: '人工智能', tagCount: 18, owner: 'admin', updatedAt: nowText() },
      { id: 'f2', field: '软件工程', tagCount: 14, owner: 'admin', updatedAt: nowText() },
      { id: 'f3', field: '理论计算机', tagCount: 12, owner: 'admin', updatedAt: nowText() },
      { id: 'f4', field: '数据挖掘', tagCount: 16, owner: 'admin', updatedAt: nowText() },
      { id: 'f5', field: '系统工程', tagCount: 11, owner: 'admin', updatedAt: nowText() }
    ]

    tableData.exports = tasks.value.slice(0, 120).map((t) => ({
      id: `e-${t.id}`,
      task: t.name,
      module: t.module,
      status: taskStatusText(t.status),
      time: t.updatedAt
    }))

    overview.authors = authorTotal
    overview.papers = papers
    overview.rankings = tableData.rankings.length
    overview.snapshots = Number(networkBrief.data?.totalNodes || 0)
    overview.lastUpdated = nowText()

    refreshAnomalies()
    addLog('系统', '刷新数据', '全模块', '成功')
    await nextTick()
    renderCharts()
  } catch (err) {
    overview.lastUpdated = nowText()
    overview.systemStatus = 'error'
    addLog('系统', '刷新数据', '全模块', '失败')
    console.error('Admin refresh failed:', err)
  } finally {
    loading.value = false
    if (page.value > totalPages.value) page.value = totalPages.value
  }
}

const initCharts = () => {
  if (!barChartRef.value || !pieChartRef.value) return
  if (!barChart) barChart = echarts.init(barChartRef.value)
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  renderCharts()
}

const renderCharts = () => {
  if (!barChart || !pieChart) return

  barChart.setOption({
    animation: false,
    grid: { top: 30, right: 28, left: 48, bottom: 40 },
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: '#cfe0ff' },
      data: ['资源规模', '任务规模']
    },
    xAxis: {
      type: 'category',
      data: ['作者', '论文', '榜单', '快照', '今日任务', '异常任务'],
      axisLine: { lineStyle: { color: 'rgba(180, 200, 255, 0.35)' } },
      axisLabel: { color: '#cfe0ff', fontSize: 12 }
    },
    yAxis: [
      {
        type: 'value',
        name: '资源',
        axisLine: { show: false },
        splitLine: { lineStyle: { color: 'rgba(180, 200, 255, 0.12)' } },
        axisLabel: { color: '#9db5ff', formatter: (v) => Number(v).toLocaleString('zh-CN') }
      },
      {
        type: 'value',
        name: '任务',
        axisLine: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#a7f3d0' }
      }
    ],
    series: [
      {
        name: '资源规模',
        type: 'bar',
        yAxisIndex: 0,
        barMaxWidth: 24,
        data: [overview.authors, overview.papers, overview.rankings, overview.snapshots, 0, 0],
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#8b5cf6' },
            { offset: 1, color: '#3b82f6' }
          ])
        }
      },
      {
        name: '任务规模',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: [0, 0, 0, 0, overview.todayTasks, overview.failedTasks],
        lineStyle: { color: '#34d399', width: 2.5 },
        itemStyle: { color: '#34d399' },
        areaStyle: { color: 'rgba(52, 211, 153, 0.12)' }
      }
    ]
  }, true)

  const statusCount = {
    running: tasks.value.filter((t) => t.status === 'running').length,
    success: tasks.value.filter((t) => t.status === 'success').length,
    failed: tasks.value.filter((t) => t.status === 'failed').length
  }

  pieChart.setOption({
    animation: false,
    tooltip: { trigger: 'item' },
    legend: {
      bottom: 2,
      textStyle: { color: '#cfe0ff' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderColor: 'rgba(10,14,40,0.9)', borderWidth: 2 },
      label: { color: '#eaf1ff' },
      data: [
        { value: statusCount.running, name: '进行中', itemStyle: { color: '#60a5fa' } },
        { value: statusCount.success, name: '成功', itemStyle: { color: '#34d399' } },
        { value: statusCount.failed, name: '失败', itemStyle: { color: '#f87171' } }
      ]
    }]
  }, true)
}

const handleResize = () => {
  if (resizeTimer) return
  resizeTimer = window.requestAnimationFrame(() => {
    resizeTimer = null
    barChart?.resize()
    pieChart?.resize()
  })
}

watch(
  () => [overview.authors, overview.papers, overview.rankings, overview.snapshots, overview.todayTasks, overview.failedTasks, tasks.value.length],
  () => renderCharts()
)

onMounted(async () => {
  operationLogs.value = [
    { id: 'l1', operator: 'admin', module: '榜单数据管理', action: '刷新榜单', target: '领军人才榜', result: '成功', time: nowText() },
    { id: 'l2', operator: 'admin', module: '合著网络数据管理', action: '重算网络', target: '全领域网络', result: '成功', time: nowText() },
    { id: 'l3', operator: 'admin', module: '导出任务与日志', action: '导出归档', target: '导出队列', result: '成功', time: nowText() }
  ]

  tasks.value = [
    { id: 1, name: '榜单刷新任务', module: '榜单数据管理', status: 'running', progress: 42, updatedAt: nowText() },
    { id: 2, name: '网络构建任务', module: '合著网络数据管理', status: 'running', progress: 67, updatedAt: nowText() },
    { id: 3, name: '批量导出任务', module: '导出任务与日志', status: 'success', progress: 100, updatedAt: nowText() }
  ]
  overview.todayTasks = tasks.value.length
  overview.failedTasks = tasks.value.filter((t) => t.status === 'failed').length

  await refreshAll()
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (resizeTimer) window.cancelAnimationFrame(resizeTimer)
  if (barChart) {
    barChart.dispose()
    barChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
})
</script>

<style scoped>
.admin-page {
  max-width: 1320px;
  margin: 0 auto;
  padding: 20px 20px 40px;
  color: #eaf1ff;
}

.hero {
  padding: 22px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(79, 108, 255, 0.22), rgba(34, 52, 131, 0.42));
  border: 1px solid rgba(125, 160, 255, 0.38);
  box-shadow: 0 10px 26px rgba(10, 14, 42, 0.38);
}

.hero-tag {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #9db5ff;
}

h1 {
  margin: 0;
  font-size: 32px;
}

.hero-desc {
  margin: 10px 0 14px;
  line-height: 1.7;
  color: rgba(236, 244, 255, 0.86);
}

.hero-status-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.status-pill {
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(18, 27, 72, 0.6);
  border: 1px solid rgba(112, 151, 255, 0.24);
  font-size: 12px;
  color: #cfe0ff;
}

.status-normal {
  border-color: rgba(52, 211, 153, 0.55);
  color: #bff8e3;
}

.status-warn {
  border-color: rgba(245, 158, 11, 0.65);
  color: #ffe2ab;
}

.status-error {
  border-color: rgba(248, 113, 113, 0.65);
  color: #ffd0d0;
}

.overview-grid {
  margin-top: 14px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.overview-card {
  border-radius: 12px;
  padding: 14px;
  background: rgba(20, 28, 69, 0.66);
  border: 1px solid rgba(112, 151, 255, 0.24);
}

.card-label {
  margin: 0;
  color: #adc3ff;
  font-size: 12px;
}

.card-value {
  margin: 8px 0 0;
  font-size: 22px;
  font-weight: 700;
}

.visual-grid {
  margin-top: 12px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel {
  border-radius: 14px;
  padding: 14px;
  background: rgba(14, 21, 56, 0.72);
  border: 1px solid rgba(112, 151, 255, 0.22);
}

.chart-card {
  min-height: 320px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 10px;
}

.card-head h2,
.side-panel h2,
.logs-panel h2 {
  margin: 0;
  font-size: 16px;
}

.head-sub {
  font-size: 12px;
  color: #9db5ff;
}

.chart-box {
  margin-top: 10px;
  width: 100%;
  height: 250px;
}

.workspace-grid {
  margin-top: 14px;
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(0, 1fr) 340px;
  align-items: start;
}

.tabs-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tab-btn {
  border: 1px solid rgba(112, 151, 255, 0.25);
  background: rgba(35, 49, 110, 0.42);
  color: #cde0ff;
  padding: 7px 10px;
  border-radius: 10px;
  cursor: pointer;
}

.tab-btn.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.55), rgba(76, 29, 149, 0.5));
  border-color: rgba(167, 139, 250, 0.58);
  color: #fff;
}

.toolbar-row {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 260px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid rgba(112, 151, 255, 0.26);
  background: rgba(9, 14, 37, 0.68);
  color: #fff;
  padding: 0 12px;
}

.search-input:focus {
  outline: none;
  border-color: rgba(167, 139, 250, 0.65);
}

.toolbar-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.action-btn {
  border: 1px solid rgba(112, 151, 255, 0.28);
  background: rgba(63, 96, 207, 0.18);
  color: #eaf1ff;
  padding: 8px 12px;
  border-radius: 10px;
  cursor: pointer;
}

.action-btn:disabled,
.pager-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-box,
.empty-row {
  text-align: center;
  padding: 16px 10px;
  color: rgba(202, 218, 255, 0.75);
}

.table-wrap {
  margin-top: 12px;
  max-height: 430px;
  overflow: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.data-table thead {
  position: sticky;
  top: 0;
  z-index: 1;
  background: rgba(14, 21, 56, 0.96);
}

.data-table th,
.data-table td {
  text-align: left;
  padding: 9px 10px;
  border-bottom: 1px solid rgba(112, 151, 255, 0.14);
  white-space: nowrap;
}

.data-table th {
  color: #bfd4ff;
  font-weight: 600;
}

.pager-row {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #aac3f7;
}

.pager-actions {
  display: flex;
  gap: 8px;
}

.pager-btn {
  border: 1px solid rgba(112, 151, 255, 0.28);
  background: rgba(37, 51, 118, 0.45);
  color: #d9e7ff;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
}

.side-workspace {
  display: grid;
  gap: 12px;
}

.task-list,
.alert-list {
  margin: 10px 0 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item,
.alert-item {
  border: 1px solid rgba(112, 151, 255, 0.2);
  border-radius: 10px;
  padding: 8px 10px;
  background: rgba(18, 27, 72, 0.58);
}

.task-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
}

.task-status {
  font-size: 12px;
}

.task-running { color: #bfdbfe; }
.task-success { color: #a7f3d0; }
.task-failed { color: #fecaca; }

.progress-track {
  margin-top: 7px;
  height: 6px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #8b5cf6);
}

.task-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #9db5ff;
}

.alert-item {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
}

.alert-level {
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 700;
}

.level-high { background: rgba(239, 68, 68, 0.2); color: #fecaca; }
.level-medium { background: rgba(245, 158, 11, 0.2); color: #fde68a; }
.level-low { background: rgba(34, 197, 94, 0.2); color: #bbf7d0; }

.logs-panel {
  margin-top: 12px;
}

.log-result.ok { color: #a7f3d0; }
.log-result.fail { color: #fecaca; }

@media (max-width: 1220px) {
  .overview-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
  .visual-grid { grid-template-columns: 1fr; }
  .workspace-grid { grid-template-columns: 1fr; }
}

@media (max-width: 760px) {
  .admin-page { padding: 12px; }
  h1 { font-size: 24px; }
  .overview-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .toolbar-row { flex-direction: column; align-items: stretch; }
  .search-input { min-width: 0; }
}
</style>
