<template>
  <div class="author-detail">
    <h2>{{ author.name }}</h2>
    <div class="info">
      <p>机构: {{ author.org }}</p>
      <p>论文数: {{ author.paperCount }}</p>
      <p>总被引量: {{ author.totalCitations }}</p>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

export default {
  setup() {
    const author = ref({})
    const route = useRoute()

    onMounted(async () => {
      try {
        const response = await axios.get(`/api/authors/${route.params.id}`)
        author.value = response.data
      } catch (error) {
        console.error('Error fetching author details:', error)
      }
    })

    return { author }
  }
}
</script>

<style scoped>
.author-detail {
  padding: 20px;
}
.info p {
  margin: 10px 0;
}
</style>