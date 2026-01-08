<template>
  <div class="product-card card">
    <!-- Изображение товара -->
    <div class="card-image">
      <figure class="image is-4by3">
        <img
          :src="product.images && product.images.length > 0
          ? (product.images[0].startsWith('http') ? product.images[0] : `/api/products/images/${product.images[0]}`)
         : 'https://via.placeholder.com/300x300?text=Нет+изображения'"
          :alt="product.name"
          @error="handleImageError"
        >
        <div v-if="isOutOfStock" class="out-of-stock-overlay">
          <span class="tag is-danger is-medium">Нет в наличии</span>
        </div>
      </figure>
    </div>

    <div class="card-content">
      <!-- Категория -->
      <div class="tags has-addons mb-2">
        <span class="tag is-light">{{ product.category }}</span>
      </div>

      <!-- Название товара -->
      <h3 class="title is-5 mb-2">{{ product.name }}</h3>

      <!-- Описание -->
      <p class="description mb-3">{{ truncateDescription(product.description) }}</p>

      <!-- Цена и наличие -->
      <div class="level is-mobile mb-3">
        <div class="level-left">
          <div class="price-info">
            <p class="price has-text-weight-bold">
              {{ minPrice }} ₽
              <span v-if="hasMultiplePrices" class="has-text-grey is-size-7">
                от
              </span>
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

      <!-- Выбор размера (если есть) -->
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

      <!-- Кнопки действий -->
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useCartStore } from '../../stores/cart'
import type { Product, ProductInfo } from '../../types/shop'

interface Props {
  product: Product
  productInfos?: ProductInfo[]
}

const props = defineProps<Props>()
const cartStore = useCartStore()

const selectedSize = ref<ProductInfo | null>(null)
const isAddingToCart = ref(false)

// Изображения товаров (в реальном приложении брать из API)
const productImages = [
  'https://images.unsplash.com/photo-1595435934247-5d33b7f92c70?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1594736797933-d0e49d051b43?w-400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400&h=300&fit=crop'
]

const productImage = computed(() => {
  // В реальном приложении использовать изображения из продукта
  const index = props.product.id % productImages.length
  return productImages[index]
})

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/400x300?text=Изображение+товара'
}

// Общее количество на складе
const totalStock = computed(() => {
  if (!props.productInfos || props.productInfos.length === 0) return 0
  return props.productInfos.reduce((total, info) => total + info.countItems, 0)
})

// Доступные размеры
const availableSizes = computed(() => {
  if (!props.productInfos) return []
  return props.productInfos.filter(info => info.countItems > 0)
})

// Минимальная цена
const minPrice = computed(() => {
  if (!props.productInfos || props.productInfos.length === 0) {
    return props.product.basePrice
  }
  return Math.min(...props.productInfos.map(info => info.price))
})

// Есть ли несколько цен
const hasMultiplePrices = computed(() => {
  if (!props.productInfos || props.productInfos.length <= 1) return false
  const prices = props.productInfos.map(info => info.price)
  return new Set(prices).size > 1
})

// Товар отсутствует
const isOutOfStock = computed(() => totalStock.value === 0)

// Можно ли добавить в корзину
const canAddToCart = computed(() => {
  if (isOutOfStock.value) return false
  if (availableSizes.value.length > 0) {
    return selectedSize.value !== null
  }
  return true
})

// Текст для кнопки добавления в корзину
const addToCartText = computed(() => {
  if (isOutOfStock.value) return 'Нет в наличии'
  if (availableSizes.value.length > 0 && !selectedSize.value) {
    return 'Выберите размер'
  }
  return 'В корзину'
})

// Укорачиваем описание
const truncateDescription = (description: string) => {
  if (description.length > 100) {
    return description.substring(0, 100) + '...'
  }
  return description
}

// При изменении размера
const onSizeChange = () => {
  // Можно добавить дополнительную логику
}

// Добавление в корзину
const addToCart = async () => {
  if (!canAddToCart.value) return

  isAddingToCart.value = true

  try {
    if (availableSizes.value.length > 0 && selectedSize.value) {
      cartStore.addItem(props.product, selectedSize.value, 1)
    } else {
      // Если нет размеров, создаем фиктивную ProductInfo
      const productInfo: ProductInfo = {
        id: props.product.id,
        productId: props.product.id,
        sizeName: 'стандарт',
        countItems: 1,
        price: props.product.basePrice
      }
      cartStore.addItem(props.product, productInfo, 1)
    }

    // В реальном приложении можно показать уведомление
    alert('Товар добавлен в корзину!')
  } catch (error) {
    console.error('Ошибка при добавлении в корзину:', error)
    alert('Не удалось добавить товар в корзину')
  } finally {
    isAddingToCart.value = false
  }
}

// Автоматически выбираем первый доступный размер
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
}

.product-image {
  object-fit: cover;
  height: 200px;
  width: 100%;
  transition: transform 0.3s;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.out-of-stock-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  color: #3e74d1;
  line-height: 1.3;
}

.description {
  color: #a460d1;
  font-size: 0.9rem;
  flex: 1;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.price {
  color: #6c631f;
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

@media (max-width: 768px) {
  .product-card {
    margin-bottom: 1rem;
  }
}
</style>
