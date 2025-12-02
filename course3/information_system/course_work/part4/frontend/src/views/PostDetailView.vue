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
                :disabled="isAddingComment"
              ></textarea>
            </div>
          </div>
          <div class="field">
            <div class="control">
              <button
                class="button is-primary"
                @click="addComment"
                :disabled="!newComment.trim() || isAddingComment"
              >
                <span v-if="isAddingComment" class="icon">
                  <i class="fas fa-spinner fa-spin"></i>
                </span>
                <span>{{ isAddingComment ? 'Отправка...' : 'Отправить комментарий' }}</span>
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
              <div class="content" v-if="editingCommentId !== comment.id">
                <p>{{ comment.userComment }}</p>
                <p class="is-size-7 has-text-grey">
                  {{ comment.accountName }} • {{ formatDate(comment.createdAt) }}
                </p>
              </div>

              <!-- Форма редактирования комментария -->
              <div v-else class="comment-edit-form">
                <div class="field">
                  <div class="control">
                    <textarea
                      v-model="editingCommentText"
                      class="textarea"
                      rows="3"
                      :disabled="isEditingComment"
                    ></textarea>
                  </div>
                </div>
                <div class="buttons mt-2">
                  <button
                    class="button is-small is-primary"
                    @click="saveEditedComment(comment.id)"
                    :disabled="!editingCommentText.trim() || isEditingComment"
                  >
                    <span v-if="isEditingComment" class="icon">
                      <i class="fas fa-spinner fa-spin"></i>
                    </span>
                    <span>{{ isEditingComment ? 'Сохранение...' : 'Сохранить' }}</span>
                  </button>
                  <button
                    class="button is-small is-light"
                    @click="cancelEditComment"
                    :disabled="isEditingComment"
                  >
                    Отмена
                  </button>
                </div>
              </div>

              <!-- Кнопки действий (только для автора комментария) -->
              <div
                v-if="authStore.isAuthenticated &&
                      authStore.canEditComment(comment.accountId) &&
                      editingCommentId !== comment.id"
                class="comment-actions mt-2"
              >
                <button
                  class="button is-small is-text"
                  @click="startEditComment(comment)"
                  title="Редактировать комментарий"
                >
                  <span class="icon is-small">
                    <i class="fas fa-edit"></i>
                  </span>
                  <span>Редактировать</span>
                </button>

                <button
                  class="button is-small is-text has-text-danger"
                  @click="deleteComment(comment.id)"
                  :disabled="isDeletingComment === comment.id"
                  title="Удалить комментарий"
                >
                  <span class="icon is-small">
                    <i class="fas fa-trash"></i>
                  </span>
                  <span v-if="isDeletingComment === comment.id">
                    <i class="fas fa-spinner fa-spin ml-1"></i>
                  </span>
                  <span v-else>Удалить</span>
                </button>
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
const isAddingComment = ref(false)

// Состояния для редактирования комментариев
const editingCommentId = ref<number | null>(null)
const editingCommentText = ref('')
const isEditingComment = ref(false)
const isDeletingComment = ref<number | null>(null)

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
  } finally {
    isLiking.value = false
  }
}

const addComment = async () => {
  if (!authStore.user || !post.value || !newComment.value.trim()) return

  isAddingComment.value = true

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
  } finally {
    isAddingComment.value = false
  }
}

// Редактирование комментария
const startEditComment = (comment: Comment) => {
  editingCommentId.value = comment.id
  editingCommentText.value = comment.userComment
}

const cancelEditComment = () => {
  editingCommentId.value = null
  editingCommentText.value = ''
  isEditingComment.value = false
}

const saveEditedComment = async (commentId: number) => {
  if (!editingCommentText.value.trim()) return

  isEditingComment.value = true

  try {
    // Находим оригинальный комментарий
    const originalComment = comments.value.find(c => c.id === commentId)
    if (!originalComment) return

    // Создаем обновленный DTO объекта комментария
    const updatedComment: Comment = {
      ...originalComment,
      userComment: editingCommentText.value.trim()
    }

    await postsAPI.updateComment(commentId, updatedComment)

    // Обновляем локально
    const commentIndex = comments.value.findIndex(c => c.id === commentId)
    if (commentIndex !== -1) {
      comments.value[commentIndex] = updatedComment
    }

    cancelEditComment()

    // Можно добавить уведомление об успехе
    // useToast().success('Комментарий обновлен')
  } catch (error) {
    console.error('Ошибка при обновлении комментария:', error)
    // Можно добавить уведомление об ошибке
    // useToast().error('Не удалось обновить комментарий')
  } finally {
    isEditingComment.value = false
  }
}

// Удаление комментария
const deleteComment = async (commentId: number) => {
  if (!authStore.user || !authStore.canDeleteComment(
    comments.value.find(c => c.id === commentId)?.accountId || 0
  )) {
    return
  }

  isDeletingComment.value = commentId

  try {
    await postsAPI.deleteComment(commentId)
    // Удаляем из списка
    comments.value = comments.value.filter(c => c.id !== commentId)
  } catch (error) {
    console.error('Ошибка при удалении комментария:', error)
  } finally {
    isDeletingComment.value = null
  }
}

const editPost = () => {
  // Реализуйте логику редактирования поста
  console.log('Редактировать пост:', post.value)
  // Можно добавить роутинг на страницу редактирования
  // или открыть модальное окно, как в PostsView.vue
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
.comment-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.comment-actions .button {
  padding: 4px 8px;
  font-size: 0.8rem;
}

.comment-edit-form .textarea {
  font-size: 0.9rem;
  min-height: 80px;
}

.comment-card {
  border: 1px solid #f0f0f0;
  box-shadow: none;
  transition: border-color 0.2s ease;
}

.comment-card:hover {
  border-color: #e0e0e0;
}

.button.is-text.has-text-danger:hover {
  background-color: rgba(255, 56, 96, 0.1);
}

.fa-spinner {
  font-size: 0.8em;
}
</style>
