package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.presentation.viewmodel.AnalisisReportesViewModel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;

/**
 * Controlador para la vista de Análisis y Reportes.
 * Gestiona el gráfico de barras de fichajes del mes actual, los bindings
 * reactivos con el ViewModel y la internacionalización dinámica.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AnalisisReportesController implements Limpiable {

    @FXML private Label          lblTituloGraficoFichajes;
    @FXML private BarChart<String, Number> graficaFichajes;
    @FXML private NumberAxis     ejeYFichajes;
    @FXML private ProgressIndicator indicadorFichajes;
    @FXML private Label          lblFichajesVacio;
    @FXML private Label          lblErrorFichajes;

    private AnalisisReportesViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el controlador: crea el ViewModel, configura los bindings,
     * registra el listener de idioma y lanza la carga de fichajes.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createAnalisisReportesViewModel();

        configurarBindings();
        configurarListenerFichajes();
        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.cargarFichajesMesActual();
    }

    /**
     * Libera el listener de idioma al destruirse la vista para evitar memory leaks.
     */
    @Override
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Configura los bindings reactivos entre los componentes de la vista
     * y las Properties del ViewModel.
     */
    private void configurarBindings() {
        indicadorFichajes.visibleProperty().bind(viewModel.cargandoFichajesProperty());
        indicadorFichajes.managedProperty().bind(viewModel.cargandoFichajesProperty());

        graficaFichajes.visibleProperty().bind(
                viewModel.fichajesVacioProperty().not()
                        .and(viewModel.errorVisibleProperty().not())
        );
        graficaFichajes.managedProperty().bind(
                viewModel.fichajesVacioProperty().not()
                        .and(viewModel.errorVisibleProperty().not())
        );

        lblFichajesVacio.visibleProperty().bind(viewModel.fichajesVacioProperty());
        lblFichajesVacio.managedProperty().bind(viewModel.fichajesVacioProperty());

        lblErrorFichajes.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorFichajes.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorFichajes.managedProperty().bind(viewModel.errorVisibleProperty());

        lblTituloGraficoFichajes.textProperty().bind(viewModel.tituloGraficoProperty());
    }

    /**
     * Registra el listener que reconstruye las series del gráfico cada vez
     * que cambian los datos de fichajes en el ViewModel.
     */
    private void configurarListenerFichajes() {
        viewModel.getFichajesPorDia().addListener(
                (javafx.collections.ListChangeListener<XYChart.Data<String, Number>>) cambio ->
                        Platform.runLater(this::actualizarGraficaFichajes)
        );
    }

    /**
     * Reconstruye las series del gráfico de fichajes desde cero con los
     * datos actuales del ViewModel e instala tooltips sobre cada barra.
     */
    private void actualizarGraficaFichajes() {
        graficaFichajes.getData().clear();

        ejeYFichajes.setAutoRanging(false);
        ejeYFichajes.setTickUnit(1);
        ejeYFichajes.setMinorTickVisible(false);
        ejeYFichajes.setLowerBound(0);

        int maxValor = viewModel.getFichajesPorDia().stream()
                .mapToInt(d -> d.getYValue().intValue())
                .max()
                .orElse(5);
        ejeYFichajes.setUpperBound(maxValor + 1);

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (XYChart.Data<String, Number> dato : viewModel.getFichajesPorDia()) {
            XYChart.Data<String, Number> barra = new XYChart.Data<>(
                    dato.getXValue(), dato.getYValue());

            barra.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    node.getStyleClass().add("chart-bar--primary");
                    instalarTooltipBarra(node, dato.getXValue(), dato.getYValue().intValue());
                }
            });

            serie.getData().add(barra);
        }

        graficaFichajes.getData().add(serie);
    }

    /**
     * Instala un tooltip sobre el nodo de una barra con la fecha completa
     * localizada y el total de fichajes de ese día.
     *
     * @param node   Nodo JavaFX de la barra sobre el que se instala el tooltip.
     * @param dia    Etiqueta del día en formato "dd".
     * @param total  Número de fichajes del día.
     */
    private void instalarTooltipBarra(javafx.scene.Node node, String dia, int total) {
        try {
            int numeroDia = Integer.parseInt(dia);
            java.time.LocalDate fecha = java.time.YearMonth.now().atDay(numeroDia);
            String fechaFormateada = fecha.format(
                    java.time.format.DateTimeFormatter.ofPattern(
                            "d 'de' MMMM 'de' yyyy",
                            LanguageManager.getInstance().getCurrentLocale()));
            String etiquetaY = LanguageManager.getInstance()
                    .getString("analisis.chart.fichajes.ejeY");
            Tooltip tooltip = new Tooltip(fechaFormateada + "\n" + etiquetaY + ": " + total);
            Tooltip.install(node, tooltip);
        } catch (Exception ignored) {
        }
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     * Se ejecuta al inicializar y cada vez que cambia el idioma.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        viewModel.actualizarTituloGrafico();

        lblFichajesVacio.setText(lang.getString("analisis.chart.fichajes.vacio"));

        graficaFichajes.getXAxis().setLabel(lang.getString("analisis.chart.fichajes.ejeX"));
        graficaFichajes.getYAxis().setLabel(lang.getString("analisis.chart.fichajes.ejeY"));

        actualizarGraficaFichajes();
    }
}
