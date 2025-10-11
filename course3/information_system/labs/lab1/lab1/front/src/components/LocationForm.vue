<template>
  <div v-if="readonly" class="readonly-view">
    <h4>Текущие данные локации</h4>
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
          <td>{{ entity.y }}</td>
        </tr>
        <tr>
          <th>Координата Z</th>
          <td>{{ entity.z || 'Не указано' }}</td>
        </tr>
        <tr>
          <th>Название</th>
          <td>{{ entity.name }}</td>
        </tr>
      </tbody>
    </table>
  </div>
  <BaseForm
    v-else
    :title="isEditMode ? 'Редактирование локации' : 'Создание локации'"
    :fields-config="fieldsConfig"
    :submit-button-text="isEditMode ? 'Обновить' : 'Создать'"
    :submit-url="submitUrl"
    :nested="nested"
    :custom-validators="customValidators"
    :initial-data="initialData"
    :method="isEditMode ? 'put' : 'post'"
    @submitted="onSubmitted"
  >
  </BaseForm>
</template>

<script>
import BaseForm from './BaseForm.vue'

export default {
  name: 'LocationForm',
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
          name: 'x',
          label: 'Координата X',
          type: 'number',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            required: 'Ввод координаты X обязателен',
            pattern: 'Координата X должна быть вещественным числом'
          }
        },
        {
          name: 'y',
          label: 'Координата Y',
          type: 'number',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            required: 'Ввод координаты Y обязателен',
            pattern: 'Координата Y должна быть вещественным числом'
          }
        },
        {
          name: 'z',
          label: 'Координата Z',
          type: 'number',
          required: false,
          pattern: /^(|0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            pattern: 'Координата Z должна быть вещественным числом'
          }
        },
        {
          name: 'name',
          label: 'Название локации',
          type: 'text',
          required: true,
          maxLength: 255,
          errorMessages: {
            required: 'Ввод названия локации обязателен',
            maxLength: 'Длина названия не должна превышать 255 символов'
          }
        },
      ],
      customValidators: {
        name: (value) => {
          if (value && value.trim().length === 0) {
            return 'Название локации не может быть пустым'
          }
          if (value && value.length > 255) {
            return 'Длина названия не должна превышать 255 символов'
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
      return this.isEditMode ? `/api/update/location/${this.entity.id}` : '/api/create/location'
    },
    initialData() {
      return this.entity ? {
        x: this.entity.x,
        y: this.entity.y,
        z: this.entity.z,
        name: this.entity.name
      } : null
    }
  },
  methods: {
    onSubmitted({ response }) {
      this.$emit('submitted', {response})
      if (!this.nested) {
        this.$router.push('/success')
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
</style>
