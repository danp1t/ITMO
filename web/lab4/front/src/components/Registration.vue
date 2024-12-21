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
  </div>
</template>

<script>
export default {
  data() {
    return {
      login: '',
      password: '',
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
          throw new Error('Ошибка при регистрации');
        }

        const data = await response.json();
        console.log('Успех:', data);
        // Логика перехода на основную страницу
        this.$router.push('/main');
      } catch (error) {
        console.error('Ошибка:', error);
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
  padding: 10px;
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
</style>
