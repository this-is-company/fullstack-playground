import { type FormEvent, useState } from 'react'
import { useCreateProduct, useProducts, useUpdateProduct, useUpdateStock } from '../api/hooks'
import type { Product } from '../api/types'

function formatPrice(price: number) {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(price)
}

const emptyForm = {
  name: '',
  description: '',
  price: '0',
  stock: '0',
  active: true,
}

export function ProductsPage() {
  const { data, isLoading, error, refetch, isFetching } = useProducts()
  const createProduct = useCreateProduct()
  const updateProduct = useUpdateProduct()
  const updateStock = useUpdateStock()

  const [form, setForm] = useState(emptyForm)
  const [editing, setEditing] = useState<Product | null>(null)
  const [stockDrafts, setStockDrafts] = useState<Record<number, string>>({})
  const [message, setMessage] = useState<string | null>(null)

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    setMessage(null)
    try {
      await createProduct.mutateAsync({
        name: form.name.trim(),
        description: form.description.trim(),
        price: Number(form.price),
        stock: Number(form.stock),
        active: form.active,
      })
      setForm(emptyForm)
      setMessage('Product created.')
    } catch (err) {
      setMessage((err as Error).message)
    }
  }

  async function onSaveEdit(e: FormEvent) {
    e.preventDefault()
    if (!editing) return
    setMessage(null)
    try {
      await updateProduct.mutateAsync({
        id: editing.id,
        body: {
          name: editing.name,
          description: editing.description,
          price: Number(editing.price),
          active: editing.active,
        },
      })
      setEditing(null)
      setMessage(`Product #${editing.id} updated.`)
    } catch (err) {
      setMessage((err as Error).message)
    }
  }

  async function onSetStock(product: Product) {
    const raw = stockDrafts[product.id] ?? String(product.stock)
    const stock = Number(raw)
    if (Number.isNaN(stock) || stock < 0) {
      setMessage('Stock must be a non-negative number.')
      return
    }
    setMessage(null)
    try {
      await updateStock.mutateAsync({ id: product.id, stock })
      setMessage(`Stock for #${product.id} set to ${stock}.`)
    } catch (err) {
      setMessage((err as Error).message)
    }
  }

  async function onAdjustStock(product: Product, delta: number) {
    setMessage(null)
    try {
      await updateStock.mutateAsync({ id: product.id, delta })
      setMessage(`Adjusted stock for #${product.id} by ${delta > 0 ? '+' : ''}${delta}.`)
    } catch (err) {
      setMessage((err as Error).message)
    }
  }

  return (
    <section className="admin-page">
      <div className="admin-page-head">
        <div>
          <h1>Products</h1>
          <p className="muted">Create catalog items and adjust stock.</p>
        </div>
        <button type="button" className="btn ghost" onClick={() => void refetch()} disabled={isFetching}>
          Refresh
        </button>
      </div>

      {message && <p className="notice">{message}</p>}
      {isLoading && <p className="muted">Loading…</p>}
      {error && <p className="error">{(error as Error).message}</p>}

      <div className="admin-grid">
        <form className="panel" onSubmit={(e) => void onCreate(e)}>
          <h2>New product</h2>
          <label>
            Name
            <input
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              required
            />
          </label>
          <label>
            Description
            <textarea
              value={form.description}
              onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
              rows={3}
              required
            />
          </label>
          <div className="row-2">
            <label>
              Price
              <input
                type="number"
                min={0}
                step="0.01"
                value={form.price}
                onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))}
                required
              />
            </label>
            <label>
              Stock
              <input
                type="number"
                min={0}
                value={form.stock}
                onChange={(e) => setForm((f) => ({ ...f, stock: e.target.value }))}
                required
              />
            </label>
          </div>
          <label className="check">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Active
          </label>
          <button type="submit" className="btn primary" disabled={createProduct.isPending}>
            {createProduct.isPending ? 'Creating…' : 'Create'}
          </button>
        </form>

        <div className="panel grow">
          <h2>Catalog ({data?.length ?? 0})</h2>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Price</th>
                  <th>Stock</th>
                  <th>Active</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {(data ?? []).map((p) => (
                  <tr key={p.id}>
                    <td>{p.id}</td>
                    <td>
                      <button type="button" className="linkish" onClick={() => setEditing(p)}>
                        {p.name}
                      </button>
                    </td>
                    <td>{formatPrice(Number(p.price))}</td>
                    <td>
                      <div className="stock-controls">
                        <button type="button" className="btn tiny" onClick={() => void onAdjustStock(p, -1)}>
                          −
                        </button>
                        <input
                          className="stock-input"
                          value={stockDrafts[p.id] ?? String(p.stock)}
                          onChange={(e) =>
                            setStockDrafts((prev) => ({ ...prev, [p.id]: e.target.value }))
                          }
                        />
                        <button type="button" className="btn tiny" onClick={() => void onAdjustStock(p, 1)}>
                          +
                        </button>
                        <button type="button" className="btn tiny" onClick={() => void onSetStock(p)}>
                          Set
                        </button>
                      </div>
                    </td>
                    <td>{p.active ? 'yes' : 'no'}</td>
                    <td>
                      <button type="button" className="btn tiny" onClick={() => setEditing(p)}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {editing && (
        <form className="panel edit-panel" onSubmit={(e) => void onSaveEdit(e)}>
          <div className="admin-page-head">
            <h2>Edit #{editing.id}</h2>
            <button type="button" className="btn ghost" onClick={() => setEditing(null)}>
              Close
            </button>
          </div>
          <label>
            Name
            <input
              value={editing.name}
              onChange={(e) => setEditing({ ...editing, name: e.target.value })}
              required
            />
          </label>
          <label>
            Description
            <textarea
              value={editing.description}
              onChange={(e) => setEditing({ ...editing, description: e.target.value })}
              rows={3}
              required
            />
          </label>
          <div className="row-2">
            <label>
              Price
              <input
                type="number"
                min={0}
                step="0.01"
                value={editing.price}
                onChange={(e) => setEditing({ ...editing, price: Number(e.target.value) })}
                required
              />
            </label>
            <label className="check end">
              <input
                type="checkbox"
                checked={editing.active}
                onChange={(e) => setEditing({ ...editing, active: e.target.checked })}
              />
              Active
            </label>
          </div>
          <button type="submit" className="btn primary" disabled={updateProduct.isPending}>
            {updateProduct.isPending ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      )}
    </section>
  )
}
