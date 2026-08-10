import { Link } from 'react-router-dom'
import { useMyOrders } from '../api/hooks'
import { useAuth } from '../auth/AuthContext'

function formatPrice(price: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price)
}

function formatDate(value: string) {
  try {
    return new Date(value).toLocaleString()
  } catch {
    return value
  }
}

export function MyOrdersPage() {
  const { isAuthenticated, loading: authLoading } = useAuth()
  const { data, isLoading, error, refetch, isFetching } = useMyOrders(isAuthenticated)

  if (authLoading) return <p className="muted">Checking session…</p>

  if (!isAuthenticated) {
    return (
      <section className="page narrow">
        <h1>My Orders</h1>
        <p className="lede">Sign in to see orders placed with your account.</p>
        <Link className="btn primary" to="/login" state={{ from: '/orders' }}>
          Log in
        </Link>
      </section>
    )
  }

  return (
    <section className="page">
      <div className="page-head">
        <div>
          <h1>My Orders</h1>
          <p className="lede">Orders tied to your SSO username.</p>
        </div>
        <button type="button" className="btn ghost" onClick={() => void refetch()} disabled={isFetching}>
          Refresh
        </button>
      </div>

      {isLoading && <p className="muted">Loading orders…</p>}
      {error && <p className="error">{(error as Error).message}</p>}

      {!isLoading && !error && (data?.length ?? 0) === 0 && (
        <p className="muted">
          No orders yet. <Link to="/">Browse the catalog</Link>.
        </p>
      )}

      <ul className="order-list">
        {(data ?? []).map((order) => {
          const total = order.items?.reduce((sum, item) => sum + Number(item.unitPrice) * item.quantity, 0) ?? 0
          return (
            <li key={order.id} className="order-card">
              <div className="order-card-head">
                <strong>Order #{order.id}</strong>
                <span className={`badge status-${order.status.toLowerCase()}`}>{order.status}</span>
              </div>
              <p className="muted">{formatDate(order.createdAt)}</p>
              <ul className="order-items">
                {(order.items ?? []).map((item, idx) => (
                  <li key={`${order.id}-${item.productId}-${idx}`}>
                    {item.productName} × {item.quantity}
                    <span>{formatPrice(Number(item.unitPrice) * item.quantity)}</span>
                  </li>
                ))}
              </ul>
              <p className="order-total">Total {formatPrice(total)}</p>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
