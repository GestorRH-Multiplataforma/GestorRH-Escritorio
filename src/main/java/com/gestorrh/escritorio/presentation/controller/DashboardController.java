package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.core.navigation.NavigationManager;
import com.gestorrh.escritorio.data.network.dto.DatoGraficoDTO;
import com.gestorrh.escritorio.data.network.dto.KpisDTO;
import com.gestorrh.escritorio.presentation.viewmodel.DashboardViewModel;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Controlador para la vista del panel central del Dashboard.
 * Gestiona las tarjetas KPI, el widget de top retrasos, el indicador de carga,
 * el panel de error y el botón de actualización manual.
 *
 * @author Fco Javier García Cañero
 * @version 2.2
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

    @FXML private Label lblTopRetrasosTitulo;
    @FXML private TableView<DatoGraficoDTO> tablaTopRetrasos;
    @FXML private TableColumn<DatoGraficoDTO, Integer> colPosicion;
    @FXML private TableColumn<DatoGraficoDTO, String> colEmpleado;
    @FXML private TableColumn<DatoGraficoDTO, Number> colTotal;
    @FXML private Label lblTopRetrasosVacio;
    @FXML private ProgressIndicator indicadorTopRetrasos;

    @FXML private ProgressIndicator indicadorAusenciasTipo;
    @FXML private ProgressIndicator indicadorAusenciasEstado;
    @FXML private Label lblAusenciasTipoTitulo;
    @FXML private Label lblAusenciasEstadoTitulo;
    @FXML private Label lblAusenciasTipoVacio;
    @FXML private Label lblAusenciasEstadoVacio;
    @FXML private BarChart<Number, String> graficaAusenciasTipo;
    @FXML private BarChart<Number, String> graficaAusenciasEstado;

    private java.util.function.Consumer<String> onNavegar;
    private final DashboardViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Constructor del controlador. Obtiene el ViewModel desde la fábrica.
     */
    public DashboardController() {
        this.viewModel = ViewModelFactory.getInstance().createDashboardViewModel();
    }

    /**
     * Registra el callback que se ejecutará cuando el usuario pulse una tarjeta KPI.
     *
     * @param callback Consumer que recibe la ruta FXML destino.
     */
    public void setOnNavegar(java.util.function.Consumer<String> callback) {
        this.onNavegar = callback;
    }

    /**
     * Inicializa los bindings, configura los listeners reactivos, instala los tooltips,
     * registra el listener de idioma y lanza la carga inicial de datos en paralelo.
     */
    @FXML
    public void initialize() {
        configurarBindings();
        configurarListenerKpis();
        configurarNavegacion();
        configurarTablaTopRetrasos();
        configurarListenerTopRetrasos();
        configurarWidgetAusenciasTipo();
        configurarWidgetAusenciasEstado();
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);
        cargarDashboard();
    }

    /**
     * Libera el listener de idioma al destruirse la vista.
     */
    @Override
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Gestiona el evento del botón Actualizar. Recarga todos los datos del Dashboard.
     */
    @FXML
    private void handleActualizar() {
        cargarDashboard();
    }

    /**
     * Lanza en paralelo la carga de KPIs y el ranking de retrasos.
     */
    private void cargarDashboard() {
        viewModel.cargarKpis();
        viewModel.cargarTopRetrasos();
        viewModel.cargarAusenciasAprobadasPorTipo();
        viewModel.cargarAusenciasPorEstado();
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

        indicadorTopRetrasos.visibleProperty().bind(viewModel.topRetrasosCargandoProperty());
        indicadorTopRetrasos.managedProperty().bind(viewModel.topRetrasosCargandoProperty());

        tablaTopRetrasos.visibleProperty().bind(viewModel.topRetrasosVacioProperty().not());
        tablaTopRetrasos.managedProperty().bind(viewModel.topRetrasosVacioProperty().not());

        lblTopRetrasosVacio.visibleProperty().bind(viewModel.topRetrasosVacioProperty());
        lblTopRetrasosVacio.managedProperty().bind(viewModel.topRetrasosVacioProperty());
    }

    /**
     * Registra el listener que actualiza los valores de las tarjetas KPI
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
     * Registra el listener que actualiza la tabla de top retrasos
     * cada vez que cambia la lista en el ViewModel.
     */
    private void configurarListenerTopRetrasos() {
        viewModel.getTopRetrasos().addListener(
                (javafx.collections.ListChangeListener<DatoGraficoDTO>) cambio ->
                        tablaTopRetrasos.refresh()
        );
    }

    /**
     * Configura las columnas de la tabla de top retrasos con sus CellValueFactory
     * y CellFactory personalizados.
     */
    private void configurarTablaTopRetrasos() {
        tablaTopRetrasos.setItems(viewModel.getTopRetrasos());
        tablaTopRetrasos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colPosicion.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer valor, boolean empty) {
                super.updateItem(valor, empty);
                setAlignment(javafx.geometry.Pos.CENTER);
                DatoGraficoDTO dato = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || dato == null) {
                    setText("—");
                    setGraphic(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                    setGraphic(null);
                }
            }
        });

        colEmpleado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String valor, boolean empty) {
                super.updateItem(valor, empty);
                DatoGraficoDTO dato = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || dato == null) {
                    setText("—");
                    setGraphic(null);
                } else {
                    setText(dato.etiqueta());
                    setGraphic(null);
                }
            }
        });

        colTotal.getStyleClass().add("col-centrada");

        colTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number valor, boolean empty) {
                super.updateItem(valor, empty);
                setAlignment(javafx.geometry.Pos.CENTER);
                DatoGraficoDTO dato = empty ? null : getTableView().getItems().get(getIndex());
                if (empty || dato == null) {
                    setText("—");
                    setGraphic(null);
                    return;
                }
                if (viewModel.superaUmbralAdvertencia(dato.valor())) {
                    FontIcon icono = new FontIcon("mdi2a-alert");
                    icono.setIconSize(14);
                    icono.getStyleClass().add("dashboard-topRetrasos-icono-alerta");
                    Tooltip tooltip = new Tooltip(
                            LanguageManager.getInstance()
                                    .getString("dashboard.topRetrasos.advertencia.tooltip")
                    );
                    Tooltip.install(icono, tooltip);
                    HBox contenedor = new HBox(6, new Label(String.valueOf(dato.valor().intValue())), icono);
                    contenedor.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(contenedor);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText(String.valueOf(dato.valor().intValue()));
                }
            }
        });
        tablaTopRetrasos.setFixedCellSize(46.0);
        tablaTopRetrasos.prefHeightProperty().bind(
                tablaTopRetrasos.fixedCellSizeProperty().multiply(5).add(36)
        );
        tablaTopRetrasos.maxHeightProperty().bind(
                tablaTopRetrasos.fixedCellSizeProperty().multiply(5).add(36)
        );
    }

    /**
     * Configura las tarjetas KPI como elementos clicables que navegan
     * a la sección correspondiente de la aplicación.
     */
    private void configurarNavegacion() {
        configurarTarjetaNavegable(cardTotalEmpleados, "/fxml/empleados-view.fxml");
        configurarTarjetaNavegable(cardPlanificadosHoy, "/fxml/turnos-view.fxml");
        configurarTarjetaNavegable(cardAusentesHoy, "/fxml/ausencias-view.fxml");
    }

    /**
     * Aplica el cursor de mano y el listener de clic a una tarjeta KPI.
     *
     * @param tarjeta  VBox de la tarjeta KPI a configurar.
     * @param rutaFxml Ruta del FXML destino de la navegación.
     */
    private void configurarTarjetaNavegable(VBox tarjeta, String rutaFxml) {
        tarjeta.setCursor(Cursor.HAND);
        tarjeta.setOnMouseClicked(e -> {
            if (onNavegar != null) {
                onNavegar.accept(rutaFxml);
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
     * Instala los tooltips en las tres tarjetas KPI con el texto del idioma activo.
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
     * Configura los bindings y el listener reactivo del widget de ausencias por tipo.
     */
    private void configurarWidgetAusenciasTipo() {
        indicadorAusenciasTipo.visibleProperty().bind(viewModel.ausenciasTipoCargandoProperty());
        indicadorAusenciasTipo.managedProperty().bind(viewModel.ausenciasTipoCargandoProperty());

        graficaAusenciasTipo.visibleProperty().bind(viewModel.ausenciasTipoVacioProperty().not());
        graficaAusenciasTipo.managedProperty().bind(viewModel.ausenciasTipoVacioProperty().not());

        lblAusenciasTipoVacio.visibleProperty().bind(viewModel.ausenciasTipoVacioProperty());
        lblAusenciasTipoVacio.managedProperty().bind(viewModel.ausenciasTipoVacioProperty());

        viewModel.getAusenciasPorTipo().addListener(
                (javafx.collections.ListChangeListener<DatoGraficoDTO>) cambio ->
                        Platform.runLater(this::actualizarGraficaAusenciasTipo)
        );
    }

    /**
     * Configura los bindings y el listener reactivo del widget de ausencias por estado.
     */
    private void configurarWidgetAusenciasEstado() {
        indicadorAusenciasEstado.visibleProperty().bind(viewModel.ausenciasEstadoCargandoProperty());
        indicadorAusenciasEstado.managedProperty().bind(viewModel.ausenciasEstadoCargandoProperty());

        graficaAusenciasEstado.visibleProperty().bind(viewModel.ausenciasEstadoVacioProperty().not());
        graficaAusenciasEstado.managedProperty().bind(viewModel.ausenciasEstadoVacioProperty().not());

        lblAusenciasEstadoVacio.visibleProperty().bind(viewModel.ausenciasEstadoVacioProperty());
        lblAusenciasEstadoVacio.managedProperty().bind(viewModel.ausenciasEstadoVacioProperty());

        viewModel.getAusenciasPorEstado().addListener(
                (javafx.collections.ListChangeListener<DatoGraficoDTO>) cambio ->
                        Platform.runLater(this::actualizarGraficaAusenciasEstado)
        );
    }

    /**
     * Reconstruye las series de la gráfica de ausencias por tipo con los datos actuales del ViewModel.
     * Aplica la clase CSS correspondiente a cada barra según el tipo de ausencia.
     * Se invoca también al cambiar el idioma para relocalizar las etiquetas.
     */
    private void actualizarGraficaAusenciasTipo() {
        graficaAusenciasTipo.getData().clear();
        LanguageManager lang = LanguageManager.getInstance();

        for (DatoGraficoDTO dato : viewModel.getAusenciasPorTipo()) {
            if (dato == null) continue;
            String claveTipo = dato.etiqueta().toLowerCase();
            String etiqueta  = lang.getString("ausencias.tipo." + claveTipo);

            XYChart.Series<Number, String> serie = new XYChart.Series<>();
            XYChart.Data<Number, String> barra = new XYChart.Data<>(dato.valor(), etiqueta);

            barra.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    node.getStyleClass().add("ausencias-barra-" + claveTipo);
                    Tooltip tooltip = new Tooltip(
                            etiqueta + ": " + dato.valor().intValue() + " "
                                    + lang.getString("dashboard.ausencias.tooltip.sufijo")
                    );
                    Tooltip.install(node, tooltip);
                }
            });

            serie.getData().add(barra);
            graficaAusenciasTipo.getData().add(serie);
        }
    }

    /**
     * Reconstruye las series de la gráfica de ausencias por estado con los datos actuales del ViewModel.
     * Aplica la clase CSS correspondiente a cada barra según el estado de la ausencia.
     * Se invoca también al cambiar el idioma para relocalizar las etiquetas.
     */
    private void actualizarGraficaAusenciasEstado() {
        graficaAusenciasEstado.getData().clear();
        LanguageManager lang = LanguageManager.getInstance();

        for (DatoGraficoDTO dato : viewModel.getAusenciasPorEstado()) {
            if (dato == null) continue;
            String claveEstado = dato.etiqueta().toLowerCase();
            String etiqueta    = lang.getString("ausencias.estado." + claveEstado);

            XYChart.Series<Number, String> serie = new XYChart.Series<>();
            XYChart.Data<Number, String> barra = new XYChart.Data<>(dato.valor(), etiqueta);

            barra.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    node.getStyleClass().add("ausencias-barra-" + claveEstado);
                    Tooltip tooltip = new Tooltip(
                            etiqueta + ": " + dato.valor().intValue() + " "
                                    + lang.getString("dashboard.ausencias.tooltip.sufijo")
                    );
                    Tooltip.install(node, tooltip);
                }
            });

            serie.getData().add(barra);
            graficaAusenciasEstado.getData().add(serie);
        }
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        subtituloLabel.setText(lang.getString("dashboard.subtitulo"));
        btnActualizarLabel.setText(lang.getString("dashboard.btn.actualizar"));

        kpiTotalEmpleadosTitulo.setText(lang.getString("dashboard.kpi.totalEmpleados"));
        kpiTotalEmpleadosSubtitulo.setText(lang.getString("dashboard.kpi.totalEmpleados.subtitulo"));

        kpiPlanificadosHoyTitulo.setText(lang.getString("dashboard.kpi.planificadosHoy"));
        kpiPlanificadosHoySubtitulo.setText(lang.getString("dashboard.kpi.planificadosHoy.subtitulo"));

        kpiAusentesHoyTitulo.setText(lang.getString("dashboard.kpi.ausentesHoy"));
        kpiAusentesHoySubtitulo.setText(lang.getString("dashboard.kpi.ausentesHoy.subtitulo"));

        lblTopRetrasosTitulo.setText(lang.getString("dashboard.topRetrasos.titulo"));
        lblTopRetrasosVacio.setText(lang.getString("dashboard.topRetrasos.vacio"));

        colPosicion.setText(lang.getString("dashboard.topRetrasos.col.posicion"));
        colEmpleado.setText(lang.getString("dashboard.topRetrasos.col.empleado"));
        colTotal.setText(lang.getString("dashboard.topRetrasos.col.total"));

        lblAusenciasTipoTitulo.setText(lang.getString("dashboard.ausencias.tipo.titulo"));
        lblAusenciasEstadoTitulo.setText(lang.getString("dashboard.ausencias.estado.titulo"));
        lblAusenciasTipoVacio.setText(lang.getString("dashboard.ausencias.vacio"));
        lblAusenciasEstadoVacio.setText(lang.getString("dashboard.ausencias.vacio"));
        actualizarGraficaAusenciasTipo();
        actualizarGraficaAusenciasEstado();

        instalarTooltips(lang);
        tablaTopRetrasos.refresh();
    }
}
