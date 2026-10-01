-- ============================================================
-- SISTEMA WEB DE GESTIÓN Y SEGUIMIENTO DE INSTALACIONES (FIBERPERU E.I.R.L.)
-- Script DDL: Creación de Tablas y Relaciones (PostgreSQL)
-- ============================================================

-- Eliminar tablas si existen (orden inverso a dependencias)
DROP TABLE IF EXISTS evidencias_instalacion CASCADE;
DROP TABLE IF EXISTS detalle_orden_dispositivos CASCADE;
DROP TABLE IF EXISTS ordenes_instalacion CASCADE;
DROP TABLE IF EXISTS dispositivos CASCADE;
DROP TABLE IF EXISTS tecnicos CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS usuario_roles CASCADE;
DROP TABLE IF EXISTS usuarios CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- 1. Tabla de Roles (RBAC)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE, -- ROLE_ADMINISTRADOR, ROLE_COORDINADOR, ROLE_TECNICO, ROLE_CLIENTE
    descripcion VARCHAR(255)
);

-- 2. Tabla de Usuarios
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla Intermedia Usuario - Roles
CREATE TABLE usuario_roles (
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 3. Tabla de Clientes
CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    tipo_documento VARCHAR(10) NOT NULL, -- DNI, RUC
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    referencia VARCHAR(255),
    telefono VARCHAR(20),
    email VARCHAR(100),
    usuario_id BIGINT UNIQUE,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 4. Tabla de Técnicos Instaladores
CREATE TABLE tecnicos (
    id BIGSERIAL PRIMARY KEY,
    codigo_tecnico VARCHAR(20) NOT NULL UNIQUE,
    especialidad VARCHAR(100),
    estado VARCHAR(30) DEFAULT 'DISPONIBLE', -- DISPONIBLE, EN_RUTA, OCUPADO, INACTIVO
    usuario_id BIGINT UNIQUE,
    CONSTRAINT fk_tecnico_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 5. Tabla de Dispositivos / Equipos de Red
CREATE TABLE dispositivos (
    id BIGSERIAL PRIMARY KEY,
    numero_serie VARCHAR(100) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL, -- ROUTER_ONT, SWITCH, DECODIFICADOR, CABLE_FIBRA, NAP_SPLITTER
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    mac_address VARCHAR(50),
    estado VARCHAR(30) DEFAULT 'EN_STOCK', -- EN_STOCK, ASIGNADO, INSTALADO, DEFECTUOSO
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. Tabla de Órdenes de Instalación
CREATE TABLE ordenes_instalacion (
    id BIGSERIAL PRIMARY KEY,
    codigo_orden VARCHAR(30) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    tecnico_id BIGINT,
    fecha_programada TIMESTAMP NOT NULL,
    fecha_cierre TIMESTAMP,
    estado VARCHAR(30) DEFAULT 'PENDIENTE', -- PENDIENTE, ASIGNADA, EN_PROCESO, COMPLETADA, CANCELADA
    prioridad VARCHAR(20) DEFAULT 'MEDIA', -- BAJA, MEDIA, ALTA, URGENTE
    direccion_instalacion VARCHAR(255) NOT NULL,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orden_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_orden_tecnico FOREIGN KEY (tecnico_id) REFERENCES tecnicos(id)
);

-- 7. Tabla Detalle de Dispositivos por Órden
CREATE TABLE detalle_orden_dispositivos (
    id BIGSERIAL PRIMARY KEY,
    orden_id BIGINT NOT NULL,
    dispositivo_id BIGINT NOT NULL,
    observacion VARCHAR(255),
    CONSTRAINT fk_detalle_orden FOREIGN KEY (orden_id) REFERENCES ordenes_instalacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_dispositivo FOREIGN KEY (dispositivo_id) REFERENCES dispositivos(id)
);

-- 8. Tabla de Evidencias Multimedia
CREATE TABLE evidencias_instalacion (
    id BIGSERIAL PRIMARY KEY,
    orden_id BIGINT NOT NULL,
    url_archivo VARCHAR(500) NOT NULL,
    tipo_archivo VARCHAR(50), -- FOTO_CONEXION, FOTO_EQUIPO, ACTA_CONFORMIDAD
    descripcion TEXT,
    fecha_carga TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidencia_orden FOREIGN KEY (orden_id) REFERENCES ordenes_instalacion(id) ON DELETE CASCADE
);
