<template>
  <div id="app">
    <nav class="navbar is-primary" role="navigation" aria-label="main navigation">
      <div class="container">
        <div class="navbar-brand">
          <router-link to="/" class="navbar-item">
            <strong class="is-size-4">Художественная Гимнастика</strong>
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
                <!-- Корзина -->
                <button
                  v-if="authStore.isAuthenticated"
                  class="button is-light"
                  @click="toggleCart"
                >
                  <span class="icon">
                    <i class="fas fa-shopping-cart"></i>
                  </span>
                  <span v-if="cartStore.totalItems > 0" class="ml-1">
                    {{ cartStore.totalItems }} ({{ cartStore.totalAmount }} ₽)
                  </span>
                </button>

                <!-- Авторизация -->
                <template v-if="!authStore.isAuthenticated">
                  <router-link to="/register" class="button is-light">
                    Регистрация
                  </router-link>
                  <router-link to="/login" class="button is-primary">
                    <strong>Вход</strong>
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
                        <router-link to="/shop/orders" class="dropdown-item">
                          Мои заказы
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
      </div>
    </nav>

    <!-- Основной контент -->
    <main class="container">
      <router-view />
    </main>

    <!-- Корзина -->
    <CartSidebar
      :is-visible="showCart"
      @close="showCart = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'
import CartSidebar from './components/shop/CartSidebar.vue'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

const showCart = ref(false)

const toggleCart = () => {
  showCart.value = !showCart.value
}

const logout = async () => {
  await authStore.logout()
  cartStore.clearCart()
  router.push('/login')
}
</script>
