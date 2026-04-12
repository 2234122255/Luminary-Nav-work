<p align="center">
  <a href="README.md">🇨🇳 中文</a> | <a href="README_EN.md">🇺🇸 English</a>
</p>

<div align="center">
  <img src="frontend/src/assets/logo.jpg" alt="Luminary-Nav Logo" width="200" height="auto" />
  <h1>Luminary-Nav</h1>
  <p><b>Luminary-Nav</b> is a comprehensive platform dedicated to the discovery of academic talent and the analysis of co-authorship networks.<br/>Empowered by Large Language Models (LLMs) and graph computing, it aims to unearth rising stars, build academic bridges, and track cutting-edge research trends.</p>
</div>

<p align="center">
  <a href="https://github.com/vuejs/vue-next"><img src="https://img.shields.io/badge/Vue.js-3.x-brightgreen.svg" alt="Vue"></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring_Boot-3.x-green.svg" alt="Spring Boot"></a>
  <a href="https://d3js.org/"><img src="https://img.shields.io/badge/D3.js-7.x-orange.svg" alt="D3.js"></a>
  <a href="https://bailian.console.aliyun.com/"><img src="https://img.shields.io/badge/AI-Aliyun_Bailian-blue.svg" alt="Aliyun Bailian"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-lightgrey.svg" alt="License"></a>
</p>

---

Luminary-Nav leverages the **Aliyun Bailian API** and interactive network visualization (D3.js) to enable institutions to seamlessly track academic hotspots, unearth "rising stars," and analyze global scholarly collaboration trends.

## 🔄 Updates
- **[Apr 2026] AI Scholar Assistant:** Integrated an intelligent conversational agent based on the Aliyun Bailian standard API. Users can now query co-authorship relationships dynamically and generate downloadable, stylized Scholar Business Cards.
- **[Apr 2026] Academic Hotspots Integration:** The homepage carousel now directly syncs with the "黄大年茶思屋" academic hotspots, keeping the platform updated with the latest technological trends.
- **[Mar 2026] Full-stack Overhaul:** Complete redesign of the frontend UI featuring a modern dark-tech aesthetic, along with a robust Spring Boot backend tailored for large-scale scholar data aggregation.

## 🎯 Milestones

- [x] **v1.0.0** Core Framework: Established the Spring Boot data processing foundation and Vue 3 frontend initialization.
- [x] **v1.0.5** Network Visualization: Integrated D3.js to render million-node force-directed graphs for scholarly networks.
- [x] **v1.1.0** Homepage & Visual Revamp:
  - Synchronized "黄大年茶思屋" academic hotspots (Quantum Computing, GenAI, etc.) for the homepage top carousel.
  - Fixed frontend routing to ensure the carousel displays exclusively on the homepage.
- [x] **v1.2.0** AI Copilot & Smart Scholar Cards:
  - Introduced a global AI chat floating window with drag-to-resize support and an immersive dark UI.
  - Integrated the Aliyun Bailian LLM with real-time web search capabilities to bridge the AI information gap.
  - Launched high-aesthetic "Scholar Cards" featuring real-world institutions, titles, and research directions, supporting HD PNG export via `html2canvas`.

## 🎉 Overview
Luminary-Nav adopts an efficient, multi-tiered architecture that separates computationally intensive graph analysis from real-time data serving:

- **Intelligent Scholar Discovery:** Identifies academic "leaders" and high-potential "rising stars" based on multi-dimensional evaluation algorithms.
- **Interactive Co-authorship Networks:** Renders force-directed graphs to visually map complex, multi-hop collaborations between authors and institutions.
- **AI-Powered Insights:** Features an embedded AI Copilot that can instantly compile a scholar's stats into an elegantly designed, downloadable business card using real-world data.
- **Dynamic Hotspot Tracking:** Curates a stream of academic advancements, bridging the gap between cutting-edge research and practical tracking.

以下是为您精准翻译的英文版本，已严格保留原有 Markdown 结构、代码块格式与技术细节，并补全了遗漏的代码块闭合符号，符合国际开源项目规范：

## 🚀 Quick Start

### Prerequisites
- Node.js 18.x or higher
- Java 17 or higher
- Maven 3.6+
- Valid **Aliyun Bailian API Key** (required for AI chat features)

### Repository Setup
```bash
# Clone the repository (main branch)
git clone https://github.com/your-repo/Luminary-Nav-work.git
```

### Frontend
Navigate to the frontend directory and start the development server.
```bash
# Navigate to the frontend directory
cd frontend

# Start the dev server
npm run dev
```

### Backend (Spring Boot)
The backend code resides in the root directory of the `main` branch. For rapid prototyping, it defaults to an embedded database (H2/SQLite).

```bash
# Navigate to the project root
cd Luminary-Nav-work

# Set the AI API Key in terminal (Required for AI chat, supported by Aliyun Bailian)
# Note: Use `export BAILIAN_API_KEY="..."` on macOS/Linux
$env:BAILIAN_API_KEY="sk-XXXXXXXXXXXXXXXXXXXX"

# Build and start the backend service
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

## 🤝 Contributing
We welcome contributions from the community! Please read our `CONTRIBUTING.md` before submitting pull requests.

If you encounter any bugs or have feature requests, please open an [Issue](../../issues).

## 📄 License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
