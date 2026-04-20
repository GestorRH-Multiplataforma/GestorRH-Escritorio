package com.gestorrh.escritorio;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
    public void start(Stage stage) {
        Label label = new Label("¡GestorRH - Entorno configurado correctamente!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("GestorRH - Panel de Administración");
        stage.setScene(scene);
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
