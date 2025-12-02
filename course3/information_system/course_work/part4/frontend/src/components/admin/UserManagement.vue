<template>
  <div class="user-management dark-theme">
    <!-- Заголовок и кнопки управления -->
    <div class="header-section mb-4">
      <div class="level">
        <div class="level-left">
          <h1 class="title has-text-light">
            <i class="fas fa-users mr-2"></i>
            Управление пользователями
          </h1>
        </div>
        <div class="level-right">
          <div class="buttons">
            <button
              class="button is-primary is-outlined"
              @click="refreshData"
              :disabled="loading"
            >
              <i class="fas fa-sync-alt" :class="{ 'fa-spin': loading }"></i>
            </button>
            <button
              class="button is-info is-outlined"
              @click="toggleFilters"
            >
              <i class="fas fa-filter"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Панель фильтров (сворачиваемая) -->
    <div class="filters-panel mb-4" v-if="showFilters">
      <div class="columns is-multiline">
        <div class="column is-4">
          <div class="field">
            <label class="label has-text-light">Поиск</label>
            <div class="control has-icons-left">
              <input
                v-model="searchQuery"
                class="input dark-input"
                type="text"
                placeholder="Поиск по имени или email..."
                @input="debouncedSearch"
              >
              <span class="icon is-small is-left">
                <i class="fas fa-search"></i>
              </span>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label has-text-light">Статус</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="statusFilter" class="dark-select">
                  <option value="all">Все</option>
                  <option value="active">Активные</option>
                  <option value="blocked">Заблокированные</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label has-text-light">Роль</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="roleFilter" class="dark-select">
                  <option value="all">Все</option>
                  <option value="admin">Администраторы</option>
                  <option value="publish">Публикаторы</option>
                  <option value="moderator">Модераторы</option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div class="column is-2">
          <div class="field">
            <label class="label has-text-light">&nbsp;</label>
            <div class="control">
              <button class="button is-dark is-fullwidth" @click="resetFilters">
                <i class="fas fa-redo mr-1"></i>
                Сбросить
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Статистика в карточках -->
    <div class="stats-cards mb-4">
      <div class="columns is-multiline">
        <div class="column is-3">
          <div class="stat-card">
            <div class="stat-content">
              <p class="stat-title">Всего</p>
              <p class="stat-value">{{ users.length }}</p>
            </div>
            <div class="stat-icon">
              <i class="fas fa-users"></i>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="stat-card">
            <div class="stat-content">
              <p class="stat-title">Активных</p>
              <p class="stat-value has-text-success">{{ activeUsersCount }}</p>
            </div>
            <div class="stat-icon">
              <i class="fas fa-user-check"></i>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="stat-card">
            <div class="stat-content">
              <p class="stat-title">Заблокировано</p>
              <p class="stat-value has-text-danger">{{ blockedUsersCount }}</p>
            </div>
            <div class="stat-icon">
              <i class="fas fa-user-slash"></i>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="stat-card">
            <div class="stat-content">
              <p class="stat-title">Админов</p>
              <p class="stat-value has-text-warning">{{ adminUsersCount }}</p>
            </div>
            <div class="stat-icon">
              <i class="fas fa-crown"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Таблица пользователей -->
    <div class="table-container">
      <div v-if="loading" class="loading-overlay">
        <div class="loading-content">
          <i class="fas fa-spinner fa-spin fa-2x has-text-primary"></i>
          <p class="has-text-light mt-2">Загрузка...</p>
        </div>
      </div>

      <div v-else-if="filteredUsers.length === 0" class="empty-state">
        <i class="fas fa-users-slash fa-3x has-text-grey-light mb-3"></i>
        <h3 class="has-text-light is-size-4 mb-2">Пользователи не найдены</h3>
        <p class="has-text-grey-light">Измените параметры поиска</p>
      </div>

      <div class="table-responsive" v-else>
        <table class="table is-fullwidth is-hoverable">
          <thead>
          <tr>
            <th>Пользователь</th>
            <th>Роли</th>
            <th>Статус</th>
            <th>Дата</th>
            <th>Действия</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>
              <div class="user-cell">
                <div class="user-avatar">
                  {{ user.name?.charAt(0) || 'U' }}
                </div>
                <div class="user-info">
                  <p class="user-name has-text-light">{{ user.name }}</p>
                  <p class="user-email has-text-grey-light">{{ user.email }}</p>
                </div>
              </div>
            </td>
            <td>
              <div class="tags">
                  <span
                    v-for="role in user.roles?.slice(0, 2)"
                    :key="role"
                    class="tag"
                    :class="getRoleTagClass(role)"
                  >
                    {{ formatRoleName(role) }}
                  </span>
                <span
                  v-if="user.roles && user.roles.length > 2"
                  class="tag is-dark"
                >
                    +{{ user.roles.length - 2 }}
                  </span>
              </div>
            </td>
            <td>
                <span
                  class="status-tag"
                  :class="user.isActive ? 'active' : 'blocked'"
                >
                  {{ user.isActive ? 'Активен' : 'Заблокирован' }}
                </span>
            </td>
            <td class="has-text-grey-light">
              {{ formatDate(user.createdAt) }}
            </td>
            <td>
              <div class="action-buttons">
                <button
                  class="button is-small is-info is-outlined"
                  @click="openUserDetails(user)"
                  title="Подробнее"
                >
                  <i class="fas fa-eye"></i>
                </button>
                <button
                  v-if="authStore.canManageUsers && user.id !== authStore.user?.id"
                  class="button is-small is-warning is-outlined"
                  @click="openEditUserModal(user)"
                  title="Редактировать"
                >
                  <i class="fas fa-edit"></i>
                </button>
                <button
                  v-if="authStore.canManageUsers && user.id !== authStore.user?.id"
                  class="button is-small"
                  :class="user.isActive ? 'is-danger is-outlined' : 'is-success is-outlined'"
                  @click="toggleUserStatus(user)"
                  :title="user.isActive ? 'Заблокировать' : 'Разблокировать'"
                >
                  <i class="fas" :class="user.isActive ? 'fa-lock' : 'fa-unlock'"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { adminAPI } from '@/api/admin'
import type { UserWithDetails } from '@/types/admin'
import { debounce } from 'lodash-es'

const authStore = useAuthStore()

// Состояния
const users = ref<UserWithDetails[]>([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const roleFilter = ref('all')
const showFilters = ref(true)

// Загрузка данных
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await adminAPI.getAllUsers()
    if (Array.isArray(response.data)) {
      users.value = response.data.map((user: any) => ({
        id: user.id,
        name: user.name || 'Без имени',
        email: user.email,
        roles: user.roles || [],
        isActive: true,
        createdAt: user.createdAt || new Date().toISOString()
      }))
    }
  } catch (error) {
    console.error('Ошибка при загрузке пользователей:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

// Фильтрация
const filteredUsers = computed(() => {
  let filtered = users.value

  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user =>
      user.name.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    )
  }

  if (statusFilter.value === 'active') {
    filtered = filtered.filter(user => user.isActive)
  } else if (statusFilter.value === 'blocked') {
    filtered = filtered.filter(user => !user.isActive)
  }

  if (roleFilter.value !== 'all') {
    filtered = filtered.filter(user => {
      const roles = user.roles || []
      return roles.some(role =>
        role.toLowerCase().includes(roleFilter.value.toLowerCase())
      )
    })
  }

  return filtered
})

// Статистика
const activeUsersCount = computed(() => {
  return users.value.filter(user => user.isActive).length
})

const blockedUsersCount = computed(() => {
  return users.value.filter(user => !user.isActive).length
})

const adminUsersCount = computed(() => {
  return users.value.filter(user =>
    user.roles?.some(role =>
      role.includes('Admin') || role.includes('Manage')
    )
  ).length
})

// Функции
const refreshData = () => {
  loadUsers()
}

const toggleFilters = () => {
  showFilters.value = !showFilters.value
}

const resetFilters = () => {
  searchQuery.value = ''
  statusFilter.value = 'all'
  roleFilter.value = 'all'
}

const debouncedSearch = debounce(() => {}, 300)

const openUserDetails = (user: UserWithDetails) => {
  console.log('Открыть детали:', user)
  // Можно добавить модальное окно
}

const openEditUserModal = (user: UserWithDetails) => {
  console.log('Редактировать:', user)
  // Можно добавить модальное окно
}

const toggleUserStatus = async (user: UserWithDetails) => {
  if (!authStore.canManageUsers || user.id === authStore.user?.id) return

  try {
    user.isActive = !user.isActive
    // Здесь будет вызов API
  } catch (error) {
    console.error('Ошибка при изменении статуса:', error)
  }
}

// Вспомогательные функции
const formatDate = (dateString: string) => {
  try {
    return new Date(dateString).toLocaleDateString('ru-RU')
  } catch {
    return 'Неизвестно'
  }
}

const formatRoleName = (role: string) => {
  return role.replace('OAPI:ROLE:', '')
}

const getRoleTagClass = (role: string) => {
  if (role.includes('Admin')) return 'is-danger'
  if (role.includes('Manage')) return 'is-warning'
  if (role.includes('Publish')) return 'is-success'
  return 'is-info'
}

// Инициализация
onMounted(async () => {
  if (!authStore.canManageUsers) return
  await loadUsers()
})
</script>

<style scoped>
.user-management.dark-theme {
  background-color: #121212;
  min-height: 100vh;
  padding: 20px;
  color: #e0e0e0;
}

/* Заголовок */
.header-section {
  border-bottom: 1px solid #333;
  padding-bottom: 15px;
}

/* Панель фильтров */
.filters-panel {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #333;
}

.dark-input, .dark-select {
  background: #2d2d2d;
  border-color: #444;
  color: #e0e0e0;
}

/* Карточки статистики */
.stats-cards .column {
  padding: 10px;
}

.stat-card {
  background: linear-gradient(135deg, #1e1e1e 0%, #2d2d2d 100%);
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #333;
  height: 100px;
}

.stat-content {
  flex: 1;
}

.stat-title {
  color: #aaa;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.3;
  color: #8e2de2;
}

/* Таблица */
.table-container {
  background: #1e1e1e;
  border-radius: 8px;
  border: 1px solid #333;
  overflow: hidden;
  position: relative;
  min-height: 300px;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(30, 30, 30, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.table-responsive {
  overflow-x: auto;
}

table {
  background: transparent;
}

th {
  background: #2d2d2d;
  border-color: #333 !important;
  color: #e0e0e0;
  font-weight: 600;
}

td {
  border-color: #333 !important;
  vertical-align: middle;
}

tr:hover {
  background: #2d2d2d;
}

/* Ячейки */
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-weight: 600;
  margin-bottom: 2px;
}

.user-email {
  font-size: 0.875rem;
}

/* Теги */
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tag {
  font-size: 0.75rem;
  padding: 3px 8px;
  border-radius: 4px;
}

/* Статус */
.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
}

.status-tag.active {
  background: rgba(35, 209, 96, 0.2);
  color: #23d160;
  border: 1px solid rgba(35, 209, 96, 0.4);
}

.status-tag.blocked {
  background: rgba(255, 56, 96, 0.2);
  color: #ff3860;
  border: 1px solid rgba(255, 56, 96, 0.4);
}

/* Кнопки действий */
.action-buttons {
  display: flex;
  gap: 5px;
}

.action-buttons .button {
  min-width: 32px;
  height: 32px;
}

/* Адаптивность */
@media (max-width: 768px) {
  .user-management {
    padding: 15px;
  }

  .stats-cards .column {
    width: 50%;
  }

  .stat-card {
    height: 80px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .stat-icon {
    font-size: 2rem;
  }

  .user-cell {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .user-avatar {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>
