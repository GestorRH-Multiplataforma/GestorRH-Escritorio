package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.config.ConfigManager;
import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.presentation.viewmodel.LoginViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import java.util.Locale;

/**
 * Controlador para la vista de Login.
 * Gestiona la interacción del usuario y el flujo de navegación.
 *
 * @author Fco Javier García Cañero
 * @version 1.3
 */
public class LoginController {

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label emailLabel;
    @FXML private Label passwordLabel;
    @FXML private Label errorLabel;
    @FXML private Label brandClaimLabel;
    @FXML private Label brandTagLabel;
    @FXML private Label feature1TitleLabel;
    @FXML private Label feature1DescLabel;
    @FXML private Label feature2TitleLabel;
    @FXML private Label feature2DescLabel;
    @FXML private Label feature3TitleLabel;
    @FXML private Label feature3DescLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button langEsButton;
    @FXML private Button langEnButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Hyperlink registerLink;
    @FXML private Hyperlink forgotPasswordLink;

    private final LoginViewModel viewModel;
    private final Runnable textUpdater = this::updateTexts;

    /**
     * Constructor del controlador. Obtiene el ViewModel desde la fábrica.
     */
    public LoginController() {
        this.viewModel = ViewModelFactory.getInstance().createLoginViewModel();
    }

    /**
     * Inicializa los bindings reactivos entre la vista y el ViewModel,
     * configura los botones de idioma y registra el listener de i18n.
     */
    @FXML
    public void initialize() {
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        loginButton.disableProperty().bind(viewModel.loadingProperty());
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());

        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorVisibleProperty());
        errorLabel.managedProperty().bind(viewModel.errorVisibleProperty());

        emailField.textProperty().addListener((obs, oldVal, newVal) -> viewModel.clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> viewModel.clearError());

        if (ConfigManager.getInstance().isDev()) {
            emailField.setText("admin@tech.com");
            passwordField.setText("123456");
        }

        updateLangToggle();

        updateTexts();
        LanguageManager.getInstance().addListener(textUpdater);
    }

    /**
     * Libera los recursos del controlador eliminando el listener de idioma.
     * Debe llamarse cuando la vista se destruye para evitar memory leaks.
     */
    public void cleanup() {
        LanguageManager.getInstance().removeListener(textUpdater);
    }

    /**
     * Gestiona el evento de clic en el botón de entrar.
     * Delega la lógica al ViewModel y reacciona al resultado en el hilo de UI.
     */
    @FXML
    private void handleLoginAction() {
        viewModel.login().whenComplete((res, ex) -> {
            Platform.runLater(() -> {
                viewModel.clearPassword();
                if (ex != null) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    viewModel.handleError(cause);
                } else {
                    navigateToDashboard();
                }
            });
        });
    }

    /**
     * Cambia el idioma de la aplicación a Español.
     */
    @FXML
    private void handleLangEs() {
        LanguageManager.getInstance().setLocale(Locale.of("es"));
        updateLangToggle();
    }

    /**
     * Cambia el idioma de la aplicación a Inglés.
     */
    @FXML
    private void handleLangEn() {
        LanguageManager.getInstance().setLocale(Locale.of("en"));
        updateLangToggle();
    }

    /**
     * Placeholder para la navegación a la pantalla de registro de empresa.
     * TODO: Implementar navegación en la issue correspondiente.
     */
    @FXML
    private void handleRegisterAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro-form.fxml"));
            javafx.scene.Node formularioRegistro = loader.load();

            javafx.scene.layout.HBox raiz = (javafx.scene.layout.HBox) emailField.getScene().getRoot();
            cleanup();
            raiz.getChildren().set(1, formularioRegistro);

        } catch (IOException e) {
            java.util.logging.Logger.getLogger(LoginController.class.getName())
                    .severe("LoginController: Error al cargar el formulario de registro: " + e.getMessage());
        }
    }

    /**
     * Placeholder para la funcionalidad de recuperación de contraseña.
     * TODO: Implementar en la issue correspondiente.
     */
    @FXML
    private void handleForgotPasswordAction() {
        // TODO: Implementar recuperación de contraseña
    }

    /**
     * Actualiza el estilo visual del toggle de idioma según el idioma activo.
     * Limpia ambos botones antes de aplicar el estado activo para evitar
     * acumulación de clases CSS duplicadas.
     */
    private void updateLangToggle() {
        String currentLang = LanguageManager.getInstance().getCurrentLocale().getLanguage();

        langEsButton.getStyleClass().removeAll("login-lang-btn-active");
        langEnButton.getStyleClass().removeAll("login-lang-btn-active");

        if ("es".equals(currentLang)) {
            langEsButton.getStyleClass().add("login-lang-btn-active");
        } else {
            langEnButton.getStyleClass().add("login-lang-btn-active");
        }
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     * Se ejecuta al inicializar y cada vez que cambia el idioma.
     */
    private void updateTexts() {
        LanguageManager lang = LanguageManager.getInstance();
        titleLabel.setText(lang.getString("login.title"));
        subtitleLabel.setText(lang.getString("login.subtitle"));
        emailLabel.setText(lang.getString("login.email"));
        passwordLabel.setText(lang.getString("login.password"));
        loginButton.setText(lang.getString("login.button"));
        emailField.setPromptText(lang.getString("login.email.placeholder"));
        passwordField.setPromptText(lang.getString("login.password.placeholder"));
        registerLink.setText(lang.getString("login.register"));
        forgotPasswordLink.setText(lang.getString("login.forgot.password"));
        brandClaimLabel.setText(lang.getString("login.claim"));
        brandTagLabel.setText(lang.getString("login.brand.tag"));
        feature1TitleLabel.setText(lang.getString("login.feature1.title"));
        feature1DescLabel.setText(lang.getString("login.feature1.desc"));
        feature2TitleLabel.setText(lang.getString("login.feature2.title"));
        feature2DescLabel.setText(lang.getString("login.feature2.desc"));
        feature3TitleLabel.setText(lang.getString("login.feature3.title"));
        feature3DescLabel.setText(lang.getString("login.feature3.desc"));
        updateLangToggle();
    }

    /**
     * Navega a la vista del Shell principal tras un login exitoso.
     * Maximiza la ventana de forma nativa en macOS y Windows.
     * El Shell carga internamente el Dashboard como vista inicial.
     */
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/shell-view.fxml"));
            Scene escenaShell = new Scene(loader.load());
            escenaShell.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );

            Stage stage = (Stage) loginButton.getScene().getWindow();
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
            viewModel.handleError(e);
        }
    }

    /**
     * Pre-rellena el campo email con el valor indicado.
     * Usado por RegistroController cuando el auto-login falla tras el registro,
     * para que el usuario no tenga que escribir el email de nuevo.
     *
     * @param email Email a pre-rellenar en el campo de login.
     */
    public void preRellenarEmail(String email) {
        emailField.setText(email);
    }
}