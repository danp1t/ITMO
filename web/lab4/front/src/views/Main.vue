<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Header from "@/components/Header.vue";
import InputValues from "@/components/InputValues.vue";
import Table from "@/components/Table.vue";
import Canvas from "@/components/Canvas.vue";

const router = useRouter();

// Функция выхода
const logout = () => {
    localStorage.removeItem('token')
    router.push('/');
};
</script>

<template>
  <Header />
  <div class="container">
    <InputValues />
    <Canvas />
    <Table />
  </div>
  <button @click="logout">Выйти</button>
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
</style>
