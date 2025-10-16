<template>
  <div class="organizations-main">
    <div class="header">
      <h1>Организации</h1>
      <button class="create-btn" @click="showCreateForm = true">
        Создать организацию
      </button>
    </div>

    <div class="filters-panel">
      <div class="search-section">
        <div class="search-input-wrapper">
          <input
            v-model="filters.search"
            type="text"
            placeholder="Поиск по названию..."
            class="search-input"
            @input="applyFilters"
          >
          <span class="search-icon">🔍</span>
        </div>
      </div>

      <div class="filter-controls">
        <select v-model="filters.sortBy" class="filter-select" @change="applySorting">
          <option value="">Сортировка по...</option>
          <option value="name">Названию</option>
          <option value="annualTurnover">Годовому обороту</option>
          <option value="employeesCount">Количеству сотрудников</option>
          <option value="rating">Рейтингу</option>
          <option value="type">Типу</option>
        </select>

        <select v-model="filters.sortOrder" class="filter-select" @change="applySorting">
          <option value="asc">По возрастанию</option>
          <option value="desc">По убыванию</option>
        </select>

        <select v-model="filters.type" class="filter-select" @change="applyFilters">
          <option value="">Все типы</option>
          <option value="COMMERCIAL">COMMERCIAL</option>
          <option value="GOVERNMENT">GOVERNMENT</option>
          <option value="PRIVATE_LIMITED_COMPANY">PRIVATE_LIMITED_COMPANY</option>
          <option value="OPEN_JOINT_STOCK_COMPANY">OPEN_JOINT_STOCK_COMPANY</option>
        </select>

        <button class="clear-filters-btn" @click="clearFilters">
          Сбросить фильтры
        </button>
      </div>
    </div>

    <div v-if="isFiltered" class="filter-info">
      <span>Найдено организаций: {{ filteredOrganizations.length }}</span>
      <button class="clear-filters-small" @click="clearFilters">
        × Сбросить
      </button>
    </div>

    <div class="table-container">
      <table class="organizations-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>
              <div class="sortable-header" @click="toggleSort('name')">
                Название
                <span v-if="filters.sortBy === 'name'" class="sort-indicator">
                  {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th>
              <div class="sortable-header" @click="toggleSort('annualTurnover')">
                Годовой оборот
                <span v-if="filters.sortBy === 'annualTurnover'" class="sort-indicator">
                  {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th>
              <div class="sortable-header" @click="toggleSort('employeesCount')">
                Количество сотрудников
                <span v-if="filters.sortBy === 'employeesCount'" class="sort-indicator">
                  {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th>
              <div class="sortable-header" @click="toggleSort('rating')">
                Рейтинг
                <span v-if="filters.sortBy === 'rating'" class="sort-indicator">
                  {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th>Координаты</th>
            <th>Официальный адрес</th>
            <th>Почтовый адрес</th>
            <th>
              <div class="sortable-header" @click="toggleSort('type')">
                Тип
                <span v-if="filters.sortBy === 'type'" class="sort-indicator">
                  {{ filters.sortOrder === 'asc' ? '↑' : '↓' }}
                </span>
              </div>
            </th>
            <th>Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="org in paginatedOrganizations" :key="org.id">
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

      <div v-if="filteredOrganizations.length === 0" class="no-results">
        <p>Организации не найдены</p>
        <button v-if="isFiltered" class="clear-filters-btn" @click="clearFilters">
          Сбросить фильтры
        </button>
      </div>

      <!-- Пагинация -->
      <div v-if="filteredOrganizations.length > 0" class="pagination">
        <div class="pagination-info">
          Показано {{ startItem }}-{{ endItem }} из {{ filteredOrganizations.length }}
        </div>
        <div class="pagination-controls">
          <button
            class="pagination-btn"
            :disabled="currentPage === 1"
            @click="changePage(currentPage - 1)"
          >
            ‹
          </button>

          <button
            v-for="page in visiblePages"
            :key="page"
            class="pagination-btn"
            :class="{ active: page === currentPage }"
            @click="changePage(page)"
          >
            {{ page }}
          </button>

          <button
            class="pagination-btn"
            :disabled="currentPage === totalPages"
            @click="changePage(currentPage + 1)"
          >
            ›
          </button>
        </div>
        <div class="pagination-size">
          <select v-model="itemsPerPage" class="page-size-select" @change="changePageSize">
            <option value="10">10 на странице</option>
            <option value="25">25 на странице</option>
            <option value="50">50 на странице</option>
          </select>
        </div>
      </div>
    </div>

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
      filteredOrganizations: [],
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
      filters: {
        search: '',
        type: '',
        sortBy: '',
        sortOrder: 'asc'
      },
      addressForm: markRaw(AddressForm),
      coordinatesForm: markRaw(CoordinatesForm),
      locationForm: markRaw(LocationForm),
      // Пагинация
      currentPage: 1,
      itemsPerPage: 10
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
    },
    isFiltered() {
      return this.filters.search || this.filters.type || this.filters.sortBy
    },
    // Вычисляемые свойства для пагинации
    totalPages() {
      return Math.ceil(this.filteredOrganizations.length / this.itemsPerPage)
    },
    startItem() {
      return (this.currentPage - 1) * this.itemsPerPage + 1
    },
    endItem() {
      const end = this.currentPage * this.itemsPerPage
      return end > this.filteredOrganizations.length ? this.filteredOrganizations.length : end
    },
    paginatedOrganizations() {
      const startIndex = (this.currentPage - 1) * this.itemsPerPage
      const endIndex = startIndex + this.itemsPerPage
      return this.filteredOrganizations.slice(startIndex, endIndex)
    },
    visiblePages() {
      const pages = []
      const total = this.totalPages
      let start = Math.max(1, this.currentPage - 2)
      let end = Math.min(total, start + 4)

      if (end - start < 4) {
        start = Math.max(1, end - 4)
      }

      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
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
        this.organizations = response.data.map(org => ({
          ...org,
          editing: false,
          editingData: {}
        }))
        this.filteredOrganizations = [...this.organizations]
        this.currentPage = 1 // Сброс на первую страницу при загрузке
        this.showNotification('Организации успешно загружены', 'success')
      } catch (error) {
        console.error('Ошибка загрузки организаций:', error)
        this.organizations = []
        this.filteredOrganizations = []
        this.showNotification('Ошибка загрузки организаций', 'error')
      }
    },

    applyFilters() {
      let filtered = [...this.organizations]

      if (this.filters.search) {
        const searchLower = this.filters.search.toLowerCase()
        filtered = filtered.filter(org =>
          org.name.toLowerCase().includes(searchLower)
        )
      }

      if (this.filters.type) {
        filtered = filtered.filter(org => org.type === this.filters.type)
      }

      this.filteredOrganizations = filtered
      this.currentPage = 1 // Сброс на первую страницу при применении фильтров
      this.applySorting()
    },

    applySorting() {
      if (!this.filters.sortBy) {
        return
      }

      this.filteredOrganizations.sort((a, b) => {
        let aValue = a[this.filters.sortBy]
        let bValue = b[this.filters.sortBy]

        if (typeof aValue === 'string') {
          aValue = aValue.toLowerCase()
          bValue = bValue.toLowerCase()
        }

        let result = 0
        if (aValue < bValue) result = -1
        if (aValue > bValue) result = 1

        if (this.filters.sortOrder === 'desc') {
          result = -result
        }

        return result
      })
    },

    toggleSort(field) {
      if (this.filters.sortBy === field) {
        this.filters.sortOrder = this.filters.sortOrder === 'asc' ? 'desc' : 'asc'
      } else {
        this.filters.sortBy = field
        this.filters.sortOrder = 'asc'
      }
      this.applySorting()
    },

    clearFilters() {
      this.filters = {
        search: '',
        type: '',
        sortBy: '',
        sortOrder: 'asc'
      }
      this.filteredOrganizations = [...this.organizations]
      this.currentPage = 1 // Сброс на первую страницу при сбросе фильтров
    },

    // Методы пагинации
    changePage(page) {
      if (page >= 1 && page <= this.totalPages) {
        this.currentPage = page
      }
    },

    changePageSize() {
      this.currentPage = 1 // Сброс на первую страницу при изменении размера страницы
    },

    startEditing(org) {
      this.organizations.forEach(o => {
        if (o.id !== org.id) {
          o.editing = false
        }
      })

      org.editing = true
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
        if (!this.validateOrganizationData(org.editingData)) {
          return
        }

        const updateData = {
          name: org.editingData.name,
          annualTurnover: org.editingData.annualTurnover,
          employeesCount: org.editingData.employeesCount,
          rating: org.editingData.rating,
          type: org.editingData.type
        }

        await this.$axios.put(`/api/update/organization/${org.id}`, updateData)

        const originalOrg = this.organizations.find(o => o.id === org.id)
        if (originalOrg) {
          originalOrg.name = org.editingData.name
          originalOrg.annualTurnover = org.editingData.annualTurnover
          originalOrg.employeesCount = org.editingData.employeesCount
          originalOrg.rating = org.editingData.rating
          originalOrg.type = org.editingData.type
        }

        org.editing = false
        org.editingData = {}

        this.applyFilters()

        this.showNotification('Данные организации успешно обновлены', 'success')
      } catch (error) {
        console.error('Ошибка обновления организации:', error)
        this.showNotification('Ошибка при обновлении данных организации', 'error')
      }
    },

    validateOrganizationData(data) {
      if (!data.name || data.name.trim() === '') {
        this.showNotification('Название организации не может быть пустым', 'error')
        return false
      }

      if (data.annualTurnover < 0) {
        this.showNotification('Годовой оборот не может быть отрицательным', 'error')
        return false
      }

      if (data.employeesCount < 0) {
        this.showNotification('Количество сотрудников не может быть отрицательным', 'error')
        return false
      }

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

.filters-panel {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  border: 1px solid #e1e5e9;
}

.search-section {
  margin-bottom: 15px;
}

.search-input-wrapper {
  position: relative;
  max-width: 400px;
}

.search-input {
  width: 100%;
  padding: 12px 45px 12px 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.3s ease;
  background: #f8f9fa;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.search-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #6c757d;
}

.filter-controls {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.filter-select {
  padding: 10px 15px;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 180px;
}

.filter-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.clear-filters-btn {
  background: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  margin-left: auto;
}

.clear-filters-btn:hover {
  background: #545b62;
  transform: translateY(-1px);
}

.filter-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #e7f3ff;
  padding: 12px 20px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid #007bff;
}

.clear-filters-small {
  background: none;
  border: 1px solid #007bff;
  color: #007bff;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.clear-filters-small:hover {
  background: #007bff;
  color: white;
}

/* Сортируемые заголовки */
.sortable-header {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  transition: color 0.3s ease;
  padding: 8px 0;
}

.sortable-header:hover {
  color: #667eea;
}

.sort-indicator {
  font-weight: bold;
  color: #667eea;
  font-size: 14px;
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

.no-results {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

.no-results p {
  margin: 0 0 15px 0;
  font-size: 16px;
}

/* Пагинация */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
  flex-wrap: wrap;
  gap: 15px;
}

.pagination-info {
  color: #6c757d;
  font-size: 14px;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  gap: 5px;
}

.pagination-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #dee2e6;
  background: white;
  color: #007bff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background: #e7f1ff;
  border-color: #007bff;
  transform: translateY(-1px);
}

.pagination-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.pagination-btn:disabled {
  color: #6c757d;
  cursor: not-allowed;
  background: #f8f9fa;
}

.pagination-size {
  display: flex;
  align-items: center;
}

.page-size-select {
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-size-select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
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

  .filters-panel {
    padding: 15px;
  }

  .filter-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    min-width: auto;
    width: 100%;
  }

  .clear-filters-btn {
    margin-left: 0;
    width: 100%;
  }

  .pagination {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .pagination-controls {
    order: 1;
  }

  .pagination-info {
    order: 2;
  }

  .pagination-size {
    order: 3;
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

  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
