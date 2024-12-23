<script setup>
import { ref, onMounted, onUnmounted, defineProps, defineEmits, watch } from 'vue';
const props = defineProps(['data', 'points']);
const emit = defineEmits(['point-clicked']);

const canvas = ref(null);
const ctx = ref(null);

const scale = 45;

function drawAxes(R) {
  ctx.value.clearRect(0, 0, canvas.value.width, canvas.value.height);
  const halfWidth = canvas.value.width / 2;
  const halfHeight = canvas.value.height / 2;
  const width = canvas.value.width;
  const height = canvas.value.height;
  const halfR = R / 2;
  ctx.value.beginPath();
  ctx.value.moveTo(halfHeight, 0);
  ctx.value.lineTo(halfHeight, height); // Ось Y
  ctx.value.moveTo(0, halfWidth);
  ctx.value.lineTo(width, halfWidth); // Ось X
  ctx.value.stroke();
  ctx.value.fillStyle = 'black';
  ctx.value.fillText(-R, halfHeight - R * scale, halfHeight + 15);
  ctx.value.fillText(R, halfHeight + R * scale, halfHeight + 15);
  ctx.value.fillText(halfR, halfHeight + halfR * scale, halfHeight + 15);
  ctx.value.fillText(-halfR, halfHeight - halfR * scale, halfHeight + 15);
  ctx.value.fillText(-R, halfHeight + 5, halfHeight + R * scale);
  ctx.value.fillText(R, halfHeight + 5, halfHeight - R * scale);
  ctx.value.fillText(-halfR, halfHeight + 5, halfHeight + halfR * scale);
  ctx.value.fillText(halfR, halfHeight + 5, halfHeight - halfR * scale);
  ctx.value.fillText("y", halfHeight + 5, 8);
  ctx.value.fillText("x", width, halfHeight + 15);
}

function drawShapes(R) {
const halfWidth = canvas.value.width / 2;
  const halfHeight = canvas.value.height / 2;
  const width = canvas.value.width;
  const height = canvas.value.height;
  const halfR = R / 2;
   // Масштаб для отображения
  // Первая четверть: часть окружности
  ctx.value.beginPath();
  ctx.value.arc(halfHeight, halfHeight, R * scale / 2, 0, (Math.PI * 0.5)); // Часть окружности сверху
  ctx.value.lineTo(halfHeight, halfHeight);
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
  // Вторая четверть: прямоугольник
  ctx.value.beginPath();
  ctx.value.rect(halfHeight + (R * scale), halfHeight - R * scale, -(R * scale), R * scale);
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
  // Третья четверть: треугольник
  ctx.value.beginPath();
  ctx.value.moveTo(halfHeight - R * scale, halfHeight);
  ctx.value.lineTo(halfHeight, halfHeight - R * scale);
  ctx.value.lineTo(halfHeight, halfHeight);
  ctx.value.closePath();
  ctx.value.fillStyle = 'rgba(51, 153, 255, 0.5)';
  ctx.value.fill();
  ctx.value.stroke();
}

function isHit(x, y, r) {
    // First quadrant
    if (x >= 0 && y >= 0) {
        if (x <= r && y <= r) {
            return true;
        }
    }
    // Second quadrant
    else if (x <= 0 && y >= 0) {
        if (Math.abs(x) + Math.abs(y) <= r) {
            return true;
        }
    }
    // Third quadrant
    else if (x <= 0 && y <= 0) {
        return false;
    }
    // Fourth quadrant
    else if (x >= 0 && y <= 0) {
        if (x * x + y * y <= (r / 2) * (r / 2)) {
            return true;
        }
    }
    return false;
}

function drawPoints() {
  props.points.forEach(point => {
    const halfWidth = canvas.value.width / 2;
    const halfHeight = canvas.value.height / 2;
    const scaledX = halfWidth + point.x * scale;
    const scaledY = halfHeight - point.y * scale;
    ctx.value.beginPath();
    ctx.value.arc(scaledX, scaledY, 3, 0, 2 * Math.PI);

    let color = isHit(point.x, point.y, props.data.r);
    ctx.value.fillStyle = color ? 'green' : 'red';
    ctx.value.fill();



  });
}

function drawCanvas(R, points) {
  ctx.value.clearRect(0, 0, canvas.value.width, canvas.value.height);
  drawAxes(R);
  drawShapes(R);
  drawPoints(points);
}
onMounted(() => {
  ctx.value = canvas.value.getContext('2d');
  drawCanvas(props.data.r, props.points);
  canvas.value.addEventListener('click', handleCanvasClick);
});

onUnmounted(() => {
  canvas.value.removeEventListener('click', handleCanvasClick);
});

const handleCanvasClick = (event) => {
  const rect = canvas.value.getBoundingClientRect();
  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;
  const scale = 45;
  const halfCanvas = canvas.value.width / 2;
  const actualX = (x - 178) / scale;
  const actualY = (178 - y) / scale;
  console.log(actualX, actualY);
  emit('point-clicked', { x: actualX, y: actualY, r: props.data.r });
};

watch(
  () => ({ r: props.data.r, points: props.points }),
  (newData) => {
    drawCanvas(newData.r, newData.points);
  },
  { deep: true }
);
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
