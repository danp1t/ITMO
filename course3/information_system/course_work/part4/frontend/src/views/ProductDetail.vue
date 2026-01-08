<template>
  <div class="product-detail">
    <!-- Хлебные крошки (только когда product загружен) -->
    <nav v-if="product" class="breadcrumb mb-5" aria-label="breadcrumbs">
      <ul>
        <li><router-link to="/shop">Магазин</router-link></li>
        <li><a href="#">{{ product.category }}</a></li>
        <li class="is-active"><a href="#" aria-current="page">{{ product.name }}</a></li>
      </ul>
    </nav>

    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка товара...</p>
    </div>

    <div v-else-if="product" class="columns">
      <!-- Левая колонка - Изображения -->
      <div class="column is-half">
        <!-- Главное изображение -->
        <div class="main-image mb-4">
          <figure class="image is-4by3">
            <img
              :src="mainImage"
              :alt="product.name"
              class="product-main-image"
              @error="handleImageError"
            >
            <div v-if="isOutOfStock" class="out-of-stock-overlay">
              <span class="tag is-danger is-medium">Нет в наличии</span>
            </div>
          </figure>
        </div>

        <!-- Галерея изображений -->
        <div v-if="productImages.length > 1" class="image-gallery">
          <div class="columns is-multiline is-mobile">
            <div
              v-for="(img, index) in productImages"
              :key="index"
              class="column is-3"
              @click="changeMainImage(img)"
            >
              <figure class="image is-square">
                <img
                  :src="img"
                  :alt="`${product.name} - изображение ${index + 1}`"
                  :class="{'is-active': img === mainImage}"
                  class="gallery-thumbnail"
                >
              </figure>
            </div>
          </div>
        </div>
      </div>

      <!-- Правая колонка - Информация -->
      <div class="column is-half">
        <!-- Категория -->
        <div class="tags has-addons mb-3">
          <span class="tag is-dark">{{ product.category }}</span>
          <span v-if="totalStock > 0" class="tag is-success">
            В наличии
          </span>
          <span v-else class="tag is-danger">
            Нет в наличии
          </span>
        </div>

        <!-- Название и рейтинг -->
        <div class="level is-mobile mb-4">
          <div class="level-left">
            <h1 class="title is-3">{{ product.name }}</h1>
          </div>
          <div class="level-right">
            <div class="rating">
              <span class="icon has-text-warning">
                <i class="fas fa-star"></i>
              </span>
              <span class="has-text-weight-bold ml-1">{{ product.popularity || 4.5 }}</span>
            </div>
          </div>
        </div>

        <!-- Цена -->
        <div class="price-section mb-5">
          <div class="level is-mobile">
            <div class="level-left">
              <div>
                <p class="title is-2">
                  <span v-if="hasMultiplePrices" class="subtitle is-3">
                    от
                  </span>
                  {{ minPrice }} ₽

                </p>
                <p v-if="hasMultiplePrices" class="has-text-grey">
                  Диапазон цен: {{ minPrice }} - {{ maxPrice }} ₽
                </p>
              </div>
            </div>
            <div class="level-right">
              <button class="button is-text" @click="addToWishlist">
                <span class="icon">
                  <i :class="isInWishlist ? 'fas fa-heart has-text-danger' : 'far fa-heart'"></i>
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- Описание -->
        <div class="description-section mb-5">
          <h3 class="title is-5 mb-3">Описание</h3>
          <p class="content">{{ product.description }}</p>
        </div>

        <!-- Выбор размера и количества -->
        <div class="selection-section mb-5">
          <div class="field">
            <label class="label">Размер:</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="selectedSize" @change="onSizeChange">
                  <option value="" disabled>Выберите размер</option>
                  <option
                    v-for="info in sortedProductInfos"
                    :key="info.id"
                    :value="info"
                    :disabled="info.countItems === 0"
                  >
                    {{ info.sizeName }} ({{ info.countItems }} шт.) - {{ info.price }} ₽
                  </option>
                </select>
              </div>
            </div>
          </div>

          <div class="field" v-if="selectedSize">
            <label class="label">Количество:</label>
            <div class="control">
              <div class="field has-addons">
                <div class="control">
                  <button
                    class="button"
                    @click="decreaseQuantity"
                    :disabled="quantity <= 1"
                  >
                    <span class="icon is-small">
                      <i class="fas fa-minus"></i>
                    </span>
                  </button>
                </div>
                <div class="control">
                  <input
                    v-model.number="quantity"
                    class="input"
                    type="number"
                    min="1"
                    :max="selectedSize.countItems"
                    style="width: 100px; text-align: center"
                  >
                </div>
                <div class="control">
                  <button
                    class="button"
                    @click="increaseQuantity"
                    :disabled="quantity >= selectedSize.countItems"
                  >
                    <span class="icon is-small">
                      <i class="fas fa-plus"></i>
                    </span>
                  </button>
                </div>
                <div class="control">
                  <span class="tag is-info ml-3">
                    Доступно: {{ selectedSize.countItems }} шт.
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Кнопки действий -->
        <div class="action-buttons mb-6">
          <div class="buttons">
            <button
              class="button is-primary is-large"
              :disabled="!canAddToCart || isAddingToCart"
              @click="addToCart"
              style="flex: 2"
            >
              <span class="icon" v-if="!isAddingToCart">
                <i class="fas fa-shopping-cart"></i>
              </span>
              <span class="icon" v-else>
                <i class="fas fa-spinner fa-spin"></i>
              </span>
              <span>{{ addToCartText }}</span>
            </button>
            <button
              class="button is-outlined is-large"
              :disabled="!selectedSize"
              @click="buyNow"
              style="flex: 1"
            >
              <span>Купить сейчас</span>
            </button>
          </div>
        </div>

        <!-- Детали товара -->
        <div class="details-section">
          <div class="box">
            <h3 class="title is-5 mb-3">Характеристики</h3>
            <div class="content">
              <table class="table is-fullwidth">
                <tbody>
                <tr>
                  <td><strong>Категория:</strong></td>
                  <td>{{ product.category }}</td>
                </tr>
                <tr>
                  <td><strong>Базовая цена:</strong></td>
                  <td>{{ product.basePrice }} ₽</td>
                </tr>
                <tr v-if="product.popularity">
                  <td><strong>Популярность:</strong></td>
                  <td>{{ product.popularity }}/5</td>
                </tr>
                <tr>
                  <td><strong>Общее количество:</strong></td>
                  <td>{{ totalStock }} шт.</td>
                </tr>
                <tr v-if="product.createdAt">
                  <td><strong>Добавлен:</strong></td>
                  <td>{{ formatDate(product.createdAt) }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Связанные товары -->
    <div v-if="!loading && product" class="related-products mt-6">
      <h2 class="title is-4 mb-4">Похожие товары</h2>
      <div v-if="relatedProducts.length > 0" class="columns is-multiline">
        <div
          v-for="relatedProduct in relatedProducts"
          :key="relatedProduct.id"
          class="column is-3"
        >
          <ProductCard
            :product="relatedProduct"
            :product-infos="getProductInfos(relatedProduct.id)"
          />
        </div>
      </div>
      <div v-else class="has-text-centered py-4">
        <p class="has-text-grey">Похожих товаров не найдено</p>
      </div>
    </div>

    <!-- Сообщение об ошибке -->
    <div v-if="error" class="has-text-centered py-6">
      <i class="fas fa-exclamation-triangle fa-3x has-text-danger"></i>
      <p class="title is-4 mt-4">Товар не найден</p>
      <p class="subtitle is-6 has-text-grey">
        Запрошенный товар не существует или был удален
      </p>
      <router-link to="/shop" class="button is-primary mt-3">
        Вернуться в магазин
      </router-link>
    </div>

    <!-- Уведомление -->
    <div v-if="showNotification" class="notification is-success is-light fixed-notification">
      <button class="delete" @click="showNotification = false"></button>
      <p>Товар успешно добавлен в корзину!</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import ProductCard from '../components/shop/ProductCard.vue'
import { shopAPI } from '../api/shop'
import type { Product, ProductInfo } from '../types/shop'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// Данные
const product = ref<Product | null>(null)
const productInfos = ref<ProductInfo[]>([])
const loading = ref(true)
const error = ref(false)

// Состояние
const mainImage = ref('')
const selectedSize = ref<ProductInfo | null>(null)
const quantity = ref(1)
const isAddingToCart = ref(false)
const showNotification = ref(false)
const isInWishlist = ref(false)
const relatedProducts = ref<Product[]>([])

// Изображения товаров (в реальном приложении брать из API)
const productImages = [
  'https://images.unsplash.com/photo-1595435934247-5d33b7f92c70?w=800&h=600&fit=crop',
  'https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=800&h=600&fit=crop',
  'https://images.unsplash.com/photo-1594736797933-d0e49d051b43?w=800&h=600&fit=crop',
  'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800&h=600&fit=crop'
]

// Получить ID товара из маршрута
const productId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : Array.isArray(id) ? parseInt(id[0]) : id
})

// Общее количество на складе
const totalStock = computed(() => {
  return productInfos.value.reduce((total, info) => total + info.countItems, 0)
})

// Доступные размеры
const availableSizes = computed(() => {
  return productInfos.value.filter(info => info.countItems > 0)
})

// Сортированные размеры
const sortedProductInfos = computed(() => {
  return [...productInfos.value].sort((a, b) => {
    // Сначала доступные, потом по алфавиту
    if (a.countItems === 0 && b.countItems > 0) return 1
    if (a.countItems > 0 && b.countItems === 0) return -1
    return a.sizeName.localeCompare(b.sizeName)
  })
})

// Минимальная цена
const minPrice = computed(() => {
  if (productInfos.value.length === 0) {
    return product.value?.basePrice || 0
  }
  const prices = productInfos.value.map(info => info.price)
  return Math.min(...prices)
})

// Максимальная цена
const maxPrice = computed(() => {
  if (productInfos.value.length === 0) {
    return product.value?.basePrice || 0
  }
  const prices = productInfos.value.map(info => info.price)
  return Math.max(...prices)
})

// Есть ли несколько цен
const hasMultiplePrices = computed(() => {
  if (productInfos.value.length <= 1) return false
  const prices = productInfos.value.map(info => info.price)
  return new Set(prices).size > 1
})

// Товар отсутствует
const isOutOfStock = computed(() => totalStock.value === 0)

// Можно ли добавить в корзину
const canAddToCart = computed(() => {
  if (isOutOfStock.value) return false
  if (availableSizes.value.length > 0) {
    return selectedSize.value !== null && quantity.value > 0
  }
  return quantity.value > 0
})

// Текст для кнопки добавления в корзину
const addToCartText = computed(() => {
  if (isOutOfStock.value) return 'Нет в наличии'
  if (availableSizes.value.length > 0 && !selectedSize.value) {
    return 'Выберите размер'
  }
  const price = selectedSize.value ? selectedSize.value.price : minPrice.value
  return `Добавить в корзину (${price * quantity.value} ₽)`
})

// Загрузить товар
const loadProduct = async () => {
  loading.value = true
  error.value = false

  try {
    // Загружаем основной товар
    const productResponse = await shopAPI.getProductById(productId.value as number)
    product.value = productResponse.data

    // Загружаем информацию о товаре
    const infosResponse = await shopAPI.getProductInfoByProduct(productId.value as number)
    productInfos.value = infosResponse.data

    // Устанавливаем первое изображение как главное
    if (product.value.images && product.value.images.length > 0) {
      mainImage.value = product.value.images[0]
    } else {
      const index = (productId.value as number) % productImages.length
      mainImage.value = productImages[index]
    }

    // Автоматически выбираем первый доступный размер
    if (availableSizes.value.length > 0) {
      selectedSize.value = availableSizes.value[0]
    }

    // Загружаем похожие товары
    await loadRelatedProducts()
  } catch (err) {
    console.error('Ошибка при загрузке товара:', err)
    error.value = true
  } finally {
    loading.value = false
  }
}

// Загрузить похожие товары
const loadRelatedProducts = async () => {
  try {
    const response = await shopAPI.getProducts()
    if (product.value) {
      // Фильтруем товары той же категории, исключая текущий
      relatedProducts.value = response.data
        .filter(p => p.category === product.value!.category && p.id !== productId.value)
        .slice(0, 4)
    }
  } catch (err) {
    console.error('Ошибка при загрузке похожих товаров:', err)
  }
}

// Изображения товара
const productImagesComputed = computed(() => {
  if (product.value?.images && product.value.images.length > 0) {
    return product.value.images
  }
  return productImages
})

// Получить информацию о товаре по ID (для похожих товаров)
const getProductInfos = (id: number) => {
  // В реальном приложении это должно загружаться из API
  return productInfos.value.filter(info => info.productId === id)
}

// Обработчики
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/800x600?text=Изображение+товара'
}

const changeMainImage = (img: string) => {
  mainImage.value = img
}

const onSizeChange = () => {
  quantity.value = 1
}

const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

const increaseQuantity = () => {
  if (selectedSize.value && quantity.value < selectedSize.value.countItems) {
    quantity.value++
  }
}

const addToCart = async () => {
  if (!canAddToCart.value || !product.value) return

  isAddingToCart.value = true

  try {
    if (availableSizes.value.length > 0 && selectedSize.value) {
      cartStore.addItem(product.value, selectedSize.value, quantity.value)
    } else {
      // Если нет размеров, создаем фиктивную ProductInfo
      const productInfo: ProductInfo = {
        id: product.value.id,
        productId: product.value.id,
        sizeName: 'стандарт',
        countItems: totalStock.value,
        price: product.value.basePrice
      }
      cartStore.addItem(product.value, productInfo, quantity.value)
    }

    // Показываем уведомление
    showNotification.value = true
    setTimeout(() => {
      showNotification.value = false
    }, 3000)
  } catch (err) {
    console.error('Ошибка при добавлении в корзину:', err)
    alert('Не удалось добавить товар в корзину')
  } finally {
    isAddingToCart.value = false
  }
}

const buyNow = () => {
  if (canAddToCart.value && product.value) {
    addToCart().then(() => {
      router.push('/shop/checkout')
    })
  }
}

const addToWishlist = () => {
  isInWishlist.value = !isInWishlist.value
  // В реальном приложении здесь был бы вызов API
  const message = isInWishlist.value
    ? 'Товар добавлен в избранное'
    : 'Товар удален из избранного'
  alert(message)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

onMounted(() => {
  loadProduct()
})

watch(() => route.params.id, () => {
  loadProduct()
})
</script>

<style scoped>
.product-detail {
  padding: 2rem;
}

.main-image {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.product-main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-main-image:hover {
  transform: scale(1.02);
}

.gallery-thumbnail {
  width: 100%;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.gallery-thumbnail:hover {
  opacity: 1;
  transform: translateY(-2px);
}

.gallery-thumbnail.is-active {
  opacity: 1;
  border-color: #667eea;
}

.out-of-stock-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
}

.rating {
  display: flex;
  align-items: center;
  background: #555552;
  padding: 0.5rem 1rem;
  border-radius: 20px;
}

.price-section {
  background: #393636;
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.description-section {
  border-top: 1px solid #e5e7eb;
  padding-top: 1.5rem;
}

.selection-section {
  background: #2c3030;
  padding: 1.5rem;
  border-radius: 12px;
}

.action-buttons .buttons {
  display: flex;
  gap: 1rem;
}

.title {
  color: #c8c8c8;
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

.button.is-outlined {
  border-color: #667eea;
  color: #667eea;
}

.button.is-outlined:hover {
  background-color: #667eea;
  color: white;
}

.fixed-notification {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.related-products {
  border-top: 1px solid #e5e7eb;
  padding-top: 3rem;
}

@media (max-width: 768px) {
  .product-detail {
    padding: 1rem;
  }

  .columns {
    flex-direction: column;
  }

  .product-main-image {
    height: 300px;
  }

  .action-buttons .buttons {
    flex-direction: column;
  }
}
</style>
