import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'
import DifficultyBadge from '../components/DifficultyBadge'

export default function ProblemList() {
  const [problems, setProblems] = useState([])
  const [loading, setLoading] = useState(true)
  const [difficulty, setDifficulty] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => {
    setLoading(true)
    const params = {}
    if (difficulty) params.difficulty = difficulty
    if (search) params.search = search
    api.get('/problems', { params })
      .then(res => setProblems(res.data))
      .finally(() => setLoading(false))
  }, [difficulty, search])

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20, gap: 12, flexWrap: 'wrap' }}>
        <h2>Problems</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <input placeholder="Search by title…" value={search} onChange={e => setSearch(e.target.value)} style={{ width: 220 }} />
          <select value={difficulty} onChange={e => setDifficulty(e.target.value)}>
            <option value="">All difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
      </div>

      <div className="panel">
        <div style={{ display: 'grid', gridTemplateColumns: '40px 1fr 110px 1fr 90px', padding: '10px 16px', fontSize: 12, color: 'var(--text-muted)', borderBottom: '1px solid var(--line)' }}>
          <span></span><span>Title</span><span>Difficulty</span><span>Tags</span><span>Accept %</span>
        </div>
        {loading && <div style={{ padding: 24, color: 'var(--text-muted)' }}>Loading…</div>}
        {!loading && problems.length === 0 && <div style={{ padding: 24, color: 'var(--text-muted)' }}>No problems found.</div>}
        {problems.map(p => (
          <Link key={p.id} to={`/problems/${p.id}`}
            style={{ display: 'grid', gridTemplateColumns: '40px 1fr 110px 1fr 90px', padding: '12px 16px', borderBottom: '1px solid var(--line)', alignItems: 'center', fontSize: 14 }}>
            <span style={{ color: p.solvedByUser ? 'var(--green)' : 'var(--line)' }}>{p.solvedByUser ? '✓' : '○'}</span>
            <span>{p.title}</span>
            <DifficultyBadge difficulty={p.difficulty} />
            <span style={{ color: 'var(--text-muted)', fontSize: 12.5 }}>{(p.tags || []).join(', ')}</span>
            <span className="mono" style={{ color: 'var(--text-muted)' }}>{p.acceptanceRate}%</span>
          </Link>
        ))}
      </div>
    </div>
  )
}
