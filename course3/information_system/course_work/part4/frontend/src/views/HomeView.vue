<template>
  <div class="home">
    <h1 class="title">Добро пожаловать в сообщество художественной гимнастики!</h1>

    <div class="columns">
      <!-- Лента постов -->
      <div class="column is-two-thirds">
        <section class="section">
          <h2 class="subtitle">Последние посты</h2>
          <div v-if="loading" class="has-text-centered">
            Загрузка...
          </div>
          <div v-else-if="posts.length === 0" class="has-text-centered">
            Постов пока нет
          </div>
          <div v-else>
            <PostCard
              v-for="post in posts"
              :key="post.id"
              :post="post"
            />
          </div>
        </section>
      </div>

      <!-- Боковая панель -->
      <div class="column">
        <aside class="menu">
          <p class="menu-label">
            Быстрый доступ
          </p>
          <ul class="menu-list">
            <li>
              <router-link to="/tournaments" class="is-active">
                🏆 Ближайшие соревнования
              </router-link>
            </li>
            <li v-if="authStore.isAuthenticated">
              <router-link to="/shop">
                🛒 Интернет-магазин
              </router-link>
            </li>
          </ul>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PostCard from '../components/posts/PostCard.vue'
import { useAuthStore } from '../stores/auth'
import { postsAPI } from '../api/posts'
import type { Post } from '../types/posts'

const authStore = useAuthStore()
const posts = ref<Post[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const response = await postsAPI.getPosts()
    posts.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке постов:', error)
  } finally {
    loading.value = false
  }
})
</script>
