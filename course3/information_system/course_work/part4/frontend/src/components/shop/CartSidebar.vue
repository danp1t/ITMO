<template>
  <div class="cart-sidebar" :class="{ 'is-active': isVisible }">
    <!-- Оверлей -->
    <div class="sidebar-overlay" @click="$emit('close')"></div>

    <!-- Сайдбар -->
    <div class="sidebar-content">
      <!-- Заголовок -->
      <div class="sidebar-header">
        <h3 class="title is-4">Корзина</h3>
        <button class="delete" @click="$emit('close')"></button>
      </div>

      <!-- Содержимое корзины -->
      <div class="sidebar-body">
        <!-- Пустая корзина -->
        <div v-if="cartStore.isEmpty" class="empty-cart">
          <div class="empty-cart-icon">
            <i class="fas fa-shopping-cart fa-3x has-text-grey-light"></i>
          </div>
          <p class="empty-cart-text has-text-grey">Ваша корзина пуста</p>
          <button class="button is-primary is-outlined" @click="$emit('close')">
            Продолжить покупки
          </button>
        </div>

        <!-- Товары в корзине -->
        <div v-else>
          <div class="cart-items">
            <div
              v-for="item in cartStore.getCartItemsWithDetails"
              :key="`${item.productId}-${item.productInfoId}`"
              class="cart-item"
            >
              <div class="cart-item-content">
                <div class="cart-item-info">
                  <p class="cart-item-name">{{ item.productName }}</p>
                  <p class="cart-item-size has-text-grey is-size-7">
                    Размер: {{ item.size }}
                  </p>
                  <p class="cart-item-price has-text-weight-semibold">
                    {{ item.price }} ₽ × {{ item.quantity }} = {{ item.subtotal }} ₽
                  </p>
                </div>

                <div class="cart-item-actions">
                  <div class="field has-addons">
                    <div class="control">
                      <button
                        class="button is-small"
                        @click="decreaseQuantity(item)"
                        :disabled="item.quantity <= 1"
                      >
                        <span class="icon is-small">
                          <i class="fas fa-minus"></i>
                        </span>
                      </button>
                    </div>
                    <div class="control">
                      <input
                        class="input is-small"
                        type="number"
                        :value="item.quantity"
                        @input="updateQuantity(item, $event)"
                        min="1"
                        max="99"
                        style="width: 60px; text-align: center"
                      >
                    </div>
                    <div class="control">
                      <button
                        class="button is-small"
                        @click="increaseQuantity(item)"
                      >
                        <span class="icon is-small">
                          <i class="fas fa-plus"></i>
                        </span>
                      </button>
                    </div>
                  </div>

                  <button
                    class="button is-danger is-small is-light"
                    @click="removeItem(item)"
                  >
                    <span class="icon is-small">
                      <i class="fas fa-trash"></i>
                    </span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Итоговая сумма -->
          <div class="cart-total">
            <div class="level is-mobile">
              <div class="level-left">
                <span class="has-text-weight-semibold">Итого:</span>
              </div>
              <div class="level-right">
                <span class="has-text-weight-bold is-size-4">{{ cartStore.totalAmount }} ₽</span>
              </div>
            </div>
          </div>

          <!-- Кнопки действий -->
          <div class="cart-actions">
            <button class="button is-danger is-light is-fullwidth" @click="clearCart">
              Очистить корзину
            </button>
            <router-link
              to="/shop/checkout"
              class="button is-primary is-fullwidth"
              @click="$emit('close')"
            >
              Оформить заказ
            </router-link>
            <button class="button is-light is-fullwidth" @click="$emit('close')">
              Продолжить покупки
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useCartStore } from '../../stores/cart'
import type { CartItem } from '../../types/shop'

interface Props {
  isVisible: boolean
}

interface Emits {
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const cartStore = useCartStore()

const decreaseQuantity = (item: CartItem) => {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.productId, item.productInfoId, item.quantity - 1)
  }
}

const increaseQuantity = (item: CartItem) => {
  cartStore.updateQuantity(item.productId, item.productInfoId, item.quantity + 1)
}

const updateQuantity = (item: CartItem, event: Event) => {
  const input = event.target as HTMLInputElement
  const quantity = parseInt(input.value) || 1
  cartStore.updateQuantity(item.productId, item.productInfoId, quantity)
}

const removeItem = (item: CartItem) => {
    cartStore.removeItem(item.productId, item.productInfoId)
}

const clearCart = () => {
  if (confirm('Очистить всю корзину?')) {
    cartStore.clearCart()
  }
}
</script>

<style scoped>
.cart-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  max-width: 400px;
  z-index: 1000;
  transform: translateX(100%);
  transition: transform 0.3s ease;
}

.cart-sidebar.is-active {
  transform: translateX(0);
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
}

.sidebar-content {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  background: rgb(30, 27, 27);
  display: flex;
  flex-direction: column;
  box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.sidebar-header .title {
  color: white;
  margin: 0;
}

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.empty-cart-icon {
  margin-bottom: 1rem;
}

.empty-cart-text {
  margin-bottom: 1.5rem;
}

.cart-items {
  margin-bottom: 1.5rem;
}

.cart-item {
  padding: 1rem 0;
  border-bottom: 1px solid #f1f3f5;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.cart-item-info {
  flex: 1;
}

.cart-item-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.cart-item-size {
  margin-bottom: 0.5rem;
}

.cart-item-price {
  color: #8d939f;
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-total {
  padding: 1.5rem 0;
  border-top: 2px solid #e5e7eb;
  border-bottom: 2px solid #e5e7eb;
  margin-bottom: 1.5rem;
}

.cart-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.button.is-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 600;
}

.button.is-primary:hover {
  opacity: 0.9;
}

@media (max-width: 768px) {
  .cart-sidebar {
    max-width: 100%;
  }
}
</style>
