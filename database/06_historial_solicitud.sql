-- Ejecutar en la base existente antes de publicar el backend de auditoría.
-- Es seguro repetirlo; no reconstruye eventos anteriores sin un actor verificable.
BEGIN;
CREATE TABLE IF NOT EXISTS public.historial_solicitud (
 id_historial_solicitud BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_solicitud BIGINT NOT NULL REFERENCES public.solicitud_instalacion(id_solicitud) ON DELETE CASCADE,
 id_usuario BIGINT NOT NULL REFERENCES public.usuario(id_usuario) ON DELETE RESTRICT,
 tipo_evento VARCHAR(50) NOT NULL,
 estado_anterior VARCHAR(30),
 estado_nuevo VARCHAR(30),
 fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 motivo VARCHAR(500),
 CHECK ((estado_anterior IS NULL AND estado_nuevo IS NULL) OR estado_anterior IS DISTINCT FROM estado_nuevo)
);
CREATE INDEX IF NOT EXISTS idx_historial_solicitud_fecha
 ON public.historial_solicitud(id_solicitud,fecha_hora);
DO $$
BEGIN
 IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'fiberperu_app') THEN
  GRANT SELECT, INSERT ON TABLE public.historial_solicitud TO fiberperu_app;
  GRANT USAGE, SELECT ON SEQUENCE public.historial_solicitud_id_historial_solicitud_seq TO fiberperu_app;
 END IF;
END $$;
COMMIT;
