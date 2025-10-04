<!-- InputValues.vue -->
<template>
  <div id="form">
    <!-- Form inputs -->
    <label for="coor_x">Координата X (-5 ... 3):</label>
    <input type="text" v-model="coorX" @input="validateCoordinates('x')" />
    <span v-if="errors.x" class="error">{{ errors.x }}</span>

    <label for="coor_y">Координата Y (-3 ... 3):</label>
    <input type="text" v-model="coorY" @input="validateCoordinates('y')" />
    <span v-if="errors.y" class="error">{{ errors.y }}</span>

    <label for="coor_r">Радиус R (0 ... 3):</label>
    <input type="text" v-model="coorR" @input="validateRadius" />
    <span v-if="errors.r" class="error">{{ errors.r }}</span>

    <!-- Buttons -->
    <button @click="updateCanvas">Обновить область</button>
    <button @click="sendPoint">Отправить на сервер</button>
  </div>
</template>

<!-- InputValues.vue -->
<script setup>
import { ref, defineEmits } from 'vue';

const emit = defineEmits(['update-canvas', 'point-sent']);

const coorX = ref('');
const coorY = ref('');
const coorR = ref('');
const errors = ref({ x: '', y: '', r: '' });

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


const sendPoint = () => {
  validateCoordinates('x');
  validateCoordinates('y');
  validateRadius();

  if (!errors.value.x && !errors.value.y && !errors.value.r) {
    const point = {
      x: parseFloat(coorX.value),
      y: parseFloat(coorY.value),
      r: parseFloat(coorR.value)
    };
    emit('point-sent', point);
    // Reset inputs
    coorX.value = '';
    coorY.value = '';
    coorR.value = '';
    errors.value = { x: '', y: '', r: '' };
  }
};
</script>


<style scoped>
/* General Styles */
.main-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 90%;
}

/* Form Styles */
#form {
  max-width: 100%;
  margin: auto;
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
  background-color: #e6f7ff;
  color: #333;
  font-size: 16px;
  box-shadow: inset 0 0 5px #00bfff;
  transition: border-color 0.3s, box-shadow 0.3s;
}

input[type="text"]:focus, select:focus {
  outline: none;
  box-shadow: inset 0 0 5px #00bfff, 0 0 5px #00bfff;
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
  background-color: #0099cc;
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.5);
}

.error {
  color: red;
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

/* Media Queries */

/* Tablet Styles */
@media (min-width: 889px) and (max-width: 1136px) {
  .container {
    flex-direction: row;
    flex-wrap: wrap;
  }
  .InputValues, .Table, .Canvas {
    width: 50%;
  }
  #form {
    max-width: 80%;
  }
  label {
    font-size: 16px;
  }
  input[type="text"], select {
    font-size: 14px;
  }
}

/* Desktop Styles */
@media (min-width: 1137px) {
  .container {
    flex-direction: row;
    flex-wrap: nowrap;
  }
  .InputValues {
    width: 30%;
  }
  .Table {
    width: 60%;
  }
  .Canvas {
    width: 20%;
  }
  #form {
    max-width: 35%;
  }
  label {
    font-size: 18px;
  }
  input[type="text"], select {
    font-size: 16px;
  }
}
</style>
