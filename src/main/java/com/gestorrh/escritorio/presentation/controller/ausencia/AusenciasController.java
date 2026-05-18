package com.gestorrh.escritorio.presentation.controller.ausencia;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.data.network.dto.ausencia.RespuestaAusenciaDTO;
import com.gestorrh.escritorio.presentation.viewmodel.AusenciasViewModel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controlador para la vista del buzón de ausencias.
 * Gestiona un TabPane con tres pestañas (Pendientes, Aprobadas, Rechazadas).
 * Cada tabla calcula dinámicamente cuántas filas caben en el espacio disponible
 * y dispone de su propio footer de paginación independiente.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class AusenciasController implements Limpiable {

    private static final Logger LOGGER = Logger.getLogger(AusenciasController.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final double ALTURA_CELDA    = 52.0;
    private static final double ALTURA_CABECERA = 42.0;
    private static final int    FILAS_MINIMAS   = 3;

    // TabPane y pestañas
    @FXML private TabPane tabPane;
    @FXML private Tab tabPendientes;
    @FXML private Tab tabAprobadas;
    @FXML private Tab tabRechazadas;

    // Tabla Pendientes
    @FXML private TableView<RespuestaAusenciaDTO> tablaPendientes;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, Void>   colPendAcciones;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendDescripcion;
    @FXML private ProgressIndicator indicadorPendientes;
    @FXML private Label lblErrorPendientes;
    @FXML private Label lblPlaceholderPendientes;
    @FXML private Button btnAnteriorPendientes;
    @FXML private Button btnSiguientePendientes;
    @FXML private Label  labelPaginacionPendientes;

    // Tabla Aprobadas
    @FXML private TableView<RespuestaAusenciaDTO> tablaAprobadas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprResponsable;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprObservaciones;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprDescripcion;
    @FXML private ProgressIndicator indicadorAprobadas;
    @FXML private Label lblErrorAprobadas;
    @FXML private Label lblPlaceholderAprobadas;
    @FXML private Button btnAnteriorAprobadas;
    @FXML private Button btnSiguienteAprobadas;
    @FXML private Label  labelPaginacionAprobadas;

    // Tabla Rechazadas
    @FXML private TableView<RespuestaAusenciaDTO> tablaRechazadas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecResponsable;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecObservaciones;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecDescripcion;
    @FXML private ProgressIndicator indicadorRechazadas;
    @FXML private Label lblErrorRechazadas;
    @FXML private Label lblPlaceholderRechazadas;
    @FXML private Button btnAnteriorRechazadas;
    @FXML private Button btnSiguienteRechazadas;
    @FXML private Label  labelPaginacionRechazadas;

    private AusenciasViewModel viewModel;
    private boolean aprobadasCargadas  = false;
    private boolean rechazadasCargadas = false;

    // Paginación independiente por pestaña
    private int paginaPendientes = 0;
    private int paginaAprobadas  = 0;
    private int paginaRechazadas = 0;
    private int filasPorPagina   = 10;

    private java.util.function.Consumer<Integer> onPendientesActualizados;

    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el controlador, configura las tablas, los bindings y
     * lanza la carga inicial de la pestaña Pendientes.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createAusenciasViewModel();

        configurarTablaPendientes();
        configurarTablaAprobadas();
        configurarTablaRechazadas();
        configurarBotonesPaginacion();
        configurarLazyLoading();
        configurarPaginacionDinamica();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.inicializar();
    }

    /**
     * Libera el listener de idioma al destruirse la vista.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Escucha la altura de la tabla de pendientes (referencia) para recalcular
     * cuántas filas caben y actualizar las tres pestañas.
     */
    private void configurarPaginacionDinamica() {
        tablaPendientes.heightProperty().addListener((obs, oldH, newH) -> {
            int nuevasFilas = calcularFilasPorPagina(newH.doubleValue());
            if (nuevasFilas != filasPorPagina) {
                filasPorPagina   = nuevasFilas;
                paginaPendientes = 0;
                paginaAprobadas  = 0;
                paginaRechazadas = 0;
                actualizarPaginaPendientes();
                actualizarPaginaAprobadas();
                actualizarPaginaRechazadas();
            }
        });
    }

    /**
     * Calcula cuántas filas enteras caben en la altura disponible.
     */
    private int calcularFilasPorPagina(double alturaTabla) {
        double alturaDisponible = alturaTabla - ALTURA_CABECERA;
        int filas = (int) Math.floor(alturaDisponible / ALTURA_CELDA);
        return Math.max(filas, FILAS_MINIMAS);
    }

    /**
     * Configura los botones de paginación de cada pestaña.
     */
    private void configurarBotonesPaginacion() {
        btnAnteriorPendientes.setOnAction(e -> {
            if (paginaPendientes > 0) { paginaPendientes--; actualizarPaginaPendientes(); }
        });
        btnSiguientePendientes.setOnAction(e -> {
            if ((paginaPendientes + 1) * filasPorPagina < viewModel.getPendientes().size()) {
                paginaPendientes++; actualizarPaginaPendientes();
            }
        });

        btnAnteriorAprobadas.setOnAction(e -> {
            if (paginaAprobadas > 0) { paginaAprobadas--; actualizarPaginaAprobadas(); }
        });
        btnSiguienteAprobadas.setOnAction(e -> {
            if ((paginaAprobadas + 1) * filasPorPagina < viewModel.getAprobadas().size()) {
                paginaAprobadas++; actualizarPaginaAprobadas();
            }
        });

        btnAnteriorRechazadas.setOnAction(e -> {
            if (paginaRechazadas > 0) { paginaRechazadas--; actualizarPaginaRechazadas(); }
        });
        btnSiguienteRechazadas.setOnAction(e -> {
            if ((paginaRechazadas + 1) * filasPorPagina < viewModel.getRechazadas().size()) {
                paginaRechazadas++; actualizarPaginaRechazadas();
            }
        });
    }

    /**
     * Actualiza la página visible de la tabla Pendientes y su footer.
     */
    private void actualizarPaginaPendientes() {
        List<RespuestaAusenciaDTO> todos = viewModel.getPendientes();
        int total  = todos.size();
        int desde  = paginaPendientes * filasPorPagina;
        int hasta  = Math.min(desde + filasPorPagina, total);

        tablaPendientes.getItems().setAll(desde < total ? todos.subList(desde, hasta) : List.of());

        boolean hayVarias = total > filasPorPagina;
        btnAnteriorPendientes.setVisible(hayVarias);
        btnAnteriorPendientes.setManaged(hayVarias);
        btnSiguientePendientes.setVisible(hayVarias);
        btnSiguientePendientes.setManaged(hayVarias);
        btnAnteriorPendientes.setDisable(paginaPendientes == 0);
        btnSiguientePendientes.setDisable(hasta >= total);
        actualizarLabelPaginacion(labelPaginacionPendientes, desde + 1, hasta, total);
    }

    /**
     * Actualiza la página visible de la tabla Aprobadas y su footer.
     */
    private void actualizarPaginaAprobadas() {
        List<RespuestaAusenciaDTO> todos = viewModel.getAprobadas();
        int total  = todos.size();
        int desde  = paginaAprobadas * filasPorPagina;
        int hasta  = Math.min(desde + filasPorPagina, total);

        tablaAprobadas.getItems().setAll(desde < total ? todos.subList(desde, hasta) : List.of());

        boolean hayVarias = total > filasPorPagina;
        btnAnteriorAprobadas.setVisible(hayVarias);
        btnAnteriorAprobadas.setManaged(hayVarias);
        btnSiguienteAprobadas.setVisible(hayVarias);
        btnSiguienteAprobadas.setManaged(hayVarias);
        btnAnteriorAprobadas.setDisable(paginaAprobadas == 0);
        btnSiguienteAprobadas.setDisable(hasta >= total);
        actualizarLabelPaginacion(labelPaginacionAprobadas, desde + 1, hasta, total);
    }

    /**
     * Actualiza la página visible de la tabla Rechazadas y su footer.
     */
    private void actualizarPaginaRechazadas() {
        List<RespuestaAusenciaDTO> todos = viewModel.getRechazadas();
        int total  = todos.size();
        int desde  = paginaRechazadas * filasPorPagina;
        int hasta  = Math.min(desde + filasPorPagina, total);

        tablaRechazadas.getItems().setAll(desde < total ? todos.subList(desde, hasta) : List.of());

        boolean hayVarias = total > filasPorPagina;
        btnAnteriorRechazadas.setVisible(hayVarias);
        btnAnteriorRechazadas.setManaged(hayVarias);
        btnSiguienteRechazadas.setVisible(hayVarias);
        btnSiguienteRechazadas.setManaged(hayVarias);
        btnAnteriorRechazadas.setDisable(paginaRechazadas == 0);
        btnSiguienteRechazadas.setDisable(hasta >= total);
        actualizarLabelPaginacion(labelPaginacionRechazadas, desde + 1, hasta, total);
    }

    /**
     * Actualiza el label de paginación de una pestaña.
     */
    private void actualizarLabelPaginacion(Label label, int desde, int hasta, int total) {
        LanguageManager lang = LanguageManager.getInstance();
        if (total == 0) {
            label.setText("0 " + lang.getString("empleados.paginacion.resultados"));
            return;
        }
        String plantilla = lang.getString("empleados.paginacion.mostrando");
        label.setText(plantilla
                .replace("{0}", String.valueOf(desde))
                .replace("{1}", String.valueOf(hasta))
                .replace("{2}", String.valueOf(total)));
    }

    /**
     * Configura columnas, bindings y CellFactory de la tabla Pendientes.
     */
    private void configurarTablaPendientes() {
        tablaPendientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colPendEmpleado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().nombreCompletoEmpleado()));
        colPendTipo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().tipo()));
        colPendFechas.setCellValueFactory(d ->
                new SimpleStringProperty(formatearRangoFechas(
                        d.getValue().fechaInicio(), d.getValue().fechaFin())));
        colPendDias.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        calcularDias(d.getValue().fechaInicio(), d.getValue().fechaFin()))));
        colPendDescripcion.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().descripcion() != null
                        ? d.getValue().descripcion() : "—"));

        configurarColumnaTipo(colPendTipo);
        configurarColumnaEstado(colPendEstado);
        configurarColumnaJustificante(colPendJustificante);
        configurarColumnaAcciones(colPendAcciones);

        indicadorPendientes.visibleProperty().bind(viewModel.cargandoPendientesProperty());
        indicadorPendientes.managedProperty().bind(viewModel.cargandoPendientesProperty());
        lblErrorPendientes.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorPendientes.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorPendientes.managedProperty().bind(viewModel.errorVisibleProperty());

        colPendFechas.getStyleClass().add("col-centrada");
        colPendDias.getStyleClass().add("col-centrada");

        viewModel.getPendientes().addListener(
                (javafx.collections.ListChangeListener<RespuestaAusenciaDTO>) cambio ->
                        Platform.runLater(() -> {
                            paginaPendientes = 0;
                            actualizarPaginaPendientes();
                            actualizarBadgePendientes();
                            if (onPendientesActualizados != null) {
                                onPendientesActualizados.accept(viewModel.getPendientes().size());
                            }
                        })
        );
    }

    /**
     * Configura columnas, bindings y CellFactory de la tabla Aprobadas.
     */
    private void configurarTablaAprobadas() {
        tablaAprobadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colAprEmpleado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().nombreCompletoEmpleado()));
        colAprTipo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().tipo()));
        colAprFechas.setCellValueFactory(d ->
                new SimpleStringProperty(formatearRangoFechas(
                        d.getValue().fechaInicio(), d.getValue().fechaFin())));
        colAprDias.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        calcularDias(d.getValue().fechaInicio(), d.getValue().fechaFin()))));
        colAprDescripcion.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().descripcion() != null
                        ? d.getValue().descripcion() : "—"));
        colAprResponsable.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().responsableRevision() != null
                        ? d.getValue().responsableRevision() : "—"));
        colAprObservaciones.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().observacionesRevision() != null
                        ? d.getValue().observacionesRevision() : "—"));

        configurarColumnaTipo(colAprTipo);
        configurarColumnaEstado(colAprEstado);
        configurarColumnaJustificante(colAprJustificante);

        indicadorAprobadas.visibleProperty().bind(viewModel.cargandoAprobadasProperty());
        indicadorAprobadas.managedProperty().bind(viewModel.cargandoAprobadasProperty());
        lblErrorAprobadas.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorAprobadas.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorAprobadas.managedProperty().bind(viewModel.errorVisibleProperty());

        colAprFechas.getStyleClass().add("col-centrada");
        colAprDias.getStyleClass().add("col-centrada");

        viewModel.getAprobadas().addListener(
                (javafx.collections.ListChangeListener<RespuestaAusenciaDTO>) cambio ->
                        Platform.runLater(() -> {
                            paginaAprobadas = 0;
                            actualizarPaginaAprobadas();
                        })
        );
    }

    /**
     * Configura columnas, bindings y CellFactory de la tabla Rechazadas.
     */
    private void configurarTablaRechazadas() {
        tablaRechazadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colRecEmpleado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().nombreCompletoEmpleado()));
        colRecTipo.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().tipo()));
        colRecFechas.setCellValueFactory(d ->
                new SimpleStringProperty(formatearRangoFechas(
                        d.getValue().fechaInicio(), d.getValue().fechaFin())));
        colRecDias.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(
                        calcularDias(d.getValue().fechaInicio(), d.getValue().fechaFin()))));
        colRecDescripcion.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().descripcion() != null
                        ? d.getValue().descripcion() : "—"));
        colRecResponsable.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().responsableRevision() != null
                        ? d.getValue().responsableRevision() : "—"));
        colRecObservaciones.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().observacionesRevision() != null
                        ? d.getValue().observacionesRevision() : "—"));

        configurarColumnaTipo(colRecTipo);
        configurarColumnaEstado(colRecEstado);
        configurarColumnaJustificante(colRecJustificante);

        indicadorRechazadas.visibleProperty().bind(viewModel.cargandoRechadasProperty());
        indicadorRechazadas.managedProperty().bind(viewModel.cargandoRechadasProperty());
        lblErrorRechazadas.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorRechazadas.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorRechazadas.managedProperty().bind(viewModel.errorVisibleProperty());

        colRecFechas.getStyleClass().add("col-centrada");
        colRecDias.getStyleClass().add("col-centrada");

        viewModel.getRechazadas().addListener(
                (javafx.collections.ListChangeListener<RespuestaAusenciaDTO>) cambio ->
                        Platform.runLater(() -> {
                            paginaRechazadas = 0;
                            actualizarPaginaRechazadas();
                        })
        );
    }

    /**
     * Configura el CellFactory de una columna Tipo con badge coloreado y texto localizado.
     *
     * @param columna Columna a configurar.
     */
    private void configurarColumnaTipo(TableColumn<RespuestaAusenciaDTO, String> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) { setGraphic(null); return; }
                Label badge = new Label(localizar("ausencias.tipo." + tipo.toLowerCase()));
                badge.getStyleClass().addAll("badge", "badge-ausencia-" + tipo.toLowerCase());
                setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /**
     * Configura el CellFactory de una columna Estado con badge coloreado.
     *
     * @param columna Columna a configurar.
     */
    private void configurarColumnaEstado(TableColumn<RespuestaAusenciaDTO, String> columna) {
        columna.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        columna.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label badge = new Label(localizar("ausencias.estado." + estado.toLowerCase()));
                badge.getStyleClass().addAll("badge", "badge-ausencia-" + estado.toLowerCase());
                setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /**
     * Configura el CellFactory de una columna Justificante.
     * Muestra un icono de clip clicable si hay adjunto, guión si no.
     *
     * @param columna Columna a configurar.
     */
    private void configurarColumnaJustificante(TableColumn<RespuestaAusenciaDTO, String> columna) {
        columna.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().justificante()));
        columna.setCellFactory(col -> new TableCell<>() {
            private final Button btnDescargar = new Button();
            {
                org.kordamp.ikonli.javafx.FontIcon icono =
                        new org.kordamp.ikonli.javafx.FontIcon("mdi2p-paperclip");
                icono.setIconSize(16);
                icono.getStyleClass().add("ausencias-justificante-icono");
                btnDescargar.setGraphic(icono);
                btnDescargar.getStyleClass().add("btn-clip");
                btnDescargar.setOnAction(e -> {
                    RespuestaAusenciaDTO ausencia = getTableView().getItems().get(getIndex());
                    handleDescargarJustificante(ausencia.justificante());
                });
                getStyleClass().add("tabla-celda-centrada");
            }
            @Override
            protected void updateItem(String justificante, boolean empty) {
                super.updateItem(justificante, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                if (justificante != null) { setGraphic(btnDescargar); setText(null); }
                else { setGraphic(null); setText("—"); }
            }
        });
    }

    /**
     * Configura el CellFactory de la columna Acciones de la pestaña Pendientes.
     * Muestra los botones Aprobar y Rechazar.
     *
     * @param columna Columna a configurar.
     */
    private void configurarColumnaAcciones(TableColumn<RespuestaAusenciaDTO, Void> columna) {
        columna.setCellFactory(col -> new TableCell<>() {
            private final Button btnAprobar  = new Button();
            private final Button btnRechazar = new Button();
            private final HBox contenedor    = new HBox(6, btnAprobar, btnRechazar);
            {
                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
                btnAprobar.getStyleClass().addAll("btn-tabla", "btn-tabla-readmitir");
                btnRechazar.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");
                btnAprobar.setOnAction(e -> {
                    RespuestaAusenciaDTO ausencia = getTableView().getItems().get(getIndex());
                    abrirModalRevision(ausencia, "APROBADA");
                });
                btnRechazar.setOnAction(e -> {
                    RespuestaAusenciaDTO ausencia = getTableView().getItems().get(getIndex());
                    abrirModalRevision(ausencia, "RECHAZADA");
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(javafx.geometry.Pos.CENTER);
                if (empty) { setGraphic(null); return; }
                btnAprobar.setText(localizar("ausencias.btn.aprobar"));
                btnRechazar.setText(localizar("ausencias.btn.rechazar"));
                setGraphic(contenedor);
            }
        });
    }

    /**
     * Configura el lazy loading de las pestañas Aprobadas y Rechazadas.
     * Solo carga los datos la primera vez que se selecciona cada pestaña.
     */
    private void configurarLazyLoading() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (nueva == tabAprobadas && !aprobadasCargadas) {
                aprobadasCargadas = true;
                viewModel.cargarAprobadas();
            } else if (nueva == tabRechazadas && !rechazadasCargadas) {
                rechazadasCargadas = true;
                viewModel.cargarRechazadas();
            }
        });
    }

    /**
     * Abre el modal de revisión para aprobar o rechazar una ausencia.
     *
     * @param ausencia      Ausencia a revisar.
     * @param estadoDestino APROBADA o RECHAZADA.
     */
    private void abrirModalRevision(RespuestaAusenciaDTO ausencia, String estadoDestino) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/revision-ausencia-modal.fxml"));
            Parent root = loader.load();
            RevisionAusenciaModalController controller = loader.getController();
            controller.inicializar(ausencia, estadoDestino, viewModel);
            controller.setOnRevisionExitosa(() -> {
                aprobadasCargadas  = false;
                rechazadasCargadas = false;
                recargarPestanaActivaSiNecesario();
            });

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaPendientes.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("AusenciasController: Error al abrir modal de revisión: " + e.getMessage());
        }
    }

    /**
     * Abre un FileChooser para que el usuario elija dónde guardar el justificante,
     * luego descarga el archivo y muestra confirmación con la ruta final.
     *
     * @param nombreArchivo Nombre del archivo tal como viene en el DTO.
     */
    private void handleDescargarJustificante(String nombreArchivo) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle(localizar("ausencias.justificante.guardar.titulo"));
        fileChooser.setInitialFileName(nombreArchivo);
        String extension = nombreArchivo.contains(".")
                ? "*" + nombreArchivo.substring(nombreArchivo.lastIndexOf('.')) : "*.*";
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter(
                        localizar("ausencias.justificante.filtro"), extension));
        fileChooser.setInitialDirectory(
                java.nio.file.Paths.get(System.getProperty("user.home"), "Downloads").toFile());

        java.io.File destino = fileChooser.showSaveDialog(tablaPendientes.getScene().getWindow());
        if (destino == null) return;

        viewModel.descargarJustificante(nombreArchivo, destino.toPath())
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(localizar("dialog.error.title"));
                        alert.setHeaderText(null);
                        alert.setContentText(causa.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Actualiza el badge contador de la pestaña Pendientes.
     * Muestra el número de ausencias pendientes junto al título de la pestaña.
     */
    private void actualizarBadgePendientes() {
        LanguageManager lang = LanguageManager.getInstance();
        int total = viewModel.getPendientes().size();

        Label lblTab = new Label(lang.getString("ausencias.tab.pendientes"));
        lblTab.getStyleClass().add("ausencias-tab-label");

        if (total > 0) {
            Label badge = new Label(String.valueOf(total));
            badge.getStyleClass().add("ausencias-tab-badge");
            HBox contenedor = new HBox(6, lblTab, badge);
            contenedor.setAlignment(javafx.geometry.Pos.CENTER);
            tabPendientes.setGraphic(contenedor);
        } else {
            tabPendientes.setGraphic(new HBox(lblTab));
        }
        tabPendientes.setText(null);
    }

    private void recargarPestanaActivaSiNecesario() {
        Tab pestanaActiva = tabPane.getSelectionModel().getSelectedItem();
        if (pestanaActiva == tabAprobadas && !aprobadasCargadas) {
            aprobadasCargadas = true;
            viewModel.cargarAprobadas();
        } else if (pestanaActiva == tabRechazadas && !rechazadasCargadas) {
            rechazadasCargadas = true;
            viewModel.cargarRechazadas();
        }
    }

    /**
     * Registra el callback para actualizar el badge del sidebar.
     */
    public void setOnPendientesActualizados(java.util.function.Consumer<Integer> callback) {
        this.onPendientesActualizados = callback;
    }

    private String formatearRangoFechas(String inicioIso, String finIso) {
        try {
            return LocalDate.parse(inicioIso).format(FORMATTER)
                    + " – " + LocalDate.parse(finIso).format(FORMATTER);
        } catch (Exception e) {
            return inicioIso + " – " + finIso;
        }
    }

    /**
     * Calcula el número de días de una ausencia incluyendo ambos extremos.
     *
     * @param inicioIso Fecha de inicio en formato ISO.
     * @param finIso    Fecha de fin en formato ISO.
     * @return Número de días o 0 si las fechas no son válidas.
     */
    private long calcularDias(String inicioIso, String finIso) {
        try {
            return ChronoUnit.DAYS.between(LocalDate.parse(inicioIso), LocalDate.parse(finIso)) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private String localizar(String clave) {
        return LanguageManager.getInstance().getString(clave);
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        tabPendientes.setText(lang.getString("ausencias.tab.pendientes"));
        tabAprobadas.setText(lang.getString("ausencias.tab.aprobadas"));
        tabRechazadas.setText(lang.getString("ausencias.tab.rechazadas"));

        colPendEmpleado.setText(lang.getString("ausencias.col.empleado"));
        colPendTipo.setText(lang.getString("ausencias.col.tipo"));
        colPendFechas.setText(lang.getString("ausencias.col.fechas"));
        colPendDias.setText(lang.getString("ausencias.col.dias"));
        colPendJustificante.setText(lang.getString("ausencias.col.justificante"));
        colPendEstado.setText(lang.getString("ausencias.col.estado"));
        colPendAcciones.setText(lang.getString("ausencias.col.acciones"));
        colPendDescripcion.setText(lang.getString("ausencias.col.descripcion"));

        colAprEmpleado.setText(lang.getString("ausencias.col.empleado"));
        colAprTipo.setText(lang.getString("ausencias.col.tipo"));
        colAprFechas.setText(lang.getString("ausencias.col.fechas"));
        colAprDias.setText(lang.getString("ausencias.col.dias"));
        colAprJustificante.setText(lang.getString("ausencias.col.justificante"));
        colAprEstado.setText(lang.getString("ausencias.col.estado"));
        colAprResponsable.setText(lang.getString("ausencias.col.responsable"));
        colAprObservaciones.setText(lang.getString("ausencias.col.observaciones"));
        colAprDescripcion.setText(lang.getString("ausencias.col.descripcion"));

        colRecEmpleado.setText(lang.getString("ausencias.col.empleado"));
        colRecTipo.setText(lang.getString("ausencias.col.tipo"));
        colRecFechas.setText(lang.getString("ausencias.col.fechas"));
        colRecDias.setText(lang.getString("ausencias.col.dias"));
        colRecJustificante.setText(lang.getString("ausencias.col.justificante"));
        colRecEstado.setText(lang.getString("ausencias.col.estado"));
        colRecResponsable.setText(lang.getString("ausencias.col.responsable"));
        colRecObservaciones.setText(lang.getString("ausencias.col.observaciones"));
        colRecDescripcion.setText(lang.getString("ausencias.col.descripcion"));

        lblPlaceholderPendientes.setText(lang.getString("ausencias.tabla.pendientes.vacia"));
        lblPlaceholderAprobadas.setText(lang.getString("ausencias.tabla.aprobadas.vacia"));
        lblPlaceholderRechazadas.setText(lang.getString("ausencias.tabla.rechazadas.vacia"));

        btnAnteriorPendientes.setText(lang.getString("empleados.btn.anterior"));
        btnSiguientePendientes.setText(lang.getString("empleados.btn.siguiente"));
        btnAnteriorAprobadas.setText(lang.getString("empleados.btn.anterior"));
        btnSiguienteAprobadas.setText(lang.getString("empleados.btn.siguiente"));
        btnAnteriorRechazadas.setText(lang.getString("empleados.btn.anterior"));
        btnSiguienteRechazadas.setText(lang.getString("empleados.btn.siguiente"));

        tablaPendientes.refresh();
        tablaAprobadas.refresh();
        tablaRechazadas.refresh();
        actualizarBadgePendientes();
    }
}
