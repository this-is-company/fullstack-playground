export type Product = {
  id: number
  name: string
  description: string
  price: number
  stock: number
  active: boolean
}

export type LoginResponse = {
  accessToken: string
  tokenType: string
  expiresIn: number
  roles: string[]
}

export type UserInfo = {
  username: string
  roles: string[]
  sub?: string
  preferred_username?: string
}

export type OrderItem = {
  productId: number
  productName: string
  quantity: number
  unitPrice: number
}

export type Order = {
  id: number
  username: string
  status: 'PENDING' | 'PAID' | 'CANCELLED' | string
  createdAt: string
  items: OrderItem[]
}

export type CreateOrderRequest = {
  items: Array<{ productId: number; quantity: number }>
}
