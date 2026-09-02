import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import ProblemList from './pages/ProblemList'
import ProblemSolve from './pages/ProblemSolve'
import Dashboard from './pages/Dashboard'
import Leaderboard from './pages/Leaderboard'
import AdminProblems from './pages/AdminProblems'
import AdminProblemForm from './pages/AdminProblemForm'

export default function App() {
  return (
    <div className="app-shell">
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/problems" replace />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route path="/problems" element={<ProtectedRoute><ProblemList /></ProtectedRoute>} />
        <Route path="/problems/:id" element={<ProtectedRoute><ProblemSolve /></ProtectedRoute>} />
        <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
        <Route path="/leaderboard" element={<ProtectedRoute><Leaderboard /></ProtectedRoute>} />

        <Route path="/admin/problems" element={<ProtectedRoute adminOnly><AdminProblems /></ProtectedRoute>} />
        <Route path="/admin/problems/new" element={<ProtectedRoute adminOnly><AdminProblemForm /></ProtectedRoute>} />
        <Route path="/admin/problems/:id/edit" element={<ProtectedRoute adminOnly><AdminProblemForm /></ProtectedRoute>} />

        <Route path="*" element={<Navigate to="/problems" replace />} />
      </Routes>
    </div>
  )
}
