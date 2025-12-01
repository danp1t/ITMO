import apiClient from '.'
import type { LoginData, RegisterData } from '../types/auth'

export const authAPI = {
  // Регистрация
  register(data: RegisterData) {
    return apiClient.post('/api/auth/register', data)
  },

  // Вход
  login(data: LoginData) {
    return apiClient.post('/api/auth/login', data)
  },

  // Выход
  logout() {
    return apiClient.post('/api/auth/logout')
  },

  // Подтверждение email
  verifyEmail(email: string, code: string) {
    return apiClient.post('/api/auth/verify', null, {
      params: { email, code },
    })
  },

  // Запрос сброса пароля
  forgotPassword(email: string) {
    return apiClient.post('/api/auth/forgot-password', null, {
      params: { email },
    })
  },

  // Сброс пароля
  resetPassword(token: string, newPassword: string) {
    return apiClient.post('/api/auth/reset-password', {
      token,
      newPassword,
    })
  },
}
