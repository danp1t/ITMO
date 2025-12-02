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
                  <option v-for="role in uniqueRoles" :key="role" :value="role">
                    {{ formatRoleName(role) }}
                  </option>
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
            <th>Дата регистрации</th>
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
                  v-for="role in getUserRoles(user)"
                  :key="role.name"
                  class="tag"
                  :class="getRoleTagClass(role.name)"
                >
                  {{ formatRoleName(role.name) }}
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

    <!-- Модальное окно детальной информации о пользователе -->
    <div class="modal dark-modal" :class="{ 'is-active': showUserDetailsModal }">
      <div class="modal-background" @click="closeUserDetailsModal"></div>
      <div class="modal-card" style="max-width: 800px;">
        <header class="modal-card-head dark-modal-header">
          <p class="modal-card-title has-text-light">Детальная информация о пользователе</p>
          <button class="delete" @click="closeUserDetailsModal"></button>
        </header>

        <section class="modal-card-body dark-modal-body">
          <div v-if="loadingUserDetails" class="loading">
            <i class="fas fa-spinner fa-spin fa-2x has-text-primary"></i>
            <p class="has-text-light mt-2">Загрузка информации...</p>
          </div>

          <div v-else-if="selectedUser && userDetails" class="user-details-content">
            <!-- Заголовок пользователя -->
            <div class="user-header mb-4">
              <div class="user-avatar-large">
                {{ selectedUser.name?.charAt(0) || 'U' }}
              </div>
              <div class="user-info-large">
                <h3 class="has-text-light is-size-4">{{ selectedUser.name }}</h3>
                <p class="has-text-grey-light">{{ selectedUser.email }}</p>
                <div class="user-id-tag">
                  <span class="tag is-dark">ID: {{ selectedUser.id }}</span>
                </div>
              </div>
            </div>

            <!-- Табы -->
            <div class="tabs is-boxed is-small mb-4">
              <ul>
                <li :class="{ 'is-active': activeTab === 'general' }">
                  <a @click.prevent="activeTab = 'general'">
                    <i class="fas fa-user mr-2"></i>
                    Основное
                  </a>
                </li>
                <li :class="{ 'is-active': activeTab === 'roles' }">
                  <a @click.prevent="activeTab = 'roles'">
                    <i class="fas fa-user-tag mr-2"></i>
                    Роли
                  </a>
                </li>
                <li :class="{ 'is-active': activeTab === 'activity' }">
                  <a @click.prevent="activeTab = 'activity'">
                    <i class="fas fa-chart-line mr-2"></i>
                    Активность
                  </a>
                </li>
              </ul>
            </div>

            <!-- Вкладка "Основное" -->
            <div v-if="activeTab === 'general'" class="tab-content">
              <div class="columns is-multiline">
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Имя пользователя</label>
                    <p class="has-text-light">{{ selectedUser.name || 'Не указано' }}</p>
                  </div>
                </div>
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Email</label>
                    <p class="has-text-light">{{ selectedUser.email }}</p>
                  </div>
                </div>
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Статус аккаунта</label>
                    <span
                      class="status-tag"
                      :class="selectedUser.isActive ? 'active' : 'blocked'"
                    >
                      {{ selectedUser.isActive ? 'Активен' : 'Заблокирован' }}
                    </span>
                  </div>
                </div>
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Дата регистрации</label>
                    <p class="has-text-grey-light">{{ formatDateTime(selectedUser.createdAt) }}</p>
                  </div>
                </div>
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Последнее обновление</label>
                    <p class="has-text-grey-light">{{ formatDateTime(selectedUser.updatedAt || selectedUser.createdAt) }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Вкладка "Роли" -->
            <div v-if="activeTab === 'roles'" class="tab-content">
              <div class="detail-field mb-4">
                <label class="label has-text-light">Назначенные роли</label>
                <div v-if="userDetails.roles && userDetails.roles.length" class="roles-grid">
                  <div v-for="role in userDetails.roles" :key="role.id" class="role-item">
                    <div class="role-header">
                      <span class="tag is-medium" :class="getRoleTagClass(role.name)">
                        {{ formatRoleName(role.name) }}
                      </span>
                      <button
                        v-if="authStore.canManageUsers && selectedUser.id !== authStore.user?.id"
                        class="delete is-small ml-2"
                        @click="removeRole(role)"
                        :title="`Удалить роль ${formatRoleName(role.name)}`"
                      ></button>
                    </div>
                    <div class="role-description">
                      <p class="has-text-grey-light">{{ role.description }}</p>
                    </div>
                    <div class="role-id">
                      <small class="has-text-grey">ID: {{ role.id }}</small>
                    </div>
                  </div>
                </div>
                <p v-else class="has-text-grey-light">Роли не назначены</p>
              </div>

              <div class="detail-field">
                <label class="label has-text-light">Добавить роль</label>
                <div class="field has-addons">
                  <div class="control is-expanded">
                    <div class="select is-fullwidth">
                      <select v-model="newRoleToAdd" class="dark-select">
                        <option value="" disabled selected>Выберите роль</option>
                        <option
                          v-for="availableRole in availableRoles"
                          :key="availableRole.id"
                          :value="availableRole.id"
                          :disabled="isRoleAssigned(availableRole.id)"
                        >
                          {{ formatRoleName(availableRole.name) }} - {{ availableRole.description }}
                        </option>
                      </select>
                    </div>
                  </div>
                  <div class="control">
                    <button
                      class="button is-primary"
                      @click="addRoleToUser"
                      :disabled="!newRoleToAdd || addingRole"
                    >
                      <span v-if="addingRole">
                        <i class="fas fa-spinner fa-spin"></i>
                      </span>
                      <span v-else>
                        <i class="fas fa-plus mr-2"></i> Добавить
                      </span>
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Вкладка "Активность" -->
            <div v-if="activeTab === 'activity'" class="tab-content">
              <div class="columns is-multiline">
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Количество постов</label>
                    <p class="has-text-light is-size-4">{{ userDetails.posts?.length || 0 }}</p>
                  </div>
                </div>
                <div class="column is-6">
                  <div class="detail-field">
                    <label class="label has-text-light">Количество комментариев</label>
                    <p class="has-text-light is-size-4">{{ userDetails.comments?.length || 0 }}</p>
                  </div>
                </div>

                <!-- Посты пользователя -->
                <div class="column is-12" v-if="userDetails.posts && userDetails.posts.length">
                  <div class="detail-field">
                    <label class="label has-text-light">Последние посты</label>
                    <div class="posts-list">
                      <div v-for="post in userDetails.posts.slice(0, 3)" :key="post.id" class="post-item">
                        <div class="post-content">
                          <h4 class="has-text-light is-size-6 mb-1">{{ post.title }}</h4>
                          <div class="post-meta">
                            <span class="has-text-grey-light">
                              <i class="fas fa-calendar mr-1"></i>
                              {{ formatDate(post.createdAt) }}
                            </span>
                            <span class="has-text-grey-light ml-3">
                              <i class="fas fa-heart mr-1"></i>
                              {{ post.countLike }} лайков
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Комментарии пользователя -->
                <div class="column is-12" v-if="userDetails.comments && userDetails.comments.length">
                  <div class="detail-field">
                    <label class="label has-text-light">Последние комментарии</label>
                    <div class="comments-list">
                      <div v-for="comment in userDetails.comments.slice(0, 3)" :key="comment.id" class="comment-item">
                        <div class="comment-content">
                          <p class="has-text-light mb-1">{{ comment.userComment }}</p>
                          <div class="comment-meta">
                            <span class="has-text-grey-light">
                              <i class="fas fa-calendar mr-1"></i>
                              {{ formatDate(comment.createdAt) }}
                            </span>
                            <span class="has-text-grey-light ml-3">
                              <i class="fas fa-user mr-1"></i>
                              {{ comment.accountName }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <footer class="modal-card-foot dark-modal-footer">
          <div class="buttons">
            <button
              v-if="authStore.canManageUsers && selectedUser && selectedUser.id !== authStore.user?.id"
              class="button is-warning"
              @click="openEditUserModal(selectedUser)"
            >
              <i class="fas fa-edit mr-2"></i>
              Редактировать профиль
            </button>
            <button
              v-if="authStore.canManageUsers && selectedUser && selectedUser.id !== authStore.user?.id"
              class="button"
              :class="selectedUser?.isActive ? 'is-danger' : 'is-success'"
              @click="toggleUserStatus(selectedUser!)"
              :disabled="togglingStatus"
            >
              <span v-if="togglingStatus">
                <i class="fas fa-spinner fa-spin mr-2"></i>
              </span>
              <span v-else>
                <i class="fas mr-2" :class="selectedUser?.isActive ? 'fa-lock' : 'fa-unlock'"></i>
              </span>
              {{ selectedUser?.isActive ? 'Заблокировать' : 'Разблокировать' }}
            </button>
            <button class="button is-dark" @click="closeUserDetailsModal">
              Закрыть
            </button>
          </div>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { adminAPI } from '@/api/admin'
import type { UserWithDetails, Role, UserDetail } from '@/types/admin'
import { debounce } from 'lodash-es'

const authStore = useAuthStore()

// Состояния
const users = ref<UserWithDetails[]>([])
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('all')
const roleFilter = ref('all')
const showFilters = ref(true)

// Новые состояния для детальной информации
const showUserDetailsModal = ref(false)
const selectedUser = ref<UserWithDetails | null>(null)
const userDetails = ref<UserDetail | null>(null)
const loadingUserDetails = ref(false)
const togglingStatus = ref(false)
const addingRole = ref(false)

// Для управления ролями
const availableRoles = ref<Role[]>([])
const newRoleToAdd = ref<string>('')
const activeTab = ref<'general' | 'roles' | 'activity'>('general')

// Получение уникальных ролей для фильтра
const uniqueRoles = computed(() => {
  const roles = new Set<string>()
  users.value.forEach(user => {
    if (user.roles && Array.isArray(user.roles)) {
      user.roles.forEach(role => {
        if (typeof role === 'object' && role.name) {
          roles.add(role.name)
        } else if (typeof role === 'string') {
          roles.add(role)
        }
      })
    }
  })
  return Array.from(roles)
})

// Получение ролей пользователя (поддержка старого и нового формата)
const getUserRoles = (user: UserWithDetails) => {
  if (!user.roles) return []

  if (Array.isArray(user.roles) && user.roles.length > 0) {
    // Проверяем, это объекты ролей или строки
    if (typeof user.roles[0] === 'object' && user.roles[0].name) {
      return user.roles as any[]
    } else if (typeof user.roles[0] === 'string') {
      // Конвертируем строки в объекты ролей
      return user.roles.map((role: string) => ({
        id: 0,
        name: role,
        description: ''
      }))
    }
  }
  return []
}

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
        isActive: user.isActive !== false,
        createdAt: user.createdAt || new Date().toISOString(),
        updatedAt: user.updatedAt || user.createdAt || new Date().toISOString()
      }))
    }
  } catch (error) {
    console.error('Ошибка при загрузке пользователей:', error)
    users.value = []
  } finally {
    loading.value = false
  }
}

// Загрузка доступных ролей
const loadAvailableRoles = async () => {
  try {
    const response = await adminAPI.getAllRoles()
    availableRoles.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке ролей:', error)
    availableRoles.value = []
  }
}

// Загрузка детальной информации о пользователе
const loadUserDetails = async (userId: number) => {
  loadingUserDetails.value = true
  try {
    const response = await adminAPI.getUserDetails(userId)
    userDetails.value = response.data

    // Загружаем доступные роли
    await loadAvailableRoles()
  } catch (error) {
    console.error('Ошибка при загрузке детальной информации:', error)
    userDetails.value = null
  } finally {
    loadingUserDetails.value = false
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
      const roles = getUserRoles(user)
      return roles.some(role => role.name === roleFilter.value)
    })
  }

  return filtered
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

const openUserDetails = async (user: UserWithDetails) => {
  selectedUser.value = user
  activeTab.value = 'general'
  showUserDetailsModal.value = true
  await loadUserDetails(user.id)
}

const closeUserDetailsModal = () => {
  showUserDetailsModal.value = false
  selectedUser.value = null
  userDetails.value = null
  newRoleToAdd.value = ''
  addingRole.value = false
  togglingStatus.value = false
}

const openEditUserModal = (user: UserWithDetails) => {
  console.log('Редактировать профиль:', user)
  // Можно добавить модальное окно редактирования
  closeUserDetailsModal()
}

const toggleUserStatus = async (user: UserWithDetails) => {
  if (!authStore.canManageUsers || user.id === authStore.user?.id) return

  if (!confirm(`Вы уверены, что хотите ${user.isActive ? 'заблокировать' : 'разблокировать'} пользователя ${user.name}?`)) {
    return
  }

  togglingStatus.value = true
  try {
    const newStatus = !user.isActive
    await adminAPI.toggleUserStatus(user.id, newStatus)
    user.isActive = newStatus
    await loadUsers()
  } catch (error) {
    console.error('Ошибка при изменении статуса:', error)
    alert('Не удалось изменить статус пользователя')
  } finally {
    togglingStatus.value = false
  }
}

// Проверка, назначена ли уже роль
const isRoleAssigned = (roleId: string) => {
  if (!userDetails.value || !userDetails.value.roles) return false
  return userDetails.value.roles.some(role => role.id.toString() === roleId)
}

// Управление ролями
const addRoleToUser = async () => {
  if (!newRoleToAdd.value || !selectedUser.value || !userDetails.value || addingRole.value) return

  addingRole.value = true
  try {
    const roleToAdd = availableRoles.value.find(role => role.id.toString() === newRoleToAdd.value)
    if (!roleToAdd) {
      throw new Error('Роль не найдена')
    }

    // Обновляем локально
    if (!userDetails.value.roles) {
      userDetails.value.roles = []
    }
    userDetails.value.roles.push(roleToAdd)

    // Отправляем на сервер
    const updatedRoles = userDetails.value.roles.map(role => role.name)
    await adminAPI.updateUserRoles(selectedUser.value.id, updatedRoles)

    // Обновляем основной список
    const userIndex = users.value.findIndex(u => u.id === selectedUser.value!.id)
    if (userIndex !== -1) {
      users.value[userIndex].roles = updatedRoles
    }

    newRoleToAdd.value = ''
    alert('Роль успешно добавлена')
  } catch (error) {
    console.error('Ошибка при добавлении роли:', error)
    alert('Не удалось добавить роль')
  } finally {
    addingRole.value = false
  }
}

const removeRole = async (roleToRemove: Role) => {
  if (!selectedUser.value || !userDetails.value) return

  if (!confirm(`Удалить роль ${formatRoleName(roleToRemove.name)} у пользователя ${selectedUser.value.name}?`)) {
    return
  }

  try {
    // Обновляем локально
    if (userDetails.value.roles) {
      userDetails.value.roles = userDetails.value.roles.filter(role => role.id !== roleToRemove.id)
    }

    // Отправляем на сервер
    const updatedRoles = userDetails.value.roles ? userDetails.value.roles.map(role => role.name) : []
    await adminAPI.updateUserRoles(selectedUser.value.id, updatedRoles)

    // Обновляем основной список
    const userIndex = users.value.findIndex(u => u.id === selectedUser.value!.id)
    if (userIndex !== -1) {
      users.value[userIndex].roles = updatedRoles
    }

    alert('Роль успешно удалена')
  } catch (error) {
    console.error('Ошибка при удалении роли:', error)
    alert('Не удалось удалить роль')
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

const formatDateTime = (dateString: string) => {
  try {
    return new Date(dateString).toLocaleString('ru-RU')
  } catch {
    return 'Неизвестно'
  }
}

const formatRoleName = (role: string) => {
  return role.replace('OAPI:ROLE:', '')
}

const getRoleTagClass = (role: string) => {
  if (role.includes('Admin') || role.includes('BlockAccount')) return 'is-danger'
  if (role.includes('Manage')) return 'is-warning'
  if (role.includes('Publish')) return 'is-success'
  if (role.includes('Edit')) return 'is-info'
  if (role.includes('Delete')) return 'is-dark'
  if (role.includes('Get')) return 'is-link'
  if (role.includes('Update')) return 'is-primary'
  return 'is-light'
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

/* Стили для модального окна */
.dark-modal .modal-card {
  background: #1e1e1e;
  border: 1px solid #333;
}

.dark-modal-header {
  background: #2d2d2d;
  border-bottom: 1px solid #333;
}

.dark-modal-body {
  background: #1e1e1e;
}

.dark-modal-footer {
  background: #2d2d2d;
  border-top: 1px solid #333;
}

/* Стили для детальной информации */
.user-details-content {
  padding: 10px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #333;
}

.user-avatar-large {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 32px;
  flex-shrink: 0;
}

.user-info-large {
  flex: 1;
}

.user-id-tag {
  margin-top: 5px;
}

.detail-field {
  margin-bottom: 20px;
}

.detail-field .label {
  margin-bottom: 5px;
  font-size: 0.9rem;
  color: #aaa;
}

/* Табы */
.tabs ul {
  border-bottom-color: #333;
}

.tabs a {
  color: #aaa;
  border-color: #333;
  background-color: #2d2d2d;
}

.tabs a:hover {
  background-color: #3d3d3d;
  border-color: #444;
}

.tabs li.is-active a {
  background-color: #4a00e0;
  border-color: #4a00e0;
  color: white;
}

/* Роли */
.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
}

.role-item {
  background: #2d2d2d;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #333;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.role-description {
  margin-bottom: 8px;
  min-height: 40px;
}

.role-description p {
  font-size: 0.875rem;
  line-height: 1.4;
}

.role-id {
  text-align: right;
}

/* Посты и комментарии */
.posts-list, .comments-list {
  background: #2d2d2d;
  border-radius: 8px;
  padding: 15px;
  border: 1px solid #333;
}

.post-item, .comment-item {
  padding: 10px 0;
  border-bottom: 1px solid #444;
}

.post-item:last-child, .comment-item:last-child {
  border-bottom: none;
}

.post-meta, .comment-meta {
  display: flex;
  align-items: center;
  font-size: 0.875rem;
}

/* Адаптивность */
@media (max-width: 768px) {
  .user-management {
    padding: 15px;
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

  .user-header {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .user-info-large {
    text-align: center;
  }

  .tabs ul {
    flex-direction: column;
  }

  .tabs li {
    width: 100%;
  }

  .roles-grid {
    grid-template-columns: 1fr;
  }

  .post-meta, .comment-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>
