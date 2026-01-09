import apiClient from './index'
import type { CreateRoleData } from '@/types/admin'

export const adminAPI = {
  getAllUsers() {
    return apiClient.get('/api/accounts')
  },

  getUserDetails(userId: number) {
    return apiClient.get(`/api/accounts/${userId}/detail`)
  },

  toggleUserStatus(userId: number, isActive: boolean) {
    return apiClient.patch(`/api/accounts/${userId}/status`, { isActive })
  },

  getAllRoles() {
    return apiClient.get('/api/roles')
  },
  getAccountsByRole: (roleId: number) => {
    return apiClient.get(`/api/roles/${roleId}/users`)
  },

  getRoleUsersCount: (roleId: number) => {
    return apiClient.get(`/api/roles/${roleId}/users/count`)
  },

  getAccountByEmail: (email: string) => {
    return apiClient.get(`/api/accounts/email/${email}`)
  },

  addRoleToAccountByName: (accountId: number, roleName: string) => {
    return apiClient.post(`/api/accounts/${accountId}/roles/by-name/${roleName}`)
  },

  removeRoleFromAccountByName: (accountId: number, roleName: string) => {
    return apiClient.delete(`/api/accounts/${accountId}/roles/by-name/${roleName}`)
  },

  createRole(data: CreateRoleData) {
    return apiClient.post('/api/roles', data)
  },

  deleteRole(roleId: number) {
    return apiClient.delete(`/api/roles/${roleId}`)
  },
}
