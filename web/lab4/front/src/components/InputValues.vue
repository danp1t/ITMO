<script setup>
import { ref } from 'vue';

const coorX = ref('');
const coorY = ref('');
const coorR = ref('');
const errors = ref({ x: '', y: '', r: '' });
const emit = defineEmits(['update-canvas']);

// Add results ref to store point history
const results = ref([]);

// Notification variables
const notificationMessage = ref('');
const notificationType = ref('success');
const notificationVisible = ref(false);

const validateCoordinates = (axis) => {
  const value = axis === 'x' ? parseFloat(coorX.value) : parseFloat(coorY.value);

  if (axis === 'x') {
    if (isNaN(value) || value < -5 || value > 3) {
      errors.value.x = 'Координата X должна быть в диапазоне от -5 до 3.';
    } else {
      errors.value.x = '';
    }
  } else if (axis === 'y') {
    if (isNaN(value) || value < -3 || value > 3) {
      errors.value.y = 'Координата Y должна быть в диапазоне от -3 до 3.';
    } else {
      errors.value.y = '';
    }
  }
};

const validateRadius = () => {
  const value = parseFloat(coorR.value);

  if (isNaN(value) || value < 0 || value > 3) {
    errors.value.r = 'Радиус R должен быть в диапазоне от 0 до 3.';
  } else {
    errors.value.r = '';
  }
};

const updateCanvas = () => {
  if (!errors.value.x && !errors.value.y && !errors.value.r) {
    emit('update-canvas', {
      x: parseFloat(coorX.value),
      y: parseFloat(coorY.value),
      r: parseFloat(coorR.value)
    });
  }
};

const sendCoordinates = async () => {
  validateCoordinates('x');
  validateCoordinates('y');
  validateRadius();

  if (!errors.value.x && !errors.value.y && !errors.value.r) {
    const point = {
      x: parseFloat(coorX.value),
      y: parseFloat(coorY.value),
      r: parseFloat(coorR.value)
    };

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
      if (typeof data.hit === 'boolean') {
        // Store the point and hit result in the results array
        results.value.push({
          x: point.x,
          y: point.y,
          r: point.r,
          hit: data.hit
        });

        // Show success notification
        notificationMessage.value = 'Точка успешно отправлена!';
        notificationType.value = 'success';
        notificationVisible.value = true;

        setTimeout(() => {
          notificationVisible.value = false;
        }, 3000);
      } else {
        throw new Error('Ответ сервера не содержит информацию о попадании.');
      }
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
  }
};
</script>

<template>
  <div id="form">
    <!-- Form inputs and buttons remain the same -->
    <label for="coor_x">Координата X (-5 ... 3):</label>
    <input type="text" v-model="coorX" @input="validateCoordinates('x')" />
    <span v-if="errors.x" class="error">{{ errors.x }}</span>

    <label for="coor_y">Координата Y (-3 ... 3):</label>
    <input type="text" v-model="coorY" @input="validateCoordinates('y')" />
    <span v-if="errors.y" class="error">{{ errors.y }}</span>

    <label for="coor_r">Радиус R (0 ... 3):</label>
    <input type="text" v-model="coorR" @input="validateRadius" />
    <span v-if="errors.r" class="error">{{ errors.r }}</span>

    <button @click="updateCanvas">Обновить область</button>
    <button @click="sendCoordinates">Отправить на сервер</button>
  </div>

  <table>
    <thead>
      <tr>
        <th>Координата X</th>
        <th>Координата Y</th>
        <th>Радиус R</th>
        <th>Результат</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="(result, index) in results" :key="index">
        <td>{{ result.x }}</td>
        <td>{{ result.y }}</td>
        <td>{{ result.r }}</td>
        <td>{{ result.hit ? 'Попал' : 'Не попал' }}</td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.error {
  color: red;
}

 #form {
  width: 35%;
  margin: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
 }

 label {
  font-size: 18px;
  margin-bottom: 10px;
  display: block;
   text-shadow: 0 0 5px #6b4e5a, 0 0 10px #00bfff;
 }

 input[type="text"], select {
  width: calc(100% - 20px);
  padding: 10px;
  margin-bottom: 15px;
  border: none;
  border-radius: 5px;
  background-color: #e6f7ff; /* Светлый фон для полей ввода */
  color: #333;
  font-size: 16px;
  box-shadow: inset 0 0 5px #00bfff;
  transition: border-color 0.3s, box-shadow 0.3s;
 }

 input[type="text"]:focus, select:focus {
  outline: none;
  box-shadow: inset 0 0 5px #00bfff, 0 0 5px #00bfff; /* Неоновый эффект при фокусе */
 }

 input[type="checkbox"] {
  margin-right: 10px;
 }

 button {
  background-color: #00bfff;
  color: #ffffff;
  border: none;
  margin: 10px;
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

 .error {
  margin-top: 10px;
 }

 table {
  width: 100%;
  margin-top: 20px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border-collapse: collapse;
}

th, td {
  padding: 12px;
  text-align: center;
  border-bottom: 1px solid #e0e0e0;
}

th {
  background-color: #00bfff;
  color: white;
  font-size: 18px;
  text-shadow: 0 0 5px #6b4e5a, 0 0 10px #00bfff;
}

td {
  background-color: rgba(230, 247, 255, 0.9);
  color: #333;
}

tr:hover {
  background-color: rgba(173, 216, 230, 0.5);
}
 </style>
