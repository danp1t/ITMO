<template>
  <div v-if="readonly" class="readonly-view">
    <h4>Текущие данные адреса</h4>
    <table class="data-table">
      <tbody>
        <tr>
          <th>ID</th>
          <td>{{ entity.id }}</td>
        </tr>
        <tr>
          <th>Улица</th>
          <td>{{ entity.street }}</td>
        </tr>
        <tr>
          <th>Почтовый индекс</th>
          <td>{{ entity.zipCode || 'Не указано' }}</td>
        </tr>
      </tbody>
    </table>
  </div>

  <form v-else @submit.prevent="submitForm" class="custom-form">
    <h3>{{ isEditMode ? 'Редактирование адреса' : 'Создание адреса' }}</h3>

    <div class="form-field">
      <label for="street">Улица *</label>
      <input
        id="street"
        v-model="formData.street"
        type="text"
        required
        maxlength="180"
        class="form-input"
      >
      <div v-if="errors.street" class="error-message">{{ errors.street }}</div>
    </div>

    <div class="form-field">
      <label for="zipCode">Почтовый индекс *</label>
      <input
        id="zipCode"
        v-model="formData.zipCode"
        type="text"
        required
        maxlength="30"
        class="form-input"
      >
      <div v-if="errors.zipCode" class="error-message">{{ errors.zipCode }}</div>
    </div>

    <button type="submit" class="submit-button">
      {{ isEditMode ? 'Обновить' : 'Создать' }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'AddressForm',
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
        street: '',
        zipCode: ''
      },
      errors: {}
    }
  },
  computed: {
    isEditMode() {
      return !!this.entity
    },
    submitUrl() {
      return this.isEditMode ? `/api/address/${this.entity.id}` : '/api/address'
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
          street: this.entity.street || '',
          zipCode: this.entity.zipCode || ''
        }
      } else {
        this.formData = {
          street: '',
          zipCode: ''
        }
      }
      this.errors = {}
    },

    validateForm() {
      this.errors = {}
      let isValid = true

      if (!this.formData.street) {
        this.errors.street = 'Ввод улицы обязателен'
        isValid = false
      } else if (this.formData.street.trim().length === 0) {
        this.errors.street = 'Улица не может быть пустой строкой'
        isValid = false
      } else if (this.formData.street.length > 180) {
        this.errors.street = 'Длина улицы не должна превышать 180 символов'
        isValid = false
      }

      if (!this.formData.zipCode) {
        this.errors.zipCode = 'Ввод почтового индекса обязателен'
        isValid = false
      } else if (this.formData.zipCode.trim().length === 0) {
        this.errors.zipCode = 'Почтовый индекс не может быть пустым'
        isValid = false
      } else if (this.formData.zipCode.length > 30) {
        this.errors.zipCode = 'Длина почтового индекса не должна превышать 30 символов'
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
