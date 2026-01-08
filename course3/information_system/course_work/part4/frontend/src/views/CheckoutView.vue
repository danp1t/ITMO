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

            <!-- Пункты самовывоза -->
            <div v-if="form.deliveryMethod === 'pickup'" class="mt-4">
              <div class="field">
                <label class="label">Выберите пункт выдачи</label>
                <div class="control">
                  <div class="select is-fullwidth">
                    <select v-model="form.pickupPointId">
                      <option value="">Выберите пункт выдачи</option>
                      <option v-for="point in pickupPoints" :key="point.id" :value="point.id">
                        {{ point.address }} ({{ point.workHours }})
                      </option>
                    </select>
                  </div>
                </div>
                <p v-if="errors.pickupPointId" class="help is-danger">{{ errors.pickupPointId }}</p>
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

            <!-- Карта для оплаты -->
            <div v-if="form.paymentMethod === 'card'" class="mt-4">
              <div class="field">
                <label class="label">Номер карты</label>
                <div class="control">
                  <input
                    v-model="form.cardNumber"
                    type="text"
                    class="input"
                    placeholder="1234 5678 9012 3456"
                    maxlength="19"
                    @input="formatCardNumber"
                  >
                </div>
              </div>

              <div class="columns">
                <div class="column">
                  <div class="field">
                    <label class="label">Срок действия (ММ/ГГ)</label>
                    <div class="control">
                      <input
                        v-model="form.cardExpiry"
                        type="text"
                        class="input"
                        placeholder="MM/YY"
                        maxlength="5"
                        @input="formatCardExpiry"
                      >
                    </div>
                  </div>
                </div>

                <div class="column">
                  <div class="field">
                    <label class="label">CVV/CVC</label>
                    <div class="control">
                      <input
                        v-model="form.cardCvv"
                        type="password"
                        class="input"
                        placeholder="123"
                        maxlength="3"
                      >
                    </div>
                  </div>
                </div>
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
  pickupPointId: '',
  paymentMethod: 'card',
  cardNumber: '',
  cardExpiry: '',
  cardCvv: '',
  notes: ''
})

// Отображение телефона с маской
const phoneDisplay = ref('+7 ')

// Подсказки для городов
const citySuggestions = ref([
  'Москва', 'Санкт-Петербург', 'Новосибирск', 'Екатеринбург', 'Казань',
  'Нижний Новгород', 'Челябинск', 'Самара', 'Омск', 'Ростов-на-Дону'
])

// Пункты самовывоза
const pickupPoints = ref([
  { id: 1, address: 'ул. Тверская, 10', workHours: '10:00-22:00' },
  { id: 2, address: 'пр. Мира, 25', workHours: '9:00-21:00' },
  { id: 3, address: 'ул. Арбат, 15', workHours: '11:00-23:00' }
])

// Ошибки валидации
const errors = reactive({
  name: '',
  phone: '',
  email: '',
  city: '',
  street: '',
  house: '',
  postalCode: '',
  pickupPointId: ''
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
  if (form.paymentMethod === 'card' || form.paymentMethod === 'online') {
    return 'Оплатить заказ'
  }
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
}

// Форматирование номера карты
const formatCardNumber = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/\D/g, '')

  if (value.length > 16) {
    value = value.substring(0, 16)
  }

  // Добавляем пробелы каждые 4 цифры
  const formatted = value.replace(/(\d{4})(?=\d)/g, '$1 ')
  form.cardNumber = formatted
}

// Форматирование срока действия карты
const formatCardExpiry = (event: Event) => {
  const input = event.target as HTMLInputElement
  let value = input.value.replace(/\D/g, '')

  if (value.length > 4) {
    value = value.substring(0, 4)
  }

  if (value.length >= 2) {
    value = value.substring(0, 2) + '/' + value.substring(2)
  }

  form.cardExpiry = value
}

// Автодополнение города
const onCityInput = () => {
  // В реальном приложении здесь будет запрос к API
  const input = document.getElementById('citySuggestions') as HTMLDataListElement
  // Можно добавить debounce и запрос к сервису подсказок
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

  // Проверка пункта самовывоза
  if (form.deliveryMethod === 'pickup' && !form.pickupPointId) {
    errors.pickupPointId = 'Выберите пункт выдачи'
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
      products: cartStore.items.map(item => ({
        id: item.productId,
        productInfoId: item.productInfoId,
        quantity: item.quantity
      }))
    }

    // Если выбран самовывоз, добавляем ID пункта
    if (form.deliveryMethod === 'pickup') {
      orderData.pickupPointId = form.pickupPointId
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
/* Существующие стили остаются, добавляем новые */

/* Маска телефона */
input[type="tel"] {
  font-family: 'Courier New', monospace;
  font-size: 1.1em;
  letter-spacing: 0.5px;
}

/* Анимация при фокусе */
.input:focus, .textarea:focus {
  transform: translateY(-2px);
  transition: all 0.3s ease;
}

/* Стили для даталиста */
datalist {
  position: absolute;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 1000;
}

datalist option {
  padding: 0.5rem 1rem;
  cursor: pointer;
}

datalist option:hover {
  background-color: #f3f4f6;
}

/* Стили для карточки оплаты */
.card-input-group {
  position: relative;
}

.card-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  background-size: contain;
  background-repeat: no-repeat;
}

/* Стили для пунктов самовывоза */
.pickup-point {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.3s;
}

.pickup-point:hover {
  border-color: #667eea;
  background-color: #f8fafc;
}

.pickup-point.selected {
  border-color: #667eea;
  background-color: #eff6ff;
}

.pickup-point-title {
  font-weight: 600;
  color: #1f2937;
}

.pickup-point-address {
  color: #6b7280;
  font-size: 0.9em;
}

/* Адаптивность для мобильных устройств */
@media (max-width: 768px) {
  .columns.is-multiline-mobile {
    flex-wrap: wrap;
  }

  .column.is-quarter-mobile {
    flex: none;
    width: 50%;
  }

  .sticky-cart {
    position: static;
    margin-top: 2rem;
  }
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

/* Валидация в реальном времени */
.input.is-danger {
  animation: shake 0.5s;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}

/* Подсказки для полей */
.field-hint {
  font-size: 0.8rem;
  color: #6b7280;
  margin-top: 0.25rem;
}

/* Иконки в полях ввода */
.field-with-icon {
  position: relative;
}

.field-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.field-with-icon .input {
  padding-left: 40px;
}
</style>
