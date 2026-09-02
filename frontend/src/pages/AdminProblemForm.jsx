import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api from '../api/client'

const emptyTestCase = () => ({ input: '', expectedOutput: '', isHidden: true, displayOrder: 0 })

export default function AdminProblemForm() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [form, setForm] = useState({
    title: '', description: '', difficulty: 'EASY', tags: '',
    hints: '', editorial: '', unlockEditorialAfterFailures: 3,
    starterCodeJava: '', starterCodePython: '', starterCodeCpp: '', starterCodeJavascript: '',
    timeLimitMs: 2000, memoryLimitMb: 256,
  })
  const [testCases, setTestCases] = useState([emptyTestCase()])
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!isEdit) return
    api.get(`/problems/${id}`).then(res => {
      const p = res.data
      setForm(f => ({
        ...f,
        title: p.title, description: p.description, difficulty: p.difficulty,
        tags: (p.tags || []).join(', '),
        hints: (p.hints || []).join('\n'),
        editorial: p.editorial || '',
        starterCodeJava: p.starterCodeJava || '', starterCodePython: p.starterCodePython || '',
        starterCodeCpp: p.starterCodeCpp || '', starterCodeJavascript: p.starterCodeJavascript || '',
        timeLimitMs: p.timeLimitMs, memoryLimitMb: p.memoryLimitMb,
      }))
      // Note: GET /problems/:id only returns sample (non-hidden) test cases for security.
      // Use the sample cases as a starting point; add hidden cases fresh when editing.
      setTestCases(p.sampleTestCases.map((tc, i) => ({ input: tc.input, expectedOutput: tc.expectedOutput, isHidden: false, displayOrder: i })))
    })
  }, [id])

  function updateField(key, value) {
    setForm(f => ({ ...f, [key]: value }))
  }

  function updateTestCase(i, key, value) {
    setTestCases(tcs => tcs.map((tc, idx) => idx === i ? { ...tc, [key]: value } : tc))
  }

  function addTestCase() {
    setTestCases(tcs => [...tcs, { ...emptyTestCase(), displayOrder: tcs.length }])
  }

  function removeTestCase(i) {
    setTestCases(tcs => tcs.filter((_, idx) => idx !== i))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSaving(true)
    try {
      const payload = {
        ...form,
        tags: form.tags.split(',').map(t => t.trim()).filter(Boolean),
        hints: form.hints.split('\n').map(h => h.trim()).filter(Boolean),
        testCases: testCases.map((tc, i) => ({ ...tc, displayOrder: i })),
      }
      if (isEdit) {
        await api.put(`/admin/problems/${id}`, payload)
      } else {
        await api.post('/admin/problems', payload)
      }
      navigate('/admin/problems')
    } catch (err) {
      setError(err.response?.data?.message || 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 800 }}>
      <h2 style={{ marginBottom: 20 }}>{isEdit ? 'Edit problem' : 'New problem'}</h2>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        <input placeholder="Title" value={form.title} onChange={e => updateField('title', e.target.value)} required />
        <textarea placeholder="Description (Markdown)" rows={8} value={form.description} onChange={e => updateField('description', e.target.value)} required />

        <div style={{ display: 'flex', gap: 12 }}>
          <select value={form.difficulty} onChange={e => updateField('difficulty', e.target.value)}>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
          <input placeholder="Tags (comma-separated)" value={form.tags} onChange={e => updateField('tags', e.target.value)} style={{ flex: 1 }} />
        </div>

        <div style={{ display: 'flex', gap: 12 }}>
          <input type="number" placeholder="Time limit (ms)" value={form.timeLimitMs} onChange={e => updateField('timeLimitMs', Number(e.target.value))} />
          <input type="number" placeholder="Memory limit (MB)" value={form.memoryLimitMb} onChange={e => updateField('memoryLimitMb', Number(e.target.value))} />
          <input type="number" placeholder="Unlock editorial after N fails" value={form.unlockEditorialAfterFailures} onChange={e => updateField('unlockEditorialAfterFailures', Number(e.target.value))} />
        </div>

        <textarea placeholder="Hints (one per line)" rows={3} value={form.hints} onChange={e => updateField('hints', e.target.value)} />
        <textarea placeholder="Editorial (Markdown)" rows={5} value={form.editorial} onChange={e => updateField('editorial', e.target.value)} />

        <h4 style={{ marginTop: 8 }}>Starter code</h4>
        <textarea className="mono" placeholder="Java starter code" rows={4} value={form.starterCodeJava} onChange={e => updateField('starterCodeJava', e.target.value)} />
        <textarea className="mono" placeholder="Python starter code" rows={4} value={form.starterCodePython} onChange={e => updateField('starterCodePython', e.target.value)} />
        <textarea className="mono" placeholder="C++ starter code" rows={4} value={form.starterCodeCpp} onChange={e => updateField('starterCodeCpp', e.target.value)} />
        <textarea className="mono" placeholder="JavaScript starter code" rows={4} value={form.starterCodeJavascript} onChange={e => updateField('starterCodeJavascript', e.target.value)} />

        <h4 style={{ marginTop: 8 }}>Test cases</h4>
        {testCases.map((tc, i) => (
          <div key={i} className="panel" style={{ padding: 12, display: 'flex', flexDirection: 'column', gap: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <label style={{ fontSize: 13 }}>
                <input type="checkbox" checked={!tc.isHidden} onChange={e => updateTestCase(i, 'isHidden', !e.target.checked)} /> Visible sample case
              </label>
              <button type="button" onClick={() => removeTestCase(i)} style={{ color: 'var(--red)' }}>Remove</button>
            </div>
            <textarea className="mono" placeholder="Input" rows={2} value={tc.input} onChange={e => updateTestCase(i, 'input', e.target.value)} required />
            <textarea className="mono" placeholder="Expected output" rows={2} value={tc.expectedOutput} onChange={e => updateTestCase(i, 'expectedOutput', e.target.value)} required />
          </div>
        ))}
        <button type="button" onClick={addTestCase}>+ Add test case</button>

        {error && <div style={{ color: 'var(--red)' }}>{error}</div>}
        <button className="primary" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save problem'}</button>
      </form>
    </div>
  )
}
