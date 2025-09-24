<template>
  <BaseForm
      title="Координаты"
      :fields-config="fieldsConfig"
      submit-button-text="Отправить"
      submit-url="/api/register"
      @submitted="onSubmitted"
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
          label: 'Координата x',
          type: 'text',
          required: true,
          pattern: /^\d+(?:[\.]\d+)?$/,
          errorMessages: {
            required: 'Ввод координаты X обязательный',
            pattern: 'Координата X - это вещественное число'
          }
        },
        {
          name: 'y',
          label: 'Координата y',
          type: 'text',
          required: false,
          pattern: /^\d+(?:[\.]\d+)?$/,
          errorMessages: {
            pattern: 'Координата Y - это вещественное число'
          }
        },
      ],
    }
  },
  methods: {
    onSubmitted({ response }) {
      this.$router.push('/success')
    }
  }
}
</script>