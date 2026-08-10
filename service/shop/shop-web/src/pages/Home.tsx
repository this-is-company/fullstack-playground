import { Link } from 'react-router-dom'
import { useProducts } from '../api/hooks'

function formatPrice(price: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price)
}

export function HomePage() {
  const { data, isLoading, error, refetch, isFetching } = useProducts()
  const products = (data ?? []).filter((p) => p.active !== false)

  return (
    <section className="page">
      <div className="page-head">
        <div>
          <h1>Catalog</h1>
          <p className="lede">Browse what’s in stock and place an order when you’re ready.</p>
        </div>
        <button type="button" className="btn ghost" onClick={() => void refetch()} disabled={isFetching}>
          Refresh
        </button>
      </div>

      {isLoading && <p className="muted">Loading products…</p>}
      {error && <p className="error">Could not load catalog: {(error as Error).message}</p>}

      {!isLoading && !error && products.length === 0 && (
        <p className="muted">No products yet. Start catalog-api on port 8101.</p>
      )}

      <div className="product-grid">
        {products.map((p) => (
          <Link key={p.id} to={`/products/${p.id}`} className="product-tile">
            <div className="product-tile-top">
              <h2>{p.name}</h2>
              <span className="price">{formatPrice(Number(p.price))}</span>
            </div>
            <p className="product-desc">{p.description}</p>
            <div className="product-meta">
              <span className={p.stock > 0 ? 'stock ok' : 'stock out'}>
                {p.stock > 0 ? `${p.stock} in stock` : 'Out of stock'}
              </span>
              <span className="cta-hint">View →</span>
            </div>
          </Link>
        ))}
      </div>
    </section>
  )
}
