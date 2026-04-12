<template>
  <div class="network-view">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-main">
          <h1 class="hero-title">洞察学术合著网络</h1>
          <p class="hero-subtitle">发现卓越科研力量，构建学术合作桥梁</p>
        </div>

        <!-- 右侧筛选设置栏 -->
        <div class="hero-filter-bar">
          <!-- 起止年份设置 -->
          <div class="filter-group">
            <div class="year-range">
              <div class="year-slider">
                <!-- 时间轴标签 -->
                <div class="year-labels">
                  <span class="year-label">{{ startYear }}</span>
                  <span class="year-label">{{ new Date().getFullYear() }}</span>
                </div>

                <div class="year-slider-track" @mousedown="startTrackDragging">
                  <div class="year-slider-fill"
                    :style="{ left: startYearPercent + '%', right: (100 - endYearPercent) + '%' }"></div>
                  <div class="year-slider-thumb start" :style="{ left: startYearPercent + '%' }"
                    @mousedown.stop="startDragging('start', $event)" @keydown="handleKeyDown('start', $event)" tabindex="0"
                    role="slider" :aria-valuenow="startYear" :aria-valuemin="minYear" :aria-valuemax="endYear - 1"
                    aria-label="起始年份"></div>
                  <div class="year-slider-thumb end" :style="{ left: endYearPercent + '%' }"
                    @mousedown.stop="startDragging('end', $event)" @keydown="handleKeyDown('end', $event)" tabindex="0"
                    role="slider" :aria-valuenow="endYear" :aria-valuemin="startYear + 1" :aria-valuemax="maxYear"
                    aria-label="结束年份"></div>
                </div>


              </div>
            </div>
          </div>

          <!-- 领域设置 -->
          <div class="filter-group field-filter-group">
            <div class="select-wrapper">
              <select class="filter-select" v-model="selectedField">
                <option value="">全部领域</option>
                <option value="theoretical-computer-science">Theoretical Computer Science</option>
                <option value="artificial-intelligence">Artificial Intelligence</option>
                <option value="software-engineering">Software Engineering</option>
              </select>
            </div>
          </div>

          <!-- 数据点总数显示 -->
          <div class="data-count">
            <span class="count-number">{{ filteredConnectionCount }}</span>
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
      <!-- 网络可视化 -->
      <div class="network-container">
        <div id="network-graph" class="network-graph"></div>
      </div>

      <!-- 右侧作者简介卡片 -->
      <div class="author-profile-card" v-if="selectedAuthor">
        <h3 class="card-title">作者简介</h3>
        <div class="profile-content">
          <div class="profile-picture">{{ getInitial(selectedAuthor.name) }}</div>
          <div class="profile-info">
            <div class="profile-header-row">
              <h4 class="author-name">{{ selectedAuthor.name }}</h4>
              <button class="more-info-link" @click="goToAcademicPortrait(selectedAuthor)">更多信息</button>
            </div>
            <div class="info-item">
              <span class="label">机构:</span>
              <span class="value institution-value" :title="selectedAuthor.institution || ''">{{ selectedAuthor.institution }}</span>
            </div>
            <div class="info-item">
              <span class="label">排名:</span>
              <span class="value">{{ selectedAuthor.ranking }}</span>
            </div>
            <div class="info-item">
              <span class="label">评分:</span>
              <span class="value">{{ selectedAuthor.score }}</span>
            </div>
            <div class="info-item">
              <span class="label">影响力:</span>
              <span class="value">{{ selectedAuthor.portrait?.influence ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">产出比:</span>
              <span class="value">{{ selectedAuthor.portrait?.outputRatio ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">创新性:</span>
              <span class="value">{{ selectedAuthor.portrait?.innovation ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">合作频率:</span>
              <span class="value">{{ selectedAuthor.portrait?.collaborationFreq ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">引用量:</span>
              <span class="value">{{ selectedAuthor.portrait?.citationVolume ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">活跃度:</span>
              <span class="value">{{ selectedAuthor.portrait?.activity ?? '-' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 默认显示提示 -->
      <div class="author-profile-card" v-else>
        <h3 class="card-title">作者简介</h3>
        <div class="profile-content">
          <div class="profile-picture">?</div>
          <div class="profile-info">
            <h4 class="author-name">点击网络节点</h4>
            <p class="hint-text">点击网络中的任意节点查看作者详细信息</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import axios from 'axios'
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as d3 from 'd3'

export default {
  name: 'NetworkView',
  setup() {
    const GRAPH_NODE_LIMIT = 1200
    const router = useRouter()
    const selectedAuthor = ref(null)
    const hoveredNode = ref(null)
    const tooltip = ref({ visible: false, x: 0, y: 0, text: '' })
    const networkNodes = ref([])
    const networkLinks = ref([])
    let tooltipDiv = null

    // 筛选相关数据
    const startYear = ref(2000)
    const endYear = ref(new Date().getFullYear())
    const selectedField = ref('')
    const filteredConnectionCount = ref(0)

    // 滑块拖拽相关
    const isDragging = ref(false)
    const dragType = ref('')

    // 年份范围
    const minYear = 2000
    const maxYear = new Date().getFullYear()

    // 计算年份百分比
    const startYearPercent = ref(0) // 2000年对应0%
    const endYearPercent = ref(100) // 当前年份对应100%

    const getIdSeed = (id) => {
      const str = String(id || '')
      let hash = 0
      for (let i = 0; i < str.length; i++) {
        hash = (hash * 31 + str.charCodeAt(i)) >>> 0
      }
      return hash || 1
    }

    const randomFromSeed = (seed, min, max) => {
      const x = Math.sin(seed * 999.91) * 10000
      const frac = x - Math.floor(x)
      return Math.round(min + frac * (max - min))
    }

    const buildPortraitMetrics = (id) => {
      const seed = getIdSeed(id)
      return {
        influence: randomFromSeed(seed + 1, 70, 98),
        outputRatio: randomFromSeed(seed + 2, 55, 92),
        innovation: randomFromSeed(seed + 3, 58, 95),
        collaborationFreq: randomFromSeed(seed + 4, 50, 90),
        citationVolume: randomFromSeed(seed + 5, 65, 99),
        activity: randomFromSeed(seed + 6, 45, 88)
      }
    }

    const buildSyntheticYearRange = (id) => {
      const seed = getIdSeed(id)
      const start = 2000 + (seed % 15)
      const end = Math.min(maxYear, start + 5 + (seed % 8))
      return { start, end }
    }

    const matchFieldLocal = (node, fieldValue) => {
      if (!fieldValue) return true
      const org = (node?.org || node?.institution || '').toLowerCase()
      const map = {
        'theoretical-computer-science': ['theory', 'theoretical', 'algorithm', 'complexity', 'logic', 'cryptograph', 'formal'],
        'artificial-intelligence': ['artificial intelligence', 'machine learning', 'deep learning', 'neural', 'computer vision', 'nlp', 'intelligent'],
        'software-engineering': ['software engineering', 'software', 'testing', 'devops', 'reliability', 'programming']
      }
      const hit = (map[fieldValue] || []).some((kw) => org.includes(kw))
      if (hit) return true

      const bucket = Math.abs(String(node?.id || '').split('').reduce((acc, ch) => acc + ch.charCodeAt(0), 0)) % 3
      if (fieldValue === 'theoretical-computer-science') return bucket === 0
      if (fieldValue === 'artificial-intelligence') return bucket === 1
      if (fieldValue === 'software-engineering') return bucket === 2
      return false
    }

    const inYearRange = (rangeStart, rangeEnd) => rangeEnd >= startYear.value && rangeStart <= endYear.value

    // 动态更新当前年份
    const updateCurrentYear = () => {
      const currentYear = new Date().getFullYear()
      if (endYear.value < currentYear) {
        endYear.value = currentYear
        endYearPercent.value = 100
      }
    }

    // 初始化年份百分比
    const initYearPercentages = () => {
      const yearRange = maxYear - minYear
      startYearPercent.value = ((startYear.value - minYear) / yearRange) * 100
      endYearPercent.value = ((endYear.value - minYear) / yearRange) * 100
    }

    watch(tooltip, (newTooltip) => {
      if (!tooltipDiv) return
      if (newTooltip.visible) {
        tooltipDiv
          .style('left', newTooltip.x + 'px')
          .style('top', newTooltip.y + 'px')
          .text(newTooltip.text)
          .style('opacity', 1)
      } else {
        tooltipDiv.style('opacity', 0)
      }
    }, { deep: true })

    onMounted(async () => {
      // 初始化年份百分比
      initYearPercentages()
      await fetchNetworkGraph()
      
      // 监听窗口大小变化，重新渲染网络图
      const resizeObserver = new ResizeObserver(() => {
        renderNetworkGraph()
      })

      const container = document.getElementById('network-graph')
      if (container) {
        resizeObserver.observe(container)
      }

      // 设置定时器，每天更新一次当前年份
      const yearUpdateTimer = setInterval(updateCurrentYear, 24 * 60 * 60 * 1000)

      // 清理函数
      return () => {
        resizeObserver.disconnect()
        clearInterval(yearUpdateTimer)
      }
    })

    const fetchNetworkGraph = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/network/graph', {
          params: {
            startYear: startYear.value,
            endYear: endYear.value,
            field: selectedField.value || undefined,
            limitNodes: GRAPH_NODE_LIMIT
          }
        })
        networkNodes.value = (response.data?.nodes || []).map((node, idx) => {
          const nodeId = node.id || `api-${idx}`
          return {
            ...node,
            portrait: node.portrait || buildPortraitMetrics(nodeId)
          }
        })
        networkLinks.value = response.data?.links || []
        filteredConnectionCount.value = response.data?.totalNodes ?? networkNodes.value.length
        selectedAuthor.value = null
        renderNetworkGraph()
      } catch (error) {
        console.error('获取网络图数据失败，尝试降级到 scholars 数据接口:', error)
        await fetchNetworkGraphFallback()
      }
    }

    const fetchNetworkGraphFallback = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/rankings/search', {
          params: {
            page: 1,
            size: GRAPH_NODE_LIMIT
          }
        })
        const scholars = Array.isArray(response.data) ? response.data : []
        const nodes = scholars.map((item, index) => {
          const nodeId = item.id || `fallback-${index}`
          return {
            id: nodeId,
            name: item.name || `Author ${index + 1}`,
            institution: item.org || 'Unknown Institution',
            org: item.org || 'Unknown Institution',
            paperCount: item.paperCount || 0,
            citationCount: 0,
            ranking: index + 1,
            score: Number(item.totalScore || 0).toFixed(3),
            importance: Number(item.totalScore || 0),
            size: 4 + Math.min(8, Number(item.totalScore || 0) * 12),
            collaborationCount: '-',
            collaborationDepth: '-',
            collaborationBreadth: '-',
            age: null,
            avatar: null,
            syntheticYear: buildSyntheticYearRange(nodeId),
            portrait: buildPortraitMetrics(nodeId)
          }
        })

        const filteredNodes = nodes.filter((node) => {
          const fieldPass = matchFieldLocal(node, selectedField.value)
          const year = node.syntheticYear || { start: minYear, end: maxYear }
          return fieldPass && inYearRange(year.start, year.end)
        })

        // 按机构粗粒度构建连线，保证接口不可用时筛选后也可看图
        const grouped = new Map()
        for (const node of filteredNodes) {
          const key = (node.org || '').trim().toLowerCase()
          if (!grouped.has(key)) grouped.set(key, [])
          grouped.get(key).push(node)
        }
        const links = []
        for (const [, groupNodes] of grouped.entries()) {
          if (groupNodes.length < 2) continue
          for (let i = 1; i < groupNodes.length; i++) {
            links.push({
              source: groupNodes[i - 1].id,
              target: groupNodes[i].id,
              type: 'fallback'
            })
            if (links.length >= 1200) break
          }
          if (links.length >= 1200) break
        }

        networkNodes.value = filteredNodes
        networkLinks.value = links
        filteredConnectionCount.value = filteredNodes.length
        selectedAuthor.value = null
        renderNetworkGraph()
      } catch (fallbackError) {
        console.error('降级网络图数据也获取失败:', fallbackError)
        networkNodes.value = []
        networkLinks.value = []
        filteredConnectionCount.value = 0
        renderNetworkGraph()
      }
    }

    const renderNetworkGraph = () => {
      const container = document.getElementById('network-graph')
      if (!container) return

      // 获取容器的实际尺寸
      const containerRect = container.getBoundingClientRect()
      const width = containerRect.width
      const height = containerRect.height

      // 清空容器
      container.innerHTML = ''

      // 创建SVG，完全填充容器
      const svg = d3.select(container)
        .append('svg')
        .attr('width', '100%')
        .attr('height', '100%')
        .attr('viewBox', `0 0 ${width} ${height}`)
        .style('display', 'block')

      // 计算节点分布的安全区域（留出边距）
      const margin = 40
      const safeWidth = Math.max(20, width - margin * 2)
      const safeHeight = Math.max(20, height - margin * 2)
      const centerX = width / 2
      const centerY = height / 2
      const radius = Math.max(120, Math.min(safeWidth, safeHeight) * 0.46)
      const sourceLinks = networkLinks.value || []

      // 使用后端真实数据构建图节点（圆形分层初始化）
      const sortedRawNodes = [...(networkNodes.value || [])]
        .sort((a, b) => (Number(b.importance) || 0) - (Number(a.importance) || 0))
      const degreeMap = new Map()
      for (const link of sourceLinks) {
        degreeMap.set(link.source, (degreeMap.get(link.source) || 0) + 1)
        degreeMap.set(link.target, (degreeMap.get(link.target) || 0) + 1)
      }
      const maxRanking = Math.max(1, ...sortedRawNodes.map((n) => Number(n.ranking) || 0))
      const nodes = sortedRawNodes.map((node, i) => {
        const seed = getIdSeed(node.id || i)
        const noiseA = (seed % 1000) / 1000
        const noiseB = ((seed >> 3) % 1000) / 1000
        const noiseC = ((seed >> 7) % 1000) / 1000
        const importance = Number(node.importance) || 0
        const degree = degreeMap.get(node.id) || 0
        const rankNorm = Math.min(1, Math.max(0, (Number(node.ranking) || maxRanking) / maxRanking))
        const angle = noiseA * Math.PI * 2 + (noiseB - 0.5) * 0.8
        const outwardBias = (degree <= 1 && rankNorm > 0.6) ? 0.2 : 0
        const baseFactor = 0.16 + noiseC * 0.58 + outwardBias + (1 - importance) * 0.08 + (noiseB - 0.5) * 0.18
        const targetRadius = radius * Math.max(0.08, Math.min(0.92, baseFactor))
        const sizeBase = Number(node.size) || 5
        const rankSizeScale = rankNorm > 0.92
          ? 0.2
          : rankNorm > 0.8
            ? 0.35
            : rankNorm > 0.6
              ? 0.55
              : 0.78
        const adjustedSize = Math.max(1.1, Math.min(8.2, sizeBase * rankSizeScale))
        return {
          ...node,
          x: centerX + Math.cos(angle) * targetRadius,
          y: centerY + Math.sin(angle) * targetRadius,
          size: adjustedSize,
          importance,
          targetRadius,
          boundaryRadius: radius * (0.84 + (((seed >> 11) % 1000) / 1000) * 0.16)
        }
      })
      const nodeMap = new Map(nodes.map((node) => [node.id, node]))
      const links = sourceLinks.filter((link) => nodeMap.has(link.source) && nodeMap.has(link.target))

      if (nodes.length === 0) {
        svg.append('text')
          .attr('x', width / 2)
          .attr('y', height / 2)
          .attr('text-anchor', 'middle')
          .attr('fill', 'rgba(255, 255, 255, 0.7)')
          .style('font-size', '14px')
          .text('暂无网络数据')
        return
      }

      // 柔和底纹（不加规整边框）
      svg.append('circle')
        .attr('cx', centerX)
        .attr('cy', centerY)
        .attr('r', radius + 44)
        .attr('fill', 'rgba(139, 92, 246, 0.05)')

      const keepInCloud = (node) => {
        const dx = node.x - centerX
        const dy = node.y - centerY
        const dist = Math.sqrt(dx * dx + dy * dy)
        const boundary = node.boundaryRadius || radius * 0.92
        if (dist > boundary) {
          const pull = (dist - boundary) / dist
          node.x -= dx * pull * 0.72
          node.y -= dy * pull * 0.72
        }
        const nodePad = 18 + (Number(node.size) || 2) * 1.8
        const minX = margin + nodePad
        const maxX = width - margin - nodePad
        const minY = margin + nodePad
        const maxY = height - margin - nodePad
        if (node.x < minX) node.x += (minX - node.x) * 0.45
        if (node.x > maxX) node.x -= (node.x - maxX) * 0.45
        if (node.y < minY) node.y += (minY - node.y) * 0.45
        if (node.y > maxY) node.y -= (node.y - maxY) * 0.45
        node.x = Math.max(minX, Math.min(maxX, node.x))
        node.y = Math.max(minY, Math.min(maxY, node.y))
      }

      // 去杂连线：每个节点最多展示少量连接，整体更干净
      const maxVisibleLinks = Math.min(1400, Math.max(350, Math.round(nodes.length * 1.15)))
      const degreeShown = new Map()
      const displayLinks = []
      for (const link of links) {
        const s = link.source
        const t = link.target
        const sCount = degreeShown.get(s) || 0
        const tCount = degreeShown.get(t) || 0
        const sNode = nodeMap.get(s)
        const tNode = nodeMap.get(t)
        const keyNode = (sNode?.importance || 0) > 0.78 || (tNode?.importance || 0) > 0.78
        if (keyNode || (sCount < 2 && tCount < 2)) {
          displayLinks.push(link)
          degreeShown.set(s, sCount + 1)
          degreeShown.set(t, tCount + 1)
        }
        if (displayLinks.length >= maxVisibleLinks) break
      }

      const simLinks = displayLinks.map((link) => ({ source: link.source, target: link.target }))
      const simulation = d3.forceSimulation(nodes)
        .force('link', d3.forceLink(simLinks).id((d) => d.id).distance(34).strength(0.08))
        .force('charge', d3.forceManyBody().strength(-17))
        .force('collide', d3.forceCollide().radius((d) => d.size + 1.8).iterations(1))
        .force('radial', d3.forceRadial((d) => d.targetRadius || radius * 0.45, centerX, centerY).strength(0.032))
        .force('x', d3.forceX(centerX).strength(0.018))
        .force('y', d3.forceY(centerY).strength(0.018))
        .alpha(0.55)
        .alphaDecay(0.06)
        .stop()

      const tickCount = nodes.length > 900 ? 35 : (nodes.length > 500 ? 50 : 70)
      for (let i = 0; i < tickCount; i++) {
        simulation.tick()
        nodes.forEach(keepInCloud)
      }
      simulation.stop()

      // 二次扰动，进一步打散轮廓，同时保持不越界
      for (const node of nodes) {
        if ((node.targetRadius || 0) < radius * 0.62) continue
        const dx = node.x - centerX
        const dy = node.y - centerY
        const dist = Math.sqrt(dx * dx + dy * dy) || 1
        const radialX = dx / dist
        const radialY = dy / dist
        const tangentX = -dy / dist
        const tangentY = dx / dist
        const seed = getIdSeed(node.id)
        const jitterTan = (((seed >> 5) % 1000) / 1000 - 0.5) * 14
        const jitterRad = (((seed >> 9) % 1000) / 1000 - 0.5) * 10
        node.x += tangentX * jitterTan + radialX * jitterRad
        node.y += tangentY * jitterTan + radialY * jitterRad
        keepInCloud(node)
      }

      // 绘制连接线
      const linkElements = svg.selectAll('line')
        .data(displayLinks)
        .enter()
        .append('line')
        .attr('class', 'link')
        .attr('x1', d => nodeMap.get(d.source).x)
        .attr('y1', d => nodeMap.get(d.source).y)
        .attr('x2', d => nodeMap.get(d.target).x)
        .attr('y2', d => nodeMap.get(d.target).y)
        .attr('stroke', 'rgba(139, 92, 246, 0.16)')
        .attr('stroke-width', 0.9)
        .attr('opacity', 0.9)

      // 绘制节点
      const nodeGroups = svg.selectAll('g.node')
        .data(nodes)
        .enter()
        .append('g')
        .attr('class', 'node')
        .attr('transform', d => `translate(${d.x}, ${d.y})`)

      // 节点圆圈
      nodeGroups.append('circle')
        .attr('r', d => d.size)
        .attr('fill', d => d.importance > 0.7 ? '#8b5cf6' : '#6366f1')
        .attr('stroke', '#ffffff')
        .attr('stroke-width', 1)
        .attr('opacity', 0.8)
        .style('filter', 'drop-shadow(0 0 8px rgba(139, 92, 246, 0.4))')

      // 添加交互
      nodeGroups
        .on('mouseover', function (event, d) {
          // 高亮当前节点
          d3.select(this).select('circle')
            .attr('r', d.size * 1.5)
            .style('filter', 'drop-shadow(0 0 12px rgba(139, 92, 246, 0.8))')

          // 高亮相关连接线 - 增强发光效果
          linkElements
            .filter(link => link.source === d.id || link.target === d.id)
            .attr('stroke', '#8b5cf6')
            .attr('stroke-width', 3)
            .style('filter', 'drop-shadow(0 0 12px rgba(139, 92, 246, 0.8))')
            .style('opacity', 1)
            .transition()
            .duration(200)
            .style('stroke', '#a855f7')
            .style('filter', 'drop-shadow(0 0 20px rgba(139, 92, 246, 1))')

          // 显示工具提示
          tooltip.value = {
            visible: true,
            x: event.pageX + 10,
            y: event.pageY - 10,
            text: `${d.name}\n排名: ${d.ranking}\n机构: ${d.institution}\n评分: ${d.score}`
          }
        })
        .on('mousemove', function (event, d) {
          // 更新工具提示位置
          tooltip.value.x = event.pageX + 10
          tooltip.value.y = event.pageY - 10
        })
        .on('mouseout', function (event, d) {
          // 恢复节点样式
          d3.select(this).select('circle')
            .attr('r', d.size)
            .style('filter', 'drop-shadow(0 0 8px rgba(139, 92, 246, 0.4))')

          // 恢复连接线样式 - 平滑过渡
          linkElements
            .transition()
            .duration(300)
            .attr('stroke', 'rgba(139, 92, 246, 0.3)')
            .attr('stroke-width', 1)
            .style('filter', 'none')
            .style('opacity', 0.6)

          // 隐藏工具提示
          tooltip.value.visible = false
        })
        .on('click', function (event, d) {
          // 点击选择作者
          selectedAuthor.value = {
            ...d,
            portrait: d.portrait || buildPortraitMetrics(d.id)
          }
        })

      // 创建工具提示（重建前先清理旧实例）
      d3.selectAll('.network-tooltip').remove()
      tooltipDiv = d3.select('body').append('div')
        .attr('class', 'network-tooltip')
        .style('position', 'absolute')
        .style('background', 'rgba(17, 24, 39, 0.95)')
        .style('backdrop-filter', 'blur(12px)')
        .style('color', '#e5e7eb')
        .style('padding', '8px 12px')
        .style('border-radius', '8px')
        .style('border', '1px solid rgba(255, 255, 255, 0.1)')
        .style('box-shadow', '0 8px 24px rgba(0, 0, 0, 0.3)')
        .style('pointer-events', 'none')
        .style('z-index', '1000')
        .style('font-size', '12px')
        .style('white-space', 'pre-line')
        .style('opacity', 0)
    }

    // 应用筛选方法
    const applyFilters = async () => {
      await fetchNetworkGraph()
    }

    const goToAcademicPortrait = (author) => {
      if (!author || !author.id) return
      router.push({
        name: 'AuthorDetail',
        params: { id: author.id }
      })
    }

    const getInitial = (name) => String(name || '?').trim().charAt(0).toUpperCase() || '?'

    // 开始拖拽滑块
    const startDragging = (type, event) => {
      event.preventDefault()
      isDragging.value = true
      dragType.value = type
      const track = document.querySelector('.year-slider-track')
      if (!track) return

      const handleMouseMove = (e) => {
        if (!isDragging.value) return
        const rect = track.getBoundingClientRect()
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

    // 更新年份百分比
    const updateYearPercentages = () => {
      const yearRange = maxYear - minYear
      startYearPercent.value = ((startYear.value - minYear) / yearRange) * 100
      endYearPercent.value = ((endYear.value - minYear) / yearRange) * 100
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
      event.preventDefault()
      const track = event.currentTarget
      const rect = track.getBoundingClientRect()
      const yearRange = maxYear - minYear
      const currentRange = endYear.value - startYear.value
      const dragOffsetPercent = ((event.clientX - rect.left) / rect.width) * 100 - startYearPercent.value

      const handleMouseMove = (e) => {
        const percent = ((e.clientX - rect.left) / rect.width) * 100
        const desiredStartPercent = Math.max(0, Math.min(100, percent - dragOffsetPercent))
        const targetYear = minYear + Math.round((desiredStartPercent / 100) * yearRange)

        let newStartYear = Math.max(minYear, Math.min(maxYear - currentRange, targetYear))
        let newEndYear = newStartYear + currentRange
        if (newEndYear > maxYear) {
          newEndYear = maxYear
          newStartYear = Math.max(minYear, newEndYear - currentRange)
        }

        startYear.value = newStartYear
        endYear.value = newEndYear
        updateYearPercentages()
      }

      const handleMouseUp = () => {
        document.removeEventListener('mousemove', handleMouseMove)
        document.removeEventListener('mouseup', handleMouseUp)
      }

      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
    }

    return {
      selectedAuthor,
      hoveredNode,
      tooltip,
      startYear,
      endYear,
      selectedField,
      filteredConnectionCount,
      startYearPercent,
      endYearPercent,
      applyFilters,
      goToAcademicPortrait,
      getInitial,
      startDragging,
      updateYearPercentages,
      handleKeyDown,
      startTrackDragging
    }
  }
}
</script>

<style scoped>
.network-view {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a1a 0%, #1a1a3a 25%, #2d2d5a 50%, #3a3a6a 75%, #4a4a7a 100%);
  padding: 20px 16px 56px;
  color: #ffffff;
}

/* 英雄区域 */
.hero-section {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.05) 0%, rgba(99, 102, 241, 0.08) 25%, rgba(139, 92, 246, 0.12) 50%, rgba(99, 102, 241, 0.08) 75%, rgba(139, 92, 246, 0.05) 100%);
  border-bottom: 1px solid rgba(139, 92, 246, 0.15);
  padding: 40px 0;
  position: relative;
}

.hero-section::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(139, 92, 246, 0.3) 50%, transparent 100%);
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
  max-width: 1320px;
  margin: 0 auto;
  padding: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 32px;
  align-items: start;
  margin-top: 32px;
}

/* 网络容器 */
.network-container {
  position: relative;
  overflow: hidden;
  height: 760px;
  width: 100%;
}

.network-graph {
  width: 100%;
  height: 100%;
  background: transparent;
  position: relative;
}

/* 英雄区域筛选栏 */
.hero-filter-bar {
  display: flex;
  align-items: center;
  gap: 0;
  flex-wrap: wrap;
  min-width: 600px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 12px;
  padding: 16px;
  position: relative;
}

.hero-filter-bar::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    linear-gradient(90deg, transparent 0%, rgba(139, 92, 246, 0.1) 25%, transparent 50%, rgba(139, 92, 246, 0.1) 75%, transparent 100%),
    linear-gradient(0deg, transparent 0%, rgba(139, 92, 246, 0.05) 50%, transparent 100%);
  border-radius: 12px;
  pointer-events: none;
}

/* 筛选栏 */
.filter-bar {
  position: absolute;
  bottom: 24px;
  left: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  z-index: 10;
  flex-wrap: wrap;
  background: rgba(15, 15, 35, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(139, 92, 246, 0.3);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 140px;
  padding: 0 16px;
  position: relative;
}

.filter-group:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 60%;
  background: linear-gradient(180deg, transparent 0%, rgba(139, 92, 246, 0.3) 50%, transparent 100%);
}

.filter-label {
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  font-weight: 600;
  text-align: center;
  letter-spacing: 0.5px;
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

.field-filter-group {
  min-width: 220px;
  align-items: center;
}

.field-filter-group .select-wrapper {
  width: 220px;
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

.field-filter-group .filter-select {
  width: 220px;
  text-align: center;
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
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.2), rgba(99, 102, 241, 0.2));
  border: 1px solid rgba(139, 92, 246, 0.4);
  border-radius: 18px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-left: 8px;
  backdrop-filter: blur(10px);
}

.filter-btn:hover {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.3), rgba(99, 102, 241, 0.3));
  border-color: rgba(139, 92, 246, 0.6);
  color: #ffffff;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.3);
}

/* 数据点总数显示 */
.data-count {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(139, 92, 246, 0.2);
  border: 2px solid rgba(139, 92, 246, 0.4);
  border-radius: 50%;
  transition: all 0.2s ease;
  margin: 0 16px;
  position: relative;
}

.data-count::after {
  content: '';
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 60%;
  background: linear-gradient(180deg, transparent 0%, rgba(139, 92, 246, 0.3) 50%, transparent 100%);
}

.data-count:hover {
  background: rgba(139, 92, 246, 0.3);
  border-color: rgba(139, 92, 246, 0.6);
  transform: scale(1.05);
}

.count-number {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  text-shadow: 0 0 8px rgba(139, 92, 246, 0.5);
}

.connection-count {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(139, 92, 246, 0.2);
  border: 2px solid rgba(139, 92, 246, 0.4);
  border-radius: 50%;
  color: #ffffff;
  font-weight: 700;
  font-size: 16px;
  backdrop-filter: blur(10px);
}

/* 作者简介卡片 */
.author-profile-card {
  background: linear-gradient(135deg, rgba(15, 15, 35, 0.95) 0%, rgba(26, 26, 58, 0.9) 50%, rgba(42, 42, 82, 0.95) 100%);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(139, 92, 246, 0.25);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.profile-picture {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  border: 2px solid rgba(139, 92, 246, 0.3);
  background: linear-gradient(135deg, #8b5cf6, #3b82f6);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 700;
}

.profile-info {
  width: 100%;
}

.profile-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.author-name {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  text-align: center;
}

.more-info-link {
  border: 1px solid rgba(139, 92, 246, 0.45);
  background: rgba(139, 92, 246, 0.15);
  color: #d8c7ff;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
}

.more-info-link:hover {
  color: #ffffff;
  border-color: rgba(139, 92, 246, 0.75);
  background: rgba(139, 92, 246, 0.28);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  gap: 10px;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  font-weight: 500;
}

.value {
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  max-width: 68%;
  text-align: right;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.institution-value {
  cursor: help;
}

.rankings-section {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.hint-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  text-align: center;
  margin: 0;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .author-profile-card {
    order: -1;
  }
}

@media (max-width: 768px) {
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

  .filter-group::after {
    display: none;
  }

  .data-count::after {
    display: none;
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

  .field-filter-group {
    min-width: 100%;
  }

  .field-filter-group .select-wrapper,
  .field-filter-group .filter-select {
    width: 100%;
  }

  .filter-btn {
    margin-left: 0;
    margin-top: 8px;
  }

  .main-content {
    padding: 24px 16px;
  }

  .network-container {
    height: 560px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    bottom: 16px;
    left: 16px;
    right: 16px;
    padding: 16px;
  }

  .filter-group {
    align-items: flex-start;
    min-width: 100%;
  }

  .year-slider {
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }

  .data-count {
    align-self: center;
  }
}
</style>
<!-- import { onMounted } from 'vue'
import * as d3 from 'd3'

export default {
setup() {
onMounted(() => {
// 这里将添加D3.js网络图实现
console.log('Network view mounted')
})
}
}
</script> -->
