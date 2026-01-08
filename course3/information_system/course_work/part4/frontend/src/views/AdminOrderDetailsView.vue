<template>
  <div class="admin-order-details-view dark-theme">
    <div class="content-header">
      <h1 class="title">Заказ #{{ orderId }}</h1>
      <div class="content-actions">
        <button class="button is-light" @click="router.push('/admin/orders')">
          <i class="fas fa-arrow-left mr-2"></i>
          Назад к заказам
        </button>
        <button class="button is-primary ml-2" @click="saveChanges" :disabled="!hasChanges">
          <i class="fas fa-save mr-2"></i>
          Сохранить изменения
        </button>
      </div>
    </div>

    <!-- Загрузка -->
    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка информации о заказе...</p>
    </div>

    <!-- Ошибка -->
    <div v-else-if="error" class="notification is-danger">
      {{ error }}
      <button class="button is-light ml-2" @click="loadOrder">
        Повторить
      </button>
    </div>

    <!-- Информация о заказе -->
    <div v-else-if="order" class="columns">
      <!-- Основная информация -->
      <div class="column is-8">
        <!-- Форма редактирования -->
        <div class="box mb-4">
          <h3 class="title is-4 mb-4">Редактирование заказа</h3>

          <div class="columns">
            <div class="column">
              <div class="field">
                <label class="label">Статус заказа</label>
                <div class="control">
                  <div class="select is-fullwidth">
                    <select v-model="editForm.orderStatusId">
                      <option v-for="status in orderStatuses" :key="status.id" :value="status.id">
                        {{ status.name }}
                      </option>
                    </select>
                  </div>
                </div>
              </div>
            </div>
            <div class="column">
              <div class="field">
                <label class="label">Общая сумма</label>
                <div class="control">
                  <input v-model="editForm.totalAmount" type="number" class="input">
                </div>
              </div>
            </div>
          </div>

          <div class="field">
            <label class="label">Телефон</label>
            <div class="control">
              <input v-model="editForm.phone" type="tel" class="input">
            </div>
          </div>

          <div class="field">
            <label class="label">Адрес доставки</label>
            <div class="control">
              <textarea v-model="editForm.address" class="textarea" rows="3"></textarea>
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
                      {{ truncateDescription(product.description, 50) }}
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
      </div>

      <!-- Боковая панель -->
      <div class="column is-4">
        <!-- Информация о пользователе -->
        <div class="box mb-4">
          <h3 class="title is-5 mb-3">Информация о пользователе</h3>
          <div class="content">
            <p v-if="order.accountName">
              <strong>Имя:</strong> {{ order.accountName }}
            </p>
            <p>
              <strong>ID аккаунта:</strong> {{ order.accountId }}
            </p>
            <p>
              <strong>Дата создания заказа:</strong> {{ formatDate(order.createdAt) }}
            </p>
            <p v-if="order.updatedAt">
              <strong>Последнее обновление:</strong> {{ formatDate(order.updatedAt) }}
            </p>
          </div>
        </div>

        <!-- Действия -->
        <div class="box">
          <h3 class="title is-5 mb-3">Действия</h3>
          <div class="buttons are-small is-flex-direction-column">
            <button
              v-if="authStore.canManageRoles && order.orderStatusId !== 5"
              class="button is-danger is-fullwidth mb-2"
              @click="cancelOrder"
            >
              <i class="fas fa-times mr-2"></i>
              Отменить заказ
            </button>

            <button
              class="button is-info is-light is-fullwidth mb-2"
              @click="exportOrder"
            >
              <i class="fas fa-file-export mr-2"></i>
              Экспорт в PDF
            </button>

            <button
              class="button is-warning is-light is-fullwidth"
              @click="sendNotification"
            >
              <i class="fas fa-bell mr-2"></i>
              Уведомить пользователя
            </button>
          </div>
        </div>

        <!-- История изменений -->
        <div class="box">
          <h3 class="title is-5 mb-3">История статусов</h3>
          <div class="timeline">
            <div class="timeline-item">
              <div class="timeline-marker is-primary">
                <i class="fas fa-shopping-cart"></i>
              </div>
              <div class="timeline-content">
                <p class="heading">{{ formatDate(order.createdAt) }}</p>
                <p>Заказ создан</p>
              </div>
            </div>
            <!-- Здесь можно добавить больше событий из истории -->
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { shopAPI } from '@/api/shop'
import type { Order, OrderStatus } from '@/types/shop'
import {useAuthStore} from "@/stores/auth.ts";

const route = useRoute()
const router = useRouter()

const orderId = ref(parseInt(route.params.id as string))
const order = ref<Order | null>(null)
const orderStatuses = ref<OrderStatus[]>([])
const loading = ref(false)
const error = ref('')
const saving = ref(false)

// Форма редактирования
const editForm = reactive({
  orderStatusId: 0,
  totalAmount: 0,
  phone: '',
  address: ''
})

// Проверка изменений
const hasChanges = computed(() => {
  if (!order.value) return false
  return editForm.orderStatusId !== order.value.orderStatusId ||
    editForm.totalAmount !== order.value.totalAmount ||
    editForm.phone !== order.value.phone ||
    editForm.address !== order.value.address
})

// Загрузка заказа
const loadOrder = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await shopAPI.getOrderById(orderId.value)
    order.value = response.data

    // Загружаем статусы заказов
    const statusesResponse = await shopAPI.getOrderStatuses()
    orderStatuses.value = statusesResponse.data

    // Инициализируем форму
    if (order.value) {
      editForm.orderStatusId = order.value.orderStatusId
      editForm.totalAmount = order.value.totalAmount
      editForm.phone = order.value.phone || ''
      editForm.address = order.value.address || ''
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Не удалось загрузить информацию о заказе'
    console.error('Ошибка при загрузке заказа:', err)
  } finally {
    loading.value = false
  }
}

const authStore = useAuthStore()

// Сохранение изменений
const saveChanges = async () => {
  if (!order.value || !hasChanges.value) return

  saving.value = true
  try {
    await shopAPI.updateOrder(order.value.id, {
      orderStatusId: editForm.orderStatusId,
      totalAmount: editForm.totalAmount,
      phone: editForm.phone,
      address: editForm.address
    })

    // Обновляем локальные данные
    if (order.value) {
      order.value.orderStatusId = editForm.orderStatusId
      order.value.totalAmount = editForm.totalAmount
      order.value.phone = editForm.phone
      order.value.address = editForm.address
    }

  } catch (err: any) {
    const message = err.response?.data?.message || 'Не удалось сохранить изменения'
    console.error('Ошибка при сохранении:', err)
  } finally {
    saving.value = false
  }
}

// Отмена заказа
const cancelOrder = async () => {
  if (!order.value || !confirm('Вы уверены, что хотите отменить этот заказ?')) {
    return
  }

  try {
    await shopAPI.updateOrder(order.value.id, { orderStatusId: 5 })
    order.value.orderStatusId = 5
    editForm.orderStatusId = 5
  } catch (err: any) {
    const message = err.response?.data?.message || 'Не удалось отменить заказ'
  }
}

// Экспорт заказа
const exportOrder = () => {
  // В реальном приложении здесь будет запрос на генерацию PDF
  alert('Функция экспорта в разработке')
}

// Уведомление пользователя
const sendNotification = () => {
  if (!order.value) return
  const message = `Статус вашего заказа #${order.value.id} изменен. Проверьте в личном кабинете.`
  alert(`Уведомление отправлено пользователю:\n${message}`)
}

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
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// Инициализация
onMounted(() => {
  loadOrder()
})

// Отслеживаем изменения ID заказа в маршруте
watch(() => route.params.id, (newId) => {
  orderId.value = parseInt(newId as string)
  loadOrder()
})
</script>

<style scoped>
.admin-order-details-view {
  padding: 20px;
}

.box {
  background-color: #1a1a1a;
  border: 1px solid #333;
}

.label {
  color: #fff;
}

.input, .textarea, .select select {
  background-color: #2d2d2d;
  border-color: #444;
  color: #fff;
}

.input:focus, .textarea:focus {
  border-color: #4a00e0;
  box-shadow: 0 0 0 0.125em rgba(74, 0, 224, 0.25);
}

.table {
  background-color: transparent;
}

.table th {
  background-color: #1a1a1a;
  color: #fff;
  border-bottom: 2px solid #333;
}

.table td {
  border-bottom: 1px solid #333;
}

.timeline {
  border-left: 2px solid #4a00e0;
  padding-left: 20px;
}

.timeline-item {
  position: relative;
  margin-bottom: 20px;
}

.timeline-marker {
  position: absolute;
  left: -28px;
  top: 0;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #4a00e0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.timeline-content {
  padding-left: 10px;
}

.heading {
  font-size: 0.875rem;
  color: #aaa;
  margin-bottom: 5px;
}
</style>
