import { useAllOrders } from '../api/hooks'
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

export function OrdersPage() {
  const { isAdmin } = useAuth()
  const { data, isLoading, error, refetch, isFetching } = useAllOrders(isAdmin)

  if (!isAdmin) {
    return (
      <section className="admin-page">
        <h1>Orders</h1>
        <p className="error">ADMIN role required to list all orders.</p>
      </section>
    )
  }

  return (
    <section className="admin-page">
      <div className="admin-page-head">
        <div>
          <h1>Orders</h1>
          <p className="muted">All orders from order-api (ADMIN).</p>
        </div>
        <button type="button" className="btn ghost" onClick={() => void refetch()} disabled={isFetching}>
          Refresh
        </button>
      </div>

      {isLoading && <p className="muted">Loading orders…</p>}
      {error && <p className="error">{(error as Error).message}</p>}

      <div className="panel">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>User</th>
                <th>Status</th>
                <th>Created</th>
                <th>Items</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {(data ?? []).map((order) => {
                const total =
                  order.items?.reduce((sum, item) => sum + Number(item.unitPrice) * item.quantity, 0) ?? 0
                return (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.username}</td>
                    <td>
                      <span className={`badge status-${order.status.toLowerCase()}`}>{order.status}</span>
                    </td>
                    <td>{formatDate(order.createdAt)}</td>
                    <td>
                      <ul className="mini-list">
                        {(order.items ?? []).map((item, idx) => (
                          <li key={`${order.id}-${item.productId}-${idx}`}>
                            {item.productName} × {item.quantity}
                          </li>
                        ))}
                      </ul>
                    </td>
                    <td>{formatPrice(total)}</td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
        {!isLoading && !error && (data?.length ?? 0) === 0 && (
          <p className="muted pad">No orders yet.</p>
        )}
      </div>
    </section>
  )
}
