import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '../api/auth'
import type { User, LoginResponse } from '../types/user'

export const useAuthStore = defineStore('auth', () => {
  // Загружаем данные из localStorage при инициализации
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const userStr = localStorage.getItem('auth_user')
  const user = ref<User | null>(userStr ? JSON.parse(userStr) : null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  // Computed свойство для получения ID пользователя
  const userId = computed(() => {
    if (!user.value) return null
    return user.value.id
  })

  // Сохранение токена
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('auth_token', newToken)
  }

  // Сохранение пользователя
  const setUser = (userData: User) => {
    user.value = userData
    localStorage.setItem('auth_user', JSON.stringify(userData))
    console.log('Пользователь сохранен:', userData)
  }

  // Очистка данных
  const clearAuth = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  // Вход - исправленная версия
  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password })
      const { token: authToken, id, email: userEmail, name, roles } = response.data

      console.log('Ответ от сервера при входе:', response.data)

      // Создаем объект пользователя из данных ответа
      const userData: User = {
        id,
        email: userEmail,
        name,
        roles
      }

      setToken(authToken)
      setUser(userData)

      return { success: true }
    } catch (error: any) {
      console.error('Ошибка входа:', error)
      const message = error.response?.data?.message || 'Ошибка входа'
      return { success: false, error: message }
    }
  }

  // Проверка аутентификации при загрузке приложения
  const checkAuth = async () => {
    if (!token.value) return false

    try {
      // Пытаемся получить данные пользователя по токену
      // Если у вас есть endpoint для получения текущего пользователя
      if (!user.value) {
        // Можно попробовать декодировать JWT токен
        const payload = decodeJWT(token.value)
        if (payload && payload.sub) {
          // Если в токене есть email, можем использовать его
          console.log('Данные из JWT токена:', payload)
        }
      }
      return true
    } catch (error) {
      console.error('Ошибка проверки аутентификации:', error)
      clearAuth()
      return false
    }
  }

  // Декодирование JWT токена
  const decodeJWT = (token: string) => {
    try {
      const base64Url = token.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
      return JSON.parse(jsonPayload)
    } catch (e) {
      console.error('Ошибка декодирования JWT:', e)
      return null
    }
  }

  // Выход
  const logout = async () => {
    try {
      if (token.value) {
        await authAPI.logout()
      }
    } catch (error) {
      console.error('Ошибка при выходе:', error)
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
