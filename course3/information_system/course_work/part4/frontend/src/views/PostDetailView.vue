<template>
  <div class="post-detail-view">
    <div v-if="loading" class="has-text-centered">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p>Загрузка поста...</p>
    </div>

    <div v-else-if="!post" class="has-text-centered">
      <p class="is-size-4">Пост не найден</p>
      <router-link to="/posts" class="button is-primary mt-4">
        Вернуться к ленте
      </router-link>
    </div>

    <div v-else>
      <article class="card post-detail-card">
        <header class="card-header">
          <p class="card-header-title">
            {{ post.title }}
          </p>
          <button
            v-if="authStore.isAuthenticated && authStore.canEditPost(post.ownerId)"
            class="card-header-icon"
            @click="editPost"
            title="Редактировать пост"
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
            :title="authStore.isAuthenticated ? 'Поставить лайк' : 'Войдите, чтобы поставить лайк'"
          >
            <span class="icon">
              <i class="fas fa-heart" :class="{ 'has-text-danger': isLiked }"></i>
            </span>
            <span>{{ isLiked ? 'Убрать лайк' : 'Поставить лайк' }} ({{ post.countLike }})</span>
            <span v-if="isLiking" class="icon">
              <i class="fas fa-spinner fa-spin ml-2"></i>
            </span>
          </button>
        </footer>
      </article>

      <!-- Комментарии -->
      <div class="comments-section mt-6">
        <h2 class="title is-4">Комментарии</h2>

        <div v-if="authStore.isAuthenticated" class="comment-form mb-5">
          <div class="field">
            <div class="control">
              <textarea
                v-model="newComment"
                class="textarea"
                placeholder="Напишите комментарий..."
                rows="3"
              ></textarea>
            </div>
          </div>
          <div class="field">
            <div class="control">
              <button
                class="button is-primary"
                @click="addComment"
                :disabled="!newComment.trim()"
              >
                Отправить комментарий
              </button>
            </div>
          </div>
        </div>

        <div v-if="!authStore.isAuthenticated" class="notification is-light">
          <p>Войдите в систему, чтобы оставлять комментарии</p>
        </div>

        <div v-if="comments.length === 0 && authStore.isAuthenticated">
          <p class="has-text-grey">Комментариев пока нет. Будьте первым!</p>
        </div>

        <div v-else-if="comments.length === 0">
          <p class="has-text-grey">Комментариев пока нет.</p>
        </div>

        <div v-else>
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="card comment-card mb-3"
          >
            <div class="card-content">
              <div class="content">
                <p>{{ comment.userComment }}</p>
                <p class="is-size-7 has-text-grey">
                  {{ comment.accountName }} • {{ formatDate(comment.createdAt) }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postsAPI } from '../api/posts'
import type { Post, Comment } from '../types/posts'

const route = useRoute()
const authStore = useAuthStore()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const isLiking = ref(false)
const isLiked = ref(false)
const newComment = ref('')

const loadPost = async () => {
  loading.value = true
  try {
    const postId = parseInt(route.params.id as string)
    const response = await postsAPI.getPostById(postId)
    post.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке поста:', error)
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  if (!post.value) return

  try {
    const response = await postsAPI.getComments(post.value.id)
    comments.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке комментариев:', error)
  }
}

const toggleLike = async () => {
  if (!authStore.isAuthenticated || !post.value) return

  isLiking.value = true

  try {
    // Оптимистичное обновление
    isLiked.value = !isLiked.value
    post.value.countLike += isLiked.value ? 1 : -1

    await postsAPI.likePost(post.value.id)

  } catch (error) {
    // Откатываем изменения в случае ошибки
    isLiked.value = !isLiked.value
    post.value.countLike += isLiked.value ? -1 : 1
    console.error('Ошибка при оценке поста:', error)
    alert('Не удалось поставить лайк')
  } finally {
    isLiking.value = false
  }
}

const addComment = async () => {
  if (!authStore.user || !post.value || !newComment.value.trim()) return

  try {
    const commentData = {
      userComment: newComment.value.trim(),
      postId: post.value.id,
      accountId: authStore.user.id,
    }

    await postsAPI.createComment(commentData)
    newComment.value = ''
    await loadComments()
  } catch (error) {
    console.error('Ошибка при добавлении комментария:', error)
    alert('Не удалось добавить комментарий')
  }
}

const editPost = () => {
  console.log('Редактировать пост:', post.value)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await loadPost()
  await loadComments()
})
</script>
