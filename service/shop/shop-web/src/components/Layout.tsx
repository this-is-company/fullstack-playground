import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const { isAuthenticated, user, logout } = useAuth()

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">
          Harbor Goods
        </Link>
        <nav className="nav">
          <NavLink to="/" end>
            Shop
          </NavLink>
          <NavLink to="/orders">My Orders</NavLink>
          {isAuthenticated ? (
            <>
              <span className="nav-user">{user?.username}</span>
              <button type="button" className="linkish" onClick={logout}>
                Log out
              </button>
            </>
          ) : (
            <NavLink to="/login">Log in</NavLink>
          )}
        </nav>
      </header>
      <main className="main">
        <Outlet />
      </main>
      <footer className="footer">
        <span>Harbor Goods · demo storefront</span>
      </footer>
    </div>
  )
}
