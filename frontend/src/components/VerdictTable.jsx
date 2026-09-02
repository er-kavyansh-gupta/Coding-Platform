import React from 'react'

function fmtVerdict(v) {
  return (v || '').replaceAll('_', ' ')
}

export default function VerdictTable({ submission }) {
  if (!submission) return null

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'baseline', gap: 10, marginBottom: 12 }}>
        <span className={`verdict ${submission.overallStatus?.toLowerCase()}`} style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 18 }}>
          {fmtVerdict(submission.overallStatus)}
        </span>
        {submission.totalCount > 0 && (
          <span className="mono" style={{ color: 'var(--text-muted)', fontSize: 13 }}>
            {submission.passedCount}/{submission.totalCount} test cases passed
          </span>
        )}
      </div>

      {submission.compileError && (
        <pre className="mono scrollpane" style={{ background: 'var(--bg-inset)', border: '1px solid var(--line)', borderRadius: 4, padding: 12, color: 'var(--red)', fontSize: 12.5, maxHeight: 220 }}>
          {submission.compileError}
        </pre>
      )}

      {submission.results?.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 12 }}>
          {submission.results.map((r, i) => (
            <div key={r.testCaseId} className="panel" style={{ padding: '10px 14px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className="mono" style={{ fontSize: 13 }}>
                  Test case {i + 1} {r.hidden && <span style={{ color: 'var(--text-muted)' }}>(hidden)</span>}
                </span>
                <span className={`verdict ${r.verdict?.toLowerCase()} mono`} style={{ fontSize: 12.5 }}>
                  {fmtVerdict(r.verdict)} · {r.executionTimeMs ?? 0}ms
                </span>
              </div>
              {!r.hidden && (
                <div style={{ marginTop: 8, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, fontSize: 12.5 }}>
                  <div>
                    <div style={{ color: 'var(--text-muted)', marginBottom: 4 }}>Input</div>
                    <pre className="mono scrollpane" style={{ margin: 0, background: 'var(--bg-inset)', padding: 6, borderRadius: 4, maxHeight: 90 }}>{r.input}</pre>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-muted)', marginBottom: 4 }}>Expected</div>
                    <pre className="mono scrollpane" style={{ margin: 0, background: 'var(--bg-inset)', padding: 6, borderRadius: 4, maxHeight: 90 }}>{r.expectedOutput}</pre>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-muted)', marginBottom: 4 }}>Your output</div>
                    <pre className="mono scrollpane" style={{ margin: 0, background: 'var(--bg-inset)', padding: 6, borderRadius: 4, maxHeight: 90 }}>{r.actualOutput}</pre>
                  </div>
                </div>
              )}
              {r.stderr && (
                <pre className="mono scrollpane" style={{ marginTop: 8, color: 'var(--red)', fontSize: 12, maxHeight: 80 }}>{r.stderr}</pre>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
