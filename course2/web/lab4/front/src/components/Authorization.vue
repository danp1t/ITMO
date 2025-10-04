<template>
  <div class="home">
    <h2>Авторизация</h2>
    <form @submit.prevent="handleSubmit" class="auth-form">
      <div class="form-group">
        <label for="login">Логин:</label>
        <input type="text" id="login" v-model="login" required />
      </div>
      <div class="form-group">
        <label for="password">Пароль:</label>
        <input type="password" id="password" v-model="password" required />
      </div>
      <button type="submit" class="submit-button">Войти</button>
    </form>
    <div v-if="notificationVisible" :class="notificationType">
      {{ notificationMessage }}
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      login: '',
      password: '',
      notificationMessage: '',
      notificationType: '',
      notificationVisible: false,
    };
  },
  methods: {
    async handleSubmit() {
      try {
        const response = await axios.post('http://localhost:8080/backend-1.0-SNAPSHOT/api/auth/login', {
          login: this.login,
          password: this.password,
        });

        // Сохраните токен в локальное хранилище (если он возвращается)
        if (response.data.token) {
          localStorage.setItem('token', response.data.token);
        }

        // Уведомление об успешной регистрации
        this.notificationMessage = 'Вы успешно зарегистрированы!';
        this.notificationType = 'success';
        this.notificationVisible = true;

        // Перенаправление на главную страницу
        this.$router.push('/main');

        // Скрыть уведомление через 3 секунды
        setTimeout(() => {
          this.notificationVisible = false;
        }, 3000);
      } catch (error) {
        console.error('Ошибка:', error.response ? error.response.data : error.message);

        // Уведомление об ошибке
        this.notificationMessage = error.response && error.response.data && error.response.data.message
          ? error.response.data.message
          : 'Неверный логин или пароль';
        this.notificationType = 'error';
        this.notificationVisible = true;

        // Скрыть уведомление через 3 секунды
        setTimeout(() => {
          this.notificationVisible = false;
        }, 3000);
      }
    },
  },
};
</script>


<style scoped>
.success {
  color: green;
}
.error {
  color: red;
}

.home {
  max-width: 400px;
  width: 90%; /* Mobile default */
  margin: auto;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  background-color: #f9f9f9;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input {
  width: 100%; /* Full width on mobile */
  padding: 3px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

input:focus {
  border-color: #007bff;
  outline: none;
}

.submit-button {
  width: 100%;
  padding: 10px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.submit-button:hover {
  background-color: #0056b3;
}

/* Tablet Styles */
@media (min-width: 889px) and (max-width: 1136px) {
  .home {
    width: 50%; /* Tablet width */
  }
  input {
    width: 95%; /* Adjusted for tablet */
  }
}

/* Desktop Styles */
@media (min-width: 1137px) {
  .home {
    width: 25%; /* Desktop width */
  }
}
</style>
