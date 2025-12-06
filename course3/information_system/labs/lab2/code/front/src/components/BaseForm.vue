<template>
  <div :class="['form-container', { 'nested-form': nested }]">
    <header v-if="!nested && title" class="form-header">
      <h2 class="info_header">{{ title }}</h2>
    </header>

    <slot name="before-fields"></slot>

    <div class="form-fields">
      <div
          v-for="field in fieldsConfig"
          :key="field.name"
          class="form-group"
      >
        <label :for="field.name" class="form-label">
          {{ field.label }}{{ field.required ? '*' : '' }}
        </label>

        <input
            v-if="field.type === 'text' || field.type === 'email' || field.type === 'password' || field.type === 'number'"
            :id="field.name"
            v-model="formData[field.name]"
            :type="field.type"
            :placeholder="field.placeholder"
            :maxlength="field.maxLength"
            :class="{ 'error-input': errors[field.name] }"
            @blur="validateField(field.name)"
            class="form-input"
        >

        <textarea
            v-else-if="field.type === 'textarea'"
            :id="field.name"
            v-model="formData[field.name]"
            :placeholder="field.placeholder"
            :maxlength="field.maxLength"
            :rows="field.rows || 3"
            :class="{ 'error-input': errors[field.name] }"
            @blur="validateField(field.name)"
            class="form-textarea"
        ></textarea>

        <select
            v-else-if="field.type === 'select'"
            :id="field.name"
            v-model="formData[field.name]"
            :class="{ 'error-input': errors[field.name] }"
            @blur="validateField(field.name)"
            class="form-select"
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

        <span v-if="errors[field.name]" class="error-message">
          {{ errors[field.name] }}
        </span>
      </div>
    </div>

    <slot name="after-fields"></slot>

    <button
        v-if="!noButton"
        type="submit"
        :disabled="isSubmitting || hasErrors"
        class="submit-btn"
        @click.prevent="handleSubmit"
    >
      {{ isSubmitting ? 'Отправка...' : submitButtonText }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'BaseForm',
  props: {
    title: {
      type: String,
      default: ''
    },
    fieldsConfig: {
      type: Array,
      required: true
    },
    submitButtonText: {
      type: String,
      default: 'Отправить'
    },
    submitUrl: {
      type: String,
      default: ''
    },
    customValidators: {
      type: Object,
      default: () => ({})
    },
    noButton: {
      type: Boolean,
      default: false
    },
    nested: {
      type: Boolean,
      default: false
    }
  },
  data() {
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

      if (this.customValidators[fieldName]) {
        const customError = this.customValidators[fieldName](value, this.formData)
        if (customError) error = customError
      }

      this.errors[fieldName] = error
      this.$emit('field-validated', { fieldName, isValid: !error, value })
    },

    async handleSubmit() {
      this.fieldsConfig.forEach(field => this.validateField(field.name))

      if (this.hasErrors) {
        this.$emit('validation-failed', this.errors)
        return
      }

      if (!this.submitUrl) {
        this.$emit('submit', this.formData)
        return
      }

      this.isSubmitting = true
      this.$emit('submitting', this.formData)

      try {
        const response = await this.$axios.post(this.submitUrl, this.formData)
        this.$emit('submitted', { data: this.formData, response })
        if (!this.nested) {
          this.$emit('success', response.data)
        }
      } catch (error) {
        this.$emit('error', error)
      } finally {
        this.isSubmitting = false
      }
    },
  },
  watch: {
    formData: {
      handler(newValue) {
        this.$emit('update:formData', newValue)
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.form-container {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-container.nested-form {
  background: transparent;
  box-shadow: none;
  padding: 0;
}

.form-header {
  border-radius: 10px 10px 0 0;
  padding: 15px 20px;
  margin: 0;
  border-bottom: 1px solid #00bfff;
  background: linear-gradient(135deg, #00bfff 0%, #0099cc 100%);
}

.form-header .info_header {
  font-size: 18px;
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin: 0;
}

.form-fields {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.form-input,
.form-textarea,
.form-select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #00bfff;
  box-shadow: 0 0 0 2px rgba(0, 191, 255, 0.2);
}

.error-input {
  border-color: #ff4444 !important;
}

.error-message {
  color: #ff4444;
  font-size: 12px;
  display: block;
  margin-top: 4px;
}

.submit-btn {
  background: linear-gradient(135deg, #00bfff 0%, #0099cc 100%);
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  width: 100%;
  transition: all 0.3s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 191, 255, 0.3);
}

.submit-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.nested-form .form-fields {
  padding: 0;
}

.nested-form .form-group {
  margin-bottom: 12px;
}

.nested-form .form-label {
  font-size: 13px;
  margin-bottom: 3px;
}

.nested-form .form-input,
.nested-form .form-textarea,
.nested-form .form-select {
  padding: 6px 10px;
  font-size: 13px;
}

@media (max-width: 768px) {
  .form-fields {
    padding: 15px;
  }

  .form-header {
    padding: 12px 15px;
  }

  .form-header .info_header {
    font-size: 16px;
  }
}
</style>