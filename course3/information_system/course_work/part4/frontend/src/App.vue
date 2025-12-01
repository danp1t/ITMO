<template>
  <div id="app">
    <nav class="navbar is-primary custom-navbar" role="navigation" aria-label="main navigation">
      <div class="container">
        <div class="navbar-brand">
          <router-link to="/" class="navbar-item brand-item">
            <div class="brand-content">
              <span class="brand-icon">💫</span>
              <div class="brand-text">
                <strong class="brand-title">Художественная Гимнастика</strong>
                <span class="brand-subtitle">Artistic Rhythmic</span>
              </div>
            </div>
          </router-link>
        </div>

        <div class="navbar-menu">
          <div class="navbar-start">
            <router-link to="/" class="navbar-item nav-item">
              <span class="nav-icon">🏠</span>
              <span>Главная</span>
            </router-link>
            <router-link to="/posts" class="navbar-item nav-item">
              <span class="nav-icon">📰</span>
              <span>Лента постов</span>
            </router-link>
            <router-link to="/tournaments" class="navbar-item nav-item">
              <span class="nav-icon">🏆</span>
              <span>Соревнования</span>
            </router-link>
            <router-link
              v-if="authStore.isAuthenticated"
              to="/shop"
              class="navbar-item nav-item"
            >
              <span class="nav-icon">🛍️</span>
              <span>Магазин</span>
            </router-link>
          </div>

          <div class="navbar-end">
            <div class="navbar-item">
              <div class="buttons">
                <!-- Корзина -->
                <button
                  v-if="authStore.isAuthenticated"
                  class="button cart-button"
                  @click="toggleCart"
                  :class="{ 'has-items': cartStore.totalItems > 0 }"
                >
                  <span class="icon">
                    <i class="fas fa-shopping-cart"></i>
                  </span>
                  <span v-if="cartStore.totalItems > 0" class="cart-info">
                    <span class="cart-count">{{ cartStore.totalItems }}</span>
                    <span class="cart-amount">{{ cartStore.totalAmount }} ₽</span>
                  </span>
                  <span v-else class="cart-label">Корзина</span>
                </button>

                <!-- Авторизация -->
                <template v-if="!authStore.isAuthenticated">
                  <router-link to="/register" class="button register-button">
                    Регистрация
                  </router-link>
                  <router-link to="/login" class="button login-button">
                    <strong>Вход</strong>
                  </router-link>
                </template>
                <template v-else>
                  <div class="dropdown is-hoverable custom-dropdown">
                    <div class="dropdown-trigger">
                      <button class="button user-button" aria-haspopup="true" aria-controls="user-menu">
                        <span class="user-avatar">
                          {{ authStore.user?.name?.charAt(0) || '👤' }}
                        </span>
                        <span class="user-name">{{ authStore.user?.name }}</span>
                        <span class="icon is-small">
                          <i class="fas fa-angle-down" aria-hidden="true"></i>
                        </span>
                      </button>
                    </div>
                    <div class="dropdown-menu" id="user-menu" role="menu">
                      <div class="dropdown-content custom-dropdown-content">
                        <router-link to="/profile" class="dropdown-item">
                          <i class="fas fa-user dropdown-icon"></i>
                          <span>Профиль</span>
                        </router-link>
                        <router-link to="/shop/orders" class="dropdown-item">
                          <i class="fas fa-box dropdown-icon"></i>
                          <span>Мои заказы</span>
                        </router-link>
                        <hr class="dropdown-divider">
                        <a class="dropdown-item logout-item" @click="logout">
                          <i class="fas fa-sign-out-alt dropdown-icon"></i>
                          <span>Выйти</span>
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
    <main class="container main-content">
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

<style scoped>
/* Общие стили для навбара */
.custom-navbar {
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 0.5rem 0;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

/* Бренд */
.brand-item {
  padding: 0.5rem 0;
}

.brand-content {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.brand-icon {
  font-size: 1.8rem;
  animation: sparkle 3s infinite;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: white;
  letter-spacing: 0.5px;
}

.brand-subtitle {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 300;
  letter-spacing: 1px;
}

/* Навигационные пункты */
.nav-item {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500;
  margin: 0 0.25rem;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.nav-item:hover {
  color: white !important;
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.nav-item.router-link-active {
  color: white !important;
  background: rgba(255, 255, 255, 0.15);
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 50%;
  height: 2px;
  background: white;
  border-radius: 1px;
}

.nav-icon {
  margin-right: 0.5rem;
  font-size: 0.9rem;
}

/* Кнопки */
.button {
  border: none;
  border-radius: 25px;
  font-weight: 500;
  transition: all 0.3s ease;
  height: auto;
  padding: 0.5rem 1.25rem;
}

/* Кнопка корзины */
.cart-button {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  overflow: visible;
}

.cart-button:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cart-button.has-items {
  background: rgba(255, 107, 107, 0.2);
  border-color: rgba(255, 107, 107, 0.3);
}

.cart-button.has-items::before {
  content: '';
  position: absolute;
  top: -3px;
  right: -3px;
  width: 10px;
  height: 10px;
  background: #ff6b6b;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.cart-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: 0.5rem;
}

.cart-count {
  background: #ff6b6b;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
}

.cart-amount {
  font-weight: 600;
}

.cart-label {
  margin-left: 0.5rem;
}

/* Кнопки регистрации и входа */
.register-button {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.register-button:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.login-button {
  background: white;
  color: #8e2de2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-button:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Кнопка пользователя */
.user-button {
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-button:hover {
  background: rgba(255, 255, 255, 0.25);
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Выпадающее меню */
.custom-dropdown-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: #f8f9fa;
  color: #333;
  transition: all 0.2s;
}

.dropdown-item:hover {
  background: #f8f9fa;
  color: #8e2de2;
}

.dropdown-icon {
  width: 16px;
  text-align: center;
  font-size: 0.875rem;
}

.dropdown-divider {
  margin: 0.5rem 0;
  background: #e9ecef;
}

.logout-item:hover {
  color: #ff4757;
}

/* Основной контент */
.main-content {
  padding: 2rem 0;
  min-height: calc(100vh - 70px);
}

/* Анимации */
@keyframes sparkle {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.7;
  }
}

/* Адаптивность */
@media (max-width: 1024px) {
  .brand-title {
    font-size: 1.1rem;
  }

  .nav-item {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }
}

@media (max-width: 768px) {
  .navbar-menu {
    background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }

  .brand-subtitle {
    display: none;
  }

  .cart-amount {
    display: none;
  }

  .user-name {
    max-width: 80px;
  }
}

@media (max-width: 480px) {
  .brand-title {
    font-size: 1rem;
  }

  .nav-icon {
    display: none;
  }

  .cart-label {
    display: none;
  }
}
</style>
