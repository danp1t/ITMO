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

      <div class="post-meta">
        <small>
          Автор: {{ post.ownerName }} |
          {{ formatDate(post.createdAt) }} |
          Лайки: {{ post.countLike }}
        </small>
      </div>
    </div>

    <footer class="card-footer">
      <button
        class="card-footer-item like-button"
        @click="toggleLike"
        :disabled="isLiking || !authStore.isAuthenticated"
      >
        <span class="icon">
          <i class="fas fa-heart" :class="{ 'has-text-danger': isLiked }"></i>
        </span>
        <span>Лайк ({{ post.countLike }})</span>
        <span v-if="isLiking" class="icon">
          <i class="fas fa-spinner fa-spin ml-2"></i>
        </span>
      </button>
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
import { useAuthStore } from '@/stores/auth.ts'
import type { Post } from '@/types/posts.ts'

const props = defineProps<{
  post: Post
}>()

const emit = defineEmits<{
  edit: [post: Post]
  like: [postId: number]
}>()

const authStore = useAuthStore()
const isLiked = ref(false)
const isLiking = ref(false)

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric'
  })
}

const toggleLike = () => {
  if (!authStore.isAuthenticated) {
    alert('Для оценки постов необходимо войти в систему')
    return
  }

  if (isLiking.value) return

  isLiking.value = true
  isLiked.value = !isLiked.value

  // Оптимистичное обновление счетчика
  props.post.countLike += isLiked.value ? 1 : -1

  emit('like', props.post.id)

  // Через секунду сбросим состояние загрузки
  setTimeout(() => {
    isLiking.value = false
  }, 300)
}
</script>

<style scoped>
.post-card {
  margin-bottom: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.post-card:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.post-card .card-header {
  background-color: #405ca3;
  color: white;
  padding: 12px 20px;
}

.card-header-title {
  color: white;
  margin: 0;
  font-weight: 600;
}

.card-header-icon {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.card-header-icon:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.card-content {
  padding: 20px;
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
  display: flex;
}

.card-footer-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #666;
  text-decoration: none;
  border: none;
  background: none;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.95em;
}

.card-footer-item:hover:not(:disabled) {
  background-color: #e9ecef;
  color: #333;
}

.card-footer-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.like-button:hover:not(:disabled) {
  color: #ff3860;
}

.like-button:hover:not(:disabled) .icon {
  color: #ff3860;
}

.has-text-danger {
  color: #ff3860 !important;
}

.fa-spinner {
  font-size: 0.8em;
}
</style>
