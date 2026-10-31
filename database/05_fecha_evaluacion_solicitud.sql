-- Ejecutar en la base existente antes de publicar el backend actualizado.
-- Las solicitudes evaluadas anteriormente permanecen sin fecha de evaluación.
ALTER TABLE public.solicitud_instalacion
  ADD COLUMN IF NOT EXISTS fecha_evaluacion TIMESTAMP;
