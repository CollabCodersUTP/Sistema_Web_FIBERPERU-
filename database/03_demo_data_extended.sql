-- FIBERPERU E.I.R.L. - DATOS DEMO AMPLIADOS
-- Ejecutar sobre fiberperu_db después de 01_schema.sql y 02_seeds.sql.
-- No elimina información y puede ejecutarse más de una vez.
BEGIN;

INSERT INTO usuario(id_rol,nombres,apellidos,correo,contrasena_hash,telefono)
SELECT r.id_rol,v.nombres,v.apellidos,v.correo,'$2b$12$9bi1biFe3wzEhgp.20SLF.2ATyBHiI9xV7TaMzXtxRy06wvqIxaem',v.telefono
FROM (VALUES
 ('ROLE_CLIENTE','Luis','Ramírez','luis.ramirez@fiberperu.test','910000011'),
 ('ROLE_CLIENTE','María','Quispe','maria.quispe@fiberperu.test','910000012'),
 ('ROLE_CLIENTE','Empresa','Andina','andina@fiberperu.test','910000013'),
 ('ROLE_CLIENTE','Jorge','Salazar','jorge.salazar@fiberperu.test','910000014'),
 ('ROLE_TECNICO','Miguel','Torres','miguel.torres@fiberperu.test','920000011'),
 ('ROLE_TECNICO','Andrea','Rojas','andrea.rojas@fiberperu.test','920000012'),
 ('ROLE_COORDINADOR','Rosa','Medina','rosa.medina@fiberperu.test','930000011')
) v(rol,nombres,apellidos,correo,telefono)
JOIN rol r ON r.nombre=v.rol
ON CONFLICT (correo) DO NOTHING;

INSERT INTO cliente(id_usuario,tipo_documento,numero_documento,razon_social,direccion)
SELECT u.id_usuario,v.tipo,v.documento,v.razon,v.direccion
FROM (VALUES
 ('luis.ramirez@fiberperu.test','DNI','70112233',NULL,'Av. Los Próceres 450, Lima'),
 ('maria.quispe@fiberperu.test','DNI','71445566',NULL,'Jr. Las Flores 225, Lima'),
 ('andina@fiberperu.test','RUC','20601234567','Empresa Andina S.A.C.','Av. Industrial 780, Callao'),
 ('jorge.salazar@fiberperu.test','DNI','72889911',NULL,'Calle Los Olivos 120, Lima')
) v(correo,tipo,documento,razon,direccion)
JOIN usuario u ON u.correo=v.correo
ON CONFLICT (numero_documento) DO NOTHING;

INSERT INTO tecnico(id_usuario,especialidad)
SELECT u.id_usuario,v.especialidad FROM (VALUES
 ('miguel.torres@fiberperu.test','Cableado estructurado y switching'),
 ('andrea.rojas@fiberperu.test','Fibra óptica, ONT y redes inalámbricas')
) v(correo,especialidad) JOIN usuario u ON u.correo=v.correo
WHERE NOT EXISTS (SELECT 1 FROM tecnico t WHERE t.id_usuario=u.id_usuario);

INSERT INTO coordinador(id_usuario,cargo)
SELECT u.id_usuario,'Supervisora de operaciones' FROM usuario u
WHERE u.correo='rosa.medina@fiberperu.test'
AND NOT EXISTS (SELECT 1 FROM coordinador c WHERE c.id_usuario=u.id_usuario);

INSERT INTO dispositivo(id_tipo_dispositivo,marca,modelo,numero_serie)
SELECT t.id_tipo_dispositivo,v.marca,v.modelo,v.serie FROM (VALUES
 ('ONT','Huawei','HG8245H','DEMO-HG8245H-002'), ('ONT','ZTE','F680','DEMO-ZTE-F680-002'),
 ('ROUTER','TP-Link','Archer C6','DEMO-ARCHER-C6-001'), ('ROUTER','MikroTik','hAP ac2','DEMO-HAPAC2-001'),
 ('SWITCH','Cisco','CBS110-8T','DEMO-CISCO-8T-001'), ('SWITCH','TP-Link','TL-SG108','DEMO-TPLINK-8P-002'),
 ('ACCESS_POINT','Ubiquiti','U6-Lite','DEMO-U6LITE-002'), ('ACCESS_POINT','TP-Link','EAP225','DEMO-EAP225-001')
) v(tipo,marca,modelo,serie) JOIN tipo_dispositivo t ON t.nombre=v.tipo
ON CONFLICT (numero_serie) DO NOTHING;

INSERT INTO solicitud_instalacion(id_cliente,codigo_solicitud,tipo_servicio,descripcion_servicio,direccion_instalacion,estado,resultado_evaluacion,observacion_evaluacion)
SELECT c.id_cliente,v.codigo,v.servicio,v.descripcion,v.direccion,v.estado,v.resultado,v.observacion
FROM (VALUES
 ('luis.ramirez@fiberperu.test','SOL-2026-0002','INSTALACION_FTTH','Instalación residencial de fibra óptica.','Av. Los Próceres 450, Lima','REGISTRADA',NULL,NULL),
 ('maria.quispe@fiberperu.test','SOL-2026-0003','INSTALACION_FTTH','Internet y configuración Wi-Fi.','Jr. Las Flores 225, Lima','APROBADA','APROBADA','Cobertura confirmada.'),
 ('andina@fiberperu.test','SOL-2026-0004','RED_EMPRESARIAL','Instalación de switch y puntos de acceso.','Av. Industrial 780, Callao','APROBADA','APROBADA','Visita técnica conforme.'),
 ('jorge.salazar@fiberperu.test','SOL-2026-0005','INSTALACION_FTTH','Alta de servicio residencial.','Calle Los Olivos 120, Lima','RECHAZADA','RECHAZADA','Zona aún sin cobertura.'),
 ('luis.ramirez@fiberperu.test','SOL-2026-0006','SOPORTE_RED','Reubicación de ONT y router.','Av. Los Próceres 450, Lima','APROBADA','APROBADA','Trabajo programable.')
) v(correo,codigo,servicio,descripcion,direccion,estado,resultado,observacion)
JOIN usuario u ON u.correo=v.correo JOIN cliente c ON c.id_usuario=u.id_usuario
ON CONFLICT (codigo_solicitud) DO NOTHING;

INSERT INTO orden_trabajo(id_solicitud,id_coordinador,id_tecnico,codigo_orden,fecha_programada,hora_programada,estado,observaciones)
SELECT s.id_solicitud,c.id_coordinador,t.id_tecnico,v.codigo,v.fecha,v.hora,v.estado,v.observacion
FROM (VALUES
 ('SOL-2026-0003','OT-2026-0002',DATE '2026-10-12',TIME '09:00','PROGRAMADA','Instalación residencial programada.','miguel.torres@fiberperu.test'),
 ('SOL-2026-0004','OT-2026-0003',DATE '2026-10-13',TIME '14:00','EN_PROCESO','Despliegue empresarial en ejecución.','andrea.rojas@fiberperu.test'),
 ('SOL-2026-0006','OT-2026-0004',DATE '2026-10-15',TIME '11:30','ASIGNADA','Reubicación de equipos.','tecnico@fiberperu.test')
) v(solicitud,codigo,fecha,hora,estado,observacion,correo_tecnico)
JOIN solicitud_instalacion s ON s.codigo_solicitud=v.solicitud
JOIN usuario ut ON ut.correo=v.correo_tecnico JOIN tecnico t ON t.id_usuario=ut.id_usuario
CROSS JOIN LATERAL (SELECT id_coordinador FROM coordinador ORDER BY id_coordinador LIMIT 1) c
ON CONFLICT (codigo_orden) DO NOTHING;

INSERT INTO historial_orden(id_orden,id_usuario,tipo_evento,estado_anterior,estado_nuevo,motivo)
SELECT o.id_orden,u.id_usuario,'DATOS_DEMO',NULL,o.estado,'Evento inicial de demostración ampliada.'
FROM orden_trabajo o CROSS JOIN LATERAL (SELECT id_usuario FROM usuario WHERE correo='admin@fiberperu.test') u
WHERE o.codigo_orden IN ('OT-2026-0002','OT-2026-0003','OT-2026-0004')
AND NOT EXISTS (SELECT 1 FROM historial_orden h WHERE h.id_orden=o.id_orden AND h.tipo_evento='DATOS_DEMO');

COMMIT;
