<template>
  <div class="organization-form-wrapper">
    <div v-if="serverError" class="server-error">
        <div class="error-content">
          <h4>Ошибка создания организации</h4>
          <p>{{ serverError }}</p>
        </div>
        <button class="close-error" @click="serverError = ''">×</button>
      </div>
    <BaseForm
        title="Создание организации"
        :fields-config="fieldsConfig"
        :custom-validators="customValidators"
        submit-button-text="Создать организацию"
        container-class="organization-form"
        @submitted="onSubmitted"
        @submit="onSubmit"
        :key="formKey"
    >
      <template #before-fields>
        <div class="form-section-header">
          <h3>Основная информация</h3>
          <p>Заполните основные данные организации</p>
        </div>
      </template>

      <template #after-fields>
        <div class="form-section-header">
          <h3>Дополнительные данные</h3>
          <p>Выберите связанные сущности</p>
        </div>

        <div class="entities-grid">
          <div class="form-group entity-group">
            <label class="form-label">Координаты организации*</label>
            <EntitySelector
                v-model="formData.coordinates"
                label="Выбрать координаты"
                :required="true"
                :form-component="coordinatesForm"
                entity-type="coordinates"
                display-field="displayName"
                @selected="onCoordinatesSelected"
                @cleared="onCoordinatesCleared"
            />
            <div v-if="formData.coordinates" class="selected-entity-info">
              <span class="entity-preview">{{ coordinatesDisplay }}</span>
            </div>
            <span v-if="errors.coordinates" class="error-message">
              {{ errors.coordinates }}
            </span>
          </div>

          <div class="form-group entity-group">
            <label class="form-label">Официальный адрес*</label>
            <EntitySelector
                v-model="formData.officialAddress"
                label="Выбрать адрес"
                :required="true"
                :form-component="locationForm"
                entity-type="location"
                display-field="displayName"
                @selected="onOfficialAddressSelected"
                @cleared="onOfficialAddressCleared"
            />
            <div v-if="formData.officialAddress" class="selected-entity-info">
              <span class="entity-preview">{{ officialAddressDisplay }}</span>
            </div>
            <span v-if="errors.officialAddress" class="error-message">
              {{ errors.officialAddress }}
            </span>
          </div>

          <div class="form-group entity-group">
            <label class="form-label">Почтовый адрес*</label>
            <EntitySelector
                v-model="formData.postalAddress"
                label="Выбрать адрес"
                :required="true"
                :form-component="addressForm"
                entity-type="address"
                display-field="displayName"
                @selected="onPostalAddressSelected"
                @cleared="onPostalAddressCleared"
            />
            <div v-if="formData.postalAddress" class="selected-entity-info">
              <span class="entity-preview">{{ postalAddressDisplay }}</span>
            </div>
            <span v-if="errors.postalAddress" class="error-message">
              {{ errors.postalAddress }}
            </span>
          </div>

          <div class="form-group entity-group">
            <label class="form-label">Тип организации*</label>
            <div class="type-selector">
              <select
                  v-model="formData.type"
                  class="form-select type-select"
                  :class="{ 'error-input': errors.type }"
                  @change="onTypeChange"
              >
                <option value="">Выберите тип организации</option>
                <option value="COMMERCIAL">COMMERCIAL</option>
                <option value="PUBLIC">PUBLIC</option>
                <option value="GOVERNMENT">GOVERNMENT</option>
                <option value="TRUST">TRUST</option>
                <option value="PRIVATE_LIMITED_COMPANY">PRIVATE_LIMITED_COMPANY</option>
              </select>
              <div class="select-arrow">▼</div>
            </div>
            <span v-if="errors.type" class="error-message">
              {{ errors.type }}
            </span>
          </div>
        </div>



        <div v-if="hasSelectedEntities" class="selected-summary">
          <h4>Выбранные данные:</h4>
          <div class="summary-grid">
            <div v-if="formData.coordinates" class="summary-item">
              <strong>Координаты:</strong> {{ coordinatesDisplay }}
            </div>
            <div v-if="formData.officialAddress" class="summary-item">
              <strong>Официальный адрес:</strong> {{ officialAddressDisplay }}
            </div>
            <div v-if="formData.postalAddress" class="summary-item">
              <strong>Почтовый адрес:</strong> {{ postalAddressDisplay }}
            </div>
            <div v-if="formData.type" class="summary-item">
              <strong>Тип:</strong> {{ typeDisplay }}
            </div>
          </div>
        </div>
      </template>
    </BaseForm>
  </div>
</template>

<script>
import { markRaw } from 'vue'
import BaseForm from './BaseForm.vue'
import EntitySelector from './EntitySelector.vue'
import AddressForm from './AddressForm.vue'
import CoordinatesForm from './CoordinatesForm.vue'
import LocationForm from "@/components/LocationForm.vue";

export default {
  name: 'OrganizationForm',
  components: { BaseForm, EntitySelector },
  data() {
    return {
      addressForm: markRaw(AddressForm),
      locationForm: markRaw(LocationForm),
      coordinatesForm: markRaw(CoordinatesForm),
      formData: {
        name: '',
        annualTurnover: '',
        employeesCount: '',
        rating: '',
        coordinates: null,
        officialAddress: null,
        postalAddress: null,
        type: ''
      },
      errors: {
        coordinates: '',
        officialAddress: '',
        postalAddress: '',
        type: ''
      },
      serverError: '',
      isSubmitting: false,
      formKey: 0,
      fieldsConfig: [
        {
          name: 'name',
          label: 'Название организации',
          type: 'text',
          required: true,
          placeholder: 'Введите название организации',
          errorMessages: {
            required: 'Название организации обязательно для заполнения',
          }
        },
        {
          name: 'annualTurnover',
          label: 'Годовой оборот',
          type: 'number',
          required: true,
          placeholder: 'Введите годовой оборот',
          errorMessages: {
            required: 'Годовой оборот обязательно для заполнения'
          }
        },
        {
          name: 'employeesCount',
          label: 'Количество сотрудников',
          type: 'number',
          required: true,
          placeholder: 'Введите количество сотрудников',
          errorMessages: {
            required: 'Количество сотрудников обязательно для заполнения'
          }
        },
        {
          name: 'rating',
          label: 'Рейтинг организации',
          type: 'number',
          required: true,
          placeholder: 'Введите рейтинг',
          errorMessages: {
            required: 'Рейтинг обязателен для заполнения'
          }
        },
      ],
      customValidators: {
        name: (value) => {
          if (value && value.trim().length === 0) {
            return 'Название организации не может быть пустым'
          }
          else if (value && value.trim().length > 256) {
            return 'Название организации слишком длинное'
          }
          return null
        },
        annualTurnover: (value) => {
          const num = parseFloat(value)
          if (value && value.trim().length > 40) {
            return 'Введенный годовой оборот слишком большой'
          }
          else if (value && num <= 0) {
            return 'Годовой оборот должен быть больше 0'
          }
          else if (value && num > 1000000000000000) {
            return 'Введенный годовой оборот слишком большой'
          }
          return null
        },
        employeesCount: (value) => {
          const num = parseInt(value)
          if (value && value.trim().length > 40) {
            return 'Введенное количество соотрудников слишком большое'
          }
          else if (value && num <= 0) {
            return 'Количество сотрудников должно быть больше 0'
          }
          else if (value && num > 100000000000) {
            return 'Введенное количество соотрудников слишком большое'
          }
          else if (!/^\d+$/.test(value.toString())) {
            return 'Количество сотрудников должно быть целым числом'
          }
          return null
        },
        rating: (value) => {
          const num = parseInt(value)
          if (value && value.trim().length > 40) {
            return 'Введенный рейтинг слишком большой'
          }
          else if (value && num <= 0) {
            return 'Рейтинг должен быть больше 0'
          }
          else if (value && num > 100000000000) {
            return 'Введенный рейтинг слишком большой'
          }
          else if (!/^\d+$/.test(value.toString())) {
            return 'Рейтинг должен быть целым числом'
          }
          return null
        },
      }
    }
  },
  computed: {
    coordinatesDisplay() {
      if (!this.formData.coordinates) return ''
      const target = this.formData.coordinates?.['[[Target]]'] || this.formData.coordinates
      const x = target.x !== undefined ? target.x : 'не указано'
      const y = target.y !== undefined ? target.y : 'не указано'
      return `X: ${x}, Y: ${y}`
    },
    officialAddressDisplay() {
      if (!this.formData.officialAddress) return ''
      const target = this.formData.officialAddress?.['[[Target]]'] || this.formData.officialAddress

      if (target.name) {
        return target.name
      } else if (target.x !== undefined) {
        return `X: ${target.x}, Y: ${target.y}, Z: ${target.z}`
      } else {
        return 'Локация выбрана'
      }
    },
    postalAddressDisplay() {
      if (!this.formData.postalAddress) return ''
      const target = this.formData.postalAddress?.['[[Target]]'] || this.formData.postalAddress
      const street = target.street || 'не указано'
      const zipCode = target.zipCode || 'не указано'
      return `${street}, ${zipCode}`
    },
    typeDisplay() {
      const types = {
        'COMMERCIAL': 'COMMERCIAL',
        'PUBLIC': 'PUBLIC',
        'GOVERNMENT': 'GOVERNMENT',
        'TRUST': 'TRUST',
        'PRIVATE_LIMITED_COMPANY': 'PRIVATE_LIMITED_COMPANY'
      }
      return types[this.formData.type] || ''
    },
    hasSelectedEntities() {
      return this.formData.coordinates || this.formData.officialAddress ||
          this.formData.postalAddress || this.formData.type
    }
  },
  methods: {
    onCoordinatesSelected(coordinates) {
      this.formData.coordinates = coordinates
      this.errors.coordinates = ''
    },

    onCoordinatesCleared() {
      this.formData.coordinates = null
      this.errors.coordinates = 'Необходимо выбрать координаты'
    },

    onOfficialAddressSelected(location) {
      this.formData.officialAddress = location
      this.errors.officialAddress = ''
    },

    onOfficialAddressCleared() {
      this.formData.officialAddress = null
      this.errors.officialAddress = 'Необходимо выбрать официальный адрес'
    },

    onPostalAddressSelected(address) {
      this.formData.postalAddress = address
      this.errors.postalAddress = ''
    },

    onPostalAddressCleared() {
      this.formData.postalAddress = null
      this.errors.postalAddress = 'Необходимо выбрать почтовый адрес'
    },

    onTypeChange() {
      this.errors.type = ''
    },

    validateForm() {
      let isValid = true

      if (!this.formData.coordinates) {
        this.errors.coordinates = 'Необходимо выбрать координаты'
        isValid = false
      } else {
        this.errors.coordinates = ''
      }

      if (!this.formData.officialAddress) {
        this.errors.officialAddress = 'Необходимо выбрать официальный адрес'
        isValid = false
      } else {
        this.errors.officialAddress = ''
      }

      if (!this.formData.postalAddress) {
        this.errors.postalAddress = 'Необходимо выбрать почтовый адрес'
        isValid = false
      } else {
        this.errors.postalAddress = ''
      }

      if (!this.formData.type) {
        this.errors.type = 'Необходимо выбрать тип организации'
        isValid = false
      } else {
        this.errors.type = ''
      }

      return isValid
    },

    async onSubmit(formData) {
      if (this.isSubmitting) {
        return
      }

      this.serverError = ''

      if (!this.validateForm()) {
        return
      }

      this.isSubmitting = true

      const dataToSend = {
        ...formData,
        coordinates: this.formData.coordinates,
        officialAddress: this.formData.officialAddress,
        postalAddress: this.formData.postalAddress,
        type: this.formData.type
      }

      try {
        const response = await this.$axios?.post('/api/organization', dataToSend)
        this.$emit('submitted', { response, data: dataToSend })

        this.resetForm()
      } catch (error) {
        console.error('Ошибка при создании организации:', error)
        this.$emit('error', error)
      } finally {
        this.isSubmitting = false
      }
    },

    handleServerError(error) {
      if (error.response?.data?.error) {
        const errorMessage = error.response.data.error

        if (errorMessage.includes('Длина строки name не должна быть больше 256')) {
          this.serverError = 'Название организации слишком длинное. Максимальная длина - 256 символов.'
        } else if (errorMessage.includes('annualTurnover') || errorMessage.includes('годовой оборот')) {
          this.serverError = 'Ошибка в данных годового оборота. Проверьте введенное значение.'
        } else if (errorMessage.includes('employeesCount') || errorMessage.includes('количество сотрудников')) {
          this.serverError = 'Ошибка в данных о количестве сотрудников. Проверьте введенное значение.'
        } else if (errorMessage.includes('rating') || errorMessage.includes('рейтинг')) {
          this.serverError = 'Ошибка в данных рейтинга. Проверьте введенное значение.'
        } else {
          this.serverError = errorMessage
        }
      } else {
        this.serverError = 'Произошла ошибка при создании организации. Пожалуйста, попробуйте еще раз.'
      }
    },

    resetForm() {
      this.formData = {
        name: '',
        annualTurnover: '',
        employeesCount: '',
        rating: '',
        coordinates: null,
        officialAddress: null,
        postalAddress: null,
        type: ''
      }

      this.errors = {
        coordinates: '',
        officialAddress: '',
        postalAddress: '',
        type: ''
      }

      this.serverError = ''
      this.formKey++
    },

    onSubmitted({ response }) {
      if (this.$router) {
        this.$router.push('/success')
      }
    }
  }
}
</script>

<style scoped>
.organization-form-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.form-section-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 25px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-section-header h3 {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
}

.form-section-header p {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.entities-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 25px;
}

.entity-group {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.entity-group:hover {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-color: #007bff;
}

.entity-group .form-label {
  font-weight: 600;
  color: #495057;
  margin-bottom: 10px;
  display: block;
}

.selected-entity-info {
  margin-top: 10px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #28a745;
  border-radius: 4px;
  border-left: 4px solid #28a745;
}

.entity-preview {
  font-size: 14px;
  color: #155724;
  font-weight: 500;
}

.type-selector {
  position: relative;
  width: 100%;
}

.type-select {
  appearance: none;
  padding-right: 35px;
  background: white;
  width: 100%;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  padding: 12px 15px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: white;
  cursor: pointer;
}

.type-select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
}

.type-select.error-input {
  border-color: #e53e3e;
  box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1);
 }

.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #6c757d;
  transition: transform 0.2s ease;
}

.server-error {
  display: flex;
  align-items: flex-start;
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
  border: 1px solid #fc8181;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  color: #c53030;
  animation: shake 0.5s ease-in-out;
}

.error-content {
  flex: 1;
}

.error-content h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
}

.error-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
}

.close-error {
  background: none;
  border: none;
  font-size: 20px;
  color: #c53030;
  cursor: pointer;
  padding: 0;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.close-error:hover {
  background-color: rgba(197, 48, 48, 0.1);
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.type-select:focus + .select-arrow {
  transform: translateY(-50%) rotate(180deg);
}

.selected-summary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.selected-summary h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.summary-item {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  font-size: 14px;
}

.summary-item strong {
  display: block;
  margin-bottom: 2px;
  font-size: 12px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .organization-form-wrapper {
    padding: 10px;
  }

  .server-error {
    padding: 12px;
    flex-direction: column;
    gap: 8px;
  }

  .close-error {
    align-self: flex-end;
    margin-top: -8px;
  }

  .entities-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .form-section-header {
    padding: 12px 15px;
    margin-bottom: 20px;
  }
}

.entity-group {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.organization-form.form-container) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

:deep(.organization-form .form-header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px 12px 0 0;
  padding: 20px;
}

:deep(.organization-form .form-header .info_header) {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

:deep(.organization-form .form-fields) {
  padding: 30px;
}

:deep(.organization-form .form-group) {
  margin-bottom: 20px;
}

:deep(.organization-form .form-label) {
  font-weight: 600;
  color: #495057;
  margin-bottom: 8px;
  font-size: 14px;
}

:deep(.organization-form .form-input) {
  border: 2px solid #e9ecef;
  border-radius: 6px;
  padding: 12px 15px;
  font-size: 14px;
  transition: all 0.3s ease;
}

:deep(.organization-form .form-input:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.organization-form .submit-btn) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  padding: 15px 30px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 20px;
  transition: all 0.3s ease;
}

:deep(.organization-form .submit-btn:hover:not(:disabled)) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

:deep(.organization-form .submit-btn:disabled) {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}
</style>
