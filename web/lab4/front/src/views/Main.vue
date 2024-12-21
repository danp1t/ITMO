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

//const handlePointClick = (point) => {
  // Logic to send point to server and update color
//};

// Функция выхода
const logout = () => {
    localStorage.removeItem('token')
    router.push('/');
};
</script>

<template>
  <Header />
  <div class="container">
    <InputValues @update-canvas="updateCanvasData" />
    <Canvas :data="canvasData" @point-clicked="handlePointClick" />
    <Table />
  </div>
  <div class="button-container">
    <button @click="logout">Выйти</button>
  </div>
</template>


<style scoped>
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
