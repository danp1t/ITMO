<template>
  <div class="order-details-view">
    <div class="container">
      <!-- Навигация назад -->
      <div class="mb-4">
        <router-link to="/shop/orders" class="button is-light">
          <span class="icon">
            <i class="fas fa-arrow-left"></i>
          </span>
          <span>Назад к заказам</span>
        </router-link>
      </div>

      <!-- Заголовок -->
      <div class="has-text-centered mb-5">
        <h1 class="title is-2">Заказ #{{ orderId }}</h1>
        <p class="subtitle is-5 has-text-grey">
          Детальная информация о вашем заказе
        </p>
      </div>

      <!-- Загрузка -->
      <div v-if="loading" class="has-text-centered py-6">
        <i class="fas fa-spinner fa-spin fa-2x"></i>
        <p class="mt-3">Загрузка информации о заказе...</p>
      </div>

      <!-- Ошибка -->
      <div v-else-if="error" class="notification is-danger">
        {{ error }}
        <router-link to="/shop/orders" class="button is-light ml-2">
          Вернуться к заказам
        </router-link>
      </div>

      <!-- Информация о заказе -->
      <div v-else-if="order" class="order-details">
        <!-- Статус заказа -->
        <div class="box mb-4">
          <div class="level">
            <div class="level-left">
              <div>
                <h3 class="title is-4">Статус заказа</h3>
                <p class="subtitle is-6 has-text-grey">
                  {{ formatDate(order.createdAt) }}
                </p>
              </div>
            </div>
            <div class="level-right">
              <span class="tag is-large" :class="statusClass">
                {{ orderStatusText }}
              </span>
            </div>
          </div>

          <!-- Прогресс доставки -->
          <div class="progress-container mt-4">
            <div class="progress-steps">
              <div
                v-for="step in deliverySteps"
                :key="step.id"
                class="step"
                :class="{ 'is-active': isStepActive(step.id), 'is-completed': isStepCompleted(step.id) }"
              >
                <div class="step-icon">
                  <i :class="step.icon"></i>
                </div>
                <div class="step-label">{{ step.label }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Контактная информация -->
        <div class="columns mb-4">
          <div class="column">
            <div class="box">
              <h3 class="title is-5 mb-3">Контактная информация</h3>
              <div class="content">
                <p><strong>Телефон:</strong> {{ order.phone }}</p>
                <p><strong>Адрес доставки:</strong> {{ order.address }}</p>
                <p v-if="order.orderStatus">
                  <strong>Статус:</strong> {{ order.orderStatus.name }}
                </p>
              </div>
            </div>
          </div>

          <div class="column">
            <div class="box">
              <h3 class="title is-5 mb-3">Сводка заказа</h3>
              <div class="content">
                <div class="level is-mobile">
                  <div class="level-left">Номер заказа:</div>
                  <div class="level-right"><strong>#{{ order.id }}</strong></div>
                </div>
                <div class="level is-mobile">
                  <div class="level-left">Дата оформления:</div>
                  <div class="level-right">{{ formatDate(order.createdAt) }}</div>
                </div>
                <div class="level is-mobile">
                  <div class="level-left">Общая сумма:</div>
                  <div class="level-right"><strong>{{ order.totalAmount }} ₽</strong></div>
                </div>
                <div v-if="order.updatedAt" class="level is-mobile">
                  <div class="level-left">Последнее обновление:</div>
                  <div class="level-right">{{ formatDate(order.updatedAt) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Товары в заказе -->
        <div class="box">
          <h3 class="title is-4 mb-4">Товары в заказе</h3>

          <div class="table-container">
            <table class="table is-fullwidth is-striped">
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
                <td>
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-description has-text-grey is-size-7">
                      {{ truncateDescription(product.description, 100) }}
                    </div>
                  </div>
                </td>
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
        <div class="box">
          <h3 class="title is-5 mb-3">Действия с заказом</h3>
          <div class="buttons">
            <button
              v-if="canCancelOrder"
              class="button is-danger"
              @click="cancelOrder"
              :disabled="cancelling"
            >
              <span class="icon" v-if="cancelling">
                <i class="fas fa-spinner fa-spin"></i>
              </span>
              <span class="icon" v-else>
                <i class="fas fa-times"></i>
              </span>
              <span>Отменить заказ</span>
            </button>

            <button
              class="button is-light"
              @click="repeatOrder"
            >
              <span class="icon">
                <i class="fas fa-redo"></i>
              </span>
              <span>Повторить заказ</span>
            </button>

            <button
              class="button is-primary is-light"
              @click="downloadInvoice"
              :disabled="downloadingInvoice"
            >
              <span class="icon" v-if="downloadingInvoice">
                <i class="fas fa-spinner fa-spin"></i>
              </span>
              <span class="icon" v-else>
                <i class="fas fa-file-invoice"></i>
              </span>
              <span>Скачать счет</span>
            </button>

            <button
              class="button is-info is-light"
              @click="contactSupport"
            >
              <span class="icon">
                <i class="fas fa-headset"></i>
              </span>
              <span>Связаться с поддержкой</span>
            </button>
          </div>
        </div>

        <!-- История статусов -->
        <div v-if="statusHistory.length > 0" class="box">
          <h3 class="title is-5 mb-3">История статусов</h3>
          <div class="timeline">
            <div
              v-for="(item, index) in statusHistory"
              :key="index"
              class="timeline-item"
            >
              <div class="timeline-marker" :class="getTimelineMarkerClass(item.status)">
                <i :class="getTimelineIcon(item.status)"></i>
              </div>
              <div class="timeline-content">
                <p class="heading">{{ formatDate(item.date) }}</p>
                <p>{{ item.description }}</p>
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
import { shopAPI } from '../api/shop'
import type { Order } from '../types/shop'

const route = useRoute()
const router = useRouter()

const orderId = ref(parseInt(route.params.id as string))
const order = ref<Order | null>(null)
const loading = ref(false)
const error = ref('')
const cancelling = ref(false)
const downloadingInvoice = ref(false)

// Загрузка заказа
const loadOrder = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await shopAPI.getOrderById(orderId.value)
    order.value = response.data
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Не удалось загрузить информацию о заказе'
    console.error('Ошибка при загрузке заказа:', err)
  } finally {
    loading.value = false
  }
}

// Шаги доставки
const deliverySteps = [
  { id: 1, label: 'Оформлен', icon: 'fas fa-shopping-cart' },
  { id: 2, label: 'Обрабатывается', icon: 'fas fa-cog' },
  { id: 3, label: 'Отправлен', icon: 'fas fa-shipping-fast' },
  { id: 4, label: 'В пути', icon: 'fas fa-truck' },
  { id: 5, label: 'Доставлен', icon: 'fas fa-check-circle' }
]

// Статус заказа
const orderStatusText = computed(() => {
  if (!order.value?.orderStatus) return 'Неизвестно'
  return order.value.orderStatus.name
})

const statusClass = computed(() => {
  if (!order.value) return 'is-light'

  switch (order.value.orderStatusId) {
    case 1: return 'is-info' // Новый
    case 2: return 'is-warning' // В обработке
    case 3: return 'is-primary' // Отправлен
    case 4: return 'is-success' // Доставлен
    case 5: return 'is-danger' // Отменен
    default: return 'is-light'
  }
})

// Активность шагов доставки
const isStepActive = (stepId: number) => {
  if (!order.value) return false
  return order.value.orderStatusId === stepId
}

const isStepCompleted = (stepId: number) => {
  if (!order.value) return false
  return order.value.orderStatusId > stepId
}

// Можно ли отменить заказ
const canCancelOrder = computed(() => {
  if (!order.value) return false
  // Можно отменять только новые заказы
  return order.value.orderStatusId === 1
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

// Укорачивание описания
const truncateDescription = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// Отмена заказа
const cancelOrder = async () => {
  if (!order.value || !confirm('Вы уверены, что хотите отменить этот заказ?')) {
    return
  }

  cancelling.value = true

  try {
    await shopAPI.updateOrder(order.value.id, { orderStatusId: 5 })
    await loadOrder() // Перезагружаем данные
  } catch (err: any) {
    const message = err.response?.data?.message || 'Не удалось отменить заказ'
    alert(message)
  } finally {
    cancelling.value = false
  }
}

// Повторить заказ
const repeatOrder = () => {
  // В реальном приложении здесь можно было бы добавлять товары в корзину
  alert('Функция "Повторить заказ" в разработке')
  router.push('/shop')
}

// Скачать счет
const downloadInvoice = async () => {
  if (!order.value) return

  downloadingInvoice.value = true

  try {
    // В реальном приложении здесь был бы запрос на генерацию PDF
    alert('Счет будет сгенерирован и отправлен на ваш email')
    // Имитация загрузки
    await new Promise(resolve => setTimeout(resolve, 1000))
  } catch (err) {
    alert('Не удалось сгенерировать счет')
  } finally {
    downloadingInvoice.value = false
  }
}

// Связаться с поддержкой
const contactSupport = () => {
  if (!order.value) return
  const subject = `Вопрос по заказу #${order.value.id}`
  const body = `Здравствуйте! У меня вопрос по заказу #${order.value.id}...`
  window.location.href = `mailto:support@example.com?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`
}

// История статусов (в реальном приложении брать из API)
const statusHistory = computed(() => {
  if (!order.value) return []

  const history = [
    {
      date: order.value.createdAt,
      status: 1,
      description: 'Заказ оформлен'
    }
  ]

  if (order.value.updatedAt && order.value.updatedAt !== order.value.createdAt) {
    history.push({
      date: order.value.updatedAt,
      status: order.value.orderStatusId,
      description: 'Статус обновлен'
    })
  }

  return history
})

// Стили для timeline
const getTimelineMarkerClass = (statusId: number) => {
  switch (statusId) {
    case 1: return 'is-info'
    case 5: return 'is-danger'
    default: return 'is-primary'
  }
}

const getTimelineIcon = (statusId: number) => {
  switch (statusId) {
    case 1: return 'fas fa-shopping-cart'
    case 5: return 'fas fa-times'
    default: return 'fas fa-sync'
  }
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.order-details-view {
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

.tag.is-large {
  font-size: 1.1rem;
  padding: 0.75rem 1.25rem;
}

/* Прогресс доставки */
.progress-container {
  padding: 1rem 0;
}

.progress-steps {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.progress-steps::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 0;
  right: 0;
  height: 3px;
  background-color: #e5e7eb;
  z-index: 1;
}

.step {
  position: relative;
  z-index: 2;
  text-align: center;
  flex: 1;
}

.step-icon {
  width: 40px;
  height: 40px;
  background-color: white;
  border: 3px solid #e5e7eb;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 0.5rem;
  transition: all 0.3s;
}

.step-label {
  font-size: 0.875rem;
  color: #6b7280;
  transition: color 0.3s;
}

.step.is-completed .step-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

.step.is-active .step-icon {
  background: white;
  border-color: #667eea;
  color: #667eea;
  transform: scale(1.1);
}

.step.is-active .step-label {
  color: #667eea;
  font-weight: 600;
}

/* Таблица */
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

.product-info {
  max-width: 300px;
}

.product-name {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

/* Кнопки */
.buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
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

.button.is-danger {
  background-color: #ef4444;
  color: white;
}

.button.is-danger:hover {
  background-color: #dc2626;
}

.button.is-light {
  border: 1px solid #e5e7eb;
}

/* Timeline */
.timeline {
  position: relative;
  padding-left: 2rem;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: #e5e7eb;
}

.timeline-item {
  position: relative;
  margin-bottom: 1.5rem;
}

.timeline-marker {
  position: absolute;
  left: -2rem;
  top: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background-color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.75rem;
}

.timeline-marker.is-info {
  background-color: #3b82f6;
}

.timeline-marker.is-danger {
  background-color: #ef4444;
}

.timeline-content {
  padding-left: 1rem;
}

.heading {
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 0.25rem;
}

@media (max-width: 768px) {
  .order-details-view {
    padding: 1rem;
  }

  .progress-steps {
    flex-direction: column;
    align-items: flex-start;
    gap: 1.5rem;
  }

  .progress-steps::before {
    display: none;
  }

  .step {
    display: flex;
    align-items: center;
    text-align: left;
  }

  .step-icon {
    margin: 0 1rem 0 0;
    flex-shrink: 0;
  }

  .buttons {
    flex-direction: column;
  }

  .button {
    justify-content: center;
  }
}
</style>
