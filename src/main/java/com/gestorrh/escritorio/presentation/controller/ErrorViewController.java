package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador para la vista de error de navegación.
 * Se muestra cuando el NavigationManager no puede cargar la vista solicitada.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ErrorViewController implements Limpiable {

    @FXML private Label tituloLabel;
    @FXML private Label descripcionLabel;

    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el controlador actualizando los textos con el idioma activo
     * y registrando el listener de internacionalización.
     */
    @FXML
    public void initialize() {
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Elimina el listener de idioma para evitar memory leaks.
     * Debe llamarse cuando la vista se destruye.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Actualiza los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();
        tituloLabel.setText(lang.getString("error.vista.titulo"));
        descripcionLabel.setText(lang.getString("error.vista.descripcion"));
    }
}
