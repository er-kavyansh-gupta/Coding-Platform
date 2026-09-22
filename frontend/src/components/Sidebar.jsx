import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { Code2, ListChecks, LayoutDashboard, Trophy, Award, Swords, ShieldCheck, LogOut, LogIn, UserPlus } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/problems', label: 'Problems', icon: ListChecks },
  { to: '/contests', label: 'Contests', icon: Swords },
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/leaderboard', label: 'Leaderboard', icon: Trophy },
  { to: '/certificates', label: 'Certificates', icon: Award },
]

export default function Sidebar() {
  const { user, logout, isAdmin } = useAuth()
  const navigate = useNavigate()

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <Code2 size={20} color="var(--accent)" strokeWidth={2.4} />
        <span>CodeBench</span>
      </div>

      {user ? (
        <>
          <nav className="sidebar-nav">
            {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
              <NavLink key={to} to={to} className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}>
                <Icon size={17} strokeWidth={2} />
                <span>{label}</span>
              </NavLink>
            ))}
            {isAdmin && (
              <>
                <div style={{ margin: '10px 10px 4px', fontSize: 11, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Admin</div>
                <NavLink to="/admin/problems" className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}>
                  <ShieldCheck size={17} strokeWidth={2} />
                  <span>Problems</span>
                </NavLink>
                <NavLink to="/admin/contests" className={({ isActive }) => `sidebar-link${isActive ? ' active' : ''}`}>
                  <Swords size={17} strokeWidth={2} />
                  <span>Contests</span>
                </NavLink>
              </>
            )}
          </nav>

          <div className="sidebar-footer">
            <div className="sidebar-user">
              <div className="sidebar-avatar">{user.username[0]?.toUpperCase()}</div>
              <span className="mono">{user.username}</span>
            </div>
            <button className="ghost sidebar-logout" onClick={() => { logout(); navigate('/login') }}>
              <LogOut size={15} /> Log out
            </button>
          </div>
        </>
      ) : (
        <div className="sidebar-footer" style={{ marginTop: 'auto' }}>
          <NavLink to="/login" className="sidebar-link">
            <LogIn size={17} /> <span>Log in</span>
          </NavLink>
          <NavLink to="/register" className="sidebar-link">
            <UserPlus size={17} /> <span>Sign up</span>
          </NavLink>
        </div>
      )}
    </aside>
  )
}
