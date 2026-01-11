<template>
  <div class="product-card card">
    <div class="card-image">
      <figure class="image is-4by3">
        <img
          :src="getProductImageUrl()"
          :alt="product.name"
          @error="handleImageError"
        >
        <div v-if="isOutOfStock" class="out-of-stock-overlay">
          <span class="tag is-danger is-medium">Нет в наличии</span>
        </div>
      </figure>
    </div>

    <div class="card-content">
      <div class="tags has-addons mb-2">
        <span class="tag is-dark">{{ product.category }}</span>
      </div>

      <h3 class="title is-5 mb-2">{{ product.name }}</h3>
      <p class="description mb-3">{{ truncateDescription(product.description) }}</p>

      <div class="level is-mobile mb-3">
        <div class="level-left">
          <div class="price-info">
            <p class="price has-text-weight-bold">
              <span v-if="hasMultiplePrices" class="has-text-grey is-size-7">
                от
              </span>
              {{ minPrice }} ₽
            </p>
          </div>
        </div>
        <div class="level-right">
          <span
            v-if="totalStock > 0"
            class="tag is-success is-light"
          >
            В наличии
          </span>
          <span v-else class="tag is-danger is-light">
            Нет в наличии
          </span>
        </div>
      </div>

      <div v-if="availableSizes.length > 0" class="field">
        <label class="label is-size-7">Размер:</label>
        <div class="control">
          <div class="select is-fullwidth is-small">
            <select v-model="selectedSize" @change="onSizeChange">
              <option value="" disabled>Выберите размер</option>
              <option
                v-for="size in availableSizes"
                :key="size.id"
                :value="size"
                :disabled="size.countItems === 0"
              >
                {{ size.sizeName }} ({{ size.countItems }} шт.) - {{ size.price }} ₽
              </option>
            </select>
          </div>
        </div>
      </div>

      <div class="buttons">
        <button
          class="button is-primary is-fullwidth"
          :disabled="!canAddToCart || isAddingToCart"
          @click="addToCart"
        >
          <span class="icon" v-if="!isAddingToCart">
            <i class="fas fa-shopping-cart"></i>
          </span>
          <span class="icon" v-else>
            <i class="fas fa-spinner fa-spin"></i>
          </span>
          <span>{{ addToCartText }}</span>
        </button>

        <router-link
          :to="`/shop/products/${product.id}`"
          class="button is-light is-fullwidth"
        >
          <span class="icon">
            <i class="fas fa-info-circle"></i>
          </span>
          <span>Подробнее</span>
        </router-link>
      </div>

      <div v-if="authStore.canEditProducts() || authStore.canDeleteProducts()"
           class="product-admin-actions mt-3">
        <div class="admin-buttons">
          <button
            v-if="authStore.canEditProducts()"
            class="button is-info is-small"
            @click.stop="handleEdit"
            :disabled="isEditing"
          >
            <span class="icon">
              <i class="fas fa-edit"></i>
            </span>
          </button>
          <button
            v-if="authStore.canDeleteProducts()"
            class="button is-danger is-small"
            @click.stop="handleDelete"
            :disabled="isDeleting"
          >
            <span class="icon">
              <i class="fas fa-trash"></i>
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { shopAPI } from '@/api/shop'
import type { Product, ProductInfo } from '@/types/shop'

interface Props {
  product: Product
  productInfos?: ProductInfo[]
}

interface Emits {
  (e: 'edit', productId: number): void
  (e: 'delete', productId: number): void
  (e: 'notification', data: { message: string, type: 'info' | 'success' | 'warning' | 'error' }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const cartStore = useCartStore()
const authStore = useAuthStore()

const selectedSize = ref<ProductInfo | null>(null)
const isAddingToCart = ref(false)
const isEditing = ref(false)
const isDeleting = ref(false)

const getProductImageUrl = () => {
  if (props.product.images && props.product.images.length > 0) {
    const firstImage = props.product.images[0];

    if (firstImage.startsWith('http') || firstImage.startsWith('https')) {
      return firstImage;
    } else if (firstImage.startsWith('/')) {
      return firstImage;
    } else {
      return `/api/products/images/${firstImage}`;
    }
  }
  return 'https://placehold.co/400x300?text=%D0%98%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5+%D1%82%D0%BE%D0%B2%D0%B0%D1%80%D0%B0';
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://placehold.co/400x300?text=%D0%98%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5+%D1%82%D0%BE%D0%B2%D0%B0%D1%80%D0%B0'
}

const totalStock = computed(() => {
  if (!props.productInfos || props.productInfos.length === 0) return 0
  return props.productInfos.reduce((total, info) => total + info.countItems, 0)
})

const availableSizes = computed(() => {
  if (!props.productInfos) return []
  return props.productInfos.filter(info => info.countItems > 0)
})

const minPrice = computed(() => {
  if (!props.productInfos || props.productInfos.length === 0) {
    return props.product.basePrice
  }
  return Math.min(...props.productInfos.map(info => info.price))
})

const hasMultiplePrices = computed(() => {
  if (!props.productInfos || props.productInfos.length <= 1) return false
  const prices = props.productInfos.map(info => info.price)
  return new Set(prices).size > 1
})

const isOutOfStock = computed(() => totalStock.value === 0)

const canAddToCart = computed(() => {
  if (isOutOfStock.value) return false
  if (availableSizes.value.length > 0) {
    return selectedSize.value !== null
  }
  return true
})

const addToCartText = computed(() => {
  if (isOutOfStock.value) return 'Нет в наличии'
  if (availableSizes.value.length > 0 && !selectedSize.value) {
    return 'Выберите размер'
  }
  return 'В корзину'
})

const truncateDescription = (description: string) => {
  if (description.length > 100) {
    return description.substring(0, 100) + '...'
  }
  return description
}

const onSizeChange = () => {
}

const addToCart = async () => {
  if (!canAddToCart.value) return

  isAddingToCart.value = true

  try {
    if (availableSizes.value.length > 0 && selectedSize.value) {
      cartStore.addItem(props.product, selectedSize.value, 1)
      emit('notification', {
        message: 'Товар успешно добавлен в корзину!',
        type: 'success'
      })
    } else {
      const productInfo: ProductInfo = {
        id: props.product.id,
        productId: props.product.id,
        sizeName: 'стандарт',
        countItems: 1,
        price: props.product.basePrice
      }
      cartStore.addItem(props.product, productInfo, 1)
      emit('notification', {
        message: 'Товар успешно добавлен в корзину!',
        type: 'success'
      })
    }

  } catch (error) {
    console.error('Ошибка при добавлении в корзину:', error)
    emit('notification', {
      message: 'Не удалось добавить товар в корзину',
      type: 'error'
    })
  } finally {
    isAddingToCart.value = false
  }
}

const handleEdit = () => {
  emit('edit', props.product.id)
}

const handleDelete = async () => {
  isDeleting.value = true
  try {
    await shopAPI.deleteProduct(props.product.id)
    emit('delete', props.product.id)
    emit('notification', {
      message: 'Товар успешно удален!',
      type: 'success'
    })
  } catch (error: any) {
    console.error('Ошибка при удалении товара:', error)
    const errorMessage = error.response?.data?.message || 'Не удалось удалить товар'
    emit('notification', {
      message: errorMessage,
      type: 'error'
    })
  } finally {
    isDeleting.value = false
  }
}

if (availableSizes.value.length > 0) {
  selectedSize.value = availableSizes.value[0]
}
</script>

<style scoped>
.product-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s, box-shadow 0.3s;
  border-radius: 12px;
  overflow: hidden;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.card-image {
  position: relative;
  overflow: hidden;
  height: 270px;
}

.image.is-4by3 {
  position: relative;
  height: 100%;
  width: 100%;
}

.product-card:hover {
  transform: scale(1.05);
}

.out-of-stock-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding-bottom: 1.5rem;
}

.title {
  color: #3e74d1;
  line-height: 1.3;
}

.description {
  color: #666;
  font-size: 0.9rem;
  flex: 1;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price {
  color: #949cae;
  font-size: 1.25rem;
}

.tag.is-success {
  background-color: #10b981;
  color: white;
}

.tag.is-danger {
  background-color: #ef4444;
  color: white;
}

.buttons {
  margin-top: auto;
  margin-bottom: 0.5rem;
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

.button.is-light {
  border: 1px solid #e5e7eb;
}

.product-admin-actions {
  border-top: 1px solid #f0f0f0;
  padding-top: 1rem;
}

.admin-buttons {
  display: flex;
  gap: 0.5rem;
}

.admin-buttons .button {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  padding: 0.4rem 0.5rem;
}

.admin-buttons .button.is-info {
  background-color: #3e74d1;
  color: white;
  border: none;
}

.admin-buttons .button.is-info:hover {
  opacity: 0.9;
}

.admin-buttons .button.is-danger {
  background-color: #ef4444;
  color: white;
  border: none;
}

.admin-buttons .button.is-danger:hover {
  opacity: 0.9;
}

.admin-buttons .button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .product-card {
    margin-bottom: 1rem;
  }

  .card-image {
    height: 180px;
  }

  .admin-buttons .button {
    font-size: 0.75rem;
    padding: 0.3rem 0.4rem;
  }
}
</style>
