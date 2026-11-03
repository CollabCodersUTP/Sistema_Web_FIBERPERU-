-- Ejecutar una vez en la base existente antes de publicar el backend de RF-22.
-- Es seguro repetir este archivo; no modifica las órdenes ni los usuarios actuales.
BEGIN;
CREATE TABLE IF NOT EXISTS public.notificacion_tecnico (
 id_notificacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_usuario BIGINT NOT NULL REFERENCES public.usuario(id_usuario) ON DELETE CASCADE,
 id_orden BIGINT NOT NULL REFERENCES public.orden_trabajo(id_orden) ON DELETE CASCADE,
 tipo_evento VARCHAR(30) NOT NULL CHECK (tipo_evento IN ('ASIGNACION', 'REPROGRAMACION')),
 mensaje VARCHAR(250) NOT NULL,
 fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 fecha_lectura TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notificacion_tecnico_usuario
 ON public.notificacion_tecnico(id_usuario,fecha_creacion DESC);
DO $$
BEGIN
 IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'fiberperu_app') THEN
  GRANT SELECT, INSERT, UPDATE ON TABLE public.notificacion_tecnico TO fiberperu_app;
  GRANT USAGE, SELECT ON SEQUENCE public.notificacion_tecnico_id_notificacion_seq TO fiberperu_app;
 END IF;
END $$;
COMMIT;
