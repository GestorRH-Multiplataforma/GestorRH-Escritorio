package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.security.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;

import java.io.IOException;

/**
 * Controlador para la vista principal del Dashboard.
 * Gestiona el menú lateral, el logout y el área de contenido central.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class DashboardController {

    @FXML private Label appTitleLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label companyNameLabel;
    @FXML private Label kpiActiveEmployeesLabel;
    @FXML private Label kpiWorkingNowLabel;
    @FXML private Label kpiAbsencesTodayLabel;
    @FXML private Label recentActivityLabel;

    @FXML private Button menuDashboardBtn;
    @FXML private Button menuEmployeesBtn;
    @FXML private Button menuShiftsBtn;
    @FXML private Button menuAbsencesBtn;
    @FXML private Button menuReportsBtn;
    @FXML private Button menuSettingsBtn;
    @FXML private Button logoutBtn;
    @FXML private ComboBox<String> languageSelector;

    private final Runnable textUpdater = this::updateTexts;

    @FXML
    public void initialize() {
        languageSelector.getItems().addAll("ES", "EN");
        languageSelector.setValue(
                LanguageManager.getInstance().getCurrentLocale().getLanguage().toUpperCase()
        );
        languageSelector.setOnAction(e -> {
            String selected = languageSelector.getValue();
            if (selected != null) {
                LanguageManager.getInstance().setLocale(java.util.Locale.of(selected.toLowerCase()));
            }
        });

        updateTexts();
        LanguageManager.getInstance().addListener(textUpdater);
    }

    /**
     * Libera los recursos del controlador eliminando el listener de idioma.
     */
    public void cleanup() {
        LanguageManager.getInstance().removeListener(textUpdater);
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void updateTexts() {
        LanguageManager lang = LanguageManager.getInstance();

        appTitleLabel.setText("GestorRH");
        welcomeLabel.setText(lang.getString("dashboard.welcome"));
        companyNameLabel.setText(SessionManager.getInstance().getNombreEmpresa());

        menuDashboardBtn.setText(lang.getString("menu.dashboard"));
        menuEmployeesBtn.setText(lang.getString("menu.employees"));
        menuShiftsBtn.setText(lang.getString("menu.shifts"));
        menuAbsencesBtn.setText(lang.getString("menu.absences"));
        menuReportsBtn.setText(lang.getString("menu.reports"));
        menuSettingsBtn.setText(lang.getString("menu.settings"));
        logoutBtn.setText(lang.getString("menu.logout"));

        kpiActiveEmployeesLabel.setText(lang.getString("dashboard.employees.active"));
        kpiWorkingNowLabel.setText(lang.getString("dashboard.working.now"));
        kpiAbsencesTodayLabel.setText(lang.getString("dashboard.absences.today"));
        recentActivityLabel.setText(lang.getString("dashboard.recent.activity"));
    }

    /**
     * Gestiona el clic en el botón de inicio del menú lateral.
     */
    @FXML
    private void handleMenuDashboard() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el clic en el botón de empleados del menú lateral.
     */
    @FXML
    private void handleMenuEmployees() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el clic en el botón de turnos del menú lateral.
     */
    @FXML
    private void handleMenuShifts() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el clic en el botón de ausencias del menú lateral.
     */
    @FXML
    private void handleMenuAbsences() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el clic en el botón de reportes del menú lateral.
     */
    @FXML
    private void handleMenuReports() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el clic en el botón de configuración del menú lateral.
     */
    @FXML
    private void handleMenuSettings() {
        // Placeholder — navegación interna pendiente de implementar
    }

    /**
     * Gestiona el logout: limpia la sesión y redirige al login.
     */
    @FXML
    private void handleLogout() {
        cleanup();
        SessionManager.getInstance().clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setMaximized(false);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(LanguageManager.getInstance().getString("dialog.error.title"));
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
