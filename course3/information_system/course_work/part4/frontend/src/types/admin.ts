export interface UserWithDetails {
  id: number
  email: string
  name: string
  roles: string[]
  isActive: boolean
  createdAt: string
  lastLoginAt?: string
  postCount?: number
  commentCount?: number
}

export interface Role {
  id: number
  name: string
  description: string
}

export interface UpdateUserData {
  userId: number
  roles: string[]
  isActive: boolean
}

export interface CreateRoleData {
  name: string
  description: string
}
