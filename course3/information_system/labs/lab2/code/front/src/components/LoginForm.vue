<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-header">
        <h2>Вход в систему</h2>
        <p>Введите ваши учетные данные</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input
              type="username"
              id="username"
              v-model="form.username"
              required
              placeholder="Введите ваш username"
              :class="{ 'error': errors.username }"
          />
          <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
        </div>

        <div class="form-group">
          <label for="password">Пароль</label>
          <div class="password-input">
            <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="form.password"
                required
                placeholder="Введите пароль"
                :class="{ 'error': errors.password }"
            />
            <button type="button" @click="showPassword = !showPassword" class="toggle-password">
              <svg v-if="showPassword" width="20" height="20" viewBox="0 0 24 24" fill="#718096">
                <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46A11.804 11.804 0 0 0 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="#718096">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
              </svg>
            </button>
          </div>
          <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
        </div>

        <button type="submit" :disabled="loading" class="submit-btn">
          <span v-if="loading">Вход...</span>
          <span v-else>Войти</span>
        </button>

        <div class="divider">
          <span>Или</span>
        </div>

        <p class="switch-auth">
          Нет аккаунта?
          <a href="#" @click.prevent="$emit('switchToRegister')">Зарегистрироваться</a>
        </p>
      </form>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: '',
        rememberMe: false
      },
      errors: {},
      loading: false,
      showPassword: false,
      errorMessage: ''
    }
  },
  methods: {
    validateForm() {
      this.errors = {}

      if (!this.form.username) {
        this.errors.username = 'Имя пользователя обязательно'
      }

      if (!this.form.password) {
        this.errors.password = 'Пароль обязателен'
      } else if (this.form.password.length < 6) {
        this.errors.password = 'Пароль должен содержать минимум 6 символов'
      }

      return Object.keys(this.errors).length === 0
    },

    async handleLogin() {
      if (!this.validateForm()) return

      this.loading = true
      this.errorMessage = ''

      try {
        const response = await axios.post('/api/auth/login', {
          username: this.form.username,
          password: this.form.password
        });

        if (response.data && response.data.token) {
          localStorage.setItem('authToken', response.data.token);
          localStorage.setItem('user', JSON.stringify({
            id: response.data.id,
            username: response.data.username,
            role: response.data.role
          }));

          this.$emit('loginSuccess', {
            username: response.data.username,
            role: response.data.role
          });
        }

      } catch (error) {
        if (error.response && error.response.data && error.response.data.error) {
          this.errorMessage = error.response.data.error;
        } else {
          this.errorMessage = 'Ошибка авторизации. Проверьте имя пользователя и пароль.';
        }
        console.error('Login error:', error);
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, rgba(118, 75, 162, 0) 0%);
  padding: 20px;
}

.auth-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 400px;
  padding: 40px;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 24px;
}

.auth-header p {
  color: #666;
  font-size: 14px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group label {
  color: #4a5568;
  font-weight: 500;
  font-size: 14px;
}

.form-group input {
  padding: 12px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input.error {
  border-color: #fc8181;
}

.error-text {
  color: #fc8181;
  font-size: 12px;
  margin-top: 2px;
}

.password-input {
  position: relative;
}

.toggle-password {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  padding: 5px;
  cursor: pointer;
  color: #718096;
}

.toggle-password:hover {
  color: #4a5568;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #4a5568;
}

.remember-me input {
  margin: 0;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.forgot-password:hover {
  text-decoration: underline;
}

.submit-btn {
  padding: 14px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:hover:not(:disabled) {
  background: #5a67d8;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  text-align: center;
  color: #a0aec0;
  font-size: 14px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e2e8f0;
}

.divider span {
  padding: 0 15px;
}

.switch-auth {
  text-align: center;
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.switch-auth a {
  color: #667eea;
  font-weight: 600;
  text-decoration: none;
  margin-left: 5px;
}

.switch-auth a:hover {
  text-decoration: underline;
}

.error-message {
  margin-top: 20px;
  padding: 12px;
  background: #fed7d7;
  color: #c53030;
  border-radius: 6px;
  font-size: 14px;
  border-left: 4px solid #f56565;
}
</style>