package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Controlador para el modal de confirmación de baja y readmisión de empleados.
 * Reutilizable para ambos flujos: en modo baja muestra el DatePicker,
 * en modo readmisión lo oculta.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class BajaConfirmacionModalController {

    @FXML private Label    tituloLabel;
    @FXML private Label    mensajeLabel;
    @FXML private VBox     panelFecha;
    @FXML private Label    labelFecha;
    @FXML private DatePicker datePicker;
    @FXML private Label    errorLabel;
    @FXML private Button   btnConfirmar;
    @FXML private Button   btnCancelar;

    /**
     * Enum que representa el modo de operación del modal.
     */
    public enum Modo { BAJA, READMITIR }

    private String nombreEmpleado;
    private Modo modo;
    private Consumer<String> onConfirmado;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el DatePicker con la fecha actual y registra el listener de idioma.
     */
    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Configura el modal según el modo y el empleado afectado.
     * Debe llamarse desde EmpleadosController justo después de cargar el FXML.
     *
     * @param modo           Modo de operación: BAJA o READMITIR.
     * @param nombreCompleto Nombre completo del empleado afectado.
     * @param onConfirmado   Callback que recibe la fecha seleccionada (yyyy-MM-dd)
     *                       o null en modo READMITIR.
     */
    public void inicializar(Modo modo, String nombreCompleto, Consumer<String> onConfirmado) {
        this.modo = modo;
        this.onConfirmado = onConfirmado;
        this.nombreEmpleado = nombreCompleto;

        LanguageManager lang = LanguageManager.getInstance();

        if (modo == Modo.BAJA) {
            tituloLabel.setText(lang.getString("empleados.baja.confirmar.titulo"));
            mensajeLabel.setText(
                    lang.getString("empleados.baja.confirmar.mensaje")
                            .replace("{0}", nombreCompleto)
            );
            labelFecha.setText(lang.getString("empleados.baja.fecha.label"));
            btnConfirmar.setText(lang.getString("empleados.btn.baja"));
            panelFecha.setVisible(true);
            panelFecha.setManaged(true);
        } else {
            tituloLabel.setText(lang.getString("empleados.readmitir.confirmar.titulo"));
            mensajeLabel.setText(
                    lang.getString("empleados.readmitir.confirmar.mensaje")
                            .replace("{0}", nombreCompleto)
            );
            btnConfirmar.setText(lang.getString("empleados.btn.readmitir"));
            panelFecha.setVisible(false);
            panelFecha.setManaged(false);
        }

        btnCancelar.setText(lang.getString("empleados.modal.btn.cancelar"));
    }

    /**
     * Gestiona el evento del botón Confirmar.
     * Valida la fecha en modo BAJA y ejecuta el callback.
     */
    @FXML
    private void handleConfirmar() {
        if (modo == Modo.BAJA && datePicker.getValue() == null) {
            errorLabel.setText(LanguageManager.getInstance()
                    .getString("empleados.baja.fecha.label"));
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        String fecha = modo == Modo.BAJA
                ? datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null;

        cerrar();
        onConfirmado.accept(fecha);
    }

    /**
     * Gestiona el evento del botón Cancelar.
     */
    @FXML
    private void handleCancelar() {
        limpiar();
        cerrar();
    }

    /**
     * Cierra la ventana del modal.
     */
    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    /**
     * Elimina el listener de idioma para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Actualiza los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        if (modo != null) {
            inicializar(modo, nombreEmpleado, onConfirmado);
        }
    }
}
