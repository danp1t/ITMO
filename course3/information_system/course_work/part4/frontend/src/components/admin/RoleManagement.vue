<template>
  <div class="role-management dark-theme">
    <div class="role-header mb-4">
      <h1 class="title has-text-light">
        <i class="fas fa-user-tag mr-2"></i>
        Управление ролями
      </h1>
      <button class="button is-primary" @click="showCreateModal = true">
        <i class="fas fa-plus mr-2"></i>
        Создать роль
      </button>
    </div>

    <div class="roles-list">
      <div v-if="loading" class="loading">
        <i class="fas fa-spinner fa-spin fa-2x has-text-primary"></i>
      </div>

      <div v-else-if="roles.length === 0" class="empty-state">
        <i class="fas fa-user-tag fa-3x has-text-grey-light mb-3"></i>
        <h3 class="has-text-light">Ролей пока нет</h3>
        <p class="has-text-grey-light">Создайте первую роль</p>
      </div>

      <div v-else class="roles-grid">
        <div v-for="role in roles" :key="role.id" class="role-card">
          <div class="role-header">
            <h3 class="has-text-light">{{ formatRoleName(role.name) }}</h3>
            <div class="role-actions">
              <button class="button is-small is-info is-outlined" @click="manageRoleUsers(role)">
                <i class="fas fa-users"></i>
              </button>
            </div>
          </div>
          <div class="role-body">
            <p class="has-text-grey-light">{{ role.description }}</p>
            <div class="role-users mt-3">
              <span class="tag is-dark">
                <i class="fas fa-users mr-1"></i>
                {{ role.userCount || 0 }} пользователей
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal dark-modal" :class="{ 'is-active': showCreateModal }">
      <div class="modal-background" @click="closeModal"></div>
      <div class="modal-card">
        <header class="modal-card-head dark-modal-header">
          <p class="modal-card-title has-text-light">Создать роль</p>
          <button class="delete" @click="closeModal"></button>
        </header>

        <section class="modal-card-body dark-modal-body">
          <div class="field">
            <label class="label has-text-light">Название роли</label>
            <div class="control">
              <input v-model="newRole.name" class="input dark-input" placeholder="OAPI:ROLE:NewRole">
            </div>
          </div>
          <div class="field">
            <label class="label has-text-light">Описание</label>
            <div class="control">
              <textarea v-model="newRole.description" class="textarea dark-textarea" rows="3"></textarea>
            </div>
          </div>
        </section>

        <footer class="modal-card-foot dark-modal-footer">
          <button class="button is-primary" @click="createRole" :disabled="isSaving">
            {{ isSaving ? 'Создание...' : 'Создать' }}
          </button>
          <button class="button is-dark" @click="closeModal">Отмена</button>
        </footer>
      </div>
    </div>

    <div class="modal dark-modal" :class="{ 'is-active': showManageUsersModal }">
      <div class="modal-background" @click="closeManageUsersModal"></div>
      <div class="modal-card" style="width: 800px; max-width: 90vw;">
        <header class="modal-card-head dark-modal-header">
          <p class="modal-card-title has-text-light">
            Управление пользователями роли: {{ formatRoleName(selectedRole?.name || '') }}
          </p>
          <button class="delete" @click="closeManageUsersModal"></button>
        </header>

        <section class="modal-card-body dark-modal-body">
          <div v-if="manageLoading" class="has-text-centered py-6">
            <i class="fas fa-spinner fa-spin fa-2x has-text-primary"></i>
          </div>

          <div v-else>
            <div class="field">
              <label class="label has-text-light">Добавить пользователя</label>
              <div class="field has-addons">
                <div class="control is-expanded">
                  <input
                    v-model="searchEmail"
                    class="input dark-input"
                    placeholder="Введите email пользователя"
                    @keyup.enter="searchUser"
                  >
                </div>
                <div class="control">
                  <button class="button is-primary" @click="searchUser" :disabled="!searchEmail.trim()">
                    Найти
                  </button>
                </div>
              </div>
            </div>

            <div v-if="searchResult && !searchResult.hasRole" class="notification is-info is-dark mt-3">
              <div class="is-flex is-justify-content-space-between is-align-items-center">
                <div>
                  <strong>{{ searchResult.name }}</strong> ({{ searchResult.email }})
                </div>
                <button class="button is-small is-primary" @click="addRoleToUser(searchResult.id)">
                  Добавить роль
                </button>
              </div>
            </div>

            <div v-if="searchResult && searchResult.hasRole" class="notification is-warning is-dark mt-3">
              <div class="is-flex is-justify-content-space-between is-align-items-center">
                <div>
                  <strong>{{ searchResult.name }}</strong> ({{ searchResult.email }}) - уже имеет эту роль
                </div>
                <button class="button is-small is-danger" @click="removeRoleFromUser(searchResult.id)">
                  Удалить роль
                </button>
              </div>
            </div>

            <div class="mt-5">
              <h4 class="subtitle has-text-light mb-3">Пользователи с этой ролью ({{ roleUsers.length }})</h4>

              <div v-if="roleUsers.length === 0" class="notification is-dark">
                Нет пользователей с этой ролью
              </div>

              <div v-else class="users-list">
                <div v-for="user in roleUsers" :key="user.id" class="user-item">
                  <div class="is-flex is-justify-content-space-between is-align-items-center">
                    <div>
                      <strong class="has-text-light">{{ user.name }}</strong>
                      <br>
                      <small class="has-text-grey-light">{{ user.email }}</small>
                    </div>
                    <button class="button is-small is-danger is-outlined" @click="removeRoleFromUser(user.id)">
                      Удалить
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <footer class="modal-card-foot dark-modal-footer">
          <button class="button is-dark" @click="closeManageUsersModal">Закрыть</button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api/admin'
import type { Role } from '@/types/admin'

interface User {
  id: number
  name: string
  email: string
  hasRole?: boolean
}

const roles = ref<Role[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const isSaving = ref(false)
const showManageUsersModal = ref(false)
const manageLoading = ref(false)

const selectedRole = ref<Role | null>(null)
const roleUsers = ref<User[]>([])
const searchEmail = ref('')
const searchResult = ref<User | null>(null)

const newRole = ref({
  name: '',
  description: ''
})

const loadRoles = async () => {
  loading.value = true
  try {
    const response = await adminAPI.getAllRoles()
    roles.value = response.data || []

    for (const role of roles.value) {
      await loadRoleUsersCount(role)
    }
  } catch (error) {
    console.error('Ошибка при загрузке ролей:', error)
  } finally {
    loading.value = false
  }
}

const loadRoleUsers = async (roleId: number) => {
  manageLoading.value = true
  try {
    const response = await adminAPI.getAccountsByRole(roleId)
    roleUsers.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке пользователей роли:', error)
    roleUsers.value = []
  } finally {
    manageLoading.value = false
  }
}

const loadRoleUsersCount = async (role: Role) => {
  try {
    const response = await adminAPI.getRoleUsersCount(role.id)
    role.userCount = response.data?.count || 0
  } catch (error) {
    console.error('Ошибка при загрузке количества пользователей:', error)
    role.userCount = 0
  }
}

const manageRoleUsers = async (role: Role) => {
  selectedRole.value = role
  searchEmail.value = ''
  searchResult.value = null
  showManageUsersModal.value = true
  await loadRoleUsers(role.id)
}

const searchUser = async () => {
  if (!searchEmail.value.trim() || !selectedRole.value) return

  try {
    const response = await adminAPI.getAccountByEmail(searchEmail.value.trim())
    const user = response.data

    if (user) {
      const hasRole = roleUsers.value.some(u => u.id === user.id)
      searchResult.value = {
        ...user,
        hasRole
      }
    } else {
      alert('Пользователь не найден')
      searchResult.value = null
    }
  } catch (error) {
    console.error('Ошибка при поиске пользователя:', error)
    alert('Ошибка при поиске пользователя')
  }
}

const addRoleToUser = async (userId: number) => {
  if (!selectedRole.value) return

  try {
    await adminAPI.addRoleToAccountByName(userId, selectedRole.value.name)
    await loadRoleUsers(selectedRole.value.id)
    await loadRoleUsersCount(selectedRole.value)

    if (searchResult.value) {
      searchResult.value.hasRole = true
    }

  } catch (error) {
    console.error('Ошибка при добавлении роли:', error)
    alert('Ошибка при добавлении роли')
  }
}

const removeRoleFromUser = async (userId: number) => {
  if (!selectedRole.value) return

  try {
    await adminAPI.removeRoleFromAccountByName(userId, selectedRole.value.name)
    await loadRoleUsers(selectedRole.value.id)
    await loadRoleUsersCount(selectedRole.value)

    if (searchResult.value && searchResult.value.id === userId) {
      searchResult.value.hasRole = false
    }

  } catch (error) {
    console.error('Ошибка при удалении роли:', error)
    alert('Ошибка при удалении роли')
  }
}

const createRole = async () => {
  if (!newRole.value.name.trim() || !newRole.value.description.trim()) return

  isSaving.value = true
  try {
    await adminAPI.createRole(newRole.value)
    await loadRoles()
    closeModal()
  } catch (error) {
    console.error('Ошибка при создании роли:', error)
  } finally {
    isSaving.value = false
  }
}

const closeModal = () => {
  showCreateModal.value = false
  newRole.value = { name: '', description: '' }
}

const closeManageUsersModal = () => {
  showManageUsersModal.value = false
  selectedRole.value = null
  roleUsers.value = []
  searchEmail.value = ''
  searchResult.value = null
}

const formatRoleName = (role: string) => {
  return role.replace('OAPI:ROLE:', '')
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-management.dark-theme {
  padding: 20px;
}

.role-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #333;
  padding-bottom: 15px;
}

.roles-list {
  min-height: 400px;
}

.loading, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.empty-state i {
  opacity: 0.3;
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.role-card {
  background: #1e1e1e;
  border-radius: 8px;
  border: 1px solid #333;
  overflow: hidden;
  transition: all 0.3s ease;
}

.role-card:hover {
  border-color: #8e2de2;
  transform: translateY(-2px);
}

.role-header {
  background: #2d2d2d;
  padding: 15px 20px;
  border-bottom: 1px solid #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-header h3 {
  margin: 0;
  font-size: 1.1rem;
}

.role-actions {
  display: flex;
  gap: 5px;
}

.role-body {
  padding: 20px;
}

.role-body p {
  margin-bottom: 10px;
  font-size: 0.9rem;
}

.users-list {
  max-height: 300px;
  overflow-y: auto;
}

.user-item {
  padding: 12px 15px;
  border-bottom: 1px solid #333;
  transition: background-color 0.2s;
}

.user-item:hover {
  background-color: #2d2d2d;
}

.user-item:last-child {
  border-bottom: none;
}

.dark-modal .modal-card {
  background: #1e1e1e;
  border: 1px solid #333;
  max-width: 500px;
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

.notification {
  background-color: #2d2d2d;
  border-left: 4px solid;
}

.notification.is-info {
  border-left-color: #2d89e1;
}

.notification.is-warning {
  border-left-color: #ffdd57;
}

@media (max-width: 768px) {
  .role-header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .roles-grid {
    grid-template-columns: 1fr;
  }
}
</style>
