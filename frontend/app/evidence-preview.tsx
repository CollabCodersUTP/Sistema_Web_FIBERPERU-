'use client'

import { useEffect, useRef, useState } from 'react'
import { fetchApiBlob } from '../services/api'

type Props = { id: number; name: string; order: string; onClose: () => void }

export default function EvidencePreview({ id, name, order, onClose }: Props) {
  const dialog = useRef<HTMLDialogElement>(null)
  const [file, setFile] = useState<{ url: string; type: string } | null>(null)
  const [error, setError] = useState('')
  const [previewFailed, setPreviewFailed] = useState(false)

  useEffect(() => {
    const element = dialog.current
    const previousFocus = document.activeElement
    const previousOverflow = document.body.style.overflow
    element?.showModal()
    document.body.style.overflow = 'hidden'
    return () => {
      element?.close()
      document.body.style.overflow = previousOverflow
      if (previousFocus instanceof HTMLElement) previousFocus.focus()
    }
  }, [])

  useEffect(() => {
    let active = true
    let url: string | undefined
    void fetchApiBlob(`/evidencias/${id}/archivo`).then(blob => {
      if (!active) return
      url = URL.createObjectURL(blob)
      setFile({ url, type: blob.type.toLowerCase().split(';')[0] })
    }).catch(reason => {
      if (active) setError(reason instanceof Error ? reason.message : 'No fue posible cargar la evidencia.')
    })
    return () => {
      active = false
      if (url) URL.revokeObjectURL(url)
    }
  }, [id])

  return <dialog ref={dialog} className="evidence-preview" aria-labelledby="evidence-preview-title" onCancel={event => { event.preventDefault(); onClose() }}>
    <header className="evidence-preview-heading">
      <div><h2 id="evidence-preview-title">Vista previa de evidencia</h2><p>{order} · {name}</p></div>
      <button className="secondary" type="button" autoFocus onClick={onClose} aria-label="Cerrar vista previa">Cerrar ×</button>
    </header>
    <div className="evidence-preview-toolbar">
      {file && <a className="primary" href={file.url} download={name}>Descargar archivo</a>}
    </div>
    <div className="evidence-preview-content" aria-busy={!file && !error}>
      {error ? <p className="evidence-preview-error" role="alert">{error}</p> : !file ? <p role="status">Cargando evidencia…</p> : previewFailed ? <p role="alert">No se pudo mostrar la vista previa. Puedes descargar el archivo.</p> : ['image/png', 'image/jpeg'].includes(file.type) ?
        // Authenticated local blob; no image optimization or external URL is needed.
        // eslint-disable-next-line @next/next/no-img-element
        <img src={file.url} alt={name} onError={() => setPreviewFailed(true)} /> : file.type === 'application/pdf' ?
        <iframe src={file.url} title={`Vista previa de ${name}`} onError={() => setPreviewFailed(true)} /> :
        <p>Este formato no admite vista previa. Usa «Descargar archivo».</p>}
    </div>
  </dialog>
}
