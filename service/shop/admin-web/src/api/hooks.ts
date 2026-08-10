import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { catalogUrl, http, orderUrl, ssoUrl } from './http'
import type { LoginResponse, Order, Product, ProductInput, UserInfo } from './types'

export function useProducts() {
  return useQuery({
    queryKey: ['products'],
    queryFn: () => http<Product[]>(catalogUrl, '/api/products', { auth: false }),
  })
}

export function useAllOrders(enabled: boolean) {
  return useQuery({
    queryKey: ['orders', 'all'],
    enabled,
    queryFn: () => http<Order[]>(orderUrl, '/api/orders'),
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

export function useCreateProduct() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (body: ProductInput) =>
      http<Product>(catalogUrl, '/api/products', { method: 'POST', body, auth: false }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['products'] })
    },
  })
}

export function useUpdateProduct() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: number; body: Partial<ProductInput> }) =>
      http<Product>(catalogUrl, `/api/products/${id}`, { method: 'PATCH', body, auth: false }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['products'] })
    },
  })
}

export function useUpdateStock() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, stock, delta }: { id: number; stock?: number; delta?: number }) =>
      http<Product>(catalogUrl, `/api/products/${id}/stock`, {
        method: 'PATCH',
        body: stock !== undefined ? { stock } : { delta },
        auth: false,
      }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ['products'] })
    },
  })
}
