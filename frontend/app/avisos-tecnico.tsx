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
  const [error, setError] = useState('')
  const cargar = useCallback(async () => {
    try {
      setAvisos(await fetchApi<AvisoTecnico[]>('/notificaciones/mias'))
      setError('')
    } catch {
      setError('No se pudieron cargar los avisos.')
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
      setError('No se pudo marcar el aviso como leído.')
    }
  }
  const pendientes = avisos.filter((aviso) => !aviso.fechaLectura).length

  return <section className="panel avisos-tecnico" aria-label="Avisos de órdenes">
    <div className="panel-heading"><div><h2>Avisos de órdenes</h2><p>{pendientes ? `${pendientes} sin leer` : 'No tienes avisos sin leer.'}</p></div></div>
    {error && <p role="alert" className="section-help">{error}</p>}
    {avisos.length ? <div className="avisos-lista">{avisos.map((aviso) =>
      <div className={`aviso-item${aviso.fechaLectura ? '' : ' sin-leer'}`} key={aviso.idNotificacion}>
        <div><b>{aviso.tipoEvento === 'ASIGNACION' ? 'Nueva asignación' : 'Cambio de programación'}</b>
          <span>{aviso.mensaje}</span>
          <small>{aviso.fechaCreacion.replace('T', ' ').slice(0, 16)}</small>
        </div>
        {!aviso.fechaLectura && <button className="secondary mini" onClick={() => void marcarLeido(aviso.idNotificacion)}>Marcar como leído</button>}
      </div>
    )}</div> : <p className="section-help">Las nuevas asignaciones y reprogramaciones aparecerán aquí.</p>}
  </section>
}
