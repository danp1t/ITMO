
<script setup>
import Header from "@/components/Header.vue";
import Authorization from "@/components/Authorization.vue";
import Registration from "@/components/Registration.vue";
</script>

<template>
  <body>
  <Header />
  <div class="container">
    <Authorization />
    <Registration />
  </div>
  </body>
</template>

<style scoped>
body {
  background-color: rgb(232, 232, 232);
  font-family: 'Arial', sans-serif;
  color: #333;
  margin: 0;
  padding: 20px;
}

.container {
  display: flex;
  align-items: flex-start;
  gap: 40px;
}

Authorization {
  flex: 1;
}

Registration {
  flex: 2;
}
</style>