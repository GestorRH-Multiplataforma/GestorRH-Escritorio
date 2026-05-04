package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoFormViewModel;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoFormViewModel.ModoFormulario;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Controlador para el modal de alta y edición de empleados.
 * Gestiona el formulario con pestañas, las validaciones reactivas mediante
 * binding con el ViewModel, y el flujo de guardado tanto en modo Alta como
 * en modo Edición. En modo Edición también expone el panel de restablecimiento
 * de contraseña.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadoFormController {

    private static final Logger LOGGER = Logger.getLogger(EmpleadoFormController.class.getName());

    @FXML private Label modalTituloLabel;
    @FXML private Label errorLabel;
    @FXML private TabPane tabPane;
    @FXML private Tab tabPersonal;
    @FXML private Tab tabLaboral;

    // Pestaña Datos Personales
    @FXML private Label labelNombre;
    @FXML private Label labelApellidos;
    @FXML private Label labelEmail;
    @FXML private Label labelTelefono;
    @FXML private TextField fieldNombre;
    @FXML private TextField fieldApellidos;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldTelefono;

    // Pestaña Datos Laborales
    @FXML private Label labelDepartamento;
    @FXML private Label labelPuesto;
    @FXML private Label labelRol;
    @FXML private TextField fieldDepartamento;
    @FXML private TextField fieldPuesto;
    @FXML private ComboBox<String> comboRol;

    // Panel reset contraseña
    @FXML private VBox panelResetPassword;
    @FXML private Label labelResetTitulo;
    @FXML private Label labelNuevaPassword;
    @FXML private Label labelConfirmarPassword;
    @FXML private PasswordField fieldNuevaPassword;
    @FXML private PasswordField fieldConfirmarPassword;
    @FXML private Label errorResetLabel;
    @FXML private Button btnReset;

    // Footer
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private EmpleadoFormViewModel viewModel;
    private Runnable onGuardadoExitoso;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el ComboBox de roles y registra el listener de idioma.
     * Los bindings con el ViewModel se configuran en {@link #inicializar}.
     */
    @FXML
    public void initialize() {
        comboRol.getItems().addAll("EMPLEADO", "SUPERVISOR");
        LanguageManager.getInstance().addListener(actualizadorTextos);
    }

    /**
     * Configura el modal según el modo indicado e inicializa el ViewModel.
     * Debe llamarse desde {@link EmpleadosController} justo después de cargar el FXML.
     *
     * @param modo     Modo de operación: ALTA para nuevo empleado, EDICION para modificar uno existente.
     * @param empleado Datos del empleado a editar. Debe ser null en modo ALTA.
     */
    public void inicializar(ModoFormulario modo, RespuestaEmpleadoDTO empleado) {
        this.viewModel = ViewModelFactory.getInstance().createEmpleadoFormViewModel();

        if (modo == ModoFormulario.ALTA) {
            viewModel.inicializarParaAlta();
        } else {
            viewModel.inicializarParaEdicion(empleado);
        }

        configurarBindings(modo);
        actualizarTextos();
    }

    /**
     * Registra el callback que se ejecutará tras un guardado exitoso.
     * Normalmente recarga la tabla de empleados en {@link EmpleadosController}.
     *
     * @param callback Acción a ejecutar tras guardar correctamente.
     */
    public void setOnGuardadoExitoso(Runnable callback) {
        this.onGuardadoExitoso = callback;
    }

    /**
     * Configura todos los bindings bidireccionales entre los campos de la vista
     * y las Properties del ViewModel, así como los bindings de estado de UI.
     *
     * @param modo Modo activo del formulario para ajustar visibilidad de campos.
     */
    private void configurarBindings(ModoFormulario modo) {
        // Campos de texto
        fieldNombre.textProperty().bindBidirectional(viewModel.nombreProperty());
        fieldApellidos.textProperty().bindBidirectional(viewModel.apellidosProperty());
        fieldEmail.textProperty().bindBidirectional(viewModel.emailProperty());
        fieldTelefono.textProperty().bindBidirectional(viewModel.telefonoProperty());
        fieldDepartamento.textProperty().bindBidirectional(viewModel.departamentoProperty());
        fieldPuesto.textProperty().bindBidirectional(viewModel.puestoProperty());

        // ComboBox de rol
        comboRol.valueProperty().bindBidirectional(viewModel.rolProperty());

        // Campos de reset
        fieldNuevaPassword.textProperty().bindBidirectional(viewModel.nuevaPasswordProperty());
        fieldConfirmarPassword.textProperty().bindBidirectional(viewModel.confirmarPasswordProperty());

        // Estado de botones
        btnGuardar.disableProperty().bind(
                viewModel.formularioValidoProperty().not()
                        .or(viewModel.cargandoProperty())
        );
        btnReset.disableProperty().bind(
                viewModel.resetValidoProperty().not()
                        .or(viewModel.cargandoProperty())
        );

        // Visibilidad del panel de reset (solo en edición)
        panelResetPassword.visibleProperty().bind(viewModel.panelResetVisibleProperty());
        panelResetPassword.managedProperty().bind(viewModel.panelResetVisibleProperty());

        // Error inline principal
        errorLabel.textProperty().bind(viewModel.mensajeErrorProperty());
        errorLabel.visibleProperty().bind(viewModel.errorVisibleProperty());
        errorLabel.managedProperty().bind(viewModel.errorVisibleProperty());

        // En edición: email no editable y panel reset visible
        if (modo == ModoFormulario.EDICION) {
            fieldEmail.setDisable(true);
            viewModel.panelResetVisibleProperty().set(true);
        }
    }

    /**
     * Gestiona el evento del botón Guardar.
     * Delega en {@link #ejecutarAlta} o {@link #ejecutarEdicion} según el modo activo.
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
     * Ejecuta el alta del nuevo empleado de forma asíncrona.
     * Tras éxito cierra el modal, notifica la recarga de la tabla y muestra
     * el modal con la contraseña generada.
     */
    private void ejecutarAlta() {
        viewModel.guardarAlta()
                .thenAccept(respuesta -> Platform.runLater(() -> {
                    if (onGuardadoExitoso != null) onGuardadoExitoso.run();
                    cerrarModal();
                    mostrarModalPasswordGenerada(respuesta.passwordGenerada());
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
     * Ejecuta la actualización del empleado en edición de forma asíncrona.
     * Tras éxito cierra el modal y notifica la recarga de la tabla.
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
     * Gestiona el evento del botón Restablecer contraseña.
     * Ejecuta el reset de forma asíncrona y muestra confirmación o error.
     */
    @FXML
    private void handleReset() {
        viewModel.ejecutarResetPassword()
                .thenAccept(respuesta -> Platform.runLater(() -> {
                    errorResetLabel.setVisible(false);
                    errorResetLabel.setManaged(false);
                    fieldNuevaPassword.clear();
                    fieldConfirmarPassword.clear();
                    mostrarConfirmacionReset();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        errorResetLabel.setText(cause.getMessage());
                        errorResetLabel.setVisible(true);
                        errorResetLabel.setManaged(true);
                    });
                    return null;
                });
    }

    /**
     * Cierra el modal liberando el listener de idioma para evitar memory leaks.
     */
    @FXML
    private void handleCancelar() {
        limpiar();
        cerrarModal();
    }

    /**
     * Abre el modal secundario que muestra la contraseña generada tras el alta.
     *
     * @param password Contraseña generada por la API.
     */
    private void mostrarModalPasswordGenerada(String password) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/empleado-password-modal.fxml")
            );
            Parent root = loader.load();
            EmpleadoPasswordModalController controller = loader.getController();
            controller.setPasswordGenerada(password);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle(LanguageManager.getInstance().getString("empleados.modal.password.titulo"));
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(event -> {
                controller.limpiar();
            });
            stage.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("EmpleadoFormController: Error al abrir el modal de contraseña: " + e.getMessage());
        }
    }

    /**
     * Muestra un Alert de confirmación tras restablecer la contraseña correctamente.
     */
    private void mostrarConfirmacionReset() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(LanguageManager.getInstance().getString("dialog.confirm.title"));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.getInstance().getString("empleados.modal.reset.exito"));
        alert.showAndWait();
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
     * Actualiza todos los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        if (viewModel != null) {
            modalTituloLabel.setText(lang.getString(
                    viewModel.getModo() == ModoFormulario.ALTA
                            ? "empleados.modal.titulo.alta"
                            : "empleados.modal.titulo.edicion"
            ));
        }

        tabPersonal.setText(lang.getString("empleados.modal.tab.personal"));
        tabLaboral.setText(lang.getString("empleados.modal.tab.laboral"));

        labelNombre.setText(lang.getString("empleados.modal.campo.nombre"));
        labelApellidos.setText(lang.getString("empleados.modal.campo.apellidos"));
        labelEmail.setText(lang.getString("empleados.modal.campo.email"));
        labelTelefono.setText(lang.getString("empleados.modal.campo.telefono"));
        labelDepartamento.setText(lang.getString("empleados.modal.campo.departamento"));
        labelPuesto.setText(lang.getString("empleados.modal.campo.puesto"));
        labelRol.setText(lang.getString("empleados.modal.campo.rol"));

        fieldNombre.setPromptText(lang.getString("empleados.modal.campo.nombre.placeholder"));
        fieldApellidos.setPromptText(lang.getString("empleados.modal.campo.apellidos.placeholder"));
        fieldEmail.setPromptText(lang.getString("empleados.modal.campo.email.placeholder"));
        fieldTelefono.setPromptText(lang.getString("empleados.modal.campo.telefono.placeholder"));
        fieldDepartamento.setPromptText(lang.getString("empleados.modal.campo.departamento.placeholder"));
        fieldPuesto.setPromptText(lang.getString("empleados.modal.campo.puesto.placeholder"));

        labelResetTitulo.setText(lang.getString("empleados.modal.reset.titulo"));
        labelNuevaPassword.setText(lang.getString("empleados.modal.reset.nueva"));
        labelConfirmarPassword.setText(lang.getString("empleados.modal.reset.confirmar"));
        fieldNuevaPassword.setPromptText(lang.getString("empleados.modal.reset.nueva.placeholder"));
        fieldConfirmarPassword.setPromptText(lang.getString("empleados.modal.reset.confirmar.placeholder"));
        btnReset.setText(lang.getString("empleados.modal.reset.btn"));

        btnGuardar.setText(lang.getString("empleados.modal.btn.guardar"));
        btnCancelar.setText(lang.getString("empleados.modal.btn.cancelar"));
    }
}
