export interface Product {
  id: number
  name: string
  description: string
  category: string
  basePrice: number
  popularity: number
  images?: string[]
  createdAt?: string
  updatedAt?: string
}

export interface ProductInfo {
  id: number
  productId: number
  sizeName: string
  countItems: number
  price: number
  product?: Product // Вложенный продукт для удобства
}

export interface ProductDetail extends Product {
  productInfos: ProductInfo[]
}

export interface CartItem {
  productId: number
  productInfoId: number
  quantity: number
  size: string
  price: number
  productName: string
}

export interface Order {
  id: number
  address: string
  phone: string
  totalAmount: number
  accountId: number
  orderStatusId: number
  orderStatus?: OrderStatus
  createdAt: string
  updatedAt?: string
  products: Product[]
  productInfos?: ProductInfo[] // Детали по размерам и количеству
}

export interface OrderStatus {
  id: number
  name: string
  description?: string
}

export interface CreateOrderData {
  address: string
  phone: string
  totalAmount: number
  accountId: number
  products: Array<{
    id: number
    productInfoId?: number
    quantity: number
  }>
}

export interface UpdateOrderData {
  address?: string
  phone?: string
  totalAmount?: number
  orderStatusId?: number
}

export interface CreateProductData {
  name: string
  description: string
  category: string
  basePrice: number
  popularity?: number
}

export interface UpdateProductData {
  name?: string
  description?: string
  category?: string
  basePrice?: number
  popularity?: number
}

export interface CreateProductInfoData {
  productId: number
  sizeName: string
  countItems: number
  price: number
}

export interface UpdateProductInfoData {
  sizeName?: string
  countItems?: number
  price?: number
}

export interface Category {
  id: number
  name: string
  description?: string
  productCount?: number
}

export interface Size {
  id: number
  name: string
  description?: string
}

export interface CreateProductData {
  name: string
  description: string
  category: string
  basePrice: number
  popularity?: number
  images?: string[]
}

export interface CreateProductInfoData {
  productId: number
  sizeName: string
  countItems: number
  price: number
}
