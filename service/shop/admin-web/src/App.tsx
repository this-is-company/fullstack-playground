import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AdminLayout, RequireAuth } from './components/Layout'
import { LoginPage } from './pages/Login'
import { OrdersPage } from './pages/Orders'
import { ProductsPage } from './pages/Products'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          element={
            <RequireAuth>
              <AdminLayout />
            </RequireAuth>
          }
        >
          <Route index element={<Navigate to="/products" replace />} />
          <Route path="products" element={<ProductsPage />} />
          <Route
            path="orders"
            element={
              <RequireAuth adminOnly>
                <OrdersPage />
              </RequireAuth>
            }
          />
        </Route>
        <Route path="*" element={<Navigate to="/products" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
