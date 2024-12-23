<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Header from "@/components/Header.vue";
import InputValues from "@/components/InputValues.vue";
import Table from "@/components/Table.vue";
import Canvas from "@/components/Canvas.vue";
import Notification from "@/components/Notification.vue"; // Import the Notification component

const router = useRouter();
const canvasData = ref({ x: 0, y: 0, r: 1 });
const pointsList = ref([]);

const notificationVisible = ref(false);
const notificationMessage = ref('');
const notificationType = ref('success');

const updateCanvasData = (data) => {

  canvasData.value = data;
};

const onPointSent = async (point) => {
  try {
    const response = await fetch('http://localhost:8080/backend-1.0-SNAPSHOT/api/point', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(point),
    });

    if (!response.ok) {
      throw new Error('Ошибка отправки точки.');
    }

    const data = await response.json();
    pointsList.value.push({ ...point, hit: data.hit });

    // Show success notification
    showNotification('Точка успешно отправлена!', 'success');

  } catch (error) {
    // Show error notification
    showNotification(error.message || 'Ошибка при отправке точки. Попробуйте еще раз.', 'error');
  }
};

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
    pointsList.value.push({ ...point, hit: data.hit });

    // Show success notification
    showNotification('Точка успешно отправлена!', 'success');

  } catch (error) {
    console.error('Ошибка:', error);

    // Show error notification
    showNotification(error.message || 'Ошибка при отправке точки. Попробуйте еще раз.', 'error');
  }
};

// Function to handle logout
const logout = () => {
  localStorage.removeItem('token');
  router.push('/');
};

// Function to show notifications
const showNotification = (message, type) => {
  notificationMessage.value = message;
  notificationType.value = type;
  notificationVisible.value = true;

  setTimeout(() => {
    notificationVisible.value = false;
  }, 3000);
};
</script>

<template>
  <Header />
  <div class="container">
    <InputValues @update-canvas="updateCanvasData" @point-sent="onPointSent" />
    <Table :results="pointsList" />
    <Canvas :data="canvasData" :points="pointsList" @point-clicked="handlePointClick" />
  </div>
  <div class="button-container">
    <button @click="logout">Выйти</button>
  </div>

  <!-- Include the Notification component -->
  <Notification
    v-if="notificationVisible"
    :message="notificationMessage"
    :type="notificationType"
    @close="notificationVisible = false"
  />
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
