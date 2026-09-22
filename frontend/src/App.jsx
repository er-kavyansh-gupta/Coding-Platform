import React from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import ProblemList from './pages/ProblemList'
import ProblemSolve from './pages/ProblemSolve'
import Dashboard from './pages/Dashboard'
import Leaderboard from './pages/Leaderboard'
import Certificates from './pages/Certificates'
import Contests from './pages/Contests'
import ContestDetail from './pages/ContestDetail'
import ContestLeaderboard from './pages/ContestLeaderboard'
import AdminProblems from './pages/AdminProblems'
import AdminProblemForm from './pages/AdminProblemForm'
import AdminContests from './pages/AdminContests'
import AdminContestForm from './pages/AdminContestForm'
import { useAuth } from './context/AuthContext'

export default function App() {
  const { user } = useAuth()
  const location = useLocation()
  // The solve page manages its own full-height two-pane layout; give it the whole
  // content area without the default page padding/scroll container.
  const isSolvePage = /^\/problems\/\d+$/.test(location.pathname) || /^\/contests\/\d+\/problems\/\d+$/.test(location.pathname)

  return (
    <div className="app-shell-sidebar">
      {user && <Sidebar />}
      <main className={isSolvePage ? 'main-content no-scroll' : 'main-content'}>
        <Routes>
          <Route path="/" element={<Navigate to="/problems" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route path="/problems" element={<ProtectedRoute><ProblemList /></ProtectedRoute>} />
          <Route path="/problems/:id" element={<ProtectedRoute><ProblemSolve /></ProtectedRoute>} />
          <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
          <Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />
          <Route path="/certificates" element={<ProtectedRoute><Certificates /></ProtectedRoute>} />

          <Route path="/contests" element={<ProtectedRoute><Contests /></ProtectedRoute>} />
          <Route path="/contests/:id" element={<ProtectedRoute><ContestDetail /></ProtectedRoute>} />
          <Route path="/contests/:id/leaderboard" element={<ProtectedRoute><ContestLeaderboard /></ProtectedRoute>} />
          <Route path="/contests/:contestId/problems/:id" element={<ProtectedRoute><ProblemSolve /></ProtectedRoute>} />

          <Route path="/admin/problems" element={<ProtectedRoute adminOnly><AdminProblems /></ProtectedRoute>} />
          <Route path="/admin/problems/new" element={<ProtectedRoute adminOnly><AdminProblemForm /></ProtectedRoute>} />
          <Route path="/admin/problems/:id/edit" element={<ProtectedRoute adminOnly><AdminProblemForm /></ProtectedRoute>} />

          <Route path="/admin/contests" element={<ProtectedRoute adminOnly><AdminContests /></ProtectedRoute>} />
          <Route path="/admin/contests/new" element={<ProtectedRoute adminOnly><AdminContestForm /></ProtectedRoute>} />
          <Route path="/admin/contests/:id/edit" element={<ProtectedRoute adminOnly><AdminContestForm /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/problems" replace />} />
        </Routes>
      </main>
    </div>
  )
}
