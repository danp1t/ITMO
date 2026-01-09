import apiClient from '.'
import type { Product, ProductInfo, ProductDetail, Order, OrderStatus } from '../types/shop'

export const shopAPI = {
  getProducts() {
    return apiClient.get<Product[]>('/api/products')
  },

  getProductById(id: number) {
    return apiClient.get<ProductDetail>(`/api/products/${id}/view`)
  },

  updateProduct(id: number, data: Partial<Product>) {
    return apiClient.put<Product>(`/api/products/${id}`, data)
  },

  deleteProduct(id: number) {
    return apiClient.delete(`/api/products/${id}`)
  },

  getProductInfoByProduct(productId: number) {
    return apiClient.get<ProductInfo[]>(`/api/product-infos/product/${productId}`)
  },

  createProductInfo(data: Omit<ProductInfo, 'id'>) {
    return apiClient.post<ProductInfo>('/api/product-infos', data)
  },

  updateProductInfo(id: number, data: Partial<ProductInfo>) {
    return apiClient.put<ProductInfo>(`/api/product-infos/${id}`, data)
  },

  createOrder(data: {
    address: string;
    phone: string;
    email: string;
    customerName: string;
    totalAmount: number;
    deliveryMethod: string;
    paymentMethod: string;
    postalCode: string;
    notes: string;
    accountId: number;
    orderProducts: Array<{
      productId: number;
      productInfoId: number;
      quantity: number;
      price: number;
      size: string;
      productName: string;
    }>;
    pickupPointId?: string;
  }) {
    return apiClient.post<Order>('/api/orders', data)
  },

  getOrderById(id: number) {
    return apiClient.get<Order>(`/api/orders/${id}`)
  },

  getOrdersByAccount(accountId: number) {
    return apiClient.get<Order[]>(`/api/orders/account/${accountId}`)
  },

  getOrderStatuses() {
    return apiClient.get<OrderStatus[]>('/api/order-statuses')
  },

  updateOrder(id: number, data: Partial<Order>) {
    return apiClient.put<Order>(`/api/orders/${id}`, data)
  },

  createProductWithImages(formData: FormData) {
    return apiClient.post<Product>('/api/products/create-with-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  uploadProductImage(productId: number, formData: FormData) {
    return apiClient.post<string>(`/api/products/${productId}/images`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  getAllOrders() {
    return apiClient.get<Order[]>('/api/orders')
  },

  deleteProductImage(productId: number, imagePath: string) {
    return apiClient.delete(`/api/products/${productId}/images`, {
      params: { imagePath }
    })
  },

  getProductForEdit(id: number) {
    return apiClient.get<ProductDetail>(`/api/products/${id}/detail`)
  },
}
