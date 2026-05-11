package com.gestorrh.escritorio;

import com.gestorrh.escritorio.core.navigation.NavigationManager;
import com.gestorrh.escritorio.core.security.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicación GestorRH - Cliente de Escritorio.
 * Punto de entrada para el framework JavaFX.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class GestorRhApp extends Application {

    /**
     * Método principal de inicialización de la interfaz gráfica.
     *
     * @param stage Escenario principal proporcionado por JavaFX.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GestorRhApp.class.getResource("/fxml/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().addAll(
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-16.png"))),
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-32.png"))),
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-48.png"))),
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-64.png"))),
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-128.png"))),
                new Image(Objects.requireNonNull(GestorRhApp.class.getResourceAsStream("/images/icon-256.png")))
        );

        configurarIconoDock();

        stage.setTitle("GestorRH - Panel de Administración");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Configura el icono del Dock en macOS usando la API de Taskbar de Java.
     * En otras plataformas la llamada se ignora silenciosamente.
     */
    private void configurarIconoDock() {
        try {
            var taskbar = java.awt.Taskbar.getTaskbar();
            if (taskbar.isSupported(java.awt.Taskbar.Feature.ICON_IMAGE)) {
                var url = Objects.requireNonNull(GestorRhApp.class.getResource("/images/icon-512.png"));
                var awtImage = javax.imageio.ImageIO.read(url);
                taskbar.setIconImage(awtImage);
            }
        } catch (UnsupportedOperationException | SecurityException | java.io.IOException e) {
        }
    }

    /**
     * Método main estándar para lanzar la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Método llamado automáticamente por JavaFX al cerrar la aplicación.
     * Limpia la sesión en memoria, el controlador activo y libera recursos.
     */
    @Override
    public void stop() {
        NavigationManager.getInstance().limpiarControladorActual();
        SessionManager.getInstance().clearSessionSilencioso();
    }
}
