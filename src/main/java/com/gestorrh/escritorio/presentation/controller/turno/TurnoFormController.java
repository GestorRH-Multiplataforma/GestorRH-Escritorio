package com.gestorrh.escritorio.presentation.controller.turno;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaTurnoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.TurnoFormViewModel;
import com.gestorrh.escritorio.presentation.viewmodel.TurnoFormViewModel.ModoFormulario;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.HashSet;
import java.util.Set;

/**
 * Controlador para el modal de alta y edición de turnos.
 * Gestiona el formulario, las validaciones reactivas mediante binding
 * con el ViewModel y el flujo de guardado en modo Alta y Edición.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class TurnoFormController {

    @FXML private Label    modalTituloLabel;
    @FXML private Label    errorLabel;

    @FXML private Label     labelDescripcion;
    @FXML private TextField fieldDescripcion;
    @FXML private Label     errorDescripcion;

    @FXML private Label     labelHoraInicio;
    @FXML private TextField fieldHoraInicio;
    @FXML private Label     errorHoraInicio;

    @FXML private Label     labelHoraFin;
    @FXML private TextField fieldHoraFin;
    @FXML private Label     errorHoraFin;

    @FXML private Label  labelAvisoNocturno;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private TurnoFormViewModel viewModel;
    private final Set<javafx.scene.control.TextField> camposTocados = new HashSet<>();
    private Runnable onGuardadoExitoso;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Registra el listener de idioma. Los bindings se configuran en
     * {@link #inicializar} una vez se conoce el modo.
     */
    @FXML
    public void initialize() {
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Configura el modal según el modo indicado e inicializa el ViewModel.
     * Debe llamarse desde {@link TurnosController} justo después de cargar el FXML.
     *
     * @param modo   Modo de operación: ALTA para nuevo turno, EDICION para modificar uno existente.
     * @param turno  Datos del turno a editar. Debe ser null en modo ALTA.
     */
    public void inicializar(ModoFormulario modo, RespuestaTurnoDTO turno) {
        this.viewModel = ViewModelFactory.getInstance().createTurnoFormViewModel();

        if (modo == ModoFormulario.ALTA) {
            viewModel.inicializarParaAlta();
        } else {
            viewModel.inicializarParaEdicion(turno);
        }

        configurarBindings();
        configurarValidacionInline();
        actualizarTextos();
    }

    /**
     * Registra el callback que se ejecutará tras un guardado exitoso.
     * Normalmente recarga la tabla de turnos en {@link TurnosController}.
     *
     * @param callback Acción a ejecutar tras guardar correctamente.
     */
    public void setOnGuardadoExitoso(Runnable callback) {
        this.onGuardadoExitoso = callback;
    }

    /**
     * Configura los bindings bidireccionales entre los campos de la vista
     * y las Properties del ViewModel.
     */
    private void configurarBindings() {
        fieldDescripcion.textProperty().bindBidirectional(viewModel.descripcionProperty());
        fieldHoraInicio.textProperty().bindBidirectional(viewModel.horaInicioProperty());
        fieldHoraFin.textProperty().bindBidirectional(viewModel.horaFinProperty());

        btnGuardar.disableProperty().bind(
                viewModel.formularioValidoProperty().not()
                        .or(viewModel.cargandoProperty())
        );

        errorLabel.textProperty().bind(viewModel.mensajeErrorProperty());
        errorLabel.visibleProperty().bind(viewModel.errorVisibleProperty());
        errorLabel.managedProperty().bind(viewModel.errorVisibleProperty());
    }

    /**
     * Configura los listeners de validación inline sobre los campos,
     * siguiendo el mismo patrón que EmpleadoFormController.
     */
    private void configurarValidacionInline() {
        viewModel.descripcionProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.horaInicioProperty().addListener((obs, o, n)  -> actualizarErrores());
        viewModel.horaFinProperty().addListener((obs, o, n)     -> actualizarErrores());

        registrarListenerFoco(fieldDescripcion);
        registrarListenerFoco(fieldHoraInicio);
        registrarListenerFoco(fieldHoraFin);
    }

    /**
     * Gestiona el evento del botón Guardar.
     * Delega en guardarAlta o guardarEdicion según el modo activo.
     */
    @FXML
    private void handleGuardar() {
        if (viewModel.getModo() == ModoFormulario.ALTA) {
            ejecutarAlta();
        } else {
            ejecutarEdicion();
        }
    }

    /**
     * Ejecuta el alta del nuevo turno de forma asíncrona.
     */
    private void ejecutarAlta() {
        viewModel.guardarAlta()
                .thenAccept(respuesta -> Platform.runLater(() -> {
                    if (onGuardadoExitoso != null) onGuardadoExitoso.run();
                    cerrarModal();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        viewModel.mostrarError(cause.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Ejecuta la actualización del turno en edición de forma asíncrona.
     */
    private void ejecutarEdicion() {
        viewModel.guardarEdicion()
                .thenAccept(respuesta -> Platform.runLater(() -> {
                    if (onGuardadoExitoso != null) onGuardadoExitoso.run();
                    cerrarModal();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        viewModel.mostrarError(cause.getMessage());
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
        cerrarModal();
    }

    /**
     * Cierra la ventana del modal.
     */
    private void cerrarModal() {
        limpiar();
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    /**
     * Elimina el listener de idioma para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Actualiza los mensajes de error inline debajo de cada campo.
     */
    private void actualizarErrores() {
        LanguageManager lang = LanguageManager.getInstance();

        String desc = viewModel.descripcionProperty().get();
        boolean descVacia = desc.isBlank();
        boolean descLarga = desc.length() > 100;
        if (campoTocado(fieldDescripcion)) {
            if (descVacia) {
                mostrarErrorCampo(errorDescripcion,
                        lang.getString("turnos.form.error.descripcionVacia"));
            } else if (descLarga) {
                mostrarErrorCampo(errorDescripcion,
                        lang.getString("turnos.form.error.descripcionLarga"));
            } else {
                ocultarErrorCampo(errorDescripcion);
            }
        }

        boolean inicioInvalido = viewModel.parsearHora(
                viewModel.horaInicioProperty().get()) == null
                && !viewModel.horaInicioProperty().get().isBlank();
        if (campoTocado(fieldHoraInicio) && inicioInvalido) {
            mostrarErrorCampo(errorHoraInicio,
                    lang.getString("turnos.form.error.horaInvalida"));
        } else {
            ocultarErrorCampo(errorHoraInicio);
        }

        boolean finInvalido = viewModel.parsearHora(
                viewModel.horaFinProperty().get()) == null
                && !viewModel.horaFinProperty().get().isBlank();
        if (campoTocado(fieldHoraFin) && finInvalido) {
            mostrarErrorCampo(errorHoraFin,
                    lang.getString("turnos.form.error.horaInvalida"));
        } else {
            ocultarErrorCampo(errorHoraFin);
        }

        java.time.LocalTime inicio = viewModel.parsearHora(viewModel.horaInicioProperty().get());
        java.time.LocalTime fin    = viewModel.parsearHora(viewModel.horaFinProperty().get());
        if (inicio != null && fin != null && inicio.equals(fin)) {
            mostrarErrorCampo(errorHoraFin,
                    lang.getString("turnos.form.error.horasIguales"));
        }
    }

    /**
     * Muestra el label de error de un campo con el mensaje indicado.
     *
     * @param errorLabel Label de error del campo.
     * @param mensaje    Mensaje a mostrar.
     */
    private void mostrarErrorCampo(Label errorLabel, String mensaje) {
        errorLabel.setText(mensaje);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Oculta el label de error de un campo.
     *
     * @param errorLabel Label de error a ocultar.
     */
    private void ocultarErrorCampo(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void registrarListenerFoco(TextField field) {
        field.focusedProperty().addListener((obs, tenieFoco, tieneFoco) -> {
            if (!tieneFoco) {
                camposTocados.add(field);
            }
        });
    }

    /**
     * Indica si el usuario ha interactuado con un campo de texto.
     *
     * @param field Campo de texto a comprobar.
     * @return true si el campo tiene contenido o está enfocado.
     */
    private boolean campoTocado(TextField field) {
        return camposTocados.contains(field) || !field.getText().isEmpty();
    }

    /**
     * Actualiza todos los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        if (viewModel != null) {
            modalTituloLabel.setText(lang.getString(
                    viewModel.getModo() == ModoFormulario.ALTA
                            ? "turnos.form.titulo.crear"
                            : "turnos.form.titulo.editar"
            ));
        }

        labelDescripcion.setText(lang.getString("turnos.form.campo.descripcion"));
        fieldDescripcion.setPromptText(lang.getString("turnos.form.campo.descripcion.placeholder"));

        labelHoraInicio.setText(lang.getString("turnos.form.campo.horaInicio"));
        fieldHoraInicio.setPromptText(lang.getString("turnos.form.campo.horaInicio.placeholder"));

        labelHoraFin.setText(lang.getString("turnos.form.campo.horaFin"));
        fieldHoraFin.setPromptText(lang.getString("turnos.form.campo.horaFin.placeholder"));

        labelAvisoNocturno.setText(lang.getString("turnos.form.aviso.nocturno"));

        btnGuardar.setText(lang.getString("empleados.modal.btn.guardar"));
        btnCancelar.setText(lang.getString("empleados.modal.btn.cancelar"));
    }
}
