<template>
  <div class="admin-view dark-theme">
    <div class="columns">
      <!-- Боковое меню -->
      <div class="column is-3 sidebar">
        <div class="admin-menu">
          <div class="menu-header">
            <div class="admin-avatar">
              <i class="fas fa-shield-alt"></i>
            </div>
            <div class="admin-info">
              <h3 class="has-text-light">Админ-панель</h3>
              <p class="has-text-grey-light">{{ authStore.user?.name }}</p>
            </div>
          </div>

          <nav class="menu">
            <ul class="menu-list">
              <li>
                <router-link
                  to="/admin/users"
                  class="menu-item"
                  :class="{ 'active': $route.path.includes('/admin/users') }"
                >
                  <i class="fas fa-users menu-icon"></i>
                  <span>Пользователи</span>
                </router-link>
              </li>
              <li v-if="authStore.canManageRoles">
                <router-link
                  to="/admin/roles"
                  class="menu-item"
                  :class="{ 'active': $route.path.includes('/admin/roles') }"
                >
                  <i class="fas fa-user-tag menu-icon"></i>
                  <span>Роли</span>
                </router-link>
              </li>
              <!-- В AdminView.vue добавьте новый пункт меню после комментариев -->
              <li v-if="authStore.canManageRoles">
                <router-link
                  to="/admin/orders"
                  class="menu-item"
                  :class="{ 'active': $route.path.includes('/admin/orders') }"
                >
                  <i class="fas fa-shopping-cart menu-icon"></i>
                  <span>Заказы</span>
                </router-link>
              </li>
            </ul>
          </nav>

          <div class="menu-footer">
            <button class="button is-dark is-fullwidth" @click="goBack">
              <i class="fas fa-arrow-left mr-2"></i>
              На главную
            </button>
          </div>
        </div>
      </div>

      <!-- Основной контент -->
      <div class="column is-9 main-content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const pageTitle = computed(() => {
  const titles: { [key: string]: string } = {
    '/admin/roles': 'Управление ролями',
    '/admin/posts': 'Модерация постов',
    '/admin/comments': 'Модерация комментариев'
  }

  return titles[route.path] || 'Админ-панель'
})

const refreshPage = () => {
  window.location.reload()
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.admin-view.dark-theme {
  background-color: #121212;
  min-height: 100vh;
}

.columns {
  margin: 0;
  min-height: 100vh;
}

/* Боковое меню */
.sidebar {
  background: linear-gradient(180deg, #1a1a1a 0%, #2d2d2d 100%);
  border-right: 1px solid #333;
  padding: 0;
}

.admin-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu-header {
  padding: 30px 20px;
  border-bottom: 1px solid #333;
  text-align: center;
}

.admin-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin: 0 auto 15px;
}

.admin-info h3 {
  font-size: 1.2rem;
  margin-bottom: 5px;
}

.admin-info p {
  font-size: 0.9rem;
  color: #aaa;
}

/* Навигационное меню */
.menu {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.menu-list {
  list-style: none;
  padding: 0;
}

.menu-list li {
  margin-bottom: 5px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 15px;
  border-radius: 8px;
  color: #aaa;
  text-decoration: none;
  transition: all 0.3s ease;
}

.menu-item:hover {
  background: #2d2d2d;
  color: white;
}

.menu-item.active {
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  color: white;
}

.menu-icon {
  width: 20px;
  text-align: center;
}

.menu-footer {
  padding: 20px;
  border-top: 1px solid #333;
}

/* Основной контент */
.main-content {
  padding: 30px;
  background: #121212;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #333;
}

.content-header .title {
  font-size: 1.8rem;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 10px;
}

.content-wrapper {
  background: #1a1a1a;
  border-radius: 8px;
  border: 1px solid #333;
  min-height: 500px;
  overflow: hidden;
}

/* Адаптивность */
@media (max-width: 768px) {
  .columns {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #333;
  }

  .main-content {
    padding: 20px;
  }

  .content-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .content-header .title {
    font-size: 1.5rem;
  }
}
</style>
