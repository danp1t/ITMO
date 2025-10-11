<template>
  <div v-if="readonly" class="readonly-view">
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
  </div>

  <!-- Кастомная форма для гарантированного заполнения -->
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

    <button type="submit" class="submit-button">
      {{ isEditMode ? 'Обновить' : 'Создать' }}
    </button>
  </form>
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
    }
  },
  data() {
    return {
      formData: {
        x: '',
        y: ''
      },
      errors: {}
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
        }
      },
      immediate: true,
      deep: true
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
        this.$emit('submitted', { response })

      } catch (error) {
        console.error('Ошибка отправки формы:', error)
        this.$emit('error', error)
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

.submit-button {
  background-color: #3498db;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

.submit-button:hover {
  background-color: #2980b9;
}
</style>
