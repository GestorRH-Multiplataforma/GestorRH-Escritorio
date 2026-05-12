module com.gestorrh.escritorio {

    // --- JDK ---
    requires java.base;
    requires java.desktop;       // java.awt.Desktop (apertura de PDFs en el visor del sistema)
    requires java.logging;       // java.util.logging.Logger
    requires java.net.http;      // HttpClient en GeocodingService
    requires java.prefs;         // java.util.prefs.Preferences en LanguageManager

    // --- JavaFX ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // --- Red ---
    requires retrofit2;
    requires retrofit2.converter.gson;
    requires okhttp3;
    requires okhttp3.logging;

    // --- Gson ---
    requires com.google.gson;

    // --- Ikonli ---
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;

    // --- Apertura de paquetes a JavaFX para reflexión (FXML + bindings) ---

    // Controllers
    opens com.gestorrh.escritorio.presentation.controller.analisis  to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.ausencia   to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.auth       to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.dashboard  to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.empleado   to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.shell      to javafx.fxml;
    opens com.gestorrh.escritorio.presentation.controller.turno      to javafx.fxml;

    // Componentes personalizados usados en FXML (CalendarioMensual)
    opens com.gestorrh.escritorio.presentation.component             to javafx.fxml;

    // DTOs: Gson necesita reflexión para deserializar
    opens com.gestorrh.escritorio.data.network.dto.ausencia          to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.auth              to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.empleado          to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.empresa           to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.estadisticas      to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.fichaje           to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.reporte           to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.shared            to com.google.gson;
    opens com.gestorrh.escritorio.data.network.dto.turno             to com.google.gson;

    // Clase principal
    exports com.gestorrh.escritorio;
}