import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '../api/auth'
import type { User } from '../types/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('auth_token'))
  const userStr = localStorage.getItem('auth_user')
  const user = ref<User | null>(userStr ? JSON.parse(userStr) : null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  const roles = computed(() => {
    return user.value?.roles || []
  })

  const userId = computed(() => {
    if (!user.value) return null
    return user.value.id
  })

  const hasRole = (roleName: string): boolean => {
    return roles.value.includes(roleName)
  }

  const hasAnyRole = (roleNames: string[]): boolean => {
    return roleNames.some(role => roles.value.includes(role))
  }

  const hasAllRoles = (roleNames: string[]): boolean => {
    return roleNames.every(role => roles.value.includes(role))
  }

  const canPublishPosts = (): boolean => {
    return hasRole('OAPI:ROLE:PublishPost')
  }

  const canEditPost = (postOwnerId: number): boolean => {
    if (!user.value) return false

    return user.value.id === postOwnerId || hasRole('OAPI:ROLE:EditPost')
  }

  const canDeletePost = (postOwnerId: number): boolean => {
    if (!user.value) return false

    return user.value.id === postOwnerId || hasRole('OAPI:ROLE:DeletePost')
  }

  const canEditComment = (postOwnerId: number): boolean => {
    if (!user.value) return false

    return user.value.id === postOwnerId || hasRole('OAPI:ROLE:EditPost')
  }

  const canDeleteComment = (postOwnerId: number): boolean => {
    if (!user.value) return false

    return user.value.id === postOwnerId || hasRole('OAPI:ROLE:DeletePost')
  }

  const canPublishTournaments = (): boolean => {
    return hasRole('OAPI:ROLE:PublishTournament')
  }

  const canEditTournaments = (): boolean => {
    return hasRole('OAPI:ROLE:EditTournament')
  }

  const canDeleteTournaments = (): boolean => {
    return hasRole('OAPI:ROLE:DeleteTournament')
  }

  const canPublishProducts = (): boolean => {
    return hasRole('OAPI:ROLE:PublishProduct')
  }

  const canEditProducts = (): boolean => {
    return hasRole('OAPI:ROLE:EditProduct')
  }

  const canDeleteProducts = (): boolean => {
    return hasRole('OAPI:ROLE:DeleteProduct')
  }

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('auth_token', newToken)
  }

  const setUser = (userData: User) => {
    user.value = userData
    localStorage.setItem('auth_user', JSON.stringify(userData))
    console.log('Пользователь сохранен с ролями:', userData.roles)
  }

  const setAuthData = (authData: { user: User; token: string }) => {
    setToken(authData.token)
    setUser(authData.user)
  }

  const clearAuth = () => {
    token.value = null
    user.value = null
    localStorage.removeItem('auth_token')
    localStorage.removeItem('auth_user')
  }

  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password })
      const { token: authToken, id, email: userEmail, name, roles } = response.data

      console.log('Ответ от сервера при входе (роли):', roles)

      const userData: User = {
        id,
        email: userEmail,
        name,
        roles: roles || []
      }
      setAuthData({ token: authToken, user: userData })

      return { success: true }
    } catch (error: any) {
      console.error('Ошибка входа:', error)
      const message = error.response?.data || 'Ошибка входа'
      return { success: false, error: message }
    }
  }

  const checkAuth = async () => {
    if (!token.value) return false

    try {
      if (!user.value && token.value) {
        const payload = decodeJWT(token.value)
        if (payload && payload.sub) {
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

  const register = async (name: string, email: string, password: string) => {
    try {
      const response = await authAPI.register({ name, email, password })
      return {
        success: true,
        data: response.data,
        message: 'Регистрация успешна! Проверьте email для подтверждения.'
      }
    } catch (error: any) {
      const message = error.response?.data || 'Ошибка регистрации'
      return { success: false, error: message }
    }
  }

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

  const forgotPassword = async (email: string) => {
    try {
      await authAPI.forgotPassword(email)
      return { success: true, message: 'Инструкции отправлены на email' }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка запроса сброса пароля'
      return { success: false, error: message }
    }
  }

  const resetPassword = async (token: string, newPassword: string) => {
    try {
      await authAPI.resetPassword(token, newPassword)
      return { success: true, message: 'Пароль успешно изменен' }
    } catch (error: any) {
      const message = error.response?.data?.message || 'Ошибка сброса пароля'
      return { success: false, error: message }
    }
  }

  const isAdmin = computed(() => {
    return hasRole('OAPI:ROLE:BlockAccount') || hasRole('OAPI:ROLE:ManageUsers')
  })

  const canManageUsers = computed(() => {
    return hasRole('OAPI:ROLE:BlockAccount') || hasRole('OAPI:ROLE:ManageUsers')
  })

  const canManageRoles = computed(() => {
    return hasRole('OAPI:ROLE:BlockAccount') || hasRole('OAPI:ROLE:ManageRoles')
  })

  return {
    token,
    user,
    isAuthenticated,
    roles,
    userId,
    isAdmin,
    canManageUsers,
    canManageRoles,
    hasRole,
    hasAnyRole,
    hasAllRoles,
    canPublishPosts,
    canEditPost,
    canDeletePost,
    canPublishTournaments,
    canEditTournaments,
    canDeleteTournaments,
    canPublishProducts,
    canEditProducts,
    canDeleteProducts,
    canEditComment,
    canDeleteComment,
    setToken,
    setUser,
    setAuthData,
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
