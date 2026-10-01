-- ============================================================
-- SISTEMA WEB DE GESTIÓN Y SEGUIMIENTO DE INSTALACIONES (FIBERPERU E.I.R.L.)
-- Script DML: Seeds / Datos Iniciales de Prueba
-- ============================================================

-- 1. Insertar Roles de Usuario (RBAC)
INSERT INTO roles (nombre, descripcion) VALUES
('ROLE_ADMINISTRADOR', 'Administrador total del sistema'),
('ROLE_COORDINADOR', 'Coordinador de instalaciones y programación de técnicos'),
('ROLE_TECNICO', 'Técnico de campo encargado de la instalación de dispositivos'),
('ROLE_CLIENTE', 'Cliente solicitante con acceso a seguimiento de tickets')
ON CONFLICT (nombre) DO NOTHING;

-- 2. Insertar Usuarios por Defecto (Contraseña cifrada con BCrypt: 'admin123' / 'password123')
-- Nota: En producción las contraseñas se generan vía Spring Security BCryptPasswordEncoder
INSERT INTO usuarios (username, password, email, nombre, apellido, telefono, activo) VALUES
('admin', '$2a$10$e.wS.t88lG8g/H9f1y/E5.Ff5X51W03V6Y2Zq28G3tq1R0/N1vK.C', 'admin@fiberperu.pe', 'Carlos', 'Mendoza', '987654321', true),
('coord_juan', '$2a$10$e.wS.t88lG8g/H9f1y/E5.Ff5X51W03V6Y2Zq28G3tq1R0/N1vK.C', 'j.perez@fiberperu.pe', 'Juan', 'Pérez', '912345678', true),
('tec_roberto', '$2a$10$e.wS.t88lG8g/H9f1y/E5.Ff5X51W03V6Y2Zq28G3tq1R0/N1vK.C', 'r.gomez@fiberperu.pe', 'Roberto', 'Gómez', '998877665', true),
('cliente_empresa', '$2a$10$e.wS.t88lG8g/H9f1y/E5.Ff5X51W03V6Y2Zq28G3tq1R0/N1vK.C', 'contacto@innovaperu.com', 'Innova', 'Peru S.A.C.', '955443322', true)
ON CONFLICT (username) DO NOTHING;

-- Asignar Roles a Usuarios
-- Admin (ID 1 -> ROL 1)
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
-- Coordinador (ID 2 -> ROL 2)
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (2, 2) ON CONFLICT DO NOTHING;
-- Técnico (ID 3 -> ROL 3)
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (3, 3) ON CONFLICT DO NOTHING;
-- Cliente (ID 4 -> ROL 4)
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES (4, 4) ON CONFLICT DO NOTHING;

-- 3. Insertar Clientes
INSERT INTO clientes (tipo_documento, numero_documento, razon_social, direccion, referencia, telefono, email, usuario_id) VALUES
('RUC', '20601234567', 'Innova Perú S.A.C.', 'Av. Javier Prado Este 2450, San Borja, Lima', 'Frente a Museo de la Nación', '955443322', 'contacto@innovaperu.com', 4);

-- 4. Insertar Técnicos
INSERT INTO tecnicos (codigo_tecnico, especialidad, estado, usuario_id) VALUES
('TEC-001', 'Fibra Óptica / Redes FTTH', 'DISPONIBLE', 3);

-- 5. Insertar Dispositivos de Ejemplo
INSERT INTO dispositivos (numero_serie, tipo, marca, modelo, mac_address, estado) VALUES
('SN-HG8145V5-001', 'ROUTER_ONT', 'Huawei', 'HG8145V5', 'AA:BB:CC:11:22:33', 'EN_STOCK'),
('SN-HG8145V5-002', 'ROUTER_ONT', 'Huawei', 'HG8145V5', 'AA:BB:CC:11:22:34', 'EN_STOCK'),
('SN-ZTE-F670L-001', 'ROUTER_ONT', 'ZTE', 'F670L', 'DD:EE:FF:44:55:66', 'EN_STOCK'),
('SN-SW-TPLINK-24P', 'SWITCH', 'TP-Link', 'TL-SG1024D', '11:22:33:44:55:66', 'EN_STOCK');

-- 6. Insertar Órden de Instalación de Ejemplo
INSERT INTO ordenes_instalacion (codigo_orden, cliente_id, tecnico_id, fecha_programada, estado, prioridad, direccion_instalacion, observaciones) VALUES
('ORD-2026-0001', 1, 1, '2026-10-02 10:00:00', 'ASIGNADA', 'ALTA', 'Av. Javier Prado Este 2450, San Borja, Lima', 'Instalación de Router ONT de fibra dedicada 500Mbps');
