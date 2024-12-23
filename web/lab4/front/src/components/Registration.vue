<template>
  <div class="home">
    <h2>Регистрация</h2>
    <form @submit.prevent="handleSubmit" class="auth-form">
      <div class="form-group">
        <label for="login">Логин:</label>
        <input type="text" id="new_login" v-model="login" required />
      </div>
      <div class="form-group">
        <label for="password">Пароль:</label>
        <input type="password" id="new_password" v-model="password" required />
      </div>
      <button type="submit" class="submit-button">Зарегистрироваться</button>
    </form>

    <!-- Всплывающее уведомление -->
    <Notification v-if="notificationVisible" :message="notificationMessage" :type="notificationType" />
  </div>
</template>

<script>
import Notification from './Notification.vue';

export default {
  components: {
    Notification,
  },
  data() {
    return {
      login: '',
      password: '',
      notificationVisible: false,
      notificationMessage: '',
      notificationType: 'success', // 'success' или 'error'
    };
  },
  methods: {
    async handleSubmit() {
  try {
      const response = await fetch('http://localhost:8080/backend-1.0-SNAPSHOT/api/register', {
          method: 'POST',
          headers: {
              'Content-Type': 'application/json',
          },
          body: JSON.stringify({
              login: this.login,
              password: this.password,
          }),
      });

      if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || 'Логин уже существует. Пожалуйста, выберите другой.');
      }

      const data = await response.json();
      console.log('Успех:', data);

      // Уведомление об успешной регистрации
      this.notificationMessage = 'Вы успешно зарегистрированы!';
      this.notificationType = 'success';
      this.notificationVisible = true;

      // Скрыть уведомление через 3 секунды
      setTimeout(() => {
          this.notificationVisible = false;
      }, 3000);

  } catch (error) {
      console.error('Ошибка:', error);

      // Уведомление об ошибке
      this.notificationMessage = error.message || 'Ошибка при регистрации. Попробуйте еще раз.';
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
.home {
  max-width: 400px;
  width: 25%;
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
  width: 95%;
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
    width: 50%;
  }
  input {
    width: 95%; /* Adjust input width if necessary */
  }
}

/* Mobile Styles */
@media (max-width: 888px) {
  .home {
    width: 90%;
  }
  input {
    width: 100%; /* Ensure inputs take full width on mobile */
  }
}
</style>
