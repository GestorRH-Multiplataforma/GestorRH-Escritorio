package com.gestorrh.escritorio.presentation.controller;

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

/**
 * Controlador para la vista de Login.
 * Gestiona la interacción del usuario y el flujo de navegación.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class LoginController {

    @FXML private Label titleLabel, emailLabel, passwordLabel;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final LoginViewModel viewModel;

    public LoginController() {
        this.viewModel = ViewModelFactory.getInstance().createLoginViewModel();
    }

    @FXML
    public void initialize() {
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());

        loginButton.disableProperty().bind(viewModel.loadingProperty());
        loadingIndicator.visibleProperty().bind(viewModel.loadingProperty());

        updateTexts();
        LanguageManager.getInstance().addListener(this::updateTexts);
    }

    /**
     * Gestiona el evento de clic en el botón de entrar.
     */
    @FXML
    private void handleLoginAction() {
        viewModel.login().thenRun(() -> {
            Platform.runLater(this::navigateToDashboard);
        }).exceptionally(ex -> {
            Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;

            Platform.runLater(() -> showError(cause));
            return null;
        });
    }

    private void updateTexts() {
        LanguageManager lang = LanguageManager.getInstance();
        titleLabel.setText(lang.getString("login.title"));
        emailLabel.setText(lang.getString("login.email"));
        passwordLabel.setText(lang.getString("login.password"));
        loginButton.setText(lang.getString("login.button"));
        emailField.setPromptText(lang.getString("login.email"));
    }

    private void showError(Throwable cause) {
        LanguageManager lang = LanguageManager.getInstance();
        String finalMessage;

        if (cause instanceof com.gestorrh.escritorio.core.exception.ApiException apiEx && apiEx.hasI18nKey()) {
            finalMessage = lang.getString(apiEx.getI18nKey());
        } else {
            finalMessage = (cause != null && cause.getMessage() != null)
                    ? cause.getMessage()
                    : lang.getString("error.unknown");
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.getString("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(finalMessage);
        alert.showAndWait();
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError(e);
        }
    }
}
