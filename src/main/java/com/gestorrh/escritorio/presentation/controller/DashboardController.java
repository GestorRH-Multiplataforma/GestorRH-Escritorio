package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.presentation.viewmodel.DashboardViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controlador para la vista del panel central del Dashboard.
 * Gestiona únicamente el contenido de la sección principal (KPIs, actividad reciente).
 * El header, sidebar y footer son responsabilidad del ShellController.
 *
 * @author Fco Javier García Cañero
 * @version 2.0
 */
public class DashboardController {

    @FXML private Label bienvenidaLabel;
    @FXML private Label subtituloLabel;
    @FXML private Label kpiEmpleadosActivosTitulo;
    @FXML private Label kpiEmpleadosActivosValor;
    @FXML private Label kpiTrabajandoAhoraTitulo;
    @FXML private Label kpiTrabajandoAhoraValor;
    @FXML private Label kpiAusenciasHoyTitulo;
    @FXML private Label kpiAusenciasHoyValor;
    @FXML private Label actividadRecienteLabel;

    private final DashboardViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Constructor del controlador. Obtiene el ViewModel desde la fábrica.
     */
    public DashboardController() {
        this.viewModel = ViewModelFactory.getInstance().createDashboardViewModel();
    }

    /**
     * Inicializa los bindings, actualiza los textos y registra el listener de idioma.
     */
    @FXML
    public void initialize() {
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
        viewModel.cargarEstadisticas();
    }

    /**
     * Libera el listener de idioma al destruirse la vista.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Actualiza todos los textos del Dashboard con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();
        bienvenidaLabel.setText(lang.getString("dashboard.saludo"));
        subtituloLabel.setText(lang.getString("dashboard.subtitulo"));
        kpiEmpleadosActivosTitulo.setText(lang.getString("dashboard.employees.active"));
        kpiTrabajandoAhoraTitulo.setText(lang.getString("dashboard.working.now"));
        kpiAusenciasHoyTitulo.setText(lang.getString("dashboard.absences.today"));
        actividadRecienteLabel.setText(lang.getString("dashboard.recent.activity"));
    }
}
