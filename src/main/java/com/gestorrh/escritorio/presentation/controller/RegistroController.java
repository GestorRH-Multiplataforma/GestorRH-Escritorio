package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.presentation.viewmodel.RegistroViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de registro de nueva empresa.
 * Se carga dinámicamente en el panel derecho del login cuando el usuario
 * pulsa "¿Eres nuevo? Registra tu empresa", sin reemplazar el panel izquierdo.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class RegistroController {

    private static final Logger LOGGER = Logger.getLogger(RegistroController.class.getName());

    @FXML private Button langEsButton;
    @FXML private Button langEnButton;

    @FXML private Label tituloLabel;
    @FXML private Label subtituloLabel;
    @FXML private Label errorLabel;

    @FXML private Label labelNombre;
    @FXML private Label labelEmail;
    @FXML private Label labelPassword;
    @FXML private Label labelConfirmarPassword;
    @FXML private Label labelDireccion;
    @FXML private Label labelTelefono;

    @FXML private TextField fieldNombre;
    @FXML private TextField fieldEmail;
    @FXML private PasswordField fieldPassword;
    @FXML private PasswordField fieldConfirmarPassword;
    @FXML private TextField fieldDireccion;
    @FXML private TextField fieldTelefono;

    @FXML private Label errorNombre;
    @FXML private Label errorEmail;
    @FXML private Label errorPassword;
    @FXML private Label errorConfirmarPassword;
    @FXML private Label errorDireccion;
    @FXML private Label errorTelefono;

    @FXML private Button btnCrearCuenta;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Hyperlink volverLoginLink;

    private RegistroViewModel viewModel;
    private final Runnable textUpdater = this::actualizarTextos;

    /**
     * Inicializa el controlador: crea el ViewModel, configura bindings,
     * validaciones inline y registra el listener de idioma.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createRegistroViewModel();

        configurarBindings();
        configurarValidacionInline();
        configurarListenerAutoLogin();
        actualizarTextos();
        actualizarToggleIdioma();
        LanguageManager.getInstance().addListener(textUpdater);
    }

    /**
     * Libera el listener de idioma para evitar memory leaks.
     * Debe llamarse antes de destruir el controlador.
     */
    public void cleanup() {
        LanguageManager.getInstance().removeListener(textUpdater);
    }

    // -------------------------------------------------------------------------
    // Handlers FXML
    // -------------------------------------------------------------------------

    /** Cambia el idioma a Español. */
    @FXML
    private void handleLangEs() {
        LanguageManager.getInstance().setLocale(Locale.of("es"));
        actualizarToggleIdioma();
    }

    /** Cambia el idioma a Inglés. */
    @FXML
    private void handleLangEn() {
        LanguageManager.getInstance().setLocale(Locale.of("en"));
        actualizarToggleIdioma();
    }

    /**
     * Gestiona el evento del botón "Crear cuenta".
     * Delega en el ViewModel y reacciona al resultado en el hilo de UI.
     */
    @FXML
    private void handleCrearCuenta() {
        viewModel.registrar();
    }

    /**
     * Gestiona el evento del hyperlink "¿Ya tienes cuenta? Iniciar sesión".
     * Vuelve al formulario de login dentro del mismo Stage.
     */
    @FXML
    private void handleVolverLogin() {
        navegarALogin(null);
    }

    // -------------------------------------------------------------------------
    // Configuración interna
    // -------------------------------------------------------------------------

    /**
     * Configura los bindings bidireccionales entre los campos de la vista
     * y las Properties del ViewModel, así como los bindings de estado de UI.
     */
    private void configurarBindings() {
        fieldNombre.textProperty().bindBidirectional(viewModel.nombreProperty());
        fieldEmail.textProperty().bindBidirectional(viewModel.emailProperty());
        fieldPassword.textProperty().bindBidirectional(viewModel.passwordProperty());
        fieldConfirmarPassword.textProperty().bindBidirectional(viewModel.confirmarPasswordProperty());
        fieldDireccion.textProperty().bindBidirectional(viewModel.direccionProperty());
        fieldTelefono.textProperty().bindBidirectional(viewModel.telefonoProperty());

        errorLabel.textProperty().bind(viewModel.mensajeErrorProperty());
        errorLabel.visibleProperty().bind(viewModel.errorVisibleProperty());
        errorLabel.managedProperty().bind(viewModel.errorVisibleProperty());

        loadingIndicator.visibleProperty().bind(viewModel.registrandoProperty());
        btnCrearCuenta.disableProperty().bind(
                viewModel.formularioValidoProperty().not()
                        .or(viewModel.registrandoProperty())
        );

        // Limpiar error general al escribir en cualquier campo
        fieldNombre.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
        fieldEmail.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
        fieldPassword.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
        fieldConfirmarPassword.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
        fieldDireccion.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
        fieldTelefono.textProperty().addListener((obs, o, n) -> viewModel.limpiarError());
    }

    /**
     * Configura los listeners de validación inline sobre cada campo,
     * siguiendo el mismo patrón que EmpleadoFormController.
     */
    private void configurarValidacionInline() {
        viewModel.nombreProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.emailProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.passwordProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.confirmarPasswordProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.direccionProperty().addListener((obs, o, n) -> actualizarErrores());
        viewModel.telefonoProperty().addListener((obs, o, n) -> actualizarErrores());
    }

    /**
     * Configura los listeners sobre las Properties de resultado del ViewModel
     * para reaccionar al éxito del auto-login o a su fallo.
     */
    private void configurarListenerAutoLogin() {
        viewModel.autoLoginExitosoProperty().addListener((obs, oldVal, exitoso) -> {
            if (exitoso) {
                navegarAlShell();
            }
        });

        viewModel.autoLoginFallidoProperty().addListener((obs, oldVal, fallido) -> {
            if (fallido) {
                mostrarAlertaAutoLoginFallido();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Validación inline
    // -------------------------------------------------------------------------

    /**
     * Actualiza los mensajes de error inline debajo de cada campo
     * según el estado actual de las Properties del ViewModel.
     */
    private void actualizarErrores() {
        LanguageManager lang = LanguageManager.getInstance();

        // Nombre
        mostrarErrorCampo(errorNombre,
                campoTocado(fieldNombre),
                viewModel.nombreProperty().get().isBlank(),
                lang.getString("empleados.modal.error.campos.requeridos"));

        // Email
        String emailVal = viewModel.emailProperty().get();
        boolean emailVacio = emailVal.isBlank();
        boolean emailInvalido = !emailVacio && !emailVal.trim().matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        if (campoTocado(fieldEmail)) {
            if (emailVacio) {
                mostrarErrorCampo(errorEmail, true, true,
                        lang.getString("empleados.modal.error.campos.requeridos"));
            } else if (emailInvalido) {
                mostrarErrorCampo(errorEmail, true, true,
                        lang.getString("registro.error.emailInvalido"));
            } else if (viewModel.emailDuplicadoProperty().get()) {
                mostrarErrorCampo(errorEmail, true, true,
                        lang.getString("registro.error.emailDuplicado"));
            } else {
                ocultarErrorCampo(errorEmail);
            }
        }

        // Password
        String pass = viewModel.passwordProperty().get();
        boolean passCorta = !pass.isEmpty() && pass.length() < 8;
        mostrarErrorCampo(errorPassword,
                campoTocado(fieldPassword),
                passCorta,
                lang.getString("registro.error.passwordCorta"));

        // Confirmar password
        String confirmar = viewModel.confirmarPasswordProperty().get();
        boolean noCoincide = !confirmar.isEmpty() && !pass.equals(confirmar);
        mostrarErrorCampo(errorConfirmarPassword,
                campoTocado(fieldConfirmarPassword),
                noCoincide,
                lang.getString("registro.error.passwordNoCoincide"));

        // Dirección
        mostrarErrorCampo(errorDireccion,
                campoTocado(fieldDireccion),
                viewModel.direccionProperty().get().isBlank(),
                lang.getString("empleados.modal.error.campos.requeridos"));

        // Teléfono
        mostrarErrorCampo(errorTelefono,
                campoTocado(fieldTelefono),
                viewModel.telefonoProperty().get().isBlank(),
                lang.getString("empleados.modal.error.campos.requeridos"));
    }

    /**
     * Muestra u oculta el label de error de un campo.
     *
     * @param errorLabel Label de error del campo.
     * @param tocado     Indica si el usuario ha interactuado con el campo.
     * @param invalido   Indica si el valor actual es inválido.
     * @param mensaje    Mensaje de error a mostrar.
     */
    private void mostrarErrorCampo(Label errorLabel, boolean tocado,
                                   boolean invalido, String mensaje) {
        if (tocado && invalido) {
            errorLabel.setText(mensaje);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            ocultarErrorCampo(errorLabel);
        }
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

    /**
     * Indica si el usuario ha interactuado con un campo de texto.
     *
     * @param field Campo de texto a comprobar.
     * @return true si el campo tiene contenido o está enfocado.
     */
    private boolean campoTocado(TextField field) {
        return field.isFocused() || !field.getText().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Navegación
    // -------------------------------------------------------------------------

    /**
     * Navega al shell principal tras un registro y auto-login exitosos.
     * Reutiliza exactamente el mismo flujo que LoginController.
     */
    private void navegarAlShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shell-view.fxml"));
            Scene escenaShell = new Scene(loader.load());
            escenaShell.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );

            Stage stage = (Stage) btnCrearCuenta.getScene().getWindow();
            cleanup();

            stage.setMinWidth(1024.0);
            stage.setMinHeight(640.0);
            stage.setResizable(true);
            stage.setScene(escenaShell);

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            } else {
                stage.setMaximized(true);
            }

        } catch (IOException e) {
            LOGGER.severe("RegistroController: Error al navegar al shell: " + e.getMessage());
        }
    }

    /**
     * Navega de vuelta al formulario de login cargando login-view.fxml completo.
     * Si se proporciona un email, lo pre-rellena en el formulario de login.
     *
     * @param emailPrerellenar Email a pre-rellenar en el login, o null si no aplica.
     */
    private void navegarALogin(String emailPrerellenar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Scene escenaLogin = new Scene(loader.load());

            if (emailPrerellenar != null && !emailPrerellenar.isBlank()) {
                LoginController loginController = loader.getController();
                loginController.preRellenarEmail(emailPrerellenar);
            }

            Stage stage = (Stage) volverLoginLink.getScene().getWindow();
            cleanup();
            stage.setScene(escenaLogin);

        } catch (IOException e) {
            LOGGER.severe("RegistroController: Error al navegar al login: " + e.getMessage());
        }
    }

    /**
     * Muestra un Alert informativo cuando el registro fue exitoso pero
     * el auto-login falló, y navega al login con el email pre-rellenado.
     */
    private void mostrarAlertaAutoLoginFallido() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(LanguageManager.getInstance().getString("dialog.confirm.title"));
            alert.setHeaderText(null);
            alert.setContentText(
                    LanguageManager.getInstance().getString("registro.error.autoLoginFallido")
            );
            alert.showAndWait();
            navegarALogin(viewModel.emailProperty().get());
        });
    }

    // -------------------------------------------------------------------------
    // Internacionalización
    // -------------------------------------------------------------------------

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        tituloLabel.setText(lang.getString("registro.titulo"));
        subtituloLabel.setText(lang.getString("registro.subtitulo"));

        labelNombre.setText(lang.getString("registro.campo.nombre"));
        fieldNombre.setPromptText(lang.getString("registro.campo.nombre.placeholder"));

        labelEmail.setText(lang.getString("registro.campo.email"));
        fieldEmail.setPromptText(lang.getString("registro.campo.email.placeholder"));

        labelPassword.setText(lang.getString("registro.campo.password"));
        fieldPassword.setPromptText(lang.getString("registro.campo.password.placeholder"));

        labelConfirmarPassword.setText(lang.getString("registro.campo.confirmarPassword"));
        fieldConfirmarPassword.setPromptText(
                lang.getString("registro.campo.confirmarPassword.placeholder"));

        labelDireccion.setText(lang.getString("registro.campo.direccion"));
        fieldDireccion.setPromptText(lang.getString("registro.campo.direccion.placeholder"));

        labelTelefono.setText(lang.getString("registro.campo.telefono"));
        fieldTelefono.setPromptText(lang.getString("registro.campo.telefono.placeholder"));

        btnCrearCuenta.setText(lang.getString("registro.btn.crear"));
        volverLoginLink.setText(lang.getString("registro.btn.volverLogin"));

        actualizarToggleIdioma();
    }

    /**
     * Actualiza el estilo visual del toggle de idioma según el idioma activo.
     */
    private void actualizarToggleIdioma() {
        String currentLang = LanguageManager.getInstance().getCurrentLocale().getLanguage();

        langEsButton.getStyleClass().removeAll("login-lang-btn-active");
        langEnButton.getStyleClass().removeAll("login-lang-btn-active");

        if ("es".equals(currentLang)) {
            langEsButton.getStyleClass().add("login-lang-btn-active");
        } else {
            langEnButton.getStyleClass().add("login-lang-btn-active");
        }
    }
}
