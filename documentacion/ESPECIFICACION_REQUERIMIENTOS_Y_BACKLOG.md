# 📑 Especificación de Requerimientos de Software (SRS), Reglas de Negocio y Product Backlog

**Proyecto:** Sistema web para la gestión y seguimiento de la instalación de dispositivos de red en FIBERPERU E.I.R.L.  
**Asignatura:** Curso Integrador II: Sistemas (Sección 35174) — Universidad Tecnológica del Perú (UTP)  
**Periodo:** 2026  

---

## 📌 1. Introducción y Propósito del Sistema

El sistema web centraliza y gestiona integralmente el ciclo de vida de instalación de dispositivos de red en **FIBERPERU E.I.R.L.**, desde la captación y evaluación de solicitudes de clientes hasta la asignación de técnicos en campo, control de stock por números de serie, auditoría de evidencias fotográficas y el cierre formal de órdenes de trabajo.

---

## 🎯 2. Product Backlog (PB)

Alineado con el framework ágil Scrum, el Product Backlog organiza las necesidades del negocio en historias de usuario priorizadas:

| ID | Historia de Usuario | Prioridad | Módulo | Estado en el Sistema |
| :---: | :--- | :---: | :---: | :---: |
| **PB-01** | Como Administrador, quiero iniciar sesión de forma segura y gestionar roles para controlar el acceso de los usuarios al sistema. | Alta | Seguridad y Auth | ✅ **Implementado** (`AuthController`, Spring Security, JWT, BCrypt) |
| **PB-02** | Como Administrador, quiero registrar y administrar los datos de los clientes para mantener organizada la cartera. | Alta | Clientes | ✅ **Implementado** (`ClienteController`, `ClienteService`) |
| **PB-03** | Como Coordinador, quiero registrar solicitudes de instalación para clientes nuevos, iniciando el ciclo de atención. | Alta | Solicitudes | ✅ **Implementado** (`SolicitudInstalacionController`) |
| **PB-04** | Como Coordinador, quiero generar una orden de trabajo a partir de una solicitud aprobada para gestionar el servicio. | Alta | Órdenes (Coordinación) | ✅ **Implementado** (`POST /api/ordenes/generar/{id}`) |
| **PB-05** | Como Administrador, quiero registrar el catálogo de dispositivos de red (marca, modelo, serie) para controlar los equipos disponibles. | Alta | Inventario/Equipos | ✅ **Implementado** (`DispositivoController`, `TipoDispositivoController`) |
| **PB-06** | Como Coordinador, quiero agendar fechas y asignar técnicos disponibles a una orden, evitando cruces de horarios. | Alta | Programación | ✅ **Implementado** (`OrdenTrabajoService.asignarTecnico`) |
| **PB-07** | Como Técnico Instalador, quiero consultar la lista detallada de mis instalaciones asignadas para organizar mi día de trabajo. | Alta | Órdenes (Técnico) | ✅ **Implementado** (`GET /api/ordenes/mis-ordenes`) |
| **PB-08** | Como Técnico Instalador, quiero subir fotografías y documentos como evidencia del servicio terminado directamente desde el campo. | Alta | Evidencias | ✅ **Implementado** (`EvidenciaInstalacionController`) |
| **PB-09** | Como Técnico Instalador, quiero actualizar el estado de una orden (ej. En proceso, Finalizada) para informar el avance en tiempo real. | Media | Seguimiento | ✅ **Implementado** (`PATCH /api/ordenes/{id}/estado`) |
| **PB-10** | Como Técnico Instalador, quiero vincular los números de serie de los dispositivos físicos que instalé a la orden de trabajo actual. | Media | Instalación | ✅ **Implementado** (`OrdenDispositivoController`) |
| **PB-11** | Como Coordinador, quiero aprobar o rechazar (observar) las evidencias enviadas por los técnicos antes de cerrar formalmente una orden. | Media | Validación y Cierre | ✅ **Implementado** (`EvidenciaInstalacionService.evaluarEvidencia`) |
| **PB-12** | Como Coordinador, quiero consultar el historial completo de instalaciones realizadas para mantener la trazabilidad operativa. | Media | Trazabilidad | ✅ **Implementado** (`HistorialOrdenController` + auditoría de BD) |
| **PB-13** | Como Administrador, quiero visualizar un dashboard y generar reportes con KPIs operativos para supervisar el desempeño general. | Media | Reportes | 🟡 **Frontend en integración** (Métricas de BD listas) |
| **PB-14** | Como Cliente, quiero consultar el estado de avance de mis solicitudes desde mi cuenta para realizar seguimiento al servicio. | Baja | Seguimiento Cliente | ✅ **Implementado** (`SeguimientoPublicoController`) |

---

## ⚙️ 3. Requerimientos Funcionales (RF-01 a RF-23)

| Código | Requerimiento Funcional | Rol Autorizado | Componente Técnico Backend | Estado |
| :---: | :--- | :---: | :---: | :---: |
| **RF-01** | Inicio y cierre de sesión mediante credenciales de acceso (correo/usuario y contraseña). | Todos | `AuthController.java` | ✅ Implementado |
| **RF-02** | Registrar, consultar, actualizar y desactivar usuarios del sistema. | Administrador | `UsuarioController.java` | ✅ Implementado |
| **RF-03** | Gestionar roles (`ADMINISTRADOR`, `COORDINADOR`, `TECNICO`, `CLIENTE`) y permisos para cada perfil. | Administrador | `UsuarioService.java`, `SecurityConfig.java` | ✅ Implementado |
| **RF-04** | Registrar, consultar, actualizar y desactivar perfiles de clientes. | Administrador, Coordinador | `ClienteController.java` | ✅ Implementado |
| **RF-05** | Registrar, consultar, actualizar y definir la disponibilidad de los técnicos instaladores. | Administrador, Coordinador | `TecnicoController.java` | ✅ Implementado |
| **RF-06** | Registrar y gestionar el catálogo y stock de dispositivos de red (marca, modelo, tipo y número de serie). | Administrador | `DispositivoController.java`, `TipoDispositivoController.java` | ✅ Implementado |
| **RF-07** | Registrar solicitudes de instalación asociando el tipo de servicio y los datos del cliente. | Cliente, Coordinador | `SolicitudInstalacionController.java` | ✅ Implementado |
| **RF-08** | Consultar, aprobar o rechazar el estado de las solicitudes de instalación evaluadas. | Coordinador | `SolicitudInstalacionService.evaluarSolicitud` | ✅ Implementado |
| **RF-09** | Generar una orden de trabajo (OT) a partir de una solicitud de instalación aprobada. | Coordinador | `OrdenTrabajoController.generarDesdeSolicitud` | ✅ Implementado |
| **RF-10** | Programar la fecha y hora de una instalación verificando el calendario operativo. | Coordinador | `OrdenTrabajoService.programar` | ✅ Implementado |
| **RF-11** | Asignar un técnico instalador a una orden de trabajo, validando que no tenga conflictos de horario (cruce de agenda). | Coordinador | `OrdenTrabajoService.asignarTecnico` | ✅ Implementado |
| **RF-12** | Consultar por parte del técnico el listado y detalle de las órdenes que le fueron asignadas. | Técnico | `OrdenTrabajoController.listarMisOrdenes` | ✅ Implementado |
| **RF-13** | Actualizar el estado de una orden de instalación durante su ejecución (`REGISTRADA`, `PROGRAMADA`, `ASIGNADA`, `EN_PROCESO`, `OBSERVADA`, `FINALIZADA`). | Técnico, Coordinador | `OrdenTrabajoService.cambiarEstado` | ✅ Implementado |
| **RF-14** | Vincular los números de serie de los dispositivos físicos instalados a la orden de trabajo actual. | Técnico | `OrdenDispositivoController.java` | ✅ Implementado |
| **RF-15** | Registrar observaciones, anomalías o notas técnicas sobre el desarrollo de la instalación. | Técnico, Coordinador | `ObservacionServicioController.java` | ✅ Implementado |
| **RF-16** | Adjuntar evidencias multimedia de la instalación, aceptando formatos de imagen (JPG, PNG) y documentos (PDF). | Técnico | `EvidenciaInstalacionController.java` | ✅ Implementado |
| **RF-17** | Revisar evidencias por el coordinador; en caso de inconsistencias, devolver la orden al técnico para su subsanación. | Coordinador | `EvidenciaInstalacionService.evaluarEvidencia` | ✅ Implementado |
| **RF-18** | Cerrar definitivamente una orden cuando la instalación, los equipos y sus evidencias hayan sido validadas como conformes. | Coordinador | `OrdenTrabajoService.validarCierreConforme` | ✅ Implementado |
| **RF-19** | Consultar el historial completo de solicitudes, órdenes e instalaciones realizadas (Trazabilidad operativa). | Administrador, Coordinador | `HistorialOrdenController.java` | ✅ Implementado |
| **RF-20** | Buscar y filtrar órdenes por múltiples criterios: cliente, técnico, rango de fechas y estado actual. | Administrador, Coordinador | `OrdenTrabajoRepository.java` | ✅ Implementado |
| **RF-21** | Generar reportes consolidados (en formatos exportables) sobre instalaciones programadas, pendientes, ejecutadas y cerradas. | Administrador | `ReporteService.java` / Dashboard | 🟡 En integración |
| **RF-22** | Generar automáticamente una alerta o notificación al técnico cuando se le asigne o modifique una orden. | Sistema | Eventos de dominio | 🟡 En integración |
| **RF-23** | Consultar el estado de avance de solicitudes e instalaciones por parte del cliente mediante número de ticket/código. | Cliente, Público | `SeguimientoPublicoController.java` | ✅ Implementado |

---

## 🛡️ 4. Requerimientos No Funcionales (RNF-01 a RNF-14)

| Código | Categoría | Requerimiento No Funcional | Solución Técnica Implementada |
| :---: | :---: | :--- | :--- |
| **RNF-01** | Usabilidad | Diseño responsivo (*Mobile First*), garantizando visualización fluida en smartphones de técnicos en campo. | Framework React / Next.js con TailwindCSS y diseño viewport responsivo. |
| **RNF-02** | Rendimiento | Tiempo máximo de respuesta $\le 2$ segundos en operaciones habituales de consulta y registro bajo red estándar. | Índices relacionales en PostgreSQL, DTOs ligeros y consultas JPQL optimizadas. |
| **RNF-03** | Seguridad (Auth) | Autenticación y manejo de sesiones de la API REST de forma sin estado (*Stateless*) mediante tokens JWT. | `JwtAuthenticationFilter` en Spring Security con validación de firma Bearer token. |
| **RNF-04** | Seguridad (Datos) | Encriptación asimétrica/hash fuerte de contraseñas de usuario, prohibiendo almacenamiento en texto plano. | Uso de `BCryptPasswordEncoder` (cost factor 10) al registrar y autenticar credenciales. |
| **RNF-05** | Autorización | Validación de roles en cada petición HTTP denegando accesos no autorizados con código HTTP 403 Forbidden. | Configuración de reglas `hasRole()` / `hasAnyRole()` en `SecurityConfig.java`. |
| **RNF-06** | Seguridad (Validación) | Sanitización y validación estricta de todos los datos de entrada para prevenir inyecciones SQL y ataques XSS. | Anotaciones Jakarta Bean Validation (`@NotNull`, `@Size`, `@Pattern`) y JPA Parameter Binding. |
| **RNF-07** | Disponibilidad | Arquitectura orientada a un Uptime operativo del 99.9% durante las ventanas de servicio de la empresa. | Servicios desacoplados en contenedores Docker y despliegue en Microsoft Azure. |
| **RNF-08** | Respaldo | Base de datos relacional con políticas de respaldo (*backups*) automatizados diarios y almacenamiento redundante. | Políticas de snapshots periódicos de base de datos PostgreSQL en cloud storage. |
| **RNF-09** | Compatibilidad | Compatibilidad cruzada en navegadores basados en Chromium (Google Chrome, Microsoft Edge) y Mozilla Firefox. | Estándares web ECMAScript 6+ y soporte multiplataforma verificado. |
| **RNF-10** | Escalabilidad | Arquitectura modular de capas (Controladores, Servicios, Repositorios) que permite anexar módulos sin impacto al core. | Arquitectura en capas desacopladas en Spring Boot orientada a microcomponentes. |
| **RNF-11** | Mantenibilidad | Código fuente versionado semánticamente a través de Git y GitHub, con gestión formal en Maven (`pom.xml`) y pnpm. | Control de dependencias estricto y ramas estructuradas (`main`, `develop`, `feature/*`). |
| **RNF-12** | Trazabilidad | Tablas transaccionales con campos de auditoría (`fecha_creacion`, `fecha_actualizacion`, usuario responsable). | Campos de timestamp y tabla `historial_orden` para registro inmutable de transiciones de estado. |
| **RNF-13** | Integración | Pipeline de Integración y Despliegue Continuo (CI/CD) automatizado con GitHub Actions hacia Microsoft Azure. | Workflows configurados en `.github/workflows` para compilación y test suite. |
| **RNF-14** | Almacenamiento | Almacenamiento físico de evidencias fotográficas en servicio Cloud / Blob Storage, persistiendo en BD solo la URL. | Entidad `evidencia_instalacion` almacena la URL de acceso y metadatos de validación. |

---

## ⚖️ 5. Reglas de Negocio (RN-01 a RN-16)

Las reglas de negocio implementadas en el backend garantizan la integridad de los datos y el cumplimiento de las políticas operativas de FIBERPERU:

| Código | Categoría | Regla de Negocio | Validación en Backend |
| :---: | :---: | :--- | :--- |
| **RN-01** | Solicitudes | Toda solicitud de instalación deberá estar asociada a un cliente registrado en el sistema. | `SolicitudInstalacionService`: comprueba existencia de `Cliente` antes de crear la solicitud. |
| **RN-02** | Órdenes | Una orden de instalación solo podrá generarse a partir de una solicitud previamente registrada y aprobada. | `OrdenTrabajoService.generarDesdeSolicitud`: valida que el estado de la solicitud sea estrictamente `APROBADA`. |
| **RN-03** | Programación | Cada orden deberá tener un coordinador responsable, una fecha programada y un técnico asignado antes de iniciar su ejecución. | `OrdenTrabajoService.cambiarEstado`: no permite pasar a `EN_PROCESO` sin técnico asignado. |
| **RN-04** | Asignación | Un técnico no podrá ser asignado a dos instalaciones programadas para la misma fecha y horario (**Prevención de cruce de agenda**). | `OrdenRepository.existeConflictoHorario()` detecta colisiones de agenda previniendo la asignación. |
| **RN-05** | Asignación | Solo podrán asignarse técnicos que se encuentren activos y disponibles en el sistema. | `OrdenTrabajoService.asignarTecnico`: comprueba `tecnico.getEstado() == true` y `tecnico.getDisponibilidad() == true`. |
| **RN-06** | Dispositivos | Los dispositivos de red deberán registrarse obligatoriamente con su tipo, marca, modelo y número de serie único. | Restricción única en base de datos (`UNIQUE numero_serie`) y validaciones en `DispositivoService`. |
| **RN-07** | Dispositivos | Un dispositivo con un número de serie único no podrá asignarse simultáneamente a más de una orden de instalación activa. | `OrdenDispositivoService`: verifica que el dispositivo no esté asociado a otra orden activa. |
| **RN-08** | Seguimiento | El estado de una orden debe seguir el flujo estricto: `REGISTRADA` $\to$ `PROGRAMADA` $\to$ `ASIGNADA` $\to$ `EN_PROCESO` $\to$ `OBSERVADA` $\to$ `FINALIZADA` / `CANCELADA`. | Validado en `OrdenTrabajoService.ESTADOS_PERMITIDOS` y lógica secuencial de transiciones. |
| **RN-09** | Autorización | El técnico solo podrá modificar o interactuar con las órdenes de instalación que tenga formalmente asignadas a su nombre. | Endpoint `/api/ordenes/mis-ordenes` valida la identidad del técnico logueado mediante JWT. |
| **RN-10** | Ejecución | Para marcar una instalación como finalizada, el técnico debe registrar obligatoriamente dispositivos instalados, observaciones y evidencias multimedia. | `validarCierreConforme`: comprueba existencia de dispositivos con estado `INSTALADO` y evidencias cargadas. |
| **RN-11** | Validación | Una orden solo podrá cerrarse definitivamente después de que el coordinador revise y apruebe formalmente las evidencias. | `validarCierreConforme`: valida que el 100% de las evidencias estén en estado `APROBADA`. |
| **RN-12** | Validación | Si las evidencias presentan observaciones o inconsistencias, la orden debe regresar al técnico para su subsanación antes del cierre. | Transición de estado a `OBSERVADA` con notificación y registro de observaciones pendientes. |
| **RN-13** | Reprogramación | Toda reprogramación deberá exigir el registro de la nueva fecha, el motivo del cambio y el usuario responsable de la acción. | `OrdenTrabajoService.programar`: parámetro `motivo` obligatorio, registrado en entidad y en historial. |
| **RN-14** | Trazabilidad | Las órdenes cerradas (`FINALIZADA` o `CANCELADA`) no podrán eliminarse ni modificarse directamente. | Bloqueo estricto ante intentos de edición de órdenes cerradas. |
| **RN-15** | Auditoría | Las acciones relevantes realizadas sobre solicitudes y órdenes deberán registrar de forma automática la fecha, hora y el usuario responsable. | Registro transaccional en tabla `historial_orden` (`fecha_cambio`, `usuario_id`, `estado_anterior`, `estado_nuevo`, `detalle`). |
| **RN-16** | Seguridad | Cada usuario solo podrá acceder a las funciones y a la información estrictamente autorizadas para su rol (**Principio de mínimo privilegio**). | Configuración de seguridad Spring Security con RBAC granular en todos los endpoints REST. |

---

## 🚫 6. Restricciones del Sistema (RS-01 a RS-12)

| Código | Categoría | Restricción |
| :---: | :---: | :--- |
| **RS-01** | Arquitectura | La aplicación es estrictamente web (no de escritorio). |
| **RS-02** | Académica / Tecnológica | Backend desarrollado al 100% en Java y Spring Boot (consigna académica UTP). |
| **RS-03** | Arquitectura | Persistencia centralizada en base de datos relacional PostgreSQL 16. |
| **RS-04** | Infraestructura | Despliegue en Microsoft Azure utilizando la suscripción Azure for Students. |
| **RS-05** | Operativa / Seguridad | Acceso condicionado a conexión a internet y credenciales JWT válidas. |
| **RS-06** | Cronograma | Desarrollo acotado estrictamente entre el 10 de agosto y el 7 de diciembre de 2026. |
| **RS-07** | Presupuesto | Empleo de herramientas gratuitas, de código abierto o con licencias educativas (Costo de software S/. 0.00). |
| **RS-08** | Alcance / Integración | No contempla integración directa vía API con plataformas externas de operadores (Claro, Movistar, etc.). |
| **RS-09** | Plataforma Móvil | No se desarrollará app móvil nativa; el acceso en campo se realiza por navegador web con diseño responsivo. |
| **RS-10** | Alcance Funcional | No incluye módulos de facturación electrónica SUNAT, contabilidad general ni nóminas. |
| **RS-11** | Infraestructura | Capacidad de almacenamiento multimedia acotada por los límites del tier gratuito de la nube. |
| **RS-12** | Seguridad de Datos | Uso exclusivo de datos de prueba simulados y anonimizados para proteger la privacidad de la empresa. |
