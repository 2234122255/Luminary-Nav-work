<template>
    <div>
          <h1>作者搜索</h1>
      <div class="search-container">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="输入作者姓名搜索..."
          @keyup.enter="searchAuthors"
        >
        <button @click="searchAuthors">搜索</button>
      </div>
      
      <div v-if="searchResults.length > 0" class="results">
        <h2>搜索结果</h2>
        <div v-for="author in searchResults" :key="author.id" class="author-item">
          <h3>{{ author.name }}</h3>
          <p>{{ author.org }}</p>
          <p>总分: {{ author.totalScore }}</p>
        </div>
      </div>
    </div>
  </template>

<script>
import { ref } from 'vue'


export default {
  name: 'AuthorSearchView',

  setup() {
    const searchQuery = ref('')
    const searchResults = ref([])

    const searchAuthors = () => {
      // 这里可以添加搜索逻辑
      console.log('搜索:', searchQuery.value)
    }

    return {
      searchQuery,
      searchResults,
      searchAuthors
    }
  }
}
</script>

<style scoped>
.search-container {
  margin: 20px 0;
  display: flex;
  gap: 10px;
}

.search-container input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.search-container button {
  padding: 10px 20px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.results {
  margin-top: 20px;
}

.author-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 10px;
}

.author-item h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.author-item p {
  margin: 5px 0;
  color: #666;
}
</style>