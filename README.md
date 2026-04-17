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

## 🔄 版本更新 (Version History)
- **[2026年4月] 用户认证与权限管理：** 前端实现了用户注册、登录、退出功能以及管理员专属页面，并新增了数据下载权限控制。
- **[2026年4月] 可视化与交互体验升级：** 新增热点词云展示；合著网络图大幅美化与交互完善；移除了地理热力图页面以聚焦核心图分析；优化轮播图功能及全站前端搜索功能。
- **[2026年4月] AI 学术助手与名片导出：** 基于阿里云百炼标准 API 集成智能对话助手。引入 `html2canvas` 与 `jspdf` 实现高清学者名片/画报一键导出功能。
- **[2026年4月] 测试套件：** 引入完整的测试套件，包含导航搜索黑盒测试（Python+pytest）与核心算法白盒测试（Java+Jacoco覆盖率），详情见 [测试说明文档](tests/README.md)。
- **[2026年4月] 学术热点集成：** 首页轮播图现已与“黄大年茶思屋”学术热点直连同步，实时跟进生成式 AI、Mooncake 式解耦推理等前沿技术动态。
- **[2026年3月] 全栈重构：** 前端 UI 全面焕新，采用现代暗黑科技风格；后端基于 Spring Boot 深度优化，专为海量学者数据聚合与高并发查询设计。

## 🎯 项目里程碑 (Milestones)

- [x] **v1.0.0** 核心骨架搭建：Spring Boot 数据处理底座与 Vue 3 前端初始化。
- [x] **v1.0.5** 网络可视化：接入 D3.js，完成百万级节点学者的力导向图（Force-Directed Graph）渲染。
- [x] **v1.1.0** 首页内容与视觉重构：同步学术头条，实现首页顶部轮播图大图渲染。
- [x] **v1.2.0** AI Copilot 与学者名片引擎：引入全局 AI 对话悬浮窗，支持拖拽缩放与沉浸式暗色 UI。
- [x] **v1.3.0** 权限管理与深度可视化：新增用户与管理员登录体系，实现热点词云展示与深度网络交互。
- [x] **v1.4.0** 测试与质量保证：新增完整的黑盒/白盒测试演示套件及自动化代码覆盖率报告，并补充软件维护计划。

## 🎉 功能特性 (Features)
Luminary-Nav 采用高效的多层架构设计，将计算密集型的图分析与实时数据服务彻底解耦：

- **用户权限系统：** 完整的注册、登录、退出体系，区分普通用户与管理员，提供细粒度的下载权限控制。
- **智能学者发现：** 内置多维评估算法，精准识别学术“领军者”（综合影响力高）与“潜力新星”（具备高成长潜力）。
- **交互式合著网络：** 基于 D3.js 渲染高性能力导向图，直观映射学者与机构间复杂的多跳合作关系。
- **热点词云洞察：** 实时提炼并展示学术热点关键词词云，帮助快速把握研究趋势。
- **AI 驱动洞察：** 内置 AI 学术副驾驶，支持自然语言查询学术网络，自动生成精美可导出的学者名片画报。
- **动态热点追踪：** 平台持续推送精选学术前沿动态，有效弥合科研追踪之间的信息差。

## 🚀 安装配置说明 (Installation)

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

### 前端配置
定位前端目录，安装依赖并启动。
```bash
# 定位前端目录
cd frontend

# 安装依赖（包含新增的 d3, echarts, html2canvas, jspdf 等）
npm install

# npm 启动
npm run dev
```

### 后端配置 (Spring Boot)
后端代码位于 `main` 分支根目录。为便于快速原型验证，默认使用内嵌数据库（H2/SQLite）。

```bash
# 定位后端目录
cd Luminary-Nav-work

# 终端配置 AI API Key（运行 AI 对话功能必需，阿里云百炼平台支持）
$env:BAILIAN_API_KEY="sk-XXXXXXXXXXXXXXXXXXXX"

# 编译并启动后端服务
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

## 💡 使用示例 (Usage Examples)
1. **浏览首页热点**：打开平台首页，轮播图会自动展示最新学术热点，下方呈现热点词云。
2. **账号注册与登录**：点击右上角“登录”进行账号注册或使用管理员账号（`administrator`/`123456`）体验完整权限。
3. **学者查询与分析**：在搜索框输入学者姓名，查看其合著网络图与详情；支持一键将详情页面导出为高清学术画报。
4. **AI 对话交互**：点击悬浮的 AI 助手图标，输入自然语言问题，如“请分析某位学者的主要研究方向”。

## 📖 API文档 (API Documentation)
后端核心 RESTful API 示例：
- `GET /api/rankings/search` - 模糊搜索学者（支持按姓名和机构分页查询）
- `GET /api/rankings/top10` - 获取总分排名前10的顶尖学者
- `GET /api/rankings/hot-orgs` - 获取活跃度最高的前50个机构
- `GET /api/rankings/institutions/top10` - 获取机构综合实力排名前10列表
- `GET /api/rankings/rising-stars` - 获取处于成长期的潜力新星
- `GET /api/rankings/leaders` - 分页获取领域领军人物列表

## 🤝 贡献指南 (Contributing)
我们欢迎任何形式的贡献！
1. Fork 本仓库。
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)。
3. 提交您的更改 (`git commit -m 'Add some AmazingFeature'`)。
4. 将您的更改推送到分支 (`git push origin feature/AmazingFeature`)。
5. 开启一个 Pull Request。

## 📄 许可证 (License)
本项目基于 Apache License 2.0 许可协议开源。详情请参阅 [LICENSE](LICENSE) 文件。