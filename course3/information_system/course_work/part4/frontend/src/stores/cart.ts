import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Product, ProductInfo, CartItem } from '../types/shop'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const loadCartFromStorage = () => {
    const savedCart = localStorage.getItem('cart')
    if (savedCart) {
      items.value = JSON.parse(savedCart)
    }
  }

  const saveCartToStorage = () => {
    localStorage.setItem('cart', JSON.stringify(items.value))
  }

  loadCartFromStorage()

  const totalItems = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  const totalAmount = computed(() => {
    return items.value.reduce((total, item) => total + (item.price * item.quantity), 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  const addItem = (product: Product, productInfo: ProductInfo, quantity: number = 1) => {
    const existingItemIndex = items.value.findIndex(
      item => item.productId === product.id && item.productInfoId === productInfo.id
    )

    if (existingItemIndex !== -1) {
      items.value[existingItemIndex].quantity += quantity
    } else {
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

  const removeItem = (productId: number, productInfoId: number) => {
    const index = items.value.findIndex(
      item => item.productId === productId && item.productInfoId === productInfoId
    )

    if (index !== -1) {
      items.value.splice(index, 1)
      saveCartToStorage()
    }
  }

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

  const clearCart = () => {
    items.value = []
    localStorage.removeItem('cart')
  }

  const getCartItemsWithDetails = computed(() => {
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
