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
          <div class="card-header-icons">
            <button
              v-if="authStore.isAuthenticated && authStore.canEditPost(post.ownerId)"
              class="card-header-icon"
              @click="startEdit"
              title="Редактировать пост"
            >
              <span class="icon">
                <i class="fas fa-edit"></i>
              </span>
            </button>
            <button
              v-if="authStore.isAuthenticated && authStore.canDeletePost(post.ownerId)"
              class="card-header-icon"
              @click="confirmDelete"
              title="Удалить пост"
              :disabled="isDeleting"
            >
              <span class="icon">
                <i class="fas fa-trash"></i>
              </span>
              <span v-if="isDeleting" class="icon">
                <i class="fas fa-spinner fa-spin ml-2"></i>
              </span>
            </button>
          </div>
        </header>

        <!-- Режим просмотра -->
        <div v-if="!isEditing" class="card-content">
          <div class="post-content" v-html="post.text"></div>

          <!-- Отображение тегов поста -->
          <TagList
            v-if="post.tags && post.tags.length > 0"
            :tags="post.tags"
            class="mt-4"
          />

          <div class="post-meta mt-4">
            <small>
              <span class="icon-text">
                <span class="icon">
                  <i class="fas fa-user"></i>
                </span>
                <span>{{ post.ownerName }}</span>
              </span>
              <span class="icon-text">
                <span class="icon">
                  <i class="fas fa-calendar"></i>
                </span>
                <span>{{ formatDate(post.createdAt) }}</span>
              </span>
              <span class="icon-text">
                <span class="icon">
                  <i class="fas fa-heart"></i>
                </span>
                <span>{{ post.countLike }}</span>
              </span>
            </small>
          </div>
        </div>

        <!-- Режим редактирования -->
        <div v-else class="card-content">
          <div class="field">
            <label class="label">Заголовок</label>
            <div class="control">
              <input
                v-model="editForm.title"
                class="input"
                type="text"
                placeholder="Введите заголовок"
                :disabled="isSaving"
              >
            </div>
          </div>

          <div class="field">
            <label class="label">Содержание</label>
            <div class="control">
              <RichTextEditor
                ref="editorRef"
                v-model="editForm.content"
                :disabled="isSaving"
                :postId="post?.id"
                placeholder="Редактируйте содержимое поста..."
                @file-upload-error="handleFileUploadError"
              />
            </div>
          </div>

          <div class="field is-grouped mt-4">
            <div class="control">
              <button
                class="button is-primary"
                @click="saveEdit"
                :disabled="isSaving || !editForm.title.trim() || !editForm.content.trim()"
              >
                <span v-if="isSaving" class="icon">
                  <i class="fas fa-spinner fa-spin"></i>
                </span>
                <span>{{ isSaving ? 'Сохранение...' : 'Сохранить' }}</span>
              </button>
            </div>
            <div class="control">
              <button
                class="button is-light"
                @click="cancelEdit"
                :disabled="isSaving"
              >
                Отмена
              </button>
            </div>
          </div>
        </div>

        <footer v-if="!isEditing" class="card-footer">
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

      <!-- Комментарии (только в режиме просмотра) -->
      <div v-if="!isEditing" class="comments-section mt-6">
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
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import RichTextEditor from '../components/posts/RichTextEditor.vue'
import TagList from '../components/posts/TagList.vue'
import { postsAPI } from '../api/posts'
import type { Post, Comment, UpdatePostRequest } from '../types/posts'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const post = ref<Post | null>(null)
const comments = ref<Comment[]>([])
const loading = ref(false)
const isLiking = ref(false)
const isLiked = ref(false)
const newComment = ref('')
const isAddingComment = ref(false)
const isEditing = ref(false)
const isSaving = ref(false)
const isDeleting = ref(false)

// Реф для редактора
const editorRef = ref<InstanceType<typeof RichTextEditor>>()

// Состояния для редактирования комментариев
const editingCommentId = ref<number | null>(null)
const editingCommentText = ref('')
const isEditingComment = ref(false)
const isDeletingComment = ref<number | null>(null)

// Форма редактирования поста
const editForm = ref({
  title: '',
  content: '',
})

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
    const originalComment = comments.value.find(c => c.id === commentId)
    if (!originalComment) return

    const updatedComment: Comment = {
      ...originalComment,
      userComment: editingCommentText.value.trim()
    }

    await postsAPI.updateComment(commentId, updatedComment)

    const commentIndex = comments.value.findIndex(c => c.id === commentId)
    if (commentIndex !== -1) {
      comments.value[commentIndex] = updatedComment
    }

    cancelEditComment()
  } catch (error) {
    console.error('Ошибка при обновлении комментария:', error)
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
    comments.value = comments.value.filter(c => c.id !== commentId)
  } catch (error) {
    console.error('Ошибка при удалении комментария:', error)
  } finally {
    isDeletingComment.value = null
  }
}

// Редактирование поста
const startEdit = () => {
  if (!post.value) return

  editForm.value = {
    title: post.value.title,
    content: post.value.text
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {
    title: '',
    content: ''
  }

  if (editorRef.value) {
    editorRef.value.clearPendingFiles()
  }
}

// Обработчик ошибок загрузки файлов
const handleFileUploadError = (error: string) => {
  alert(error)
}

const saveEdit = async () => {
  if (!post.value || !authStore.user) return

  // Валидация
  if (!editForm.value.title.trim()) {
    alert('Введите заголовок поста')
    return
  }

  if (!editForm.value.content.trim() || editForm.value.content === '<p></p>') {
    alert('Введите содержание поста')
    return
  }

  isSaving.value = true

  try {
    const postData: UpdatePostRequest = {
      title: editForm.value.title,
      text: editForm.value.content,
      ownerId: post.value.ownerId,
    }

    await postsAPI.updatePost(post.value.id, postData)

    // Обновляем данные поста
    post.value.title = editForm.value.title
    post.value.text = editForm.value.content
    post.value.updatedAt = new Date().toISOString()

    isEditing.value = false
  } catch (error: any) {
    console.error('Ошибка при обновлении поста:', error)
    const message = error.response?.data?.message || 'Не удалось обновить пост'
    alert(message)
  } finally {
    isSaving.value = false
  }
}

// Удаление поста
const confirmDelete = async () => {
  if (!post.value || !authStore.canDeletePost(post.value.ownerId)) {
    return
  }

  isDeleting.value = true

  try {
    await postsAPI.deletePost(post.value.id)
    router.push('/posts')
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить пост'
    alert(errorMessage)
  } finally {
    isDeleting.value = false
  }
}

const formatDate = (dateString: string, timeOnly = false) => {
  const date = new Date(dateString)

  if (timeOnly) {
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  return date.toLocaleDateString('ru-RU', {
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

.card-header-icons {
  display: flex;
  align-items: center;
}

.card-header-icon {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: background-color 0.2s;
  margin-left: 4px;
}

.card-header-icon:hover {
  background: rgba(255, 255, 255, 0.2);
}

.card-header-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Стили для отображения HTML контента поста */
.post-content {
  font-size: 1rem;
  line-height: 1.6;
  color: #d5d2d2;
  margin-bottom: 20px;
}

.post-content h1 {
  font-size: 1.8em;
  font-weight: 600;
  margin: 1.2em 0 0.8em;
  color: #111827;
}

.post-content h2 {
  font-size: 1.5em;
  font-weight: 600;
  margin: 1em 0 0.6em;
  color: #111827;
}

.post-content h3 {
  font-size: 1.2em;
  font-weight: 600;
  margin: 0.8em 0 0.4em;
  color: #111827;
}

.post-content p {
  margin: 0 0 1em;
}

.post-content strong {
  font-weight: 700;
}

.post-content em {
  font-style: italic;
}

.post-content ul,
.post-content ol {
  padding-left: 1.5em;
  margin: 1em 0;
}

.post-content ul li {
  list-style-type: disc;
  margin-bottom: 0.5em;
}

.post-content ol li {
  list-style-type: decimal;
  margin-bottom: 0.5em;
}

.post-content blockquote {
  border-left: 3px solid #dbdbdb;
  padding-left: 1em;
  margin: 1em 0;
  color: #666;
  font-style: italic;
}

.post-content img {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
}

.post-content hr {
  border: none;
  border-top: 1px solid #dbdbdb;
  margin: 2em 0;
}

.post-content a {
  color: #3273dc;
  text-decoration: underline;
}

.post-content a:hover {
  color: #363636;
}

.post-meta {
  color: #6b7280;
  font-size: 0.9em;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.icon-text {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
