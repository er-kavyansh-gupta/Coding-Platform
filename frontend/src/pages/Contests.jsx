import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Building2, Users, Clock } from 'lucide-react'
import api from '../api/client'

const STATUS_META = {
  UPCOMING: { label: 'Upcoming', color: 'var(--blue)' },
  ONGOING: { label: 'Live', color: 'var(--green)' },
  ENDED: { label: 'Ended', color: 'var(--text-muted)' },
}

function fmt(dt) {
  return new Date(dt).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export default function Contests() {
  const [contests, setContests] = useState([])
  const [loading, setLoading] = useState(true)

  function load() {
    setLoading(true)
    api.get('/contests').then(res => setContests(res.data)).finally(() => setLoading(false))
  }

  useEffect(load, [])

  async function handleRegister(id, e) {
    e.preventDefault()
    await api.post(`/contests/${id}/register`)
    load()
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <h2 style={{ marginBottom: 6 }}>Contests</h2>
      <p style={{ color: 'var(--text-muted)', fontSize: 14, marginBottom: 24 }}>
        Timed challenges hosted by organizations. Register ahead of time or join once one goes live.
      </p>

      {loading && <div style={{ color: 'var(--text-muted)' }}>Loading…</div>}
      {!loading && contests.length === 0 && <div style={{ color: 'var(--text-muted)' }}>No contests yet.</div>}

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        {contests.map(c => {
          const status = STATUS_META[c.status]
          return (
            <Link key={c.id} to={`/contests/${c.id}`} className="panel" style={{ padding: 18, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 16 }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
                  <span className="badge" style={{ color: status.color, borderColor: status.color }}>{status.label}</span>
                  <h3 style={{ fontSize: 16 }}>{c.title}</h3>
                </div>
                <div style={{ display: 'flex', gap: 16, fontSize: 12.5, color: 'var(--text-muted)', flexWrap: 'wrap' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><Building2 size={13} /> {c.organizationName}</span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><Clock size={13} /> {fmt(c.startTime)} → {fmt(c.endTime)}</span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: 5 }}><Users size={13} /> {c.participantCount} registered</span>
                  <span>{c.problemCount} problem{c.problemCount === 1 ? '' : 's'}</span>
                </div>
              </div>
              {c.status !== 'ENDED' && (
                c.registered
                  ? <span className="badge easy">Registered</span>
                  : <button className="primary" onClick={(e) => handleRegister(c.id, e)}>Register</button>
              )}
            </Link>
          )
        })}
      </div>
    </div>
  )
}
