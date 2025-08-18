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
          <div class="profile-picture">
            <img :src="selectedAuthor.avatar || `https://via.placeholder.com/80x80/4f46e5/ffffff?text=${selectedAuthor.name.charAt(0)}`" :alt="selectedAuthor.name" class="avatar">
          </div>
                      <div class="profile-info">
              <h4 class="author-name">{{ selectedAuthor.name }}</h4>
              <div class="info-item">
                <span class="label">机构:</span>
                <span class="value">{{ selectedAuthor.institution }}</span>
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
                <span class="label">年龄:</span>
                <span class="value">{{ selectedAuthor.age }}岁</span>
              </div>
              <div class="info-item">
                <span class="label">总文章数:</span>
                <span class="value">{{ selectedAuthor.paperCount }}</span>
              </div>
              <div class="info-item">
                <span class="label">总引用量:</span>
                <span class="value">{{ selectedAuthor.citationCount }}</span>
              </div>
              <div class="info-item">
                <span class="label">合作数量:</span>
                <span class="value">{{ selectedAuthor.collaborationCount }}</span>
              </div>
              <div class="info-item">
                <span class="label">合作深度:</span>
                <span class="value">{{ selectedAuthor.collaborationDepth }}</span>
              </div>
              <div class="info-item">
                <span class="label">合作广度:</span>
                <span class="value">{{ selectedAuthor.collaborationBreadth }}</span>
              </div>
            </div>
        </div>
      </div>
      
      <!-- 默认显示提示 -->
      <div class="author-profile-card" v-else>
        <h3 class="card-title">作者简介</h3>
        <div class="profile-content">
          <div class="profile-picture">
            <img src="https://via.placeholder.com/80x80/4f46e5/ffffff?text=?" alt="选择作者" class="avatar">
          </div>
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
<<<<<<< HEAD
import { onMounted, ref, watch } from 'vue'
import * as d3 from 'd3'

export default {
  name: 'NetworkView',
  setup() {
    const selectedAuthor = ref(null)
    const hoveredNode = ref(null)
    const tooltip = ref({ visible: false, x: 0, y: 0, text: '' })
    
    // 筛选相关数据
    const startYear = ref(2000)
    const endYear = ref(new Date().getFullYear())
    const selectedField = ref('')
    const selectedRegion = ref('')
    const filteredConnectionCount = ref(213)
    
    // 滑块拖拽相关
    const isDragging = ref(false)
    const dragType = ref('')
    
    // 年份范围
    const minYear = 2000
    const maxYear = new Date().getFullYear()
    
    // 计算年份百分比
    const startYearPercent = ref(0) // 2000年对应0%
    const endYearPercent = ref(100) // 当前年份对应100%
    
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

    onMounted(() => {
      // 初始化年份百分比
      initYearPercentages()
      
      renderNetworkGraph()
      
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
      const safeWidth = width - margin * 2
      const safeHeight = height - margin * 2

      // 模拟网络数据 - 包含作者信息，确保节点在安全区域内
      const nodes = Array.from({ length: 50 }, (_, i) => ({
        id: i,
        x: margin + Math.random() * safeWidth,
        y: margin + Math.random() * safeHeight,
        size: Math.random() * 6 + 3, // 稍微减小节点大小
        importance: Math.random(),
        name: `Author ${i + 1}`,
        institution: `Institution ${Math.floor(Math.random() * 10) + 1}`,
        age: Math.floor(Math.random() * 40) + 25, // 25-65岁
        ranking: Math.floor(Math.random() * 100) + 1, // 1-100名
        score: (Math.random() * 100).toFixed(1), // 0-100分
        paperCount: Math.floor(Math.random() * 100) + 10,
        citationCount: Math.floor(Math.random() * 5000) + 100,
        collaborationCount: (Math.random() * 2).toFixed(3) + 'MS/T',
        collaborationDepth: (Math.random() * 2).toFixed(4) + 'MS/T',
        collaborationBreadth: (Math.random() * 2).toFixed(3) + 'MS/T',
        avatar: null
      }))

      const links = Array.from({ length: 80 }, () => ({
        source: Math.floor(Math.random() * nodes.length),
        target: Math.floor(Math.random() * nodes.length)
      }))

      // 绘制连接线
      const linkElements = svg.selectAll('line')
        .data(links)
        .enter()
        .append('line')
        .attr('class', 'link')
        .attr('x1', d => nodes[d.source].x)
        .attr('y1', d => nodes[d.source].y)
        .attr('x2', d => nodes[d.target].x)
        .attr('y2', d => nodes[d.target].y)
        .attr('stroke', 'rgba(139, 92, 246, 0.3)')
        .attr('stroke-width', 1)
        .attr('opacity', 0.6)

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
        .on('mouseover', function(event, d) {
          // 高亮当前节点
          d3.select(this).select('circle')
            .attr('r', d.size * 1.5)
            .style('filter', 'drop-shadow(0 0 12px rgba(139, 92, 246, 0.8))')
          
          // 高亮相关连接线 - 增强发光效果
          linkElements
            .filter(link => link.source.id === d.id || link.target.id === d.id)
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
        .on('mousemove', function(event, d) {
          // 更新工具提示位置
          tooltip.value.x = event.pageX + 10
          tooltip.value.y = event.pageY - 10
        })
        .on('mouseout', function(event, d) {
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
        .on('click', function(event, d) {
          // 点击选择作者
          selectedAuthor.value = d
        })

      // 创建工具提示
      const tooltipDiv = d3.select('body').append('div')
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

      // 监听工具提示状态变化
      watch(tooltip, (newTooltip) => {
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
    }

    // 应用筛选方法
    const applyFilters = () => {
      // 这里可以添加实际的筛选逻辑
      console.log('应用筛选:', {
        startYear: startYear.value,
        endYear: endYear.value,
        field: selectedField.value,
        region: selectedRegion.value
      })
      
      // 模拟筛选后的连接数量变化
      const baseCount = 213
      let filteredCount = baseCount
      
      // 根据年份范围筛选
      const yearRange = endYear.value - startYear.value
      if (yearRange < 5) {
        filteredCount = Math.floor(filteredCount * 0.6) // 年份范围小，数据量少
      } else if (yearRange < 10) {
        filteredCount = Math.floor(filteredCount * 0.8) // 年份范围中等
      }
      
      if (selectedField.value) filteredCount = Math.floor(filteredCount * 0.8)
      if (selectedRegion.value) filteredCount = Math.floor(filteredCount * 0.7)
      
      filteredConnectionCount.value = filteredCount
      
      // 重新渲染网络图（这里可以添加实际的筛选逻辑）
      renderNetworkGraph()
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

    return {
      selectedAuthor,
      hoveredNode,
      tooltip,
      startYear,
      endYear,
      selectedField,
      selectedRegion,
      filteredConnectionCount,
      startYearPercent,
      endYearPercent,
      applyFilters,
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
  max-width: 1120px;
  margin: 0 auto;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 32px;
  align-items: start;
  margin-top: 32px;
}

/* 网络容器 */
.network-container {
  position: relative;
  overflow: hidden;
  height: 600px;
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
  overflow: hidden;
  border: 2px solid rgba(139, 92, 246, 0.3);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-info {
  width: 100%;
}

.author-name {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px;
  text-align: center;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
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
  
  .filter-btn {
    margin-left: 0;
    margin-top: 8px;
  }
  
  .main-content {
    padding: 24px 16px;
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
=======
import { onMounted } from 'vue'
import * as d3 from 'd3'

export default {
  setup() {
    onMounted(() => {
      // 这里将添加D3.js网络图实现
      console.log('Network view mounted')
    })
  }
}
</script>
>>>>>>> a0fb820d53d87b0009869acd394b0a9bcbce725b
