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
                {{ getStatusText(order.orderStatusName) }}
              </span>
            </div>
          </div>

          <!-- Прогресс доставки (только для активных заказов) -->
          <div v-if="order.orderStatusName !== 'cancelled'" class="progress-container mt-4">
            <div class="progress-steps">
              <div
                v-for="step in deliverySteps"
                :key="step.id"
                class="step"
                :class="{
                  'is-active': isStepActive(step.statusName),
                  'is-completed': isStepCompleted(step.statusName)
                }"
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
                <p>
                  <strong>Статус:</strong> {{ getStatusText(order.orderStatusName) }}
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
                <th>Размер</th>
                <th>Цена за шт.</th>
                <th>Количество</th>
                <th>Сумма</th>
              </tr>
              </thead>
              <tbody>
              <!-- Используем orderProducts вместо products -->
              <tr v-for="item in order.orderProducts" :key="`${item.productId}-${item.productInfoId}`">
                <td>
                  <div class="product-info">
                    <div class="product-name">{{ item.productName }}</div>
                    <div v-if="getProductById(item.productId)" class="product-description has-text-grey is-size-7">
                      {{ truncateDescription(getProductById(item.productId)?.description || '', 100) }}
                    </div>
                  </div>
                </td>
                <td>{{ item.size }}</td>
                <td>{{ item.price }} ₽</td>
                <td>{{ item.quantity }}</td>
                <td>{{ item.price * item.quantity }} ₽</td>
              </tr>
              </tbody>
              <tfoot>
              <tr>
                <td colspan="4" class="has-text-right has-text-weight-semibold">
                  Товары:
                </td>
                <td class="has-text-weight-bold">
                  {{ productsSubtotal }} ₽
                </td>
              </tr>
              <tr>
                <td colspan="4" class="has-text-right has-text-weight-semibold">
                  Доставка:
                </td>
                <td class="has-text-weight-bold">
                  {{ deliveryCost }} ₽
                </td>
              </tr>
              <tr>
                <td colspan="4" class="has-text-right has-text-weight-bold is-size-5">
                  Итого:
                </td>
                <td class="has-text-weight-bold is-size-5">
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shopAPI } from '../api/shop'
import type { Order, OrderProduct, Product } from '../types/shop'

const route = useRoute()
const router = useRouter()

const orderId = ref(parseInt(route.params.id as string))
const order = ref<Order | null>(null)
const loading = ref(false)
const error = ref('')
const cancelling = ref(false)

// Карта статусов
const statusMap = {
  'pending': { id: 1, text: 'Ожидает обработки', class: 'is-info', icon: 'fas fa-clock' },
  'processing': { id: 2, text: 'В обработке', class: 'is-warning', icon: 'fas fa-cog' },
  'shipped': { id: 3, text: 'Отправлен', class: 'is-primary', icon: 'fas fa-shipping-fast' },
  'delivered': { id: 4, text: 'Доставлен', class: 'is-success', icon: 'fas fa-check-circle' },
  'cancelled': { id: 5, text: 'Отменен', class: 'is-danger', icon: 'fas fa-times-circle' }
}

// Шаги доставки (только для активных заказов)
const deliverySteps = [
  { id: 1, statusName: 'pending', label: 'Ожидает обработки', icon: 'fas fa-clock' },
  { id: 2, statusName: 'processing', label: 'В обработке', icon: 'fas fa-cog' },
  { id: 3, statusName: 'shipped', label: 'Отправлен', icon: 'fas fa-shipping-fast' },
  { id: 4, statusName: 'delivered', label: 'Доставлен', icon: 'fas fa-check-circle' }
]

// Computed свойства для расчета сумм
const productsSubtotal = computed(() => {
  if (!order.value?.orderProducts) return 0
  return order.value.orderProducts.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

const deliveryCost = computed(() => {
  if (!order.value) return 0
  return order.value.totalAmount - productsSubtotal.value
})

// Вспомогательная функция для получения продукта по ID
const getProductById = computed(() => {
  return (productId: number) => {
    if (!order.value?.products) return null
    return order.value.products.find(p => p.id === productId)
  }
})

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

// Получение текста статуса
const getStatusText = (statusName: string) => {
  const status = statusMap[statusName as keyof typeof statusMap]
  return status ? status.text : 'Неизвестно'
}

// Получение класса для статуса
const statusClass = computed(() => {
  if (!order.value) return 'is-light'
  const status = statusMap[order.value.orderStatusName as keyof typeof statusMap]
  return status ? status.class : 'is-light'
})

// Активность шагов доставки
const isStepActive = (statusName: string) => {
  if (!order.value || order.value.orderStatusName === 'cancelled') return false
  return order.value.orderStatusName === statusName
}

const isStepCompleted = (statusName: string) => {
  if (!order.value || order.value.orderStatusName === 'cancelled') return false

  const currentStatus = order.value.orderStatusName
  const currentStep = deliverySteps.find(step => step.statusName === currentStatus)
  const targetStep = deliverySteps.find(step => step.statusName === statusName)

  if (!currentStep || !targetStep) return false
  return currentStep.id > targetStep.id
}

// Можно ли отменить заказ
const canCancelOrder = computed(() => {
  if (!order.value) return false
  // Можно отменять только заказы со статусом "pending" (Ожидает обработки)
  return order.value.orderStatusName === 'pending'
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

  cancelling.value = true

  try {
    // Создаем полный объект с данными заказа для обновления
    const updateData = {
      id: order.value.id,
      address: order.value.address,
      phone: order.value.phone,
      email: order.value.email,
      customerName: order.value.customerName,
      totalAmount: order.value.totalAmount,
      accountId: order.value.accountId,
      orderStatusId: 5, // Изменяем статус на "Отменен"
      orderStatusName: 'cancelled',
      deliveryMethod: order.value.deliveryMethod,
      paymentMethod: order.value.paymentMethod,
      postalCode: order.value.postalCode,
      notes: order.value.notes || ''
    }

    console.log('Отправка данных для обновления заказа:', updateData)

    // Используем updateOrder с полными данными
    const response = await shopAPI.updateOrder(order.value.id, updateData)
    console.log('Ответ от сервера:', response)

    // Перезагружаем данные заказа
    await loadOrder()

  } catch (err: any) {
    console.error('Ошибка при отмене заказа:', err)
    console.error('Полный ответ ошибки:', err.response)

  } finally {
    cancelling.value = false
  }
}

// Связаться с поддержкой
const contactSupport = () => {
  window.open('http://t.me/danp1t', '_blank')
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
