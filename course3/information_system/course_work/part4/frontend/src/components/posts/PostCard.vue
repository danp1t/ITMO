<template>
  <article class="post-card card">
    <header class="card-header">
      <p class="card-header-title">
        {{ post.title }}
      </p>
      <button
        v-if="authStore.isAuthenticated && authStore.user?.id === post.ownerId"
        class="card-header-icon"
        @click="$emit('edit', post)"
      >
        <span class="icon">
          <i class="fas fa-edit"></i>
        </span>
      </button>
    </header>

    <div class="card-content">
      <div class="content">
        {{ post.text }}
      </div>

      <div class="tags">
        <span
          v-for="tag in post.tags"
          :key="tag"
          class="tag is-light"
        >
          {{ tag }}
        </span>
      </div>

      <div class="post-meta">
        <small>
          Автор: {{ post.ownerName }} |
          {{ formatDate(post.createdAt) }} |
          Комментарии: {{ post.commentsCount }} |
          Лайки: {{ post.likesCount }}
        </small>
      </div>
    </div>

    <footer class="card-footer">
      <a
        href="#"
        class="card-footer-item"
        @click.prevent="toggleLike"
      >
        <span class="icon">
          <i :class="{'fas': true, 'fa-heart': true, 'has-text-danger': isLiked}"></i>
        </span>
        <span>Лайк ({{ post.likesCount }})</span>
      </a>
      <router-link
        :to="`/posts/${post.id}`"
        class="card-footer-item"
      >
        <span class="icon">
          <i class="fas fa-comment"></i>
        </span>
        <span>Комментировать</span>
      </router-link>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../../stores/auth'
import type { Post } from '../../types/posts'

defineProps<{
  post: Post
}>()

const emit = defineEmits<{
  edit: [post: Post]
  like: [postId: number]
}>()

const authStore = useAuthStore()
const isLiked = ref(false)

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU')
}

const toggleLike = () => {
  if (!authStore.isAuthenticated) {
    alert('Для оценки постов необходимо войти в систему')
    return
  }

  emit('like', props.post.id)
  isLiked.value = !isLiked.value
}
</script>

<style scoped>
.post-card {
  margin-bottom: 20px;
}

.post-card .card-header {
  background-color: #405ca3;
}

.tags {
  margin-top: 10px;
  margin-bottom: 10px;
}

.post-meta {
  color: #d3a94b;
  font-size: 0.9em;
  margin-top: 10px;
}

.card-footer {
  border-top: 1px solid #f5f5f5;
}
</style>
