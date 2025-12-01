<template>
  <div id="app">
    <!-- Обновленная шапка -->
    <header class="header">
      <div class="header-container">
        <!-- Логотип и название -->
        <div class="header-brand">
          <router-link to="/" class="logo-link">
            <div class="logo-icon">
              <svg width="28" height="32" viewBox="0 0 28 32" fill="none">
                <path d="M14 2L26 10V22L14 30L2 22V10L14 2Z" stroke="currentColor" stroke-width="2"/>
                <path d="M14 8L20 12V20L14 24L8 20V12L14 8Z" fill="currentColor"/>
                <circle cx="14" cy="16" r="3" fill="white"/>
              </svg>
            </div>
            <div class="logo-text">
              <span class="logo-title">Гимнастика</span>
              <span class="logo-subtitle">Artistic</span>
            </div>
          </router-link>
        </div>

        <!-- Основная навигация -->
        <nav class="header-nav">
          <router-link to="/" class="nav-link">
            <span class="nav-icon">🏠</span>
            <span class="nav-text">Главная</span>
          </router-link>
          <router-link to="/posts" class="nav-link">
            <span class="nav-icon">📰</span>
            <span class="nav-text">Лента</span>
          </router-link>
          <router-link to="/tournaments" class="nav-link">
            <span class="nav-icon">🏆</span>
            <span class="nav-text">Соревнования</span>
          </router-link>
          <router-link
            v-if="authStore.isAuthenticated"
            to="/shop"
            class="nav-link"
          >
            <span class="nav-icon">🛍️</span>
            <span class="nav-text">Магазин</span>
          </router-link>
        </nav>

        <!-- Действия пользователя -->
        <div class="header-actions">
          <!-- Корзина -->
          <button
            v-if="authStore.isAuthenticated"
            class="action-btn cart-btn"
            @click="toggleCart"
            :class="{ 'has-items': cartStore.totalItems > 0 }"
          >
            <span class="cart-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M6 2L3 6V20C3 20.5304 3.21071 21.0391 3.58579 21.4142C3.96086 21.7893 4.46957 22 5 22H19C19.5304 22 20.0391 21.7893 20.4142 21.4142C20.7893 21.0391 21 20.5304 21 20V6L18 2H6Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M3 6H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 10C16 11.0609 15.5786 12.0783 14.8284 12.8284C14.0783 13.5786 13.0609 14 12 14C10.9391 14 9.92172 13.5786 9.17157 12.8284C8.42143 12.0783 8 11.0609 8 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <span v-if="cartStore.totalItems > 0" class="cart-badge">
              {{ cartStore.totalItems }}
            </span>
            <span v-if="cartStore.totalItems > 0" class="cart-total">
              {{ cartStore.totalAmount }} ₽
            </span>
          </button>

          <!-- Авторизация -->
          <template v-if="!authStore.isAuthenticated">
            <router-link to="/register" class="action-btn register-btn">
              Регистрация
            </router-link>
            <router-link to="/login" class="action-btn login-btn">
              Войти
            </router-link>
          </template>

          <!-- Профиль пользователя -->
          <template v-else>
            <div class="user-dropdown">
              <button class="user-btn">
                <div class="user-avatar">
                  {{ authStore.user?.name?.charAt(0) || '👤' }}
                </div>
                <span class="user-name">{{ authStore.user?.name }}</span>
                <span class="dropdown-arrow">▼</span>
              </button>
              <div class="dropdown-menu">
                <router-link to="/profile" class="dropdown-item">
                  <span class="item-icon">👤</span>
                  <span>Профиль</span>
                </router-link>
                <router-link to="/shop/orders" class="dropdown-item">
                  <span class="item-icon">📦</span>
                  <span>Мои заказы</span>
                </router-link>
                <div class="dropdown-divider"></div>
                <button class="dropdown-item logout-item" @click="logout">
                  <span class="item-icon">🚪</span>
                  <span>Выйти</span>
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </header>

    <!-- Основной контент -->
    <main class="main-content">
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
/* Основные стили шапки */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

/* Логотип */
.header-brand .logo-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: white;
  transition: opacity 0.3s;
}

.logo-link:hover {
  opacity: 0.9;
}

.logo-icon {
  margin-right: 12px;
  color: white;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.logo-subtitle {
  font-size: 12px;
  font-weight: 300;
  opacity: 0.9;
  letter-spacing: 1px;
}

/* Навигация */
.header-nav {
  display: flex;
  gap: 32px;
  margin-left: 48px;
}

.nav-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: rgba(255, 255, 255, 0.9);
  padding: 8px 0;
  position: relative;
  transition: color 0.3s;
}

.nav-link:hover {
  color: white;
}

.nav-link.router-link-active {
  color: white;
}

.nav-link.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: white;
  border-radius: 1px;
}

.nav-icon {
  font-size: 16px;
  margin-right: 8px;
}

.nav-text {
  font-size: 15px;
  font-weight: 500;
}

/* Действия пользователя */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  padding: 8px 20px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.register-btn {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.register-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
}

.login-btn {
  background: white;
  color: #667eea;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Кнопка корзины */
.cart-btn {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cart-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.cart-btn.has-items {
  background: rgba(255, 255, 255, 0.15);
}

.cart-icon {
  display: flex;
  align-items: center;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ff4757;
  color: white;
  font-size: 11px;
  font-weight: 600;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cart-total {
  margin-left: 8px;
  font-size: 13px;
  font-weight: 600;
}

/* Профиль пользователя */
.user-dropdown {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.user-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  font-size: 10px;
  opacity: 0.7;
}

/* Выпадающее меню */
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  opacity: 0;
  visibility: hidden;
  transform: translateY(-10px);
  transition: all 0.3s;
  z-index: 1000;
}

.user-dropdown:hover .dropdown-menu {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  text-decoration: none;
  color: #333;
  font-size: 14px;
  transition: background-color 0.3s;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.dropdown-item:hover {
  background: #f8f9fa;
}

.item-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}

.dropdown-divider {
  height: 1px;
  background: #e9ecef;
  margin: 4px 0;
}

.logout-item {
  color: #ff4757;
}

.logout-item:hover {
  background: #fff5f5;
}

/* Основной контент */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: calc(100vh - 70px);
}

/* Адаптивность */
@media (max-width: 1024px) {
  .header-nav {
    gap: 24px;
    margin-left: 32px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
    height: 60px;
  }

  .header-nav {
    display: none; /* Можно заменить на гамбургер-меню */
  }

  .nav-text {
    display: none;
  }

  .action-btn {
    padding: 8px 12px;
  }

  .user-name {
    display: none;
  }

  .header-actions {
    gap: 8px;
  }
}

@media (max-width: 480px) {
  .logo-title {
    font-size: 18px;
  }

  .logo-subtitle {
    font-size: 11px;
  }

  .cart-total {
    display: none;
  }
}
</style>
