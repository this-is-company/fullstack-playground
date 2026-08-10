import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogUrl, http, orderUrl, ssoUrl } from './http'
import type { CreateOrderRequest, LoginResponse, Order, Product, UserInfo } from './types'

export function useProducts() {
  return useQuery({
    queryKey: ['products'],
    queryFn: () => http<Product[]>(catalogUrl, '/api/products', { auth: false }),
  })
}

export function useProduct(id: number | undefined) {
  return useQuery({
    queryKey: ['products', id],
    enabled: id != null && !Number.isNaN(id),
    queryFn: () => http<Product>(catalogUrl, `/api/products/${id}`, { auth: false }),
  })
}

export function useMyOrders(enabled: boolean) {
  return useQuery({
    queryKey: ['orders', 'me'],
    enabled,
    queryFn: () => http<Order[]>(orderUrl, '/api/orders/me'),
  })
}

export function useMe(enabled: boolean) {
  return useQuery({
    queryKey: ['auth', 'me'],
    enabled,
    queryFn: () => http<UserInfo>(ssoUrl, '/api/auth/me'),
    retry: false,
  })
}

export function useLogin() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: { username: string; password: string }) =>
      http<LoginResponse>(ssoUrl, '/api/auth/login', { method: 'POST', body, auth: false }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['auth'] })
      void qc.invalidateQueries({ queryKey: ['orders'] })
    },
  })
}

export function useCreateOrder() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateOrderRequest) =>
      http<Order>(orderUrl, '/api/orders', { method: 'POST', body }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['orders'] })
      void qc.invalidateQueries({ queryKey: ['products'] })
    },
  })
}
