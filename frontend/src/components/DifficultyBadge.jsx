import React from 'react'

export default function DifficultyBadge({ difficulty }) {
  const cls = (difficulty || '').toLowerCase()
  return <span className={`badge ${cls}`}>{difficulty}</span>
}
