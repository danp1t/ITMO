<template>
  <BaseForm
      title="Координаты"
      :fields-config="fieldsConfig"
      :custom-validators="customValidators"
      submit-button-text=""
      submit-url=""
      no-button
  >
  </BaseForm>
</template>

<script>
import BaseForm from './BaseForm.vue'

export default {
  name: 'CoordinatesForm',
  components: { BaseForm },
  data() {
    return {
      fieldsConfig: [
        {
          name: 'x',
          label: 'Координата X',
          type: 'text',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            required: 'Координата X обязательна',
            pattern: 'Координата X должна быть вещественным числом'
          }
        },
        {
          name: 'y',
          label: 'Координата Y',
          type: 'text',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/,
          errorMessages: {
            required: 'Координата Y обязательна',
            pattern: 'Координата Y должна быть вещественным числом'
          }
        }
      ],
      customValidators: {
        x: (value) => {
          if (value && value <= -59) {
            return 'Координата X должна быть больше -59'
          }
        },
        y: (value) => {
          if (value && value <= -5) {
            return 'Координата X должна быть больше -5'
          }
          return null
        }
      }
    }
  }
}
</script>