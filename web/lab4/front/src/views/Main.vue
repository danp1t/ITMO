<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Header from "@/components/Header.vue";
import InputValues from "@/components/InputValues.vue";
import Table from "@/components/Table.vue";
import Canvas from "@/components/Canvas.vue";

const router = useRouter();

const canvasData = ref({ x: 0, y: 0, r: 1 });

const updateCanvasData = (data) => {
  canvasData.value = data;
};

const notificationMessage = ref('');
const notificationType = ref('');
const notificationVisible = ref(false);

const handlePointClick = async (point) => {
  try {
    const response = await fetch('http://localhost:8080/backend-1.0-SNAPSHOT/api/point', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(point),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Ошибка отправки точки.');
    }

    const data = await response.json();
    console.log('Успех:', data);

    // Show success notification
    notificationMessage.value = 'Точка успешно отправлена!';
    notificationType.value = 'success';
    notificationVisible.value = true;

    setTimeout(() => {
      notificationVisible.value = false;
    }, 3000);

  } catch (error) {
    console.error('Ошибка:', error);

    // Show error notification
    notificationMessage.value = error.message || 'Ошибка при отправке точки. Попробуйте еще раз.';
    notificationType.value = 'error';
    notificationVisible.value = true;

    setTimeout(() => {
      notificationVisible.value = false;
    }, 3000);
  }
};

// Функция выхода
const logout = () => {
  localStorage.removeItem('token');
  router.push('/');
};
</script>

<template>
  <Header />
  <div class="container">
    <InputValues @update-canvas="updateCanvasData" />
    <Canvas :data="canvasData" @point-clicked="handlePointClick" />
  </div>
  <div class="button-container">
    <button @click="logout">Выйти</button>
  </div>

  <div v-if="notificationVisible" class="notification" :class="notificationType">
    {{ notificationMessage }}
  </div>
</template>

<style scoped>
.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 10px;
  border-radius: 5px;
  font-size: 14px;
}

.notification.success {
  background-color: #4caf50;
  color: white;
}

.notification.error {
  background-color: #f44336;
  color: white;
}

.container {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

InputValues {
  flex: 1;
}

Table {
  flex: 2;
}

.button-container {
  display: grid;
  place-items: center; /* Центрирует по горизонтали и вертикали */
}
button {
  background-color: #00bfff;
  color: #ffffff;
  border: none;
  margin-left: 10px;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
 }

 button:hover {
  background-color: #0099cc; /* Темнее при наведении */
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.5); /* Неоновый эффект при наведении */
 }


</style>
