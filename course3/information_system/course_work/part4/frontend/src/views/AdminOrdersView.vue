<template>
  <div class="admin-orders-view dark-theme">
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

    <div class="content-header">
      <h1 class="title">Управление заказами</h1>
      <div class="content-actions">
        <button class="button is-primary" @click="refreshOrders">
          <i class="fas fa-sync-alt mr-2"></i>
          Обновить
        </button>
      </div>
    </div>

    <div class="search-filters box mb-4">
      <h3 class="title is-5 mb-3">Расширенный поиск</h3>
      <div class="columns is-multiline">
        <div class="column is-3">
          <div class="field">
            <label class="label">По ID заказа</label>
            <div class="control">
              <input v-model="search.orderId" type="number" class="input" placeholder="Введите ID заказа" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">По телефону</label>
            <div class="control">
              <input v-model="search.phone" type="text" class="input" placeholder="+7 (XXX) XXX-XX-XX" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">По ID клиента</label>
            <div class="control">
              <input v-model="search.accountId" type="number" class="input" placeholder="ID клиента" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">По адресу</label>
            <div class="control">
              <input v-model="search.address" type="text" class="input" placeholder="Часть адреса" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">По имени клиента</label>
            <div class="control">
              <input v-model="search.customerName" type="text" class="input" placeholder="Имя клиента" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">По email</label>
            <div class="control">
              <input v-model="search.email" type="email" class="input" placeholder="Email клиента" @input="applyFilters">
            </div>
          </div>
        </div>
        <div class="column is-3">
          <div class="field">
            <label class="label">Статус заказа</label>
            <div class="control">
              <div class="select is-fullwidth">
                <select v-model="filters.statusId" @change="applyFilters">
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
            <label class="label">Сумма от</label>
            <div class="control">
              <input v-model="filters.minAmount" type="number" class="input" placeholder="0" @input="applyFilters">
            </div>
          </div>
        </div>
      </div>
      <div class="columns">
        <div class="column is-6">
          <div class="field">
            <label class="label">Дата с</label>
            <div class="control">
              <input
                v-model="filters.dateFrom"
                type="date"
                class="input"
                :max="filters.dateTo"
                @change="validateDates"
              >
            </div>
          </div>
        </div>
        <div class="column is-6">
          <div class="field">
            <label class="label">Дата по</label>
            <div class="control">
              <input
                v-model="filters.dateTo"
                type="date"
                class="input"
                :min="filters.dateFrom"
                @change="validateDates"
              >
            </div>
          </div>
        </div>
      </div>
      <div v-if="dateError" class="notification is-danger is-light mt-3">
        <button class="delete" @click="dateError = ''"></button>
        {{ dateError }}
      </div>
      <div class="has-text-right mt-4">
        <button class="button is-light" @click="clearFilters">
          <i class="fas fa-times mr-2"></i>
          Сбросить все фильтры
        </button>
      </div>
    </div>

    <div class="stats box mb-4">
      <div class="columns is-mobile is-multiline">
        <div class="column">
          <div class="stat-card">
            <div class="stat-value">{{ totalOrders }}</div>
            <div class="stat-label">Всего заказов</div>
          </div>
        </div>
        <div class="column">
          <div class="stat-card">
            <div class="stat-value">{{ totalRevenue }} ₽</div>
            <div class="stat-label">Общая выручка</div>
          </div>
        </div>
        <div class="column">
          <div class="stat-card">
            <div class="stat-value">{{ pendingOrdersCount }}</div>
            <div class="stat-label">Ожидают обработки</div>
          </div>
        </div>
        <div class="column">
          <div class="stat-card">
            <div class="stat-value">{{ deliveredOrdersCount }}</div>
            <div class="stat-label">Доставлено</div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка заказов...</p>
    </div>

    <div v-else class="box">
      <div class="table-header mb-3">
        <div class="level">
          <div class="level-left">
            <div class="level-item">
              <p class="has-text-grey">
                Найдено {{ filteredOrders.length }} заказов
                <span v-if="filteredOrders.length < orders.length">из {{ orders.length }}</span>
              </p>
            </div>
          </div>
          <div class="level-right">
            <div class="level-item">
              <div class="field">
                <div class="control">
                  <div class="select is-small">
                    <select v-model="itemsPerPage" @change="applyFilters">
                      <option :value="10">10 на странице</option>
                      <option :value="20">20 на странице</option>
                      <option :value="50">50 на странице</option>
                      <option :value="100">100 на странице</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

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
          <tr v-for="order in paginatedOrders" :key="order.id">
            <td>
              <router-link :to="`/admin/orders/${order.id}`" class="order-id-link">
                #{{ order.id }}
              </router-link>
            </td>
            <td>
              <div v-if="order.accountName">
                <div class="has-text-weight-semibold">{{ order.accountName }}</div>
                <div class="has-text-grey is-size-7">ID: {{ order.accountId }}</div>
              </div>
              <div v-else class="has-text-grey">
                Анонимный пользователь
              </div>
            </td>
            <td>
              <div class="phone-cell">{{ formatPhone(order.phone) }}</div>
            </td>
            <td>
              <div class="address-cell">
                {{ truncateAddress(order.address, 30) }}
                <div v-if="order.notes" class="has-text-grey is-size-7 mt-1" title="Примечание">
                  <i class="fas fa-sticky-note mr-1"></i>{{ truncateText(order.notes, 20) }}
                </div>
              </div>
            </td>
            <td>
              <span class="status-tag" :class="getStatusClass(order.orderStatusId)">
                    {{ getStatusName(order.orderStatusId) }}
              </span>
            </td>
            <td>
              <span class="has-text-weight-semibold">{{ order.totalAmount }} ₽</span>
            </td>
            <td>
              <div class="date-cell">
                <div>{{ formatDate(order.createdAt) }}</div>
                <div class="has-text-grey is-size-7">
                  {{ formatTime(order.createdAt) }}
                </div>
              </div>
            </td>
            <td>
              <div class="action-buttons">
                <router-link
                  :to="`/admin/orders/${order.id}`"
                  class="button is-info is-small is-light"
                  title="Просмотр деталей"
                >
                  <i class="fas fa-eye"></i>
                </router-link>
                <button
                  v-if="authStore.canManageRoles && order.orderStatusId !== 5"
                  class="button is-danger is-small is-light"
                  @click="cancelOrder(order)"
                  title="Отменить заказ"
                  :disabled="cancellingOrderId === order.id"
                >
                  <i v-if="cancellingOrderId === order.id" class="fas fa-spinner fa-spin"></i>
                  <i v-else class="fas fa-times"></i>
                </button>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination-container mt-4">
        <nav class="pagination is-centered" role="navigation" aria-label="pagination">
          <button class="pagination-previous"
                  :disabled="currentPage === 1"
                  @click="goToPage(currentPage - 1)">
            Назад
          </button>
          <button class="pagination-next"
                  :disabled="currentPage === totalPages"
                  @click="goToPage(currentPage + 1)">
            Вперед
          </button>

          <ul class="pagination-list">
            <li v-if="currentPage > 2">
              <button class="pagination-link" @click="goToPage(1)">1</button>
            </li>
            <li v-if="currentPage > 3">
              <span class="pagination-ellipsis">&hellip;</span>
            </li>

            <li v-for="page in visiblePages" :key="page">
              <button class="pagination-link"
                      :class="{ 'is-current': currentPage === page }"
                      @click="goToPage(page)">
                {{ page }}
              </button>
            </li>

            <li v-if="currentPage < totalPages - 2">
              <span class="pagination-ellipsis">&hellip;</span>
            </li>
            <li v-if="currentPage < totalPages - 1">
              <button class="pagination-link" @click="goToPage(totalPages)">{{ totalPages }}</button>
            </li>
          </ul>

          <div class="pagination-info">
            Страница {{ currentPage }} из {{ totalPages }}
          </div>
        </nav>
      </div>

      <div v-if="orders.length === 0" class="has-text-centered py-6">
        <i class="fas fa-box-open fa-3x has-text-grey-light"></i>
        <p class="title is-4 mt-4">Заказы не найдены</p>
        <p class="subtitle is-6 has-text-grey">
          Начните продавать товары, чтобы здесь появились заказы
        </p>
      </div>

      <div v-else-if="filteredOrders.length === 0" class="has-text-centered py-6">
        <i class="fas fa-search fa-3x has-text-grey-light"></i>
        <p class="title is-4 mt-4">Заказы не найдены</p>
        <p class="subtitle is-6 has-text-grey">
          Попробуйте изменить параметры поиска
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { shopAPI } from '@/api/shop'
import type { Order, OrderStatus } from '@/types/shop'
import AppNotification from '../components/AppNotification.vue'

const authStore = useAuthStore()

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
const orders = ref<Order[]>([])
const orderStatuses = ref<OrderStatus[]>([])
const loading = ref(false)
const currentPage = ref(1)
const itemsPerPage = ref(20)
const updatingStatusId = ref<number | null>(null)
const cancellingOrderId = ref<number | null>(null)
const editingStatus = reactive<Record<number, boolean>>({})
const dropdownOpen = reactive<Record<number, boolean>>({})
const selectedStatus = reactive<Record<number, number>>({})
const dateError = ref('')

const search = reactive({
  orderId: '',
  phone: '',
  accountId: '',
  address: '',
  customerName: '',
  email: ''
})

const filters = reactive({
  statusId: '',
  dateFrom: '',
  dateTo: '',
  minAmount: ''
})

const loadOrders = async () => {
  loading.value = true
  try {
    const ordersResponse = await shopAPI.getAllOrders()
    orders.value = ordersResponse.data

    try {
      const statusesResponse = await shopAPI.getOrderStatuses()
      orderStatuses.value = statusesResponse.data
    } catch (statusError) {
      console.warn('Не удалось загрузить статусы:', statusError)
      orderStatuses.value = [
        { id: 1, name: 'Новый', description: 'Заказ создан' },
        { id: 2, name: 'В обработке', description: 'Заказ обрабатывается' },
        { id: 3, name: 'Отправлен', description: 'Заказ отправлен' },
        { id: 4, name: 'Доставлен', description: 'Заказ доставлен' },
        { id: 5, name: 'Отменен', description: 'Заказ отменен' }
      ]
    }
  } catch (error) {
    console.error('Ошибка при загрузке заказов:', error)
    showNotification('Не удалось загрузить список заказов', 'error')
  } finally {
    loading.value = false
  }
}

const validateDates = () => {
  dateError.value = ''

  if (filters.dateFrom && filters.dateTo) {
    const fromDate = new Date(filters.dateFrom)
    const toDate = new Date(filters.dateTo)

    if (fromDate > toDate) {
      dateError.value = 'Дата начала не может быть больше даты окончания'
      filters.dateTo = ''
    }
  }

  applyFilters()
}

const totalOrders = computed(() => orders.value.length)

const totalRevenue = computed(() => {
  return orders.value.reduce((sum, order) => sum + order.totalAmount, 0)
})

const pendingOrdersCount = computed(() => {
  return orders.value.filter(order => order.orderStatusId === 1 || order.orderStatusId === 2).length
})

const deliveredOrdersCount = computed(() => {
  return orders.value.filter(order => order.orderStatusId === 4).length
})

const filteredOrders = computed(() => {
  let result = [...orders.value]

  if (search.orderId) {
    const orderId = parseInt(search.orderId)
    if (!isNaN(orderId)) {
      result = result.filter(order => order.id === orderId)
    }
  }

  if (search.phone) {
    const phoneQuery = search.phone.replace(/\D/g, '').toLowerCase()
    result = result.filter(order =>
      order.phone && order.phone.replace(/\D/g, '').toLowerCase().includes(phoneQuery)
    )
  }

  if (search.accountId) {
    const accountId = parseInt(search.accountId)
    if (!isNaN(accountId)) {
      result = result.filter(order => order.accountId === accountId)
    }
  }

  if (search.address) {
    const addressQuery = search.address.toLowerCase()
    result = result.filter(order =>
      order.address && order.address.toLowerCase().includes(addressQuery)
    )
  }

  if (search.customerName) {
    const nameQuery = search.customerName.toLowerCase()
    result = result.filter(order =>
      order.accountName && order.accountName.toLowerCase().includes(nameQuery)
    )
  }

  if (search.email) {
    const emailQuery = search.email.toLowerCase()
    result = result.filter(order =>
      order.email && order.email.toLowerCase().includes(emailQuery)
    )
  }

  if (filters.statusId) {
    const statusId = parseInt(filters.statusId)
    if (!isNaN(statusId)) {
      result = result.filter(order => order.orderStatusId === statusId)
    }
  }

  if (filters.dateFrom) {
    const fromDate = new Date(filters.dateFrom)
    fromDate.setHours(0, 0, 0, 0)
    result = result.filter(order => new Date(order.createdAt) >= fromDate)
  }

  if (filters.dateTo) {
    const toDate = new Date(filters.dateTo)
    toDate.setHours(23, 59, 59, 999)
    result = result.filter(order => new Date(order.createdAt) <= toDate)
  }

  if (filters.minAmount) {
    const minAmount = parseFloat(filters.minAmount)
    if (!isNaN(minAmount)) {
      result = result.filter(order => order.totalAmount >= minAmount)
    }
  }

  return result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
})

const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value
  const end = start + itemsPerPage.value
  return filteredOrders.value.slice(start, end)
})

const totalPages = computed(() => {
  return Math.ceil(filteredOrders.value.length / itemsPerPage.value)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 1)
  const end = Math.min(totalPages.value, currentPage.value + 1)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

const getStatusName = (statusId: number): string => {
  const status = orderStatuses.value.find(s => s.id === statusId)
  return status ? status.name : `Статус #${statusId}`
}

const getStatusClass = (statusId: number): string => {
  switch (statusId) {
    case 1: return 'is-info'
    case 2: return 'is-warning'
    case 3: return 'is-primary'
    case 4: return 'is-success'
    case 5: return 'is-danger'
    default: return 'is-light'
  }
}

const cancelOrder = async (order: Order) => {
  cancellingOrderId.value = order.id
  try {
    await shopAPI.updateOrder(order.id, {
      orderStatusId: 5,
      address: order.address,
      phone: order.phone,
      totalAmount: order.totalAmount,
      accountId: order.accountId
    })

    const orderIndex = orders.value.findIndex(o => o.id === order.id)
    if (orderIndex !== -1) {
      orders.value[orderIndex].orderStatusId = 5
    }

    showNotification('Заказ успешно отменен', 'success')

  } catch (error: any) {
    console.error('Ошибка при отмене заказа:', error)
    const message = error.response?.data?.message || 'Не удалось отменить заказ'
    showNotification(`Ошибка: ${message}`, 'error')
  } finally {
    cancellingOrderId.value = null
  }
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

const formatTime = (dateString: string) => {
  return new Date(dateString).toLocaleTimeString('ru-RU', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPhone = (phone: string) => {
  if (!phone) return ''
  const cleaned = phone.replace(/\D/g, '')
  if (cleaned.length === 11) {
    return `+${cleaned[0]} (${cleaned.slice(1, 4)}) ${cleaned.slice(4, 7)}-${cleaned.slice(7, 9)}-${cleaned.slice(9)}`
  }
  return phone
}

const truncateAddress = (address: string, maxLength: number) => {
  if (!address) return 'Без адреса'
  if (address.length <= maxLength) return address
  return address.substring(0, maxLength) + '...'
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const applyFilters = () => {
  currentPage.value = 1
}

const clearFilters = () => {
  search.orderId = ''
  search.phone = ''
  search.accountId = ''
  search.address = ''
  search.customerName = ''
  search.email = ''
  filters.statusId = ''
  filters.dateFrom = ''
  filters.dateTo = ''
  filters.minAmount = ''
  dateError.value = ''
  currentPage.value = 1
}

const refreshOrders = () => {
  loadOrders()
}

const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleClickOutside = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.status-edit-mode') && !target.closest('.status-display')) {
    Object.keys(editingStatus).forEach(key => {
      editingStatus[parseInt(key)] = false
    })
    Object.keys(dropdownOpen).forEach(key => {
      dropdownOpen[parseInt(key)] = false
    })
  }
}

onMounted(() => {
  loadOrders()
  document.addEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.admin-orders-view {
  padding: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #333;
}

.content-header .title {
  font-size: 1.8rem;
  margin: 0;
}

.content-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-filters {
  background-color: #1a1a1a;
  border: 1px solid #333;
}

.stats {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
}

.stat-card {
  text-align: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #252525;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #4a00e0;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 0.9rem;
  color: #aaa;
}

.table th {
  background-color: #1a1a1a;
  color: #fff;
  border-bottom: 2px solid #333;
  font-weight: 600;
}

.table td {
  vertical-align: middle;
  border-bottom: 1px solid #333;
  padding: 12px 10px;
}

.table tr:hover {
  background-color: #252525;
}

.order-id-link {
  color: #4a00e0;
  font-weight: bold;
  text-decoration: none;
}

.order-id-link:hover {
  text-decoration: underline;
  color: #8e2de2;
}

.address-cell {
  max-width: 200px;
  word-break: break-word;
}

.phone-cell {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

.date-cell {
  min-width: 120px;
}

.status-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: white;
  transition: all 0.2s;
  white-space: nowrap;
}

.status-tag.is-info {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.status-tag.is-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.status-tag.is-primary {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.status-tag.is-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.status-tag.is-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.status-tag.is-light {
  background-color: #6b7280;
}

.action-buttons {
  display: flex;
  gap: 6px;
}

.action-buttons .button {
  padding: 4px 8px;
  min-width: 32px;
  font-size: 0.85rem;
}

.pagination-container {
  background-color: #1a1a1a;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #333;
}

.pagination {
  margin-bottom: 0;
}

.pagination-previous,
.pagination-next {
  background-color: #252525;
  border-color: #444;
  color: #ddd;
  font-weight: 600;
}

.pagination-previous:hover,
.pagination-next:hover {
  background-color: #333;
  border-color: #555;
}

.pagination-previous:disabled,
.pagination-next:disabled {
  background-color: #1a1a1a;
  border-color: #333;
  color: #666;
  cursor: not-allowed;
}

.pagination-link {
  background-color: #252525;
  border-color: #444;
  color: #ddd;
  min-width: 40px;
}

.pagination-link:hover {
  background-color: #333;
  border-color: #555;
  color: #fff;
}

.pagination-link.is-current {
  background: linear-gradient(135deg, #4a00e0 0%, #8e2de2 100%);
  border-color: #4a00e0;
  color: #fff;
}

.pagination-ellipsis {
  color: #666;
}

.pagination-info {
  margin-left: 20px;
  color: #aaa;
  font-size: 0.9rem;
}

@media (max-width: 1024px) {
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .content-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .table-container {
    overflow-x: auto;
  }

  .table {
    min-width: 1000px;
  }

  .stats .columns {
    gap: 10px;
  }

  .stat-card {
    padding: 10px;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}

@media (max-width: 768px) {
  .search-filters .columns {
    margin-bottom: 0;
  }

  .search-filters .column {
    padding-bottom: 0.75rem;
  }

  .pagination-list {
    display: none;
  }
}
</style>
