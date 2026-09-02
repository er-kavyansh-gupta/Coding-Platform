import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  return (
    <header style={{ borderBottom: '1px solid var(--line)', background: 'var(--bg-raised)' }}>
      <div className="container" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', height: 56 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 28 }}>
          <Link to="/" style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 18, display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ color: 'var(--accent)' }}>&gt;_</span> CodeBench
          </Link>
          {user && (
            <nav style={{ display: 'flex', gap: 20, fontSize: 14 }}>
              <Link to="/problems" style={{ color: 'var(--text-muted)' }}>Problems</Link>
              <Link to="/dashboard" style={{ color: 'var(--text-muted)' }}>Dashboard</Link>
              <Link to="/leaderboard" style={{ color: 'var(--text-muted)' }}>Leaderboard</Link>
              {isAdmin && <Link to="/admin/problems" style={{ color: 'var(--text-muted)' }}>Admin</Link>}
            </nav>
          )}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {user ? (
            <>
              <span className="mono" style={{ fontSize: 13, color: 'var(--text-muted)' }}>{user.username}</span>
              <button onClick={() => { logout(); navigate('/login') }}>Log out</button>
            </>
          ) : (
            <>
              <Link to="/login"><button className="ghost">Log in</button></Link>
              <Link to="/register"><button className="primary">Sign up</button></Link>
            </>
          )}
        </div>
      </div>
    </header>
  )
}
