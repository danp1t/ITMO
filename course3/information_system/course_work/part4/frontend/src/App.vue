<template>
  <div id="app">
    <nav class="navbar">
      <div class="navbar-brand">
        <router-link to="/" class="navbar-item">
          Художественная Гимнастика
        </router-link>
      </div>

      <div class="navbar-menu">
        <div class="navbar-start">
          <router-link to="/posts" class="navbar-item">
            Лента постов
          </router-link>
          <router-link to="/tournaments" class="navbar-item">
            Соревнования
          </router-link>
          <router-link
            v-if="authStore.isAuthenticated"
            to="/shop"
            class="navbar-item"
          >
            Магазин
          </router-link>
        </div>

        <div class="navbar-end">
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/login" class="navbar-item">
              Вход
            </router-link>
            <router-link to="/register" class="navbar-item">
              Регистрация
            </router-link>
          </template>
          <template v-else>
            <span class="navbar-item">
              {{ authStore.user?.name }}
            </span>
            <button class="navbar-item button is-light" @click="logout">
              Выйти
            </button>
          </template>
        </div>
      </div>
    </nav>

    <main class="container">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from './stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const logout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style>
@import 'https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css';

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  flex: 1;
  padding: 20px;
}

.navbar {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>
