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

    <div v-if="loading" class="has-text-centered py-6">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p class="mt-3">Загрузка информации о заказе...</p>
    </div>

    <div v-else-if="error" class="notification is-danger">
      {{ error }}
      <button class="button is-light ml-2" @click="loadOrder">
        Повторить
      </button>
    </div>

    <div v-else-if="order" class="columns">
      <div class="column is-8">
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
          </div>

          <div class="field">
            <label class="label">Телефон</label>
            <div class="control">
              <input
                v-model="editForm.phone"
                type="tel"
                class="input"
                maxlength="100"
                @input="truncateField('phone', 100)"
              >
              <p class="help">Максимум 100 символов</p>
            </div>
          </div>

          <div class="field">
            <label class="label">Адрес доставки</label>
            <div class="control">
              <textarea
                v-model="editForm.address"
                class="textarea"
                rows="3"
                maxlength="100"
                @input="truncateField('address', 100)"
              ></textarea>
              <p class="help">Максимум 100 символов</p>
            </div>
          </div>
        </div>

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
              <template v-if="order.orderProducts && order.orderProducts.length > 0">
                <tr v-for="item in order.orderProducts" :key="`${item.productId}-${item.productInfoId}`">
                  <td>
                    <div class="product-info">
                      <div class="product-name">{{ item.productName }}</div>
                      <div v-if="getProductById(item.productId)" class="product-description has-text-grey is-size-7">
                        {{ truncateDescription(getProductById(item.productId)?.description || '', 50) }}
                      </div>
                    </div>
                  </td>
                  <td>{{ item.size }}</td>
                  <td>{{ item.price }} ₽</td>
                  <td>{{ item.quantity }}</td>
                  <td>{{ item.price * item.quantity }} ₽</td>
                </tr>
              </template>
              <template v-else>
                <tr v-for="product in order.products" :key="product.id">
                  <td>
                    <div class="product-info">
                      <div class="product-name">{{ product.name }}</div>
                      <div class="product-description has-text-grey is-size-7">
                        {{ truncateDescription(product.description, 50) }}
                      </div>
                    </div>
                  </td>
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
      </div>

      <div class="column is-4">
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
              <strong>Email:</strong> {{ order.email }}
            </p>
            <p>
              <strong>Имя получателя:</strong> {{ order.customerName }}
            </p>
            <p>
              <strong>Дата создания заказа:</strong> {{ formatDate(order.createdAt) }}
            </p>
            <p v-if="order.updatedAt">
              <strong>Последнее обновление:</strong> {{ formatDate(order.updatedAt) }}
            </p>
          </div>
        </div>

        <div class="box mb-4">
          <h3 class="title is-5 mb-3">Детали доставки и оплаты</h3>
          <div class="content">
            <p><strong>Способ доставки:</strong> {{ getDeliveryMethodText(order.deliveryMethod) }}</p>
            <p><strong>Способ оплаты:</strong> {{ getPaymentMethodText(order.paymentMethod) }}</p>
            <p v-if="order.postalCode"><strong>Почтовый индекс:</strong> {{ order.postalCode }}</p>
            <p v-if="order.notes"><strong>Комментарий:</strong> {{ order.notes }}</p>
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

const editForm = reactive({
  orderStatusId: 0,
  totalAmount: 0,
  phone: '',
  address: ''
})

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

const getProductById = computed(() => {
  return (productId: number) => {
    if (!order.value?.products) return null
    return order.value.products.find(p => p.id === productId)
  }
})

const hasChanges = computed(() => {
  if (!order.value) return false
  return editForm.orderStatusId !== order.value.orderStatusId ||
    editForm.totalAmount !== order.value.totalAmount ||
    editForm.phone !== order.value.phone ||
    editForm.address !== order.value.address
})

const truncateField = (field: keyof typeof editForm, maxLength: number) => {
  if (typeof editForm[field] === 'string' && editForm[field].length > maxLength) {
    editForm[field] = editForm[field].substring(0, maxLength)
  }
}

const loadOrder = async () => {
  loading.value = true
  error.value = ''

  try {
    const response = await shopAPI.getOrderById(orderId.value)
    order.value = response.data

    const statusesResponse = await shopAPI.getOrderStatuses()
    orderStatuses.value = statusesResponse.data

    if (order.value) {
      editForm.orderStatusId = order.value.orderStatusId
      editForm.totalAmount = order.value.totalAmount
      editForm.phone = order.value.phone || ''
      editForm.address = order.value.address || ''

      // Обрезаем значения при загрузке, если они превышают лимит
      if (editForm.phone.length > 100) editForm.phone = editForm.phone.substring(0, 100)
      if (editForm.address.length > 100) editForm.address = editForm.address.substring(0, 100)
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Не удалось загрузить информацию о заказе'
    console.error('Ошибка при загрузке заказа:', err)
  } finally {
    loading.value = false
  }
}

const saveChanges = async () => {
  if (!order.value || !hasChanges.value) return

  saving.value = true
  try {
    const dataToSend = {
      orderStatusId: editForm.orderStatusId,
      totalAmount: editForm.totalAmount,
      phone: editForm.phone.substring(0, 100),
      address: editForm.address.substring(0, 100)
    }

    await shopAPI.updateOrder(order.value.id, dataToSend)

    if (order.value) {
      order.value.orderStatusId = editForm.orderStatusId
      order.value.totalAmount = editForm.totalAmount
      order.value.phone = editForm.phone
      order.value.address = editForm.address
    }

  } catch (err: any) {
    console.error('Ошибка при сохранении:', err)
  } finally {
    saving.value = false
  }
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

const truncateDescription = (text: string, maxLength: number) => {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const getDeliveryMethodText = (method: string) => {
  switch (method) {
    case 'courier': return 'Курьерская доставка'
    case 'pickup': return 'Самовывоз'
    default: return method
  }
}

const getPaymentMethodText = (method: string) => {
  switch (method) {
    case 'card': return 'Банковская карта'
    case 'cash': return 'Наличные при получении'
    case 'online': return 'Онлайн оплата'
    default: return method
  }
}

onMounted(() => {
  loadOrder()
})

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

.help {
  color: #b5b5b5;
  font-size: 0.85em;
  margin-top: 0.25em;
}
</style>
