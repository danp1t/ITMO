<template>
  <div class="verify-email-view">
    <div class="columns is-centered">
      <div :class="columnClass">
        <div class="card">
          <div class="card-header">
            <h2 class="card-header-title">Подтверждение Email</h2>
          </div>

          <div class="card-content">
            <div v-if="!verificationComplete">
              <div v-if="autoVerifying" class="has-text-centered">
                <div class="mb-4">
                  <i class="fas fa-spinner fa-spin fa-2x"></i>
                </div>
                <p>Подтверждаем ваш email...</p>
              </div>

              <div v-else>
                <p class="mb-4">
                  Введите код подтверждения, который был отправлен на ваш email.
                  Если вы не получили код, проверьте папку "Спам" или запросите новый.
                </p>

                <form @submit.prevent="handleSubmit">
                  <div class="field">
                    <label class="label">Email</label>
                    <div class="control has-icons-left">
                      <input
                        v-model="form.email"
                        type="email"
                        class="input"
                        :class="{ 'is-danger': errors.email }"
                        placeholder="Введите ваш email"
                        :disabled="!!routeEmail"
                        maxlength="100"
                        required
                      >
                      <span class="icon is-small is-left">
                        <i class="fas fa-envelope"></i>
                      </span>
                    </div>
                    <p v-if="errors.email" class="help is-danger">{{ errors.email }}</p>
                    <p class="help">Максимум 100 символов</p>
                  </div>

                  <div class="field">
                    <label class="label">Код подтверждения</label>
                    <div class="control">
                      <div class="field has-addons">
                        <div class="control is-expanded">
                          <input
                            v-model="form.code"
                            type="text"
                            class="input"
                            :class="{ 'is-danger': errors.code }"
                            placeholder="Введите 6-значный код"
                            maxlength="6"
                            pattern="[0-9]*"
                            inputmode="numeric"
                            required
                          >
                        </div>
                      </div>
                    </div>
                    <p v-if="errors.code" class="help is-danger">{{ errors.code }}</p>
                    <p class="help">Код состоит из 6 цифр</p>
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
                        Подтвердить Email
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>

            <div v-else class="has-text-centered">
              <div class="notification is-success is-light p-5">
                <div class="mb-4">
                  <i class="fas fa-check-circle fa-3x has-text-success"></i>
                </div>
                <h3 class="title is-4 mb-3">Email успешно подтвержден!</h3>
                <p class="subtitle is-5 mb-4">Ваш аккаунт активирован. Теперь вы можете войти в систему.</p>

                <div class="buttons is-centered is-flex is-justify-content-center">
                  <div class="buttons-group">
                    <router-link to="/login" class="button is-primary is-medium">
                      <span class="icon is-small">
                        <i class="fas fa-sign-in-alt"></i>
                      </span>
                      <span>Войти в систему</span>
                    </router-link>
                    <router-link to="/" class="button is-light is-medium ml-3">
                      <span class="icon is-small">
                        <i class="fas fa-home"></i>
                      </span>
                      <span>На главную</span>
                    </router-link>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!verificationComplete && !autoVerifying" class="has-text-centered mt-4">
              <router-link to="/login" class="is-size-7">
                Вернуться к входу
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const form = reactive({
  email: '',
  code: '',
})

const errors = reactive({
  email: '',
  code: '',
})

const error = ref('')
const isLoading = ref(false)
const autoVerifying = ref(false)
const verificationComplete = ref(false)
const resending = ref(false)
const canResend = ref(true)
const countdown = ref(60)

const routeEmail = computed(() => route.query.email as string)
const routeCode = computed(() => route.query.code as string)

const columnClass = computed(() => {
  if (verificationComplete.value) {
    return 'column is-10-tablet is-8-desktop is-6-widescreen'
  }
  return 'column is-half-tablet is-one-third-desktop'
})

onMounted(() => {
  if (routeEmail.value && routeCode.value) {
    // Ограничиваем длину email и кода из query параметров
    form.email = routeEmail.value.substring(0, 100)
    form.code = routeCode.value.substring(0, 6)
    autoVerifyEmail()
  }
})

let countdownInterval: number | null = null

const startCountdown = () => {
  canResend.value = false
  countdown.value = 60

  countdownInterval = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      canResend.value = true
      if (countdownInterval) {
        clearInterval(countdownInterval)
        countdownInterval = null
      }
    }
  }, 1000)
}

const validateForm = () => {
  let isValid = true

  errors.email = ''
  errors.code = ''

  // Ограничиваем длину email перед валидацией
  if (form.email.length > 100) {
    form.email = form.email.substring(0, 100)
  }

  if (!form.email) {
    errors.email = 'Email обязателен'
    isValid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.email = 'Некорректный формат email'
    isValid = false
  } else if (form.email.length > 100) {
    errors.email = 'Email не должен превышать 100 символов'
    isValid = false
  }

  // Ограничиваем длину кода перед валидацией
  if (form.code.length > 6) {
    form.code = form.code.substring(0, 6)
  }

  if (!form.code) {
    errors.code = 'Код подтверждения обязателен'
    isValid = false
  } else if (!/^\d{6}$/.test(form.code)) {
    errors.code = 'Код должен состоять из 6 цифр'
    isValid = false
  } else if (form.code.length !== 6) {
    errors.code = 'Код должен состоять из 6 цифр'
    isValid = false
  }

  return isValid
}

const autoVerifyEmail = async () => {
  autoVerifying.value = true
  error.value = ''

  try {
    // Ограничиваем длину данных перед отправкой
    const email = form.email.substring(0, 100)
    const code = form.code.substring(0, 6)

    const result = await authStore.verifyEmail(email, code)

    if (result.success) {
      verificationComplete.value = true
    } else {
      error.value = result.error || 'Ошибка подтверждения email'
    }
  } catch (err: any) {
    error.value = 'Произошла ошибка при подтверждении email'
  } finally {
    autoVerifying.value = false
  }
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    // Ограничиваем длину данных перед отправкой
    const email = form.email.substring(0, 100)
    const code = form.code.substring(0, 6)

    const result = await authStore.verifyEmail(email, code)

    if (result.success) {
      verificationComplete.value = true
    } else {
      error.value = result.error || 'Ошибка подтверждения email'
    }
  } catch (err: any) {
    if (err.response?.data) {
      error.value = err.response.data
    } else if (err.message) {
      error.value = err.message
    } else {
      error.value = 'Произошла ошибка при подтверждении email'
    }
  } finally {
    isLoading.value = false
  }
}

import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (countdownInterval) {
    clearInterval(countdownInterval)
  }
})
</script>

<style scoped>
.verify-email-view {
  padding: 4rem 1rem;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
}

.card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #201f1f;
  padding: 1rem 1.5rem;
}

.fa-spinner {
  color: #3273dc;
}

.notification {
  border: 1px solid #e6e6e6;
  border-radius: 8px;
}

.buttons-group {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 1rem;
}

@media screen and (max-width: 768px) {
  .buttons-group {
    flex-direction: column;
    align-items: center;
  }

  .buttons-group .button {
    width: 100%;
    max-width: 250px;
    margin-bottom: 0.5rem;
  }
}

.title {
  color: #333;
  font-weight: 600;
}

.subtitle {
  color: #666;
}

.fa-check-circle {
  color: #23d160;
}

.card-content {
  padding: 2rem;
}
</style>
