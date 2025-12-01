import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '../api/auth'
import type { User } from '../types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const user = ref<User | null>(null)
  const isAuthenticated = computed(() => !!token.value)

  // Сохранение токена
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('auth_token', newToken)
  }

  // Сохранение пользователя
  const setUser = (userData: User) => {
    user.value = userData
  }

  // Очистка данных
  const clearAuth = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('auth_token')
  }

  // Вход
  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password })
      const { token: authToken, user: userData } = response.data

      setToken(authToken)
      setUser(userData)

      return { success: true }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка входа'
      return { success: false, error: message }
    }
  }

  // Выход
  const logout = async () => {
    try {
      await authAPI.logout()
    } finally {
      clearAuth()
    }
  }

  // Регистрация
  const register = async (name: string, email: string, password: string) => {
    try {
      const response = await authAPI.register({ name, email, password })
      return {
        success: true,
        data: response.data,
        message: 'Регистрация успешна! Проверьте email для подтверждения.'
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка регистрации'
      return { success: false, error: message }
    }
  }

  // Подтверждение email
  const verifyEmail = async (email: string, code: string) => {
    try {
      const response = await authAPI.verifyEmail(email, code)
      return {
        success: true,
        message: response.data.message || 'Email успешно подтвержден!'
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка подтверждения email'
      return { success: false, error: message }
    }
  }

  // Запрос сброса пароля
  const forgotPassword = async (email: string) => {
    try {
      await authAPI.forgotPassword(email)
      return { success: true, message: 'Инструкции отправлены на email' }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка запроса сброса пароля'
      return { success: false, error: message }
    }
  }

  // Сброс пароля
  const resetPassword = async (token: string, newPassword: string) => {
    try {
      await authAPI.resetPassword(token, newPassword)
      return { success: true, message: 'Пароль успешно изменен' }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка сброса пароля'
      return { success: false, error: message }
    }
  }

  // Проверка аутентификации при загрузке приложения
  const checkAuth = async () => {
    if (!token.value) return false

    try {
      // Здесь можно добавить запрос для проверки токена
      // Например, получить данные пользователя
      return true
    } catch (error) {
      clearAuth()
      return false
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    setToken,
    setUser,
    clearAuth,
    login,
    logout,
    register,
    verifyEmail,
    forgotPassword,
    resetPassword,
    checkAuth,
  }
})
