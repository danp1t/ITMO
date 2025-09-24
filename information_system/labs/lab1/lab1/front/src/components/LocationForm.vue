<template>
  <BaseForm
      :fields-config="fieldsConfig"
      :nested="nested"
      :no-button="true"
      @update:formData="$emit('update:formData', $event)"
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
    }
  },
  data() {
    return {
      fieldsConfig: [
        {
          name: 'x',
          label: 'X',
          type: 'text',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
          errorMessages: {
            required: 'Ввод X обязательный',
            pattern: 'X - это вещественное число'
          }
        },
        {
          name: 'y',
          label: 'Y',
          type: 'text',
          required: true,
          pattern: /^(0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
          errorMessages: {
            required: 'Ввод Y обязательный',
            pattern: 'Y - это вещественное число'
          }
        },
        {
          name: 'z',
          label: 'Z',
          type: 'text',
          required: false,
          pattern: /^(|0$|-?[1-9]\d*(\.\d*[0-9]$)?|-?0\.\d*[0-9])$/ ,
          errorMessages: {
            pattern: 'Z - это вещественное число'
          }
        },
        {
          name: 'name',
          label: 'Name',
          type: 'text',
          required: true,
          errorMessages: {
            required: 'Ввод Name обязательный',
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