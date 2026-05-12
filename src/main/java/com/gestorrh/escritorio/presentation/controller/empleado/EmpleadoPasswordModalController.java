package com.gestorrh.escritorio.presentation.controller.empleado;

import com.gestorrh.escritorio.core.i18n.LanguageManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;


/**
 * Controlador para el modal que muestra la contraseña generada automáticamente
 * por la API tras dar de alta a un nuevo empleado.
 * Este modal es de solo lectura y se cierra manualmente por el administrador
 * una vez ha anotado o copiado la contraseña.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadoPasswordModalController {

    @FXML private Label tituloLabel;
    @FXML private Label subtituloLabel;
    @FXML private Label passwordLabel;
    @FXML private Label avisoLabel;
    @FXML private Button btnCopiar;
    @FXML private Button btnCerrar;

    private String passwordGenerada;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa los textos del modal con el idioma activo y registra
     * el listener de internacionalización.
     */
    @FXML
    public void initialize() {
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Establece la contraseña generada que se mostrará en el modal.
     * Debe llamarse desde {@link EmpleadoFormController} justo después de
     * cargar el FXML y antes de mostrar la ventana.
     *
     * @param password Contraseña generada por la API tras el alta del empleado.
     */
    public void setPasswordGenerada(String password) {
        this.passwordGenerada = password;
        passwordLabel.setText(password);
    }

    /**
     * Copia la contraseña generada al portapapeles del sistema y actualiza
     * el texto del botón para confirmar visualmente la acción al usuario.
     */
    @FXML
    private void handleCopiar() {
        if (passwordGenerada == null || passwordGenerada.isBlank()) return;

        ClipboardContent content = new ClipboardContent();
        content.putString(passwordGenerada);
        Clipboard.getSystemClipboard().setContent(content);

        btnCopiar.setText(LanguageManager.getInstance().getString("empleados.modal.password.copiado"));
        btnCopiar.setDisable(true);
    }

    /**
     * Cierra el modal liberando el listener de idioma para evitar memory leaks.
     */
    @FXML
    private void handleCerrar() {
        limpiar();
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }

    /**
     * Elimina el listener de idioma. Debe llamarse al cerrar el modal.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Actualiza todos los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();
        tituloLabel.setText(lang.getString("empleados.modal.password.titulo"));
        subtituloLabel.setText(lang.getString("empleados.modal.password.subtitulo"));
        avisoLabel.setText(lang.getString("empleados.modal.password.aviso"));
        btnCopiar.setText(lang.getString("empleados.modal.password.copiar"));
        btnCerrar.setText(lang.getString("empleados.modal.password.cerrar"));
    }
}
