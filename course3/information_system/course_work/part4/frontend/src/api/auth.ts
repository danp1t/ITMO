import apiClient from '.'
import type { LoginData, RegisterData } from '../types/auth'

export const authAPI = {
  register(data: RegisterData) {
    return apiClient.post('/api/auth/register', data)
  },

  login(data: LoginData) {
    return apiClient.post('/api/auth/login', data)
  },

  logout() {
    return apiClient.post('/api/auth/logout')
  },

  verifyEmail(email: string, code: string) {
    return apiClient.post<{ message: string }>('/api/auth/verify', null, {
      params: { email, code },
    })
  },

  forgotPassword(email: string) {
    return apiClient.post('/api/auth/forgot-password', null, {
      params: { email },
    })
  },

  resetPassword(token: string, newPassword: string) {
    return apiClient.post('/api/auth/reset-password', {
      token,
      newPassword,
    })
  },
}
