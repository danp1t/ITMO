export interface User {
  id: number
  email: string
  name: string
  roles?: string[]
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  id: number
  email: string
  name: string
  roles: string[]
  type: string
}
