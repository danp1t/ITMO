<template>
  <div class="organizations-main">
    <div class="header">
      <h1>Организации</h1>
      <button class="create-btn" @click="showCreateForm = true">
        Создать организацию
      </button>
    </div>

    <div class="table-container">
      <table class="organizations-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Название</th>
            <th>Годовой оборот</th>
            <th>Количество сотрудников</th>
            <th>Рейтинг</th>
            <th>Координаты</th>
            <th>Официальный адрес</th>
            <th>Почтовый адрес</th>
            <th>Тип</th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="org in organizations" :key="org.id">
            <td>{{ org.id }}</td>
            <td>
              <span v-if="!org.editing">{{ org.name }}</span>
              <input
                v-else
                v-model="org.editingData.name"
                class="inline-input"
                @keyup.enter="saveOrganization(org)"
                @keyup.esc="cancelEditing(org)"
              >
            </td>
            <td>
              <span v-if="!org.editing">{{ formatCurrency(org.annualTurnover) }}</span>
              <input
                v-else
                v-model.number="org.editingData.annualTurnover"
                type="number"
                class="inline-input"
                @keyup.enter="saveOrganization(org)"
                @keyup.esc="cancelEditing(org)"
              >
            </td>
            <td>
              <span v-if="!org.editing">{{ org.employeesCount }}</span>
              <input
                v-else
                v-model.number="org.editingData.employeesCount"
                type="number"
                min="0"
                class="inline-input"
                @keyup.enter="saveOrganization(org)"
                @keyup.esc="cancelEditing(org)"
              >
            </td>
            <td>
              <span v-if="!org.editing" class="rating" :class="getRatingClass(org.rating)">
                {{ org.rating }}
              </span>
              <input
                v-else
                v-model.number="org.editingData.rating"
                type="number"
                step="0.1"
                min="0"
                max="5"
                class="inline-input"
                @keyup.enter="saveOrganization(org)"
                @keyup.esc="cancelEditing(org)"
              >
            </td>
            <td
              class="clickable-cell"
              @click="viewChildEntity(org.coordinates, 'coordinates')"
            >
              {{ org.coordinates ? `ID: ${org.coordinates}` : 'Не указано' }}
            </td>
            <td
              class="clickable-cell"
              @click="viewChildEntity(org.officialAddress, 'location')"
            >
              {{ org.officialAddress ? `ID: ${org.officialAddress}` : 'Не указано' }}
            </td>
            <td
              class="clickable-cell"
              @click="viewChildEntity(org.postalAddress, 'address')"
            >
              {{ org.postalAddress ? `ID: ${org.postalAddress}` : 'Не указано' }}
            </td>
            <td>
              <span v-if="!org.editing" class="type-badge" :class="getTypeClass(org.type)">
                {{ org.type }}
              </span>
              <select
                v-else
                v-model="org.editingData.type"
                class="inline-input"
                @keyup.enter="saveOrganization(org)"
                @keyup.esc="cancelEditing(org)"
              >
                <option value="COMMERCIAL">COMMERCIAL</option>
                <option value="GOVERNMENT">GOVERNMENT</option>
                <option value="PRIVATE_LIMITED_COMPANY">PRIVATE_LIMITED_COMPANY</option>
                <option value="OPEN_JOINT_STOCK_COMPANY">OPEN_JOINT_STOCK_COMPANY</option>
              </select>
            </td>
            <td>
              <div class="action-buttons">
                <template v-if="!org.editing">
                  <button class="edit-btn" @click="startEditing(org)">
                    Редактировать
                  </button>
                  <button class="delete-btn" @click="deleteOrganization(org.id)">
                    Удалить
                  </button>
                </template>
                <template v-else>
                  <button class="save-btn" @click="saveOrganization(org)">
                    Сохранить
                  </button>
                  <button class="cancel-btn" @click="cancelEditing(org)">
                    Отмена
                  </button>
                </template>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Остальные модальные окна остаются без изменений -->
    <div v-if="showCreateForm" class="modal-overlay" @click="showCreateForm = false">
      <div class="modal-content large-modal" @click.stop>
        <div class="modal-header">
          <h3>Создание организации</h3>
          <button type="button" class="close-btn" @click="showCreateForm = false">×</button>
        </div>
        <div class="modal-body">
          <OrganizationForm
            @submitted="onOrganizationCreated"
            @cancel="showCreateForm = false"
          />
        </div>
      </div>
    </div>

    <!-- Модальное окно просмотра/редактирования дочерней сущности -->
    <div v-if="showChildEntityModal" class="modal-overlay" @click="closeChildEntityModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ isEditMode ? 'Редактирование' : 'Просмотр' }} {{ getChildEntityTitle }}</h3>
          <div class="modal-actions">
            <button
              v-if="!isEditMode && currentChildEntity"
              class="edit-mode-btn"
              @click="enableEditMode"
            >
              Редактировать
            </button>
            <button type="button" class="close-btn" @click="closeChildEntityModal">×</button>
          </div>
        </div>
        <div class="modal-body">
          <div v-if="loadingChildEntity" class="loading">
            Загрузка данных...
          </div>
          <component
            v-else
            :is="currentChildEntityForm"
            :entity="currentChildEntity"
            :readonly="!isEditMode"
            @submitted="onChildEntityUpdated"
            @cancel="closeChildEntityModal"
          />
        </div>
      </div>
    </div>

    <!-- Уведомления -->
    <div v-if="notification.show" class="notification" :class="notification.type">
      {{ notification.message }}
    </div>
  </div>
</template>

<script>
import { markRaw } from 'vue'
import OrganizationForm from './OrganizationForm.vue'
import AddressForm from './AddressForm.vue'
import CoordinatesForm from './CoordinatesForm.vue'
import LocationForm from './LocationForm.vue'

export default {
  name: 'OrganizationsMain',
  components: { OrganizationForm },
  data() {
    return {
      organizations: [],
      showCreateForm: false,
      showChildEntityModal: false,
      currentChildEntity: null,
      currentChildEntityType: '',
      isEditMode: false,
      loadingChildEntity: false,
      notification: {
        show: false,
        message: '',
        type: 'success'
      },
      addressForm: markRaw(AddressForm),
      coordinatesForm: markRaw(CoordinatesForm),
      locationForm: markRaw(LocationForm)
    }
  },
  computed: {
    currentChildEntityForm() {
      switch (this.currentChildEntityType) {
        case 'coordinates': return this.coordinatesForm
        case 'address': return this.addressForm
        case 'location': return this.locationForm
        default: return null
      }
    },
    getChildEntityTitle() {
      switch (this.currentChildEntityType) {
        case 'coordinates': return 'координат'
        case 'address': return 'адреса'
        case 'location': return 'локации'
        default: return 'сущности'
      }
    }
  },
  mounted() {
    this.loadOrganizations()
  },
  methods: {
    async loadOrganizations() {
      try {
        this.showNotification('Загрузка организаций...', 'info')
        const response = await this.$axios.get('/api/get/organization')
        // Добавляем флаг редактирования и копию данных для каждой организации
        this.organizations = response.data.map(org => ({
          ...org,
          editing: false,
          editingData: {}
        }))
        this.showNotification('Организации успешно загружены', 'success')
      } catch (error) {
        console.error('Ошибка загрузки организаций:', error)
        this.organizations = []
        this.showNotification('Ошибка загрузки организаций', 'error')
      }
    },

    startEditing(org) {
      // Выходим из режима редактирования для всех других организаций
      this.organizations.forEach(o => {
        if (o.id !== org.id) {
          o.editing = false
        }
      })

      // Входим в режим редактирования для выбранной организации
      org.editing = true
      // Создаем копию данных для редактирования
      org.editingData = {
        name: org.name,
        annualTurnover: org.annualTurnover,
        employeesCount: org.employeesCount,
        rating: org.rating,
        type: org.type
      }
    },

    cancelEditing(org) {
      org.editing = false
      org.editingData = {}
    },

    async saveOrganization(org) {
      try {
        // Валидация данных
        if (!this.validateOrganizationData(org.editingData)) {
          return
        }

        // Подготавливаем данные для отправки
        const updateData = {
          name: org.editingData.name,
          annualTurnover: org.editingData.annualTurnover,
          employeesCount: org.editingData.employeesCount,
          rating: org.editingData.rating,
          type: org.editingData.type
        }

        // Отправляем запрос на обновление
        await this.$axios.put(`/api/update/organization/${org.id}`, updateData)

        // Обновляем данные в локальном состоянии
        org.name = org.editingData.name
        org.annualTurnover = org.editingData.annualTurnover
        org.employeesCount = org.editingData.employeesCount
        org.rating = org.editingData.rating
        org.type = org.editingData.type

        // Выходим из режима редактирования
        org.editing = false
        org.editingData = {}

        this.showNotification('Данные организации успешно обновлены', 'success')
      } catch (error) {
        console.error('Ошибка обновления организации:', error)
        this.showNotification('Ошибка при обновлении данных организации', 'error')
      }
    },

    validateOrganizationData(data) {
      // Проверка названия
      if (!data.name || data.name.trim() === '') {
        this.showNotification('Название организации не может быть пустым', 'error')
        return false
      }

      // Проверка годового оборота
      if (data.annualTurnover < 0) {
        this.showNotification('Годовой оборот не может быть отрицательным', 'error')
        return false
      }

      // Проверка количества сотрудников
      if (data.employeesCount < 0) {
        this.showNotification('Количество сотрудников не может быть отрицательным', 'error')
        return false
      }

      // Проверка рейтинга
      if (data.rating < 0) {
        this.showNotification('Рейтинг должен быть больше 0', 'error')
        return false
      }

      return true
    },

    async viewChildEntity(entityId, type) {
      if (!entityId) {
        this.showNotification('Данные не указаны', 'info')
        return
      }
      await this.loadChildEntityData(entityId, type, false)
    },

    async loadChildEntityData(entityId, type, editMode = false) {
      this.loadingChildEntity = true
      this.showChildEntityModal = true

      try {
        let endpoint
        switch (type) {
          case 'coordinates':
            endpoint = `/api/get/coordinates/${entityId}`
            break
          case 'address':
            endpoint = `/api/get/address/${entityId}`
            break
          case 'location':
            endpoint = `/api/get/location/${entityId}`
            break
          default:
            console.error('Unknown entity type:', type)
            return
        }

        const response = await this.$axios.get(endpoint)
        this.currentChildEntity = { ...response.data }
        this.currentChildEntityType = type
        this.isEditMode = editMode

        console.log(`Загружены данные для ${type}:`, this.currentChildEntity)

      } catch (error) {
        console.error(`Ошибка загрузки ${type}:`, error)
        this.showNotification(`Ошибка загрузки ${this.getChildEntityTitle}`, 'error')
        this.closeChildEntityModal()
      } finally {
        this.loadingChildEntity = false
      }
    },

    enableEditMode() {
      this.isEditMode = true
    },

    closeChildEntityModal() {
      this.showChildEntityModal = false
      this.isEditMode = false
      this.currentChildEntity = null
      this.currentChildEntityType = ''
      this.loadingChildEntity = false
    },

    async deleteOrganization(id) {
      if (confirm('Вы уверены, что хотите удалить эту организацию?')) {
        try {
          await this.$axios.delete(`/api/delete/organization/${id}`)
          this.showNotification('Организация успешно удалена', 'success')
          await this.loadOrganizations()
        } catch (error) {
          console.error('Ошибка удаления организации:', error)
          this.showNotification('Ошибка при удалении организации', 'error')
        }
      }
    },

    onOrganizationCreated() {
      this.showCreateForm = false
      this.showNotification('Организация успешно создана', 'success')
      this.loadOrganizations()
    },

    onChildEntityUpdated() {
      this.closeChildEntityModal()
      this.showNotification(`${this.getChildEntityTitle} успешно обновлены`, 'success')
      this.loadOrganizations()
    },

    formatCurrency(value) {
      if (!value) return '0 ₽'
      return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
      }).format(value)
    },

    getRatingClass(rating) {
      if (rating >= 4) return 'rating-high'
      if (rating >= 3) return 'rating-medium'
      return 'rating-low'
    },

    getTypeClass(type) {
      const typeMap = {
        'COMMERCIAL': 'type-commercial',
        'GOVERNMENT': 'type-government',
        'PRIVATE_LIMITED_COMPANY': 'type-private',
        'OPEN_JOINT_STOCK_COMPANY': 'type-open'
      }
      return typeMap[type] || 'type-default'
    },

    showNotification(message, type = 'success') {
      this.notification = {
        show: true,
        message,
        type
      }
      setTimeout(() => {
        this.notification.show = false
      }, 3000)
    }
  }
}
</script>

<style scoped>
.organizations-main {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  position: relative;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e9ecef;
}

.header h1 {
  margin: 0;
  color: #2c3e50;
  font-size: 2rem;
  font-weight: 700;
}

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #e1e5e9;
}

.organizations-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.organizations-table th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 12px;
  text-align: left;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.5px;
  border: none;
}

.organizations-table td {
  padding: 14px 12px;
  border-bottom: 1px solid #f1f3f4;
  vertical-align: middle;
  transition: background-color 0.2s ease;
}

.organizations-table tbody tr:hover {
  background-color: #f8fafc;
  transform: scale(1);
}

.organizations-table tbody tr:last-child td {
  border-bottom: none;
}

.clickable-cell {
  color: #007bff;
  cursor: pointer;
  text-decoration: underline;
  transition: all 0.3s ease;
  font-weight: 500;
}

.clickable-cell:hover {
  color: #0056b3;
  background-color: #f0f8ff;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-btn, .delete-btn, .save-btn, .cancel-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 100px;
}

.edit-btn {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
  color: white;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.delete-btn {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
  color: white;
}

.delete-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.save-btn {
  background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
  color: white;
}

.save-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
}

.cancel-btn {
  background: linear-gradient(135deg, #6c757d 0%, #545b62 100%);
  color: white;
}

.cancel-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

.inline-input {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.inline-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.inline-input[type="number"] {
  text-align: right;
}

.rating {
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 12px;
}

.rating-high {
  background: #d4edda;
  color: #155724;
}

.rating-medium {
  background: #fff3cd;
  color: #856404;
}

.rating-low {
  background: #f8d7da;
  color: #721c24;
}

.type-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.type-commercial {
  background: #e3f2fd;
  color: #1565c0;
}

.type-government {
  background: #f3e5f5;
  color: #7b1fa2;
}

.type-private {
  background: #e8f5e8;
  color: #2e7d32;
}

.type-open {
  background: #fff3e0;
  color: #ef6c00;
}

.type-default {
  background: #f5f5f5;
  color: #424242;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(5px);
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalAppear 0.3s ease-out;
}

.large-modal {
  max-width: 900px;
}

@keyframes modalAppear {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
  background: #f8f9fa;
  border-radius: 12px 12px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.5rem;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.edit-mode-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.edit-mode-btn:hover {
  background: #0056b3;
  transform: translateY(-1px);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.close-btn:hover {
  color: #495057;
  background: #e9ecef;
}

.modal-body {
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 40px;
  font-size: 16px;
  color: #666;
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1001;
  animation: slideIn 0.3s ease-out;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.notification.success {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
}

.notification.error {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
}

.notification.info {
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Адаптивность */
@media (max-width: 1400px) {
  .table-container {
    overflow-x: auto;
  }

  .organizations-table {
    min-width: 1200px;
  }
}

@media (max-width: 768px) {
  .organizations-main {
    padding: 10px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .modal-content {
    width: 95%;
    margin: 10px;
  }

  .modal-header {
    padding: 15px;
  }

  .modal-header h3 {
    font-size: 1.2rem;
  }

  .action-buttons {
    gap: 4px;
  }

  .edit-btn, .delete-btn, .save-btn, .cancel-btn {
    padding: 6px 10px;
    font-size: 11px;
    min-width: 80px;
  }
}

@media (max-width: 480px) {
  .modal-body {
    padding: 15px;
  }

  .notification {
    right: 10px;
    left: 10px;
    text-align: center;
  }
}
</style>
