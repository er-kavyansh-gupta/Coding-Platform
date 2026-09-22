import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import { Trophy, CheckCircle2 } from 'lucide-react'
import api from '../api/client'
import DifficultyBadge from '../components/DifficultyBadge'

const STATUS_META = {
  UPCOMING: { label: 'Upcoming', color: 'var(--blue)' },
  ONGOING: { label: 'Live', color: 'var(--green)' },
  ENDED: { label: 'Ended', color: 'var(--text-muted)' },
}

function fmt(dt) {
  return new Date(dt).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export default function ContestDetail() {
  const { id } = useParams()
  const [contest, setContest] = useState(null)
  const [registering, setRegistering] = useState(false)

  function load() {
    api.get(`/contests/${id}`).then(res => setContest(res.data))
  }
  useEffect(load, [id])

  async function handleRegister() {
    setRegistering(true)
    try {
      await api.post(`/contests/${id}/register`)
      load()
    } finally {
      setRegistering(false)
    }
  }

  if (!contest) return <div className="container" style={{ paddingTop: 40 }}>Loading…</div>
  const status = STATUS_META[contest.status]

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 800 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 6 }}>
        <span className="badge" style={{ color: status.color, borderColor: status.color }}>{status.label}</span>
        <h2>{contest.title}</h2>
      </div>
      <p style={{ color: 'var(--text-muted)', fontSize: 13.5, marginBottom: 20 }}>
        Hosted by <strong>{contest.organizationName}</strong> · {fmt(contest.startTime)} → {fmt(contest.endTime)} · {contest.participantCount} registered
      </p>

      <div style={{ display: 'flex', gap: 10, marginBottom: 24 }}>
        {contest.status !== 'ENDED' && !contest.registered && (
          <button className="primary" onClick={handleRegister} disabled={registering}>
            {registering ? 'Registering…' : 'Register for this contest'}
          </button>
        )}
        <Link to={`/contests/${id}/leaderboard`}><button><Trophy size={14} style={{ marginRight: 6, verticalAlign: -2 }} />Leaderboard</button></Link>
      </div>

      {contest.description && (
        <div style={{ fontSize: 14.5, lineHeight: 1.65, marginBottom: 28 }}>
          <ReactMarkdown>{contest.description}</ReactMarkdown>
        </div>
      )}

      <h4 style={{ marginBottom: 12 }}>Problems</h4>

      {contest.status === 'UPCOMING' && (
        <div className="panel" style={{ padding: 16, color: 'var(--text-muted)', fontSize: 13.5 }}>
          Problems are revealed once the contest goes live.
        </div>
      )}

      {contest.status !== 'UPCOMING' && !contest.registered && (
        <div className="panel" style={{ padding: 16, color: 'var(--text-muted)', fontSize: 13.5 }}>
          Register to view and solve this contest's problems.
        </div>
      )}

      {contest.status !== 'UPCOMING' && contest.registered && (
        <div className="panel">
          {contest.problems.map((p, i) => (
            <Link key={p.problemId}
              to={contest.status === 'ONGOING' ? `/contests/${id}/problems/${p.problemId}` : '#'}
              onClick={e => { if (contest.status !== 'ONGOING') e.preventDefault() }}
              style={{
                display: 'grid', gridTemplateColumns: '30px 1fr 110px 70px', alignItems: 'center',
                padding: '12px 16px', borderBottom: i < contest.problems.length - 1 ? '1px solid var(--line)' : 'none',
                opacity: contest.status === 'ONGOING' ? 1 : 0.6, fontSize: 14,
              }}>
              <span style={{ color: p.solvedByUser ? 'var(--green)' : 'var(--line)' }}>
                {p.solvedByUser ? <CheckCircle2 size={16} /> : '○'}
              </span>
              <span>{p.title}</span>
              <DifficultyBadge difficulty={p.difficulty} />
              <span className="mono" style={{ color: 'var(--accent)' }}>{p.points} pts</span>
            </Link>
          ))}
          {contest.status === 'ENDED' && (
            <div style={{ padding: '10px 16px', fontSize: 12.5, color: 'var(--text-muted)' }}>
              This contest has ended — problems are no longer open for submission.
            </div>
          )}
        </div>
      )}
    </div>
  )
}
