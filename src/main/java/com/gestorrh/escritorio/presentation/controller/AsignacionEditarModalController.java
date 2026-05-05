package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaTurnoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.AsignacionTurnosViewModel;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Controlador para el modal de edición de una asignación de turno existente.
 * Recibe la asignación a editar y el ViewModel compartido con la vista principal.
 * El motivo de cambio es obligatorio en edición para registro de auditoría.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AsignacionEditarModalController {

    @FXML private Label lblTitulo;
    @FXML private Label lblError;
    @FXML private Label lblEmpleado;
    @FXML private Label lblTurno;
    @FXML private Label lblModalidad;
    @FXML private Label lblMotivoCambio;
    @FXML private Label lblErrorMotivo;
    @FXML private ComboBox<RespuestaEmpleadoDTO> comboEmpleado;
    @FXML private ComboBox<RespuestaTurnoDTO> comboTurno;
    @FXML private ComboBox<String> comboModalidad;
    @FXML private TextArea textMotivoCambio;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private AsignacionTurnosViewModel viewModel;
    private RespuestaAsignacionTurnoDTO asignacionEnEdicion;
    private Runnable onGuardadoExitoso;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Registra el listener de idioma al inicializarse el controlador.
     */
    @FXML
    public void initialize() {
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Configura el modal con los datos de la asignación a editar y el ViewModel.
     * Debe llamarse desde TurnosController justo después de cargar el FXML.
     *
     * @param asignacion Asignación cuyos datos se precargan en el formulario.
     * @param vm         ViewModel compartido con la vista principal.
     */
    public void inicializar(RespuestaAsignacionTurnoDTO asignacion, AsignacionTurnosViewModel vm) {
        this.asignacionEnEdicion = asignacion;
        this.viewModel = vm;

        configurarCombos();
        precargarDatos();
        actualizarTextos();
    }

    /**
     * Registra el callback que se ejecutará tras un guardado exitoso.
     *
     * @param callback Acción a ejecutar tras guardar correctamente.
     */
    public void setOnGuardadoExitoso(Runnable callback) {
        this.onGuardadoExitoso = callback;
    }

    /**
     * Configura los StringConverter de los ComboBox y carga sus elementos
     * desde el ViewModel compartido.
     */
    private void configurarCombos() {
        comboEmpleado.setItems(viewModel.getEmpleados());
        comboEmpleado.setConverter(new StringConverter<>() {
            @Override
            public String toString(RespuestaEmpleadoDTO empleado) {
                if (empleado == null) return "";
                return empleado.nombre() + " " + empleado.apellidos();
            }
            @Override
            public RespuestaEmpleadoDTO fromString(String s) { return null; }
        });

        comboTurno.setItems(viewModel.getTurnos());
        comboTurno.setConverter(new StringConverter<>() {
            @Override
            public String toString(RespuestaTurnoDTO turno) {
                if (turno == null) return "";
                return turno.descripcion() + " (" + formatearHora(turno.horaInicio())
                        + " - " + formatearHora(turno.horaFin()) + ")";
            }
            @Override
            public RespuestaTurnoDTO fromString(String s) { return null; }
        });

        comboModalidad.setItems(viewModel.getModalidades());
    }

    /**
     * Precarga los campos del formulario con los datos de la asignación en edición.
     */
    private void precargarDatos() {
        viewModel.getEmpleados().stream()
                .filter(e -> e.idEmpleado().equals(asignacionEnEdicion.idEmpleado()))
                .findFirst()
                .ifPresent(comboEmpleado::setValue);

        viewModel.getTurnos().stream()
                .filter(t -> t.idTurno().equals(asignacionEnEdicion.idTurno()))
                .findFirst()
                .ifPresent(comboTurno::setValue);

        comboModalidad.setValue(asignacionEnEdicion.modalidad());
        textMotivoCambio.setText("");
    }

    /**
     * Gestiona el evento del botón Guardar.
     * Valida que el motivo de cambio no esté vacío antes de llamar al ViewModel.
     */
    @FXML
    private void handleGuardar() {
        ocultarError();

        String motivo = textMotivoCambio.getText().trim();
        if (motivo.isBlank()) {
            lblErrorMotivo.setText(
                    LanguageManager.getInstance().getString("asignaciones.error.motivoRequerido"));
            lblErrorMotivo.setVisible(true);
            lblErrorMotivo.setManaged(true);
            return;
        }

        if (comboEmpleado.getValue() == null || comboTurno.getValue() == null
                || comboModalidad.getValue() == null) {
            mostrarError(LanguageManager.getInstance()
                    .getString("asignaciones.error.camposRequeridos"));
            return;
        }

        btnGuardar.setDisable(true);

        viewModel.editarAsignacion(
                asignacionEnEdicion.idAsignacion(),
                comboEmpleado.getValue(),
                comboTurno.getValue(),
                comboModalidad.getValue(),
                motivo,
                java.time.LocalDate.parse(asignacionEnEdicion.fecha())
        ).thenAccept(actualizada -> Platform.runLater(() -> {
            if (onGuardadoExitoso != null) onGuardadoExitoso.run();
            cerrar();
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                btnGuardar.setDisable(false);
                Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                mostrarError(causa.getMessage());
            });
            return null;
        });
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
     * Muestra el label de error general con el mensaje indicado.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    /**
     * Oculta el label de error general.
     */
    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblErrorMotivo.setVisible(false);
        lblErrorMotivo.setManaged(false);
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
     * Convierte una hora en formato "HH:mm:ss" a "HH:mm" para mostrar en el combo.
     *
     * @param hora Hora en formato "HH:mm:ss" o "HH:mm".
     * @return Hora en formato "HH:mm".
     */
    private String formatearHora(String hora) {
        if (hora == null || hora.isBlank()) return "";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    /**
     * Actualiza todos los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();
        lblTitulo.setText(lang.getString("asignaciones.modal.editar.titulo"));
        lblEmpleado.setText(lang.getString("asignaciones.form.empleado"));
        lblTurno.setText(lang.getString("asignaciones.form.turno"));
        lblModalidad.setText(lang.getString("asignaciones.form.modalidad"));
        lblMotivoCambio.setText(lang.getString("asignaciones.form.motivoCambio"));
        textMotivoCambio.setPromptText(lang.getString("asignaciones.form.motivoCambio.placeholder"));
        btnGuardar.setText(lang.getString("asignaciones.btn.guardar"));
        btnCancelar.setText(lang.getString("asignaciones.btn.cancelar"));
    }
}
