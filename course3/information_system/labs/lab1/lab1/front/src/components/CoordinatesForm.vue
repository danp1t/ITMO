<template>
  <BaseForm
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
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
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
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
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
