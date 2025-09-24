<template>
  <form @submit.prevent="handleSubmit" class="form-container">
    <header class="form-header">
      <h2 class="info_header">{{ title }}</h2>
    </header>

    <!-- Слот для дополнительного контента перед полями -->
    <slot name="before-fields"></slot>

    <!-- Динамические поля формы -->
    <div
        v-for="field in fieldsConfig"
        :key="field.name"
        class="form-group"
    >
      <label :for="field.name">
        {{ field.label }}{{ field.required ? '*' : '' }}
      </label>

      <!-- Текстовое поле -->
      <input
          v-if="field.type === 'text' || field.type === 'email' || field.type === 'password'"
          :id="field.name"
          v-model="formData[field.name]"
          :type="field.type"
          :placeholder="field.placeholder"
          :maxlength="field.maxLength"
          :class="{ 'error-input': errors[field.name] }"
          @blur="validateField(field.name)"
      >

      <!-- Textarea -->
      <textarea
          v-else-if="field.type === 'textarea'"
          :id="field.name"
          v-model="formData[field.name]"
          :placeholder="field.placeholder"
          :maxlength="field.maxLength"
          :rows="field.rows || 4"
          :class="{ 'error-input': errors[field.name] }"
          @blur="validateField(field.name)"
      ></textarea>

      <!-- Select -->
      <select
          v-else-if="field.type === 'select'"
          :id="field.name"
          v-model="formData[field.name]"
          :class="{ 'error-input': errors[field.name] }"
          @blur="validateField(field.name)"
      >
        <option value="">Выберите вариант</option>
        <option
            v-for="option in field.options"
            :key="option.value"
            :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>

      <!-- Checkbox -->
      <div v-else-if="field.type === 'checkbox'" class="checkbox-group">
        <input
            :id="field.name"
            v-model="formData[field.name]"
            type="checkbox"
            :class="{ 'error-input': errors[field.name] }"
            @blur="validateField(field.name)"
        >
        <label :for="field.name" class="checkbox-label">
          {{ field.label }}
        </label>
      </div>

      <span v-if="errors[field.name]" class="error-message">
        {{ errors[field.name] }}
      </span>
    </div>

    <!-- Слот для дополнительного контента после полей -->
    <slot name="after-fields"></slot>

    <button
        type="submit"
        :disabled="isSubmitting || hasErrors"
        class="submit-btn"
    >
      {{ isSubmitting ? 'Отправка...' : submitButtonText }}
    </button>
  </form>
</template>

<script>
export default {
  name: 'BaseForm',
  props: {
    title: {
      type: String,
      default: 'Форма'
    },
    fieldsConfig: {
      type: Array,
      required: true,
      validator: (value) => {
        return value.every(field =>
            field.name && field.label && field.type
        )
      }
    },
    submitButtonText: {
      type: String,
      default: 'Отправить'
    },
    submitUrl: {
      type: String,
      required: true
    },
    // Дополнительные правила валидации
    customValidators: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    // Динамически инициализируем formData на основе fieldsConfig
    const initialFormData = {}
    const initialErrors = {}

    this.fieldsConfig.forEach(field => {
      initialFormData[field.name] = field.defaultValue || ''
      initialErrors[field.name] = ''
    })

    return {
      formData: initialFormData,
      errors: initialErrors,
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
      const fieldConfig = this.fieldsConfig.find(f => f.name === fieldName)
      if (!fieldConfig) return

      const value = this.formData[fieldName]
      let error = ''

      // Базовые правила валидации
      if (fieldConfig.required && !value) {
        error = fieldConfig.errorMessages?.required || 'Это поле обязательно для заполнения'
      } else if (fieldConfig.minLength && value.length < fieldConfig.minLength) {
        error = fieldConfig.errorMessages?.minLength ||
            `Минимальная длина: ${fieldConfig.minLength} символов`
      } else if (fieldConfig.maxLength && value.length > fieldConfig.maxLength) {
        error = fieldConfig.errorMessages?.maxLength ||
            `Максимальная длина: ${fieldConfig.maxLength} символов`
      } else if (fieldConfig.pattern && !fieldConfig.pattern.test(value)) {
        error = fieldConfig.errorMessages?.pattern || 'Неверный формат'
      }

      // Кастомные валидаторы
      if (this.customValidators[fieldName]) {
        const customError = this.customValidators[fieldName](value, this.formData)
        if (customError) error = customError
      }

      this.errors[fieldName] = error
      this.$emit('field-validated', { fieldName, isValid: !error, value })
    },

    async handleSubmit() {
      // Валидация всех полей
      this.fieldsConfig.forEach(field => this.validateField(field.name))

      if (this.hasErrors) {
        this.$emit('validation-failed', this.errors)
        return
      }

      this.isSubmitting = true
      this.$emit('submitting', this.formData)

      try {
        const response = await this.$http.post(this.submitUrl, this.formData)
        this.$emit('submitted', { data: this.formData, response })
        this.$emit('success', response.data)

        // Автоматический сброс если нужно
        if (this.autoReset) {
          this.resetForm()
        }
      } catch (error) {
        this.$emit('error', error)
        this.$emit('submit-failed', error)
      } finally {
        this.isSubmitting = false
      }
    },

    resetForm() {
      this.fieldsConfig.forEach(field => {
        this.formData[field.name] = field.defaultValue || ''
        this.errors[field.name] = ''
      })
      this.$emit('form-reset')
    },

    // Метод для внешнего сброса
    clearForm() {
      this.resetForm()
    },

    // Метод для установки значений извне
    setFormData(data) {
      Object.keys(data).forEach(key => {
        if (key in this.formData) {
          this.formData[key] = data[key]
        }
      })
    }
  },
  watch: {
    // Реакция на изменения конфигурации полей
    fieldsConfig: {
      handler(newConfig) {
        newConfig.forEach(field => {
          if (!(field.name in this.formData)) {
            this.$set(this.formData, field.name, field.defaultValue || '')
            this.$set(this.errors, field.name, '')
          }
        })
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.form-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.form-header {
  border-radius: 15px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  margin: 10px;
  border-bottom: 2px solid #00bfff;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.info_header {
  font-size: 20px;
  text-shadow: 0 0 5px #00bfff, 0 0 10px #00bfff;
  margin-right: 0;
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

input, textarea, select {
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

.checkbox-group {
  display: flex;
  align-items: center;
}

.checkbox-group input {
  width: auto;
  margin-right: 10px;
}

.checkbox-label {
  margin-bottom: 0;
  font-weight: normal;
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

.submit-btn:not(:disabled):hover {
  background: #0099cc;
}

@media (max-width: 888px) {
  .form-header {
    flex-direction: column;
  }

  .info_header {
    font-size: 16px;
    margin-bottom: 10px;
  }

  .form-container {
    margin: 10px;
    padding: 15px;
  }
}
</style>