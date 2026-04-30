package com.gestorrh.escritorio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

        stage.setTitle("GestorRH - Panel de Administración");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Método main estándar para lanzar la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
