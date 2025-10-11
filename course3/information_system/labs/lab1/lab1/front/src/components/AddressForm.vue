<template>
  <div v-if="readonly" class="readonly-view">
    <h4>Текущие данные адреса</h4>
    <table class="data-table">
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
    </table>
  </div>
  <BaseForm
    v-else
    :title="isEditMode ? 'Редактирование адреса' : 'Создание адреса'"
    :fields-config="fieldsConfig"
    :submit-button-text="isEditMode ? 'Обновить' : 'Создать'"
    :submit-url="submitUrl"
    :nested="nested"
    :custom-validators="customValidators"
    :initial-data="initialData"
    :method="isEditMode ? 'put' : 'post'"
    @submitted="onSubmitted"
    @error="onError"
  >
  </BaseForm>
</template>

<script>
import BaseForm from './BaseForm.vue'

export default {
  name: 'AddressForm',
  components: { BaseForm },
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
      fieldsConfig: [
        {
          name: 'street',
          label: 'Улица',
          type: 'text',
          required: true,
          maxLength: 180,
          errorMessages: {
            required: 'Ввод улицы обязателен',
            maxLength: 'Длина улицы не должна превышать 180 символов'
          }
        },
        {
          name: 'zipCode',
          label: 'Почтовый индекс',
          type: 'text',
          required: true,
          maxLength: 30,
          errorMessages: {
            required: 'Ввод почтового индекса обязателен',
            maxLength: 'Длина почтового индекса не должна превышать 30 символов'
          }
        },
      ],
      customValidators: {
        street: (value) => {
          if (value && value.trim().length === 0) {
            return 'Улица не может быть пустой строкой'
          }
          if (value && value.length > 180) {
            return 'Длина улицы не должна превышать 180 символов'
          }
          return null
        },
        zipCode: (value) => {
          if (value && value.trim().length === 0) {
            return 'Почтовый индекс не может быть пустым'
          }
          if (value && value.length > 30) {
            return 'Длина почтового индекса не должна превышать 30 символов'
          }
          return null
        }
      }
    }
  },
  computed: {
    isEditMode() {
      return !!this.entity
    },
    submitUrl() {
      return this.isEditMode ? `/api/update/address/${this.entity.id}` : '/api/create/address'
    },
    initialData() {
      return this.entity ? {
        street: this.entity.street,
        zipCode: this.entity.zipCode
      } : null
    }
  },
  methods: {
    onSubmitted({ response }) {
      this.$emit('submitted', {response})
      if (!this.nested) {
        this.$router.push('/success')
      }
    },
    onError(error) {
      console.error('Ошибка создания/обновления адреса:', error)
      this.$emit('error', error)
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
</style>
