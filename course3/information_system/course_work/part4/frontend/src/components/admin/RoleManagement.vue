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

    <!-- Список ролей -->
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
              <button class="button is-small is-warning is-outlined" @click="editRole(role)">
                <i class="fas fa-edit"></i>
              </button>
              <button class="button is-small is-danger is-outlined" @click="deleteRole(role.id)">
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
          <div class="role-body">
            <p class="has-text-grey-light">{{ role.description }}</p>
            <div class="role-users mt-3">
              <span class="tag is-light">
                <i class="fas fa-users mr-1"></i>
                {{ getRoleUsersCount(role.name) }} пользователей
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Модальное окно создания роли -->
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminAPI } from '@/api/admin'
import type { Role } from '@/types/admin'

const roles = ref<Role[]>([])
const loading = ref(false)
const showCreateModal = ref(false)
const isSaving = ref(false)

const newRole = ref({
  name: '',
  description: ''
})

// Загрузка ролей
const loadRoles = async () => {
  loading.value = true
  try {
    const response = await adminAPI.getAllRoles()
    roles.value = response.data || []
  } catch (error) {
    console.error('Ошибка при загрузке ролей:', error)
  } finally {
    loading.value = false
  }
}

// Функции
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

const editRole = (role: Role) => {
  console.log('Редактировать роль:', role)
  // Реализация редактирования
}

const deleteRole = async (roleId: number) => {
  if (!confirm('Удалить роль?')) return
  try {
    await adminAPI.deleteRole(roleId)
    await loadRoles()
  } catch (error) {
    console.error('Ошибка при удалении роли:', error)
  }
}

const closeModal = () => {
  showCreateModal.value = false
  newRole.value = { name: '', description: '' }
}

const formatRoleName = (role: string) => {
  return role.replace('OAPI:ROLE:', '')
}

const getRoleUsersCount = (roleName: string) => {
  // Здесь должна быть логика подсчета пользователей с этой ролью
  return 0
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

/* Список ролей */
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

/* Сетка ролей */
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

/* Модальное окно */
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

/* Адаптивность */
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
