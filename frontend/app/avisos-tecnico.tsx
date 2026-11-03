'use client'

import { useCallback, useEffect, useState } from 'react'
import { fetchApi } from '../services/api'

type AvisoTecnico = {
  idNotificacion: number
  idOrden: number
  codigoOrden: string
  tipoEvento: 'ASIGNACION' | 'REPROGRAMACION'
  mensaje: string
  fechaCreacion: string
  fechaLectura: string | null
}

export default function AvisosTecnico() {
  const [avisos, setAvisos] = useState<AvisoTecnico[]>([])
  const [abierto, setAbierto] = useState(false)
  const [error, setError] = useState('')
  const cargar = useCallback(async () => {
    try {
      setAvisos(await fetchApi<AvisoTecnico[]>('/notificaciones/mias'))
      setError('')
    } catch {
      setError('No se pudieron cargar las notificaciones.')
    }
  }, [])

  useEffect(() => {
    const inicio = window.setTimeout(() => void cargar(), 0)
    const intervalo = window.setInterval(() => void cargar(), 30000)
    return () => { window.clearTimeout(inicio); window.clearInterval(intervalo) }
  }, [cargar])

  const marcarLeido = async (id: number) => {
    try {
      const actualizado = await fetchApi<AvisoTecnico>(`/notificaciones/${id}/leer`, { method: 'PATCH' })
      setAvisos((actuales) => actuales.map((aviso) => aviso.idNotificacion === id ? actualizado : aviso))
      setError('')
    } catch {
      setError('No se pudo marcar la notificación como leída.')
    }
  }
  const pendientes = avisos.filter((aviso) => !aviso.fechaLectura).length

  return <div className="avisos-tecnico">
    <button
      type="button"
      className="avisos-trigger"
      aria-label={`Notificaciones${pendientes ? `, ${pendientes} sin leer` : ''}`}
      aria-expanded={abierto}
      aria-controls="avisos-tecnico-panel"
      onClick={() => setAbierto((actual) => !actual)}
    >
      <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
        <path d="M10 21h4" />
      </svg>
      {pendientes > 0 && <span className="avisos-badge" aria-hidden="true">{pendientes > 99 ? '99+' : pendientes}</span>}
    </button>
    {abierto && <section id="avisos-tecnico-panel" className="avisos-popover" aria-label="Notificaciones del técnico">
      <div className="avisos-heading">
        <div><h2>Notificaciones</h2><p>{pendientes ? `${pendientes} sin leer` : 'Estás al día'}</p></div>
        <button type="button" className="avisos-cerrar" aria-label="Cerrar notificaciones" onClick={() => setAbierto(false)}>×</button>
      </div>
      {error && <p role="alert" className="avisos-error">{error}</p>}
      {avisos.length ? <div className="avisos-lista">{avisos.map((aviso) =>
        <div className={`aviso-item${aviso.fechaLectura ? '' : ' sin-leer'}`} key={aviso.idNotificacion}>
          <b>{aviso.tipoEvento === 'ASIGNACION' ? 'Nueva asignación' : 'Cambio de programación'}</b>
          <span>{aviso.mensaje}</span>
          <small>{aviso.fechaCreacion.replace('T', ' ').slice(0, 16)}</small>
          {!aviso.fechaLectura && <button type="button" className="secondary mini" onClick={() => void marcarLeido(aviso.idNotificacion)}>Marcar como leído</button>}
        </div>
      )}</div> : <p className="avisos-vacio">Las nuevas asignaciones y reprogramaciones aparecerán aquí.</p>}
    </section>}
  </div>
}
