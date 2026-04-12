<template>
  <div class="admin-gallery-view">
    <section class="admin-hero">
      <span class="eyebrow">administrator</span>
      <h1>管理员界面</h1>
    </section>

    <section class="gallery-grid">
      <article v-for="image in images" :key="image.title" class="image-card">
        <div class="image-frame">
          <img :src="image.url" :alt="image.title">
        </div>
        <div class="image-caption">
          <h2>{{ image.title }}</h2>
        </div>
      </article>
    </section>
  </div>
</template>

<script>
export default {
  name: 'AdminGalleryView',
  data() {
    const baseUrl = 'https://raw.githubusercontent.com/26hzhang/DBLPParser/master/img'
    return {
      images: [
        { title: 'General', url: `${baseUrl}/general.png` },
        { title: 'All Feature', url: `${baseUrl}/all_feature.png` },
        { title: 'Article Feature', url: `${baseUrl}/article_feature.png` },
        { title: 'Article Year', url: `${baseUrl}/article_year.png` }
      ]
    }
  }
}
</script>

<style scoped>
.admin-gallery-view {
  min-height: calc(100vh - 220px);
  padding: 120px 30px 50px;
  color: #fff;
}

.admin-hero {
  max-width: 1180px;
  margin: 0 auto 28px;
  padding: 28px;
  border-radius: 8px;
  border: 1px solid rgba(100, 150, 255, 0.25);
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(18px);
}

.eyebrow {
  display: inline-flex;
  margin-bottom: 10px;
  color: #9bb8ff;
  font-size: 13px;
  letter-spacing: 0;
}

.admin-hero h1 {
  margin: 0;
  font-size: 34px;
  font-weight: 700;
}

.gallery-grid {
  max-width: 1180px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px;
  align-items: start;
}

.image-card {
  align-self: start;
  border-radius: 8px;
  border: 1px solid rgba(100, 150, 255, 0.25);
  background: rgba(255, 255, 255, 0.06);
  overflow: hidden;
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(16px);
}

.image-frame {
  padding: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(7, 10, 28, 0.58);
}

.image-frame img {
  width: 100%;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  background: #fff;
}

.image-caption {
  padding: 18px 20px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.image-caption h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

@media (max-width: 860px) {
  .admin-gallery-view {
    padding-top: 180px;
  }

  .gallery-grid {
    grid-template-columns: 1fr;
  }
}
</style>
