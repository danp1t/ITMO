<script setup>
import { ref, onMounted, onUnmounted, defineProps, defineEmits, watch } from 'vue';

const props = defineProps(['data']);
const emit = defineEmits(['point-clicked']);

const canvas = ref(null);
const ctx = ref(null);

function drawAxes(R) {
  ctx.value.clearRect(0, 0, canvas.value.width, canvas.value.height);
  const halfR = R / 2;
  ctx.value.beginPath();
  ctx.value.moveTo(200, 0);
  ctx.value.lineTo(200, 400); // Ось Y
  ctx.value.moveTo(0, 200);
  ctx.value.lineTo(400, 200); // Ось X
  ctx.value.stroke();
  ctx.value.fillStyle = 'black';
  ctx.value.fillText(-R, 200 - R * 60, 215);
  ctx.value.fillText(R, 200 + R * 60, 215);
  ctx.value.fillText(halfR, 200 + halfR * 60, 215);
  ctx.value.fillText(-halfR, 200 - halfR * 60, 215);
  ctx.value.fillText(-R, 205, 200 + R * 60);
  ctx.value.fillText(R, 205, 200 - R * 60);
  ctx.value.fillText(-halfR, 205, 200 + halfR * 60);
  ctx.value.fillText(halfR, 205, 200 - halfR * 60);
  ctx.value.fillText("y", 205, 8);
  ctx.value.fillText("x", 400, 215);
}

function drawShapes(R) {
  const scale = 60; // Масштаб для отображения
  // Первая четверть: часть окружности
  ctx.value.beginPath();
  ctx.value.arc(200, 200, R * scale, -(Math.PI * 0.5), 0); // Часть окружности сверху
  ctx.value.lineTo(200, 200);
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
  // Вторая четверть: прямоугольник
  ctx.value.beginPath();
  ctx.value.rect(200 - (R * scale) / 2, 200 - R * scale, (R * scale) / 2, R * scale);
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
  // Третья четверть: треугольник
  ctx.value.beginPath();
  ctx.value.moveTo(200 - R * scale, 200);
  ctx.value.lineTo(200, 200 + R * scale);
  ctx.value.lineTo(200, 200);
  ctx.value.closePath();
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
}

onMounted(() => {
  ctx.value = canvas.value.getContext('2d');
  drawAxes(props.data.r);
  drawShapes(props.data.r);
  canvas.value.addEventListener('click', handleCanvasClick);
});

onUnmounted(() => {
  canvas.value.removeEventListener('click', handleCanvasClick);
});

const handleCanvasClick = (event) => {
  const rect = canvas.value.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;
  const scale = 60;
  const actualX = (x - 200) / scale;
  const actualY = (200 - y) / scale;
  emit('point-clicked', { x: actualX, y: actualY });
};

watch(() => props.data, (newData) => {
  drawAxes(newData.r);
  drawShapes(newData.r);
}, { deep: true });
</script>

<template>
  <canvas ref="canvas" width="315" height="315"></canvas>
</template>

<style scoped>
canvas {
  margin: 10px;
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.5);
  background-color: rgba(250, 250, 250, 1); /* Светлый фон для канваса */
}
</style>
