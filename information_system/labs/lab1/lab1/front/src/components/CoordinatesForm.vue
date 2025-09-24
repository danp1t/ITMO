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
          errorMessages: {
            required: 'Ввод координаты X обязательный',
          }
        },
        {
          name: 'y',
          label: 'Координата y',
          type: 'text',
          required: false,
          errorMessages: {
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