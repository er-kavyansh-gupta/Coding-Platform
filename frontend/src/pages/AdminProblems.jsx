import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'
import DifficultyBadge from '../components/DifficultyBadge'

export default function AdminProblems() {
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)

  function load() {
    setLoading(true)
    api.get('/problems').then(res => setProblems(res.data)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleDelete(id) {
    if (!window.confirm('Delete this problem? This cannot be undone.')) return
    await api.delete(`/admin/problems/${id}`)
    load()
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Admin · Problems</h2>
        <Link to="/admin/problems/new"><button className="primary">+ New problem</button></Link>
      </div>

      <div className="panel">
        {loading && <div style={{ padding: 24, color: 'var(--text-muted)' }}>Loading…</div>}
        {!loading && problems.map(p => (
          <div key={p.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', borderBottom: '1px solid var(--line)' }}>
            <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
              <span>{p.title}</span>
              <DifficultyBadge difficulty={p.difficulty} />
            </div>
            <div style={{ display: 'flex', gap: 8 }}>
              <Link to={`/admin/problems/${p.id}/edit`}><button>Edit</button></Link>
              <button onClick={() => handleDelete(p.id)} style={{ color: 'var(--red)' }}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
