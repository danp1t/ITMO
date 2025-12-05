<template>
  <div class="posts-view">
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
              class="notification is-light is-small"
            >
              <p class="is-size-7">
                <i class="fas fa-lock mr-1"></i>
                Для создания постов требуется роль публикации
                <router-link to="/profile" class="has-text-primary ml-1">
                  (Обратитесь к администратору)
                </router-link>
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
            @like="handleLike"
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
                  <option value="likesCount">По лайкам</option>
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
        </div>
      </div>
    </div>

    <!-- Модальное окно создания поста -->
    <div class="modal" :class="{ 'is-active': showCreateModal }">
      <div class="modal-background" @click="showCreateModal = false"></div>
      <div class="modal-card">
        <header class="modal-card-head">
          <p class="modal-card-title">Создать пост</p>
          <button class="delete" @click="showCreateModal = false"></button>
        </header>

        <section class="modal-card-body">
          <div class="field">
            <label class="label">Заголовок</label>
            <div class="control">
              <input
                v-model="newPost.title"
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
                v-model="newPost.content"
                :disabled="isSaving"
                placeholder="Начните писать свой пост..."
              />
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button
            class="button is-primary"
            @click="createPost"
            :disabled="isSaving || !newPost.title.trim() || !newPost.content.trim()"
          >
            <span v-if="isSaving" class="icon">
              <i class="fas fa-spinner fa-spin"></i>
            </span>
            <span>{{ isSaving ? 'Публикация...' : 'Опубликовать' }}</span>
          </button>
          <button
            class="button"
            @click="showCreateModal = false"
            :disabled="isSaving"
          >
            Отмена
          </button>
        </footer>
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
            <label class="label">Содержание</label>
            <div class="control">
              <RichTextEditor
                v-model="editingPost.content"
                :disabled="isSaving"
                placeholder="Редактируйте содержимое поста..."
              />
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button
            class="button is-primary"
            @click="updatePost"
            :disabled="isSaving || !editingPost.title.trim() || !editingPost.content.trim()"
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
import { postsAPI } from '../api/posts'
import type { Post, UpdatePostRequest } from '../types/posts'

const authStore = useAuthStore()
const posts = ref<Post[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const showEditModal = ref(false)
const sortBy = ref('createdAt')
const sortDirection = ref('desc')
const isSaving = ref(false)

const newPost = ref({
  title: '',
  content: '',
})

// Данные для редактирования поста
const editingPost = ref({
  id: 0,
  title: '',
  content: '',
  ownerId: 0
})

const loadPosts = async () => {
  loading.value = true
  try {
    const params = {
      sortBy: sortBy.value,
      sortDirection: sortDirection.value,
    }
    const response = await postsAPI.getPosts(params)
    posts.value = response.data
  } catch (error) {
    console.error('Ошибка при загрузке постов:', error)
  } finally {
    loading.value = false
  }
}

const createPost = async () => {
  if (!authStore.user) return

  // Валидация
  if (!newPost.value.title.trim()) {
    alert('Введите заголовок поста')
    return
  }

  if (!newPost.value.content.trim() || newPost.value.content === '<p></p>') {
    alert('Введите содержание поста')
    return
  }

  try {
    const postData = {
      title: newPost.value.title,
      text: newPost.value.content, // Здесь text - это HTML контент
      ownerId: authStore.user.id,
    }

    await postsAPI.createPost(postData)
    showCreateModal.value = false
    newPost.value = { title: '', content: '' }
    await loadPosts()
  } catch (error) {
    console.error('Ошибка при создании поста:', error)
    alert('Не удалось создать пост')
  }
}

// Обработчик редактирования поста
const handleEdit = (post: Post) => {
  // Копируем данные поста в объект редактирования
  editingPost.value = {
    id: post.id,
    title: post.title || '',
    content: post.text || '', // Используем post.text, который содержит HTML
    ownerId: post.ownerId
  }
  showEditModal.value = true
}

// Функция обновления поста
const updatePost = async () => {
  if (!authStore.user) return

  // Валидация
  if (!editingPost.value.title.trim()) {
    alert('Введите заголовок поста')
    return
  }

  if (!editingPost.value.content.trim() || editingPost.value.content === '<p></p>') {
    alert('Введите текст поста')
    return
  }

  isSaving.value = true

  try {
    const postData: UpdatePostRequest = {
      title: editingPost.value.title,
      text: editingPost.value.content, // Отправляем HTML контент
      ownerId: editingPost.value.ownerId,
    }

    await postsAPI.updatePost(editingPost.value.id, postData)

    // Обновляем пост в списке
    const index = posts.value.findIndex(p => p.id === editingPost.value.id)
    if (index !== -1) {
      posts.value[index] = {
        ...posts.value[index],
        title: editingPost.value.title,
        text: editingPost.value.content,
        updatedAt: new Date().toISOString() // Обновляем дату редактирования
      }
    }

    closeEditModal()
  } catch (error: any) {
    console.error('Ошибка при обновлении поста:', error)
    const message = error.response?.data?.message || 'Не удалось обновить пост'
    alert(message)
  } finally {
    isSaving.value = false
  }
}

// Закрытие модального окна редактирования
const closeEditModal = () => {
  showEditModal.value = false
  // Сбрасываем форму редактирования
  editingPost.value = {
    id: 0,
    title: '',
    content: '',
    ownerId: 0
  }
}

// Обработчик удаления поста
const handleDelete = async (postId: number) => {
  try {
    await postsAPI.deletePost(postId)
    posts.value = posts.value.filter(p => p.id !== postId)
  } catch (error: any) {
    console.error('Ошибка при удалении поста:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить пост'
    alert(errorMessage)
  }
}

const handleLike = async (postId: number) => {
  try {
    await postsAPI.likePost(postId)
    await loadPosts()
  } catch (error) {
    console.error('Ошибка при оценке поста:', error)
  }
}

onMounted(loadPosts)

watch([sortBy, sortDirection], loadPosts)
</script>

<style scoped>
.posts-view {
  padding: 20px;
}
</style>
