import apiClient from './index'
import type { CreateRoleData } from '@/types/admin'

export const adminAPI = {
  // Получить всех пользователей - исправленный endpoint
  getAllUsers() {
    return apiClient.get('/api/accounts')  // Изменил с /api/accounts/admin на /api/accounts
  },

  // Получить детальную информацию о пользователе
  getUserDetails(userId: number) {
    return apiClient.get(`/api/accounts/${userId}/detail`)  // Использую существующий endpoint
  },

  // Обновить роли пользователя - нужно будет создать на бэкенде
  updateUserRoles(userId: number, roles: string[]) {
    return apiClient.put(`/api/accounts/${userId}/roles`, { roles })
  },

  // Заблокировать/разблокировать пользователя
  toggleUserStatus(userId: number, isActive: boolean) {
    return apiClient.patch(`/api/accounts/${userId}/status`, { isActive })
  },

  // Получить все роли
  getAllRoles() {
    return apiClient.get('/api/roles')
  },

  // Создать новую роль
  createRole(data: CreateRoleData) {
    return apiClient.post('/api/roles', data)
  },

  // Удалить роль
  deleteRole(roleId: number) {
    return apiClient.delete(`/api/roles/${roleId}`)
  },

  // Поиск пользователей
  searchUsers(query: string) {
    return apiClient.get(`/api/accounts/search?q=${query}`)
  },

  // Получить статистику пользователей
  getUserStats() {
    return apiClient.get('/api/admin/stats')
  }
}
