import type { ReactNode } from 'react'
import { NavLink, Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function RequireAuth({ children, adminOnly = false }: { children: ReactNode; adminOnly?: boolean }) {
  const { isAuthenticated, isAdmin, loading } = useAuth()

  if (loading) return <p className="muted pad">Checking session…</p>
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (adminOnly && !isAdmin) {
    return (
      <div className="pad">
        <h1>Admin required</h1>
        <p className="muted">Sign in with an ADMIN account (demo: admin / admin123).</p>
      </div>
    )
  }
  return <>{children}</>
}

export function AdminLayout() {
  const { user, logout, isAuthenticated } = useAuth()

  return (
    <div className="admin-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">Shop Admin</div>
        <nav className="sidebar-nav">
          <NavLink to="/products">Products</NavLink>
          <NavLink to="/orders">Orders</NavLink>
        </nav>
        <div className="sidebar-foot">
          {isAuthenticated ? (
            <>
              <div className="sidebar-user">{user?.username}</div>
              <div className="sidebar-roles">{user?.roles?.join(', ')}</div>
              <button type="button" className="btn ghost full" onClick={logout}>
                Log out
              </button>
            </>
          ) : (
            <NavLink to="/login">Log in</NavLink>
          )}
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}
