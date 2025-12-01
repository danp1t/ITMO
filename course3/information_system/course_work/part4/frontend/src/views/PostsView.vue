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
              v-if="authStore.isAuthenticated"
              class="button is-primary"
              @click="showCreateModal = true"
            >
              <span class="icon">
                <i class="fas fa-plus"></i>
              </span>
              <span>Создать пост</span>
            </button>
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
              >
            </div>
          </div>

          <div class="field">
            <label class="label">Текст</label>
            <div class="control">
              <textarea
                v-model="newPost.text"
                class="textarea"
                placeholder="Введите текст поста"
                rows="6"
              ></textarea>
            </div>
          </div>
        </section>

        <footer class="modal-card-foot">
          <button class="button is-primary" @click="createPost">
            Опубликовать
          </button>
          <button class="button" @click="showCreateModal = false">
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
import { postsAPI } from '../api/posts'
import type { Post } from '../types/posts'

const authStore = useAuthStore()
const posts = ref<Post[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const sortBy = ref('createdAt')
const sortDirection = ref('desc')

const newPost = ref({
  title: '',
  text: '',
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

  try {
    const postData = {
      ...newPost.value,
      ownerId: authStore.user.id,
    }

    await postsAPI.createPost(postData)
    showCreateModal.value = false
    newPost.value = { title: '', text: '' }
    await loadPosts()
  } catch (error) {
    console.error('Ошибка при создании поста:', error)
    alert('Не удалось создать пост')
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

const handleEdit = (post: Post) => {
  // Реализовать редактирование поста
  console.log('Редактировать пост:', post)
}

onMounted(loadPosts)

watch([sortBy, sortDirection], loadPosts)
</script>
