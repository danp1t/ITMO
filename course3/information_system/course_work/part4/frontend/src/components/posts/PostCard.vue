<template>
  <article class="post-card card">
    <header class="card-header">
      <p class="card-header-title">
        {{ post.title }}
      </p>
      <div class="card-header-icons" v-if="authStore.isAuthenticated && authStore.canEditPost(post.ownerId)">
        <button
          class="card-header-icon mr-1"
          @click="$emit('edit', post)"
          title="Редактировать пост"
        >
          <span class="icon">
            <i class="fas fa-edit"></i>
          </span>
        </button>
        <button
          v-if="authStore.canDeletePost(post.ownerId)"
          class="card-header-icon"
          @click="confirmDelete"
          title="Удалить пост"
        >
          <span class="icon">
            <i class="fas fa-trash"></i>
          </span>
        </button>
      </div>
    </header>

    <div class="card-content">
      <div class="content">
        {{ post.text }}
      </div>

      <div class="post-meta">
        <small>
          Автор: {{ post.ownerName }} |
          {{ formatDate(post.createdAt) }} |
          Лайки: {{ currentLikeCount }}
        </small>
      </div>
    </div>

    <footer class="card-footer">
      <button
        class="card-footer-item like-button"
        @click="toggleLike"
        :disabled="isLiking || !authStore.isAuthenticated"
        :title="!authStore.isAuthenticated ? 'Войдите, чтобы поставить лайк' : ''"
      >
        <span class="icon">
          <i class="fas fa-heart" :class="{ 'has-text-danger': isLikedByCurrentUser }"></i>
        </span>
        <span>{{ isLikedByCurrentUser ? 'Убрать лайк' : 'Поставить лайк' }} ({{ currentLikeCount }})</span>
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
import { ref, computed, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.ts'
import { postsAPI } from '@/api/posts.ts'
import type { Post } from '@/types/posts.ts'

const props = defineProps<{
  post: Post
}>()

const emit = defineEmits<{
  edit: [post: Post]
  like: [postId: number]
  delete: [postId: number]
}>()

const authStore = useAuthStore()

// Состояние для отслеживания, лайкал ли текущий пользователь этот пост
const isLikedByCurrentUser = ref(false)
// Состояние загрузки для предотвращения множественных кликов
const isLiking = ref(false)
// Локальное состояние счетчика лайков
const currentLikeCount = ref(props.post.countLike || 0)

// Проверяем, лайкал ли пользователь этот пост
const checkIfLiked = () => {
  // В реальном приложении эту информацию должен возвращать сервер
  // Сейчас просто сбрасываем состояние
  isLikedByCurrentUser.value = false
}

// При изменении поста обновляем счетчик
watch(() => props.post.countLike, (newCount) => {
  currentLikeCount.value = newCount
})

// При изменении пользователя проверяем лайки
watch(() => authStore.user, () => {
  checkIfLiked()
})

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric'
  })
}

const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    alert('Для оценки постов необходимо войти в систему')
    return
  }

  if (isLiking.value) return

  isLiking.value = true

  try {
    // Сохраняем предыдущее состояние для возможности отката
    const wasLiked = isLikedByCurrentUser.value
    const oldCount = currentLikeCount.value

    // Оптимистичное обновление UI
    isLikedByCurrentUser.value = !wasLiked
    currentLikeCount.value = wasLiked ? oldCount - 1 : oldCount + 1

    // Отправляем запрос на сервер
    await postsAPI.likePost(props.post.id)

    // Если запрос успешен, эмитируем событие
    emit('like', props.post.id)

  } catch (error: any) {
    // Откатываем изменения в случае ошибки
    isLikedByCurrentUser.value = !isLikedByCurrentUser.value
    currentLikeCount.value = isLikedByCurrentUser.value
      ? currentLikeCount.value + 1
      : currentLikeCount.value - 1

    console.error('Ошибка при оценке поста:', error)

    // Показываем пользователю понятное сообщение
    const errorMessage = error.response?.data?.message || 'Не удалось поставить лайк'
    alert(errorMessage)
  } finally {
    isLiking.value = false
  }
}

const confirmDelete = () => {
  if (confirm(`Вы уверены, что хотите удалить пост "${props.post.title}"?`)) {
    emit('delete', props.post.id)
  }
}

// Инициализация при монтировании
checkIfLiked()
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
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-title {
  color: white;
  margin: 0;
  font-weight: 600;
  flex-grow: 1;
}

.card-header-icons {
  display: flex;
  align-items: center;
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
