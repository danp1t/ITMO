<template>
  <div class="orders-view">
    <!-- Компонент уведомлений -->
    <AppNotification
      :notification="notification"
      @hide="hideNotification"
    />

    <div class="container">
      <div class="has-text-centered mb-5">
        <h1 class="title is-2">Мои заказы</h1>
        <p class="subtitle is-5 has-text-grey">
          История ваших заказов и их статусы
        </p>
      </div>

      <div v-if="loading" class="has-text-centered py-6">
        <i class="fas fa-spinner fa-spin fa-2x"></i>
        <p class="mt-3">Загрузка заказов...</p>
      </div>

      <div v-else-if="filteredOrders.length > 0">
        <div class="orders-list">
          <div
            v-for="order in filteredOrders"
            :key="order.id"
            class="order-card box mb-4"
          >
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

            <div class="order-products">
              <h4 class="title is-5 mb-3">Товары:</h4>
              <div class="table-container">
                <table class="table is-fullwidth">
                  <thead>
                  <tr>
                    <th>Товар</th>
                    <th>Размер</th>
                    <th>Цена</th>
                    <th>Количество</th>
                    <th>Сумма</th>
                  </tr>
                  </thead>
                  <tbody>
                  <template v-if="order.orderProducts && order.orderProducts.length > 0">
                    <tr v-for="item in order.orderProducts" :key="`${item.productId}-${item.productInfoId}`">
                      <td>{{ item.productName }}</td>
                      <td>{{ item.size }}</td>
                      <td>{{ item.price }} ₽</td>
                      <td>{{ item.quantity }}</td>
                      <td>{{ item.price * item.quantity }} ₽</td>
                    </tr>
                  </template>
                  <template v-else>
                    <tr v-for="product in order.products" :key="product.id">
                      <td>{{ product.name }}</td>
                      <td>-</td>
                      <td>{{ product.basePrice }} ₽</td>
                      <td>1</td>
                      <td>{{ product.basePrice }} ₽</td>
                    </tr>
                  </template>
                  </tbody>
                  <tfoot>
                  <tr>
                    <td colspan="4" class="has-text-right has-text-weight-semibold">
                      Товары:
                    </td>
                    <td class="has-text-weight-bold">
                      {{ getProductsSubtotal(order) }} ₽
                    </td>
                  </tr>
                  <tr>
                    <td colspan="4" class="has-text-right has-text-weight-semibold">
                      Доставка:
                    </td>
                    <td class="has-text-weight-bold">
                      {{ getDeliveryCost(order) }} ₽
                    </td>
                  </tr>
                  <tr>
                    <td colspan="4" class="has-text-right has-text-weight-bold">
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

            <div class="order-actions mt-4">
              <div class="buttons">
                <button
                  v-if="canCancelOrder(order)"
                  class="button is-danger is-light"
                  @click="cancelOrder(order)"
                  :disabled="cancellingOrderId === order.id"
                >
                  <span class="icon" v-if="cancellingOrderId === order.id">
                    <i class="fas fa-spinner fa-spin"></i>
                  </span>
                  <span v-else class="icon">
                    <i class="fas fa-times"></i>
                  </span>
                  <span>Отменить заказ</span>
                </button>
                <button
                  class="button is-primary is-light"
                  @click="viewOrderDetails(order)"
                >
                  <span class="icon">
                    <i class="fas fa-eye"></i>
                  </span>
                  <span>Подробнее</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

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
import { shopAPI } from '../api/shop'
import type { Order, OrderStatus } from '../types/shop'
import AppNotification from '@/components/AppNotification.vue'

const authStore = useAuthStore()
const router = useRouter()

interface NotificationState {
  visible: boolean
  message: string
  type: 'info' | 'success' | 'warning' | 'error'
}

const orders = ref<Order[]>([])
const orderStatuses = ref<OrderStatus[]>([])
const loading = ref(false)
const cancellingOrderId = ref<number | null>(null)
const notification = ref<NotificationState>({
  visible: false,
  message: '',
  type: 'info'
})

const selectedStatus = ref<number | null>(null)

const loadOrders = async () => {
  loading.value = true

  try {
    if (!authStore.user?.id) {
      throw new Error('Пользователь не авторизован')
    }

    const ordersResponse = await shopAPI.getOrdersByAccount(authStore.user.id)
    orders.value = ordersResponse.data

    const statusesResponse = await shopAPI.getOrderStatuses()
    orderStatuses.value = statusesResponse.data

  } catch (error) {
    console.error('Ошибка при загрузке заказов:', error)
    showNotification('Ошибка при загрузке заказов', 'error')
  } finally {
    loading.value = false
  }
}

const filteredOrders = computed(() => {
  if (selectedStatus.value === null) {
    return orders.value
  }
  return orders.value.filter(order => order.orderStatusId === selectedStatus.value)
})

const getProductsSubtotal = (order: Order) => {
  if (order.orderProducts && order.orderProducts.length > 0) {
    return order.orderProducts.reduce((total, item) => {
      return total + (item.price * item.quantity)
    }, 0)
  } else {
    return order.products.reduce((total, product) => {
      return total + product.basePrice
    }, 0)
  }
}

const getDeliveryCost = (order: Order) => {
  return order.totalAmount - getProductsSubtotal(order)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getStatusText = (statusId: number) => {
  const status = orderStatuses.value.find(s => s.id === statusId)
  return status ? status.name : 'Неизвестно'
}

const getStatusClass = (statusId: number) => {
  switch (statusId) {
    case 1: return 'is-info'
    case 2: return 'is-warning'
    case 3: return 'is-primary'
    case 4: return 'is-success'
    case 5: return 'is-danger'
    default: return 'is-light'
  }
}

const canCancelOrder = (order: Order) => {
  return order.orderStatusId === 1
}

const cancelOrder = async (order: Order) => {
  cancellingOrderId.value = order.id

  try {
    const updateData = {
      id: order.id,
      address: order.address,
      phone: order.phone,
      email: order.email,
      customerName: order.customerName,
      totalAmount: order.totalAmount,
      accountId: order.accountId,
      orderStatusId: 5,
      orderStatusName: 'cancelled',
      deliveryMethod: order.deliveryMethod,
      paymentMethod: order.paymentMethod,
      postalCode: order.postalCode,
      notes: order.notes || ''
    }

    console.log('Отправка данных для обновления заказа:', updateData)

    const response = await shopAPI.updateOrder(order.id, updateData)
    console.log('Ответ от сервера:', response)

    const orderIndex = orders.value.findIndex(o => o.id === order.id)
    if (orderIndex !== -1) {
      orders.value[orderIndex].orderStatusId = 5
      orders.value[orderIndex].orderStatusName = 'cancelled'
    }

    showNotification('Заказ успешно отменен', 'success')

  } catch (error: any) {
    console.error('Ошибка при отмене заказа:', error)
    console.error('Полный ответ ошибки:', error.response)
    const message = error.response?.data?.message || 'Не удалось отменить заказ'
    showNotification(message, 'error')
  } finally {
    cancellingOrderId.value = null
  }
}

const viewOrderDetails = (order: Order) => {
  router.push(`/shop/orders/${order.id}`)
}

const filterByStatus = (statusId: number | null) => {
  selectedStatus.value = statusId
}

const showNotification = (message: string, type: 'info' | 'success' | 'warning' | 'error' = 'info') => {
  notification.value = {
    visible: true,
    message,
    type
  }

  // Автоматически скрыть уведомление через 5 секунд
  setTimeout(() => {
    notification.value.visible = false
  }, 5000)
}

const hideNotification = () => {
  notification.value.visible = false
}

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
  color: #bcc0c8;
}

.subtitle {
  color: #bfc6d1;
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
  color: #202833;
  border-bottom: 2px solid #dee2e6;
}

.table td {
  vertical-align: middle;
  border-bottom: 1px solid #f1f3f5;
}

.empty-state {
  padding: 3rem 1rem;
}

.button.is-primary:hover {
  opacity: 0.9;
}

.button.is-light {
  border: 1px solid #e5e7eb;
}

.button.is-danger.is-light {
  border-color: #f14668;
  color: #f14668;
}

.button.is-danger.is-light:hover {
  background-color: #f14668;
  color: white;
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
