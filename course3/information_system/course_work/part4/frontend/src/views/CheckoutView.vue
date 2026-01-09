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
                      ref="phoneInput"
                      v-model="phoneDisplay"
                      type="tel"
                      class="input"
                      :class="{ 'is-danger': errors.phone }"
                      placeholder="+7 (___) ___-__-__"
                      required
                      @input="formatPhone"
                      @blur="validatePhone"
                      @focus="onPhoneFocus"
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
                      placeholder="Москва"
                      required
                      list="citySuggestions"
                      @input="onCityInput"
                    >
                    <datalist id="citySuggestions">
                      <option v-for="city in citySuggestions" :key="city" :value="city"></option>
                    </datalist>
                  </div>
                  <p v-if="errors.city" class="help is-danger">{{ errors.city }}</p>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Улица *</label>
                  <div class="control">
                    <input
                      v-model="form.street"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.street }"
                      placeholder="ул. Ленина"
                      required
                      @input="onStreetInput"
                    >
                  </div>
                  <p v-if="errors.street" class="help is-danger">{{ errors.street }}</p>
                </div>
              </div>
            </div>

            <div class="columns">
              <div class="column">
                <div class="field">
                  <label class="label">Дом *</label>
                  <div class="control">
                    <input
                      v-model="form.house"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.house }"
                      placeholder="10"
                      required
                    >
                  </div>
                  <p v-if="errors.house" class="help is-danger">{{ errors.house }}</p>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Квартира / Офис</label>
                  <div class="control">
                    <input
                      v-model="form.apartment"
                      type="text"
                      class="input"
                      placeholder="25"
                    >
                  </div>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Подъезд</label>
                  <div class="control">
                    <input
                      v-model="form.entrance"
                      type="text"
                      class="input"
                      placeholder="3"
                    >
                  </div>
                </div>
              </div>

              <div class="column">
                <div class="field">
                  <label class="label">Этаж</label>
                  <div class="control">
                    <input
                      v-model="form.floor"
                      type="text"
                      class="input"
                      placeholder="5"
                    >
                  </div>
                </div>
              </div>
            </div>

            <div class="columns">
              <div class="column">
                <div class="field">
                  <label class="label">Индекс</label>
                  <div class="control">
                    <input
                      v-model="form.postalCode"
                      type="text"
                      class="input"
                      :class="{ 'is-danger': errors.postalCode }"
                      placeholder="123456"
                      maxlength="6"
                      @input="formatPostalCode"
                    >
                  </div>
                  <p v-if="errors.postalCode" class="help is-danger">{{ errors.postalCode }}</p>
                </div>
              </div>
            </div>

            <div class="field">
              <label class="label">Комментарий к доставке</label>
              <div class="control">
                <textarea
                  v-model="form.notes"
                  class="textarea"
                  placeholder="Например: домофон не работает, позвонить за 15 минут"
                  rows="2"
                ></textarea>
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
                  <span class="ml-2">Оплата картой</span>
                </label>
                <label class="radio mr-4">
                  <input
                    v-model="form.paymentMethod"
                    type="radio"
                    value="cash"
                  >
                  <span class="ml-2">Наличные при получении</span>
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
                {{ paymentButtonText }}
              </button>

              <p v-if="form.paymentMethod === 'cash'" class="has-text-centered has-text-grey is-size-7 mt-2">
                Оплата производится при получении
              </p>

              <!-- Ссылка на корзину -->
              <router-link
                to="/shop"
                class="button is-dark is-fullwidth mt-2"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { shopAPI } from '../api/shop'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const phoneInput = ref<HTMLInputElement>()

// Форма заказа
const form = reactive({
  name: '',
  phone: '',
  email: '',
  city: '',
  street: '',
  house: '',
  apartment: '',
  entrance: '',
  floor: '',
  postalCode: '',
  deliveryMethod: 'courier',
  paymentMethod: 'card',
  notes: ''
})

// Отображение телефона с маской
const phoneDisplay = ref('+7 ')

// Подсказки для городов
const citySuggestions = ref([
  'Москва', 'Санкт-Петербург', 'Новосибирск', 'Екатеринбург', 'Казань',
  'Нижний Новгород', 'Челябинск', 'Самара', 'Омск', 'Ростов-на-Дону'
])

// Ошибки валидации
const errors = reactive({
  name: '',
  phone: '',
  email: '',
  city: '',
  street: '',
  house: '',
  postalCode: ''
})

const isSubmitting = ref(false)

// Computed свойства
const deliveryCost = computed(() => {
  return form.deliveryMethod === 'courier' ? 300 : 0
})

const totalWithDelivery = computed(() => {
  return cartStore.totalAmount + deliveryCost.value
})

const paymentButtonText = computed(() => {
  return 'Оформить заказ'
})

// Маска для телефона
const formatPhone = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/\D/g, '')

  if (value.startsWith('7') || value.startsWith('8')) {
    value = value.substring(1)
  }

  if (value.length > 10) {
    value = value.substring(0, 10)
  }

  let formattedValue = '+7 '

  if (value.length > 0) {
    formattedValue += '(' + value.substring(0, 3)
  }
  if (value.length >= 4) {
    formattedValue += ') ' + value.substring(3, 6)
  }
  if (value.length >= 7) {
    formattedValue += '-' + value.substring(6, 8)
  }
  if (value.length >= 9) {
    formattedValue += '-' + value.substring(8, 10)
  }

  phoneDisplay.value = formattedValue
  form.phone = '+7' + value
}

const onPhoneFocus = () => {
  if (!phoneDisplay.value || phoneDisplay.value === '+7 ') {
    phoneDisplay.value = '+7 ('
  }
}

const validatePhone = () => {
  const phoneDigits = form.phone.replace(/\D/g, '')
  if (phoneDigits.length !== 11) {
    errors.phone = 'Введите полный номер телефона'
    return false
  }
  errors.phone = ''
  return true
}

// Форматирование почтового индекса
const formatPostalCode = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/\D/g, '')

  if (value.length > 6) {
    value = value.substring(0, 6)
  }

  form.postalCode = value
  errors.postalCode = value.length !== 6 && value.length > 0 ? 'Индекс должен содержать 6 цифр' : ''
}

// Автодополнение города
const onCityInput = () => {
  // В реальном приложении здесь будет запрос к API
}

// Автодополнение улицы
const onStreetInput = () => {
  // В реальном приложении здесь будет запрос к API
}

// Заполнение формы данными пользователя
const populateUserData = () => {
  if (authStore.user) {
    form.name = authStore.user.name
    form.email = authStore.user.email

    // Если у пользователя есть сохраненные данные, можно подставить их
    const savedAddress = localStorage.getItem('user_address')
    if (savedAddress) {
      try {
        const address = JSON.parse(savedAddress)
        Object.assign(form, address)
      } catch (e) {
        console.error('Error parsing saved address', e)
      }
    }
  }
}

// Сохранение адреса пользователя
const saveUserAddress = () => {
  const addressData = {
    city: form.city,
    street: form.street,
    house: form.house,
    apartment: form.apartment,
    entrance: form.entrance,
    floor: form.floor,
    postalCode: form.postalCode
  }

  localStorage.setItem('user_address', JSON.stringify(addressData))
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
  if (!validatePhone()) {
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

  // Проверка города
  if (!form.city.trim()) {
    errors.city = 'Город обязателен'
    isValid = false
  }

  // Проверка улицы (только для доставки курьером)
  if (form.deliveryMethod === 'courier' && !form.street.trim()) {
    errors.street = 'Улица обязательна'
    isValid = false
  }

  // Проверка дома (только для доставки курьером)
  if (form.deliveryMethod === 'courier' && !form.house.trim()) {
    errors.house = 'Номер дома обязателен'
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
    // Собираем адрес из компонентов
    let address = `${form.city}, ${form.street}, д. ${form.house}`
    if (form.apartment) address += `, кв. ${form.apartment}`
    if (form.entrance) address += `, подъезд ${form.entrance}`
    if (form.floor) address += `, этаж ${form.floor}`

    // Создаем массив товаров с информацией из ProductInfo
    const orderProducts = cartStore.items.map(item => ({
      productId: item.productId,
      productInfoId: item.productInfoId,
      quantity: item.quantity,
      price: item.price,
      size: item.size,
      productName: item.productName
    }))

    const orderData = {
      address: address,
      phone: form.phone,
      email: form.email,
      customerName: form.name,
      totalAmount: totalWithDelivery.value,
      deliveryMethod: form.deliveryMethod,
      paymentMethod: form.paymentMethod,
      postalCode: form.postalCode,
      notes: form.notes,
      accountId: authStore.user?.id || 0,
      orderProducts: orderProducts
    }

    // Отправка заказа
    const response = await shopAPI.createOrder(orderData)

    // Сохраняем адрес пользователя
    saveUserAddress()

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
onMounted(() => {
  populateUserData()
})
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
  margin-bottom: 1.5rem;
}

.sticky-cart {
  position: sticky;
  top: 2rem;
}

.title {
  color: #bac1c8;
}

.subtitle {
  color: #c4cad3;
}

.field {
  margin-bottom: 1.25rem;
}

.label {
  font-weight: 600;
  color: #8f949f;
  margin-bottom: 0.5rem;
}

.input, .textarea, .select select {
  border: 2px solid #394451;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  transition: all 0.3s;
}

.input:focus, .textarea:focus, .select select:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  outline: none;
}

.input.is-danger, .textarea.is-danger {
  border-color: #f56565;
}

.help.is-danger {
  color: #f56565;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.radio {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.radio input {
  margin-right: 0.5rem;
}

.cart-summary {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
  padding-right: 0.5rem;
}

.cart-item-summary {
  padding: 0.75rem 0;
  border-bottom: 1px solid #e2e8f0;
}

.cart-item-summary:last-child {
  border-bottom: none;
}

.order-totals {
  background-color: #202022;
  border-radius: 8px;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
}

.order-totals .level {
  margin-bottom: 0.75rem;
}

.order-totals hr {
  margin: 1rem 0;
  border-color: #e2e8f0;
}

@media (max-width: 768px) {
  .checkout-view {
    padding: 1rem;
  }

  .columns {
    flex-direction: column;
  }

  .column.is-two-thirds {
    width: 100%;
  }

  .sticky-cart {
    position: static;
    margin-top: 1.5rem;
  }

  .radio {
    margin-bottom: 0.75rem;
  }
}

/* Прокрутка для корзины */
.cart-summary::-webkit-scrollbar {
  width: 6px;
}

.cart-summary::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.cart-summary::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.cart-summary::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Анимация появления */
.box {
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
