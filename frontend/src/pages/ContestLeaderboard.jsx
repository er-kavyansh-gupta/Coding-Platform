import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function ContestLeaderboard() {
  const { id } = useParams()
  const { user } = useAuth()
  const [entries, setEntries] = useState([])
  const [contestTitle, setContestTitle] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([
      api.get(`/contests/${id}/leaderboard`),
      api.get(`/contests/${id}`),
    ]).then(([lb, detail]) => {
      setEntries(lb.data)
      setContestTitle(detail.data.title)
    }).finally(() => setLoading(false))
  }, [id])

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <Link to={`/contests/${id}`} style={{ fontSize: 13, color: 'var(--text-muted)' }}>← Back to contest</Link>
      <h2 style={{ margin: '10px 0 20px' }}>{contestTitle || 'Contest'} · Leaderboard</h2>

      <div className="panel">
        <div style={{ display: 'grid', gridTemplateColumns: '60px 1fr 120px 140px 120px', padding: '10px 16px', fontSize: 12, color: 'var(--text-muted)', borderBottom: '1px solid var(--line)' }}>
          <span>Rank</span><span>User</span><span>Solved</span><span>Penalty (min)</span><span>Points</span>
        </div>
        {loading && <div style={{ padding: 24, color: 'var(--text-muted)' }}>Loading…</div>}
        {!loading && entries.length === 0 && <div style={{ padding: 24, color: 'var(--text-muted)' }}>No one has solved a problem yet.</div>}
        {entries.map(e => (
          <div key={e.userId}
            style={{
              display: 'grid', gridTemplateColumns: '60px 1fr 120px 140px 120px', padding: '10px 16px',
              borderBottom: '1px solid var(--line)', fontSize: 14,
              background: e.userId === user?.userId ? 'rgba(242,183,5,0.06)' : 'transparent'
            }}>
            <span className="mono" style={{ color: e.rank <= 3 ? 'var(--accent)' : 'var(--text-muted)' }}>#{e.rank}</span>
            <span>{e.username}</span>
            <span className="mono">{e.problemsSolved}</span>
            <span className="mono">{e.totalPenaltyMinutes}</span>
            <span className="mono">{e.totalPoints}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
