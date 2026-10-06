-- FIBERPERU E.I.R.L. - DATOS DEMO POSTGRESQL 18
-- Ejecutar después de 01_schema.sql.
-- Cuentas demo: contraseña local FiberPeruDemo2026! (hash BCrypt).
BEGIN;
INSERT INTO rol(nombre,descripcion) VALUES
 ('ROLE_ADMINISTRADOR','Administración general del sistema'),
 ('ROLE_COORDINADOR','Coordinación y validación de instalaciones'),
 ('ROLE_TECNICO','Ejecución de instalaciones'),
 ('ROLE_CLIENTE','Registro y consulta de solicitudes');
INSERT INTO usuario(id_rol,nombres,apellidos,correo,contrasena_hash,telefono)
SELECT r.id_rol,v.nombres,v.apellidos,v.correo,v.hash,v.telefono FROM (VALUES
 ('ROLE_ADMINISTRADOR','Carlos','Mendoza','admin@fiberperu.test','$2b$12$9bi1biFe3wzEhgp.20SLF.2ATyBHiI9xV7TaMzXtxRy06wvqIxaem','900000001'),
 ('ROLE_COORDINADOR','Juan','Perez','coordinador@fiberperu.test','$2b$12$9bi1biFe3wzEhgp.20SLF.2ATyBHiI9xV7TaMzXtxRy06wvqIxaem','900000002'),
 ('ROLE_TECNICO','Roberto','Gomez','tecnico@fiberperu.test','$2b$12$9bi1biFe3wzEhgp.20SLF.2ATyBHiI9xV7TaMzXtxRy06wvqIxaem','900000003'),
 ('ROLE_CLIENTE','Ana','Torres','cliente@fiberperu.test','$2b$12$9bi1biFe3wzEhgp.20SLF.2ATyBHiI9xV7TaMzXtxRy06wvqIxaem','900000004')
) v(rol,nombres,apellidos,correo,hash,telefono) JOIN rol r ON r.nombre=v.rol;
INSERT INTO coordinador(id_usuario,cargo) SELECT id_usuario,'Coordinador de instalaciones' FROM usuario WHERE correo='coordinador@fiberperu.test';
INSERT INTO tecnico(id_usuario,especialidad) SELECT id_usuario,'Fibra óptica y redes FTTH' FROM usuario WHERE correo='tecnico@fiberperu.test';
INSERT INTO cliente(id_usuario,tipo_documento,numero_documento,razon_social,direccion) SELECT id_usuario,'RUC','20999999991','Cliente Demo S.A.C.','Av. Demostración 100, Lima' FROM usuario WHERE correo='cliente@fiberperu.test';
INSERT INTO tipo_dispositivo(nombre,descripcion) VALUES
 ('ONT','Terminal óptico para FTTH'),('ROUTER','Equipo de enrutamiento'),
 ('SWITCH','Conmutador de red'),('ACCESS_POINT','Punto de acceso inalámbrico');
INSERT INTO dispositivo(id_tipo_dispositivo,marca,modelo,numero_serie)
SELECT t.id_tipo_dispositivo,v.marca,v.modelo,v.serie FROM (VALUES
 ('ONT','Huawei','HG8145V5','DEMO-HG8145V5-001'),('ONT','ZTE','F670L','DEMO-ZTE-F670L-001'),
 ('SWITCH','TP-Link','TL-SG1024D','DEMO-TPLINK-24P-001'),('ACCESS_POINT','Ubiquiti','UAP-AC-LITE','DEMO-UAP-001')
) v(tipo,marca,modelo,serie) JOIN tipo_dispositivo t ON t.nombre=v.tipo;
INSERT INTO solicitud_instalacion(id_cliente,codigo_solicitud,tipo_servicio,descripcion_servicio,direccion_instalacion,estado,resultado_evaluacion,observacion_evaluacion)
SELECT c.id_cliente,'SOL-2026-0001','INSTALACION_FTTH','Instalación de fibra óptica de demostración.','Av. Demostración 100, Lima','APROBADA','APROBADA','Solicitud académica.' FROM cliente c JOIN usuario u ON u.id_usuario=c.id_usuario WHERE u.correo='cliente@fiberperu.test';
INSERT INTO orden_trabajo(id_solicitud,id_coordinador,id_tecnico,codigo_orden,fecha_programada,hora_programada,estado,observaciones)
SELECT s.id_solicitud,c.id_coordinador,t.id_tecnico,'OT-2026-0001',DATE '2026-10-10',TIME '10:00','ASIGNADA','Orden demo.' FROM solicitud_instalacion s CROSS JOIN coordinador c CROSS JOIN tecnico t WHERE s.codigo_solicitud='SOL-2026-0001';
INSERT INTO orden_dispositivo(id_orden,id_dispositivo,estado_asignacion,observaciones)
SELECT o.id_orden,d.id_dispositivo,'ASIGNADO','Equipo reservado.' FROM orden_trabajo o JOIN dispositivo d ON d.numero_serie='DEMO-HG8145V5-001' WHERE o.codigo_orden='OT-2026-0001';
UPDATE dispositivo SET estado='ASIGNADO',disponibilidad=FALSE WHERE numero_serie='DEMO-HG8145V5-001';
INSERT INTO evidencia_instalacion(id_orden,nombre_archivo,tipo_archivo,url_archivo,descripcion)
SELECT id_orden,'evidencia_demo_01.jpg','FOTO_EQUIPO','https://storage.example.test/evidencias/evidencia_demo_01.jpg','Evidencia simulada pendiente.' FROM orden_trabajo WHERE codigo_orden='OT-2026-0001';
INSERT INTO observacion_servicio(id_orden,id_tecnico,descripcion)
SELECT o.id_orden,t.id_tecnico,'Observación técnica simulada.' FROM orden_trabajo o CROSS JOIN tecnico t WHERE o.codigo_orden='OT-2026-0001';
INSERT INTO historial_orden(id_orden,id_usuario,tipo_evento,estado_anterior,estado_nuevo,motivo)
SELECT o.id_orden,u.id_usuario,'CREACION_Y_ASIGNACION','REGISTRADA','ASIGNADA','Registro automático demo.' FROM orden_trabajo o JOIN usuario u ON u.correo='coordinador@fiberperu.test' WHERE o.codigo_orden='OT-2026-0001';
COMMIT;
