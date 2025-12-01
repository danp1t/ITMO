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
          <div class="card-header-icons" v-if="authStore.isAuthenticated && authStore.canEditPost(post.ownerId)">
            <button
              class="card-header-icon mr-1"
              @click="editPost"
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
            :title="authStore.isAuthenticated ? 'Поставить лайк' : 'Войдите, чтобы поставить лайк'"
          >
            <span class="icon">
              <i class="fas fa-heart" :class="{ 'has-text-danger': isLiked }"></i>
            </span>
            <span>{{ isLiked ? 'Убрать лайк' : 'Поставить лайк' }} ({{ currentLikeCount }})</span>
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

    <!-- Модальное окно редактирования поста -->
    <div class="modal" :class="{ 'is-active': showEditModal }">
      <div class="modal-background" @click="closeEditModal"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">Редактировать пост</p>
          <button class="delete" @click="closeEditModal"></button>
        </header>

        <section class="modal-card-body">
          <div class="field">
            <label class="label">Заголовок</label>
            <div class="control">
              <input
                v-model="editingPost.title"
                class="input"
                type="text"
                placeholder="Введите заголовок"
                :disabled="isSaving"
              >
            </div>
          </div>

          <div class="field">
            <label class="label">Текст</label>
            <div class="control">
              <textarea
                v-model="editingPost.text"
                class="textarea"
                placeholder="Введите текст поста"
                rows="8"
                :disabled="isSaving"
              ></textarea>
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button
            class="button is-primary"
            @click="savePost"
            :disabled="isSaving"
          >
            <span v-if="isSaving" class="icon">
              <i class="fas fa-spinner fa-spin"></i>
            </span>
            <span>{{ isSaving ? 'Сохранение...' : 'Сохранить' }}</span>
          </button>
          <button
            class="button"
            @click="closeEditModal"
            :disabled="isSaving"
          >
            Отмена
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postsAPI } from '../api/posts'
import type { Post, Comment } from '../types/posts'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const isLiking = ref(false)
const isLiked = ref(false)
const newComment = ref('')
const showEditModal = ref(false)
const isSaving = ref(false)
const currentLikeCount = ref(0)

// Данные для редактирования
const editingPost = reactive({
  id: 0,
  title: '',
  text: '',
  ownerId: 0
})

const loadPost = async () => {
  loading.value = true
  try {
    const postId = parseInt(route.params.id as string)
    const response = await postsAPI.getPostById(postId)
    post.value = response.data

    // Заполняем данные для редактирования
    if (post.value) {
      editingPost.id = post.value.id
      editingPost.title = post.value.title
      editingPost.text = post.value.text
      editingPost.ownerId = post.value.ownerId
      currentLikeCount.value = post.value.countLike || 0
    }
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

// Открытие модального окна редактирования
const editPost = () => {
  if (!post.value) return

  showEditModal.value = true
}

// Сохранение изменений
const savePost = async () => {
  if (!editingPost.title.trim()) {
    alert('Введите заголовок поста')
    return
  }

  if (!editingPost.text.trim()) {
    alert('Введите текст поста')
    return
  }

  isSaving.value = true

  try {
    const postData = {
      title: editingPost.title,
      text: editingPost.text,
      ownerId: editingPost.ownerId,
    }

    await postsAPI.updatePost(editingPost.id, postData)

    // Обновляем текущий пост
    if (post.value) {
      post.value.title = editingPost.title
      post.value.text = editingPost.text
    }

    closeEditModal()
    alert('Пост успешно обновлен!')
  } catch (error: any) {
    console.error('Ошибка при обновлении поста:', error)
    const message = error.response?.data?.message || 'Не удалось обновить пост'
    alert(message)
  } finally {
    isSaving.value = false
  }
}

// Закрытие модального окна
const closeEditModal = () => {
  showEditModal.value = false
  // Восстанавливаем оригинальные значения
  if (post.value) {
    editingPost.title = post.value.title
    editingPost.text = post.value.text
  }
}

const toggleLike = async () => {
  if (!authStore.isAuthenticated || !post.value) return

  isLiking.value = true

  try {
    // Оптимистичное обновление
    isLiked.value = !isLiked.value
    currentLikeCount.value += isLiked.value ? 1 : -1

    await postsAPI.likePost(post.value.id)

  } catch (error) {
    // Откатываем изменения в случае ошибки
    isLiked.value = !isLiked.value
    currentLikeCount.value += isLiked.value ? -1 : 1
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

const confirmDelete = async () => {
  if (!post.value) return

  if (!confirm(`Вы уверены, что хотите удалить пост "${post.value.title}"? Это действие нельзя отменить.`)) {
    return
  }

  try {
    await postsAPI.deletePost(post.value.id)
    alert('Пост успешно удален!')
    router.push('/posts')
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const message = error.response?.data?.message || 'Не удалось удалить пост'
    alert(message)
  }
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

<style scoped>
.post-detail-view {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.post-detail-card {
  margin-bottom: 30px;
}

.post-detail-card .card-header {
  background-color: #405ca3;
  color: white;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-detail-card .card-header-title {
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

.comments-section {
  margin-top: 30px;
}

.comment-form {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.comment-card {
  border-left: 4px solid #405ca3;
}
</style>
