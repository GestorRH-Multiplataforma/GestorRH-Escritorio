# GestorRH - Cliente de Escritorio (Administración)
 
[![CI Escritorio](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml/badge.svg)](https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-en%20desarrollo-orange)](https://github.com/GestorRH-Multiplataforma/gestorrh-desktop)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue)](https://openjfx.io/)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
 
Panel de control administrativo del ecosistema **GestorRH**. Desarrollado en **JavaFX 21**, permite a las empresas centralizar la gestión de sus recursos humanos mediante una interfaz moderna y eficiente, consumiendo de forma exclusiva la **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** REST sin acceso directo a base de datos.
 
> Este repositorio forma parte del ecosistema **GestorRH Multiplataforma**. Para entender el contexto general del proyecto (API, cliente móvil y arquitectura global), consulta el [README de la organización](https://github.com/GestorRH-Multiplataforma#gestorrh---ecosistema-multiplataforma).
 
> **Estado actual:** Repositorio en fase de inicio. La infraestructura base está configurada (dependencias, perfiles Maven y pipeline CI), pero las funcionalidades de negocio están pendientes de implementación según el roadmap definido.
 
---
 
## Tecnologías Utilizadas
 
- **Lenguaje:** Java 21 (LTS)
- **Framework UI:** JavaFX 21.0.1 con FXML y estilos CSS personalizados
- **Arquitectura:** MVVM (Model-View-ViewModel)
- **Gestor de Dependencias:** Maven con perfiles `dev` / `prod`
- **Red:** Retrofit 2 & OkHttp 4
- **Reportes:** OpenPDF (generación de PDFs)
- **Tests:** JUnit 5 & Mockito
- **CI/CD:** GitHub Actions
- **Internacionalización:** `ResourceBundle` para Español (ES) e Inglés (EN)
---
 
## Requisitos Previos
 
- **JDK 21** instalado en el sistema
- **Maven** para la gestión de dependencias y compilación
- **[GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)** en ejecución para el consumo de datos
---
 
## Configuración del Entorno
 
La aplicación usa el perfil Maven `dev` por defecto, que conecta contra `http://localhost:8080/api`. No se requiere ningún archivo de configuración adicional para arrancar en desarrollo.
 
### Instalación y Ejecución
 
1. Clona el repositorio:
```bash
git clone https://github.com/GestorRH-Multiplataforma/GestorRH-Escritorio.git
```
2. Compila y ejecuta con el perfil de desarrollo:
```bash
mvn javafx:run
```
3. Para compilar con el perfil de producción:
```bash
mvn clean verify -P prod
```
 
---
 
## CI/CD
 
El proyecto dispone de un pipeline de integración continua definido en `.github/workflows/ci.yml` que se ejecuta automáticamente en cada push a `main` y en cada Pull Request a `main`.
 
El pipeline realiza las siguientes etapas en orden:
 
1. **Configuración del entorno:** Prepara JDK 21 con caché de Maven para acelerar builds sucesivos.
2. **Compilación:** Compila y verifica el proyecto con `mvn clean verify` bajo el perfil `dev`.
3. **Tests:** Ejecuta la suite de tests unitarios con `mvn test`.
---
 
## Roadmap
 
El desarrollo se organiza en épicas funcionales. Todas las funcionalidades listadas están definidas como issues en el repositorio y pendientes de implementación.
 
### Épica E0 — Infraestructura Base *(en curso)*
Cimientos técnicos del proyecto sobre los que se construirá el resto de épicas.
 
- Estructura MVVM e inyección manual de dependencias
- Capa de red (Retrofit + interceptores de autenticación JWT)
- Internacionalización base (i18n)
- Pantalla de Login
- Layout principal (Shell UI con navegación)
### Épica E1 — Dashboard y Estadísticas
Panel de control con métricas operativas en tiempo real para la toma de decisiones.
 
- Tarjetas de resumen (KPI Cards)
- Gráfico de fichajes del mes actual
- Gráfico de distribución por departamento
- Widget de ausencias por tipo y estado
- Widget de top retrasos
### Épica E2 — Gestión de Empleados
CRUD completo del directorio de empleados de la empresa.
 
- Directorio de empleados (vista tabla)
- Formulario de alta/edición de empleado
- Baja lógica de empleado
### Épica E3 — Planificación de Turnos
Herramientas visuales para la asignación y gestión de cuadrantes horarios.
 
- CRUD de turnos
- Componente calendario visual
- Asignador de turnos en calendario
- Buzón de ausencias (bandeja de revisión y aprobación)
### Épica E4 — Reportes PDF
Exportación de informes oficiales para auditoría y cumplimiento legal.
 
- Exportación de cuadrante mensual (PDF)
- Informe de control horario por empleado (PDF)
---
 
## Estándares de Calidad
 
- **Documentación:** Javadoc obligatorio en todas las clases y métodos públicos.
- **UI/UX:** Diseño basado en Material Design con soporte para modo oscuro y tipografía corporativa.
- **Concurrencia:** Uso de `Task` y `Service` de JavaFX para garantizar una interfaz no bloqueante.
- **Tests:** Cobertura de la capa de servicios con JUnit 5 y Mockito.
---
 
## Normativa de Contribución
 
1. **Prohibido hacer commits directos a `main`**.
2. Todo cambio debe realizarse en una rama `feature/` o `fix/`.
3. Se requiere la apertura de una **Pull Request** y la superación de los **Status Checks** de CI para el merge.
---
 
## Licencia
 
Este proyecto se distribuye bajo la **Licencia MIT** — consulta el archivo [LICENSE](LICENSE) para más detalles.

