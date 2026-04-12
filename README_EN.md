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
- **[Aug 2025] Full-stack Overhaul:** Complete redesign of the frontend UI featuring a modern dark-tech aesthetic, along with a robust Spring Boot backend tailored for large-scale scholar data aggregation.

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

## 🚀 Quick Start

### Prerequisites
- Node.js 18.x or higher
- Java 17 or higher
- Maven 3.6+
- Valid **Aliyun Bailian API Key** (required for AI chat)

### 1. Backend (Spring Boot)
The backend is located in the `main` branch root. It uses an embedded database (H2/SQLite) for rapid prototyping.

```bash
# Clone the repository (main branch)
git clone https://github.com/your-repo/Luminary-Nav.git
cd Luminary-Nav

# Set up your AI API Key
export BAILIAN_API_KEY="sk-your-actual-api-key-here"

# Build and run the backend
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```
*The backend server will start on `http://localhost:8080`.*

### 2. Frontend (Vue 3 + Vite)
The frontend code resides in the `frontend` directory.

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```
*The frontend will be available at `http://localhost:5173`.*

## 🤝 Contributing
We welcome contributions from the community! Please read our `CONTRIBUTING.md` before submitting pull requests.

If you encounter any bugs or have feature requests, please open an [Issue](../../issues).

## 📄 License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.
