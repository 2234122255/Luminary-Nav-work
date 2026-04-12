<template>
  <section class="admin-page">
    <header class="hero">
      <p class="hero-tag">Admin Console</p>
      <h1>管理员控制台</h1>
      <p class="hero-desc">
        管理员专用页面已预构建完成。当前阶段暂不在导航栏显示，后续接入登录注册与权限校验后按管理员身份展示。
      </p>
    </header>

    <div class="stats-grid">
      <article class="stat-card">
        <p class="stat-label">平台用户数</p>
        <p class="stat-value">1,248</p>
      </article>
      <article class="stat-card">
        <p class="stat-label">今日新增学者</p>
        <p class="stat-value">36</p>
      </article>
      <article class="stat-card">
        <p class="stat-label">待处理反馈</p>
        <p class="stat-value">12</p>
      </article>
      <article class="stat-card">
        <p class="stat-label">系统健康度</p>
        <p class="stat-value">99.9%</p>
      </article>
    </div>

    <div class="panel-grid">
      <section class="panel">
        <h2>快捷操作</h2>
        <div class="action-list">
          <button type="button" disabled>用户权限管理（待接入）</button>
          <button type="button" disabled>内容审核中心（待接入）</button>
          <button type="button" disabled>系统日志查询（待接入）</button>
        </div>
      </section>

      <section class="panel">
        <h2>最近操作记录（示例）</h2>
        <ul class="log-list">
          <li>
            <span>更新榜单缓存任务配置</span>
            <time>2026-04-12 09:20</time>
          </li>
          <li>
            <span>新增机构别名映射 15 条</span>
            <time>2026-04-11 18:42</time>
          </li>
          <li>
            <span>修复作者详情接口超时策略</span>
            <time>2026-04-11 14:15</time>
          </li>
        </ul>
      </section>
    </div>
  </section>
</template>

<script>
export default {
  name: 'AdminView'
}
</script>

<style scoped>
.admin-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  color: #eaf1ff;
}

.hero {
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(79, 108, 255, 0.25), rgba(34, 52, 131, 0.4));
  border: 1px solid rgba(125, 160, 255, 0.45);
  box-shadow: 0 10px 24px rgba(10, 14, 42, 0.35);
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
  margin: 12px 0 0;
  max-width: 840px;
  color: rgba(236, 244, 255, 0.88);
  line-height: 1.7;
}

.stats-grid {
  margin-top: 20px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat-card {
  border-radius: 14px;
  padding: 18px;
  background: rgba(20, 28, 69, 0.68);
  border: 1px solid rgba(112, 151, 255, 0.28);
}

.stat-label {
  margin: 0;
  font-size: 14px;
  color: #adc3ff;
}

.stat-value {
  margin: 8px 0 0;
  font-size: 28px;
  font-weight: 700;
}

.panel-grid {
  margin-top: 18px;
  display: grid;
  gap: 12px;
  grid-template-columns: 1fr 1fr;
}

.panel {
  border-radius: 14px;
  padding: 18px;
  background: rgba(14, 21, 56, 0.72);
  border: 1px solid rgba(112, 151, 255, 0.24);
}

h2 {
  margin: 0 0 12px;
  font-size: 18px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-list button {
  text-align: left;
  border: 1px dashed rgba(140, 170, 255, 0.4);
  border-radius: 10px;
  padding: 10px 12px;
  background: rgba(63, 96, 207, 0.16);
  color: rgba(238, 245, 255, 0.85);
}

.log-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-list li {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(132, 168, 255, 0.18);
}

.log-list time {
  color: #9db5ff;
  font-size: 13px;
  white-space: nowrap;
}

@media (max-width: 980px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .admin-page {
    padding: 14px;
  }

  h1 {
    font-size: 24px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .log-list li {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
