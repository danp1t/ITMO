<template>
  <div v-if="readonly" class="readonly-view">
    <h4>Текущие данные координат</h4>
    <table class="data-table">
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
    </table>
  </div>
  <BaseForm
    v-else
    :title="isEditMode ? 'Редактирование координат' : 'Создание координат'"
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
  name: 'CoordinatesForm',
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
          label: 'Координата x',
          type: 'number',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            required: 'Ввод координаты X обязательный',
            pattern: 'Координата X - это вещественное число'
          }
        },
        {
          name: 'y',
          label: 'Координата y',
          type: 'number',
          required: false,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            pattern: 'Координата Y - это вещественное число'
          }
        },
      ],
      customValidators: {
        x: (value) => {
          if (value && value <= -59) {
            return 'Координата X должна быть больше -59'
          }
          return null
        },
        y: (value) => {
          if (value && value <= -5) {
            return 'Координата Y должна быть больше -5'
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
      return this.isEditMode ? `/api/update/coordinates/${this.entity.id}` : '/api/create/coordinates'
    },
    initialData() {
      return this.entity ? { x: this.entity.x, y: this.entity.y } : null
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
