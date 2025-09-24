<template>
  <BaseForm
      :fields-config="fieldsConfig"
      :custom-validators="customValidators"
      :nested="nested"
      :no-button="true"
      @update:formData="$emit('update:formData', $event)"
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
          errorMessages: {
            required: 'Ввод улицы обязательный',
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
          if (value && value.length >= 181) {
            return 'Длина строки не должна быть больше 181'
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