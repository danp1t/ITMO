<template>
  <div class="organization-form">
    <form @submit.prevent="handleSubmit" class="main-form">
      <header class="form-header">
        <h2 class="info_header">Организация</h2>
      </header>

      <!-- Основные поля организации -->
      <div class="form-section">
        <h3>Основная информация</h3>
        <div class="form-group">
          <label for="name">Название организации*</label>
          <input
              id="name"
              v-model="organizationData.name"
              type="text"
              :class="{ 'error-input': errors.name }"
              @blur="validateField('name')"
          >
          <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
        </div>

        <div class="form-group">
          <label for="annualTurnover">Годовой оборот*</label>
          <input
              id="annualTurnover"
              v-model="organizationData.annualTurnover"
              type="text"
              :class="{ 'error-input': errors.annualTurnover }"
              @blur="validateField('annualTurnover')"
          >
          <span v-if="errors.annualTurnover" class="error-message">{{ errors.annualTurnover }}</span>
        </div>

        <div class="form-group">
          <label for="employeesCount">Количество сотрудников*</label>
          <input
              id="employeesCount"
              v-model="organizationData.employeesCount"
              type="text"
              :class="{ 'error-input': errors.employeesCount }"
              @blur="validateField('employeesCount')"
          >
          <span v-if="errors.employeesCount" class="error-message">{{ errors.employeesCount }}</span>
        </div>

        <div class="form-group">
          <label for="rating">Рейтинг*</label>
          <input
              id="rating"
              v-model="organizationData.rating"
              type="text"
              :class="{ 'error-input': errors.rating }"
              @blur="validateField('rating')"
          >
          <span v-if="errors.rating" class="error-message">{{ errors.rating }}</span>
        </div>

        <div class="form-group">
          <label for="type">Тип организации</label>
          <select
              id="type"
              v-model="organizationData.type"
              :class="{ 'error-input': errors.type }"
          >
            <option value="">Выберите тип</option>
            <option value="COMMERCIAL">Коммерческая</option>
            <option value="PUBLIC">Публичная</option>
            <option value="PRIVATE_LIMITED_COMPANY">Частная компания</option>
            <option value="OPEN_JOINT_STOCK_COMPANY">ОАО</option>
          </select>
        </div>
      </div>

      <!-- Вложенные формы как отдельные секции -->
      <div class="form-section">
        <h3>Координаты</h3>
        <CoordinatesForm
            ref="coordinatesForm"
            @update:formData="updateCoordinates"
        />
      </div>

      <div class="form-section">
        <h3>Локация</h3>
        <LocationForm
            ref="postalAddressForm"
            @update:formData="updatePostalAddress"
        />
      </div>

      <button
          type="submit"
          :disabled="isSubmitting || hasErrors"
          class="submit-btn"
      >
        {{ isSubmitting ? 'Отправка...' : 'Создать организацию' }}
      </button>
    </form>
  </div>
</template>

<script>
import CoordinatesForm from './CoordinatesForm.vue'
import AddressForm from './AddressForm.vue'
import LocationForm from "@/components/LocationForm.vue";

export default {
  name: 'OrganizationForm',
  components: {LocationForm, CoordinatesForm, AddressForm },
  data() {
    return {
      organizationData: {
        name: '',
        annualTurnover: '',
        employeesCount: '',
        rating: '',
        type: '',
        coordinates: { x: '', y: '' },
        officialAddress: { street: '', zipCode: '' },
        postalAddress: { street: '', zipCode: '' }
      },
      errors: {},
      isSubmitting: false
    }
  },
  computed: {
    hasErrors() {
      return Object.values(this.errors).some(error => error !== '')
    }
  },
  methods: {
    validateField(fieldName) {
      const rules = {
        name: value => {
          if (!value) return 'Название организации обязательно'
          if (value.length > 255) return 'Название не должно превышать 255 символов'
          return ''
        },
        annualTurnover: value => {
          if (!value) return 'Годовой оборот обязателен'
          const numValue = parseFloat(value)
          if (isNaN(numValue)) return 'Годовой оборот должен быть числом'
          if (numValue <= 0) return 'Годовой оборот должен быть больше 0'
          return ''
        },
        employeesCount: value => {
          if (!value) return 'Количество сотрудников обязательно'
          const numValue = parseInt(value)
          if (isNaN(numValue)) return 'Количество сотрудников должно быть числом'
          if (numValue <= 0) return 'Количество сотрудников должно быть больше 0'
          return ''
        },
        rating: value => {
          if (!value) return 'Рейтинг обязателен'
          const numValue = parseInt(value)
          if (isNaN(numValue)) return 'Рейтинг должен быть числом'
          if (numValue <= 0) return 'Рейтинг должен быть больше 0'
          return ''
        }
      }

      this.errors[fieldName] = rules[fieldName]
          ? rules[fieldName](this.organizationData[fieldName])
          : ''
    },

    updateCoordinates(data) {
      this.organizationData.coordinates = data
    },

    updateOfficialAddress(data) {
      this.organizationData.officialAddress = data
    },

    updatePostalAddress(data) {
      this.organizationData.postalAddress = data
    },

    async handleSubmit() {
      // Валидация основных полей
      ['name', 'annualTurnover', 'employeesCount', 'rating'].forEach(field => {
        this.validateField(field)
      })
      //TODO: Настроить валидацию для всех полей. Даже вложенных.
      this.isSubmitting = true

      try {
        const response = await this.$http.post('/api/organizations', this.organizationData)
        this.$emit('submitted', { data: this.organizationData, response })

        if (this.$router) {
          this.$router.push('/success')
        } else {
          window.location.href = '/success'
        }
      } catch (error) {
        console.error('Ошибка создания организации:', error)
        alert('Ошибка при создании организации: ' + (error.response?.data?.message || error.message))
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
</script>

<style scoped>
.organization-form {
  max-width: 800px;
  margin: 0 auto;
}

.main-form {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background: #fafafa;
}

.form-section h3 {
  margin-top: 0;
  color: #00bfff;
  border-bottom: 1px solid #00bfff;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 2px solid #00bfff;
  border-radius: 5px;
  font-size: 16px;
  box-sizing: border-box;
}

.error-input {
  border-color: #ff4444;
}

.error-message {
  color: #ff4444;
  font-size: 14px;
  display: block;
  margin-top: 5px;
}

.submit-btn {
  background: #00bfff;
  color: white;
  padding: 12px 30px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  width: 100%;
}

.submit-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

@media (max-width: 888px) {
  .organization-form {
    margin: 10px;
  }

  .form-section {
    padding: 15px;
  }
}
</style>