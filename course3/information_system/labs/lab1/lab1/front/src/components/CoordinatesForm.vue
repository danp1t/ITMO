<template>
  <div v-if="readonly && !showFormAfterReplace" class="readonly-view">
    <h4>Текущие данные координат</h4>
    <table class="data-table">
      <tbody>
        <tr>
          <th>ID</th>
          <td>{{ entity.id }}</td>
        </tr>
        <tr>
          <th>Координата X</th>
          <td>{{ entity.x }}</td>
        </tr>
        <tr>
          <th>Координата Y</th>
          <td>{{ entity.y || 'Не указано' }}</td>
        </tr>
      </tbody>
    </table>

    <!-- Кнопка удаления в режиме просмотра -->
    <div v-if="!nested && showDeleteButton" class="delete-section">
      <button type="button" class="delete-btn" @click="showReplaceModal = true">
        Удалить и выбрать другие координаты
      </button>
    </div>
  </div>

  <!-- Форма для редактирования или после замены -->
  <form v-else @submit.prevent="submitForm" class="custom-form">
    <h3>{{ isEditMode ? 'Редактирование координат' : 'Создание координат' }}</h3>

    <div class="form-field">
      <label for="x">Координата X *</label>
      <input
        id="x"
        v-model="formData.x"
        type="number"
        required
        step="any"
        class="form-input"
      >
      <div v-if="errors.x" class="error-message">{{ errors.x }}</div>
    </div>

    <div class="form-field">
      <label for="y">Координата Y</label>
      <input
        id="y"
        v-model="formData.y"
        type="number"
        step="any"
        class="form-input"
      >
      <div v-if="errors.y" class="error-message">{{ errors.y }}</div>
    </div>

    <div class="form-actions">
      <button type="submit" class="submit-button">
        {{ isEditMode ? 'Обновить' : 'Создать' }}
      </button>

      <!-- Кнопка удаления в режиме редактирования -->
      <button
        v-if="!nested && showDeleteButton && isEditMode"
        type="button"
        class="delete-btn"
        @click="showReplaceModal = true"
      >
        Удалить и выбрать другие
      </button>

      <!-- Кнопка отмены после замены -->
      <button
        v-if="showFormAfterReplace"
        type="button"
        class="cancel-btn"
        @click="cancelReplacement"
      >
        Отмена
      </button>
    </div>
  </form>

  <!-- Модальное окно замены координат -->
  <div v-if="showReplaceModal" class="modal-overlay" @click="showReplaceModal = false">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h3>Выберите новые координаты</h3>
        <button type="button" class="close-btn" @click="showReplaceModal = false">×</button>
      </div>

      <div class="modal-body">
        <!-- Список существующих координат -->
        <div class="entities-list-section">
          <h4>Выберите существующие координаты</h4>
          <div class="entities-list">
            <div v-if="loadingCoordinates" class="loading">Загрузка координат...</div>
            <div v-else-if="availableCoordinates.length === 0" class="no-entities">
              Нет доступных координат
            </div>
            <div
              v-else
              v-for="coord in availableCoordinates"
              :key="coord.id"
              class="entity-item"
              :class="{ 'selected': selectedCoordinates && selectedCoordinates.id === coord.id }"
              @click="selectExistingCoordinates(coord)"
            >
              <div class="coordinates-info">
                <span class="coord-id">ID: {{ coord.id }}</span>
                <span class="coord-values">X: {{ coord.x }}, Y: {{ coord.y || 'не указано' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Разделитель -->
        <div class="divider">
          <span>или</span>
        </div>

        <!-- Создание новых координат -->
        <div class="create-new-section">
          <h4>Создайте новые координаты</h4>
          <CoordinatesForm
            :nested="true"
            @submitted="onNewCoordinatesCreated"
            @cancel="showReplaceModal = false"
          />
        </div>

        <!-- Кнопка подтверждения выбора -->
        <div v-if="selectedCoordinates" class="selection-confirm">
          <button type="button" class="confirm-btn" @click="confirmReplacement">
            Выбрать эти координаты
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CoordinatesForm',
  props: {
    nested: {
      type: Boolean,
      default: false
    },
    entity: {
      type: Object,
      default: null
    },
    readonly: {
      type: Boolean,
      default: false
    },
    showDeleteButton: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      formData: {
        x: '',
        y: ''
      },
      errors: {},
      showReplaceModal: false,
      availableCoordinates: [],
      loadingCoordinates: false,
      selectedCoordinates: null,
      showFormAfterReplace: false, // Новый флаг для показа формы после замены
      originalEntity: null // Сохраняем оригинальные данные
    }
  },
  computed: {
    isEditMode() {
      return !!this.entity
    },
    submitUrl() {
      return this.isEditMode ? `/api/update/coordinates/${this.entity.id}` : '/api/create/coordinates'
    }
  },
  watch: {
    entity: {
      handler(newEntity) {
        if (newEntity) {
          this.initializeForm()
          // Сохраняем оригинальные данные
          this.originalEntity = { ...newEntity }
        }
      },
      immediate: true,
      deep: true
    },
    showReplaceModal(newVal) {
      if (newVal) {
        this.loadAvailableCoordinates()
      } else {
        this.selectedCoordinates = null
      }
    }
  },
  methods: {
    initializeForm() {
      if (this.entity) {
        this.formData = {
          x: this.entity.x || '',
          y: this.entity.y || ''
        }
      } else {
        this.formData = {
          x: '',
          y: ''
        }
      }
      this.errors = {}
    },

    validateForm() {
      this.errors = {}
      let isValid = true

      // Проверка X
      if (!this.formData.x && this.formData.x !== 0) {
        this.errors.x = 'Ввод координаты X обязателен'
        isValid = false
      } else if (!/^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/.test(this.formData.x.toString())) {
        this.errors.x = 'Координата X должна быть вещественным числом'
        isValid = false
      } else if (parseFloat(this.formData.x) <= -59) {
        this.errors.x = 'Координата X должна быть больше -59'
        isValid = false
      }

      // Проверка Y
      if (this.formData.y && !/^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/.test(this.formData.y.toString())) {
        this.errors.y = 'Координата Y должна быть вещественным числом'
        isValid = false
      } else if (this.formData.y && parseFloat(this.formData.y) <= -5) {
        this.errors.y = 'Координата Y должна быть больше -5'
        isValid = false
      }

      return isValid
    },

    async submitForm() {
      if (!this.validateForm()) {
        return
      }

      try {
        const config = {
          method: this.isEditMode ? 'put' : 'post',
          url: this.submitUrl,
          data: this.formData
        }

        const response = await this.$axios(config)

        // После успешного сохранения возвращаемся в режим просмотра
        if (this.showFormAfterReplace) {
          this.showFormAfterReplace = false
        }

        this.$emit('submitted', { response })

      } catch (error) {
        console.error('Ошибка отправки формы:', error)
        this.$emit('error', error)
      }
    },

    async loadAvailableCoordinates() {
      this.loadingCoordinates = true
      try {
        const response = await this.$axios.get('/api/get/coordinates')
        // Исключаем текущие координаты из списка доступных
        this.availableCoordinates = response.data.filter(coord =>
          !this.entity || coord.id !== this.entity.id
        )
      } catch (error) {
        console.error('Ошибка загрузки координат:', error)
        this.availableCoordinates = []
      } finally {
        this.loadingCoordinates = false
      }
    },

    selectExistingCoordinates(coord) {
      this.selectedCoordinates = coord
    },

    onNewCoordinatesCreated({ response }) {
      try {
        const newCoordinates = response?.data

        if (newCoordinates && newCoordinates.id) {
          this.selectedCoordinates = newCoordinates
          // Автоматически подтверждаем выбор при создании новых координат
          this.confirmReplacement()
        }
      } catch (error) {
        console.error('Ошибка при обработке созданных координат:', error)
      }
    },

    confirmReplacement() {
      if (this.selectedCoordinates) {
        // ОБНОВЛЯЕМ ФОРМУ выбранными координатами
        this.formData = {
          x: this.selectedCoordinates.x,
          y: this.selectedCoordinates.y
        }

        // Показываем форму для редактирования вместо режима просмотра
        this.showFormAfterReplace = true

        // Эмитируем событие замены с выбранными координатами
        this.$emit('replaced', this.selectedCoordinates)
        this.showReplaceModal = false

        // Показываем уведомление об успешной замене
        this.showNotification('Координаты успешно заменены. Теперь вы можете отредактировать их при необходимости.', 'success')
      }
    },

    cancelReplacement() {
      // Возвращаемся к исходным данным
      this.initializeForm()
      this.showFormAfterReplace = false
      this.$emit('cancel-replacement')
    },

    // Метод для удаления текущих координат (если нужно именно удаление, а не замена)
    async deleteCurrentCoordinates() {
      if (!this.entity || !this.entity.id) {
        return
      }

      try {
        await this.$axios.delete(`/api/delete/coordinates/${this.entity.id}`)
        this.$emit('deleted', this.entity)
        this.showNotification('Координаты успешно удалены', 'success')
      } catch (error) {
        console.error('Ошибка удаления координат:', error)
        if (error.response && error.response.status === 409) {
          this.showNotification(error.response.data.error, 'error')
        } else {
          this.showNotification('Ошибка при удалении координат', 'error')
        }
      }
    },

    showNotification(message, type = 'success') {
      // Используем более информативное уведомление
      if (type === 'success') {
        alert(`✅ ${message}`)
      } else {
        alert(`❌ ${message}`)
      }
    }
  }
}
</script>

<style scoped>
.readonly-view {
  padding: 20px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e9ecef;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  width: 30%;
}

.data-table td {
  background-color: white;
}

.delete-section {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #e9ecef;
}

.custom-form {
  max-width: 500px;
  margin: 0 auto;
  padding: 20px;
}

.form-field {
  margin-bottom: 20px;
}

.form-field label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

.error-message {
  color: #e74c3c;
  font-size: 14px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: space-between;
  margin-top: 20px;
  flex-wrap: wrap;
}

.submit-button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  flex: 1;
  min-width: 120px;
}

.submit-button:hover {
  background-color: #2980b9;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  flex: 1;
  min-width: 120px;
}

.delete-btn:hover {
  background-color: #c0392b;
}

.cancel-btn {
  background-color: #95a5a6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  flex: 1;
  min-width: 120px;
}

.cancel-btn:hover {
  background-color: #7f8c8d;
}

/* Стили для модального окна замены */
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
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 700px;
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

.entities-list-section h4,
.create-new-section h4 {
  margin-bottom: 15px;
  color: #495057;
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

.coordinates-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.coord-id {
  font-weight: bold;
  font-size: 12px;
  opacity: 0.7;
}

.coord-values {
  font-family: monospace;
}

.divider {
  text-align: center;
  margin: 20px 0;
  position: relative;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #dee2e6;
  z-index: 1;
}

.divider span {
  background: white;
  padding: 0 15px;
  position: relative;
  z-index: 2;
  color: #6c757d;
  font-style: italic;
}

.create-new-section {
  border-top: 1px solid #dee2e6;
  padding-top: 20px;
}

.selection-confirm {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #dee2e6;
  text-align: center;
}

.confirm-btn {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.confirm-btn:hover {
  background: #218838;
}

.confirm-btn:disabled {
  background: #6c757d;
  cursor: not-allowed;
}
</style>
