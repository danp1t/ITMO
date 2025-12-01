<template>
  <div id="app">
    <nav class="navbar is-primary" role="navigation" aria-label="main navigation">
      <div class="navbar-brand">
        <router-link to="/" class="navbar-item">
          <strong>Художественная Гимнастика</strong>
        </router-link>
      </div>

      <div class="navbar-menu">
        <div class="navbar-start">
          <router-link to="/" class="navbar-item">Главная</router-link>
          <router-link to="/posts" class="navbar-item">Лента постов</router-link>
          <router-link to="/tournaments" class="navbar-item">Соревнования</router-link>
          <router-link
            v-if="authStore.isAuthenticated"
            to="/shop"
            class="navbar-item"
          >
            Магазин
          </router-link>
        </div>

        <div class="navbar-end">
          <div class="navbar-item">
            <div class="buttons">
              <template v-if="!authStore.isAuthenticated">
                <router-link to="/register" class="button is-light">
                  Регистрация
                </router-link>
                <router-link to="/login" class="button is-primary">
                  Вход
                </router-link>
              </template>
              <template v-else>
                <div class="dropdown is-hoverable">
                  <div class="dropdown-trigger">
                    <button class="button is-light" aria-haspopup="true" aria-controls="user-menu">
                      <span class="icon">
                        <i class="fas fa-user"></i>
                      </span>
                      <span>{{ authStore.user?.name }}</span>
                      <span class="icon is-small">
                        <i class="fas fa-angle-down" aria-hidden="true"></i>
                      </span>
                    </button>
                  </div>
                  <div class="dropdown-menu" id="user-menu" role="menu">
                    <div class="dropdown-content">
                      <router-link to="/profile" class="dropdown-item">
                        Профиль
                      </router-link>
                      <hr class="dropdown-divider">
                      <a class="dropdown-item" @click="logout">
                        Выйти
                      </a>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
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
/* Стили для выпадающего меню */
.dropdown-item {
  cursor: pointer;
}

.container {
  padding: 20px;
  flex: 1;
}
</style>
