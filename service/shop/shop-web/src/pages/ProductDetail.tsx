import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useCreateOrder, useProduct } from '../api/hooks'
import { useAuth } from '../auth/AuthContext'

function formatPrice(price: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price)
}

export function ProductDetailPage() {
  const { id } = useParams()
  const productId = Number(id)
  const { data: product, isLoading, error } = useProduct(productId)
  const { isAuthenticated } = useAuth()
  const createOrder = useCreateOrder()
  const navigate = useNavigate()
  const [qty, setQty] = useState(1)
  const [message, setMessage] = useState<string | null>(null)

  async function onOrder(e: FormEvent) {
    e.preventDefault()
    setMessage(null)
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/products/${productId}` } })
      return
    }
    try {
      const order = await createOrder.mutateAsync({
        items: [{ productId, quantity: qty }],
      })
      setMessage(`Order #${order.id} placed (${order.status}).`)
      navigate('/orders')
    } catch (err) {
      setMessage((err as Error).message)
    }
  }

  if (isLoading) return <p className="muted">Loading product…</p>
  if (error) return <p className="error">{(error as Error).message}</p>
  if (!product) return <p className="error">Product not found.</p>

  return (
    <section className="page narrow">
      <Link to="/" className="back">
        ← Back to catalog
      </Link>
      <article className="detail">
        <h1>{product.name}</h1>
        <p className="price lg">{formatPrice(Number(product.price))}</p>
        <p className="lede">{product.description}</p>
        <p className={product.stock > 0 ? 'stock ok' : 'stock out'}>
          {product.stock > 0 ? `${product.stock} available` : 'Out of stock'}
        </p>

        <form className="order-form" onSubmit={(e) => void onOrder(e)}>
          <label>
            Quantity
            <input
              type="number"
              min={1}
              max={Math.max(product.stock, 1)}
              value={qty}
              onChange={(e) => setQty(Number(e.target.value))}
              disabled={product.stock <= 0}
            />
          </label>
          <button
            type="submit"
            className="btn primary"
            disabled={product.stock <= 0 || createOrder.isPending}
          >
            {isAuthenticated ? (createOrder.isPending ? 'Placing…' : 'Place order') : 'Log in to order'}
          </button>
        </form>
        {message && <p className="notice">{message}</p>}
      </article>
    </section>
  )
}
