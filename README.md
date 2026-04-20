# GestorRH - Cliente de Escritorio (Administración)

## Visión General
Este módulo constituye el panel de control administrativo del ecosistema **GestorRH**. Desarrollado en **JavaFX 21**, permite a las PYMES centralizar la gestión de sus Recursos Humanos mediante una interfaz moderna, robusta y eficiente.

El cliente de escritorio está diseñado para consumir de forma exclusiva la **API REST de GestorRH**, eliminando el acceso directo a base de datos (JDBC) y garantizando una arquitectura distribuida, segura y escalable.

---

## Enlaces del Proyecto
Este repositorio es una pieza clave de la solución multiplataforma integrada:
* **Organización Principal:** [GestorRH-Multiplataforma](https://github.com/GestorRH-Multiplataforma)
* **Servidor (API REST):** [GestorRH-API](https://github.com/GestorRH-Multiplataforma/GestorRH-API)

---

## Stack Tecnológico
* **Lenguaje:** Java 21 (LTS).
* **Framework UI:** JavaFX con FXML y estilos CSS personalizados.
* **Gestor de Dependencias:** Maven.
* **Comunicación API:** HttpClient / Retrofit para consumo de servicios RESTful.
* **Reportes:** OpenPDF para la generación de listados de empleados y cuadrantes horarios.
* **Arquitectura:** Patrón **MVVM** (Model-View-ViewModel) para un desacoplamiento estricto entre lógica y vista.

---

## Funcionalidades Clave (Roadmap)
1.  **Gestión de Empleados:** Operaciones CRUD completas, incluyendo bajas lógicas y gestión de roles.
2.  **Planificación de Turnos:** Asignación visual de cuadrantes horarios evitando solapamientos lógicos.
3.  **Control de Ausencias:** Panel de revisión para validar, aprobar o rechazar solicitudes (vacaciones, bajas, etc.).
4.  **Reporting:** Exportación de informes técnicos en formato PDF para cumplimiento legal.
5.  **Internacionalización (i18n):** Soporte nativo para Español e Inglés mediante `ResourceBundle`, cumpliendo con los criterios de evaluación del módulo de DI.

---

## Configuración del Entorno
Para ejecutar este proyecto en un entorno de desarrollo:

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/GestorRH-Multiplataforma/gestorrh-desktop.git](https://github.com/GestorRH-Multiplataforma/gestorrh-desktop.git)
    ```
2.  **Requisitos previos:** Asegurarse de tener instalado el **JDK 21** y **Maven**.
3.  **Variable de Entorno:** Definir la URL de la API mediante la variable `GESTORRH_API_URL` (por defecto intentará conectar a `http://localhost:8080/api`).
4.  **Ejecutar la aplicación:**
    ```bash
    mvn javafx:run
    ```

---

## Estándares de Calidad
* **Documentación:** Uso estricto de Javadoc en todas las clases y métodos públicos.
* **UI/UX:** Diseño basado en Material Design con soporte para Modo Oscuro y tipografía corporativa.
* **Concurrencia:** Uso de `Task` y `Service` de JavaFX para garantizar una interfaz fluida (non-blocking UI).

---
*Desarrollado como parte del Proyecto Final de Grado Superior en DAM - IES Francisco de los Ríos (2026).*
