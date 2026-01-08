<template>
  <div class="admin-orders-view dark-theme">
    <div class="content-header">
      <h1 class="title">Управление заказами</h1>
      <div class="content-actions">
        <div class="field has-addons">
          <div class="control">
            <input
              v-model="searchQuery"
              class="input"
              type="text"
              placeholder="Поиск по ID, телефону или имени"
              @input="debouncedSearch"
            >
          </div>
        </div>
        <button class="button is-primary" @click="refreshOrders">
          <i class="fas fa-sync-alt mr-2"></i>
          Обновить
        </button>
      </div>
    </div>

    <!-- Фильтры -->
    <div class="filters box mb-4">
      <div class="columns is-multiline">
        <div class="column is-3">
          <div class="field">
            <label class="label">Статус заказа</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="filters.statusId">
                  <option value="">Все статусы</option>
                  <option v-for="status in orderStatuses" :key="status.id" :value="status.id">
                    {{ status.name }}
                  </option>
                </select>
              </div>
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">Дата с</label>
            <div class="control">
              <input v-model="filters.dateFrom" type="date" class="input">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">Дата по</label>
            <div class="control">
              <input v-model="filters.dateTo" type="date" class="input">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">Минимальная сумма</label>
            <div class="control">
              <input v-model="filters.minAmount" type="number" class="input" placeholder="0">
            </div>
          </div>
        </div>
      </div>
      <div class="has-text-right">
        <button class="button is-light" @click="clearFilters">
          Сбросить фильтры
        </button>
        <button class="button is-primary ml-2" @click="applyFilters">
          Применить
        </button>
      </div>
    </div>

    <!-- Загрузка -->
    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка заказов...</p>
    </div>

    <!-- Таблица заказов -->
    <div v-else class="box">
      <div class="table-container">
        <table class="table is-fullwidth is-striped is-hoverable">
          <thead>
          <tr>
            <th>ID</th>
            <th>Пользователь</th>
            <th>Телефон</th>
            <th>Адрес</th>
            <th>Статус</th>
            <th>Сумма</th>
            <th>Дата</th>
            <th>Действия</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td>
              <router-link :to="`/admin/orders/${order.id}`" class="has-text-weight-semibold">
                #{{ order.id }}
              </router-link>
            </td>
            <td>
              <div v-if="order.accountName">
                {{ order.accountName }}
                <span class="tag is-dark is-small ml-1">ID: {{ order.accountId }}</span>
              </div>
              <div v-else class="has-text-grey">
                Анонимный пользователь
              </div>
            </td>
            <td>{{ order.phone }}</td>
            <td>
              <div class="address-cell">
                {{ truncateAddress(order.address, 30) }}
              </div>
            </td>
            <td>
               <span class="tag" :class="getStatusClass(order.orderStatusId)">
                   {{ getStatusName(order.orderStatusId) }}
               </span>
            </td>
            <td>
              <span class="has-text-weight-semibold">{{ order.totalAmount }} ₽</span>
            </td>
            <td>
              <div class="has-text-grey is-size-7">
                {{ formatDate(order.createdAt) }}
              </div>
            </td>
            <td>
              <div class="buttons are-small">
                <router-link
                  :to="`/admin/orders/${order.id}`"
                  class="button is-info is-light"
                  title="Просмотр деталей"
                >
                  <i class="fas fa-eye"></i>
                </router-link>
                <button
                  v-if="authStore.canManageRoles && order.orderStatusId !== 5"
                  class="button is-danger is-light"
                  @click="cancelOrder(order)"
                  title="Отменить заказ"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Пагинация -->
      <nav v-if="totalPages > 1" class="pagination is-centered mt-4" role="navigation">
        <ul class="pagination-list">
          <li v-for="page in totalPages" :key="page">
            <button
              class="pagination-link"
              :class="{ 'is-current': currentPage === page }"
              @click="goToPage(page)"
            >
              {{ page }}
            </button>
          </li>
        </ul>
      </nav>

      <!-- Нет заказов -->
      <div v-if="orders.length === 0" class="has-text-centered py-6">
        <i class="fas fa-box-open fa-3x has-text-grey-light"></i>
        <p class="title is-4 mt-4">Заказы не найдены</p>
        <p class="subtitle is-6 has-text-grey">
          Попробуйте изменить параметры фильтрации
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { shopAPI } from '@/api/shop'
import type { Order, OrderStatus } from '@/types/shop'

const authStore = useAuthStore()
const router = useRouter()

// Данные
const orders = ref<Order[]>([])
const orderStatuses = ref<OrderStatus[]>([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 20

// Фильтры
const filters = reactive({
  statusId: '',
  dateFrom: '',
  dateTo: '',
  minAmount: ''
})

// Загрузка данных
const loadOrders = async () => {
  loading.value = true
  try {
    // Загружаем все заказы (нужно добавить метод в API)
    const ordersResponse = await shopAPI.getAllOrders()
    orders.value = ordersResponse.data

    // Загружаем статусы
    const statusesResponse = await shopAPI.getOrderStatuses()
    orderStatuses.value = statusesResponse.data
  } catch (error) {
    console.error('Ошибка при загрузке заказов:', error)
  } finally {
    loading.value = false
  }
}

const getStatusClass = (statusId: number): string => {
  switch (statusId) {
    case 1: return 'is-info' // Новый
    case 2: return 'is-warning' // В обработке
    case 3: return 'is-primary' // Отправлен
    case 4: return 'is-success' // Доставлен
    case 5: return 'is-danger' // Отменен
    default: return 'is-light'
  }
}

const getStatusName = (statusId: number): string => {
  const status = orderStatuses.value.find(s => s.id === statusId)
  return status ? status.name : `Статус #${statusId}`
}

// Отфильтрованные заказы
const filteredOrders = computed(() => {
  let result = [...orders.value]

  // Поиск по запросу
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(order =>
      order.id.toString().includes(query) ||
      order.phone?.toLowerCase().includes(query) ||
      order.accountName?.toLowerCase().includes(query) ||
      order.address?.toLowerCase().includes(query)
    )
  }

  // Фильтрация по статусу
  if (filters.statusId) {
    result = result.filter(order => order.orderStatusId.toString() === filters.statusId)
  }

  // Фильтрация по дате
  if (filters.dateFrom) {
    const fromDate = new Date(filters.dateFrom)
    result = result.filter(order => new Date(order.createdAt) >= fromDate)
  }

  if (filters.dateTo) {
    const toDate = new Date(filters.dateTo)
    result = result.filter(order => new Date(order.createdAt) <= toDate)
  }

  // Фильтрация по сумме
  if (filters.minAmount) {
    const minAmount = parseFloat(filters.minAmount)
    result = result.filter(order => order.totalAmount >= minAmount)
  }

  // Пагинация
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return result.slice(start, end)
})

// Общее количество страниц
const totalPages = computed(() => {
  const totalItems = orders.value.length
  return Math.ceil(totalItems / itemsPerPage)
})

// Обновление статуса заказа
const updateOrderStatus = async (orderId: number, statusId: string) => {
  try {
    await shopAPI.updateOrder(orderId, { orderStatusId: parseInt(statusId) })

    // Обновляем локально
    const order = orders.value.find(o => o.id === orderId)
    if (order) {
      order.orderStatusId = parseInt(statusId)
    }
  } catch (error) {
    console.error('Ошибка при обновлении статуса:', error)
    alert('Не удалось обновить статус заказа')
  }
}

// Отмена заказа
const cancelOrder = async (order: Order) => {
  if (!confirm(`Вы уверены, что хотите отменить заказ #${order.id}?`)) {
    return
  }

  try {
    await shopAPI.updateOrder(order.id, { orderStatusId: 5 }) // 5 = Отменен
    order.orderStatusId = 5
  } catch (error) {
    console.error('Ошибка при отмене заказа:', error)
    alert('Не удалось отменить заказ')
  }
}

// Форматирование даты
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

// Укорачивание адреса
const truncateAddress = (address: string, maxLength: number) => {
  if (!address) return ''
  if (address.length <= maxLength) return address
  return address.substring(0, maxLength) + '...'
}

// Поиск с задержкой
let searchTimeout: number
const debouncedSearch = () => {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 1
  }, 300)
}

// Применение фильтров
const applyFilters = () => {
  currentPage.value = 1
}

// Сброс фильтров
const clearFilters = () => {
  filters.statusId = ''
  filters.dateFrom = ''
  filters.dateTo = ''
  filters.minAmount = ''
  searchQuery.value = ''
  currentPage.value = 1
}

// Обновление списка заказов
const refreshOrders = () => {
  loadOrders()
}

// Переход на страницу
const goToPage = (page: number) => {
  currentPage.value = page
}

// Инициализация
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders-view {
  padding: 20px;
}

.table th {
  background-color: #1a1a1a;
  color: #fff;
  border-bottom: 2px solid #333;
}

.table td {
  vertical-align: middle;
  border-bottom: 1px solid #333;
}

.address-cell {
  max-width: 200px;
  word-break: break-word;
}

.select select {
  background-color: #2d2d2d;
  border-color: #444;
  color: #fff;
}

.select select:disabled {
  background-color: #3d3d3d;
  color: #888;
}

.pagination-link {
  background-color: #2d2d2d;
  border-color: #444;
  color: #fff;
}

.pagination-link.is-current {
  background-color: #4a00e0;
  border-color: #4a00e0;
  color: #fff;
}

.pagination-link:hover {
  background-color: #3d3d3d;
  border-color: #555;
  color: #fff;
}
</style>
