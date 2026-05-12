# GestorRH - Cliente de Escritorio (Administración)

[![CI Escritorio](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml/badge.svg)](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-en%20desarrollo-orange)](https://github.com/GestorRH-Multiplataforma/gestorrh-desktop)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)](https://openjfx.io/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Panel de control administrativo del ecosistema **GestorRH**. Desarrollado en **JavaFX 21**, permite a las empresas centralizar la gestión de sus recursos humanos mediante una interfaz moderna y eficiente, consumiendo de forma exclusiva la **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** REST sin acceso directo a base de datos.

> Este repositorio forma parte del ecosistema **GestorRH Multiplataforma**. Para entender el contexto general del proyecto (API, cliente móvil y arquitectura global), consulta el [README de la organización](https://github.com/GestorRH-Multiplataforma#gestorrh---ecosistema-multiplataforma).
 
---

## Tecnologías Utilizadas

- **Lenguaje:** Java 21 (LTS)
- **Framework UI:** JavaFX 21.0.1 con FXML y estilos CSS personalizados
- **Arquitectura:** MVVM (Model-View-ViewModel) con inyección manual de dependencias
- **Gestor de dependencias:** Maven con perfiles `dev` / `prod`
- **Red:** Retrofit 2 & OkHttp 4 con interceptores JWT y gestión global de errores
- **Iconografía:** Ikonli + Material Design 2 Icon Pack
- **Reportes:** OpenPDF (generación y descarga de PDFs)
- **Internacionalización:** `ResourceBundle` para Español (ES) e Inglés (EN) con cambio dinámico en tiempo de ejecución
- **Tests:** JUnit 5 & Mockito
- **CI/CD:** GitHub Actions
---

## Requisitos Previos

- **JDK 21** instalado en el sistema
- **Maven** para la gestión de dependencias y compilación
- **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** en ejecución para el consumo de datos
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

## Configuración del Entorno

La aplicación utiliza perfiles Maven (`dev` / `prod`) para separar la configuración por entorno. El perfil `dev` está activo por defecto y apunta a `http://localhost:8080/api`.

Los archivos de propiedades se cargan desde `src/main/resources/config/`:

| Archivo | Entorno | URL base por defecto |
|---|---|---|
| `application-dev.properties` | Desarrollo | `http://localhost:8080/api` |
| `application-prod.properties` | Producción | Variable de entorno `GESTORRH_API_URL` |

En producción, la URL del backend se inyecta mediante la variable de entorno `GESTORRH_API_URL`. El entorno activo se resuelve leyendo primero `GESTORRH_ENV` y, si no está definida, la propiedad de sistema `env`.

### Instalación y Ejecución

1. Clona el repositorio:
```bash
git clone https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio.git
```

2. Compila y ejecuta con el perfil de desarrollo:
```bash
mvn javafx:run
```

3. Para compilar con el perfil de producción (requiere `GESTORRH_API_URL` definida):
```bash
mvn clean verify -P prod
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

El proyecto dispone de un pipeline de integración continua definido en `.github/workflows/ci.yml` que se ejecuta automáticamente en cada push a `main` y en cada Pull Request a `main`.

El pipeline realiza las siguientes etapas en orden:

1. **Configuración del entorno:** Prepara JDK 21 con caché de Maven para acelerar builds sucesivos.
2. **Compilación y verificación:** Compila y verifica el proyecto con `mvn clean verify` bajo el perfil `dev`.
3. **Tests:** Ejecuta la suite de tests unitarios con JUnit 5 y Mockito.
---

## Roadmap

El desarrollo se organiza en épicas funcionales. Las marcadas como *(completada)* están disponibles en la rama `main`.

### Épica E0 — Infraestructura Base *(completada)*
Cimientos técnicos del proyecto sobre los que se construye el resto de épicas.

- Arquitectura MVVM e inyección manual de dependencias
- Capa de red (Retrofit + interceptores de autenticación JWT y errores HTTP)
- Internacionalización dinámica (i18n)
- Pantalla de Login con validación inline y toggle de idioma
- Pantalla de Registro de empresa con auto-login encadenado
- Shell UI con sidebar colapsable, header, footer con reloj y navegación reactiva
### Épica E1 — Dashboard y Estadísticas *(completada)*

- Tarjetas KPI (total empleados, planificados hoy, ausentes hoy)
- Gráfico de ausencias por estado (BarChart)
- Gráfico de distribución por departamento (PieChart)
- Widget de top retrasos con alerta visual
- Overlay de carga global y botón de actualización manual
### Épica E2 — Gestión de Empleados *(completada)*

- Directorio con paginación, búsqueda en tiempo real y filtro por estado
- Alta con contraseña generada automáticamente por la API
- Edición de datos y restablecimiento de contraseña
- Baja programada con fecha de contrato y readmisión con nueva contraseña
### Épica E3 — Planificación de Turnos *(completada)*

- Catálogo de turnos con CRUD completo y soporte de turnos nocturnos
- Componente `CalendarioMensual` reutilizable con marcas visuales por modalidad
- Asignador de turnos en calendario con validación de sede configurada
- Edición con registro de motivo de cambio obligatorio
- Buzón de ausencias con pestañas por estado, revisión con observaciones y descarga de justificantes
### Épica E4 — Reportes PDF *(completada)*

- Gráfico de fichajes del mes actual
- Previsualización de informes en tabla (detallado y resumido)
- Descarga de PDF con FileChooser y apertura automática en el visor del sistema
---

## Estándares de Calidad

- **Documentación:** Javadoc obligatorio en todas las clases y métodos públicos.
- **UI/UX:** Diseño basado en Material Design con paleta corporativa Deep Navy / Electric Cyan, soporte para tipografía corporativa y modo no bloqueante en todas las operaciones de red.
- **Concurrencia:** Uso de `Task`/`CompletableFuture` para garantizar que el hilo de UI de JavaFX nunca se bloquea. Vuelta al hilo de UI siempre mediante `Platform.runLater`.
- **Gestión de memoria:** Todos los controllers implementan `Limpiable` y desregistran sus listeners al ser sustituidos por navegación, evitando memory leaks en sesiones largas.
- **Tests:** Cobertura de la capa de servicios con JUnit 5 y Mockito.
---

## Normativa de Contribución

1. **Prohibido hacer commits directos a `main`**.
2. Todo cambio debe realizarse en una rama `feature/` o `fix/`.
3. Se requiere la apertura de una **Pull Request** y la superación de los **Status Checks** de CI para el merge.
---

## Licencia

Este proyecto se distribuye bajo la **Licencia MIT** — consulta el archivo [LICENSE](LICENSE) para más detalles.


