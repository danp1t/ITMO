<template>
  <div class="shop-view">
    <!-- Заголовок и фильтры -->
    <div class="shop-header mb-5">
      <div class="level">
        <div class="level-left">
          <div>
            <h1 class="title is-2">Интернет-магазин</h1>
            <p class="subtitle is-5 has-text-grey">Товары для художественной гимнастики</p>
          </div>
        </div>

        <div class="level-right">
          <div class="level-item">
            <!-- Корзина -->
            <div class="cart-indicator" @click="toggleCart">
              <div class="cart-icon">
                <i class="fas fa-shopping-cart fa-lg"></i>
                <span v-if="cartStore.totalItems > 0" class="cart-badge">
                  {{ cartStore.totalItems }}
                </span>
              </div>
              <div class="cart-summary">
                <span class="has-text-weight-bold">{{ cartStore.totalAmount }} ₽</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Панель фильтров -->
      <div class="filters-panel">
        <div class="columns">
          <div class="column is-3">
            <div class="field">
              <label class="label">Категория</label>
              <div class="control">
                <div class="select is-fullwidth">
                  <select v-model="selectedCategory" @change="applyFilters">
                    <option value="">Все категории</option>
                    <option
                      v-for="category in categories"
                      :key="category"
                      :value="category"
                    >
                      {{ category }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div class="column is-3">
            <div class="field">
              <label class="label">Сортировка</label>
              <div class="control">
                <div class="select is-fullwidth">
                  <select v-model="selectedSort" @change="applySorting">
                    <option value="name:asc">По названию (А-Я)</option>
                    <option value="name:desc">По названию (Я-А)</option>
                    <option value="price:asc">По цене (возрастание)</option>
                    <option value="price:desc">По цене (убывание)</option>
                    <option value="popularity:desc">По популярности</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div class="column is-6">
            <div class="field">
              <label class="label">Поиск</label>
              <div class="control has-icons-left">
                <input
                  v-model="searchQuery"
                  class="input"
                  type="text"
                  placeholder="Поиск товаров..."
                  @input="debouncedSearch"
                >
                <span class="icon is-left">
                  <i class="fas fa-search"></i>
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Основной контент -->
    <div class="columns">
      <!-- Каталог товаров -->
      <div class="column is-three-quarters">
        <!-- Загрузка -->
        <div v-if="loading" class="has-text-centered py-6">
          <i class="fas fa-spinner fa-spin fa-2x"></i>
          <p class="mt-3">Загрузка товаров...</p>
        </div>

        <!-- Товары -->
        <div v-else-if="filteredProducts.length > 0" class="products-grid">
          <div class="columns is-multiline">
            <div
              v-for="product in filteredProducts"
              :key="product.id"
              class="column is-4-tablet is-3-desktop"
            >
              <ProductCard
                :product="product"
                :product-infos="getProductInfos(product.id)"
              />
            </div>
          </div>
        </div>

        <!-- Нет товаров -->
        <div v-else class="has-text-centered py-6">
          <div class="empty-state">
            <i class="fas fa-box-open fa-3x has-text-grey-light"></i>
            <p class="title is-4 mt-4">Товары не найдены</p>
            <p class="subtitle is-6 has-text-grey">
              Попробуйте изменить параметры поиска или фильтры
            </p>
            <button class="button is-primary mt-3" @click="resetFilters">
              Сбросить фильтры
            </button>
          </div>
        </div>
      </div>

      <!-- Боковая панель -->
      <div class="column">
        <!-- Фильтры -->
        <div class="box">
          <h3 class="title is-5 mb-4">Фильтры</h3>

          <!-- Цена -->
          <div class="field">
            <label class="label">Цена, ₽</label>
            <div class="price-range">
              <div class="field is-grouped">
                <div class="control is-expanded">
                  <input
                    v-model.number="priceRange.min"
                    class="input"
                    type="number"
                    placeholder="от"
                    min="0"
                  >
                </div>
                <div class="control is-expanded">
                  <input
                    v-model.number="priceRange.max"
                    class="input"
                    type="number"
                    placeholder="до"
                    min="0"
                  >
                </div>
              </div>
              <button
                class="button is-primary is-fullwidth mt-2"
                @click="applyPriceFilter"
              >
                Применить
              </button>
            </div>
          </div>

          <hr class="my-4">

          <!-- Наличие -->
          <div class="field">
            <label class="label">Наличие</label>
            <div class="control">
              <label class="checkbox">
                <input
                  v-model="inStockOnly"
                  type="checkbox"
                  @change="applyFilters"
                >
                <span class="ml-2">Только в наличии</span>
              </label>
            </div>
          </div>

          <hr class="my-4">

          <!-- Сброс фильтров -->
          <button class="button is-light is-fullwidth" @click="resetFilters">
            <span class="icon">
              <i class="fas fa-redo"></i>
            </span>
            <span>Сбросить все фильтры</span>
          </button>
        </div>

        <!-- Категории -->
        <div class="box">
          <h3 class="title is-5 mb-3">Категории</h3>
          <aside class="menu">
            <ul class="menu-list">
              <li v-for="category in categories" :key="category">
                <a
                  :class="{ 'is-active': selectedCategory === category }"
                  @click="selectCategory(category)"
                >
                  {{ category }}
                  <span class="tag is-light is-pulled-right">
                    {{ getCategoryCount(category) }}
                  </span>
                </a>
              </li>
            </ul>
          </aside>
        </div>

        <!-- Статистика -->
        <div class="box">
          <h3 class="title is-5 mb-3">Статистика</h3>
          <div class="content">
            <p>Всего товаров: <strong>{{ filteredProducts.length }}</strong></p>
            <p>В наличии: <strong>{{ availableProductsCount }}</strong></p>
            <p>Средняя цена: <strong>{{ averagePrice }} ₽</strong></p>
          </div>
        </div>
      </div>
    </div>

    <!-- Сайдбар корзины -->
    <CartSidebar
      :is-visible="showCart"
      @close="showCart = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { debounce } from 'lodash'
import { useCartStore } from '../stores/cart'
import ProductCard from '../components/shop/ProductCard.vue'
import CartSidebar from '../components/shop/CartSidebar.vue'
import { shopAPI } from '../api/shop'
import type { Product, ProductInfo } from '../types/shop'

const cartStore = useCartStore()

// Данные
const products = ref<Product[]>([])
const productInfos = ref<ProductInfo[]>([])
const loading = ref(false)

// Фильтры
const selectedCategory = ref('')
const selectedSort = ref('name:asc')
const searchQuery = ref('')
const priceRange = ref({ min: 0, max: 0 })
const inStockOnly = ref(false)
const showCart = ref(false)

// Получение всех уникальных категорий
const categories = computed(() => {
  const allCategories = products.value.map(p => p.category)
  return Array.from(new Set(allCategories))
})

// Получение информации о товаре по ID
const getProductInfos = (productId: number) => {
  return productInfos.value.filter(info => info.productId === productId)
}

// Фильтрация товаров
const filteredProducts = computed(() => {
  let result = [...products.value]

  // Фильтрация по категории
  if (selectedCategory.value) {
    result = result.filter(p => p.category === selectedCategory.value)
  }

  // Фильтрация по поисковому запросу
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(p =>
      p.name.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query) ||
      p.category.toLowerCase().includes(query)
    )
  }

  // Фильтрация по цене
  if (priceRange.value.min > 0 || priceRange.value.max > 0) {
    result = result.filter(p => {
      const minPrice = Math.min(...getProductInfos(p.id).map(info => info.price))
      const maxPrice = Math.max(...getProductInfos(p.id).map(info => info.price))

      if (priceRange.value.min > 0 && minPrice < priceRange.value.min) return false
      if (priceRange.value.max > 0 && maxPrice > priceRange.value.max) return false
      return true
    })
  }

  // Фильтрация по наличию
  if (inStockOnly.value) {
    result = result.filter(p => {
      const infos = getProductInfos(p.id)
      return infos.some(info => info.countItems > 0)
    })
  }

  // Сортировка
  const [sortField, sortDirection] = selectedSort.value.split(':')
  result.sort((a, b) => {
    let aValue: any, bValue: any

    if (sortField === 'price') {
      const aInfos = getProductInfos(a.id)
      const bInfos = getProductInfos(b.id)
      aValue = aInfos.length > 0 ? Math.min(...aInfos.map(i => i.price)) : a.basePrice
      bValue = bInfos.length > 0 ? Math.min(...bInfos.map(i => i.price)) : b.basePrice
    } else {
      aValue = a[sortField as keyof Product]
      bValue = b[sortField as keyof Product]
    }

    if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1
    if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1
    return 0
  })

  return result
})

// Статистика
const availableProductsCount = computed(() => {
  return filteredProducts.value.filter(p => {
    const infos = getProductInfos(p.id)
    return infos.some(info => info.countItems > 0)
  }).length
})

const averagePrice = computed(() => {
  if (filteredProducts.value.length === 0) return 0

  const total = filteredProducts.value.reduce((sum, product) => {
    const infos = getProductInfos(product.id)
    const minPrice = infos.length > 0
      ? Math.min(...infos.map(info => info.price))
      : product.basePrice
    return sum + minPrice
  }, 0)

  return Math.round(total / filteredProducts.value.length)
})

// Количество товаров по категории
const getCategoryCount = (category: string) => {
  return products.value.filter(p => p.category === category).length
}

// Загрузка данных
const loadProducts = async () => {
  loading.value = true

  try {
    const productsResponse = await shopAPI.getProducts()
    products.value = productsResponse.data

    // Загружаем информацию о товарах
    for (const product of products.value) {
      try {
        const infosResponse = await shopAPI.getProductInfoByProduct(product.id)
        productInfos.value.push(...infosResponse.data)
      } catch (error) {
        console.warn(`Не удалось загрузить информацию для товара ${product.id}:`, error)
      }
    }
  } catch (error) {
    console.error('Ошибка при загрузке товаров:', error)
    // В реальном приложении показывать уведомление
  } finally {
    loading.value = false
  }
}

// Применение фильтров
const applyFilters = () => {
  // Реактивные computed свойства уже обработают изменения
}

// Применение сортировки
const applySorting = () => {
  // Реактивные computed свойства уже обработают изменения
}

// Применение фильтра по цене
const applyPriceFilter = () => {
  applyFilters()
}

// Выбор категории
const selectCategory = (category: string) => {
  selectedCategory.value = selectedCategory.value === category ? '' : category
  applyFilters()
}

// Сброс всех фильтров
const resetFilters = () => {
  selectedCategory.value = ''
  selectedSort.value = 'name:asc'
  searchQuery.value = ''
  priceRange.value = { min: 0, max: 0 }
  inStockOnly.value = false
}

// Отложенный поиск
const debouncedSearch = debounce(() => {
  applyFilters()
}, 500)

// Открытие/закрытие корзины
const toggleCart = () => {
  showCart.value = !showCart.value
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.shop-view {
  padding: 1rem;
}

.shop-header {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cart-indicator {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.cart-indicator:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cart-icon {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #ef4444;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
}

.cart-summary {
  font-size: 1.1rem;
}

.filters-panel {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #f1f3f5;
}

.products-grid {
  margin-top: 1rem;
}

.empty-state {
  padding: 3rem 1rem;
}

.box {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.menu-list a {
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  transition: all 0.2s;
}

.menu-list a:hover {
  background-color: #f8f9fa;
}

.menu-list a.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.price-range {
  margin-top: 0.5rem;
}

@media (max-width: 768px) {
  .shop-view {
    padding: 0.5rem;
  }

  .columns {
    flex-direction: column;
  }

  .column.is-three-quarters {
    width: 100%;
  }

  .filters-panel .columns {
    flex-direction: column;
  }
}
</style>
