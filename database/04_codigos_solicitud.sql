-- Ejecutar después de cargar los datos iniciales y antes de publicar
-- el backend que genera los códigos. Es seguro repetir este archivo.
CREATE TABLE IF NOT EXISTS solicitud_codigo_consecutivo (
 anio INTEGER PRIMARY KEY,
 ultimo_numero INTEGER NOT NULL CHECK (ultimo_numero BETWEEN 1 AND 9999)
);

CREATE TABLE IF NOT EXISTS solicitud_codigo_bloque (
 bloque VARCHAR(6) PRIMARY KEY CHECK (bloque ~ '^[A-Z0-9]{6}$')
);

-- Reserva los bloques de los códigos que ya tengan el formato nuevo.
INSERT INTO solicitud_codigo_bloque (bloque)
SELECT DISTINCT split_part(codigo_solicitud, '-', 3)
FROM solicitud_instalacion
WHERE codigo_solicitud ~ '^SOL-[0-9]{4}-[A-Z0-9]{6}-[0-9]{4}$'
ON CONFLICT (bloque) DO NOTHING;

-- Continúa la numeración anual después del mayor sufijo de los códigos anteriores.
INSERT INTO solicitud_codigo_consecutivo (anio, ultimo_numero)
SELECT split_part(codigo_solicitud, '-', 2)::integer,
       MAX(regexp_replace(codigo_solicitud, '^.*-', '')::integer)
FROM solicitud_instalacion
WHERE codigo_solicitud ~ '^SOL-[0-9]{4}-([0-9]{1,4}|[A-Z0-9]{6}-[0-9]{4})$'
GROUP BY split_part(codigo_solicitud, '-', 2)::integer
ON CONFLICT (anio) DO UPDATE
SET ultimo_numero = GREATEST(
    solicitud_codigo_consecutivo.ultimo_numero,
    EXCLUDED.ultimo_numero
);

-- La API usa fiberperu_app en Azure. En desarrollo local ese rol puede no existir.
DO $$
BEGIN
 IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'fiberperu_app') THEN
  GRANT SELECT, INSERT ON TABLE public.solicitud_codigo_bloque TO fiberperu_app;
  GRANT SELECT, INSERT, UPDATE ON TABLE public.solicitud_codigo_consecutivo TO fiberperu_app;
 END IF;
END $$;
