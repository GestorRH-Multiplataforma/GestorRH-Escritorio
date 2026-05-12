# GestorRH - Cliente de Escritorio (Administración)

[![CI Escritorio](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml/badge.svg)](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml)
[![Release](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/release.yml/badge.svg)](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/release.yml)
[![Version](https://img.shields.io/badge/version-v1.0.0-brightgreen)](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/releases/tag/v1.0.0)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)](https://openjfx.io/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Panel de control administrativo del ecosistema **GestorRH**. Desarrollado en **JavaFX 21**, permite a las empresas centralizar la gestión de sus recursos humanos mediante una interfaz moderna y eficiente, consumiendo de forma exclusiva la **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** REST sin acceso directo a base de datos.

> Este repositorio forma parte del ecosistema **GestorRH Multiplataforma**. Para entender el contexto general del proyecto (API, cliente móvil y arquitectura global), consulta el [README de la organización](https://github.com/GestorRH-Multiplataforma#gestorrh---ecosistema-multiplataforma).
 
---

## Tecnologías Utilizadas

- **Lenguaje:** Java 21 (LTS)
- **Framework UI:** JavaFX 21.0.1 con FXML y estilos CSS personalizados
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Gestor de Dependencias:** Maven con perfiles `dev` / `prod`
- **Red:** Retrofit 2 & OkHttp 4
- **CI/CD:** GitHub Actions
- **Internacionalización:** `ResourceBundle` para Español (ES) e Inglés (EN)
---

## Requisitos Previos

### Para usar la aplicación
- Descarga el instalador para tu plataforma desde la [última release](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/releases/latest).
- El instalador incluye su propio JRE. **No necesitas instalar Java.**
### Para desarrollo
- **JDK 21** instalado en el sistema
- **Maven** para la gestión de dependencias y compilación
- **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** en ejecución para el consumo de datos
---

## Descarga e Instalación

| Plataforma | Instalador | Requisitos |
|---|---|---|
| 🍎 macOS | `GestorRH-1.0.0.dmg` | macOS 11 o superior |
| 🪟 Windows | `GestorRH-1.0.0.msi` | Windows 10 o superior |

Descarga el instalador desde la [página de releases](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/releases/latest).

### macOS
1. Abre el `.dmg` descargado
2. Arrastra **GestorRH** a la carpeta Aplicaciones
3. Click derecho → Abrir (la primera vez, por seguridad de Gatekeeper)
### Windows
1. Ejecuta el `.msi` descargado
2. Sigue el asistente de instalación
---

## Estructura del Proyecto

```
src/main/java/com/gestorrh/escritorio/
├── GestorRhApp.java                    # Punto de entrada JavaFX
│
├── config/
│   └── ConfigManager.java              # Gestor de configuración por entorno (Singleton)
│
├── core/
│   ├── di/
│   │   ├── ServiceFactory.java         # Fábrica de servicios Retrofit (Singleton)
│   │   ├── RepositoryFactory.java      # Fábrica de repositorios (Singleton)
│   │   └── ViewModelFactory.java       # Fábrica de ViewModels (Prototype)
│   ├── exception/
│   │   └── ApiException.java           # Excepción personalizada con soporte i18n
│   ├── i18n/
│   │   └── LanguageManager.java        # Gestor i18n con patrón Observer (Singleton)
│   ├── navigation/
│   │   ├── NavigationManager.java      # Gestor de navegación FXML (Singleton)
│   │   └── Limpiable.java              # Interfaz de ciclo de vida para Controllers
│   └── security/
│       └── SessionManager.java         # Gestión de sesión JWT en memoria (Singleton)
│
├── data/
│   ├── network/
│   │   ├── dto/                        # DTOs organizados por módulo (records Java)
│   │   ├── interceptor/
│   │   │   ├── AuthInterceptor.java    # Inyección automática de token JWT
│   │   │   └── ErrorInterceptor.java   # Conversión global de errores HTTP a ApiException
│   │   └── service/                    # Interfaces Retrofit por entidad
│   └── repository/
│       ├── BaseRepository.java         # Patrón async centralizado con CompletableFuture
│       └── [EntidadRepository.java]    # Un repositorio por entidad de dominio
│
└── presentation/
    ├── component/
    │   └── CalendarioMensual.java      # Componente de calendario reutilizable
    ├── controller/                     # Controladores FXML organizados por módulo
    │   ├── analisis/
    │   ├── ausencia/
    │   ├── auth/
    │   ├── dashboard/
    │   ├── empleado/
    │   ├── shell/
    │   └── turno/
    └── viewmodel/                      # Un ViewModel por vista, sin dependencias de UI
```
 
---

## Configuración del Entorno de Desarrollo

La aplicación usa el perfil Maven `dev` por defecto, que conecta contra `http://localhost:8080/`. No se requiere ningún archivo de configuración adicional para arrancar en desarrollo.

### Instalación y Ejecución

1. Clona el repositorio:
```bash
git clone https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio.git
```
2. Compila y ejecuta con el perfil de desarrollo:
```bash
mvn javafx:run
```

### Configuración de producción

El perfil `prod` requiere un archivo `src/main/resources/config/application-prod.properties` (no incluido en el repositorio, ver `application-prod.properties.example`):

```properties
gestorrh.api.url=https://tu-dominio-o-ip.com/
log.level=WARN
pdf.output.dir=reportes_pdf
```
 
---

## Funcionalidades Implementadas

- **Autenticación:** Login con JWT, gestión de sesión en memoria con record inmutable, logout y expiración automática con redirección al login.
- **Registro de empresa:** Flujo completo de alta con auto-login encadenado.
- **Dashboard:** KPIs en tiempo real (empleados, turnos del día, ausencias), gráfico de barras de ausencias por estado, gráfico circular de distribución por departamento y widget de top retrasos con alerta visual.
- **Gestión de empleados:** Directorio con paginación, búsqueda y filtro por estado (activos/inactivos/todos), alta con contraseña generada por la API, edición, baja programada, readmisión y restablecimiento de contraseña.
- **Planificación de turnos:** Catálogo de turnos con CRUD completo (incluido soporte de turnos nocturnos), asignación visual en calendario mensual, edición con registro de motivo de cambio y eliminación con confirmación.
- **Gestión de ausencias:** Bandeja de revisión con pestañas por estado (Pendientes, Aprobadas, Rechazadas), aprobación/rechazo con observaciones, descarga de justificantes adjuntos y badge de pendientes en el sidebar.
- **Análisis y Reportes:** Gráfico de fichajes del mes actual, previsualización de informes de control horario (detallado por día y resumido por empleado) y descarga de PDF con apertura automática en el visor del sistema.
- **Configuración de empresa:** Edición de datos generales, geocodificación de sede vía Nominatim (OpenStreetMap), introducción manual de coordenadas con validación, ajuste de radio de geovallado y cambio de contraseña.
- **Shell:** Sidebar colapsable con animación, reloj en tiempo real en el footer, avatar con iniciales de empresa y cambio de idioma global.
- **Internacionalización:** Soporte completo Español/Inglés con cambio dinámico sin reiniciar la aplicación, persistido en preferencias del sistema operativo.
---

## Arquitectura

La aplicación sigue el patrón **MVVM** con una separación estricta en tres capas:

```
View (FXML + Controller)
    ↕ bindings reactivos (JavaFX Properties)
ViewModel
    ↕ CompletableFuture
Repository
    ↕ Retrofit / OkHttp
API REST
```

**Decisiones de diseño relevantes:**

- Los ViewModels se crean como **Prototype** (nueva instancia por apertura de vista) desde `ViewModelFactory`, garantizando estado limpio en cada navegación.
- Los repositorios y servicios de red son **Singleton** gestionados por sus respectivas factorías.
- El `SessionManager` almacena el estado de autenticación en un `record` inmutable con referencia `volatile`, lo que garantiza consistencia sin locks ante accesos concurrentes.
- La comunicación asíncrona se gestiona íntegramente con `CompletableFuture`, con un pool de hilos dedicado en `BaseRepository` separado del `ForkJoinPool` común.
- Los errores HTTP se interceptan globalmente en `ErrorInterceptor`, que convierte las respuestas fallidas en `ApiException` antes de que lleguen a los repositorios. Los errores 401 limpian la sesión automáticamente.
- Los Controllers implementan `Limpiable` para desregistrar listeners de idioma y sesión al navegar, evitando memory leaks.
---

## CI/CD

El proyecto dispone de dos pipelines:

**Integración continua** (`.github/workflows/ci.yml`): se ejecuta en cada push a `main` y en cada Pull Request. Compila y verifica el proyecto en perfiles `dev` y `prod`.

**Release** (`.github/workflows/release.yml`): se ejecuta al publicar una release en GitHub. Genera automáticamente los instaladores nativos para macOS (`.dmg`) y Windows (`.msi`) y los adjunta a la release.
 
---

## Versionado

Este proyecto utiliza **Git tags anotados** para marcar hitos funcionales, siguiendo **Semantic Versioning** (`MAJOR.MINOR.PATCH`):

- **MAJOR**: cambios incompatibles o nuevos roles funcionales completos.
- **MINOR**: nuevas funcionalidades compatibles (épicas cerradas).
- **PATCH**: correcciones compatibles sin ruptura de funcionalidad.
### Hitos publicados

- **`v0.1.0`** → infraestructura base y autenticación.
  Arquitectura MVVM lista, Retrofit + OkHttp configurados con interceptores JWT
  y de errores, `NavigationManager` implementado, `LanguageManager` con soporte
  ES/EN, `SessionManager` en memoria, pipeline CI con GitHub Actions y flujo
  completo de login y registro de empresa funcional.
- **`v0.5.0`** → gestión de empleados y turnos operativa.
  CRUD completo de empleados con paginación, filtros por estado y búsqueda en
  tiempo real, flujo de baja/readmisión con contraseña generada, catálogo de
  turnos con validación de horario nocturno, calendario mensual reutilizable y
  asignación de turnos por día con verificación de sede GPS.
- **`v0.9.0-beta`** → versión beta.
  Buzón de ausencias con aprobación/rechazo y descarga de justificantes en
  streaming, configuración de empresa con geocodificación Nominatim y coordenadas
  manuales, dashboard con KPIs reactivos, gráficos de distribución por
  departamento y top retrasos.
- **`v1.0.0`** → primera versión estable. *(latest)*
  Informes de control horario con previsualización en tabla y descarga de PDF
  en streaming, soporte multilingüe completo ES/EN en todos los módulos, gestión
  automática de sesión expirada con redirección al login, instaladores nativos
  para macOS y Windows generados automáticamente via CI/CD.
### Criterio de uso

Para integración con el backend y despliegue, la referencia será siempre la
**última versión estable aprobada**, no necesariamente el último commit de la
rama `main`.
 
---

## Estándares de Calidad

- **Documentación:** Javadoc obligatorio en todas las clases y métodos públicos.
- **UI/UX:** Diseño basado en Material Design con paleta corporativa Deep Navy / Electric Cyan.
- **Concurrencia:** Uso de `CompletableFuture` para garantizar que el hilo de UI de JavaFX nunca se bloquea.
- **Tests:** Cobertura de la capa de servicios con JUnit 5 y Mockito.
---

## Normativa de Contribución

1. **Prohibido hacer commits directos a `main`**.
2. Todo cambio debe realizarse en una rama `feature/` o `fix/`.
3. Se requiere la apertura de una **Pull Request** y la superación de los **Status Checks** de CI para el merge.
---

## Licencia

Este proyecto se distribuye bajo la **Licencia MIT** — consulta el archivo [LICENSE](LICENSE) para más detalles.


