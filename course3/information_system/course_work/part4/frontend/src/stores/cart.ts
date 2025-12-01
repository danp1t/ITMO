import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Product, ProductInfo, CartItem } from '../types/shop'

export const useCartStore = defineStore('cart', () => {
  // Корзина - массив товаров
  const items = ref<CartItem[]>([])

  // Загрузка корзины из localStorage
  const loadCartFromStorage = () => {
    const savedCart = localStorage.getItem('cart')
    if (savedCart) {
      items.value = JSON.parse(savedCart)
    }
  }

  // Сохранение корзины в localStorage
  const saveCartToStorage = () => {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  // Инициализация
  loadCartFromStorage()

  // Общее количество товаров в корзине
  const totalItems = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  // Общая стоимость корзины
  const totalAmount = computed(() => {
    return items.value.reduce((total, item) => total + (item.price * item.quantity), 0)
  })

  // Проверка, пуста ли корзина
  const isEmpty = computed(() => items.value.length === 0)

  // Добавление товара в корзину
  const addItem = (product: Product, productInfo: ProductInfo, quantity: number = 1) => {
    const existingItemIndex = items.value.findIndex(
      item => item.productId === product.id && item.productInfoId === productInfo.id
    )

    if (existingItemIndex !== -1) {
      // Увеличиваем количество существующего товара
      items.value[existingItemIndex].quantity += quantity
    } else {
      // Добавляем новый товар
      items.value.push({
        productId: product.id,
        productInfoId: productInfo.id,
        quantity,
        size: productInfo.sizeName,
        price: productInfo.price,
        productName: product.name
      })
    }

    saveCartToStorage()
  }

  // Удаление товара из корзины
  const removeItem = (productId: number, productInfoId: number) => {
    const index = items.value.findIndex(
      item => item.productId === productId && item.productInfoId === productInfoId
    )

    if (index !== -1) {
      items.value.splice(index, 1)
      saveCartToStorage()
    }
  }

  // Изменение количества товара
  const updateQuantity = (productId: number, productInfoId: number, quantity: number) => {
    const item = items.value.find(
      item => item.productId === productId && item.productInfoId === productInfoId
    )

    if (item) {
      if (quantity <= 0) {
        removeItem(productId, productInfoId)
      } else {
        item.quantity = quantity
        saveCartToStorage()
      }
    }
  }

  // Очистка корзины
  const clearCart = () => {
    items.value = []
    localStorage.removeItem('cart')
  }

  // Получение товаров из корзины с дополнительной информацией
  const getCartItemsWithDetails = computed(() => {
    // В реальном приложении здесь можно было бы загружать детали товаров из API
    return items.value.map(item => ({
      ...item,
      subtotal: item.price * item.quantity
    }))
  })

  return {
    items,
    totalItems,
    totalAmount,
    isEmpty,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    getCartItemsWithDetails,
    loadCartFromStorage,
    saveCartToStorage
  }
})
