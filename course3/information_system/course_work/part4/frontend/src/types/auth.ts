export interface LoginData {
  email: string
  password: string
}

export interface RegisterData {
  name: string
  email: string
  password: string
}

export interface User {
  id: number
  name: string
  email: string
  roles: string[]
}

