-- FIBERPERU E.I.R.L. - ESQUEMA DEFINITIVO POSTGRESQL 18
-- ADVERTENCIA: este script elimina las tablas actuales de public y las recrea.
BEGIN;

-- Modelo definitivo
DROP TABLE IF EXISTS historial_orden CASCADE;
DROP TABLE IF EXISTS observacion_servicio CASCADE;
DROP TABLE IF EXISTS evidencia_instalacion CASCADE;
DROP TABLE IF EXISTS orden_dispositivo CASCADE;
DROP TABLE IF EXISTS dispositivo CASCADE;
DROP TABLE IF EXISTS tipo_dispositivo CASCADE;
DROP TABLE IF EXISTS orden_trabajo CASCADE;
DROP TABLE IF EXISTS solicitud_instalacion CASCADE;
DROP TABLE IF EXISTS tecnico CASCADE;
DROP TABLE IF EXISTS coordinador CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS rol CASCADE;

-- Modelo anterior
DROP TABLE IF EXISTS detalle_orden_dispositivos CASCADE;
DROP TABLE IF EXISTS evidencias_instalacion CASCADE;
DROP TABLE IF EXISTS ordenes_instalacion CASCADE;
DROP TABLE IF EXISTS usuario_roles CASCADE;
DROP TABLE IF EXISTS dispositivos CASCADE;
DROP TABLE IF EXISTS tecnicos CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

CREATE TABLE rol (
 id_rol BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 nombre VARCHAR(50) NOT NULL UNIQUE,
 descripcion VARCHAR(150),
 estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE usuario (
 id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_rol BIGINT NOT NULL REFERENCES rol(id_rol) ON DELETE RESTRICT,
 nombres VARCHAR(100) NOT NULL, apellidos VARCHAR(100) NOT NULL,
 correo VARCHAR(150) NOT NULL UNIQUE, contrasena_hash VARCHAR(255) NOT NULL,
 telefono VARCHAR(20), estado BOOLEAN NOT NULL DEFAULT TRUE,
 fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE coordinador (
 id_coordinador BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_usuario BIGINT NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
 cargo VARCHAR(100) NOT NULL, estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE cliente (
 id_cliente BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_usuario BIGINT NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
 tipo_documento VARCHAR(20) NOT NULL CHECK (tipo_documento IN ('DNI','RUC','CE','PASAPORTE')),
 numero_documento VARCHAR(20) NOT NULL UNIQUE, razon_social VARCHAR(150),
 direccion VARCHAR(250) NOT NULL, estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE tecnico (
 id_tecnico BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_usuario BIGINT NOT NULL UNIQUE REFERENCES usuario(id_usuario) ON DELETE CASCADE,
 especialidad VARCHAR(100), disponibilidad BOOLEAN NOT NULL DEFAULT TRUE,
 estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE solicitud_instalacion (
 id_solicitud BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_cliente BIGINT NOT NULL REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
 codigo_solicitud VARCHAR(20) NOT NULL UNIQUE, tipo_servicio VARCHAR(100) NOT NULL,
 descripcion_servicio VARCHAR(500) NOT NULL, direccion_instalacion VARCHAR(250) NOT NULL,
 fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA'
  CHECK (estado IN ('REGISTRADA','EN_EVALUACION','APROBADA','RECHAZADA','CANCELADA')),
 resultado_evaluacion VARCHAR(30)
  CHECK (resultado_evaluacion IS NULL OR resultado_evaluacion IN ('APROBADA','RECHAZADA')),
 observacion_evaluacion VARCHAR(500)
);
CREATE TABLE orden_trabajo (
 id_orden BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_solicitud BIGINT NOT NULL UNIQUE REFERENCES solicitud_instalacion(id_solicitud) ON DELETE RESTRICT,
 id_coordinador BIGINT NOT NULL REFERENCES coordinador(id_coordinador) ON DELETE RESTRICT,
 id_tecnico BIGINT REFERENCES tecnico(id_tecnico) ON DELETE RESTRICT,
 codigo_orden VARCHAR(20) NOT NULL UNIQUE, fecha_programada DATE, hora_programada TIME,
 fecha_inicio TIMESTAMP, fecha_finalizacion TIMESTAMP,
 estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA'
  CHECK (estado IN ('REGISTRADA','PROGRAMADA','ASIGNADA','EN_PROCESO','OBSERVADA','FINALIZADA','CANCELADA')),
 motivo_reprogramacion VARCHAR(500), observaciones VARCHAR(500),
 CONSTRAINT ck_programacion_completa CHECK ((fecha_programada IS NULL AND hora_programada IS NULL) OR (fecha_programada IS NOT NULL AND hora_programada IS NOT NULL)),
 CONSTRAINT ck_orden_fechas CHECK (fecha_finalizacion IS NULL OR fecha_inicio IS NULL OR fecha_finalizacion >= fecha_inicio)
);
CREATE TABLE tipo_dispositivo (
 id_tipo_dispositivo BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL UNIQUE, descripcion VARCHAR(250),
 estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE dispositivo (
 id_dispositivo BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_tipo_dispositivo BIGINT NOT NULL REFERENCES tipo_dispositivo(id_tipo_dispositivo) ON DELETE RESTRICT,
 marca VARCHAR(80) NOT NULL, modelo VARCHAR(100) NOT NULL,
 numero_serie VARCHAR(100) NOT NULL UNIQUE,
 estado VARCHAR(30) NOT NULL DEFAULT 'EN_STOCK'
  CHECK (estado IN ('EN_STOCK','ASIGNADO','INSTALADO','RETIRADO','DEFECTUOSO','INACTIVO')),
 disponibilidad BOOLEAN NOT NULL DEFAULT TRUE,
 fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE orden_dispositivo (
 id_orden_dispositivo BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_orden BIGINT NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
 id_dispositivo BIGINT NOT NULL REFERENCES dispositivo(id_dispositivo) ON DELETE RESTRICT,
 fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, fecha_retiro TIMESTAMP,
 estado_asignacion VARCHAR(30) NOT NULL DEFAULT 'ASIGNADO'
  CHECK (estado_asignacion IN ('ASIGNADO','INSTALADO','RETIRADO','REEMPLAZADO')),
 observaciones VARCHAR(500), UNIQUE(id_orden,id_dispositivo),
 CHECK (fecha_retiro IS NULL OR fecha_retiro >= fecha_asignacion)
);
CREATE UNIQUE INDEX uq_dispositivo_asignacion_activa ON orden_dispositivo(id_dispositivo)
 WHERE fecha_retiro IS NULL AND estado_asignacion IN ('ASIGNADO','INSTALADO');
CREATE TABLE evidencia_instalacion (
 id_evidencia BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_orden BIGINT NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
 id_coordinador_validador BIGINT REFERENCES coordinador(id_coordinador) ON DELETE RESTRICT,
 nombre_archivo VARCHAR(255) NOT NULL, tipo_archivo VARCHAR(50) NOT NULL,
 url_archivo VARCHAR(500) NOT NULL, descripcion VARCHAR(250),
 fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado_validacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE'
  CHECK (estado_validacion IN ('PENDIENTE','APROBADA','OBSERVADA','RECHAZADA')),
 fecha_validacion TIMESTAMP, observacion_validacion VARCHAR(500),
 CONSTRAINT ck_evidencia_validacion CHECK ((estado_validacion='PENDIENTE' AND id_coordinador_validador IS NULL AND fecha_validacion IS NULL) OR (estado_validacion<>'PENDIENTE' AND id_coordinador_validador IS NOT NULL AND fecha_validacion IS NOT NULL))
);
CREATE TABLE observacion_servicio (
 id_observacion BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_orden BIGINT NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
 id_tecnico BIGINT NOT NULL REFERENCES tecnico(id_tecnico) ON DELETE RESTRICT,
 descripcion VARCHAR(500) NOT NULL,
 fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 estado BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE historial_orden (
 id_historial BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
 id_orden BIGINT NOT NULL REFERENCES orden_trabajo(id_orden) ON DELETE CASCADE,
 id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
 tipo_evento VARCHAR(50) NOT NULL, estado_anterior VARCHAR(30), estado_nuevo VARCHAR(30),
 fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, motivo VARCHAR(500),
 CHECK ((estado_anterior IS NULL AND estado_nuevo IS NULL) OR estado_anterior IS DISTINCT FROM estado_nuevo)
);
CREATE INDEX idx_usuario_rol ON usuario(id_rol);
CREATE INDEX idx_solicitud_cliente ON solicitud_instalacion(id_cliente);
CREATE INDEX idx_solicitud_estado ON solicitud_instalacion(estado);
CREATE INDEX idx_orden_coordinador ON orden_trabajo(id_coordinador);
CREATE INDEX idx_orden_tecnico ON orden_trabajo(id_tecnico);
CREATE INDEX idx_orden_estado ON orden_trabajo(estado);
CREATE INDEX idx_dispositivo_tipo ON dispositivo(id_tipo_dispositivo);
CREATE INDEX idx_orden_dispositivo_orden ON orden_dispositivo(id_orden);
CREATE INDEX idx_evidencia_orden ON evidencia_instalacion(id_orden);
CREATE INDEX idx_observacion_orden ON observacion_servicio(id_orden);
CREATE INDEX idx_historial_orden_fecha ON historial_orden(id_orden,fecha_hora);
COMMIT;
