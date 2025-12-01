<template>
  <div class="product-detail-view">
    <!-- Навигация назад -->
    <div class="mb-4">
      <router-link to="/shop" class="button is-light">
        <span class="icon">
          <i class="fas fa-arrow-left"></i>
        </span>
        <span>Назад в магазин</span>
      </router-link>
    </div>

    <!-- Загрузка -->
    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка товара...</p>
    </div>

    <!-- Ошибка -->
    <div v-else-if="error" class="notification is-danger">
      {{ error }}
      <router-link to="/shop" class="button is-light ml-2">
        Вернуться в магазин
      </router-link>
    </div>

    <!-- Информация о товаре -->
    <div v-else-if="product" class="product-detail">
      <div class="columns">
        <!-- Изображение товара -->
        <div class="column is-half">
          <div class="product-images">
            <div class="main-image">
              <img
                :src="mainImage"
                :alt="product.name"
                class="product-main-image"
                @error="handleImageError"
              >
            </div>

            <!-- Миниатюры -->
            <div v-if="images.length > 1" class="image-thumbnails">
              <div
                v-for="(image, index) in images"
                :key="index"
                class="thumbnail"
                :class="{ 'is-active': index === activeImageIndex }"
                @click="activeImageIndex = index"
              >
                <img :src="image" :alt="`Изображение ${index + 1}`">
              </div>
            </div>
          </div>
        </div>

        <!-- Информация о товаре -->
        <div class="column is-half">
          <!-- Категория -->
          <div class="mb-3">
            <span class="tag is-info is-medium">{{ product.category }}</span>
            <span
              v-if="popularityTag"
              class="tag is-warning is-medium ml-2"
            >
              {{ popularityTag }}
            </span>
          </div>

          <!-- Название -->
          <h1 class="title is-2 mb-3">{{ product.name }}</h1>

          <!-- Цена -->
          <div class="price-section mb-4">
            <div class="level is-mobile">
              <div class="level-left">
                <div>
                  <p class="price has-text-weight-bold is-size-2">
                    {{ selectedPrice }} ₽
                  </p>
                  <p v-if="hasMultiplePrices" class="has-text-grey">
                    Цены от {{ minPrice }} ₽ до {{ maxPrice }} ₽
                  </p>
                </div>
              </div>
              <div class="level-right">
                <div class="rating">
                  <span class="icon has-text-warning">
                    <i class="fas fa-star"></i>
                  </span>
                  <span class="ml-1">4.8</span>
                  <span class="has-text-grey ml-1">(124 отзыва)</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Описание -->
          <div class="description mb-5">
            <h3 class="title is-5 mb-2">Описание</h3>
            <p class="content">{{ product.description }}</p>
          </div>

          <!-- Размеры -->
          <div v-if="availableSizes.length > 0" class="sizes-section mb-5">
            <h3 class="title is-5 mb-3">Доступные размеры</h3>
            <div class="size-options">
              <div
                v-for="size in availableSizes"
                :key="size.id"
                class="size-option"
                :class="{ 'is-selected': selectedSize?.id === size.id }"
                @click="selectSize(size)"
              >
                <div class="size-name">{{ size.sizeName }}</div>
                <div class="size-price">{{ size.price }} ₽</div>
                <div class="size-stock">
                  <span
                    class="tag"
                    :class="size.countItems > 0 ? 'is-success' : 'is-danger'"
                  >
                    {{ size.countItems > 0 ? `${size.countItems} шт.` : 'Нет в наличии' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Количество -->
          <div class="quantity-section mb-5">
            <h3 class="title is-5 mb-2">Количество</h3>
            <div class="field has-addons">
              <div class="control">
                <button
                  class="button"
                  :disabled="quantity <= 1"
                  @click="decreaseQuantity"
                >
                  <span class="icon">
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
                  :max="maxQuantity"
                  style="width: 80px; text-align: center"
                >
              </div>
              <div class="control">
                <button
                  class="button"
                  :disabled="quantity >= maxQuantity"
                  @click="increaseQuantity"
                >
                  <span class="icon">
                    <i class="fas fa-plus"></i>
                  </span>
                </button>
              </div>
              <div class="control">
                <span class="help">
                  Макс. {{ maxQuantity }} шт.
                </span>
              </div>
            </div>
          </div>

          <!-- Кнопки действий -->
          <div class="action-buttons mb-6">
            <div class="buttons">
              <button
                class="button is-primary is-large"
                :disabled="!canAddToCart || addingToCart"
                @click="addToCart"
              >
                <span class="icon" v-if="addingToCart">
                  <i class="fas fa-spinner fa-spin"></i>
                </span>
                <span class="icon" v-else>
                  <i class="fas fa-shopping-cart"></i>
                </span>
                <span>{{ addToCartText }}</span>
              </button>

              <button
                class="button is-light is-large"
                @click="toggleWishlist"
              >
                <span class="icon">
                  <i :class="wishlistIcon"></i>
                </span>
                <span>{{ wishlistText }}</span>
              </button>
            </div>
          </div>

          <!-- Информация о доставке -->
          <div class="delivery-info mb-4">
            <div class="message is-info">
              <div class="message-body">
                <div class="level is-mobile">
                  <div class="level-left">
                    <div>
                      <p class="has-text-weight-semibold">Бесплатная доставка</p>
                      <p class="is-size-7">При заказе от 5000 ₽</p>
                    </div>
                  </div>
                  <div class="level-right">
                    <span class="tag is-info">2-3 дня</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Дополнительная информация -->
      <div class="additional-info mt-6">
        <!-- Табы -->
        <div class="tabs is-boxed">
          <ul>
            <li :class="{ 'is-active': activeTab === 'details' }">
              <a @click="activeTab = 'details'">
                <span class="icon is-small">
                  <i class="fas fa-info-circle"></i>
                </span>
                <span>Характеристики</span>
              </a>
            </li>
            <li :class="{ 'is-active': activeTab === 'reviews' }">
              <a @click="activeTab = 'reviews'">
                <span class="icon is-small">
                  <i class="fas fa-star"></i>
                </span>
                <span>Отзывы (24)</span>
              </a>
            </li>
            <li :class="{ 'is-active': activeTab === 'questions' }">
              <a @click="activeTab = 'questions'">
                <span class="icon is-small">
                  <i class="fas fa-question-circle"></i>
                </span>
                <span>Вопросы и ответы</span>
              </a>
            </li>
          </ul>
        </div>

        <!-- Содержимое табов -->
        <div class="tab-content">
          <!-- Характеристики -->
          <div v-if="activeTab === 'details'" class="box">
            <h3 class="title is-5 mb-3">Характеристики товара</h3>
            <div class="content">
              <table class="table is-fullwidth">
                <tbody>
                <tr>
                  <th>Категория</th>
                  <td>{{ product.category }}</td>
                </tr>
                <tr>
                  <th>Материал</th>
                  <td>Высококачественный эластан, кристаллы Swarovski</td>
                </tr>
                <tr>
                  <th>Производство</th>
                  <td>Россия</td>
                </tr>
                <tr>
                  <th>Возрастная группа</th>
                  <td>Дети и взрослые</td>
                </tr>
                <tr>
                  <th>Уровень</th>
                  <td>Профессиональный</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Отзывы -->
          <div v-else-if="activeTab === 'reviews'" class="box">
            <h3 class="title is-5 mb-3">Отзывы покупателей</h3>
            <div class="reviews">
              <div class="review">
                <div class="review-header">
                  <div class="review-author">
                    <strong>Анна Иванова</strong>
                  </div>
                  <div class="review-rating">
                    <span class="icon has-text-warning">
                      <i class="fas fa-star"></i>
                    </span>
                    <span class="icon has-text-warning">
                      <i class="fas fa-star"></i>
                    </span>
                    <span class="icon has-text-warning">
                      <i class="fas fa-star"></i>
                    </span>
                    <span class="icon has-text-warning">
                      <i class="fas fa-star"></i>
                    </span>
                    <span class="icon has-text-warning">
                      <i class="fas fa-star"></i>
                    </span>
                  </div>
                </div>
                <div class="review-content">
                  <p>Отличный товар! Качество на высшем уровне. Дочь занимается гимнастикой и очень довольна.</p>
                </div>
                <div class="review-date">
                  <small class="has-text-grey">2 недели назад</small>
                </div>
              </div>
            </div>
          </div>

          <!-- Вопросы и ответы -->
          <div v-else class="box">
            <h3 class="title is-5 mb-3">Вопросы и ответы</h3>
            <div class="content">
              <div class="faq-item">
                <p class="has-text-weight-semibold">Как узнать свой размер?</p>
                <p>Вы можете воспользоваться нашей таблицей размеров или обратиться к консультанту.</p>
              </div>
              <div class="faq-item">
                <p class="has-text-weight-semibold">Есть ли гарантия на товар?</p>
                <p>Да, на все товары предоставляется гарантия 6 месяцев.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../../stores/cart'
import { shopAPI } from '../../api/shop'
import type { Product, ProductInfo } from '../../types/shop'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref<Product | null>(null)
const productInfos = ref<ProductInfo[]>([])
const loading = ref(false)
const error = ref('')

// Состояние UI
const activeImageIndex = ref(0)
const selectedSize = ref<ProductInfo | null>(null)
const quantity = ref(1)
const addingToCart = ref(false)
const activeTab = ref('details')
const inWishlist = ref(false)

// Изображения товара (в реальном приложении брать из API)
const images = [
  'https://images.unsplash.com/photo-1595435934247-5d33b7f92c70?w=800&h=600&fit=crop',
  'https://images.unsplash.com/photo-1581094794329-c8112a89af12?w=800&h=600&fit=crop',
  'https://images.unsplash.com/photo-1594736797933-d0e49d051b43?w=800&h=600&fit=crop'
]

// Загрузка данных о товаре
const loadProduct = async () => {
  loading.value = true
  error.value = ''

  const productId = parseInt(route.params.id as string)

  try {
    // Загружаем информацию о товаре
    const productResponse = await shopAPI.getProductDetail(productId)
    product.value = productResponse.data

    // Загружаем информацию о размерах/вариантах
    const infosResponse = await shopAPI.getProductInfoByProduct(productId)
    productInfos.value = infosResponse.data

    // Выбираем первый доступный размер по умолчанию
    if (availableSizes.value.length > 0) {
      selectedSize.value = availableSizes.value[0]
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Не удалось загрузить информацию о товаре'
    console.error('Ошибка при загрузке товара:', err)
  } finally {
    loading.value = false
  }
}

// Обработчик ошибки загрузки изображения
const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/800x600?text=Изображение+товара'
}

// Основное изображение
const mainImage = computed(() => {
  return images[activeImageIndex.value]
})

// Доступные размеры
const availableSizes = computed(() => {
  return productInfos.value.filter(info => info.countItems > 0)
})

// Цены
const minPrice = computed(() => {
  if (availableSizes.value.length === 0) return product.value?.basePrice || 0
  return Math.min(...availableSizes.value.map(info => info.price))
})

const maxPrice = computed(() => {
  if (availableSizes.value.length === 0) return product.value?.basePrice || 0
  return Math.max(...availableSizes.value.map(info => info.price))
})

const selectedPrice = computed(() => {
  if (selectedSize.value) {
    return selectedSize.value.price
  }
  return minPrice.value
})

const hasMultiplePrices = computed(() => {
  if (availableSizes.value.length <= 1) return false
  return minPrice.value !== maxPrice.value
})

// Максимальное количество для заказа
const maxQuantity = computed(() => {
  if (!selectedSize.value) return 0
  return selectedSize.value.countItems
})

// Можно ли добавить в корзину
const canAddToCart = computed(() => {
  if (!product.value) return false
  if (availableSizes.value.length > 0 && !selectedSize.value) return false
  if (selectedSize.value && selectedSize.value.countItems === 0) return false
  return quantity.value > 0 && quantity.value <= maxQuantity.value
})

// Текст для кнопки добавления в корзину
const addToCartText = computed(() => {
  if (!canAddToCart.value) return 'Недоступно'
  return `Добавить в корзину — ${selectedPrice.value * quantity.value} ₽`
})

// Тег популярности
const popularityTag = computed(() => {
  if (!product.value) return ''
  if (product.value.popularity > 50) return 'Популярный'
  if (product.value.popularity > 20) return 'Хит продаж'
  return ''
})

// Изменение количества
const decreaseQuantity = () => {
  if (quantity.value > 1) quantity.value--
}

const increaseQuantity = () => {
  if (quantity.value < maxQuantity.value) quantity.value++
}

// Выбор размера
const selectSize = (size: ProductInfo) => {
  if (size.countItems > 0) {
    selectedSize.value = size
    quantity.value = Math.min(quantity.value, size.countItems)
  }
}

// Добавление в корзину
const addToCart = async () => {
  if (!product.value || !canAddToCart.value) return

  addingToCart.value = true

  try {
    if (selectedSize.value) {
      cartStore.addItem(product.value, selectedSize.value, quantity.value)
    } else {
      // Если нет размеров, создаем фиктивную ProductInfo
      const productInfo: ProductInfo = {
        id: product.value.id,
        productId: product.value.id,
        sizeName: 'стандарт',
        countItems: 1,
        price: product.value.basePrice
      }
      cartStore.addItem(product.value, productInfo, quantity.value)
    }

    alert('Товар добавлен в корзину!')
  } catch (err) {
    console.error('Ошибка при добавлении в корзину:', err)
    alert('Не удалось добавить товар в корзину')
  } finally {
    addingToCart.value = false
  }
}

// Избранное
const wishlistIcon = computed(() => {
  return inWishlist.value ? 'fas fa-heart has-text-danger' : 'far fa-heart'
})

const wishlistText = computed(() => {
  return inWishlist.value ? 'В избранном' : 'В избранное'
})

const toggleWishlist = () => {
  inWishlist.value = !inWishlist.value
  alert(inWishlist.value ? 'Добавлено в избранное' : 'Удалено из избранного')
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped>
.product-detail-view {
  padding: 2rem 1rem;
}

/* Изображения */
.product-images {
  border-radius: 12px;
  overflow: hidden;
}

.main-image {
  margin-bottom: 1rem;
}

.product-main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.product-main-image:hover {
  transform: scale(1.02);
}

.image-thumbnails {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding: 0.5rem 0;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  opacity: 0.7;
  transition: all 0.3s;
  flex-shrink: 0;
}

.thumbnail:hover {
  opacity: 0.9;
}

.thumbnail.is-active {
  opacity: 1;
  border: 2px solid #667eea;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Цена */
.price-section {
  padding: 1rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.price {
  color: #2d3748;
}

.rating {
  display: flex;
  align-items: center;
}

/* Размеры */
.size-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
}

.size-option {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.size-option:hover {
  border-color: #667eea;
  transform: translateY(-2px);
}

.size-option.is-selected {
  border-color: #667eea;
  background-color: rgba(102, 126, 234, 0.05);
}

.size-name {
  font-weight: 600;
  margin-bottom: 0.5rem;
}

.size-price {
  font-weight: 500;
  color: #667eea;
  margin-bottom: 0.5rem;
}

.size-stock .tag {
  font-size: 0.75rem;
}

/* Количество */
.quantity-section .field {
  align-items: center;
}

/* Кнопки действий */
.action-buttons .buttons {
  display: flex;
  gap: 1rem;
}

.button.is-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 600;
  flex: 2;
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
  flex: 1;
}

/* Информация о доставке */
.message {
  border-radius: 8px;
}

/* Табы */
.tabs {
  margin-bottom: 1rem;
}

.tab-content {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.box {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

/* Таблица характеристик */
.table th {
  width: 200px;
  background-color: #f8f9fa;
  font-weight: 600;
}

/* Отзывы */
.review {
  padding: 1rem 0;
  border-bottom: 1px solid #f1f3f5;
}

.review:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.review-rating {
  display: flex;
  gap: 0.1rem;
}

.review-date {
  margin-top: 0.5rem;
}

/* FAQ */
.faq-item {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f1f3f5;
}

.faq-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

@media (max-width: 768px) {
  .product-detail-view {
    padding: 1rem;
  }

  .columns {
    flex-direction: column;
  }

  .product-main-image {
    height: 300px;
  }

  .size-options {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .action-buttons .buttons {
    flex-direction: column;
  }

  .button.is-primary,
  .button.is-light {
    width: 100%;
  }
}
</style>
