
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
  flex-direction: column;
  gap: 20px;
}

.Authorization, .Registration {
  width: 100%;
}

/* Tablet styles */
@media (min-width: 889px) and (max-width: 1136px) {
  .container {
    flex-direction: column;
  }
  .Authorization {
    flex: 1;
  }
  .Registration {
    flex: 2;
  }
}

/* Desktop styles */
@media (min-width: 1137px) {
  .container {
    flex-direction: row;
    gap: 40px;
  }
  .Authorization {
    flex: 1;
    max-width: 300px;
  }
  .Registration {
    flex: 2;
    max-width: 600px;
  }
}
</style>
