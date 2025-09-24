<script setup>
  const my_name = "Путинцев Данил Денисович";
  const my_group = "P3307";
  const my_variant = "1733723"
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
  flex-direction: row;
  align-items: center;
  justify-content: space-between; /* Distributes items evenly */
  padding: 20px;
  margin: 10px;
  border-bottom: 2px solid #00bfff;
  background-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.info_header {
  font-size: 20px;
  text-shadow: 0 0 5px #00bfff, 0 0 10px #00bfff;
  margin-right: 0; /* Removed margin for even distribution */
  flex: 1; /* Ensures items take equal space if needed */
}

/* Media queries for font sizes and layout */
@media (max-width: 888px) {
  header {
    flex-direction: column;
  }
  .info_header {
    font-size: 16px;
    margin-right: 0;
    margin-bottom: 10px;
  }
  .info_header:last-child {
    margin-bottom: 0;
  }
}

@media (min-width: 889px) and (max-width: 1136px) {
  .info_header {
    font-size: 18px;
    margin-right: 0; /* Adjusted margin for even distribution */
  }
}

@media (min-width: 1137px) {
  .info_header {
    font-size: 20px;
    margin-right: 0; /* Adjusted margin for even distribution */
  }
}
</style>
