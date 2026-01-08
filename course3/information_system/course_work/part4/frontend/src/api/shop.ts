import apiClient from '.'
import type { Product, ProductInfo, ProductDetail, Order } from '../types/shop'

export const shopAPI = {
  // Получить все товары
  getProducts() {
    return apiClient.get<Product[]>('/api/products')
  },

  // Поиск товаров по названию
  searchProducts(name: string) {
    return apiClient.get<Product[]>(`/api/products/search?name=${name}`)
  },

  // Фильтрация товаров
  getFilteredProducts(params: {
    category?: string
    size?: string
    sortBy?: string
    sortOrder?: string
  }) {
    return apiClient.get<Product[]>('/api/products/filtered', { params })
  },

  // Получить товар по ID
  getProductById(id: number) {
    return apiClient.get<ProductDetail>(`/api/products/${id}/view`)
  },

  // Проверить наличие товара
  checkAvailability(id: number) {
    return apiClient.get(`/api/products/${id}/availability`)
  },

  // Детальная проверка наличия
  checkDetailedAvailability(id: number, size: string, quantity: number) {
    return apiClient.get(`/api/products/${id}/available`, {
      params: { size, quantity },
    })
  },

  // Создать товар
  createProduct(data: Omit<Product, 'id'>) {
    return apiClient.post<Product>('/api/products', data)
  },

  // Обновить товар
  updateProduct(id: number, data: Partial<Product>) {
    return apiClient.put<Product>(`/api/products/${id}`, data)
  },

  // Удалить товар
  deleteProduct(id: number) {
    return apiClient.delete(`/api/products/${id}`)
  },

  // Получить остатки товаров
  getStock() {
    return apiClient.get<ProductInfo[]>('/api/products/stock')
  },

  // Получить информацию о товаре по ID
  getProductInfoById(id: number) {
    return apiClient.get<ProductInfo>(`/api/product-infos/${id}`)
  },

  // Получить информацию о товаре по размеру
  getProductInfoBySize(sizeName: string) {
    return apiClient.get<ProductInfo[]>(`/api/product-infos/size/${sizeName}`)
  },

  // Получить информацию о товаре по продукту
  getProductInfoByProduct(productId: number) {
    return apiClient.get<ProductInfo[]>(`/api/product-infos/product/${productId}`)
  },

  // Создать информацию о товаре
  createProductInfo(data: Omit<ProductInfo, 'id'>) {
    return apiClient.post<ProductInfo>('/api/product-infos', data)
  },

  // Обновить информацию о товаре
  updateProductInfo(id: number, data: Partial<ProductInfo>) {
    return apiClient.put<ProductInfo>(`/api/product-infos/${id}`, data)
  },

  // Создать заказ
  createOrder(data: Omit<Order, 'id' | 'createdAt'>) {
    return apiClient.post<Order>('/api/orders', data)
  },

  // Получить заказ по ID
  getOrderById(id: number) {
    return apiClient.get<Order>(`/api/orders/${id}`)
  },

  // Получить заказы пользователя
  getOrdersByAccount(accountId: number) {
    return apiClient.get<Order[]>(`/api/orders/account/${accountId}`)
  },

  // Получить заказы по статусу
  getOrdersByStatus(statusId: number) {
    return apiClient.get<Order[]>(`/api/orders/status/${statusId}`)
  },

  // Обновить заказ
  updateOrder(id: number, data: Partial<Order>) {
    return apiClient.put<Order>(`/api/orders/${id}`, data)
  },

  // Создать товар с изображениями и размерами
  createProductWithImages(formData: FormData) {
    return apiClient.post<Product>('/api/products/create-with-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Загрузить изображение для товара
  uploadProductImage(productId: number, formData: FormData) {
    return apiClient.post<string>(`/api/products/${productId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Удалить изображение товара
  deleteProductImage(productId: number, imagePath: string) {
    return apiClient.delete(`/api/products/${productId}/images`, {
      params: { imagePath }
    })
  },

  // Получить изображение товара
  getProductImage(path: string) {
    return apiClient.get(`/api/products/images/${path}`, {
      responseType: 'blob'
    })
  },

  // Удалить заказ
  deleteOrder(id: number) {
    return apiClient.delete(`/api/orders/${id}`)
  },
}
