<template>
  <article class="post-card card">
    <!-- Заголовок поста -->
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
          @click="deletePost"
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

    <!-- Превью изображение -->
    <div v-if="postImage" class="post-image-preview" @click="viewPost">
      <img :src="postImage" :alt="post.title" class="preview-image" />
      <div class="image-overlay">
        <span class="icon">
          <i class="fas fa-eye"></i>
        </span>
      </div>
    </div>

    <!-- Содержимое поста -->
    <div class="card-content">
      <!-- HTML контент с ограничением по высоте -->
      <div
        class="post-content-preview"
        :class="{ 'expanded': isExpanded }"
        v-html="safeHtml"
      ></div>

      <!-- Кнопка "Читать далее" если контент длинный -->
      <button
        v-if="hasLongContent && !isExpanded"
        class="button is-text read-more-button"
        @click.stop="isExpanded = true"
      >
        <span>Читать далее</span>
        <span class="icon">
          <i class="fas fa-chevron-down"></i>
        </span>
      </button>

      <button
        v-if="isExpanded"
        class="button is-text collapse-button"
        @click.stop="isExpanded = false"
      >
        <span>Свернуть</span>
        <span class="icon">
          <i class="fas fa-chevron-up"></i>
        </span>
      </button>

      <!-- Мета-информация -->
      <div class="post-meta">
        <div class="tags" v-if="post.tags && post.tags.length > 0">
          <span
            v-for="tag in post.tags.slice(0, 3)"
            :key="tag.id"
            class="tag is-light is-small"
          >
            {{ tag.name }}
          </span>
          <span v-if="post.tags.length > 3" class="tag is-light is-small">
            +{{ post.tags.length - 3 }}
          </span>
        </div>

        <div class="meta-info">
          <small class="has-text-grey">
            <span class="icon-text ml-3">
              <span class="icon">
                <i class="fas fa-user"></i>
              </span>
              <span>{{ post.ownerName }}</span>
            </span>

            <span class="icon-text ml-3">
              <span class="icon">
                <i class="fas fa-calendar"></i>
              </span>
              <span>{{ formatDate(post.createdAt) }}</span>
            </span>

            <span class="icon-text ml-3">
              <span class="icon">
                <i class="fas fa-heart"></i>
              </span>
              <span>{{ currentLikeCount }}</span>
            </span>

          </small>
        </div>
      </div>
    </div>

    <!-- Футер с действиями -->
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
        <span>{{ isLikedByCurrentUser ? 'Лайкнуто' : 'Нравится' }}</span>
        <span class="like-count">{{ currentLikeCount }}</span>
        <span v-if="isLiking" class="icon">
          <i class="fas fa-spinner fa-spin ml-2"></i>
        </span>
      </button>

      <router-link
        :to="`/posts/${post.id}`"
        class="card-footer-item"
        @click="viewPost"
      >
        <span class="icon">
          <i class="fas fa-comment"></i>
        </span>
        <span>Комментарии</span>
        <span class="comment-count" v-if="post.commentCount > 0">
          {{ post.commentCount }}
        </span>
      </router-link>

      <button
        class="card-footer-item share-button"
        @click="sharePost"
        title="Поделиться"
      >
        <span class="icon">
          <i class="fas fa-share-alt"></i>
        </span>
        <span>Поделиться</span>
      </button>
    </footer>

    <!-- Модальное окно для общего доступа -->
    <div class="modal" :class="{ 'is-active': showShareModal }">
      <div class="modal-background" @click="showShareModal = false"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">Поделиться постом</p>
          <button class="delete" @click="showShareModal = false"></button>
        </header>
        <section class="modal-card-body">
          <div class="field">
            <label class="label">Ссылка на пост</label>
            <div class="control">
              <input
                ref="shareUrlInput"
                class="input"
                type="text"
                readonly
                :value="postUrl"
                @click="selectShareUrl"
              >
            </div>
            <p class="help">Нажмите на поле, чтобы скопировать ссылку</p>
          </div>
          <div class="social-share-buttons mt-4">
            <button class="button is-primary is-fullwidth" @click="copyToClipboard">
              <span class="icon">
                <i class="fas fa-copy"></i>
              </span>
              <span>Скопировать ссылку</span>
            </button>
          </div>
        </section>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { postsAPI } from '@/api/posts'
import type { Post, Tag } from '@/types/posts'

const props = defineProps<{
  post: Post
}>()

const emit = defineEmits<{
  edit: [post: Post]
  like: [postId: number]
  delete: [postId: number]
}>()

const router = useRouter()
const authStore = useAuthStore()

// Реактивные состояния
const isExpanded = ref(false)
const isLikedByCurrentUser = ref(false)
const isLiking = ref(false)
const isDeleting = ref(false)
const currentLikeCount = ref(props.post.countLike || 0)
const showShareModal = ref(false)

// Получаем первое изображение из HTML контента
const postImage = computed(() => {
  const content = props.post.text || ''
  const imgMatch = content.match(/<img[^>]+src="([^">]+)"/)
  return imgMatch ? imgMatch[1] : null
})

// Безопасный HTML для отображения
const safeHtml = computed(() => {
  let html = props.post.text || ''

  // Ограничиваем изображения в превью
  if (!isExpanded.value) {
    html = html.replace(/<img[^>]*>/g, '')
  }

  // Ограничиваем количество символов в превью
  if (!isExpanded.value) {
    const textOnly = html.replace(/<[^>]*>/g, '')
    if (textOnly.length > 500) {
      // Находим место для обрезки, чтобы не обрезать середину тега
      const truncated = textOnly.substring(0, 500)
      // Возвращаем с многоточием
      return html.substring(0, truncated.length) + '...'
    }
  }

  return html
})

// Проверяем, длинный ли контент
const hasLongContent = computed(() => {
  const text = props.post.text || ''
  const textOnly = text.replace(/<[^>]*>/g, '')
  return textOnly.length > 500 || text.includes('<img')
})

// URL для шаринга
const postUrl = computed(() => {
  return `${window.location.origin}/posts/${props.post.id}`
})

// Получаем ссылку на input для копирования
const shareUrlInput = ref<HTMLInputElement>()

// Проверяем, лайкал ли пользователь этот пост
const checkIfLiked = () => {
  // В реальном приложении эту информацию должен возвращать сервер
  // Сейчас просто сбрасываем состояние
  isLikedByCurrentUser.value = false
}

// Форматирование даты
const formatDate = (dateString: string, timeOnly = false) => {
  const date = new Date(dateString)

  if (timeOnly) {
    return date.toLocaleTimeString('ru-RU', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'только что'
  if (diffMins < 60) return `${diffMins} мин. назад`
  if (diffHours < 24) return `${diffHours} ч. назад`
  if (diffDays < 7) return `${diffDays} дн. назад`

  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: diffDays > 365 ? 'numeric' : undefined
  })
}

// Лайк поста
const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    alert('Для оценки постов необходимо войти в систему')
    return
  }

  if (isLiking.value) return

  isLiking.value = true

  try {
    const wasLiked = isLikedByCurrentUser.value
    const oldCount = currentLikeCount.value

    // Оптимистичное обновление UI
    isLikedByCurrentUser.value = !wasLiked
    currentLikeCount.value = wasLiked ? oldCount - 1 : oldCount + 1

    // Отправляем запрос на сервер
    await postsAPI.likePost(props.post.id)

    // Эмитируем событие
    emit('like', props.post.id)

  } catch (error: any) {
    // Откатываем изменения в случае ошибки
    isLikedByCurrentUser.value = !isLikedByCurrentUser.value
    currentLikeCount.value = isLikedByCurrentUser.value
      ? currentLikeCount.value + 1
      : currentLikeCount.value - 1

    console.error('Ошибка при оценке поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось поставить лайк'
    alert(errorMessage)
  } finally {
    isLiking.value = false
  }
}

// Удаление поста
const deletePost = async () => {
  if (!authStore.isAuthenticated || !authStore.canDeletePost(props.post.ownerId)) {
    return
  }

  if (isDeleting.value) return

  isDeleting.value = true

  try {
    emit('delete', props.post.id)
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить пост'
    alert(errorMessage)
  } finally {
    isDeleting.value = false
  }
}

// Переход к просмотру поста
const viewPost = () => {
  router.push(`/posts/${props.post.id}`)
}

// Поделиться постом
const sharePost = () => {
  showShareModal.value = true
}

// Копировать ссылку в буфер обмена
const copyToClipboard = async () => {
  if (shareUrlInput.value) {
    shareUrlInput.value.select()
    try {
      await navigator.clipboard.writeText(postUrl.value)
      alert('Ссылка скопирована в буфер обмена!')
      showShareModal.value = false
    } catch (err) {
      // Fallback для старых браузеров
      document.execCommand('copy')
      alert('Ссылка скопирована в буфер обмена!')
      showShareModal.value = false
    }
  }
}

// Выделить URL при клике
const selectShareUrl = () => {
  if (shareUrlInput.value) {
    shareUrlInput.value.select()
  }
}

// Наблюдатели
watch(() => props.post.countLike, (newCount) => {
  currentLikeCount.value = newCount
})

watch(() => authStore.user, () => {
  checkIfLiked()
})

// Инициализация при монтировании
onMounted(() => {
  checkIfLiked()
})
</script>

<style scoped>
.post-card {
  margin-bottom: 24px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #121111;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.post-card:hover {
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  border-color: #d1d5db;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-header-title {
  color: white;
  margin: 0;
  font-weight: 600;
  font-size: 1.25rem;
  flex-grow: 1;
  line-height: 1.4;
}

.card-header-icons {
  display: flex;
  align-items: center;
}

.card-header-icon {
  background: rgba(255, 255, 255, 0.1);
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

/* Превью изображения */
.post-image-preview {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
  background: #f3f4f6;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.post-image-preview:hover .preview-image {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.post-image-preview:hover .image-overlay {
  opacity: 1;
}

.image-overlay .icon {
  color: white;
  font-size: 2rem;
}

/* Контент поста */
.card-content {
  padding: 20px;
}

.post-content-preview {
  font-size: 1rem;
  line-height: 1.6;
  color: #d0d8e3;
  max-height: 120px;
  overflow: hidden;
  position: relative;
  transition: max-height 0.3s ease;
}

.post-content-preview.expanded {
  max-height: none;
}

.post-content-preview::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  opacity: 1;
  transition: opacity 0.3s ease;
}

.post-content-preview.expanded::after {
  opacity: 0;
}

/* Стили для HTML контента внутри preview */
.post-content-preview :deep(h1) {
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1rem 0 0.5rem;
  color: #c0c6cd;
}

.post-content-preview :deep(h2) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0.875rem 0 0.5rem;
  color: #dedfe3;
}

.post-content-preview :deep(h3) {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0.75rem 0 0.5rem;
  color: #cdcfd5;
}

.post-content-preview :deep(p) {
  margin: 0 0 1rem;
}

.post-content-preview :deep(ul),
.post-content-preview :deep(ol) {
  padding-left: 1.5rem;
  margin: 0 0 1rem;
}

.post-content-preview :deep(blockquote) {
  border-left: 3px solid #e5e7eb;
  padding-left: 1rem;
  margin: 1rem 0;
  color: #6b7280;
  font-style: italic;
}

.post-content-preview :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1rem 0;
}

/* Кнопки "Читать далее" и "Свернуть" */
.read-more-button,
.collapse-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  font-weight: 500;
  color: #667eea;
}

.read-more-button:hover,
.collapse-button:hover {
  color: #764ba2;
}

/* Мета-информация */
.post-meta {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.tag {
  font-size: 0.75rem;
  background: #f3f4f6;
  color: #6b7280;
  border: none;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.icon-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
}

.icon-text .icon {
  font-size: 0.75rem;
  color: #9ca3af;
}

/* Футер поста */
.card-footer {
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  padding: 0;
}

.card-footer-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 12px;
  color: #6b7280;
  text-decoration: none;
  border: none;
  background: none;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
  font-weight: 500;
  position: relative;
}

.card-footer-item:hover:not(:disabled) {
  background: #f3f4f6;
  color: #374151;
}

.card-footer-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.like-button:hover:not(:disabled) {
  color: #ef4444;
}

.like-button:hover:not(:disabled) .icon {
  color: #ef4444;
}

.has-text-danger {
  color: #ef4444 !important;
}

.like-count,
.comment-count {
  background: #e5e7eb;
  color: #6b7280;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 4px;
  min-width: 20px;
  text-align: center;
}

.share-button:hover:not(:disabled) {
  color: #667eea;
}

.share-button:hover:not(:disabled) .icon {
  color: #667eea;
}

/* Модальное окно шаринга */
.modal-card {
  border-radius: 12px;
  overflow: hidden;
}

.modal-card-body {
  padding: 24px;
}

.social-share-buttons {
  display: flex;
  gap: 12px;
}

.fa-spinner {
  font-size: 0.8em;
}

/* Адаптивность */
@media (max-width: 768px) {
  .card-header-title {
    font-size: 1.125rem;
  }

  .post-image-preview {
    height: 160px;
  }

  .card-content {
    padding: 16px;
  }

  .card-footer-item {
    padding: 12px 8px;
    font-size: 0.75rem;
  }

  .meta-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .icon-text {
    margin: 0 !important;
  }
}
</style>
