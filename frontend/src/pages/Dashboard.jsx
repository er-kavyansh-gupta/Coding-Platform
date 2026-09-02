import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

function StatCard({ label, value, accent }) {
  return (
    <div className="panel" style={{ padding: '16px 18px', flex: 1 }}>
      <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 6 }}>{label}</div>
      <div style={{ fontFamily: 'var(--font-display)', fontSize: 26, fontWeight: 700, color: accent || 'var(--text)' }}>{value}</div>
    </div>
  )
}

export default function Dashboard() {
  const [data, setData] = useState(null)

  useEffect(() => {
    api.get('/dashboard').then(res => setData(res.data))
  }, [])

  if (!data) return <div className="container" style={{ paddingTop: 40 }}>Loading…</div>

  const maxHeat = Math.max(1, ...data.heatmap.map(h => h.count))

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <h2 style={{ marginBottom: 20 }}>Dashboard</h2>

      <div style={{ display: 'flex', gap: 12, marginBottom: 24, flexWrap: 'wrap' }}>
        <StatCard label="Problems solved" value={data.totalSolved} accent="var(--accent)" />
        <StatCard label="Acceptance rate" value={`${data.acceptanceRate}%`} />
        <StatCard label="Current streak" value={`${data.currentStreak} 🔥`} />
        <StatCard label="Leaderboard rank" value={data.currentRank > 0 ? `#${data.currentRank}` : '—'} />
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16, marginBottom: 24 }}>
        <div className="panel" style={{ padding: 16 }}>
          <h4 style={{ marginBottom: 12, fontSize: 14 }}>Solved by difficulty</h4>
          {Object.entries(data.solvedByDifficulty).map(([diff, count]) => (
            <div key={diff} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '6px 0', borderBottom: '1px solid var(--line)' }}>
              <span>{diff}</span><span className="mono">{count}</span>
            </div>
          ))}
        </div>
        <div className="panel" style={{ padding: 16 }}>
          <h4 style={{ marginBottom: 12, fontSize: 14 }}>Submissions by language</h4>
          {Object.entries(data.languageBreakdown).length === 0 && <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>No submissions yet.</div>}
          {Object.entries(data.languageBreakdown).map(([lang, count]) => (
            <div key={lang} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '6px 0', borderBottom: '1px solid var(--line)' }}>
              <span>{lang}</span><span className="mono">{count}</span>
            </div>
          ))}
        </div>
      </div>

      <div className="panel" style={{ padding: 16, marginBottom: 24 }}>
        <h4 style={{ marginBottom: 12, fontSize: 14 }}>Activity (last {data.heatmap.length} active days)</h4>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 3 }}>
          {data.heatmap.map(h => (
            <div key={h.date} title={`${h.date}: ${h.count} submission(s)`}
              style={{ width: 12, height: 12, borderRadius: 2, background: `rgba(242,183,5,${0.15 + 0.85 * (h.count / maxHeat)})` }} />
          ))}
          {data.heatmap.length === 0 && <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>No activity recorded yet.</div>}
        </div>
      </div>

      <div className="panel">
        <h4 style={{ padding: '14px 16px 0', fontSize: 14 }}>Recent submissions</h4>
        <div style={{ marginTop: 8 }}>
          {data.recentSubmissions.length === 0 && <div style={{ padding: 16, color: 'var(--text-muted)', fontSize: 13 }}>No submissions yet.</div>}
          {data.recentSubmissions.map(s => (
            <Link key={s.submissionId} to={`/problems/${s.problemId}`}
              style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 16px', borderTop: '1px solid var(--line)', fontSize: 13.5 }}>
              <span>{s.problemTitle}</span>
              <span style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                <span className="mono" style={{ color: 'var(--text-muted)' }}>{s.language}</span>
                <span className={`verdict ${s.status.toLowerCase()} mono`}>{s.status.replaceAll('_', ' ')}</span>
              </span>
            </Link>
          ))}
        </div>
      </div>
    </div>
  )
}
