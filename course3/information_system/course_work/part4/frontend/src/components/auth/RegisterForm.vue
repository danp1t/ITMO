<template>
  <div class="register-form">
    <div class="card">
      <div class="card-header">
        <h2 class="card-header-title">Регистрация</h2>
      </div>

      <div class="card-content">
        <form @submit.prevent="handleSubmit">
          <!-- Имя -->
          <div class="field">
            <label class="label">Имя</label>
            <div class="control has-icons-left">
              <input
                v-model="form.name"
                type="text"
                class="input"
                :class="{ 'is-danger': errors.name }"
                placeholder="Введите ваше имя"
                required
              >
              <span class="icon is-small is-left">
                <i class="fas fa-user"></i>
              </span>
            </div>
            <p v-if="errors.name" class="help is-danger">{{ errors.name }}</p>
          </div>

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
                placeholder="Введите пароль"
                required
              >
              <span class="icon is-small is-left">
                <i class="fas fa-lock"></i>
              </span>
            </div>
            <p v-if="errors.password" class="help is-danger">{{ errors.password }}</p>
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
                placeholder="Повторите пароль"
                required
              >
              <span class="icon is-small is-left">
                <i class="fas fa-lock"></i>
              </span>
            </div>
            <p v-if="errors.confirmPassword" class="help is-danger">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Ошибка сервера -->
          <div v-if="serverError" class="notification is-danger is-light">
            {{ serverError }}
          </div>

          <!-- Успешная регистрация -->
          <div v-if="successMessage" class="notification is-success is-light">
            {{ successMessage }}
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
                Зарегистрироваться
              </button>
            </div>
          </div>

          <!-- Ссылка на вход -->
          <div class="field has-text-centered">
            <router-link to="/login" class="is-size-7">
              Уже есть аккаунт? Войти
            </router-link>
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
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const errors = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const serverError = ref('')
const successMessage = ref('')
const isLoading = ref(false)

const validateForm = () => {
  let isValid = true

  // Сброс ошибок
  errors.name = ''
  errors.email = ''
  errors.password = ''
  errors.confirmPassword = ''

  // Валидация имени
  if (!form.name.trim()) {
    errors.name = 'Имя обязательно'
    isValid = false
  } else if (form.name.length < 2) {
    errors.name = 'Имя должно содержать минимум 2 символа'
    isValid = false
  }

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

  // Подтверждение пароля
  if (!form.confirmPassword) {
    errors.confirmPassword = 'Подтверждение пароля обязательно'
    isValid = false
  } else if (form.password !== form.confirmPassword) {
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
  serverError.value = ''
  successMessage.value = ''

  try {
    const result = await authStore.register(form.name, form.email, form.password)

    if (result.success) {
      successMessage.value = 'Регистрация успешна! Проверьте ваш email для подтверждения.'

      // Очищаем форму
      form.name = ''
      form.email = ''
      form.password = ''
      form.confirmPassword = ''

      // Автоматический вход после регистрации
      setTimeout(async () => {
        const loginResult = await authStore.login(form.email, form.password)
        if (loginResult.success) {
          router.push('/')
        }
      }, 2000)
    } else {
      serverError.value = result.error || 'Ошибка регистрации'
    }
  } catch (error: any) {
    serverError.value = error.response?.data?.message || 'Произошла ошибка при регистрации'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.register-form {
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
