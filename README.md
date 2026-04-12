<p align="center">
  <a href="README.md">🇨🇳 中文</a> | <a href="README_EN.md">🇺🇸 English</a>
</p>

<div align="center">
  <img src="frontend/src/assets/logo.jpg" alt="Luminary-Nav Logo" width="200" height="auto" />
  <h1>Luminary-Nav</h1>
  <p><b>Luminary-Nav</b> 是一套专注于学术人才发现与合著网络分析的综合平台。<br/>基于大语言模型（LLM）与图计算，旨在发掘潜力新星、构建学术合作桥梁、追踪科研热点。</p>
</div>

<p align="center">
  <a href="https://github.com/vuejs/vue-next"><img src="https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg" alt="Vue"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.x-green.svg" alt="Spring Boot"></a>
  <a href="https://d3js.org/"><img src="https://img.shields.io/badge/D3.js-7.x-orange.svg" alt="D3.js"></a>
  <a href="https://bailian.console.aliyun.com/"><img src="https://img.shields.io/badge/AI-Aliyun_Bailian-blue.svg" alt="Aliyun Bailian"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-lightgrey.svg" alt="License"></a>
</p>

---

Luminary-Nav 是一套致力于学术人才发掘与合著网络深度分析的综合平台。通过集成 **阿里云百炼 API** 的大语言模型（LLM）能力与交互式网络可视化技术（D3.js），本平台助力科研机构无缝追踪学术前沿、发掘“潜力新星”，并深度剖析全球学术合作趋势。

## 🔄 版本更新
- **[2026年4月] AI 学术助手：** 基于阿里云百炼标准 API 集成智能对话助手。用户现可动态查询学者合著关系，并一键生成可下载的个性化学术名片。
- **[2026年4月] 学术热点集成：** 首页轮播图现已与“黄大年茶思屋”学术热点直连同步，实时跟进生成式 AI、Mooncake 式解耦推理等前沿技术动态。
- **[2026年3月] 全栈重构：** 前端 UI 全面焕新，采用现代暗黑科技风格；后端基于 Spring Boot 深度优化，专为海量学者数据聚合与高并发查询设计。

## 🎯 项目里程碑 (Milestones)

- [x] **v1.0.0** 核心骨架搭建：Spring Boot 数据处理底座与 Vue 3 前端初始化。
- [x] **v1.0.5** 网络可视化：接入 D3.js，完成百万级节点学者的力导向图（Force-Directed Graph）渲染。
- [x] **v1.1.0** 首页内容与视觉重构：
  - 同步“黄大年茶思屋”学术头条（量子计算、生成式 AI 等），实现首页顶部轮播图大图渲染。
  - 修复前端路由监听，使轮播图仅在主页展示，保证详情页的视觉纯净度。
- [x] **v1.2.0** AI Copilot 与学者名片引擎：
  - 引入全局 AI 对话悬浮窗，支持拖拽缩放与沉浸式暗色 UI。
  - 接入阿里云百炼大模型并开启实时联网检索功能，打破 AI 信息差。
  - 推出高颜值“学者名片”卡片，包含真实机构、职称、研究方向等字段，并支持基于 `html2canvas` 的高清 PNG 导出。

## 🎉 核心概览
Luminary-Nav 采用高效的多层架构设计，将计算密集型的图分析与实时数据服务彻底解耦：

- **智能学者发现：** 内置多维评估算法，精准识别学术“领军者”（综合影响力高）与“潜力新星”（影响力增速快但基数小，具备高成长潜力）。
- **交互式合著网络：** 基于 D3.js 渲染高性能力导向图，直观映射学者与机构间复杂的多跳合作关系。
- **AI 驱动洞察：** 内置 AI 学术副驾驶（接入阿里云百炼），支持自然语言查询学术网络，并通过 `html2canvas` 自动生成精美可导出的学者名片。
- **动态热点追踪：** 平台持续推送精选学术前沿动态，有效弥合尖端研究（如 Kimi、KVCache 核心大模型系统等）与实际科研追踪之间的信息差。

## 🚀 快速开始

### 环境要求
- Node.js 18.x 或更高版本
- Java 17 或更高版本
- Maven 3.6+
- 有效的 **阿里云百炼 API Key**（用于 AI 对话功能）

### 仓库准备
```bash
# 克隆仓库（main 分支）
git clone https://github.com/your-repo/Luminary-Nav-work.git
```

### 前端
定位前端目录，npm 启动。
```bash
# 定位前端目录
cd frontend

# npm 启动
npm run dev
```

### 后端 (Spring Boot)
后端代码位于 `main` 分支根目录。为便于快速原型验证，默认使用内嵌数据库（H2/SQLite）。

```bash
# 定位后端目录
cd Luminary-Nav-work

# 终端配置 AI API Key（运行 AI 对话功能必需，阿里云百炼平台支持）
$env:BAILIAN_API_KEY="sk-XXXXXXXXXXXXXXXXXXXX"

# 编译并启动后端服务
./mvnw clean install -DskipTests
./mvnw spring-boot:run
