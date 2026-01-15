<script setup>
import { ref } from 'vue';

const my_name = "Путинцев Данил Денисович";
const my_group = "P3307";
const my_variant = "123237";

const props = defineProps({
  isAuthenticated: {
    type: Boolean,
    default: false
  },
  user: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['login', 'register', 'logout']);
</script>

<template>
  <header>
    <div class="student-info">
      <span class="info_header">ФИО: {{my_name}}</span>
      <span class="info_header">Учебная группа: {{my_group}}</span>
      <span class="info_header">Вариант: {{my_variant}}</span>
    </div>

    <div class="auth-section">
      <div v-if="isAuthenticated" class="authenticated-user">
        <span class="user-name">{{ user?.email || 'Пользователь' }}</span>
        <button @click="$emit('logout')" class="logout-btn">
          Выйти
        </button>
      </div>
      <div v-else class="auth-buttons">
        <button @click="$emit('login')" class="login-btn">
          Войти
        </button>
        <button @click="$emit('register')" class="register-btn">
          Регистрация
        </button>
      </div>
    </div>
  </header>
</template>

<style scoped>
header {
  border-radius: 15px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  margin: 10px;
  border-bottom: 2px solid #00bfff;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.student-info {
  display: flex;
  flex: 1;
  justify-content: space-between;
  align-items: center;
}

.info_header {
  font-size: 20px;
  text-shadow: 0 0 5px #00bfff, 0 0 10px #00bfff;
  margin-right: 20px;
  flex: 1;
  text-align: center;
}

.auth-section {
  display: flex;
  align-items: center;
  margin-left: 20px;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

.login-btn, .register-btn, .logout-btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.login-btn {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.login-btn:hover {
  background: #667eea;
  color: white;
}

.register-btn {
  background: #667eea;
  color: white;
  border: 2px solid #667eea;
}

.register-btn:hover {
  background: #5a67d8;
  border-color: #5a67d8;
}

.authenticated-user {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  color: #333;
  font-weight: 500;
  font-size: 16px;
  text-shadow: 0 0 3px #00bfff;
  white-space: nowrap;
}

.logout-btn {
  background: #fed7d7;
  color: #c53030;
  border: 1px solid #fc8181;
}

.logout-btn:hover {
  background: #feb2b2;
}

@media (max-width: 1080px) {
  header {
    flex-direction: column;
    gap: 15px;
  }

  .student-info {
    width: 100%;
    flex-direction: column;
    gap: 10px;
  }

  .info_header {
    margin-right: 0;
    margin-bottom: 10px;
    font-size: 18px;
    text-align: center;
    width: 100%;
  }

  .auth-section {
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }
}

@media (min-width: 889px) and (max-width: 1136px) {
  .info_header {
    font-size: 18px;
  }

  .student-info {
    flex-wrap: wrap;
  }
}

@media (min-width: 1137px) {
  .info_header {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .auth-buttons {
    flex-direction: column;
    width: 100%;
  }

  .login-btn, .register-btn, .logout-btn {
    width: 100%;
    padding: 10px;
  }

  .authenticated-user {
    flex-direction: column;
    width: 100%;
    gap: 10px;
  }

  .user-name {
    text-align: center;
  }
}
</style>