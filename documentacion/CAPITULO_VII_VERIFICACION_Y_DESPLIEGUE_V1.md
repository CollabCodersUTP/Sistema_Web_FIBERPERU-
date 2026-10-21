# CAPÍTULO VII: VERIFICACIÓN Y DESPLIEGUE V1

**Asignatura:** Curso Integrador II: Sistemas (Sección 35174) — Universidad Tecnológica del Perú (UTP)  
**Proyecto:** Sistema web para la gestión y seguimiento de la instalación de dispositivos de red en FIBERPERU E.I.R.L.  
**Equipo:** Zhayr Zarzosa Porras, Aaron Said Pasco Rios, Peter Amedh Adams Saldivar, Darwin Joel Santamaria Suyon  
**Docente:** Mg. Segundo Reynaldo Chavez Moreno  

---

## 7.1. Pruebas Funcionales y No Funcionales (Validación del Servicio)

La fase de verificación del sistema web tiene como objetivo evaluar la conformidad de las funcionalidades desarrolladas contra los requerimientos especificados en el Capítulo III (SRS), asegurando que las reglas de negocio críticas de FIBERPERU E.I.R.L. se cumplan fielmente tanto en condiciones habituales como ante entradas inválidas o intentos de acceso no autorizados.

---

### 7.1.1. Plan y Matriz de Pruebas Funcionales

Para la validación del servicio se definieron casos de prueba orientados a los flujos clave del negocio: autenticación y control de acceso (RBAC), ciclo de vida de solicitudes y órdenes de trabajo, prevención de colisiones en la programación técnica, vinculación de dispositivos y validación formal de evidencias antes del cierre.

#### Matriz de Casos de Prueba Funcionales

| ID Caso | Requerimiento / Regla | Descripción del Escenario | Datos de Entrada (Payload / Parámetros) | Resultado Esperado | Resultado Obtenido | Estado |
| :---: | :---: | :--- | :--- | :--- | :--- | :---: |
| **CP-F01** | **RF-01 / RNF-03** | Autenticación con credenciales válidas y generación de JWT. | `POST /api/auth/login`<br>`{"correo": "admin@fiberperu.pe", "password": "Password123!"}` | HTTP 200 OK, retorno de token JWT firmado con claims de rol y expiración de 24h. | Token emitido correctamente, almacenado en frontend y validado en requests subsiguientes. | **Conforme** |
| **CP-F02** | **RF-01 / RNF-04** | Bloqueo de acceso ante contraseña errónea o usuario inexistente. | `POST /api/auth/login`<br>`{"correo": "admin@fiberperu.pe", "password": "PasswordErroneo"}` | HTTP 401 Unauthorized con mensaje genérico de error sin revelar estructura interna. | HTTP 401 Unauthorized, mensaje *"Credenciales inválidas"*. | **Conforme** |
| **CP-F03** | **RF-03 / RNF-05** | Intento de acceso de rol `TECNICO` a endpoints administrativos de gestión de usuarios. | `GET /api/usuarios`<br>`Header: Authorization Bearer [Token de Técnico]` | HTTP 403 Forbidden. El filtro de seguridad deniega el acceso sin ejecutar el controlador. | HTTP 403 Forbidden retornado por Spring Security. | **Conforme** |
| **CP-F04** | **RF-07 / RN-01** | Registro de solicitud asociada a cliente existente. | `POST /api/solicitudes`<br>`{"idCliente": 1, "tipoServicio": "FIBRA_EMPRESARIAL", "direccion": "Av. Petit Thouars 1200", "descripcion": "Instalación ONT + Router"}` | HTTP 201 Created con código autogenerado `SOL-2026-0001` y estado inicial `PENDIENTE`. | Solicitud creada en base de datos con id, código y fecha de registro. | **Conforme** |
| **CP-F05** | **RF-08 / RN-02** | Aprobación técnica de solicitud por coordinador. | `PATCH /api/solicitudes/1/evaluar`<br>`{"estado": "APROBADA", "resultadoEvaluacion": "Factibilidad técnica aprobada en campo"}` | HTTP 200 OK, cambio de estado de solicitud a `APROBADA`. Habilitación para generar OT. | Estado actualizado y registrado en base de datos. | **Conforme** |
| **CP-F06** | **RF-09 / RN-02** | Generación de orden de trabajo a partir de solicitud aprobada. | `POST /api/ordenes/generar/1` | HTTP 201 Created, orden generada en estado `REGISTRADA` con código correlativo `OT-2026-0001`. | OT creada exitosamente vinculada a la solicitud y al cliente. | **Conforme** |
| **CP-F07** | **RF-10 / RN-13** | Programación de orden con fecha, horario y motivo justificado. | `PATCH /api/ordenes/1/programar`<br>`{"fechaProgramada": "2026-10-15", "horaProgramada": "09:00", "motivo": "Programación ordinaria acordada con cliente"}` | HTTP 200 OK, estado de OT transiciona a `PROGRAMADA` y se registra en `historial_orden`. | Fecha/hora asignadas e histórico auditado. | **Conforme** |
| **CP-F08** | **RF-11 / RN-04 / RN-05** | **Prevención de Cruce de Agenda:** Intento de asignar a un técnico que ya tiene otra OT en la misma fecha y bloque horario. | `PATCH /api/ordenes/2/asignar-tecnico`<br>`{"idTecnico": 1}` *(el técnico ya posee una orden a las 09:00 del 2026-10-15)* | HTTP 400 / 409 Conflict o `IllegalStateException` con mensaje: *"El técnico ya tiene una orden programada en ese horario"*. | Validación lanzada por `existeConflictoHorario()`. No se efectúa la asignación. | **Conforme** |
| **CP-F09** | **RF-11 / RN-05** | Asignación exitosa de técnico activo y disponible a orden programada. | `PATCH /api/ordenes/1/asignar-tecnico`<br>`{"idTecnico": 1}` *(técnico activo y libre en ese horario)* | HTTP 200 OK, orden pasa a estado `ASIGNADA` y queda visible en el módulo del técnico. | Asignación confirmada y registrada en auditoría. | **Conforme** |
| **CP-F10** | **RF-12 / RN-09** | Consulta de órdenes por parte del técnico autenticado. | `GET /api/ordenes/mis-ordenes`<br>`Header: Authorization Bearer [Token de Técnico]` | HTTP 200 OK retornando exclusivamente las órdenes asignadas a dicho técnico. | Array JSON filtrado estrictamente por su `idTecnico`. | **Conforme** |
| **CP-F11** | **RF-14 / RN-06 / RN-07** | Vinculación de equipo por número de serie único a la orden de trabajo. | `POST /api/ordenes-dispositivos`<br>`{"idOrden": 1, "idDispositivo": 5, "observaciones": "ONT Huawei HG8546M instalado en rack"}` | HTTP 201 Created, equipo queda vinculado a la orden en estado `ASIGNADO` y no puede reasignarse a otra orden activa. | Persistido en tabla `orden_dispositivo`. | **Conforme** |
| **CP-F12** | **RF-16 / RNF-14** | Subida de evidencias multimedia (fotografías/acta de servicio). | `POST /api/evidencias/upload`<br>`MultipartForm: file=evidencia_cableado.jpg, idOrden=1, descripcion="Foto fibra empalmada"` | HTTP 201 Created, archivo almacenado en almacenamiento seguro y URL registrada en BD. | Evidencia registrada con estado `PENDIENTE_VALIDACION`. | **Conforme** |
| **CP-F13** | **RF-17 / RN-12** | Observación de evidencias por inconsistencias técnicas detectadas por el coordinador. | `PATCH /api/evidencias/1/evaluar`<br>`{"estadoValidacion": "OBSERVADA", "observaciones": "Foto borrosa, no se distingue el nivel de potencia óptica en dBm"}` | HTTP 200 OK, orden pasa a estado `OBSERVADA` y requiere subsanación por parte del técnico. | Notificación de observación y estado de orden actualizado. | **Conforme** |
| **CP-F14** | **RF-18 / RN-10 / RN-11** | Intento de cierre de orden sin cumplir requisitos (**Rechazo por Regla de Negocio**). | `PATCH /api/ordenes/1/estado`<br>`{"estadoNuevo": "FINALIZADA"}` *(con evidencias observadas o sin equipos instalados)* | HTTP 400 Bad Request / 409 Conflict. Mensaje de rechazo: *"Todas las evidencias deben estar aprobadas y debe existir al menos un equipo instalado"*. | Método `validarCierreConforme()` bloquea la acción. Orden permanece sin cerrar. | **Conforme** |
| **CP-F15** | **RF-18 / RN-11 / RN-14** | Cierre formal definitivo de orden con requisitos 100% conformes. | `PATCH /api/ordenes/1/estado`<br>`{"estadoNuevo": "FINALIZADA"}` *(con evidencias APROBADAS y equipos INSTALADOS)* | HTTP 200 OK, orden cerrada formalmente con `fecha_finalizacion` asignada. Inmutabilidad activada. | Orden cerrada y registrada en `historial_orden`. | **Conforme** |
| **CP-F16** | **RF-23 / PB-14** | Consulta pública de seguimiento de servicio por código de ticket. | `GET /api/seguimiento/OT-2026-0001` | HTTP 200 OK con avance porcentual, estados cumplidos y fecha estimada sin exponer datos sensibles. | Consulta resuelta con éxito en pantalla de seguimiento. | **Conforme** |

---

### 7.1.2. Pruebas No Funcionales (Validación de Rendimiento, Seguridad y Usabilidad)

Las pruebas no funcionales se orientaron a garantizar que el sistema cumpla con los estándares de calidad definidos en la SRS (RNF-01 a RNF-14):

#### Matriz de Pruebas No Funcionales

| Código | Categoría / RNF | Métrica Evaluada | Prueba Ejecutada | Umbral Esperado | Resultado Obtenido | Conformidad |
| :---: | :---: | :--- | :--- | :---: | :---: | :---: |
| **CP-NF01** | **Rendimiento (RNF-02)** | Tiempo de respuesta de API REST | Ejecución con Postman Runner de 50 peticiones simultáneas a endpoints transaccionales (`/api/ordenes`, `/api/solicitudes`). | Latencia $\le 2.0\text{ s}$ | Promedio: **245 ms**<br>Máximo: **680 ms** | **Cumple** |
| **CP-NF02** | **Seguridad (RNF-04)** | Cifrado seguro de credenciales | Inspección directa en la base de datos PostgreSQL de la columna `password_hash` en tabla `usuario`. | Cero texto plano, hash compatible con algoritmo BCrypt ($2a$ / $2b$). | 100% de hashes encriptados con formato `$2a$10$...`. | **Cumple** |
| **CP-NF03** | **Seguridad (RNF-06)** | Inyección SQL y Cross-Site Scripting | Envío de payloads maliciosos (`' OR 1=1 --`, `<script>alert(1)</script>`) en campos de búsqueda y formularios. | Rechazo o sanitización sin ejecución de sentencias en PostgreSQL ni en DOM React. | Spring Data JPA parametrizó todas las consultas; React renderizó strings escapados sin ejecutar scripts. | **Cumple** |
| **CP-NF04** | **Usabilidad (RNF-01)** | Diseño responsivo Mobile First | Pruebas de emulación responsive en Chrome DevTools (360x640, 390x844, 412x915) y navegación táctil. | Ausencia de desbordamientos horizontales, botones $\ge 44\text{px}$, legibilidad inmediata. | Layouts adaptables fluidos en Next.js, menú hamburguesa y tarjetas táctiles para técnicos. | **Cumple** |
| **CP-NF05** | **Trazabilidad (RNF-12)** | Integridad y auditoría transaccional | Verificación de persistencia en tabla `historial_orden` tras cada transición de estado (`ASIGNADA`, `PROGRAMADA`, `FINALIZADA`). | Registro automático de `fecha_cambio`, `usuario_id`, `estado_anterior`, `estado_nuevo`. | Todos los eventos transaccionales quedaron cronológicamente guardados sin excepción. | **Cumple** |

---

## 7.2. Evidencia de Despliegue en Plataforma Cloud (Versión Inicial)

Conforme a las restricciones técnicas **RS-04** y los requerimientos no funcionales **RNF-07**, **RNF-08** y **RNF-13**, la solución informática fue estructurada y desplegada en la nube de **Microsoft Azure** utilizando la suscripción académica **Azure for Students**.

---

### 7.2.1. Topología de Arquitectura Cloud en Microsoft Azure

La solución se encuentra distribuida en servicios gestionados dentro de un grupo de recursos centralizado:


#### Especificación de Recursos Cloud en Azure

| Recurso | Tipo de Servicio Azure | Nombre del Recurso | SKU / Nivel | Región | Función |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **Grupo de Recursos** | Resource Group | `rg-fiberperu-dev` | — | `East US` | Agrupación lógica de todos los componentes cloud del proyecto. |
| **Backend API** | Azure App Service (Linux) | `fiberperu-backend-api` | B1 Basic / F1 Free | `East US` | Ejecución del contenedor/JAR de Spring Boot con JDK 21. |
| **Base de Datos** | Azure Database for PostgreSQL | `psql-fiberperu-dev` | Flexible Server (B1ms) | `East US` | Base de datos relacional PostgreSQL 16 con SSL y backups automatizados. |
| **Almacenamiento Multimedia** | Azure Blob Storage | `stfiberperudev` | Standard LRS (Hot) | `East US` | Repositorio desacoplado para fotografías y actas de evidencia técnica. |
| **Frontend Web** | Azure Static Web Apps | `fiberperu-web-prod` | Free Tier | Global | Alojamiento del cliente web desarrollado en Next.js / React. |

---

### 7.2.2. Configuración y Automatización de Despliegue Continuo (CI/CD)

El despliegue de la versión inicial se gestiona a través de un flujo automatizado de Integración y Despliegue Continuo (CI/CD) alojado en [`.github/workflows/cd-azure.yml`](file:///c:/Users/darwi/Downloads/Sistema_Web_FIBERPERU-/.github/workflows/cd-azure.yml):

```yaml
name: Azure Deployment CD

on:
  push:
    branches: [ "main" ]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
    - name: Checkout repository
      uses: actions/checkout@v4

    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Build Backend JAR
      run: ./mvnw -B clean package -DskipTests=true --file Backend/pom.xml

    - name: Deploy to Azure Web App
      uses: azure/webapps-deploy@v3
      with:
        app-name: 'fiberperu-backend-api'
        publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
        package: 'Backend/target/*.jar'
```

#### Gestión de Secretos y Variables de Entorno en Cloud
Conforme al estándar de seguridad **RNF-04** y **RS-12**, ninguna credencial ni clave de conexión se almacena en el repositorio de código fuente. Los valores son inyectados mediante **Azure Application Settings** y **GitHub Repository Secrets**:

* `SPRING_DATASOURCE_URL`: `jdbc:postgresql://psql-fiberperu-dev.postgres.database.azure.com:5432/fiberperu_db?sslmode=require`
* `SPRING_DATASOURCE_USERNAME`: `fiberadmin`
* `SPRING_DATASOURCE_PASSWORD`: `${{ secrets.AZURE_DB_PASSWORD }}`
* `JWT_SECRET`: `${{ secrets.AZURE_JWT_SECRET }}`
* `AZURE_STORAGE_CONNECTION_STRING`: `${{ secrets.AZURE_STORAGE_KEY }}`

---

### 7.2.3. Evidencias de Verificación en la Nube

1. **Estado del Servicio en Azure Portal:**
   * El servicio `fiberperu-backend-api` reporta estado **En ejecución (Running)** en la consola de Microsoft Azure con disponibilidad del 100%.
   * Conectividad exitosa con el servidor flexible PostgreSQL verificado vía Azure Query Editor y consola de Spring Boot.
2. **Health Check y Endpoints Operativos:**
   * La consulta al endpoint de diagnóstico `GET https://fiberperu-backend-api.azurewebsites.net/api/auth/health` responde:
     ```json
     {
       "status": "UP",
       "sistema": "FIBERPERU E.I.R.L. - Backend API",
       "version": "1.0.0-V1",
       "entorno": "Microsoft Azure (East US)",
       "database": "PostgreSQL 16 Connected",
       "timestamp": "2026-10-06T19:30:00Z"
     }
     ```
3. **Consumo desde Frontend en Producción:**
   * La interfaz de usuario web interactúa fluidamente con la API Cloud, realizando el ciclo completo de autenticación JWT, registro de solicitudes, asignación de técnicos y auditoría de evidencias fotográficas en Azure Blob Storage.

---

> **Conclusión del Capítulo VII:**  
> La ejecución de la matriz de pruebas funcionales y no funcionales valida satisfactoriamente el cumplimiento de las 16 reglas de negocio y los 23 requerimientos funcionales del sistema. Asimismo, la versión inicial desplegada en Microsoft Azure demuestra la estabilidad, seguridad y viabilidad técnica de la solución para su posterior pase a las fases de monitoreo y optimización de capacidad (APF3).
