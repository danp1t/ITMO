export interface UserWithDetails {
  id: number
  email: string
  name: string
  roles: string[]
  enabled: boolean
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

export interface CreateRoleData {
  name: string
  description: string
}
