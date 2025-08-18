<template>
  <div id="app">
    <!-- 顶部Logo栏 -->
    <div class="top-bar">
      <div class="container d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center">
          <img src="@/assets/logo.png" alt="Logo" height="40" class="me-2">
          <h1 class="m-0">合著网络人才分析平台</h1>

        </div>
        <div class="search-box">
          <div class="input-group">
            <input
                type="text"
                class="form-control"
                placeholder="搜索作者..."
                v-model="searchQuery"
                @keyup.enter="searchAuthors"
            >
            <button class="btn btn-primary" @click="searchAuthors">
              <i class="bi bi-search"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="nav-menu navbar navbar-expand-lg navbar-dark">
      <div class="container">
        <ul class="navbar-nav">
          <li class="nav-item">
            <router-link class="nav-link" to="/">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/rankings">榜单</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/heatmap">热力地图</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/network">合著网络</router-link>
          </li>
        </ul>
      </div>
    </nav>

    <!-- 主内容区 -->
    <div class="container my-4">
      <router-view/>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      searchQuery: ''
    }
  },
  methods: {
    searchAuthors() {
      if (this.searchQuery.trim()) {
        this.$router.push(`/author/search?q=${encodeURIComponent(this.searchQuery)}`)
      }
    }
  }
}
</script>

<style scoped>
/* 添加之前的样式 */
</style>