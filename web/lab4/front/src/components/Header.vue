<script setup>
  const my_name = "Путинцев Данил Денисович";
  const my_group = "P3207";
  const my_variant = "430339"
</script>

<template>
  <header>
    <span class="info_header">ФИО: {{my_name}}</span>
    <span class="info_header">Учебная группа: {{my_group}}</span>
    <span class="info_header">Вариант: {{my_variant}}</span>
  </header>
</template>

<style scoped>
header {
  border-radius: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin: 10px;
  border-bottom: 2px solid #00bfff;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.info_header {
  font-size: 20px;
  text-shadow: 0 0 5px #00bfff, 0 0 10px #00bfff;
}
</style>