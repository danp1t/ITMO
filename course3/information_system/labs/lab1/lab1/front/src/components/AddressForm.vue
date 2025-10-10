<template>
  <BaseForm
      title="Адрес"
      :fields-config="fieldsConfig"
      submit-button-text="Создать"
      submit-url="/api/create/address"
      :nested="nested"
      :custom-validators="customValidators"
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
    }
  },
  data() {
    return {
      fieldsConfig: [
        {
          name: 'street',
          label: 'Введите улицу',
          type: 'text',
          required: true,
          maxLength: 180,
          errorMessages: {
            required: 'Ввод улицы обязательный',
            maxLength: 'Длина строки не должна быть больше 181'
          }
        },
        {
          name: 'zipCode',
          label: 'Введите почтовый индекс',
          type: 'text',
          required: true,
          errorMessages: {
            required: 'Ввод почтового индекса обязательный',
          }
        },
      ],
      customValidators: {
        street: (value) => {
          if (value && value.trim().length === 0) {
            return 'Улица не может быть пустой строкой'
          }
          return null
        },
        zipCode: (value) => {
          if (value && value.trim().length === 0) {
            return 'Почтовый индекс не может быть пустым'
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
    },

    onError(error) {
      console.error('Ошибка создания адреса:', error)
      this.$emit('error', error)
    }
  }
}
</script>
