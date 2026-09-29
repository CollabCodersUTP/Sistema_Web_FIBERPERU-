# 🌐 Sistema Web para la Gestión y Seguimiento de Instalación de Dispositivos de Red

> **Empresa:** FIBERPERU E.I.R.L. (RUC: 20514491187)  
> **Curso:** Curso Integrador II: Sistemas (UTP)  
> **Estado del Proyecto:** En Desarrollo (Agosto 2026 - Diciembre 2026)

---

## 📋 Tabla de Contenidos
1. [Descripción del Proyecto](#-descripción-del-proyecto)
2. [Problema y Oportunidad de Innovación](#-problema-y-oportunidad-de-innovación)
3. [Arquitectura y Stack Tecnológico](#-arquitectura-y-stack-tecnológico)
4. [Roles del Sistema](#-roles-del-sistema)
5. [Estructura del Repositorio](#-estructura-del-repositorio)
6. [Planificación Ágil (Scrum)](#-planificación-ágil-scrum)
7. [Equipo de Desarrollo](#-equipo-de-desarrollo)

---

## 🚀 Descripción del Proyecto
El proyecto consiste en el desarrollo de un **sistema web integral** diseñado para optimizar los procesos de programación, asignación de técnicos, control de stock de dispositivos, seguimiento en tiempo real y cierre de órdenes de servicio de instalación de redes de fibra óptica y equipos de comunicaciones en **FIBERPERU E.I.R.L.**

La solución busca centralizar la trazabilidad operativa desde el momento en que se registra una solicitud de servicio por parte del cliente hasta la validación y conformidad final de las evidencias técnicas.

---

## 💡 Problema y Oportunidad de Innovación
* **El Problema:** La gestión descentralizada de solicitudes, la falta de visibilidad en tiempo real del estado de las órdenes, la duplicidad de información y las dificultades para consultar el historial de instalaciones afectan la eficiencia operativa y la supervisión de los servicios.
* **La Solución:** Una plataforma web centralizada que automatiza la asignación de personal técnico según su disponibilidad, controla el inventario mediante números de serie únicos, almacena evidencias multimedia y genera reportes para la toma de decisiones gerenciales.

---

## ⚙️ Arquitectura y Stack Tecnológico
El sistema ha sido estructurado bajo un modelo cliente-servidor robusto, seguro y escalable:

* **Backend:** Java con Spring Boot, Spring Data JPA, Hibernate, Spring Security y JSON Web Tokens (JWT).
* **Frontend:** React, HTML5, CSS3 y JavaScript[cite: 1].
* **Base de Datos:** PostgreSQL (almacenamiento centralizado)[cite: 1].
* **Control de Versiones:** Git y GitHub[cite: 1].
* **Integración Continua:** GitHub Actions[cite: 1].
* **Entorno Cloud / Despliegue:** Microsoft Azure[cite: 1].
* **Herramientas de Apoyo:** IntelliJ IDEA Community Edition, Postman, Maven, Figma, Bizagi Modeler y Draw.io[cite: 1].

---

## 👥 Roles del Sistema
El acceso a las funcionalidades del sistema está protegido mediante control de roles (RBAC):
1. **Administrador:** Gestión total de usuarios, roles, permisos, catálogos de dispositivos, clientes, técnicos y reportes de desempeño[cite: 1].
2. **Coordinador de Instalaciones:** Recepción y aprobación de solicitudes, generación de órdenes de trabajo, programación de fechas y asignación inteligente de técnicos[cite: 1].
3. **Técnico Instalador:** Consulta de órdenes asignadas, actualización de estados, vinculación de dispositivos físicos mediante números de serie, registro de observaciones y carga de evidencias[cite: 1].
4. **Cliente Solicitante (Módulo opcional):** Registro de solicitudes y seguimiento en tiempo real del avance de sus instalaciones mediante tickets[cite: 1].

---

## 📂 Estructura del Repositorio
El repositorio se encuentra organizado de la siguiente manera para mantener un código limpio y mantenible:

```text
fiberperu-instalaciones/
│
├── backend/          # API REST desarrollada en Java / Spring Boot[cite: 1]
├── frontend/         # Interfaz de usuario desarrollada en React[cite: 1]
├── documentacion/    # Documentos formales del proyecto (SRS, Project Charter, etc.)[cite: 1]
├── diagramas/        # Modelos BPMN, diagramas de procesos y arquitectura[cite: 1]
├── .gitignore        # Archivos ignorados por el control de versiones[cite: 1]
└── README.md         # Documentación principal del repositorio[cite: 1]
