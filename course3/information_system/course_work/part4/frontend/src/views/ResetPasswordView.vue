<template>
  <div class="reset-password-view">
    <div class="columns is-centered">
      <div class="column is-half-tablet is-one-third-desktop">
        <div class="card">
          <div class="card-header">
            <h2 class="card-header-title">Сброс пароля</h2>
          </div>

          <div class="card-content">
            <div v-if="!passwordReset">
              <form @submit.prevent="handleSubmit">
                <div class="field">
                  <label class="label">Новый пароль</label>
                  <div class="control has-icons-left">
                    <input
                      v-model="form.newPassword"
                      type="password"
                      class="input"
                      :class="{ 'is-danger': errors.newPassword }"
                      placeholder="Введите новый пароль"
                      maxlength="100"
                      required
                    >
                    <span class="icon is-small is-left">
                      <i class="fas fa-lock"></i>
                    </span>
                  </div>
                  <p v-if="errors.newPassword" class="help is-danger">{{ errors.newPassword }}</p>
                  <p class="help">Максимум 100 символов</p>
                </div>

                <div class="field">
                  <label class="label">Подтверждение пароля</label>
                  <div class="control has-icons-left">
                    <input
                      v-model="form.confirmPassword"
                      type="password"
                      class="input"
                      :class="{ 'is-danger': errors.confirmPassword }"
                      placeholder="Повторите новый пароль"
                      maxlength="100"
                      required
                    >
                    <span class="icon is-small is-left">
                      <i class="fas fa-lock"></i>
                    </span>
                  </div>
                  <p v-if="errors.confirmPassword" class="help is-danger">{{ errors.confirmPassword }}</p>
                  <p class="help">Максимум 100 символов</p>
                </div>

                <div v-if="error" class="notification is-danger is-light mb-4">
                  {{ error }}
                </div>

                <div class="field">
                  <div class="control">
                    <button
                      type="submit"
                      class="button is-primary is-fullwidth"
                      :class="{ 'is-loading': isLoading }"
                      :disabled="isLoading"
                    >
                      Сбросить пароль
                    </button>
                  </div>
                </div>
              </form>
            </div>

            <div v-else class="has-text-centered">
              <div class="notification is-success is-light">
                <p>Пароль успешно изменен!</p>
              </div>

              <div class="buttons is-centered mt-4">
                <router-link to="/login" class="button is-primary">
                  Войти с новым паролем
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  newPassword: '',
  confirmPassword: '',
})

const errors = reactive({
  newPassword: '',
  confirmPassword: '',
})

const error = ref('')
const isLoading = ref(false)
const passwordReset = ref(false)
const token = ref('')

onMounted(() => {
  token.value = route.query.token as string
  if (!token.value) {
    error.value = 'Неверная или отсутствующая ссылка для сброса пароля'
  }
})

const validateForm = () => {
  let isValid = true

  errors.newPassword = ''
  errors.confirmPassword = ''

  if (!form.newPassword) {
    errors.newPassword = 'Пароль обязателен'
    isValid = false
  } else if (form.newPassword.length < 6) {
    errors.newPassword = 'Пароль должен содержать минимум 6 символов'
    isValid = false
  } else if (form.newPassword.length > 100) {
    errors.newPassword = 'Пароль не должен превышать 100 символов'
    isValid = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = 'Подтверждение пароля обязательно'
    isValid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = 'Пароли не совпадают'
    isValid = false
  } else if (form.confirmPassword.length > 100) {
    errors.confirmPassword = 'Подтверждение пароля не должно превышать 100 символов'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  const newPassword = form.newPassword.substring(0, 100)
  const confirmPassword = form.confirmPassword.substring(0, 100)

  if (newPassword !== confirmPassword) {
    errors.confirmPassword = 'Пароли не совпадают'
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    const result = await authStore.resetPassword(token.value, newPassword)

    if (result.success) {
      passwordReset.value = true
      form.newPassword = ''
      form.confirmPassword = ''
    } else {
      error.value = result.error || 'Ошибка сброса пароля'
    }
  } catch (err: any) {
    if (err.response?.data) {
      error.value = err.response.data
    } else if (err.message) {
      error.value = err.message
    } else {
      error.value = 'Произошла ошибка при сбросе пароля'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.reset-password-view {
  padding: 4rem 1rem;
  max-width: 800px;
  margin: 0 auto;
  align-items: center;
}

.card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #f5f5f5;
  padding: 1rem 1.5rem;
}

.card-header-title {
  color: #333;
}
</style>
