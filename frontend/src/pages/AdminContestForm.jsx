import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/client'

export default function AdminContestForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState({
    title: '', organizationName: '', description: '',
    startTime: '', endTime: '', isPublished: false,
  })
  const [allProblems, setAllProblems] = useState([])
  const [selected, setSelected] = useState({}) // problemId -> points (string) | undefined if not selected
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get('/problems').then(res => setAllProblems(res.data))
  }, [])

  useEffect(() => {
    if (!isEdit) return
    api.get(`/contests/${id}`).then(res => {
      const c = res.data
      setForm({
        title: c.title, organizationName: c.organizationName, description: c.description || '',
        startTime: c.startTime?.slice(0, 16), endTime: c.endTime?.slice(0, 16),
        isPublished: Boolean(c.isPublished),
      })
      const sel = {}
      ;(c.problems || []).forEach(p => { sel[p.problemId] = p.points })
      setSelected(sel)
    })
  }, [id])

  function updateField(key, value) {
    setForm(f => ({ ...f, [key]: value }))
  }

  function toggleProblem(problemId, checked) {
    setSelected(s => {
      const next = { ...s }
      if (checked) next[problemId] = next[problemId] ?? 100
      else delete next[problemId]
      return next
    })
  }

  function updatePoints(problemId, points) {
    setSelected(s => ({ ...s, [problemId]: points }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        ...form,
        problems: Object.entries(selected).map(([problemId, points], i) => ({
          problemId: Number(problemId), points: Number(points), displayOrder: i,
        })),
      }
      if (isEdit) {
        await api.put(`/contests/admin/${id}`, payload)
      } else {
        await api.post('/contests/admin', payload)
      }
      navigate('/admin/contests')
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 760 }}>
      <h2 style={{ marginBottom: 20 }}>{isEdit ? 'Edit contest' : 'New contest'}</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <input placeholder="Contest title" value={form.title} onChange={e => updateField('title', e.target.value)} required />
        <input placeholder="Organization name" value={form.organizationName} onChange={e => updateField('organizationName', e.target.value)} required />
        <textarea placeholder="Description / rules (Markdown)" rows={6} value={form.description} onChange={e => updateField('description', e.target.value)} />

        <div style={{ display: 'flex', gap: 12 }}>
          <div style={{ flex: 1 }}>
            <label style={{ fontSize: 12, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>Start time</label>
            <input type="datetime-local" value={form.startTime} onChange={e => updateField('startTime', e.target.value)} required style={{ width: '100%' }} />
          </div>
          <div style={{ flex: 1 }}>
            <label style={{ fontSize: 12, color: 'var(--text-muted)', display: 'block', marginBottom: 4 }}>End time</label>
            <input type="datetime-local" value={form.endTime} onChange={e => updateField('endTime', e.target.value)} required style={{ width: '100%' }} />
          </div>
        </div>

        <label style={{ fontSize: 13, display: 'flex', alignItems: 'center', gap: 8 }}>
          <input type="checkbox" checked={form.isPublished} onChange={e => updateField('isPublished', e.target.checked)} />
          Published (visible to users — leave unchecked to keep as a draft)
        </label>

        <h4 style={{ marginTop: 8 }}>Problems</h4>
        <div className="panel" style={{ maxHeight: 320, overflow: 'auto' }}>
          {allProblems.map(p => (
            <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 14px', borderBottom: '1px solid var(--line)' }}>
              <input type="checkbox" checked={selected[p.id] !== undefined} onChange={e => toggleProblem(p.id, e.target.checked)} />
              <span style={{ flex: 1, fontSize: 14 }}>{p.title}</span>
              {selected[p.id] !== undefined && (
                <input type="number" value={selected[p.id]} onChange={e => updatePoints(p.id, e.target.value)}
                  style={{ width: 80 }} min={1} />
              )}
            </div>
          ))}
        </div>

        {error && <div style={{ color: 'var(--red)' }}>{error}</div>}
        <button className="primary" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save contest'}</button>
      </form>
    </div>
  )
}
