import React, { useEffect, useState } from 'react'
import api from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function Leaderboard() {
  const { user } = useAuth()
  const [scope, setScope] = useState('GLOBAL')
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api.get('/leaderboard', { params: { scope, limit: 100 } })
      .then(res => setEntries(res.data))
      .finally(() => setLoading(false))
  }, [scope])

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <h2>Leaderboard</h2>
        <select value={scope} onChange={e => setScope(e.target.value)}>
          <option value="GLOBAL">All time</option>
          <option value="WEEKLY">This week</option>
        </select>
      </div>

      <div className="panel">
        <div style={{ display: 'grid', gridTemplateColumns: '60px 1fr 140px 140px 140px', padding: '10px 16px', fontSize: 12, color: 'var(--text-muted)', borderBottom: '1px solid var(--line)' }}>
          <span>Rank</span><span>User</span><span>Solved</span><span>Accuracy</span><span>Score</span>
        </div>
        {loading && <div style={{ padding: 24, color: 'var(--text-muted)' }}>Loading…</div>}
        {!loading && entries.length === 0 && <div style={{ padding: 24, color: 'var(--text-muted)' }}>No activity yet.</div>}
        {entries.map(e => (
          <div key={e.userId}
            style={{
              display: 'grid', gridTemplateColumns: '60px 1fr 140px 140px 140px', padding: '10px 16px',
              borderBottom: '1px solid var(--line)', fontSize: 14,
              background: e.userId === user?.userId ? 'rgba(242,183,5,0.06)' : 'transparent'
            }}>
            <span className="mono" style={{ color: e.rank <= 3 ? 'var(--accent)' : 'var(--text-muted)' }}>#{e.rank}</span>
            <span>{e.username}</span>
            <span className="mono">{e.problemsSolved}</span>
            <span className="mono">{e.acceptanceRate}%</span>
            <span className="mono">{e.weightedScore}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
