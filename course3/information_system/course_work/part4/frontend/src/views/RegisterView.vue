<template>
  <div class="register-view">
    <div class="columns is-centered">
      <div class="column is-half">
        <div class="container">
          <div class="has-text-centered mb-6">
            <h1 class="title is-2">Создание аккаунта</h1>
            <p class="subtitle">Присоединяйтесь к нашему сообществу</p>
          </div>

          <RegisterForm />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import RegisterForm from '../components/auth/RegisterForm.vue'
</script>

<style scoped>

</style>
