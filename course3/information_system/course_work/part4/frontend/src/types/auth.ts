export interface LoginData {
  email: string
  password: string
}

export interface RegisterData {
  name: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  user: User
  expiresIn?: number
}

export interface User {
  id: number
  name: string
  email: string
  roles: string[]
}

export interface VerifyEmailData {
  email: string
  code: string
}

export interface ForgotPasswordData {
  email: string
}

export interface ResetPasswordData {
  token: string
  newPassword: string
}
