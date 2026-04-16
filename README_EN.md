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

## 🔄 Version History
- **[Apr 2026] User Authentication & Permission Management:** Implemented frontend user registration, login, and logout functionalities, alongside a dedicated administrator page and new data download access controls.
- **[Apr 2026] Visualization & Interaction Upgrades:** Introduced a hot topic word cloud; significantly beautified the co-authorship network graph and improved its interactions; removed the geographic heatmap to focus on core graph analysis; optimized carousel features and full-site search capabilities.
- **[Apr 2026] AI Scholar Assistant & Card Export:** Integrated an intelligent conversational agent based on the Aliyun Bailian standard API. Integrated `html2canvas` and `jspdf` to enable one-click exports of high-definition Scholar Cards and posters.
- **[Apr 2026] Academic Hotspots Integration:** The homepage carousel now directly syncs with academic hotspots, keeping the platform updated with the latest technological trends.
- **[Mar 2026] Full-stack Overhaul:** Complete redesign of the frontend UI featuring a modern dark-tech aesthetic, along with a robust Spring Boot backend tailored for large-scale scholar data aggregation and high-concurrency queries.

## 🎯 Milestones

- [x] **v1.0.0** Core Framework: Established the Spring Boot data processing foundation and Vue 3 frontend initialization.
- [x] **v1.0.5** Network Visualization: Integrated D3.js to render million-node force-directed graphs for scholarly networks.
- [x] **v1.1.0** Homepage & Visual Revamp: Synchronized academic hotspots for the homepage top carousel rendering.
- [x] **v1.2.0** AI Copilot & Smart Scholar Cards: Introduced a global AI chat floating window with drag-to-resize support and immersive dark UI.
- [x] **v1.3.0** Permission Management & Deep Visualization: Added user and admin login systems, introduced hot topic word clouds, and enabled deep network interactions.

## 🎉 Features
Luminary-Nav adopts an efficient, multi-tiered architecture that separates computationally intensive graph analysis from real-time data serving:

- **User Permission System:** Comprehensive registration, login, and logout workflows, distinguishing between regular users and administrators, with fine-grained download permission controls.
- **Intelligent Scholar Discovery:** Identifies academic "leaders" (high overall impact) and high-potential "rising stars" (high growth potential) based on multi-dimensional evaluation algorithms.
- **Interactive Co-authorship Networks:** Renders high-performance force-directed graphs via D3.js to visually map complex, multi-hop collaborations between authors and institutions.
- **Hotspot Word Cloud Insights:** Real-time extraction and display of academic trending keywords via interactive word clouds, helping users quickly grasp research trends.
- **AI-Powered Insights:** Features an embedded AI Copilot supporting natural language queries of the academic network, and automatically generating beautifully designed, downloadable scholar posters.
- **Dynamic Hotspot Tracking:** Curates a stream of academic advancements, effectively bridging the information gap in cutting-edge research tracking.

## 🚀 Installation & Setup

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

### Frontend Configuration
Navigate to the frontend directory, install dependencies, and start the development server.
```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies (including newly added d3, echarts, html2canvas, jspdf, etc.)
npm install

# Start the dev server
npm run dev
```

### Backend Configuration (Spring Boot)
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

## 💡 Usage Examples
1. **Browse Homepage Hotspots**: Open the platform homepage; the carousel will automatically display the latest academic hotspots, with a trending word cloud presented below.
2. **Account Registration & Login**: Click "Login" in the top right corner to register an account, or use the administrator credentials (`administrator` / `123456`) to experience full access privileges.
3. **Scholar Query & Analysis**: Enter a scholar's name in the search bar to view their co-authorship network graph and details. You can export the detail page as a high-definition academic poster with one click.
4. **AI Chat Interaction**: Click the floating AI assistant icon and input natural language questions, such as "Please analyze the main research directions of this scholar."

## 📖 API Documentation
Examples of core backend RESTful APIs:
- `GET /api/rankings/search` - Fuzzy search for scholars (supports pagination by name and institution)
- `GET /api/rankings/top10` - Retrieve the top 10 scholars by total score
- `GET /api/rankings/hot-orgs` - Retrieve the top 50 most active institutions
- `GET /api/rankings/institutions/top10` - Retrieve the top 10 institutions by comprehensive strength
- `GET /api/rankings/rising-stars` - Retrieve high-potential rising stars (5-20 papers with high scores)
- `GET /api/rankings/leaders` - Retrieve a paginated list of domain leaders

## 🤝 Contributing
We welcome contributions from the community!
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 📄 License
This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.