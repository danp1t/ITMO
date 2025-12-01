<template>
  <div class="login-form">
    <div class="card">
      <div class="card-header">
        <h2 class="card-header-title">Вход в систему</h2>
      </div>

      <div class="card-content">
        <form @submit.prevent="handleSubmit">
          <!-- Email -->
          <div class="field">
            <label class="label">Email</label>
            <div class="control has-icons-left">
              <input
                v-model="form.email"
                type="email"
                class="input"
                :class="{ 'is-danger': errors.email }"
                placeholder="Введите ваш email"
                required
              >
              <span class="icon is-small is-left">
                <i class="fas fa-envelope"></i>
              </span>
            </div>
            <p v-if="errors.email" class="help is-danger">{{ errors.email }}</p>
          </div>

          <!-- Пароль -->
          <div class="field">
            <label class="label">Пароль</label>
            <div class="control has-icons-left">
              <input
                v-model="form.password"
                type="password"
                class="input"
                :class="{ 'is-danger': errors.password }"
                placeholder="Введите ваш пароль"
                required
              >
              <span class="icon is-small is-left">
                <i class="fas fa-lock"></i>
              </span>
            </div>
            <p v-if="errors.password" class="help is-danger">{{ errors.password }}</p>
          </div>

          <!-- Ошибка сервера -->
          <div v-if="serverError" class="notification is-danger is-light">
            {{ serverError }}
          </div>

          <!-- Кнопки -->
          <div class="field">
            <div class="control">
              <button
                type="submit"
                class="button is-primary is-fullwidth"
                :class="{ 'is-loading': isLoading }"
                :disabled="isLoading"
              >
                Войти
              </button>
            </div>
          </div>

          <!-- Ссылки -->
          <div class="field">
            <div class="is-flex is-justify-content-space-between">
              <router-link to="/register" class="is-size-7">
                Нет аккаунта? Зарегистрироваться
              </router-link>
              <router-link to="/forgot-password" class="is-size-7">
                Забыли пароль?
              </router-link>
            </div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  password: '',
})

const errors = reactive({
  email: '',
  password: '',
})

const serverError = ref('')
const isLoading = ref(false)

const validateForm = () => {
  let isValid = true

  // Сброс ошибок
  errors.email = ''
  errors.password = ''

  // Валидация email
  if (!form.email) {
    errors.email = 'Email обязателен'
    isValid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.email = 'Некорректный формат email'
    isValid = false
  }

  // Валидация пароля
  if (!form.password) {
    errors.password = 'Пароль обязателен'
    isValid = false
  } else if (form.password.length < 6) {
    errors.password = 'Пароль должен содержать минимум 6 символов'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  serverError.value = ''

  try {
    const result = await authStore.login(form.email, form.password)

    if (result.success) {
      // Перенаправляем на главную страницу
      router.push('/')
    } else {
      serverError.value = result.error || 'Ошибка входа'
    }
  } catch (error: any) {
    serverError.value = error.response?.data?.message || 'Произошла ошибка при входе'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.login-form {
  max-width: 400px;
  margin: 0 auto;
}

.card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #f5f5f5;
  padding: 1rem 1.5rem;
}

.card-header-title {
  font-size: 1.5rem;
  color: #333;
}
</style>
