<template>
  <div class="login-view">
    <div class="columns is-centered">
      <div class="column is-half">
        <div class="container">
          <div class="has-text-centered mb-6">
            <h1 class="title is-2">Добро пожаловать!</h1>
            <p class="subtitle">Войдите в свой аккаунт</p>
          </div>

          <LoginForm />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import LoginForm from '../components/auth/LoginForm.vue'
</script>

<style scoped>

</style>
