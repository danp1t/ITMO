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

    <!-- Модальное окно выбора -->
    <div v-if="showSelector" class="modal-overlay" @click="showSelector = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ label }}</h3>
          <button type="button" class="close-btn" @click="showSelector = false">×</button>
        </div>

        <div class="modal-body">
          <!-- Список существующих сущностей -->
          <div class="entities-list">
            <div
                v-for="entity in entities"
                :key="entity.id"
                class="entity-item"
                :class="{ 'selected': selectedEntity && selectedEntity.id === entity.id }"
                @click="selectExisting(entity)"
            >
              {{ getEntityDisplay(entity) }}
            </div>
          </div>

          <!-- Или создать новую -->
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
    value: {
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
      selectedEntity: this.value,
      entities: []
    }
  },
  watch: {
    value(newVal) {
      this.selectedEntity = newVal
    },
    selectedEntity(newVal) {
      this.$emit('input', newVal)
    }
  },
  mounted() {
    this.loadEntities()
  },
  methods: {
    async loadEntities() {
      try {
        const response = await this.$axios.get(`/api/get/${this.entityType}`)
        this.entities = response.data
      } catch (error) {
        console.error(`Ошибка загрузки ${this.entityType}:`, error)
      }
    },

    getEntityDisplay(entity) {
      if (this.entityType === 'address') {
        return `${entity.street}, ${entity.zipCode}`
      } else if (this.entityType === 'coordinates') {
        return `X: ${entity.x}, Y: ${entity.y}`
      }
      else if (this.entityType === 'location') {
          return 'location'
      }
      return entity[this.displayField]
    },

    selectExisting(entity) {
      this.selectedEntity = entity
      this.showSelector = false
      this.$emit('selected', entity)
    },

    onNewEntityCreated({ response }) {
      const newEntity = response.data
      this.entities.push(newEntity)
      this.selectedEntity = newEntity
      this.showSelector = false
      this.$emit('selected', newEntity)
    },

    clearSelection() {
      this.selectedEntity = null
      this.$emit('cleared')
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

.select-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.selected-entity {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 8px;
}

.entity-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.entity-info {
  font-size: 14px;
}

.clear-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #dc3545;
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
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #dee2e6;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
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

.entity-item {
  padding: 10px;
  border-bottom: 1px solid #dee2e6;
  cursor: pointer;
}

.entity-item:hover {
  background: #f8f9fa;
}

.entity-item.selected {
  background: #007bff;
  color: white;
}

.create-new-section h4 {
  margin-bottom: 15px;
  color: #495057;
}
</style>
