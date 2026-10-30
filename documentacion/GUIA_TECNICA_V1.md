# Guía técnica de la V1

Esta guía describe cómo preparar y comprobar el sistema FIBERPERU en un entorno local de desarrollo. La evidencia del despliegue inicial en Azure está en el apartado 7.2 del informe APF2.

## Requisitos

- Java 21 para el backend Spring Boot y su Maven Wrapper.
- PostgreSQL y una base de datos local nueva llamada `fiberperu_db`.
- Node.js y pnpm para el frontend Next.js. El proyecto declara pnpm 10.12.1 en `frontend/package.json`.

## Base de datos local

1. Crear la base vacía `fiberperu_db` en PostgreSQL.
2. Ejecutar [`database/01_schema.sql`](../database/01_schema.sql) sobre esa base. **Este script elimina y recrea tablas**; usarlo únicamente en una base de desarrollo que pueda restablecerse.
3. Ejecutar [`database/02_seeds.sql`](../database/02_seeds.sql) si se necesitan las cuentas y datos de demostración. Los datos demo no deben cargarse en una base de producción.
4. Ejecutar [`database/04_codigos_solicitud.sql`](../database/04_codigos_solicitud.sql) después de cargar los datos iniciales. El script prepara el consecutivo anual y reserva los bloques aleatorios existentes.

En una base ya poblada, ejecutar solo `04_codigos_solicitud.sql` después de hacer un respaldo. No volver a ejecutar `01_schema.sql`, porque elimina las tablas y sus datos.

El backend usa `spring.jpa.hibernate.ddl-auto: validate`: comprueba el esquema al iniciar, pero no crea las tablas.

## Backend

Definir estas variables de entorno en la sesión o configuración local antes de iniciar la API. No guardar sus valores reales en Git.

| Variable | Uso |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC; para la base local, `jdbc:postgresql://localhost:5432/fiberperu_db`. |
| `SPRING_DATASOURCE_USERNAME` | Usuario de PostgreSQL. |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de ese usuario. |
| `JWT_SECRET` | Clave privada utilizada para firmar los tokens. |
| `AZURE_STORAGE_CONNECTION_STRING` | Necesaria para cargar evidencias en Azure Blob Storage. |
| `AZURE_STORAGE_CONTAINER_NAME` | Nombre del contenedor; el valor predeterminado es `evidencias`. |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes permitidos del frontend, separados por comas. Por defecto admite `http://localhost:3000` y `http://localhost:5173`. |

Desde la carpeta `Backend`, en PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La API local queda bajo `http://localhost:8080/api/v1`.

## Frontend

Copiar [`frontend/.env.example`](../frontend/.env.example) como `frontend/.env.local`. Para usar el backend local, el archivo debe incluir:

```dotenv
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1
```

Desde la carpeta `frontend`:

```powershell
pnpm install
pnpm dev
```

Abrir `http://localhost:3000`. Si la API se ejecuta en otra dirección, cambiar `NEXT_PUBLIC_API_BASE_URL` por su URL completa, incluido `/api/v1`, y reiniciar el frontend. Las variables `NEXT_PUBLIC_` son visibles en el navegador y no deben contener secretos.

## Comprobación

Con PostgreSQL preparado y las variables del backend configuradas, ejecutar desde `Backend`:

```powershell
.\mvnw.cmd test
```

Para comprobar la compilación del frontend, ejecutar desde `frontend`:

```powershell
pnpm build
```

El frontend usa exportación estática; la compilación genera `frontend/out`. Estas comprobaciones locales no sustituyen las pruebas funcionales ni la evidencia de Azure documentadas en el informe APF2.
