<template>
  <div class="orders-view">
    <div class="container">
      <!-- Заголовок -->
      <div class="has-text-centered mb-5">
        <h1 class="title is-2">Мои заказы</h1>
        <p class="subtitle is-5 has-text-grey">
          История ваших заказов и их статусы
        </p>
      </div>

      <!-- Загрузка -->
      <div v-if="loading" class="has-text-centered py-6">
        <i class="fas fa-spinner fa-spin fa-2x"></i>
        <p class="mt-3">Загрузка заказов...</p>
      </div>

      <!-- Заказы -->
      <div v-else-if="orders.length > 0">
        <div class="orders-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-card box mb-4"
          >
            <!-- Заголовок заказа -->
            <div class="order-header mb-4">
              <div class="level is-mobile">
                <div class="level-left">
                  <div>
                    <h3 class="title is-4 mb-1">Заказ #{{ order.id }}</h3>
                    <p class="has-text-grey">
                      {{ formatDate(order.createdAt) }}
                    </p>
                  </div>
                </div>
                <div class="level-right">
                  <div class="tags has-addons">
                    <span class="tag" :class="getStatusClass(order.orderStatusId)">
                      {{ getStatusText(order.orderStatusId) }}
                    </span>
                    <span class="tag is-dark">
                      {{ order.totalAmount }} ₽
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Информация о заказе -->
            <div class="order-info mb-4">
              <div class="columns">
                <div class="column">
                  <div class="field">
                    <label class="label has-text-weight-semibold">Адрес доставки:</label>
                    <p>{{ order.address }}</p>
                  </div>
                </div>
                <div class="column">
                  <div class="field">
                    <label class="label has-text-weight-semibold">Телефон:</label>
                    <p>{{ order.phone }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Товары в заказе -->
            <div class="order-products">
              <h4 class="title is-5 mb-3">Товары:</h4>
              <div class="table-container">
                <table class="table is-fullwidth">
                  <thead>
                  <tr>
                    <th>Товар</th>
                    <th>Цена</th>
                    <th>Количество</th>
                    <th>Сумма</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="product in order.products" :key="product.id">
                    <td>{{ product.name }}</td>
                    <td>{{ product.basePrice }} ₽</td>
                    <td>1</td>
                    <td>{{ product.basePrice }} ₽</td>
                  </tr>
                  </tbody>
                  <tfoot>
                  <tr>
                    <td colspan="3" class="has-text-right has-text-weight-semibold">
                      Итого:
                    </td>
                    <td class="has-text-weight-bold">
                      {{ order.totalAmount }} ₽
                    </td>
                  </tr>
                  </tfoot>
                </table>
              </div>
            </div>

            <!-- Действия -->
            <div class="order-actions mt-4">
              <div class="buttons">
                <button
                  v-if="canCancelOrder(order)"
                  class="button is-danger is-light"
                  @click="cancelOrder(order)"
                >
                  Отменить заказ
                </button>
                <button
                  class="button is-light"
                  @click="repeatOrder(order)"
                >
                  Повторить заказ
                </button>
                <button
                  class="button is-primary is-light"
                  @click="viewOrderDetails(order)"
                >
                  Подробнее
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Нет заказов -->
      <div v-else class="empty-orders has-text-centered py-6">
        <div class="empty-state">
          <i class="fas fa-box-open fa-3x has-text-grey-light"></i>
          <p class="title is-4 mt-4">У вас еще нет заказов</p>
          <p class="subtitle is-6 has-text-grey">
            Сделайте свой первый заказ в нашем магазине
          </p>
          <router-link to="/shop" class="button is-primary mt-3">
            Перейти в магазин
          </router-link>
        </div>
      </div>

      <!-- Фильтры -->
      <div class="box mb-4">
        <h3 class="title is-5 mb-3">Фильтры заказов</h3>
        <div class="field is-grouped is-grouped-multiline">
          <div class="control">
            <button
              class="button"
              :class="{'is-primary': selectedStatus === null}"
              @click="filterByStatus(null)"
            >
              Все заказы
            </button>
          </div>
          <div
            v-for="status in orderStatuses"
            :key="status.id"
            class="control"
          >
            <button
              class="button"
              :class="{
                'is-primary': selectedStatus === status.id,
                [getStatusClass(status.id)]: selectedStatus !== status.id
              }"
              @click="filterByStatus(status.id)"
            >
              {{ status.name }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { shopAPI } from '../api/shop'
import type { Order, OrderStatus } from '../types/shop'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

// Данные
const orders = ref<Order[]>([])
const orderStatuses = ref<OrderStatus[]>([])
const loading = ref(false)

// Фильтры
const selectedStatus = ref<number | null>(null)

// Загрузка заказов
const loadOrders = async () => {
  loading.value = true

  try {
    if (!authStore.user?.id) {
      throw new Error('Пользователь не авторизован')
    }

    // Загружаем заказы пользователя
    const ordersResponse = await shopAPI.getOrdersByAccount(authStore.user.id)
    orders.value = ordersResponse.data

    // Загружаем статусы заказов
    const statusesResponse = await shopAPI.getOrderStatuses()
    orderStatuses.value = statusesResponse.data

  } catch (error) {
    console.error('Ошибка при загрузке заказов:', error)
    // В реальном приложении можно показать уведомление
  } finally {
    loading.value = false
  }
}

// Отфильтрованные заказы
const filteredOrders = computed(() => {
  if (selectedStatus.value === null) {
    return orders.value
  }
  return orders.value.filter(order => order.orderStatusId === selectedStatus.value)
})

// Форматирование даты
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Получение текста статуса
const getStatusText = (statusId: number) => {
  const status = orderStatuses.value.find(s => s.id === statusId)
  return status ? status.name : 'Неизвестно'
}

// Получение класса для статуса
const getStatusClass = (statusId: number) => {
  switch (statusId) {
    case 1: return 'is-info' // Новый
    case 2: return 'is-warning' // В обработке
    case 3: return 'is-primary' // Отправлен
    case 4: return 'is-success' // Доставлен
    case 5: return 'is-danger' // Отменен
    default: return 'is-light'
  }
}

// Можно ли отменить заказ
const canCancelOrder = (order: Order) => {
  // Можно отменять только новые заказы
  return order.orderStatusId === 1
}

// Отмена заказа
const cancelOrder = async (order: Order) => {
  if (!confirm('Вы уверены, что хотите отменить этот заказ?')) {
    return
  }

  try {
    // Обновляем статус заказа на "Отменен"
    await shopAPI.updateOrder(order.id, { orderStatusId: 5 })

    // Обновляем список заказов
    await loadOrders()

    alert('Заказ успешно отменен')
  } catch (error: any) {
    console.error('Ошибка при отмене заказа:', error)
    const message = error.response?.data?.message || 'Не удалось отменить заказ'
    alert(message)
  }
}

// Повторить заказ
const repeatOrder = async (order: Order) => {
  // В реальном приложении здесь можно было бы добавлять товары из заказа в корзину
  alert('Функция "Повторить заказ" в разработке')
  // router.push('/shop')
}

// Просмотр деталей заказа
const viewOrderDetails = (order: Order) => {
  router.push(`/shop/orders/${order.id}`)
}

// Фильтрация по статусу
const filterByStatus = (statusId: number | null) => {
  selectedStatus.value = statusId
}

// Инициализация
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-view {
  padding: 2rem 1rem;
}

.container {
  max-width: 1200px;
}

.box {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 1.5rem;
}

.order-card {
  transition: transform 0.3s, box-shadow 0.3s;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.order-header {
  border-bottom: 2px solid #f1f3f5;
  padding-bottom: 1rem;
}

.title {
  color: #2d3748;
}

.subtitle {
  color: #718096;
}

.tag {
  border-radius: 20px;
  font-weight: 500;
}

.table-container {
  overflow-x: auto;
}

.table {
  width: 100%;
}

.table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #495057;
  border-bottom: 2px solid #dee2e6;
}

.table td {
  vertical-align: middle;
  border-bottom: 1px solid #f1f3f5;
}

.empty-state {
  padding: 3rem 1rem;
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

.button.is-light {
  border: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .orders-view {
    padding: 1rem;
  }

  .table {
    font-size: 0.875rem;
  }

  .buttons {
    flex-wrap: wrap;
  }

  .buttons .button {
    margin-bottom: 0.5rem;
  }
}
</style>
