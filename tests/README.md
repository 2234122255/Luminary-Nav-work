# 测试演示套件 (Test Suite)

本文档是本项目测试演示套件的完整说明，旨在为 PPT 展示环节提供支持。包含黑盒测试（API 接口级）与白盒测试（核心算法级），并包含软件维护计划。

## 1. 目录结构
- `blackbox/`: 包含针对导航搜索输入框的黑盒测试代码 (Python + pytest)。
- `whitebox/`: 包含白盒测试的说明和覆盖率报告。实际的 Java 测试代码位于标准 Maven 目录 `src/test/java/com/example/coauthoranalysis/service/` 中。

## 2. 环境配置与依赖安装

### 黑盒测试环境 (Python)
黑盒测试使用 Python 的 `pytest` 和 `requests` 库来验证后端 API。
**前置条件**: 
1. 确保已安装 Python 3.8+。
2. 确保 Spring Boot 后端项目已在本地启动（默认端口 8080）。

**安装依赖**:
```bash
cd tests/blackbox
pip install pytest requests
```

### 白盒测试环境 (Java)
白盒测试使用 JUnit 5 和 Jacoco（用于生成代码覆盖率报告）。
**前置条件**:
1. 确保已安装 JDK 17 和 Maven。

## 3. 测试运行命令

### 运行黑盒测试
在 `tests/blackbox` 目录下运行：
```bash
pytest test_search.py -v
```

### 运行白盒测试与生成覆盖率报告
在项目根目录下运行以下 Maven 命令：
```bash
./mvnw test jacoco:report
```
运行完成后，覆盖率报告将生成在 `target/site/jacoco/index.html`。可以在浏览器中打开该文件查看语句覆盖率和分支覆盖率。

---

## 4. 黑盒测试设计 (导航搜索输入框)
**测试目标**: `/api/rankings/search` 接口
**测试方法**: 等价类划分与边界值分析
**测试用例覆盖**:
1. **有效输入 (正常搜索)**: 存在的姓名、机构名称。
2. **无效输入 (不存在的数据)**: 数据库中没有的特殊组合。
3. **边界值测试**: 分页边界（如 page=1, size=1；page=999, size=20）。
4. **特殊字符测试**: 包含 SQL 注入字符或标点符号。
5. **超长输入测试**: 超过常规长度的字符串。
6. **性能验证**: 验证接口响应时间是否在 500ms 内。

---

## 5. 白盒测试设计 (核心算法)
**测试目标**: `ScholarService.searchScholars` 方法
**环形复杂度 (Cyclomatic Complexity)**: 
- 基础复杂度: 1
- `name == null || name.trim().isEmpty()`: +2
- `name.toLowerCase().contains(...)`: +1
- `org == null || org.trim().isEmpty()`: +2
- `org.toLowerCase().contains(...)`: +1
- `nameMatch && orgMatch`: +1
- 总环形复杂度为 **8**。
**基本路径覆盖**:
设计了 5 个测试用例，覆盖了所有独立路径（姓名为空、机构为空、全为空、全匹配、部分匹配等），详见 `ScholarServiceTest.java`。

---

## 6. 软件维护文档

### 6.1 未来演化方向
1. **功能扩展**: 引入图神经网络（GNN）以提供更深度的学者合作推荐，并在前端增加 3D 知识图谱的可视化模式。
2. **性能优化**: 对 `scholars.json` 数据加载进行优化，引入 Redis 缓存层，减少文件 I/O，并提升前端海量节点渲染（如 Canvas/WebGL）的帧率。
3. **用户体验改进**: 增加用户自定义仪表盘、多维度对比视图，并适配移动端响应式布局。

### 6.2 维护计划
- **版本发布周期**: 每两周进行一次小版本迭代（修复 Bug、优化体验），每季度进行一次大版本发布（核心功能升级）。
- **问题响应时间**: 严重问题（如接口崩溃）2 小时内响应并提供热修复；一般问题 24 小时内跟进，48 小时内给出解决方案。
- **代码重构策略**: 
  - 坚持“童子军规则”（Leave it better than you found it）。
  - 定期使用 SonarQube 进行静态代码分析，保持技术债务在可控范围内。
  - 对于复杂度超过 15 的方法强制进行拆分。

### 6.3 当前状态与可持续发展建议
**当前状态**: 项目已实现核心的学者数据加载、检索、关系图谱生成功能，前后端分离架构清晰。
**可持续发展建议**:
1. **完善 CI/CD**: 接入 GitHub Actions，实现自动化测试与部署。
2. **数据源动态化**: 将目前基于静态 JSON/CSV 文件的加载方式逐步演进为关系型数据库（如 PostgreSQL）与图数据库（如 Neo4j）结合的混合存储方案，以支持更大规模的数据演进。

---

## 7. 常见问题解答 (FAQ)

**Q1: 运行黑盒测试时提示 `Connection refused` 怎么解决？**
A1: 请确保 Spring Boot 后端服务已经启动，并且监听在 `http://localhost:8080`。

**Q2: Jacoco 覆盖率报告在哪里查看？**
A2: 在项目根目录下执行 `mvn test jacoco:report` 后，使用浏览器打开 `target/site/jacoco/index.html` 即可查看图形化覆盖率报告。

**Q3: 如何修改后端服务的端口？**
A3: 可以在 `src/main/resources/application.properties` 中添加 `server.port=新端口号`，并相应修改 `tests/blackbox/test_search.py` 中的 `BASE_URL`。
