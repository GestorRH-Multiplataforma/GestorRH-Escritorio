package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.data.network.dto.KpisDTO;
import com.gestorrh.escritorio.presentation.viewmodel.DashboardViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controlador para la vista del panel central del Dashboard.
 * Gestiona las tarjetas KPI, el indicador de carga, el panel de error,
 * los tooltips de cada tarjeta y el botón de actualización manual.
 * El header, sidebar y footer son responsabilidad del ShellController.
 *
 * @author Fco Javier García Cañero
 * @version 2.1
 */
public class DashboardController implements Limpiable {

    @FXML private Label subtituloLabel;
    @FXML private Button btnActualizar;
    @FXML private Label btnActualizarLabel;
    @FXML private Label errorLabel;
    @FXML private HBox kpiContainer;
    @FXML private ProgressIndicator indicadorCarga;

    @FXML private VBox cardTotalEmpleados;
    @FXML private Label kpiTotalEmpleadosTitulo;
    @FXML private Label kpiTotalEmpleadosValor;
    @FXML private Label kpiTotalEmpleadosSubtitulo;

    @FXML private VBox cardPlanificadosHoy;
    @FXML private Label kpiPlanificadosHoyTitulo;
    @FXML private Label kpiPlanificadosHoyValor;
    @FXML private Label kpiPlanificadosHoySubtitulo;

    @FXML private VBox cardAusentesHoy;
    @FXML private Label kpiAusentesHoyTitulo;
    @FXML private Label kpiAusentesHoyValor;
    @FXML private Label kpiAusentesHoySubtitulo;

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
     * Inicializa los bindings, configura los listeners reactivos,
     * instala los tooltips, registra el listener de idioma
     * y lanza la carga inicial de KPIs.
     */
    @FXML
    public void initialize() {
        configurarBindings();
        configurarListenerKpis();
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
        viewModel.cargarKpis();
    }

    /**
     * Libera el listener de idioma al destruirse la vista.
     */
    @Override
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Gestiona el evento del botón Actualizar.
     * Recarga los KPIs desde la API manualmente.
     */
    @FXML
    private void handleActualizar() {
        viewModel.cargarKpis();
    }

    /**
     * Configura los bindings reactivos entre los componentes de la vista
     * y las Properties del ViewModel.
     */
    private void configurarBindings() {
        indicadorCarga.visibleProperty().bind(viewModel.cargandoProperty());
        indicadorCarga.managedProperty().bind(viewModel.cargandoProperty());

        kpiContainer.visibleProperty().bind(viewModel.cargandoProperty().not());
        kpiContainer.managedProperty().bind(viewModel.cargandoProperty().not());

        errorLabel.textProperty().bind(viewModel.mensajeErrorProperty());
        errorLabel.visibleProperty().bind(viewModel.errorVisibleProperty());
        errorLabel.managedProperty().bind(viewModel.errorVisibleProperty());

        btnActualizar.disableProperty().bind(viewModel.cargandoProperty());
    }

    /**
     * Registra el listener que actualiza los valores de las tarjetas
     * cada vez que el ViewModel recibe nuevos datos de la API.
     */
    private void configurarListenerKpis() {
        viewModel.kpisProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarValoresKpis(newVal);
            }
        });
    }

    /**
     * Actualiza los Labels de valor de las tres tarjetas KPI
     * con los datos recibidos de la API.
     *
     * @param kpis DTO con los valores actualizados.
     */
    private void actualizarValoresKpis(KpisDTO kpis) {
        kpiTotalEmpleadosValor.setText(String.valueOf(kpis.totalEmpleados()));
        kpiPlanificadosHoyValor.setText(String.valueOf(kpis.planificadosHoy()));
        kpiAusentesHoyValor.setText(String.valueOf(kpis.ausentesHoy()));
    }

    /**
     * Instala los tooltips en las tres tarjetas KPI con el texto
     * del idioma activo. Se llama cada vez que cambia el idioma
     * para mantener los textos sincronizados.
     *
     * @param lang Gestor de idiomas activo.
     */
    private void instalarTooltips(LanguageManager lang) {
        Tooltip.install(cardTotalEmpleados,
                new Tooltip(lang.getString("dashboard.kpi.totalEmpleados.tooltip")));
        Tooltip.install(cardPlanificadosHoy,
                new Tooltip(lang.getString("dashboard.kpi.planificadosHoy.tooltip")));
        Tooltip.install(cardAusentesHoy,
                new Tooltip(lang.getString("dashboard.kpi.ausentesHoy.tooltip")));
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     * Se ejecuta al inicializar y cada vez que cambia el idioma.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        subtituloLabel.setText(lang.getString("dashboard.subtitulo"));
        btnActualizarLabel.setText(lang.getString("dashboard.btn.actualizar"));
        actividadRecienteLabel.setText(lang.getString("dashboard.recent.activity"));

        kpiTotalEmpleadosTitulo.setText(lang.getString("dashboard.kpi.totalEmpleados"));
        kpiTotalEmpleadosSubtitulo.setText(lang.getString("dashboard.kpi.totalEmpleados.subtitulo"));

        kpiPlanificadosHoyTitulo.setText(lang.getString("dashboard.kpi.planificadosHoy"));
        kpiPlanificadosHoySubtitulo.setText(lang.getString("dashboard.kpi.planificadosHoy.subtitulo"));

        kpiAusentesHoyTitulo.setText(lang.getString("dashboard.kpi.ausentesHoy"));
        kpiAusentesHoySubtitulo.setText(lang.getString("dashboard.kpi.ausentesHoy.subtitulo"));

        instalarTooltips(lang);
    }
}
