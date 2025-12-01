<template>
  <div class="checkout-view">
    <div class="container">
      <!-- Заголовок -->
      <div class="has-text-centered mb-5">
        <h1 class="title is-2">Оформление заказа</h1>
        <p class="subtitle is-5 has-text-grey">
          Завершите оформление заказа, заполнив информацию ниже
        </p>
      </div>

      <!-- Основной контент -->
      <div class="columns">
        <!-- Форма заказа -->
        <div class="column is-two-thirds">
          <!-- Контактная информация -->
          <div class="box mb-4">
            <h3 class="title is-4 mb-4">Контактная информация</h3>

            <div class="columns">
              <div class="column">
                <div class="field">
                  <label class="label">Имя *</label>
                  <div class="control">
                    <input
                      v-model="form.name"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.name }"
                      placeholder="Введите ваше имя"
                      required
                    >
                  </div>
                  <p v-if="errors.name" class="help is-danger">{{ errors.name }}</p>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Телефон *</label>
                  <div class="control">
                    <input
                      v-model="form.phone"
                      type="tel"
                      class="input"
                      :class="{ 'is-danger': errors.phone }"
                      placeholder="+7 (999) 123-45-67"
                      required
                    >
                  </div>
                  <p v-if="errors.phone" class="help is-danger">{{ errors.phone }}</p>
                </div>
              </div>
            </div>

            <div class="field">
              <label class="label">Email *</label>
              <div class="control">
                <input
                  v-model="form.email"
                  type="email"
                  class="input"
                  :class="{ 'is-danger': errors.email }"
                  placeholder="email@example.com"
                  required
                >
              </div>
              <p v-if="errors.email" class="help is-danger">{{ errors.email }}</p>
            </div>
          </div>

          <!-- Адрес доставки -->
          <div class="box mb-4">
            <h3 class="title is-4 mb-4">Адрес доставки</h3>

            <div class="field">
              <label class="label">Адрес *</label>
              <div class="control">
                <textarea
                  v-model="form.address"
                  class="textarea"
                  :class="{ 'is-danger': errors.address }"
                  placeholder="Введите полный адрес доставки"
                  rows="3"
                  required
                ></textarea>
              </div>
              <p v-if="errors.address" class="help is-danger">{{ errors.address }}</p>
            </div>

            <div class="columns">
              <div class="column">
                <div class="field">
                  <label class="label">Город *</label>
                  <div class="control">
                    <input
                      v-model="form.city"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.city }"
                      placeholder="Город"
                      required
                    >
                  </div>
                  <p v-if="errors.city" class="help is-danger">{{ errors.city }}</p>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Индекс</label>
                  <div class="control">
                    <input
                      v-model="form.postalCode"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.postalCode }"
                      placeholder="Почтовый индекс"
                    >
                  </div>
                  <p v-if="errors.postalCode" class="help is-danger">{{ errors.postalCode }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Способ доставки -->
          <div class="box mb-4">
            <h3 class="title is-4 mb-4">Способ доставки</h3>

            <div class="field">
              <div class="control">
                <label class="radio mr-4">
                  <input
                    v-model="form.deliveryMethod"
                    type="radio"
                    value="courier"
                  >
                  <span class="ml-2">Курьерская доставка (300 ₽)</span>
                </label>
                <label class="radio">
                  <input
                    v-model="form.deliveryMethod"
                    type="radio"
                    value="pickup"
                  >
                  <span class="ml-2">Самовывоз (бесплатно)</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Способ оплаты -->
          <div class="box">
            <h3 class="title is-4 mb-4">Способ оплаты</h3>

            <div class="field">
              <div class="control">
                <label class="radio mr-4">
                  <input
                    v-model="form.paymentMethod"
                    type="radio"
                    value="card"
                  >
                  <span class="ml-2">Банковская карта</span>
                </label>
                <label class="radio mr-4">
                  <input
                    v-model="form.paymentMethod"
                    type="radio"
                    value="cash"
                  >
                  <span class="ml-2">Наличные при получении</span>
                </label>
                <label class="radio">
                  <input
                    v-model="form.paymentMethod"
                    type="radio"
                    value="online"
                  >
                  <span class="ml-2">Онлайн оплата</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Информация о заказе -->
        <div class="column">
          <!-- Корзина -->
          <div class="box sticky-cart">
            <h3 class="title is-4 mb-4">Ваш заказ</h3>

            <div v-if="cartStore.isEmpty" class="empty-cart">
              <p class="has-text-grey">Корзина пуста</p>
            </div>

            <div v-else>
              <!-- Товары -->
              <div class="cart-summary">
                <div
                  v-for="item in cartStore.getCartItemsWithDetails"
                  :key="`${item.productId}-${item.productInfoId}`"
                  class="cart-item-summary"
                >
                  <div class="level is-mobile">
                    <div class="level-left">
                      <div>
                        <p class="has-text-weight-semibold">{{ item.productName }}</p>
                        <p class="has-text-grey is-size-7">Размер: {{ item.size }}</p>
                      </div>
                    </div>
                    <div class="level-right">
                      <p>{{ item.quantity }} × {{ item.price }} ₽</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Итоги -->
              <div class="order-totals">
                <div class="level is-mobile">
                  <div class="level-left">
                    <span>Товары:</span>
                  </div>
                  <div class="level-right">
                    <span>{{ cartStore.totalAmount }} ₽</span>
                  </div>
                </div>

                <div class="level is-mobile">
                  <div class="level-left">
                    <span>Доставка:</span>
                  </div>
                  <div class="level-right">
                    <span>{{ deliveryCost }} ₽</span>
                  </div>
                </div>

                <hr>

                <div class="level is-mobile">
                  <div class="level-left">
                    <span class="has-text-weight-bold is-size-5">Итого:</span>
                  </div>
                  <div class="level-right">
                    <span class="has-text-weight-bold is-size-5">{{ totalWithDelivery }} ₽</span>
                  </div>
                </div>
              </div>

              <!-- Кнопка оформления -->
              <button
                class="button is-primary is-fullwidth is-large mt-4"
                :class="{ 'is-loading': isSubmitting }"
                :disabled="isSubmitting || cartStore.isEmpty"
                @click="submitOrder"
              >
                Оформить заказ
              </button>

              <!-- Ссылка на корзину -->
              <router-link
                to="/shop"
                class="button is-light is-fullwidth mt-2"
              >
                Вернуться в магазин
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useCartStore } from '../../stores/cart'
import { shopAPI } from '../../api/shop'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

// Форма заказа
const form = reactive({
  name: '',
  phone: '',
  email: '',
  address: '',
  city: '',
  postalCode: '',
  deliveryMethod: 'courier',
  paymentMethod: 'card',
  notes: ''
})

// Ошибки валидации
const errors = reactive({
  name: '',
  phone: '',
  email: '',
  address: '',
  city: '',
  postalCode: ''
})

const isSubmitting = ref(false)

// Стоимость доставки
const deliveryCost = computed(() => {
  return form.deliveryMethod === 'courier' ? 300 : 0
})

// Итоговая сумма с доставкой
const totalWithDelivery = computed(() => {
  return cartStore.totalAmount + deliveryCost.value
})

// Заполнение формы данными пользователя
const populateUserData = () => {
  if (authStore.user) {
    form.name = authStore.user.name
    form.email = authStore.user.email
    // В реальном приложении можно загружать дополнительные данные пользователя
  }
}

// Валидация формы
const validateForm = () => {
  let isValid = true

  // Сброс ошибок
  Object.keys(errors).forEach(key => {
    errors[key as keyof typeof errors] = ''
  })

  // Проверка имени
  if (!form.name.trim()) {
    errors.name = 'Имя обязательно'
    isValid = false
  }

  // Проверка телефона
  if (!form.phone.trim()) {
    errors.phone = 'Телефон обязателен'
    isValid = false
  } else if (!/^(\+7|8)[0-9]{10}$/.test(form.phone.replace(/\D/g, ''))) {
    errors.phone = 'Неверный формат телефона'
    isValid = false
  }

  // Проверка email
  if (!form.email.trim()) {
    errors.email = 'Email обязателен'
    isValid = false
  } else if (!/\S+@\S+\.\S+/.test(form.email)) {
    errors.email = 'Неверный формат email'
    isValid = false
  }

  // Проверка адреса
  if (!form.address.trim()) {
    errors.address = 'Адрес обязателен'
    isValid = false
  }

  // Проверка города
  if (!form.city.trim()) {
    errors.city = 'Город обязателен'
    isValid = false
  }

  return isValid
}

// Оформление заказа
const submitOrder = async () => {
  if (!validateForm()) {
    return
  }

  if (cartStore.isEmpty) {
    alert('Корзина пуста')
    return
  }

  isSubmitting.value = true

  try {
    const orderData = {
      address: `${form.city}, ${form.address}`,
      phone: form.phone,
      totalAmount: totalWithDelivery.value,
      accountId: authStore.user?.id || 0,
      products: cartStore.items.map(item => ({
        id: item.productId,
        productInfoId: item.productInfoId,
        quantity: item.quantity
      }))
    }

    // Отправка заказа
    const response = await shopAPI.createOrder(orderData)

    // Очищаем корзину
    cartStore.clearCart()

    // Перенаправляем на страницу подтверждения
    router.push(`/shop/orders/${response.data.id}`)

  } catch (error: any) {
    console.error('Ошибка при оформлении заказа:', error)
    const message = error.response?.data?.message || 'Произошла ошибка при оформлении заказа'
    alert(message)
  } finally {
    isSubmitting.value = false
  }
}

// Инициализация
populateUserData()
</script>

<style scoped>
.checkout-view {
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

.sticky-cart {
  position: sticky;
  top: 2rem;
}

.title {
  color: #2d3748;
}

.subtitle {
  color: #718096;
}

.field {
  margin-bottom: 1.25rem;
}

.label {
  font-weight: 600;
  color: #4b5563;
  margin-bottom: 0.5rem;
}

.input, .textarea {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 0.75rem;
  font-size: 1rem;
  transition: all 0.3s;
}

.input:focus, .textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
}

.input.is-danger, .textarea.is-danger {
  border-color: #ef4444;
}

.help.is-danger {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.radio {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
}

.radio input {
  margin-right: 0.5rem;
}

.empty-cart {
  text-align: center;
  padding: 2rem;
}

.cart-summary {
  margin-bottom: 1.5rem;
}

.cart-item-summary {
  padding: 0.75rem 0;
  border-bottom: 1px solid #f1f3f5;
}

.cart-item-summary:last-child {
  border-bottom: none;
}

.order-totals {
  padding: 1rem 0;
}

.order-totals hr {
  margin: 1rem 0;
  border-top: 1px solid #e5e7eb;
}

.button.is-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-weight: 600;
  font-size: 1.1rem;
  padding: 1rem;
}

.button.is-primary:hover {
  opacity: 0.9;
}

.button.is-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.button.is-light {
  border: 1px solid #e5e7eb;
}

@media (max-width: 768px) {
  .checkout-view {
    padding: 1rem;
  }

  .columns {
    flex-direction: column;
  }

  .sticky-cart {
    position: static;
    margin-top: 2rem;
  }
}
</style>
