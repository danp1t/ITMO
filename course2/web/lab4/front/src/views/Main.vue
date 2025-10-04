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
  <main class="main-wrapper">
    <div class="container">
      <InputValues @update-canvas="updateCanvasData" @point-sent="onPointSent" />
      <Table :results="pointsList" />
      <div class="canvas-wrapper" align="center">
        <Canvas :data="canvasData" :points="pointsList" @point-clicked="handlePointClick" />
      </div>
    </div>
    <div class="button-container">
      <button @click="logout">Выйти</button>
    </div>
    <Notification
      v-if="notificationVisible"
      :message="notificationMessage"
      :type="notificationType"
      @close="notificationVisible = false"
    />
  </main>
</template>

<style scoped>
/* Main wrapper styles */
.main-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}


.button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

button {
  background-color: #00bfff;
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
}

button:hover {
  background-color: #0099cc;
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.5);
}

/* Container styles */
.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 90%;
  margin: auto;
}

/* Button container styles */
.button-container {
  display: flex;
  justify-content: center;
  margin: 20px auto;
}

/* Notification styles */
.notification {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
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

/* Mobile styles */
@media (max-width: 888px) {
  .container {
    flex-direction: column;
  }
  .InputValues, .Table, .Canvas {
    width: 100%;
  }
  /* Additional mobile-specific styles */
}

/* Tablet styles */
@media (min-width: 889px) and (max-width: 1136px) {
  .container {
    flex-direction: column;
  }

  .InputValues, .Table, .Canvas {
    width: 100%;
  }

  button {
    font-size: 18px;
  }

  .canvas-wrapper {
  position: relative;
  width: 100%;
  padding-top: 100%; /* Maintains 1:1 aspect ratio */
}

.Canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
}
}



/* Desktop styles */
@media (min-width: 1137px) {
  .container {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  .InputValues {
    flex: 1;
    max-width: 300px;
  }
  .Table {
    flex: 2;
    max-width: 600px;
  }
  .Canvas {
    flex: 1;
    max-width: 300px;
  }

}
</style>
