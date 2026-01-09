<template>
  <article class="post-card card">
    <header class="card-header">
      <p class="card-header-title">
        {{ post.title }}
      </p>
      <div class="card-header-icons" v-if="authStore.isAuthenticated">
        <button
          v-if="authStore.canEditPost(post.ownerId)"
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

    <div v-if="postImage && !isExpanded" class="post-image-preview" @click="viewPost">
      <img :src="postImage" :alt="post.title" class="preview-image" />
      <div class="image-overlay">
        <i class="fas fa-eye"></i>
      </div>
    </div>

    <div class="card-content">
      <div class="post-content-wrapper">
        <div
          class="post-content-preview"
          :class="{ 'expanded': isExpanded }"
          v-html="processedHtml"
        ></div>

        <div v-if="!isExpanded && shouldShowGradient" class="content-gradient"></div>
      </div>

      <div v-if="hasLongContent" class="content-expand-buttons">
        <button
          v-if="!isExpanded"
          class="button is-text read-more-button"
          @click.stop="isExpanded = true"
        >
          <span>Читать далее</span>
          <i class="fas fa-chevron-down"></i>
        </button>
        <button
          v-else
          class="button is-text collapse-button"
          @click.stop="isExpanded = false"
        >
          <span>Свернуть</span>
          <i class="fas fa-chevron-up"></i>
        </button>
      </div>

      <div class="post-meta">
        <div class="tags" v-if="post.tags && post.tags.length > 0">
          <span
            v-for="tag in post.tags.slice(0, 3)"
            :key="tag.id"
            class="tag"
          >
            {{ tag.name }}
          </span>
          <span v-if="post.tags.length > 3" class="tag">
            +{{ post.tags.length - 3 }}
          </span>
        </div>

        <div class="meta-info">
          <div class="meta-item">
            <i class="fas fa-user"></i>
            <span>{{ post.ownerName }}</span>
          </div>

          <div class="meta-item">
            <i class="fas fa-calendar"></i>
            <span>{{ formatDate(post.createdAt) }}</span>
          </div>

          <div class="meta-item">
            <i class="fas fa-heart"></i>
            <span>{{ currentLikeCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <footer class="card-footer">
      <button
        class="card-footer-item like-button"
        @click="toggleLike"
        :disabled="isLiking || !authStore.isAuthenticated"
        :title="!authStore.isAuthenticated ? 'Войдите, чтобы поставить лайк' : ''"
      >
        <i class="fas fa-heart" :class="{ 'liked': isLikedByCurrentUser }"></i>
        <span>{{ isLikedByCurrentUser ? 'Лайкнуто' : 'Нравится' }}</span>
        <span class="action-count">{{ currentLikeCount }}</span>
        <span v-if="isLiking" class="spinner">
          <i class="fas fa-spinner fa-spin"></i>
        </span>
      </button>

      <router-link
        :to="`/posts/${post.id}`"
        class="card-footer-item"
        @click="viewPost"
      >
        <i class="fas fa-comment"></i>
        <span>Комментарии</span>
        <span class="action-count" v-if="post.commentCount > 0">
          {{ post.commentCount }}
        </span>
      </router-link>

      <button
        class="card-footer-item share-button"
        @click="sharePost"
        title="Поделиться"
      >
        <i class="fas fa-share-alt"></i>
        <span>Поделиться</span>
      </button>
    </footer>

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
          <div class="social-share-buttons">
            <button class="button is-primary copy-button" @click="copyToClipboard">
              <i class="fas fa-copy"></i>
              <span>Скопировать ссылку</span>
            </button>
          </div>
        </section>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch} from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { postsAPI } from '@/api/posts'
import type {Post, Tag} from '@/types/posts'

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

const isExpanded = ref(false)
const isLikedByCurrentUser = ref(false)
const isLiking = ref(false)
const isDeleting = ref(false)
const currentLikeCount = ref(props.post.countLike || 0)
const showShareModal = ref(false)

const extractFirstImage = (html: string): string | null => {
  try {
    const imgMatch = html.match(/<img[^>]+src="([^">]+)"/i)
    return imgMatch ? imgMatch[1] : null
  } catch (error) {
    console.error('Ошибка при извлечении изображения:', error)
    return null
  }
}

const postImage = computed(() => {
  const content = props.post.text || ''
  return extractFirstImage(content)
})

const processedHtml = computed(() => {
  let html = props.post.text || ''

  if (!isExpanded.value) {
    html = html.replace(/<img[^>]*>/gi, '')
    html = html.replace(/<(iframe|video|audio)[^>]*>.*?<\/\1>/gis, '')
    html = html.replace(/<a[^>]*class="editor-file"[^>]*>.*?<\/a>/gi, '')
    const textOnly = html.replace(/<[^>]*>/g, '')
    if (textOnly.length > 500) {
      return html.substring(0, 500) + '...'
    }
  }

  return html
})

const hasLongContent = computed(() => {
  const text = props.post.text || ''
  const textOnly = text.replace(/<[^>]*>/g, '')
  const hasMedia = text.includes('<img') || text.includes('<iframe') ||
    text.includes('<video') || text.includes('<audio')
  return textOnly.length > 500 || hasMedia
})

const shouldShowGradient = computed(() => {
  if (!isExpanded.value) {
    const text = props.post.text || ''
    const textOnly = text.replace(/<[^>]*>/g, '')
    return textOnly.length > 500
  }
  return false
})

const postUrl = computed(() => {
  return `${window.location.origin}/posts/${props.post.id}`
})

const shareUrlInput = ref<HTMLInputElement>()

const checkIfLiked = () => {
  isLikedByCurrentUser.value = false
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
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

    isLikedByCurrentUser.value = !wasLiked
    currentLikeCount.value = wasLiked ? oldCount - 1 : oldCount + 1

    await postsAPI.likePost(props.post.id)
    emit('like', props.post.id)

  } catch (error: any) {
    isLikedByCurrentUser.value = !isLikedByCurrentUser.value
    currentLikeCount.value = isLikedByCurrentUser.value
      ? currentLikeCount.value + 1
      : currentLikeCount.value - 1

    console.error('Ошибка при оценке поста:', error)
    alert(error.response?.data?.message || 'Не удалось поставить лайк')
  } finally {
    isLiking.value = false
  }
}

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
    alert(error.response?.data?.message || 'Не удалось удалить пост')
  } finally {
    isDeleting.value = false
  }
}

const viewPost = () => {
  router.push(`/posts/${props.post.id}`)
}

const sharePost = () => {
  showShareModal.value = true
}

const copyToClipboard = async () => {
  if (shareUrlInput.value) {
    shareUrlInput.value.select()
    try {
      await navigator.clipboard.writeText(postUrl.value)
      alert('Ссылка скопирована в буфер обмена!')
      showShareModal.value = false
    } catch (err) {
      document.execCommand('copy')
      alert('Ссылка скопирована в буфер обмена!')
      showShareModal.value = false
    }
  }
}

const selectShareUrl = () => {
  if (shareUrlInput.value) {
    shareUrlInput.value.select()
  }
}

watch(() => props.post.countLike, (newCount) => {
  currentLikeCount.value = newCount
})

watch(() => authStore.user, () => {
  checkIfLiked()
})

onMounted(() => {
  checkIfLiked()
})
</script>

<style scoped>
.post-card {
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #1a1a1a;
  border: 1px solid #2d2d2d;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.post-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
  border-color: #404040;
}

.card-header {
  background: linear-gradient(135deg, #2d2d2d 0%, #1a1a1a 100%);
  padding: 16px 20px;
  border-bottom: 1px solid #333;
}

.card-header-icons {
  display: flex;
  gap: 8px;
}

.card-header-icon {
  background: #333;
  border: none;
  color: #ccc;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.card-header-icon:hover {
  background: #404040;
  color: white;
}

.card-header-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.card-header-icon .spinner {
  margin-left: 4px;
}

.post-image-preview {
  position: relative;
  height: 500px;
  overflow: hidden;
  cursor: pointer;
  background: #121212;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
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

.image-overlay i {
  color: white;
  font-size: 2rem;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.card-content {
  padding: 20px;
}

.post-content-wrapper {
  position: relative;
  margin-bottom: 16px;
}

.post-content-preview {
  font-size: 1rem;
  line-height: 1.6;
  color: #cccccc;
  position: relative;
  max-height: 120px;
  overflow: hidden;
  transition: max-height 0.4s ease;
}

.post-content-preview.expanded {
  max-height: none;
  overflow: visible;
}

.content-gradient {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(to bottom, transparent, #1a1a1a);
  pointer-events: none;
}

:deep(.post-content-preview) {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

:deep(.post-content-preview h1),
:deep(.post-content-preview h2),
:deep(.post-content-preview h3) {
  color: #ffffff;
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.3;
}

:deep(.post-content-preview h1) {
  font-size: 1.5em;
  border-bottom: 2px solid #333;
  padding-bottom: 0.3em;
}

:deep(.post-content-preview h2) {
  font-size: 1.25em;
}

:deep(.post-content-preview h3) {
  font-size: 1.1em;
}

:deep(.post-content-preview p) {
  margin-bottom: 1em;
  text-align: justify;
}

:deep(.post-content-preview ul),
:deep(.post-content-preview ol) {
  padding-left: 1.5em;
  margin-bottom: 1em;
}

:deep(.post-content-preview li) {
  margin-bottom: 0.5em;
}

:deep(.post-content-preview blockquote) {
  border-left: 3px solid #404040;
  padding-left: 1em;
  margin: 1em 0;
  color: #999;
  font-style: italic;
  background: #252525;
  padding: 1em;
  border-radius: 4px;
}

:deep(.post-content-preview img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.post-content-preview) {
  margin: 1.5em 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

:deep(.post-content-preview .editor-iframe iframe) {
  border-radius: 8px;
}

:deep(.post-content-preview) {
  width: 100%;
  margin: 1em 0;
  border-radius: 8px;
  background: #252525;
}

:deep(.post-content-preview) {
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

:deep(.post-content-preview) {
  background: #303030;
  border-color: #404040;
}

.content-expand-buttons {
  display: flex;
  justify-content: center;
  margin: 12px 0;
}

.read-more-button,
.collapse-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #333;
  border: none;
  border-radius: 20px;
  color: #ccc;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.read-more-button:hover,
.collapse-button:hover {
  background: #404040;
  color: white;
}

.read-more-button i,
.collapse-button i {
  font-size: 0.9em;
}

.post-meta {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #333;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 16px;
}

.tag {
  background: #252525;
  color: #999;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85em;
  border: 1px solid #333;
  transition: all 0.2s;
}

.tag:hover {
  background: #303030;
  color: #ccc;
}

.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 0.9em;
}

.meta-item i {
  font-size: 0.9em;
  color: #666;
}

.meta-item:hover {
  color: #ccc;
}

.card-footer {
  border-top: 1px solid #333;
  background: #252525;
  display: flex;
  padding: 0;
}

.card-footer-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px;
  color: #999;
  text-decoration: none;
  border: none;
  background: none;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.9em;
  font-weight: 500;
  position: relative;
  border-right: 1px solid #333;
}

.card-footer-item:last-child {
  border-right: none;
}

.card-footer-item:hover:not(:disabled) {
  background: #303030;
  color: #ccc;
}

.card-footer-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.like-button:hover:not(:disabled) {
  color: #ef4444;
}

.like-button:hover:not(:disabled) i {
  color: #ef4444;
}

.like-button .liked {
  color: #ef4444 !important;
}

.action-count {
  background: #333;
  color: #999;
  font-size: 0.8em;
  padding: 2px 8px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
}

.spinner {
  margin-left: 4px;
}

.share-button:hover:not(:disabled) {
  color: #3b82f6;
}

.share-button:hover:not(:disabled) i {
  color: #3b82f6;
}

.modal-card {
  border-radius: 12px;
  overflow: hidden;
  background: #1a1a1a;
  color: #ccc;
}

.modal-card-head {
  background: #252525;
  border-bottom: 1px solid #333;
  color: #fff;
}

.modal-card-title {
  color: #fff;
}

.modal-card-body {
  padding: 24px;
}

.social-share-buttons {
  margin-top: 20px;
}

.copy-button {
  width: 100%;
  background: #3b82f6;
  border: none;
  color: white;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.2s;
}

.copy-button:hover {
  background: #2563eb;
}

@media (max-width: 768px) {
  .post-image-preview {
    height: 200px;
  }

  .card-content {
    padding: 16px;
  }

  .card-footer-item {
    padding: 12px 8px;
    font-size: 0.8em;
  }

  .meta-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .post-content-preview {
    font-size: 0.95em;
  }

  :deep(.post-content-preview h1) {
    font-size: 1.3em;
  }

  :deep(.post-content-preview h2) {
    font-size: 1.15em;
  }

  :deep(.post-content-preview h3) {
    font-size: 1em;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.post-card {
  animation: fadeIn 0.3s ease;
}
</style>
