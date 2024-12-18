<script setup>

</script>

<template>
  <table>
    <thead>
    <tr>
      <th>Координаты X</th>
      <th>Координата Y</th>
      <th>Радиус (R)</th>
      <th>Результат</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(result, index) in results" :key="index">
      <td>{{ result.coordinates }}</td>
      <td>{{ result.radius }}</td>
      <td>{{ result.hit ? 'Попал' : 'Не попал' }}</td>
    </tr>
    </tbody>
  </table>
</template>

<style scoped>
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