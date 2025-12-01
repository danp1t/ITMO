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
                <!-- Новый пароль -->
                <div class="field">
                  <label class="label">Новый пароль</label>
                  <div class="control has-icons-left">
                    <input
                      v-model="form.newPassword"
                      type="password"
                      class="input"
                      :class="{ 'is-danger': errors.newPassword }"
                      placeholder="Введите новый пароль"
                      required
                    >
                    <span class="icon is-small is-left">
                      <i class="fas fa-lock"></i>
                    </span>
                  </div>
                  <p v-if="errors.newPassword" class="help is-danger">{{ errors.newPassword }}</p>
                </div>

                <!-- Подтверждение пароля -->
                <div class="field">
                  <label class="label">Подтверждение пароля</label>
                  <div class="control has-icons-left">
                    <input
                      v-model="form.confirmPassword"
                      type="password"
                      class="input"
                      :class="{ 'is-danger': errors.confirmPassword }"
                      placeholder="Повторите новый пароль"
                      required
                    >
                    <span class="icon is-small is-left">
                      <i class="fas fa-lock"></i>
                    </span>
                  </div>
                  <p v-if="errors.confirmPassword" class="help is-danger">{{ errors.confirmPassword }}</p>
                </div>

                <!-- Ошибка -->
                <div v-if="error" class="notification is-danger is-light mb-4">
                  {{ error }}
                </div>

                <!-- Кнопка -->
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
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
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
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = 'Подтверждение пароля обязательно'
    isValid = false
  } else if (form.newPassword !== form.confirmPassword) {
    errors.confirmPassword = 'Пароли не совпадают'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    const result = await authStore.resetPassword(token.value, form.newPassword)

    if (result.success) {
      passwordReset.value = true
      form.newPassword = ''
      form.confirmPassword = ''
    } else {
      error.value = result.error || 'Ошибка сброса пароля'
    }
  } catch (err: any) {
    error.value = 'Произошла ошибка при сбросе пароля'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.reset-password-view {
  padding: 4rem 1rem;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
}

.card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #f5f5f5;
  padding: 1rem 1.5rem;
}
</style>
