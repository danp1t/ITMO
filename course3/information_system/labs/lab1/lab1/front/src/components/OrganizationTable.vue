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
            <td>{{ org.name }}</td>
            <td>{{ org.annualTurnover }}</td>
            <td>{{ org.employeesCount }}</td>
            <td>{{ org.rating }}</td>
            <td
              class="clickable-cell"
              @click="editChildEntity(org.coordinates, 'coordinates')"
            >
              {{ org.coordinates ? org.coordinates.id : 'Не указано' }}
            </td>
            <td
              class="clickable-cell"
              @click="editChildEntity(org.officialAddress, 'location')"
            >
              {{ org.officialAddress ? org.officialAddress.id : 'Не указано' }}
            </td>
            <td
              class="clickable-cell"
              @click="editChildEntity(org.postalAddress, 'address')"
            >
              {{ org.postalAddress ? org.postalAddress.id : 'Не указано' }}
            </td>
            <td>{{ org.type }}</td>
            <td>
              <button class="edit-btn" @click="editOrganization(org)">Редактировать</button>
              <button class="delete-btn" @click="deleteOrganization(org.id)">Удалить</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Модальное окно создания организации -->
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

    <!-- Модальное окно редактирования дочерней сущности -->
    <div v-if="showChildEntityModal" class="modal-overlay" @click="showChildEntityModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Редактирование {{ getChildEntityTitle }}</h3>
          <button type="button" class="close-btn" @click="showChildEntityModal = false">×</button>
        </div>
        <div class="modal-body">
          <component
            :is="currentChildEntityForm"
            :entity="currentChildEntity"
            @submitted="onChildEntityUpdated"
            @cancel="showChildEntityModal = false"
          />
        </div>
      </div>
    </div>

    <!-- Модальное окно редактирования организации -->
    <div v-if="showEditOrganizationModal" class="modal-overlay" @click="showEditOrganizationModal = false">
      <div class="modal-content large-modal" @click.stop>
        <div class="modal-header">
          <h3>Редактирование организации</h3>
          <button type="button" class="close-btn" @click="showEditOrganizationModal = false">×</button>
        </div>
        <div class="modal-body">
          <OrganizationForm
            :organization="currentOrganization"
            @submitted="onOrganizationUpdated"
            @cancel="showEditOrganizationModal = false"
          />
        </div>
      </div>
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
      showEditOrganizationModal: false,
      currentChildEntity: null,
      currentChildEntityType: '',
      currentOrganization: null,
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
        const response = await this.$axios.get('/api/get/organization')
        this.organizations = response.data

        // Загружаем полные данные для дочерних сущностей
        await this.loadChildEntities()
      } catch (error) {
        console.error('Ошибка загрузки организаций:', error)
        this.organizations = []
      }
    },

    async loadChildEntities() {
      try {
        // Загружаем все дочерние сущности
        const [coordinatesResponse, addressesResponse, locationsResponse] = await Promise.all([
          this.$axios.get('/api/get/coordinates'),
          this.$axios.get('/api/get/address'),
          this.$axios.get('/api/get/location')
        ])

        const coordinates = coordinatesResponse.data
        const addresses = addressesResponse.data
        const locations = locationsResponse.data

        // Сопоставляем ID с полными объектами
        this.organizations = this.organizations.map(org => ({
          ...org,
          coordinates: coordinates.find(coord => coord.id === org.coordinates) || null,
          officialAddress: locations.find(loc => loc.id === org.officialAddress) || null,
          postalAddress: addresses.find(addr => addr.id === org.postalAddress) || null
        }))
      } catch (error) {
        console.error('Ошибка загрузки дочерних сущностей:', error)
      }
    },

    async editChildEntity(entity, type) {
      if (!entity) return

      // Если entity - это только ID, загружаем полные данные
      if (typeof entity === 'number') {
        try {
          let endpoint
          switch (type) {
            case 'coordinates':
              endpoint = `/api/get/coordinates/${entity}`
              break
            case 'address':
              endpoint = `/api/get/address/${entity}`
              break
            case 'location':
              endpoint = `/api/get/location/${entity}`
              break
          }

          const response = await this.$axios.get(endpoint)
          this.currentChildEntity = response.data
        } catch (error) {
          console.error(`Ошибка загрузки ${type}:`, error)
          return
        }
      } else {
        this.currentChildEntity = entity
      }

      this.currentChildEntityType = type
      this.showChildEntityModal = true
    },

    editOrganization(organization) {
      this.currentOrganization = organization
      this.showEditOrganizationModal = true
    },

    async deleteOrganization(id) {
      if (confirm('Вы уверены, что хотите удалить эту организацию?')) {
        try {
          await this.$axios.delete(`/api/delete/organization/${id}`)
          await this.loadOrganizations()
        } catch (error) {
          console.error('Ошибка удаления организации:', error)
          alert('Ошибка при удалении организации')
        }
      }
    },

    onOrganizationCreated() {
      this.showCreateForm = false
      this.loadOrganizations()
    },

    onOrganizationUpdated() {
      this.showEditOrganizationModal = false
      this.loadOrganizations()
    },

    onChildEntityUpdated() {
      this.showChildEntityModal = false
      this.loadOrganizations()
    }
  }
}
</script>

<style scoped>
.organizations-main {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
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
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.organizations-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.organizations-table th {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 12px;
  text-align: left;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 0.5px;
}

.organizations-table td {
  padding: 12px;
  border-bottom: 1px solid #e9ecef;
  vertical-align: middle;
}

.organizations-table tbody tr:hover {
  background-color: #f8f9fa;
}

.organizations-table tbody tr:last-child td {
  border-bottom: none;
}

.clickable-cell {
  color: #007bff;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s ease;
}

.clickable-cell:hover {
  color: #0056b3;
  background-color: #f0f8ff;
}

.edit-btn, .delete-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin: 2px;
  transition: all 0.3s ease;
}

.edit-btn {
  background: #28a745;
  color: white;
}

.edit-btn:hover {
  background: #218838;
}

.delete-btn {
  background: #dc3545;
  color: white;
}

.delete-btn:hover {
  background: #c82333;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.large-modal {
  max-width: 900px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #dee2e6;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #495057;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6c757d;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #495057;
  background: #e9ecef;
  border-radius: 50%;
}

.modal-body {
  padding: 20px;
}

/* Адаптивность */
@media (max-width: 1200px) {
  .table-container {
    overflow-x: auto;
  }

  .organizations-table {
    min-width: 1000px;
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
}
</style>
