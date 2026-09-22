import React, { useEffect, useState } from 'react'
import { Award, Lock, CheckCircle2, Printer, X } from 'lucide-react'
import api from '../api/client'

const TIER_META = {
  BRONZE: { label: 'Bronze', color: '#cd7f32', glow: 'rgba(205,127,50,0.18)' },
  SILVER: { label: 'Silver', color: '#c7ccd4', glow: 'rgba(199,204,212,0.18)' },
  GOLD:   { label: 'Gold',   color: '#f2c14e', glow: 'rgba(242,193,78,0.22)' },
}
const TIER_ORDER = ['BRONZE', 'SILVER', 'GOLD']

function CertificateModal({ cert, username, onClose }) {
  const meta = TIER_META[cert.tier]
  return (
    <div className="cert-modal-backdrop" onClick={onClose}>
      <div className="cert-modal" onClick={e => e.stopPropagation()}>
        <div className="cert-modal-toolbar">
          <button className="ghost" onClick={() => window.print()}><Printer size={15} /> Print / Save as PDF</button>
          <button className="ghost" onClick={onClose}><X size={15} /></button>
        </div>
        <div className="certificate-print-area" style={{ borderColor: meta.color }}>
          <div className="certificate-inner" style={{ borderColor: meta.color }}>
            <Award size={40} color={meta.color} strokeWidth={1.5} />
            <div className="certificate-eyebrow" style={{ color: meta.color }}>{meta.label} Certificate of Achievement</div>
            <h1 className="certificate-name">{username}</h1>
            <p className="certificate-body">
              has demonstrated consistent problem-solving skill by successfully solving
              <strong> {cert.problemsSolvedAtIssue} problems </strong>
              on CodeBench, earning the {meta.label} tier.
            </p>
            <div className="certificate-footer">
              <div>
                <div className="certificate-footer-label">Issued</div>
                <div className="mono">{new Date(cert.issuedAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' })}</div>
              </div>
              <div>
                <div className="certificate-footer-label">Serial</div>
                <div className="mono">{cert.serialCode}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function Certificates() {
  const [data, setData] = useState(null)
  const [openCert, setOpenCert] = useState(null)

  useEffect(() => {
    api.get('/certificates').then(res => setData(res.data))
  }, [])

  if (!data) return <div className="container" style={{ paddingTop: 40 }}>Loading…</div>

  const earnedByTier = Object.fromEntries(data.earned.map(c => [c.tier, c]))

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <h2 style={{ marginBottom: 6 }}>Certificates</h2>
      <p style={{ color: 'var(--text-muted)', fontSize: 14, marginBottom: 28 }}>
        Earn a certificate automatically as you solve more problems. You've solved <strong className="mono">{data.totalSolved}</strong> so far.
      </p>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        {TIER_ORDER.map(tier => {
          const meta = TIER_META[tier]
          const cert = earnedByTier[tier]
          const isNext = data.nextTier === tier
          return (
            <div key={tier} className="panel" style={{
              padding: 22, textAlign: 'center', position: 'relative', overflow: 'hidden',
              borderColor: cert ? meta.color : 'var(--line)',
            }}>
              {cert && <div style={{ position: 'absolute', inset: 0, background: `radial-gradient(circle at 50% -10%, ${meta.glow}, transparent 70%)`, pointerEvents: 'none' }} />}
              <div style={{ position: 'relative' }}>
                <Award size={34} color={cert ? meta.color : 'var(--line)'} strokeWidth={1.6} style={{ marginBottom: 10 }} />
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 16, marginBottom: 4, color: cert ? meta.color : 'var(--text)' }}>
                  {meta.label}
                </div>

                {cert && (
                  <>
                    <div style={{ fontSize: 12.5, color: 'var(--text-muted)', marginBottom: 14, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
                      <CheckCircle2 size={13} color="var(--green)" /> Earned {new Date(cert.issuedAt).toLocaleDateString()}
                    </div>
                    <button className="primary" onClick={() => setOpenCert(cert)}>View certificate</button>
                  </>
                )}

                {!cert && isNext && (
                  <>
                    <div style={{ fontSize: 12.5, color: 'var(--text-muted)', marginBottom: 10 }}>
                      {data.problemsUntilNextTier} more problem{data.problemsUntilNextTier === 1 ? '' : 's'} to unlock
                    </div>
                    <div style={{ height: 6, borderRadius: 3, background: 'var(--bg-inset)', overflow: 'hidden' }}>
                      <div style={{
                        height: '100%', borderRadius: 3, background: meta.color,
                        width: `${Math.min(100, (data.totalSolved / (data.totalSolved + data.problemsUntilNextTier)) * 100)}%`,
                      }} />
                    </div>
                  </>
                )}

                {!cert && !isNext && (
                  <div style={{ fontSize: 12.5, color: 'var(--text-muted)', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 4 }}>
                    <Lock size={13} /> Locked
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>

      {openCert && <CertificateModal cert={openCert} username={data.username} onClose={() => setOpenCert(null)} />}
    </div>
  )
}
