package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteDetalleDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteResumenDTO;
import com.gestorrh.escritorio.presentation.viewmodel.AnalisisReportesViewModel;
import com.gestorrh.escritorio.presentation.viewmodel.AnalisisReportesViewModel.TipoInforme;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controlador para la vista de Análisis y Reportes.
 * Gestiona el gráfico de barras de fichajes del mes actual y la sección
 * de informes de control horario con soporte para previsualización en tabla
 * y descarga de PDF con apertura en el visor del sistema operativo.
 *
 * @author Fco Javier García Cañero
 * @version 2.0
 */
public class AnalisisReportesController implements Limpiable {

    @FXML private Label lblTituloGraficoFichajes;
    @FXML private BarChart<String, Number> graficaFichajes;
    @FXML private NumberAxis ejeYFichajes;
    @FXML private ProgressIndicator indicadorFichajes;
    @FXML private Label lblFichajesVacio;
    @FXML private Label lblErrorFichajes;

    @FXML private Label lblSeccionTitulo;
    @FXML private Label lblTipoInforme;
    @FXML private Label lblEmpleado;
    @FXML private Label lblFechaInicio;
    @FXML private Label lblFechaFin;
    @FXML private ComboBox<TipoInforme> comboTipoInforme;
    @FXML private ComboBox<RespuestaEmpleadoDTO> comboEmpleado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Button btnPrevisualizar;
    @FXML private Button btnDescargarPdf;
    @FXML private Label lblErrorRango;
    @FXML private Label lblErrorInforme;

    @FXML private HBox barraTabla;
    @FXML private Label lblPrevia;
    @FXML private Label lblBadgeTipo;
    @FXML private Label lblConteoRegistros;
    @FXML private ProgressIndicator indicadorPrevia;
    @FXML private Separator separadorTabla;

    @FXML private TableView<RespuestaReporteDetalleDTO> tablaDetalle;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetEmpleado;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetDepartamento;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetFecha;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetTurno;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetEntrada;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetSalida;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetTiempoReal;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetTiempoTeorico;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetExtras;
    @FXML private TableColumn<RespuestaReporteDetalleDTO, String> colDetIncidencias;
    @FXML private Label lblTablaDetallVacia;

    @FXML private TableView<RespuestaReporteResumenDTO> tablaResumen;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResEmpleado;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResDepartamento;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResDias;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResTiempoTeorico;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResTiempoReal;
    @FXML private TableColumn<RespuestaReporteResumenDTO, String> colResExtras;
    @FXML private Label lblTablaResumenVacia;

    private AnalisisReportesViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el controlador: crea el ViewModel, configura los combos,
     * los bindings, los listeners y lanza la carga inicial de datos.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createAnalisisReportesViewModel();

        configurarComboTipo();
        configurarComboEmpleado();
        configurarDatePickers();
        configurarBindings();
        configurarListenerFichajes();
        configurarColumnasDetalle();
        configurarColumnasResumen();
        configurarListenerDatos();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.cargarFichajesMesActual();
        viewModel.cargarEmpleados();
    }

    /**
     * Libera el listener de idioma al destruirse la vista para evitar memory leaks.
     */
    @Override
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Configura el ComboBox de tipo de informe con sus opciones y StringConverter.
     */
    private void configurarComboTipo() {
        comboTipoInforme.getItems().addAll(TipoInforme.DETALLE, TipoInforme.RESUMEN);
        comboTipoInforme.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoInforme tipo) {
                if (tipo == null) return "";
                LanguageManager lang = LanguageManager.getInstance();
                return tipo == TipoInforme.DETALLE
                        ? lang.getString("analisis.informeHorario.tipo.detalle")
                        : lang.getString("analisis.informeHorario.tipo.resumen");
            }
            @Override
            public TipoInforme fromString(String s) { return null; }
        });
        comboTipoInforme.setValue(TipoInforme.DETALLE);
        comboTipoInforme.valueProperty().bindBidirectional(viewModel.tipoInformeProperty());

        comboTipoInforme.valueProperty().addListener((obs, o, n) -> {
            ocultarTablas();
            viewModel.getDatosDetalle().clear();
            viewModel.getDatosResumen();
        });
    }

    /**
     * Configura el ComboBox de empleados con la opción "Todos" como primera entrada
     * representada por null, y el StringConverter correspondiente.
     */
    private void configurarComboEmpleado() {
        comboEmpleado.setConverter(new StringConverter<>() {
            @Override
            public String toString(RespuestaEmpleadoDTO emp) {
                if (emp == null) {
                    return LanguageManager.getInstance()
                            .getString("analisis.informeHorario.empleado.todos");
                }
                return emp.nombre() + " " + emp.apellidos();
            }
            @Override
            public RespuestaEmpleadoDTO fromString(String s) { return null; }
        });

        viewModel.getEmpleados().addListener(
                (javafx.collections.ListChangeListener<RespuestaEmpleadoDTO>) cambio -> {
                    Platform.runLater(() -> {
                        comboEmpleado.getItems().clear();
                        comboEmpleado.getItems().add(null);
                        comboEmpleado.getItems().addAll(viewModel.getEmpleados());
                        comboEmpleado.setValue(null);
                    });
                });

        comboEmpleado.valueProperty().bindBidirectional(
                viewModel.empleadoSeleccionadoProperty());
    }

    /**
     * Configura los DatePickers con la localización activa y los bindings
     * bidireccionales con las properties del ViewModel. También configura
     * el listener de validación inline del rango.
     */
    private void configurarDatePickers() {
        actualizarLocalizacionDatePickers();

        dpFechaInicio.valueProperty().bindBidirectional(viewModel.fechaInicioProperty());
        dpFechaFin.valueProperty().bindBidirectional(viewModel.fechaFinProperty());

        dpFechaInicio.valueProperty().addListener((obs, o, n) -> validarRangoInline());
        dpFechaFin.valueProperty().addListener((obs, o, n) -> validarRangoInline());
    }

    /**
     * Actualiza la localización de los DatePickers con el idioma activo.
     */
    private void actualizarLocalizacionDatePickers() {
        Locale locale = LanguageManager.getInstance().getCurrentLocale();
        dpFechaInicio.setConverter(crearConversorFecha(locale));
        dpFechaFin.setConverter(crearConversorFecha(locale));
    }

    /**
     * Crea un StringConverter de LocalDate con el formato dd/MM/yyyy para los DatePickers.
     *
     * @param locale Locale activo para el formato de fecha.
     * @return StringConverter configurado.
     */
    private StringConverter<LocalDate> crearConversorFecha(Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale);
        return new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }
            @Override
            public LocalDate fromString(String s) {
                if (s == null || s.isBlank()) return null;
                try { return LocalDate.parse(s, formatter); }
                catch (Exception e) { return null; }
            }
        };
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
                        .and(viewModel.errorVisibleProperty().not()));
        graficaFichajes.managedProperty().bind(
                viewModel.fichajesVacioProperty().not()
                        .and(viewModel.errorVisibleProperty().not()));

        lblFichajesVacio.visibleProperty().bind(viewModel.fichajesVacioProperty());
        lblFichajesVacio.managedProperty().bind(viewModel.fichajesVacioProperty());

        lblErrorFichajes.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorFichajes.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorFichajes.managedProperty().bind(viewModel.errorVisibleProperty());

        lblTituloGraficoFichajes.textProperty().bind(viewModel.tituloGraficoProperty());

        btnPrevisualizar.disableProperty().bind(
                viewModel.rangoValidoProperty().not()
                        .or(viewModel.previsualizandoProperty()));

        btnDescargarPdf.disableProperty().bind(
                viewModel.rangoValidoProperty().not()
                        .or(viewModel.descargandoProperty()));

        indicadorPrevia.visibleProperty().bind(viewModel.previsualizandoProperty());
        indicadorPrevia.managedProperty().bind(viewModel.previsualizandoProperty());

        lblErrorInforme.textProperty().bind(viewModel.mensajeErrorInformeProperty());
        lblErrorInforme.visibleProperty().bind(viewModel.errorInformeVisibleProperty());
        lblErrorInforme.managedProperty().bind(viewModel.errorInformeVisibleProperty());
    }

    /**
     * Registra el listener que reconstruye las series del gráfico cada vez
     * que cambian los datos de fichajes en el ViewModel.
     */
    private void configurarListenerFichajes() {
        viewModel.getFichajesPorDia().addListener(
                (javafx.collections.ListChangeListener<XYChart.Data<String, Number>>) cambio ->
                        Platform.runLater(this::actualizarGraficaFichajes));
    }

    /**
     * Registra los listeners que muestran u ocultan las tablas cuando llegan
     * datos de previsualización al ViewModel.
     */
    private void configurarListenerDatos() {
        viewModel.getDatosDetalle().addListener(
                (javafx.collections.ListChangeListener<RespuestaReporteDetalleDTO>) cambio ->
                        Platform.runLater(this::actualizarVisibilidadTablas));

        viewModel.getDatosResumen().addListener(
                (javafx.collections.ListChangeListener<RespuestaReporteResumenDTO>) cambio ->
                        Platform.runLater(this::actualizarVisibilidadTablas));
    }

    /**
     * Configura los CellValueFactory de las columnas de la tabla de detalle.
     * Los campos de minutos se formatean como Xh Ym.
     */
    private void configurarColumnasDetalle() {
        tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaDetalle.setItems(viewModel.getDatosDetalle());

        colDetEmpleado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().nombreEmpleado()));
        colDetDepartamento.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().departamento()));
        colDetFecha.setCellValueFactory(d ->
                new SimpleStringProperty(formatearFecha(d.getValue().fecha())));
        colDetTurno.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().descripcionTurno()));
        colDetEntrada.setCellValueFactory(d ->
                new SimpleStringProperty(formatearHora(d.getValue().horaEntradaReal())));
        colDetSalida.setCellValueFactory(d ->
                new SimpleStringProperty(formatearHora(d.getValue().horaSalidaReal())));
        colDetTiempoReal.setCellValueFactory(d ->
                new SimpleStringProperty(formatearMinutos(d.getValue().tiempoTotalMinutos())));
        colDetTiempoTeorico.setCellValueFactory(d ->
                new SimpleStringProperty(formatearMinutos(d.getValue().tiempoTeoricoMinutos())));
        colDetExtras.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().minutosExtra() != null && d.getValue().minutosExtra() > 0
                                ? "+" + formatearMinutos(d.getValue().minutosExtra())
                                : "—"));
        colDetIncidencias.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().incidencias() != null && !d.getValue().incidencias().isBlank()
                                ? d.getValue().incidencias()
                                : ""));
    }

    /**
     * Configura los CellValueFactory de las columnas de la tabla de resumen.
     * Los campos de minutos se formatean como Xh Ym.
     */
    private void configurarColumnasResumen() {
        tablaResumen.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaResumen.setItems(viewModel.getDatosResumen());

        colResEmpleado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().nombreEmpleado()));
        colResDepartamento.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().departamento()));
        colResDias.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().diasTrabajados())));
        colResTiempoTeorico.setCellValueFactory(d ->
                new SimpleStringProperty(
                        formatearMinutos(d.getValue().totalTiempoTeoricoMinutos())));
        colResTiempoReal.setCellValueFactory(d ->
                new SimpleStringProperty(
                        formatearMinutos(d.getValue().totalTiempoTotalMinutos())));
        colResExtras.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().totalMinutosExtra() != null
                                && d.getValue().totalMinutosExtra() > 0
                                ? "+" + formatearMinutos(d.getValue().totalMinutosExtra())
                                : "—"));
    }

    /**
     * Gestiona el evento del botón Previsualizar.
     * Llama al ViewModel y muestra los datos en la tabla correspondiente.
     */
    @FXML
    private void handlePrevisualizar() {
        ocultarTablas();
        viewModel.previsualizar()
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarAlertaError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Gestiona el evento del botón Descargar PDF.
     * Abre el FileChooser con el nombre por defecto, descarga el PDF y
     * lo abre en el visor del sistema operativo.
     */
    @FXML
    private void handleDescargarPdf() {
        LanguageManager lang = LanguageManager.getInstance();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lang.getString("analisis.informeHorario.btn.descargar"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(viewModel.getNombreArchivoDefecto());

        File carpetaInicial = Paths.get(
                System.getProperty("user.home"), "Downloads").toFile();
        if (carpetaInicial.exists()) {
            fileChooser.setInitialDirectory(carpetaInicial);
        }

        File destino = fileChooser.showSaveDialog(
                btnDescargarPdf.getScene().getWindow());

        if (destino == null) return;

        viewModel.descargarPdf(destino.toPath())
                .thenRun(() -> Platform.runLater(() ->
                        mostrarAlertaExito(destino, lang)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarAlertaError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Muestra el Alert de éxito tras la descarga con la ruta del archivo
     * y un botón para abrir la carpeta en el explorador del sistema operativo.
     *
     * @param destino Archivo descargado.
     * @param lang    Gestor de idiomas activo.
     */
    private void mostrarAlertaExito(File destino, LanguageManager lang) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.getString("dialog.confirm.title"));
        alert.setHeaderText(null);
        alert.setContentText(
                lang.getString("analisis.informeHorario.exito")
                        .replace("{0}", destino.getAbsolutePath()));

        ButtonType btnAbrir = new ButtonType(
                lang.getString("analisis.btn.abrirCarpeta"));
        alert.getButtonTypes().add(btnAbrir);

        alert.showAndWait().ifPresent(bt -> {
            if (bt == btnAbrir) {
                try {
                    Desktop.getDesktop().open(destino.getParentFile());
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * Muestra un Alert de error con el mensaje recibido del servidor o del cliente.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarAlertaError(String mensaje) {
        LanguageManager lang = LanguageManager.getInstance();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.getString("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Valida el rango de fechas de forma inline y muestra u oculta el label de error.
     */
    private void validarRangoInline() {
        LocalDate ini = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();
        boolean rangoInvalido = ini != null && fin != null && ini.isAfter(fin);
        lblErrorRango.setVisible(rangoInvalido);
        lblErrorRango.setManaged(rangoInvalido);
    }

    /**
     * Actualiza la visibilidad de las tablas y la barra de estado según
     * el tipo de informe activo y si hay datos cargados.
     */
    private void actualizarVisibilidadTablas() {
        boolean esDetalle = viewModel.tipoInformeProperty().get() == TipoInforme.DETALLE;
        boolean hayDatos  = viewModel.hayDatosPreviaProperty().get();

        tablaDetalle.setVisible(esDetalle && hayDatos);
        tablaDetalle.setManaged(esDetalle && hayDatos);
        tablaResumen.setVisible(!esDetalle && hayDatos);
        tablaResumen.setManaged(!esDetalle && hayDatos);
        barraTabla.setVisible(hayDatos);
        barraTabla.setManaged(hayDatos);
        separadorTabla.setVisible(hayDatos);
        separadorTabla.setManaged(hayDatos);

        if (hayDatos) {
            actualizarBarraEstado();
        }
    }

    /**
     * Oculta ambas tablas y la barra de estado, por ejemplo al cambiar
     * el tipo de informe antes de previsualizar.
     */
    private void ocultarTablas() {
        tablaDetalle.setVisible(false);
        tablaDetalle.setManaged(false);
        tablaResumen.setVisible(false);
        tablaResumen.setManaged(false);
        barraTabla.setVisible(false);
        barraTabla.setManaged(false);
        separadorTabla.setVisible(false);
        separadorTabla.setManaged(false);
        viewModel.hayDatosPreviaProperty().set(false);
    }

    /**
     * Actualiza los textos de la barra de estado de la tabla con el tipo
     * de informe activo y el número de registros cargados.
     */
    private void actualizarBarraEstado() {
        LanguageManager lang = LanguageManager.getInstance();
        boolean esDetalle = viewModel.tipoInformeProperty().get() == TipoInforme.DETALLE;

        lblPrevia.setText(lang.getString("analisis.informeHorario.previa.label"));
        lblBadgeTipo.setText(esDetalle
                ? lang.getString("analisis.informeHorario.tipo.detalle")
                : lang.getString("analisis.informeHorario.tipo.resumen"));

        int conteo = esDetalle
                ? viewModel.getDatosDetalle().size()
                : viewModel.getDatosResumen().size();

        lblConteoRegistros.setText(conteo + " "
                + lang.getString("analisis.informeHorario.previa.registros"));
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
                    node.setStyle("-fx-bar-fill: #1A365D;");
                    instalarTooltipBarra(node, dato.getXValue(),
                            dato.getYValue().intValue());
                }
            });

            serie.getData().add(barra);
        }

        graficaFichajes.getData().add(serie);
        graficaFichajes.setBarGap(0);
        graficaFichajes.setCategoryGap(2);
    }

    /**
     * Instala un tooltip sobre el nodo de una barra con la fecha completa
     * localizada y el total de fichajes de ese día.
     *
     * @param node  Nodo JavaFX de la barra.
     * @param dia   Etiqueta del día en formato "dd".
     * @param total Número de fichajes del día.
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
        } catch (Exception ignored) {}
    }

    /**
     * Formatea una fecha ISO (yyyy-MM-dd) a formato dd/MM/yyyy.
     *
     * @param fechaIso Fecha en formato ISO.
     * @return Fecha formateada o la cadena original si no se puede parsear.
     */
    private String formatearFecha(String fechaIso) {
        if (fechaIso == null || fechaIso.isBlank()) return "—";
        try {
            return LocalDate.parse(fechaIso)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return fechaIso;
        }
    }

    /**
     * Recorta una hora en formato HH:mm:ss a HH:mm para mostrar en la tabla.
     *
     * @param hora Hora en formato HH:mm:ss.
     * @return Hora en formato HH:mm o "—" si el valor es nulo.
     */
    private String formatearHora(String hora) {
        if (hora == null || hora.isBlank()) return "—";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    /**
     * Formatea una cantidad de minutos como Xh Ym.
     * Si el valor es nulo o cero devuelve "0h 0m".
     *
     * @param minutos Total de minutos a formatear.
     * @return Cadena formateada en horas y minutos.
     */
    private String formatearMinutos(Long minutos) {
        if (minutos == null) return "0h 0m";
        return (minutos / 60) + "h " + (minutos % 60) + "m";
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

        lblSeccionTitulo.setText(lang.getString("analisis.informeHorario.seccion.titulo"));
        lblTipoInforme.setText(lang.getString("analisis.informeHorario.tipo"));
        lblEmpleado.setText(lang.getString("analisis.informeHorario.empleado"));
        lblFechaInicio.setText(lang.getString("analisis.informeHorario.fechaInicio"));
        lblFechaFin.setText(lang.getString("analisis.informeHorario.fechaFin"));
        lblErrorRango.setText(lang.getString("analisis.informeHorario.error.rangoInvalido"));

        btnPrevisualizar.setText(lang.getString("analisis.informeHorario.btn.previsualizar"));
        btnDescargarPdf.setText(lang.getString("analisis.informeHorario.btn.descargar"));

        colDetEmpleado.setText(lang.getString("analisis.detalle.col.empleado"));
        colDetDepartamento.setText(lang.getString("analisis.detalle.col.departamento"));
        colDetFecha.setText(lang.getString("analisis.detalle.col.fecha"));
        colDetTurno.setText(lang.getString("analisis.detalle.col.turno"));
        colDetEntrada.setText(lang.getString("analisis.detalle.col.entrada"));
        colDetSalida.setText(lang.getString("analisis.detalle.col.salida"));
        colDetTiempoReal.setText(lang.getString("analisis.detalle.col.tiempoReal"));
        colDetTiempoTeorico.setText(lang.getString("analisis.detalle.col.tiempoTeorico"));
        colDetExtras.setText(lang.getString("analisis.detalle.col.extras"));
        colDetIncidencias.setText(lang.getString("analisis.detalle.col.incidencias"));

        colResEmpleado.setText(lang.getString("analisis.resumen.col.empleado"));
        colResDepartamento.setText(lang.getString("analisis.resumen.col.departamento"));
        colResDias.setText(lang.getString("analisis.resumen.col.dias"));
        colResTiempoTeorico.setText(lang.getString("analisis.resumen.col.tiempoTeorico"));
        colResTiempoReal.setText(lang.getString("analisis.resumen.col.tiempoReal"));
        colResExtras.setText(lang.getString("analisis.resumen.col.extras"));

        lblTablaDetallVacia.setText(lang.getString("analisis.informeHorario.tabla.vacia"));
        lblTablaResumenVacia.setText(lang.getString("analisis.informeHorario.tabla.vacia"));

        actualizarLocalizacionDatePickers();
        comboTipoInforme.setConverter(comboTipoInforme.getConverter());
        comboEmpleado.setConverter(comboEmpleado.getConverter());
        actualizarGraficaFichajes();

        if (viewModel.hayDatosPreviaProperty().get()) {
            actualizarBarraEstado();
        }
    }
}
