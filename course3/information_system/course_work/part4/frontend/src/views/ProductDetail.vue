<template>
  <div class="product-detail">
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

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
      <div class="column is-half">
        <div class="main-image mb-4">
          <figure class="image is-4by3">
            <img
              :src="mainImageUrl"
              :alt="product.name"
              class="product-main-image"
              @error="handleImageError"
            >
            <div v-if="isOutOfStock" class="out-of-stock-overlay">
              <span class="tag is-danger is-medium">Нет в наличии</span>
            </div>
          </figure>
        </div>

        <div v-if="productImages.length > 1" class="image-gallery">
          <div class="columns is-multiline is-mobile">
            <div
              v-for="(img, index) in productImages"
              :key="index"
              class="column is-3"
              @click="changeMainImage(index)"
            >
              <figure class="image is-square">
                <img
                  :src="img"
                  :alt="`${product.name} - изображение ${index + 1}`"
                  :class="{'is-active': index === activeImageIndex}"
                  class="gallery-thumbnail"
                  @error="handleImageError"
                >
              </figure>
            </div>
          </div>
        </div>
      </div>

      <div class="column is-half">
        <div class="tags has-addons mb-3">
          <span class="tag is-dark">{{ product.category }}</span>
          <span v-if="totalStock > 0" class="tag is-success">
            В наличии
          </span>
          <span v-else class="tag is-danger">
            Нет в наличии
          </span>
        </div>

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

        <div class="price-section mb-5">
          <div class="level is-mobile">
            <div class="level-left">
              <div>
                <p class="title is-2">
                  {{ product.basePrice }} ₽
                  <span v-if="hasMultipleSizes && hasPriceRange" class="subtitle is-3 ml-2">
                    (от {{ minPrice }} ₽ за размер)
                  </span>
                </p>
                <p v-if="hasMultipleSizes" class="has-text-grey">
                  Цены на размеры: от {{ minPrice }} до {{ maxPrice }} ₽
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="description-section mb-5">
          <h3 class="title is-5 mb-3">Описание</h3>
          <p class="content">{{ product.description }}</p>
        </div>

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
                    @change="validateQuantity"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '../stores/cart'
import ProductCard from '../components/shop/ProductCard.vue'
import AppNotification from '../components/AppNotification.vue'
import { shopAPI } from '../api/shop'
import type { Product, ProductInfo } from '../types/shop'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// Уведомление
const notification = reactive({
  visible: false,
  message: '',
  type: 'info' as 'info' | 'success' | 'warning' | 'error'
})

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  notification.message = message
  notification.type = type
  notification.visible = true

  // Автоматическое скрытие через 5 секунд
  setTimeout(() => {
    hideNotification()
  }, 5000)
}

const hideNotification = () => {
  notification.visible = false
}

// Данные
const product = ref<Product | null>(null)
const productInfos = ref<ProductInfo[]>([])
const loading = ref(true)
const error = ref(false)

const activeImageIndex = ref(0)
const selectedSize = ref<ProductInfo | null>(null)
const quantity = ref(1)
const isAddingToCart = ref(false)

const relatedProductsInfos = ref<Map<number, ProductInfo[]>>(new Map())
const relatedProducts = ref<Product[]>([])

const productId = computed(() => {
  const id = route.params.id
  return typeof id === 'string' ? parseInt(id) : Array.isArray(id) ? parseInt(id[0]) : id
})

const mainImageUrl = computed(() => {
  if (productImages.value.length > 0 && activeImageIndex.value < productImages.value.length) {
    return productImages.value[activeImageIndex.value]
  }
  return 'https://placehold.co/800x600?text=%D0%9D%D0%B5%D1%82+%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F'
})

const productImages = computed(() => {
  if (product.value?.images && product.value.images.length > 0) {
    return product.value.images.map(img => {
      if (img.startsWith('http') || img.startsWith('/api/products/images/')) {
        return img
      }
      return `/api/products/images/${img}`
    })
  }
  return ['https://placehold.co/800x600?text=%D0%9D%D0%B5%D1%82+%D0%B8%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B8']
})

const totalStock = computed(() => {
  return productInfos.value.reduce((total, info) => total + info.countItems, 0)
})

const availableSizes = computed(() => {
  return productInfos.value.filter(info => info.countItems > 0)
})

const sortedProductInfos = computed(() => {
  return [...productInfos.value].sort((a, b) => {
    if (a.countItems === 0 && b.countItems > 0) return 1
    if (a.countItems > 0 && b.countItems === 0) return -1
    return a.sizeName.localeCompare(b.sizeName)
  })
})

const hasMultipleSizes = computed(() => {
  return productInfos.value.length > 1
})

const minPrice = computed(() => {
  if (productInfos.value.length === 0) {
    return product.value?.basePrice || 0
  }
  const prices = productInfos.value.map(info => info.price)
  return Math.min(...prices)
})

const maxPrice = computed(() => {
  if (productInfos.value.length === 0) {
    return product.value?.basePrice || 0
  }
  const prices = productInfos.value.map(info => info.price)
  return Math.max(...prices)
})

const hasMultiplePrices = computed(() => {
  if (productInfos.value.length <= 1) return false
  const prices = productInfos.value.map(info => info.price)
  return new Set(prices).size > 1
})

const hasPriceRange = computed(() => {
  if (!product.value || productInfos.value.length === 0) return false
  return hasMultiplePrices.value && minPrice.value < product.value.basePrice
})

const isOutOfStock = computed(() => totalStock.value === 0)

const canAddToCart = computed(() => {
  if (isOutOfStock.value) return false
  if (availableSizes.value.length > 0) {
    return selectedSize.value !== null && quantity.value > 0
  }
  return quantity.value > 0
})

const addToCartText = computed(() => {
  if (isOutOfStock.value) return 'Нет в наличии'
  if (availableSizes.value.length > 0 && !selectedSize.value) {
    return 'Выберите размер'
  }

  // Используем цену выбранного размера или базовую цену
  const price = selectedSize.value ? selectedSize.value.price : product.value?.basePrice || 0
  return `Добавить в корзину (${price * quantity.value} ₽)`
})

const loadProduct = async () => {
  loading.value = true
  error.value = false

  try {
    const productResponse = await shopAPI.getProductById(productId.value as number)
    product.value = productResponse.data

    const infosResponse = await shopAPI.getProductInfoByProduct(productId.value as number)
    productInfos.value = infosResponse.data

    if (availableSizes.value.length > 0) {
      selectedSize.value = availableSizes.value[0]
    }

    await loadRelatedProducts()
  } catch (err) {
    console.error('Ошибка при загрузке товара:', err)
    error.value = true
    showNotification('Не удалось загрузить информацию о товаре', 'error')
  } finally {
    loading.value = false
  }
}

const loadRelatedProducts = async () => {
  try {
    const response = await shopAPI.getProducts()
    if (product.value) {
      // Фильтруем товары по категории
      const filteredProducts = response.data
        .filter(p => p.category === product.value!.category && p.id !== productId.value)
        .slice(0, 4)

      // Сохраняем товары
      relatedProducts.value = filteredProducts

      // Загружаем productInfos для каждого товара
      await loadProductInfosForRelatedProducts(filteredProducts)
    }
  } catch (err) {
    console.error('Ошибка при загрузке похожих товаров:', err)
    showNotification('Не удалось загрузить похожие товары', 'error')
  }
}

const loadProductInfosForRelatedProducts = async (products: Product[]) => {
  // Очищаем предыдущие данные
  relatedProductsInfos.value.clear()

  // Для каждого товара загружаем его productInfos
  for (const product of products) {
    try {
      const infosResponse = await shopAPI.getProductInfoByProduct(product.id)
      relatedProductsInfos.value.set(product.id, infosResponse.data)
    } catch (err) {
      console.error(`Ошибка при загрузке информации для товара ${product.id}:`, err)
      relatedProductsInfos.value.set(product.id, [])
    }
  }
}

const getProductInfos = (productId: number): ProductInfo[] => {
  return relatedProductsInfos.value.get(productId) || []
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'https://placehold.co/800x600?text=%D0%98%D0%B7%D0%BE%D0%B1%D1%80%D0%B0%D0%B6%D0%B5%D0%BD%D0%B8%D0%B5+%D0%BD%D0%B5+%D0%B7%D0%B0%D0%B3%D1%80%D1%83%D0%B6%D0%B5%D0%BD%D0%BE'
}

const changeMainImage = (index: number) => {
  activeImageIndex.value = index
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

  if (selectedSize.value && quantity.value > selectedSize.value.countItems) {
    showNotification(`Нельзя добавить больше ${selectedSize.value.countItems} шт.`, 'error')
    quantity.value = selectedSize.value.countItems
    return
  }

  isAddingToCart.value = true

  try {
    if (availableSizes.value.length > 0 && selectedSize.value) {
      cartStore.addItem(product.value, selectedSize.value, quantity.value)
    } else {
      const productInfo: ProductInfo = {
        id: product.value.id,
        productId: product.value.id,
        sizeName: 'стандарт',
        countItems: totalStock.value,
        price: product.value.basePrice
      }
      cartStore.addItem(product.value, productInfo, quantity.value)
    }

    showNotification('Товар успешно добавлен в корзину!', 'success')
  } catch (err) {
    console.error('Ошибка при добавлении в корзину:', err)
    showNotification('Не удалось добавить товар в корзину', 'error')
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

const validateQuantity = () => {
  if (selectedSize.value && quantity.value > selectedSize.value.countItems) {
    quantity.value = selectedSize.value.countItems
    showNotification(`Максимальное количество: ${selectedSize.value.countItems}`, 'warning')
  }
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
