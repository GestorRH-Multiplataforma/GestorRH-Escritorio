package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador genérico para las vistas de secciones pendientes de implementación.
 * Muestra el nombre de la sección y un mensaje "Próximamente".
 * Reutilizado por empleados, turnos, ausencias, informes y configuración.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class PlaceholderController {

    @FXML private Label tituloSeccionLabel;
    @FXML private Label proximamenteLabel;

    private final Runnable actualizadorTextos = this::actualizarTextos;

    @FXML
    public void initialize() {
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Establece el título de la sección que muestra este placeholder.
     * Llamado desde ShellController tras la navegación.
     *
     * @param claveI18n Clave del ResourceBundle para el título.
     */
    public void setTituloSeccion(String claveI18n) {
        tituloSeccionLabel.setText(LanguageManager.getInstance().getString(claveI18n));
    }

    private void actualizarTextos() {
        proximamenteLabel.setText(
                LanguageManager.getInstance().getString("placeholder.coming.soon")
        );
    }
}
