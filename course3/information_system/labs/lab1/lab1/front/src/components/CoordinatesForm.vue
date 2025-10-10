<template>
  <BaseForm
      title="Координаты"
      :fields-config="fieldsConfig"
      submit-button-text="Создать"
      submit-url="/api/create/coordinates"
      :nested="nested"
      :custom-validators="customValidators"
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
            return 'Координата X должна быть больше -5'
          }
          return null
        }
      }
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
