<template>
  <BaseForm
      title="Организация"
      :fields-config="fieldsConfig"
      :custom-validators="customValidators"
      submit-button-text="Отправить"
      submit-url="/api/register"
      @submitted="onSubmitted"
  >
  </BaseForm>
</template>

<script>
import BaseForm from './BaseForm.vue'

export default {
  name: 'OrganizationForm',
  components: { BaseForm },
  data() {
    return {
      fieldsConfig: [
        {
          name: 'name',
          label: 'Имя организации',
          type: 'text',
          required: true,
          errorMessages: {
            required: 'Ввод имени организации обязательный',
          }
        },
        {
          name: 'annualTurnover',
          label: 'Годовой оборот',
          type: 'number',
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
          errorMessages: {
            pattern: 'Годовой оборот - это вещественное число'
          }
        },
        {
          name: 'employeesCount',
          label: 'Количество работников',
          type: 'number',
          pattern: /\d+/ ,
          errorMessages: {
            pattern: 'Количество работников - это целое число'
          }
        },
        {
          name: 'rating',
          label: 'Рейтинг',
          type: 'number',
          pattern: /\d+/,
          errorMessages: {
            pattern: 'Рейтинг - это целое число'
          }
        },
      ],
      customValidators: {
        name: (value) => {
          if (value && value.length <= 0) {
            return 'Имя организации не может быть пустой строкой'
          }
          return null
        },
        annualTurnover: (value) => {
          if (value && value <= 0) {
            return 'Годовой оборот должен быть больше 0'
          }
          return null
        },
        employeesCount: (value) => {
          if (value && value <= 0) {
            return 'Количество работников должно быть больше 0'
          }
          return null
        },
        rating: (value) => {
          if (value && value <= 0) {
            return 'Рейтинг должен быть больше 0'
          }
          return null
        },
      }
    }
  },
  methods: {
    onSubmitted({ response }) {
      this.$router.push('/success')
    }
  }
}
</script>
