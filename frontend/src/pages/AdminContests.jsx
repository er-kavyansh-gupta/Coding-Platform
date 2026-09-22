import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

const STATUS_META = {
  UPCOMING: { label: 'Upcoming', color: 'var(--blue)' },
  ONGOING: { label: 'Live', color: 'var(--green)' },
  ENDED: { label: 'Ended', color: 'var(--text-muted)' },
}

export default function AdminContests() {
  const [contests, setContests] = useState([])
  const [loading, setLoading] = useState(true)

  function load() {
    setLoading(true)
    api.get('/contests').then(res => setContests(res.data)).finally(() => setLoading(false))
  }
  useEffect(load, [])

  async function handleDelete(id) {
    if (!window.confirm('Delete this contest? This cannot be undone.')) return
    await api.delete(`/contests/admin/${id}`)
    load()
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Admin · Contests</h2>
        <Link to="/admin/contests/new"><button className="primary">+ New contest</button></Link>
      </div>

      <div className="panel">
        {loading && <div style={{ padding: 24, color: 'var(--text-muted)' }}>Loading…</div>}
        {!loading && contests.map(c => {
          const status = STATUS_META[c.status]
          return (
            <div key={c.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', borderBottom: '1px solid var(--line)' }}>
              <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                <span className="badge" style={{ color: status.color, borderColor: status.color }}>{status.label}</span>
                <span>{c.title}</span>
                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{c.organizationName}</span>
                {!c.isPublished && <span className="badge medium">Draft</span>}
              </div>
              <div style={{ display: 'flex', gap: 8 }}>
                <Link to={`/contests/${c.id}`}><button className="ghost">View</button></Link>
                <Link to={`/admin/contests/${c.id}/edit`}><button>Edit</button></Link>
                <button onClick={() => handleDelete(c.id)} style={{ color: 'var(--red)' }}>Delete</button>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
