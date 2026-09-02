import React, { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import Editor from '@monaco-editor/react'
import ReactMarkdown from 'react-markdown'
import api from '../api/client'
import DifficultyBadge from '../components/DifficultyBadge'
import VerdictTable from '../components/VerdictTable'

const LANG_META = {
  JAVA: { monaco: 'java', label: 'Java' },
  PYTHON: { monaco: 'python', label: 'Python' },
  CPP: { monaco: 'cpp', label: 'C++' },
  JAVASCRIPT: { monaco: 'javascript', label: 'JavaScript' },
}

export default function ProblemSolve() {
  const { id } = useParams()
  const [problem, setProblem] = useState(null)
  const [language, setLanguage] = useState('JAVA')
  const [code, setCode] = useState('')
  const [running, setRunning] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [result, setResult] = useState(null)
  const [tab, setTab] = useState('description') // description | hints | editorial
  const [visibleHints, setVisibleHints] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get(`/problems/${id}`).then(res => {
      setProblem(res.data)
      setCode(starterFor(res.data, language))
    })
  }, [id])

  function starterFor(p, lang) {
    if (!p) return ''
    return {
      JAVA: p.starterCodeJava,
      PYTHON: p.starterCodePython,
      CPP: p.starterCodeCpp,
      JAVASCRIPT: p.starterCodeJavascript,
    }[lang] || ''
  }

  function handleLanguageChange(lang) {
    setLanguage(lang)
    setCode(starterFor(problem, lang))
  }

  async function run(runOnly) {
    setError('')
    runOnly ? setRunning(true) : setSubmitting(true)
    setResult(null)
    try {
      const { data } = await api.post('/submissions', {
        problemId: Number(id),
        language,
        sourceCode: code,
        runOnly,
      })
      setResult(data)
      if (!runOnly && data.overallStatus === 'ACCEPTED') {
        // refresh problem so the solved checkmark / editorial unlock state updates
        api.get(`/problems/${id}`).then(res => setProblem(res.data))
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Submission failed')
    } finally {
      setRunning(false)
      setSubmitting(false)
    }
  }

  if (!problem) return <div className="container" style={{ paddingTop: 40 }}>Loading…</div>

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', height: 'calc(100vh - 56px)' }}>
      {/* Left: description */}
      <div className="scrollpane" style={{ borderRight: '1px solid var(--line)', padding: '24px 28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
          <h2 style={{ fontSize: 20 }}>{problem.title}</h2>
          <DifficultyBadge difficulty={problem.difficulty} />
        </div>
        <div style={{ display: 'flex', gap: 16, borderBottom: '1px solid var(--line)', marginBottom: 16, fontSize: 13 }}>
          {['description', 'hints', 'editorial'].map(t => (
            <button key={t} className="ghost" onClick={() => setTab(t)}
              style={{ padding: '8px 0', borderRadius: 0, borderBottom: tab === t ? '2px solid var(--accent)' : '2px solid transparent', color: tab === t ? 'var(--text)' : 'var(--text-muted)' }}>
              {t[0].toUpperCase() + t.slice(1)}
            </button>
          ))}
        </div>

        {tab === 'description' && (
          <div className="markdown-body" style={{ fontSize: 14.5, lineHeight: 1.65 }}>
            <ReactMarkdown>{problem.description}</ReactMarkdown>
          </div>
        )}

        {tab === 'hints' && (
          <div>
            {(problem.hints || []).length === 0 && <p style={{ color: 'var(--text-muted)' }}>No hints for this problem.</p>}
            {(problem.hints || []).slice(0, visibleHints).map((h, i) => (
              <div key={i} className="panel" style={{ padding: 12, marginBottom: 8, fontSize: 13.5 }}>{h}</div>
            ))}
            {visibleHints < (problem.hints || []).length && (
              <button onClick={() => setVisibleHints(v => v + 1)}>Reveal hint {visibleHints + 1}</button>
            )}
          </div>
        )}

        {tab === 'editorial' && (
          problem.editorialUnlocked
            ? <div style={{ fontSize: 14.5, lineHeight: 1.65 }}><ReactMarkdown>{problem.editorial || ''}</ReactMarkdown></div>
            : <p style={{ color: 'var(--text-muted)' }}>Editorial unlocks after a few failed attempts, or once you solve this problem.</p>
        )}
      </div>

      {/* Right: editor + results */}
      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 16px', borderBottom: '1px solid var(--line)' }}>
          <select value={language} onChange={e => handleLanguageChange(e.target.value)}>
            {Object.entries(LANG_META).map(([key, m]) => <option key={key} value={key}>{m.label}</option>)}
          </select>
          <div style={{ display: 'flex', gap: 8 }}>
            <button onClick={() => run(true)} disabled={running || submitting}>{running ? 'Running…' : 'Run'}</button>
            <button className="primary" onClick={() => run(false)} disabled={running || submitting}>{submitting ? 'Submitting…' : 'Submit'}</button>
          </div>
        </div>

        <div style={{ flex: '1 1 55%', minHeight: 200 }}>
          <Editor
            height="100%"
            theme="vs-dark"
            language={LANG_META[language].monaco}
            value={code}
            onChange={v => setCode(v ?? '')}
            options={{ fontSize: 13, fontFamily: 'JetBrains Mono, monospace', minimap: { enabled: false }, automaticLayout: true }}
          />
        </div>

        <div className="scrollpane" style={{ flex: '1 1 45%', borderTop: '1px solid var(--line)', padding: 16, background: 'var(--bg-inset)' }}>
          {error && <div style={{ color: 'var(--red)', marginBottom: 12 }}>{error}</div>}
          {!result && !error && <div style={{ color: 'var(--text-muted)', fontSize: 13 }}>Run or submit to see results here.</div>}
          <VerdictTable submission={result} />
        </div>
      </div>
    </div>
  )
}
