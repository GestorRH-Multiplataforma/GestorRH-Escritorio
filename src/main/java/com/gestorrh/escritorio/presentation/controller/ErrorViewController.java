package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador para la vista de error de navegación.
 * Se muestra cuando el NavigationManager no puede cargar la vista solicitada.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ErrorViewController {

    @FXML private Label tituloLabel;
    @FXML private Label descripcionLabel;

    private final Runnable actualizadorTextos = this::actualizarTextos;

    @FXML
    public void initialize() {
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();
        tituloLabel.setText(lang.getString("error.vista.titulo"));
        descripcionLabel.setText(lang.getString("error.vista.descripcion"));
    }
}
