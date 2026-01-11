<template>
  <div class="posts-view">
    <!-- Компонент уведомлений -->
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

    <div class="columns">
      <div class="column is-three-quarters">
        <div class="level">
          <div class="level-left">
            <h1 class="title">Лента постов</h1>
          </div>
          <div class="level-right">
            <button
              v-if="authStore.isAuthenticated && authStore.canPublishPosts()"
              class="button is-primary"
              @click="showCreateModal = true"
            >
              <span class="icon">
                <i class="fas fa-plus"></i>
              </span>
              <span>Создать пост</span>
            </button>

            <div
              v-else-if="authStore.isAuthenticated && !authStore.canPublishPosts()"
              class="notification is-dark is-small"
            >
              <p class="is-size-7">
                <i class="fas fa-lock mr-1"></i>
                Для создания постов требуется роль публикации
                <a href="https://t.me/danp1t" class="has-text-primary ml-1">
                  (Обратитесь к администратору)
                </a>
              </p>
            </div>

            <div v-else class="notification is-light is-small">
              <p class="is-size-7">
                <i class="fas fa-sign-in-alt mr-1"></i>
                <router-link to="/login" class="has-text-primary">
                  Войдите в систему
                </router-link>
                , чтобы создавать посты
              </p>
            </div>
          </div>
        </div>

        <div v-if="loading" class="has-text-centered">
          <i class="fas fa-spinner fa-spin fa-2x"></i>
          <p>Загрузка постов...</p>
        </div>

        <div v-else-if="posts.length === 0" class="has-text-centered">
          <p class="is-size-4">Постов пока нет</p>
          <p class="is-size-6">Будьте первым, кто поделится новостью!</p>
        </div>

        <div v-else>
          <PostCard
            v-for="post in posts"
            :key="post.id"
            :post="post"
            @edit="handleEdit"
            @delete="handleDelete"
          />
        </div>
      </div>

      <div class="column">
        <div class="box">
          <h2 class="subtitle">Фильтры</h2>

          <div class="field">
            <label class="label">Сортировка</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="sortBy">
                  <option value="">По умолчанию</option>
                  <option value="createdAt">По дате</option>
                  <option value="countLike">По лайкам</option>
                </select>
              </div>
            </div>
          </div>

          <div class="field">
            <label class="label">Направление</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="sortDirection">
                  <option value="desc">По убыванию</option>
                  <option value="asc">По возрастанию</option>
                </select>
              </div>
            </div>
          </div>

          <TagFilter
            v-if="allTags.length > 0"
            :tags="allTags"
            :selectedTagIds="selectedTagIds"
            @tag-toggle="toggleTagFilter"
            @clear="clearTagFilters"
          />
        </div>
      </div>
    </div>

    <div class="modal" :class="{ 'is-active': showCreateModal }">
      <div class="modal-background" @click="closeCreateModal"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">Создать пост</p>
          <button class="delete" @click="closeCreateModal"></button>
        </header>

        <section class="modal-card-body">
          <div class="field">
            <div class="level">
              <div class="level-left">
                <label class="label">Заголовок</label>
              </div>
              <div class="level-right">
                <span
                  class="is-size-7"
                  :class="{'has-text-danger': newPost.title.length > MAX_TITLE_LENGTH, 'has-text-grey': newPost.title.length <= MAX_TITLE_LENGTH}"
                >
                  {{ newPost.title.length }}/{{ MAX_TITLE_LENGTH }}
                </span>
              </div>
            </div>
            <div class="control">
              <input
                v-model="newPost.title"
                class="input"
                type="text"
                :maxlength="MAX_TITLE_LENGTH"
                placeholder="Введите заголовок"
                :disabled="isSaving"
                :class="{'is-danger': newPost.title.length > MAX_TITLE_LENGTH}"
              >
            </div>
            <p v-if="newPost.title.length > MAX_TITLE_LENGTH" class="help is-danger">
              Заголовок слишком длинный. Максимальная длина: {{ MAX_TITLE_LENGTH }} символов
            </p>
          </div>

          <div class="field">
            <label class="label">Содержание</label>
            <div class="control">
              <RichTextEditor
                ref="editorRef"
                v-model="newPost.content"
                :disabled="isSaving"
                :isCreating="true"
                placeholder="Начните писать свой пост..."
                @file-upload-error="handleFileUploadError"
                @files-uploaded="handleFilesUploaded"
                @tags-change="handleNewPostTagsChange"
              />
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button
            class="button is-primary"
            @click="createPostWithAttachments"
            :disabled="isSaving || !newPost.title.trim() || !newPost.content.trim() || newPost.title.length > MAX_TITLE_LENGTH"
          >
            <span v-if="isSaving" class="icon">
              <i class="fas fa-spinner fa-spin"></i>
            </span>
            <span>{{ isSaving ? 'Публикация...' : 'Опубликовать' }}</span>
          </button>
          <button
            class="button"
            @click="closeCreateModal"
            :disabled="isSaving"
          >
            Отмена
          </button>
        </footer>
      </div>
    </div>

    <div class="modal" :class="{ 'is-active': showEditModal }">
      <div class="modal-background" @click="closeEditModal"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">Редактировать пост</p>
          <button class="delete" @click="closeEditModal"></button>
        </header>

        <section class="modal-card-body">
          <div class="field">
            <div class="level">
              <div class="level-left">
                <label class="label">Заголовок</label>
              </div>
              <div class="level-right">
                <span
                  class="is-size-7"
                  :class="{'has-text-danger': editingPost.title.length > MAX_TITLE_LENGTH, 'has-text-grey': editingPost.title.length <= MAX_TITLE_LENGTH}"
                >
                  {{ editingPost.title.length }}/{{ MAX_TITLE_LENGTH }}
                </span>
              </div>
            </div>
            <div class="control">
              <input
                v-model="editingPost.title"
                class="input"
                type="text"
                :maxlength="MAX_TITLE_LENGTH"
                placeholder="Введите заголовок"
                :disabled="isSaving"
                :class="{'is-danger': editingPost.title.length > MAX_TITLE_LENGTH}"
              >
            </div>
            <p v-if="editingPost.title.length > MAX_TITLE_LENGTH" class="help is-danger">
              Заголовок слишком длинный. Максимальная длина: {{ MAX_TITLE_LENGTH }} символов
            </p>
          </div>

          <div class="field">
            <label class="label">Содержание</label>
            <div class="control">
              <RichTextEditor
                ref="editEditorRef"
                v-model="editingPost.content"
                :disabled="isSaving"
                :postId="editingPost.id"
                :showTagSelector="true"
                :initialTags="editingPost.tagIds"
                @tags-change="handleEditPostTagsChange"
                placeholder="Редактируйте содержимое поста..."
                @file-upload-error="handleFileUploadError"
              />
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button
            class="button is-primary"
            @click="updatePost"
            :disabled="isSaving || !editingPost.title.trim() || !editingPost.content.trim() || editingPost.title.length > MAX_TITLE_LENGTH"
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
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/posts/PostCard.vue'
import RichTextEditor from '../components/posts/RichTextEditor.vue'
import TagFilter from '../components/posts/TagFilter.vue'
import { postsAPI } from '../api/posts'
import type { Post, UpdatePostRequest, Tag } from '../types/posts'
import { tagsAPI } from '../api/tags'
import AppNotification from '@/components/AppNotification.vue'

const authStore = useAuthStore()
const posts = ref<Post[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const sortBy = ref('createdAt')
const sortDirection = ref('desc')
const isSaving = ref(false)

const MAX_TITLE_LENGTH = 100

interface NotificationState {
  visible: boolean
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

const notification = ref<NotificationState>({
  visible: false,
  message: '',
  type: 'info'
})

const allTags = ref<Tag[]>([])
const selectedTagIds = ref<number[]>([])

const editorRef = ref<InstanceType<typeof RichTextEditor>>()
const editEditorRef = ref<InstanceType<typeof RichTextEditor>>()

const pendingFiles = ref<Array<{file: File, type: 'image' | 'file' | 'audio'}>>([])

const newPost = ref({
  title: '',
  content: '',
  tagIds: [] as number[]
})

const editingPost = ref({
  id: 0,
  title: '',
  content: '',
  ownerId: 0,
  tagIds: [] as number[]
})

const loadPosts = async () => {
  loading.value = true
  try {
    if (selectedTagIds.value.length > 0) {
      const allPosts: Post[] = []
      const uniquePostIds = new Set<number>()

      for (const tagId of selectedTagIds.value) {
        const response = await postsAPI.getPostsByTag(tagId)
        const taggedPosts = response.data || []

        for (const post of taggedPosts) {
          if (!uniquePostIds.has(post.id)) {
            uniquePostIds.add(post.id)
            allPosts.push(post)
          }
        }
      }

      posts.value = sortPosts(allPosts)
    } else {
      const params = {
        sortBy: sortBy.value,
        sortDirection: sortDirection.value,
      }
      const response = await postsAPI.getPosts(params)
      posts.value = response.data
    }
  } catch (error) {
    console.error('Ошибка при загрузке постов:', error)
    showNotification('Ошибка при загрузке постов', 'error')
  } finally {
    loading.value = false
  }
}

const sortPosts = (postsToSort: Post[]) => {
  const sorted = [...postsToSort]

  switch (sortBy.value) {
    case 'createdAt':
      sorted.sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime()
        const dateB = new Date(b.createdAt).getTime()
        return sortDirection.value === 'asc' ? dateA - dateB : dateB - dateA
      })
      break

    case 'likesCount':
      sorted.sort((a, b) => {
        const likesA = a.countLike || 0
        const likesB = b.countLike || 0
        return sortDirection.value === 'asc' ? likesA - likesB : likesB - likesA
      })
      break

    default:
      sorted.sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime()
        const dateB = new Date(b.createdAt).getTime()
        return dateB - dateA
      })
  }

  return sorted
}

const loadAllTags = async () => {
  try {
    const response = await tagsAPI.getAllTags()
    allTags.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке тегов:', error)
    showNotification('Ошибка при загрузке тегов', 'error')
  }
}

const toggleTagFilter = (tagId: number) => {
  const index = selectedTagIds.value.indexOf(tagId)
  if (index > -1) {
    selectedTagIds.value.splice(index, 1)
  } else {
    selectedTagIds.value.push(tagId)
  }
  loadPosts()
}

const clearTagFilters = () => {
  selectedTagIds.value = []
  loadPosts()
}

const handleFileUploadError = (error: string) => {
  showNotification(error, 'error')
}

const handleFilesUploaded = (files: Array<{file: File, type: 'image' | 'file' | 'audio'}>) => {
  pendingFiles.value = files
}

const handleNewPostTagsChange = (tagIds: number[]) => {
  newPost.value.tagIds = tagIds
}

const handleEditPostTagsChange = (tagIds: number[]) => {
  editingPost.value.tagIds = tagIds
}

const createPostWithAttachments = async () => {
  if (!authStore.user) return

  if (!newPost.value.title.trim()) {
    showNotification('Введите заголовок поста', 'warning')
    return
  }

  if (newPost.value.title.length > MAX_TITLE_LENGTH) {
    showNotification(`Заголовок не должен превышать ${MAX_TITLE_LENGTH} символов`, 'warning')
    return
  }

  if (!newPost.value.content.trim() || newPost.value.content === '<p></p>') {
    showNotification('Введите содержание поста', 'warning')
    return
  }

  isSaving.value = true

  try {
    const tagObjects = newPost.value.tagIds.map(id => ({
      id: id,
      name: '',
      description: ''
    }))

    const postData = {
      title: newPost.value.title,
      text: newPost.value.content,
      ownerId: authStore.user.id,
      tags: tagObjects
    }

    const createResponse = await postsAPI.createPost(postData)
    const newPostId = createResponse.data.id

    if (editorRef.value?.hasPendingFiles && newPostId) {
      const uploadedFiles = await editorRef.value.uploadPendingFiles(newPostId)

      if (uploadedFiles.length > 0) {
        const updatedContent = editorRef.value.replacePlaceholdersInContent(
          newPost.value.content,
          uploadedFiles
        )

        const updateData: UpdatePostRequest = {
          title: newPost.value.title,
          text: updatedContent,
          ownerId: authStore.user.id,
          tags: tagObjects
        }

        await postsAPI.updatePost(newPostId, updateData)
      }
    }

    showCreateModal.value = false
    newPost.value = { title: '', content: '', tagIds: [] }
    pendingFiles.value = []

    if (editorRef.value) {
      editorRef.value.clearPendingFiles()
    }

    await loadPosts()
    showNotification('Пост успешно создан!', 'success')

  } catch (error: any) {
    console.error('Ошибка при создании поста с вложениями:', error)
    const message = error.response?.data?.message || 'Не удалось создать пост'
    showNotification(message, 'error')
  } finally {
    isSaving.value = false
  }
}

const closeCreateModal = () => {
  if (isSaving.value) {
    isSaving.value = false
    return
  }

  showCreateModal.value = false
  newPost.value = { title: '', content: '', tagIds: [] }
  pendingFiles.value = []

  if (editorRef.value) {
    editorRef.value.clearPendingFiles()
    editorRef.value.clear()
    editorRef.value.clearTags()
  }
}

const handleEdit = (post: Post) => {
  editingPost.value = {
    id: post.id,
    title: post.title || '',
    content: post.text || '',
    ownerId: post.ownerId,
    tagIds: post.tags?.map(tag => tag.id) || []
  }
  showEditModal.value = true
}

const updatePost = async () => {
  if (!authStore.user) return

  if (!editingPost.value.title.trim()) {
    showNotification('Введите заголовок поста', 'warning')
    return
  }

  if (editingPost.value.title.length > MAX_TITLE_LENGTH) {
    showNotification(`Заголовок не должен превышать ${MAX_TITLE_LENGTH} символов`, 'warning')
    return
  }

  if (!editingPost.value.content.trim() || editingPost.value.content === '<p></p>') {
    showNotification('Введите текст поста', 'warning')
    return
  }

  isSaving.value = true

  try {
    const tagObjects = editingPost.value.tagIds.map(id => ({
      id: id,
      name: '',
      description: ''
    }))

    const postData: UpdatePostRequest = {
      title: editingPost.value.title,
      text: editingPost.value.content,
      ownerId: editingPost.value.ownerId,
      tags: tagObjects
    }

    await postsAPI.updatePost(editingPost.value.id, postData)

    const index = posts.value.findIndex(p => p.id === editingPost.value.id)
    if (index !== -1) {
      const updatedResponse = await postsAPI.getPostById(editingPost.value.id)
      posts.value[index] = updatedResponse.data
    }

    closeEditModal()
    showNotification('Пост успешно обновлен!', 'success')
  } catch (error: any) {
    console.error('Ошибка при обновлении поста:', error)
    const message = error.response?.data?.message || 'Не удалось обновить пост'
    showNotification(message, 'error')
  } finally {
    isSaving.value = false
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingPost.value = {
    id: 0,
    title: '',
    content: '',
    ownerId: 0,
    tagIds: []
  }

  if (editEditorRef.value) {
    editEditorRef.value.clearPendingFiles()
  }
}

const handleDelete = async (postId: number) => {
  try {
    await postsAPI.deletePost(postId)
    posts.value = posts.value.filter(p => p.id !== postId)
    showNotification('Пост успешно удален!', 'success')
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить пост'
    showNotification(errorMessage, 'error')
  }
}

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  notification.value = {
    visible: true,
    message,
    type
  }

  // Автоматически скрыть уведомление через 5 секунд
  setTimeout(() => {
    notification.value.visible = false
  }, 5000)
}

const hideNotification = () => {
  notification.value.visible = false
}

onMounted(() => {
  loadPosts()
  loadAllTags()
})

watch([sortBy, sortDirection], loadPosts)
</script>

<style scoped>
.posts-view {
  padding: 20px;
}
</style>
