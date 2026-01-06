<template>
  <div class="forgot-password-view">
    <div class="columns is-centered">
      <div class="column is-half-tablet is-one-third-desktop">
        <div class="card">
          <div class="card-header">
            <h2 class="card-header-title">Восстановление пароля</h2>
          </div>

          <div class="card-content">
            <div v-if="!emailSent">
              <p class="mb-4">Введите ваш email, и мы отправим вам инструкции по восстановлению пароля.</p>

              <form @submit.prevent="handleSubmit">
                <div class="field">
                  <label class="label">Email</label>
                  <div class="control has-icons-left">
                    <input
                      v-model="email"
                      type="email"
                      class="input"
                      :class="{ 'is-danger': error }"
                      placeholder="Введите ваш email"
                      required
                    >
                    <span class="icon is-small is-left">
                      <i class="fas fa-envelope"></i>
                    </span>
                  </div>
                  <p v-if="error" class="help is-danger">{{ error }}</p>
                </div>

                <div class="field">
                  <div class="control">
                    <button
                      type="submit"
                      class="button is-primary is-fullwidth"
                      :class="{ 'is-loading': isLoading }"
                      :disabled="isLoading"
                    >
                      Отправить инструкции
                    </button>
                  </div>
                </div>
              </form>
            </div>

            <div v-else class="has-text-centered">
              <div class="notification is-success is-light">
                <p>Инструкции по восстановлению пароля отправлены на {{ email }}.</p>
                <p class="mt-2">Проверьте ваш email и следуйте инструкциям в письме.</p>
              </div>

              <div class="buttons is-centered mt-4">
                <router-link to="/login" class="button is-light">
                  Вернуться к входу
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
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()

const email = ref('')
const error = ref('')
const isLoading = ref(false)
const emailSent = ref(false)

const handleSubmit = async () => {
  if (!email.value) {
    error.value = 'Email обязателен'
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    const result = await authStore.forgotPassword(email.value)

    if (result.success) {
      emailSent.value = true
    } else {
      error.value = result.error || 'Ошибка отправки email'
    }
  } catch (err: any) {
    error.value = 'Произошла ошибка при отправке запроса'
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.forgot-password-view {
  padding: 4rem 1rem;
  max-width: 800px;
  margin: 0 auto;
  align-items: center;
}


.card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  background-color: #d1cece;
  padding: 1rem 1.5rem;
}

.card-header-title {
  color: #333;
}
</style>
