<template>
  <div class="cart-sidebar" :class="{ 'is-active': isVisible }">
    <div class="sidebar-overlay" @click="$emit('close')"></div>

    <div class="sidebar-content">
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
          <!-- Уведомление о недоступности товаров -->
          <div v-if="hasStockIssues" class="notification is-warning is-light mb-4">
            <div class="content is-small">
              <p class="mb-1">
                <i class="fas fa-exclamation-triangle mr-1"></i>
                Некоторые товары недоступны в запрошенном количестве
              </p>
              <p class="is-size-7">
                Количество было автоматически скорректировано по наличию
              </p>
            </div>
          </div>

          <div class="cart-items">
            <div
              v-for="item in cartStore.getCartItemsWithDetails"
              :key="`${item.productId}-${item.productInfoId}`"
              class="cart-item"
              :class="{ 'has-stock-warning': item.quantity > getAvailableStock(item) }"
            >
              <div class="cart-item-content">
                <div class="cart-item-info">
                  <p class="cart-item-name">{{ item.productName }}</p>
                  <p class="cart-item-size has-text-grey is-size-7">
                    Размер: {{ item.size }}
                  </p>
                  <div class="stock-info" v-if="item.quantity > getAvailableStock(item)">
                    <span class="tag is-danger is-light is-small mb-1">
                      Доступно: {{ getAvailableStock(item) }} шт.
                    </span>
                  </div>
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
                        :class="{ 'is-danger': item.quantity > getAvailableStock(item) }"
                      >
                        <span class="icon is-small">
                          <i class="fas fa-minus"></i>
                        </span>
                      </button>
                    </div>
                    <div class="control">
                      <input
                        class="input is-small"
                        :class="{ 'is-danger': item.quantity > getAvailableStock(item) }"
                        type="number"
                        :value="item.quantity"
                        @input="updateQuantity(item, $event)"
                        @blur="validateQuantityOnBlur(item)"
                        min="1"
                        :max="getAvailableStock(item)"
                        style="width: 60px; text-align: center"
                        :title="item.quantity > getAvailableStock(item) ? `Максимум: ${getAvailableStock(item)} шт.` : ''"
                      >
                    </div>
                    <div class="control">
                      <button
                        class="button is-small"
                        @click="increaseQuantity(item)"
                        :disabled="item.quantity >= getAvailableStock(item)"
                        :class="{ 'is-danger': item.quantity > getAvailableStock(item) }"
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
            <div v-if="hasStockIssues" class="has-text-danger is-size-7 mt-2">
              <i class="fas fa-exclamation-circle mr-1"></i>
              Итоговая сумма будет пересчитана после исправления количества
            </div>
          </div>

          <!-- Кнопки действий -->
          <div class="cart-actions">
            <button class="button is-danger is-light is-fullwidth" @click="clearCart">
              Очистить корзину
            </button>
            <button
              class="button is-primary is-fullwidth"
              @click="proceedToCheckout"
              :disabled="hasStockIssues"
              :title="hasStockIssues ? 'Исправьте количество товаров перед оформлением заказа' : ''"
            >
              Оформить заказ
            </button>
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
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { shopAPI } from '@/api/shop'
import type { CartItem, ProductInfo } from '../../types/shop'

interface Props {
  isVisible: boolean
}

interface Emits {
  (e: 'close'): void
  (e: 'notification', data: { message: string, type: 'info' | 'success' | 'warning' | 'error' }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const router = useRouter()

const cartStore = useCartStore()
const productInfosCache = ref<Map<number, ProductInfo>>(new Map())
const isLoadingInfo = ref<Set<number>>(new Set())

// Загружаем информацию о наличии товаров
const loadProductInfos = async () => {
  if (cartStore.isEmpty) return

  const uniqueProductInfoIds = Array.from(
    new Set(cartStore.items.map(item => item.productInfoId))
  )

  const promises = uniqueProductInfoIds.map(async (id) => {
    if (productInfosCache.value.has(id)) return

    try {
      isLoadingInfo.value.add(id)
      const response = await shopAPI.getProductInfoById(id)
      productInfosCache.value.set(id, response.data)
    } catch (error) {
      console.error(`Ошибка при загрузке информации о товаре ${id}:`, error)
      emit('notification', {
        message: 'Не удалось загрузить актуальную информацию о наличии товаров',
        type: 'warning'
      })
    } finally {
      isLoadingInfo.value.delete(id)
    }
  })

  await Promise.all(promises)
}

// Получаем доступное количество товара
const getAvailableStock = (item: CartItem): number => {
  const productInfo = productInfosCache.value.get(item.productInfoId)
  return productInfo?.countItems || 0
}

// Проверяем, есть ли проблемы с наличием
const hasStockIssues = computed(() => {
  return cartStore.items.some(item => item.quantity > getAvailableStock(item))
})

// Получаем информацию о товаре из кеша
const getProductInfo = (productInfoId: number): ProductInfo | null => {
  return productInfosCache.value.get(productInfoId) || null
}

const decreaseQuantity = (item: CartItem) => {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.productId, item.productInfoId, item.quantity - 1)
  }
}

const increaseQuantity = async (item: CartItem) => {
  const availableStock = getAvailableStock(item)

  if (availableStock === 0) {
    emit('notification', {
      message: 'Товар временно отсутствует в наличии',
      type: 'warning'
    })
    return
  }

  if (item.quantity < availableStock) {
    cartStore.updateQuantity(item.productId, item.productInfoId, item.quantity + 1)
  } else {
    emit('notification', {
      message: `Максимальное количество: ${availableStock}`,
      type: 'warning'
    })
  }
}

const updateQuantity = async (item: CartItem, event: Event) => {
  const input = event.target as HTMLInputElement
  let quantity = parseInt(input.value) || 1

  // Если информация о наличии не загружена, загружаем
  if (!productInfosCache.value.has(item.productInfoId)) {
    await loadProductInfos()
  }

  const availableStock = getAvailableStock(item)

  // Если введенное количество больше доступного
  if (quantity > availableStock) {
    quantity = availableStock
    input.value = availableStock.toString()

    if (availableStock > 0) {
      emit('notification', {
        message: `Максимальное количество: ${availableStock}`,
        type: 'warning'
      })
    } else {
      emit('notification', {
        message: 'Товар временно отсутствует в наличии',
        type: 'error'
      })
    }
  }

  // Если количество стало 0 или меньше
  if (quantity < 1) {
    quantity = 1
    input.value = '1'
  }

  // Обновляем только если количество изменилось
  if (quantity !== item.quantity) {
    cartStore.updateQuantity(item.productId, item.productInfoId, quantity)
  }
}

const validateQuantityOnBlur = async (item: CartItem) => {
  // Если информация о наличии не загружена, загружаем
  if (!productInfosCache.value.has(item.productInfoId)) {
    await loadProductInfos()
  }

  const availableStock = getAvailableStock(item)

  // Если текущее количество больше доступного, корректируем
  if (item.quantity > availableStock && availableStock > 0) {
    cartStore.updateQuantity(item.productId, item.productInfoId, availableStock)
    emit('notification', {
      message: `Количество скорректировано до ${availableStock} шт. по наличию`,
      type: 'info'
    })
  } else if (availableStock === 0) {
    // Если товара нет в наличии, удаляем из корзины
    cartStore.removeItem(item.productId, item.productInfoId)
    emit('notification', {
      message: 'Товар временно отсутствует в наличии и был удален из корзины',
      type: 'error'
    })
  }
}

const removeItem = (item: CartItem) => {
  cartStore.removeItem(item.productId, item.productInfoId)
  emit('notification', {
    message: 'Товар удален из корзины',
    type: 'info'
  })
}

const clearCart = () => {
  cartStore.clearCart()
  productInfosCache.value.clear()
  emit('notification', {
    message: 'Корзина очищена',
    type: 'info'
  })
}

const proceedToCheckout = () => {
  if (hasStockIssues.value) {
    emit('notification', {
      message: 'Пожалуйста, исправьте количество товаров перед оформлением заказа',
      type: 'error'
    })
    return
  }

  emit('close')
  router.push('/shop/checkout')
}

// При открытии корзины загружаем актуальную информацию
watch(() => props.isVisible, async (isVisible) => {
  if (isVisible) {
    await loadProductInfos()

    // Проверяем все товары в корзине и корректируем при необходимости
    for (const item of cartStore.items) {
      const availableStock = getAvailableStock(item)
      if (item.quantity > availableStock && availableStock > 0) {
        cartStore.updateQuantity(item.productId, item.productInfoId, availableStock)
      }
    }
  }
})

// При изменении состава корзины обновляем кеш
watch(() => cartStore.items, (newItems) => {
  // Удаляем из кеша информацию о товарах, которых больше нет в корзине
  const currentProductInfoIds = new Set(newItems.map(item => item.productInfoId))
  for (const cachedId of productInfosCache.value.keys()) {
    if (!currentProductInfoIds.has(cachedId)) {
      productInfosCache.value.delete(cachedId)
    }
  }

  // Загружаем информацию для новых товаров
  if (props.isVisible) {
    loadProductInfos()
  }
}, { deep: true })

onMounted(() => {
  if (props.isVisible) {
    loadProductInfos()
  }
})
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
  transition: background-color 0.2s;
}

.cart-item.has-stock-warning {
  background-color: rgba(239, 68, 68, 0.05);
  border-left: 3px solid #ef4444;
  padding-left: 0.5rem;
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
  color: #e0e0e0;
}

.cart-item-size {
  margin-bottom: 0.5rem;
  color: #a0a0a0;
}

.cart-item-price {
  color: #7b8591;
  margin-top: 0.5rem;
}

.stock-info {
  margin-bottom: 0.5rem;
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

.button.is-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.input.is-danger,
.button.is-danger {
  border-color: #ef4444;
  background-color: rgba(239, 68, 68, 0.1);
}

.input.is-danger:focus {
  box-shadow: 0 0 0 0.125em rgba(239, 68, 68, 0.25);
}

.notification.is-warning {
  background-color: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

@media (max-width: 768px) {
  .cart-sidebar {
    max-width: 100%;
  }

  .cart-item-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .cart-item-actions {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
