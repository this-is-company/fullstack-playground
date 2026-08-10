import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { getToken, http, setToken as persistToken, ssoUrl } from '../api/http'
import type { LoginResponse, UserInfo } from '../api/types'

type AuthState = {
  token: string | null
  user: UserInfo | null
  loading: boolean
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
  isAdmin: boolean
}

const AuthContext = createContext<AuthState | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const qc = useQueryClient()
  const [token, setTokenState] = useState<string | null>(() => getToken())
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(true)

  const applyToken = useCallback((next: string | null) => {
    persistToken(next)
    setTokenState(next)
  }, [])

  useEffect(() => {
    let cancelled = false
    async function loadMe() {
      if (!token) {
        setUser(null)
        setLoading(false)
        return
      }
      setLoading(true)
      try {
        const me = await http<UserInfo>(ssoUrl, '/api/auth/me', { token })
        if (!cancelled) setUser(normalizeUser(me))
      } catch {
        if (!cancelled) {
          applyToken(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void loadMe()
    return () => {
      cancelled = true
    }
  }, [token, applyToken])

  const login = useCallback(
    async (username: string, password: string) => {
      const res = await http<LoginResponse>(ssoUrl, '/api/auth/login', {
        method: 'POST',
        body: { username, password },
        auth: false,
      })
      applyToken(res.accessToken)
      setUser({
        username,
        roles: res.roles ?? [],
        preferred_username: username,
      })
      await qc.invalidateQueries()
    },
    [applyToken, qc],
  )

  const logout = useCallback(() => {
    applyToken(null)
    setUser(null)
    qc.clear()
  }, [applyToken, qc])

  const value = useMemo<AuthState>(
    () => ({
      token,
      user,
      loading,
      login,
      logout,
      isAuthenticated: Boolean(token && user),
      isAdmin: Boolean(user?.roles?.includes('ADMIN')),
    }),
    [token, user, loading, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

function normalizeUser(me: UserInfo): UserInfo {
  return {
    ...me,
    username: me.preferred_username || me.username || me.sub || 'user',
    roles: me.roles ?? [],
  }
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
