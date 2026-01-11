<template>
  <div class="post-detail-view">
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

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

        <div v-if="!isEditing" class="card-content">
          <div class="post-content" v-html="post.text"></div>

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
                :showTagSelector="true"
                :initialTags="editForm.tagIds"
                @tags-change="handleTagsChange"
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import RichTextEditor from '../components/posts/RichTextEditor.vue'
import TagList from '../components/posts/TagList.vue'
import AppNotification from './AppNotification.vue'
import { postsAPI } from '../api/posts'
import type { Post, Comment, UpdatePostRequest } from '../types/posts'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Уведомление
const notification = reactive({
  visible: false,
  message: '',
  type: 'info' as 'info' | 'success' | 'warning' | 'error'
})

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  notification.message = message
  notification.type = type
  notification.visible = true

  // Автоматическое скрытие через 5 секунд
  setTimeout(() => {
    hideNotification()
  }, 5000)
}

const hideNotification = () => {
  notification.visible = false
}

// Данные
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

const editorRef = ref<InstanceType<typeof RichTextEditor>>()

const editingCommentId = ref<number | null>(null)
const editingCommentText = ref('')
const isEditingComment = ref(false)
const isDeletingComment = ref<number | null>(null)

const editForm = ref({
  title: '',
  content: '',
  tagIds: [] as number[]
})

const handleTagsChange = (tagIds: number[]) => {
  editForm.value.tagIds = tagIds
}

const loadPost = async () => {
  loading.value = true
  try {
    const postId = parseInt(route.params.id as string)
    const response = await postsAPI.getPostById(postId)
    post.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке поста:', error)
    showNotification('Не удалось загрузить пост', 'error')
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
    isLiked.value = !isLiked.value
    post.value.countLike += isLiked.value ? 1 : -1

    await postsAPI.likePost(post.value.id)

    const action = isLiked.value ? 'лайк поставлен' : 'лайк убран'
    showNotification(`Ваш ${action}`, 'success')

  } catch (error) {
    isLiked.value = !isLiked.value
    post.value.countLike += isLiked.value ? -1 : 1
    console.error('Ошибка при оценке поста:', error)
    showNotification('Не удалось поставить лайк', 'error')
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
    showNotification('Комментарий добавлен', 'success')
  } catch (error) {
    console.error('Ошибка при добавлении комментария:', error)
    showNotification('Не удалось добавить комментарий', 'error')
  } finally {
    isAddingComment.value = false
  }
}

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
    showNotification('Комментарий обновлен', 'success')
  } catch (error) {
    console.error('Ошибка при обновлении комментария:', error)
    showNotification('Не удалось обновить комментарий', 'error')
  } finally {
    isEditingComment.value = false
  }
}

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
    showNotification('Комментарий удален', 'success')
  } catch (error) {
    console.error('Ошибка при удалении комментария:', error)
    showNotification('Не удалось удалить комментарий', 'error')
  } finally {
    isDeletingComment.value = null
  }
}

const startEdit = () => {
  if (!post.value) return

  editForm.value = {
    title: post.value.title,
    content: post.value.text,
    tagIds: post.value.tags?.map(tag => tag.id) || []
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editForm.value = {
    title: '',
    content: '',
    tagIds: []
  }

  if (editorRef.value) {
    editorRef.value.clearPendingFiles()
  }
}

const handleFileUploadError = (error: string) => {
  showNotification(error, 'error')
}

const saveEdit = async () => {
  if (!post.value || !authStore.user) return

  if (!editForm.value.title.trim()) {
    showNotification('Введите заголовок поста', 'warning')
    return
  }

  if (!editForm.value.content.trim() || editForm.value.content === '<p></p>') {
    showNotification('Введите содержание поста', 'warning')
    return
  }

  isSaving.value = true

  try {
    const postData: UpdatePostRequest = {
      title: editForm.value.title,
      text: editForm.value.content,
      ownerId: post.value.ownerId,
      tags: editForm.value.tagIds.map(id => ({ id }))
    }

    await postsAPI.updatePost(post.value.id, postData)

    const updatedResponse = await postsAPI.getPostById(post.value.id)
    post.value = updatedResponse.data

    isEditing.value = false
    showNotification('Пост успешно обновлен', 'success')
  } catch (error: any) {
    console.error('Ошибка при обновлении поста:', error)
    const message = error.response?.data?.message || 'Не удалось обновить пост'
    showNotification(message, 'error')
  } finally {
    isSaving.value = false
  }
}

const confirmDelete = async () => {
  if (!post.value || !authStore.canDeletePost(post.value.ownerId)) {
    return
  }

  if (!confirm('Вы уверены, что хотите удалить этот пост? Это действие нельзя отменить.')) {
    return
  }

  isDeleting.value = true

  try {
    await postsAPI.deletePost(post.value.id)
    router.push('/posts')
    showNotification('Пост успешно удален', 'success')
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить пост'
    showNotification(errorMessage, 'error')
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
  border-top: 1px solid #2d2d2d;
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
  border: 1px solid #2d2d2d;
  box-shadow: none;
  background: #1a1a1a;
  transition: border-color 0.2s ease;
}

.comment-card:hover {
  border-color: #404040;
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

/* Основные стили для контейнера с контентом */
.post-content {
  font-size: 1rem;
  line-height: 1.6;
  color: #cccccc;
  margin-bottom: 20px;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

/* Исправляем слипание текста */
.post-content {
  white-space: normal;
  word-spacing: normal;
  text-rendering: optimizeLegibility;
  font-kerning: normal;
}

/* Глобальные стили для всех элементов внутри .post-content */
.post-content :deep(*) {
  max-width: 100%;
  word-spacing: normal;
  white-space: normal;
}

/* Заголовки */
.post-content :deep(h1),
.post-content :deep(h2),
.post-content :deep(h3),
.post-content :deep(h4),
.post-content :deep(h5),
.post-content :deep(h6) {
  color: #ffffff;
  font-weight: 600;
  margin-top: 1.2em;
  margin-bottom: 0.6em;
  line-height: 1.3;
}

.post-content :deep(h1) {
  font-size: 1.8em;
  border-bottom: 2px solid #404040;
  padding-bottom: 0.3em;
  margin-top: 0;
}

.post-content :deep(h2) {
  font-size: 1.5em;
  border-left: 4px solid #3b82f6;
  padding-left: 10px;
}

.post-content :deep(h3) {
  font-size: 1.3em;
}

.post-content :deep(h4) {
  font-size: 1.2em;
  color: #a5a5a5;
}

/* Параграфы */
.post-content :deep(p) {
  margin-bottom: 1em;
  line-height: 1.6;
  text-align: justify;
}

/* Списки */
.post-content :deep(ul),
.post-content :deep(ol) {
  padding-left: 1.5em;
  margin-bottom: 1em;
  margin-top: 0.5em;
}

.post-content :deep(li) {
  margin-bottom: 0.5em;
  padding-left: 0.5em;
}

.post-content :deep(ul li) {
  list-style-type: disc;
}

.post-content :deep(ol li) {
  list-style-type: decimal;
}

/* Цитаты */
.post-content :deep(blockquote) {
  border-left: 4px solid #404040;
  padding-left: 1em;
  margin: 1.5em 0;
  color: #a5a5a5;
  font-style: italic;
  background: #252525;
  padding: 1em;
  border-radius: 6px;
}

.post-content :deep(blockquote p) {
  margin-bottom: 0;
}

/* Жирный и курсив */
.post-content :deep(strong),
.post-content :deep(b) {
  font-weight: 700;
  color: #ffffff;
}

.post-content :deep(em),
.post-content :deep(i) {
  font-style: italic;
}

/* Ссылки */
.post-content :deep(a:not(.editor-file)) {
  color: #3b82f6;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s;
}

.post-content :deep(a:not(.editor-file):hover) {
  border-bottom-color: #3b82f6;
  color: #60a5fa;
}

/* Изображения */
.post-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1.2em 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border: 1px solid #404040;
}

/* Медиа-элементы */
.post-content :deep(iframe),
.post-content :deep(video),
.post-content :deep(audio) {
  width: 100%;
  max-width: 100%;
  margin: 1.5em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border: 1px solid #404040;
  background: #000;
}

/* Файлы */
.post-content :deep(a.editor-file) {
  display: inline-flex;
  align-items: center;
  padding: 8px 12px;
  background: #252525;
  border-radius: 6px;
  color: #ccc;
  text-decoration: none;
  border: 1px solid #333;
  margin: 0.5em 0;
  transition: all 0.2s;
}

.post-content :deep(a.editor-file:hover) {
  background: #303030;
  border-color: #404040;
}

/* Линии */
.post-content :deep(hr) {
  border: none;
  border-top: 2px solid #404040;
  margin: 2em 0;
}

/* Код */
.post-content :deep(code) {
  font-family: 'Courier New', Courier, monospace;
  background: #252525;
  padding: 2px 6px;
  border-radius: 4px;
  color: #f0f0f0;
  font-size: 0.9em;
}

.post-content :deep(pre) {
  background: #1a1a1a;
  padding: 1em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.2em 0;
  border: 1px solid #333;
}

.post-content :deep(pre code) {
  background: none;
  padding: 0;
  color: #f0f0f0;
  font-size: 0.9em;
  white-space: pre-wrap;
}

/* Таблицы */
.post-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5em 0;
  background: #252525;
  border-radius: 8px;
  overflow: hidden;
}

.post-content :deep(th),
.post-content :deep(td) {
  padding: 10px;
  border: 1px solid #404040;
  text-align: left;
}

.post-content :deep(th) {
  background: #2d2d2d;
  color: #ffffff;
  font-weight: 600;
}

/* Подчеркивание и зачеркивание */
.post-content :deep(u) {
  text-decoration: underline;
}

.post-content :deep(s) {
  text-decoration: line-through;
  color: #999;
}

/* Маркировка */
.post-content :deep(mark) {
  background: rgba(255, 255, 0, 0.2);
  color: #ffffff;
  padding: 2px 4px;
}

.post-meta {
  color: #a5a5a5;
  font-size: 0.9em;
  padding-top: 16px;
  border-top: 1px solid #404040;
}

.icon-text {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-right: 16px;
}

.icon-text:last-child {
  margin-right: 0;
}

.like-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background: none;
  border: none;
  color: #a5a5a5;
  cursor: pointer;
  transition: all 0.2s;
}

.like-button:hover:not(:disabled) {
  background: #252525;
  color: #ef4444;
}

.like-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.like-button .has-text-danger {
  color: #ef4444;
}

.comment-form .textarea {
  background: #252525;
  border-color: #404040;
  color: #cccccc;
}

.comment-form .textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 0.125em rgba(59, 130, 246, 0.25);
  background: #2d2d2d;
}

.comment-form .button.is-primary {
  background: #3b82f6;
  border-color: transparent;
  color: #fff;
}

.comment-form .button.is-primary:hover:not(:disabled) {
  background: #2563eb;
}

.notification.is-light {
  background: #252525;
  color: #cccccc;
  border: 1px solid #404040;
}

.title.is-4 {
  color: #ffffff;
  margin-bottom: 20px;
  font-weight: 600;
}

.card-header {
  background: linear-gradient(135deg, #2d2d2d 0%, #1a1a1a 100%);
  border-bottom: 1px solid #404040;
}

.card-header-title {
  color: #ffffff;
  font-weight: 600;
  font-size: 1.2rem;
}

.post-detail-card {
  background: #1a1a1a;
  border: 1px solid #2d2d2d;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.card-content {
  padding: 24px;
  background: #1a1a1a;
}

.card-footer {
  background: #252525;
  border-top: 1px solid #404040;
}

/* Адаптивность */
@media (max-width: 768px) {
  .post-content {
    font-size: 0.95em;
  }

  .post-content :deep(h1) {
    font-size: 1.5em;
  }

  .post-content :deep(h2) {
    font-size: 1.3em;
  }

  .post-content :deep(h3) {
    font-size: 1.2em;
  }

  .post-content :deep(ul),
  .post-content :deep(ol) {
    padding-left: 1.2em;
  }

  .card-content {
    padding: 16px;
  }

  .icon-text {
    margin-right: 12px;
    font-size: 0.85em;
  }

  .comment-form .textarea {
    font-size: 0.95em;
  }
}
</style>
