'use client'

import { useState } from 'react'

type Solicitud = { idSolicitud: number; codigoSolicitud: string; nombreCliente?: string; fechaSolicitud?: string; estado?: string }
type Orden = { codigoOrden: string; codigoSolicitud?: string; nombreTecnico?: string; fechaProgramada?: string; horaProgramada?: string; estado?: string }
type HistorialOrden = { idHistorial: number; codigoOrden: string; nombreUsuario?: string; tipoEvento: string; estadoAnterior?: string; estadoNuevo?: string; fechaHora: string; motivo?: string }
type HistorialSolicitud = { idHistorialSolicitud: number; codigoSolicitud: string; nombreUsuario: string; tipoEvento: string; estadoAnterior?: string; estadoNuevo?: string; fechaHora: string; motivo?: string }

type Props = {
  historial: HistorialOrden[]
  historialSolicitudes: HistorialSolicitud[]
  solicitudes: Solicitud[]
  ordenes: Orden[]
}

const fecha = (value?: string) => {
  if (!value) return 'Sin programar'
  const parsed = /^\d{4}-\d{2}-\d{2}$/.test(value)
    ? new Date(Number(value.slice(0, 4)), Number(value.slice(5, 7)) - 1, Number(value.slice(8, 10)))
    : new Date(value)
  return new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium' }).format(parsed)
}

const estado = (value?: string) => {
  const label = value || 'PENDIENTE'
  const tone = /APROB|FINALIZ|CERR/.test(label) ? 'success'
    : /PROGRAM|ASIGN|PROCES/.test(label) ? 'info'
      : /PEND|EVAL/.test(label) ? 'warning' : 'neutral'
  return <span className={`status ${tone}`}>{label}</span>
}

export default function HistorialView({ historial, historialSolicitudes, solicitudes, ordenes }: Props) {
  const [search, setSearch] = useState('')
  const [tab, setTab] = useState<'lifecycle' | 'requests' | 'orders'>('lifecycle')
  const query = search.toLowerCase()
  const lifecycle = solicitudes.filter(item =>
    [item.codigoSolicitud, item.nombreCliente, item.estado].join(' ').toLowerCase().includes(query)
  ).map(item => ({ request: item, order: ordenes.find(order => order.codigoSolicitud === item.codigoSolicitud) }))
  const requestEvents = historialSolicitudes.filter(item =>
    [item.codigoSolicitud, item.nombreUsuario, item.tipoEvento, item.estadoNuevo].join(' ').toLowerCase().includes(query)
  ).sort((a, b) => b.fechaHora.localeCompare(a.fechaHora) || b.idHistorialSolicitud - a.idHistorialSolicitud)
  const orderEvents = historial.filter(item =>
    [item.codigoOrden, item.nombreUsuario, item.tipoEvento, item.estadoNuevo].join(' ').toLowerCase().includes(query)
  ).sort((a, b) => b.fechaHora.localeCompare(a.fechaHora) || b.idHistorial - a.idHistorial)

  return <>
    <div className="page-title"><div><h1>Historial y trazabilidad</h1><p>Ciclo de solicitudes y eventos registrados durante la atención.</p></div></div>
    <section className="panel filter-grid"><label>Buscar<input value={search} onChange={event => setSearch(event.target.value)} placeholder="Solicitud, orden, cliente, usuario o evento"/></label></section>
    <div className="tabs">
      <button className={tab === 'lifecycle' ? 'active' : ''} onClick={() => setTab('lifecycle')}>Ciclo de solicitudes</button>
      <button className={tab === 'requests' ? 'active' : ''} onClick={() => setTab('requests')}>Auditoría de solicitudes</button>
      <button className={tab === 'orders' ? 'active' : ''} onClick={() => setTab('orders')}>Auditoría de órdenes</button>
    </div>
    {tab === 'lifecycle' && <section className="panel"><div className="table-wrap"><table><thead><tr><th>Solicitud</th><th>Cliente</th><th>Estado solicitud</th><th>Orden</th><th>Técnico</th><th>Programación</th><th>Estado final</th></tr></thead><tbody>
      {lifecycle.map(({ request, order }) => <tr key={request.idSolicitud}><td><b>{request.codigoSolicitud}</b><br/><small>{fecha(request.fechaSolicitud)}</small></td><td>{request.nombreCliente || '—'}</td><td>{estado(request.estado)}</td><td>{order?.codigoOrden || 'Sin generar'}</td><td>{order?.nombreTecnico || 'Sin asignar'}</td><td>{order?.fechaProgramada ? fecha(order.fechaProgramada) + ' ' + (order.horaProgramada || '') : 'Sin programar'}</td><td>{estado(order?.estado)}</td></tr>)}
      {!lifecycle.length && <tr><td colSpan={7}>No se encontraron ciclos operativos.</td></tr>}
    </tbody></table></div></section>}
    {tab === 'requests' && <section className="panel"><div className="table-wrap"><table><thead><tr><th>Fecha y hora</th><th>Solicitud</th><th>Usuario</th><th>Evento</th><th>Transición</th><th>Motivo</th></tr></thead><tbody>
      {requestEvents.map(item => <tr key={item.idHistorialSolicitud}><td>{fecha(item.fechaHora)}<br/><small>{item.fechaHora.slice(11, 16)}</small></td><td><b>{item.codigoSolicitud}</b></td><td>{item.nombreUsuario}</td><td>{item.tipoEvento}</td><td>{item.estadoAnterior || '—'} → {item.estadoNuevo || '—'}</td><td>{item.motivo || '—'}</td></tr>)}
      {!requestEvents.length && <tr><td colSpan={6}>No hay eventos para esta búsqueda. Las solicitudes anteriores a esta actualización no tienen auditoría retrospectiva.</td></tr>}
    </tbody></table></div></section>}
    {tab === 'orders' && <section className="panel"><div className="table-wrap"><table><thead><tr><th>Fecha y hora</th><th>Orden</th><th>Usuario</th><th>Evento</th><th>Transición</th><th>Motivo</th></tr></thead><tbody>
      {orderEvents.map(item => <tr key={item.idHistorial}><td>{fecha(item.fechaHora)}<br/><small>{item.fechaHora.slice(11, 16)}</small></td><td><b>{item.codigoOrden}</b></td><td>{item.nombreUsuario || 'Sistema'}</td><td>{item.tipoEvento}</td><td>{item.estadoAnterior || '—'} → {item.estadoNuevo || '—'}</td><td>{item.motivo || '—'}</td></tr>)}
      {!orderEvents.length && <tr><td colSpan={6}>No se encontraron eventos.</td></tr>}
    </tbody></table></div></section>}
  </>
}
