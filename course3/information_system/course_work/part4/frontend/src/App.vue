<template>
  <div id="app">
    <nav class="navbar custom-navbar" role="navigation" aria-label="main navigation">
      <div class="container">
        <div class="navbar-brand">
          <router-link to="/" class="navbar-item brand-item">
            <div class="brand-content">
              <div class="brand-text">
                <strong class="brand-title">Artistic Rhythmic</strong>
                <span class="brand-subtitle">Художественная Гимнастика</span>
              </div>
            </div>
          </router-link>
        </div>

        <div class="navbar-menu">
          <div class="navbar-start">
            <router-link to="/" class="navbar-item nav-item">
              <span>Главная</span>
            </router-link>
            <router-link to="/posts" class="navbar-item nav-item">
              <span>Лента постов</span>
            </router-link>
            <router-link to="/tournaments" class="navbar-item nav-item">
              <span>Соревнования</span>
            </router-link>
            <router-link
              v-if="authStore.isAuthenticated"
              to="/shop"
              class="navbar-item nav-item"
            >
              <span>Магазин</span>
            </router-link>
            <router-link
              v-if="authStore.isAdmin"
              to="/admin/users"
              class="navbar-item nav-item admin-nav"
            >
              <span>Админка</span>
            </router-link>
          </div>

          <div class="navbar-end">
            <div class="navbar-item">
              <div class="buttons">
                <button
                  v-if="authStore.isAuthenticated"
                  class="button cart-button"
                  @click="toggleCart"
                  :class="{ 'has-items': cartStore.totalItems > 0 }"
                >
                  <span class="icon">
                    <i class="fas fa-shopping-bag"></i>
                  </span>
                  <span v-if="cartStore.totalItems > 0" class="cart-info">
                    <span class="cart-count">{{ cartStore.totalItems }}</span>
                  </span>
                </button>

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
                          <i class="fas fa-chevron-down" aria-hidden="true"></i>
                        </span>
                      </button>
                    </div>
                    <div class="dropdown-menu" id="user-menu" role="menu">
                      <div class="dropdown-content custom-dropdown-content">
                        <router-link to="/shop/orders" class="dropdown-item">
                          <i class="fas fa-receipt dropdown-icon"></i>
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

    <main class="container main-content">
      <router-view />
    </main>

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
  await router.push('/login')
}
</script>

<style scoped>
.custom-navbar {
  background: #1a1a1a;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(71, 85, 105, 0.5);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.brand-item {
  padding: 0.5rem 0;
}

.brand-content {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.brand-title {
  font-size: 1.3rem;
  font-weight: 800;
  background: linear-gradient(135deg, #e2e8f0 0%, #94a3b8 50%, #c7d2fe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.3px;
}

.brand-subtitle {
  font-size: 0.8rem;
  color: #94a3b8;
  font-weight: 400;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.navbar-start {
  margin-left: 2rem;
}

.nav-item {
  color: #cbd5e1 !important;
  font-weight: 500;
  margin: 0 0.25rem;
  padding: 0.75rem 1.25rem;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  font-size: 0.95rem;
}

.nav-item:hover {
  color: #f1f5f9 !important;
  background: rgba(100, 116, 139, 0.2);
  transform: translateY(-1px);
}

.admin-nav {
  color: #c4b5fd !important;
}

.admin-nav:hover {
  color: #ddd6fe !important;
  background: rgba(139, 92, 246, 0.2);
}

.admin-nav.router-link-active {
  color: #ddd6fe !important;
  background: rgba(139, 92, 246, 0.3);
}

.button {
  border: none;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: auto;
  padding: 0.75rem 1.5rem;
  font-size: 0.9rem;
}

.cart-button {
  background: rgba(71, 85, 105, 0.4);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.3);
  padding: 0.75rem 1.25rem;
}

.cart-button:hover {
  background: rgba(100, 116, 139, 0.5);
  color: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.cart-button.has-items {
  background: rgba(139, 92, 246, 0.2);
  color: #c4b5fd;
  border-color: rgba(139, 92, 246, 0.4);
}

.cart-button.has-items:hover {
  background: rgba(139, 92, 246, 0.3);
  color: #ddd6fe;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);
}

.cart-button.has-items::before {
  content: '';
  position: absolute;
  top: -4px;
  right: -4px;
  width: 12px;
  height: 12px;
  background: linear-gradient(135deg, #818cf8, #c4b5fd);
  border-radius: 50%;
  animation: pulse 2s infinite;
  border: 2px solid #1e293b;
}

.cart-info {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  margin-left: 0.5rem;
}

.cart-count {
  background: linear-gradient(135deg, #818cf8, #c4b5fd);
  color: #0f172a;
  min-width: 20px;
  height: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0 0.25rem;
}

.register-button {
  background: rgba(71, 85, 105, 0.4);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.register-button:hover {
  background: rgba(100, 116, 139, 0.5);
  color: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.login-button {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  font-weight: 600;
  border: none;
}

.login-button:hover {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.user-button {
  background: rgba(71, 85, 105, 0.4);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.3);
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
}

.user-button:hover {
  background: rgba(100, 116, 139, 0.5);
  color: #f1f5f9;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-avatar {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #818cf8 0%, #c4b5fd 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
  color: #0f172a;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.custom-dropdown-content {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.95) 0%, rgba(15, 23, 42, 0.95) 100%);
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(71, 85, 105, 0.5);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: transparent;
  color: #cbd5e1;
  transition: all 0.2s;
  font-weight: 500;
}

.dropdown-item:hover {
  background: rgba(100, 116, 139, 0.3);
  color: #f1f5f9;
  transform: translateX(2px);
}

.dropdown-icon {
  width: 16px;
  text-align: center;
  font-size: 0.875rem;
}

.dropdown-divider {
  margin: 0.5rem 0;
  background: rgba(71, 85, 105, 0.5);
  height: 1px;
}

.logout-item:hover {
  background: rgba(239, 68, 68, 0.3);
  color: #fecaca;
}

.main-content {
  padding: 2rem 0;
  min-height: calc(100vh - 80px);
}

@keyframes slideIn {
  from {
    width: 0;
    opacity: 0;
  }
  to {
    width: 20px;
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

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
    background: linear-gradient(135deg,
    rgba(7, 9, 18, 0.95) 0%,
    rgba(27, 34, 43, 0.95) 100%);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    border-top: 1px solid rgba(71, 85, 105, 0.5);
  }

  .brand-subtitle {
    display: none;
  }

  .cart-info {
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

  .brand-text {
    display: none;
  }

  .container {
    padding: 0 0.75rem;
  }

  .button {
    padding: 0.5rem 1rem;
  }
}
</style>
