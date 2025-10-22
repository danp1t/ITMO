<template>
  <div class="entity-selector">
    <div class="selector-header">
      <label class="form-label">{{ label }}{{ required ? '*' : '' }}</label>
      <button type="button" class="select-btn" @click="showSelector = true">
        {{ selectedEntity ? 'Изменить' : 'Выбрать' }}
      </button>
    </div>

    <div v-if="selectedEntity" class="selected-entity">
      <div class="entity-preview">
        <span class="entity-info">{{ getEntityDisplay(selectedEntity) }}</span>
        <button type="button" class="clear-btn" @click="clearSelection">×</button>
      </div>
    </div>

    <div v-if="showSelector" class="modal-overlay" @click="showSelector = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ label }}</h3>
          <button type="button" class="close-btn" @click="showSelector = false">×</button>
        </div>

        <div class="modal-body">
          <div class="entities-list">
            <div v-if="loading" class="loading">Загрузка...</div>
            <div v-else-if="entities.length === 0" class="no-entities">
              Нет доступных сущностей
            </div>
            <div
                v-else
                v-for="entity in entities"
                :key="entity.id"
                class="entity-item"
                :class="{ 'selected': selectedEntity && selectedEntity.id === entity.id }"
                @click="selectExisting(entity)"
            >
              {{ getEntityDisplay(entity) }}
            </div>
          </div>

          <div class="create-new-section">
            <h4>Или создайте новую</h4>
            <component
                :is="formComponent"
                :nested="true"
                @submitted="onNewEntityCreated"
                @cancel="showSelector = false"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EntitySelector',
  props: {
    label: {
      type: String,
      required: true
    },
    modelValue: {
      type: Object,
      default: null
    },
    required: {
      type: Boolean,
      default: false
    },
    formComponent: {
      type: Object,
      required: true
    },
    entityType: {
      type: String,
      required: true
    },
    displayField: {
      type: String,
      default: 'name'
    }
  },
  data() {
    return {
      showSelector: false,
      selectedEntity: this.modelValue,
      entities: [],
      loading: false
    }
  },
  watch: {
    modelValue(newVal) {
      this.selectedEntity = newVal
    },
    selectedEntity(newVal) {
      this.$emit('update:modelValue', newVal)
    },
    showSelector(newVal) {
      if (newVal) {
        this.loadEntities()
      }
    }
  },
  mounted() {
    this.loadEntities()
  },
  methods: {
    async loadEntities() {
      this.loading = true
      try {
        let endpoint
        switch (this.entityType) {
          case 'coordinates':
            endpoint = '/api/get/coordinates'
            break
          case 'location':
            endpoint = '/api/get/location'
            break
          case 'address':
            endpoint = '/api/get/address'
            break
          default:
            endpoint = `/api/${this.entityType}`
        }

        const response = await this.$axios.get(endpoint)
        this.entities = response.data
      } catch (error) {
        this.entities = []
      } finally {
        this.loading = false
      }
    },

    getEntityDisplay(entity) {
      if (!entity) return ''

      if (this.entityType === 'coordinates') {
        const x = entity.x !== undefined ? entity.x : 'не указано'
        const y = entity.y !== undefined ? entity.y : 'не указано'
        return `X: ${x}, Y: ${y}`
      }
      else if (this.entityType === 'location') {
        if (entity.name) {
          return entity.name
        } else if (entity.x !== undefined) {
          return `X: ${entity.x}, Y: ${entity.y}, Z: ${entity.z || 'не указано'}`
        } else {
          return 'Локация'
        }
      }
      else if (this.entityType === 'address') {
        const street = entity.street || 'не указано'
        const zipCode = entity.zipCode || 'не указано'
        return `${street}, ${zipCode}`
      }

      return entity[this.displayField] || 'Неизвестно'
    },

    selectExisting(entity) {
      this.selectedEntity = entity
      this.showSelector = false
      this.$emit('selected', entity)
      this.$emit('update:modelValue', entity)
    },

    async onNewEntityCreated(event) {
  let newEntity;

  if (event && event.response) {
    newEntity = event.response.data;
  } else if (event && event.data) {
    newEntity = event.data;
  } else {
    newEntity = event;
  }
  if (newEntity && newEntity.id) {
    this.selectedEntity = newEntity;
    this.$emit('selected', newEntity);
    this.$emit('update:modelValue', newEntity);

    await this.loadEntities();
  }

  this.showSelector = false;
},

    clearSelection() {
      this.selectedEntity = null
      this.$emit('cleared')
      this.$emit('update:modelValue', null)
    },

    validate() {
      if (this.required && !this.selectedEntity) {
        return `Поле "${this.label}" обязательно для заполнения`
      }
      return null
    }
  }
}
</script>

<style scoped>
.entity-selector {
  margin-bottom: 15px;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.form-label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 0;
}

.select-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.select-btn:hover {
  background: #0056b3;
}

.selected-entity {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 8px;
  margin-top: 8px;
}

.entity-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.entity-info {
  font-size: 14px;
  font-weight: 500;
  color: #155724;
}

.clear-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #dc3545;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn:hover {
  background: #f8d7da;
  border-radius: 50%;
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
  max-width: 600px;
  max-height: 80vh;
  overflow: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
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

.entities-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  margin-bottom: 20px;
}

.loading, .no-entities {
  padding: 20px;
  text-align: center;
  color: #6c757d;
  font-style: italic;
}

.entity-item {
  padding: 12px;
  border-bottom: 1px solid #dee2e6;
  cursor: pointer;
  transition: background-color 0.2s;
}

.entity-item:hover {
  background: #f8f9fa;
}

.entity-item.selected {
  background: #007bff;
  color: white;
}

.entity-item:last-child {
  border-bottom: none;
}

.create-new-section {
  border-top: 1px solid #dee2e6;
  padding-top: 20px;
}

.create-new-section h4 {
  margin-bottom: 15px;
  color: #495057;
  text-align: center;
}
</style>
