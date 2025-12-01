export interface User {
  id: number
  name: string
  email: string
  roles: string[]
}

export interface Account {
  id: number
  name: string
  email: string
  isVerified: boolean
  createdAt: string
}

export interface Role {
  id: number
  name: string
  description: string
}
