package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaAusenciaDTO;
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
import java.util.logging.Logger;

/**
 * Controlador para la vista del buzón de ausencias.
 * Gestiona un TabPane con tres pestañas (Pendientes, Aprobadas, Rechazadas).
 * La pestaña Pendientes carga al inicializar; las otras dos cargan de forma
 * lazy al hacer clic por primera vez.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AusenciasController {

    private static final Logger LOGGER = Logger.getLogger(AusenciasController.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TabPane tabPane;
    @FXML private Tab tabPendientes;
    @FXML private Tab tabAprobadas;
    @FXML private Tab tabRechazadas;

    @FXML private TableView<RespuestaAusenciaDTO> tablaPendientes;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colPendEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, Void>   colPendAcciones;
    @FXML private ProgressIndicator indicadorPendientes;
    @FXML private Label lblErrorPendientes;

    @FXML private TableView<RespuestaAusenciaDTO> tablaAprobadas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprResponsable;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colAprObservaciones;
    @FXML private ProgressIndicator indicadorAprobadas;
    @FXML private Label lblErrorAprobadas;

    @FXML private TableView<RespuestaAusenciaDTO> tablaRechazadas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecEmpleado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecTipo;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecFechas;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecDias;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecJustificante;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecEstado;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecResponsable;
    @FXML private TableColumn<RespuestaAusenciaDTO, String> colRecObservaciones;
    @FXML private ProgressIndicator indicadorRechazadas;
    @FXML private Label lblErrorRechazadas;

    private AusenciasViewModel viewModel;
    private boolean aprobadasCargadas  = false;
    private boolean rechazadasCargadas = false;

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
        configurarLazyLoading();

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

    // Configuración de tablas

    /**
     * Configura columnas, bindings y CellFactory de la tabla Pendientes.
     */
    private void configurarTablaPendientes() {
        tablaPendientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaPendientes.setItems(viewModel.getPendientes());

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

        configurarColumnaTipo(colPendTipo);
        configurarColumnaEstado(colPendEstado);
        configurarColumnaJustificante(colPendJustificante);
        configurarColumnaAcciones(colPendAcciones);

        indicadorPendientes.visibleProperty().bind(viewModel.cargandoPendientesProperty());
        indicadorPendientes.managedProperty().bind(viewModel.cargandoPendientesProperty());
        lblErrorPendientes.textProperty().bind(viewModel.mensajeErrorProperty());
        lblErrorPendientes.visibleProperty().bind(viewModel.errorVisibleProperty());
        lblErrorPendientes.managedProperty().bind(viewModel.errorVisibleProperty());
    }

    /**
     * Configura columnas, bindings y CellFactory de la tabla Aprobadas.
     */
    private void configurarTablaAprobadas() {
        tablaAprobadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaAprobadas.setItems(viewModel.getAprobadas());

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
    }

    /**
     * Configura columnas, bindings y CellFactory de la tabla Rechazadas.
     */
    private void configurarTablaRechazadas() {
        tablaRechazadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaRechazadas.setItems(viewModel.getRechazadas());

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
        columna.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().justificante()));
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
            }
            @Override
            protected void updateItem(String justificante, boolean empty) {
                super.updateItem(justificante, empty);
                if (empty) { setGraphic(null); setText(null); return; }
                if (justificante != null) {
                    setGraphic(btnDescargar);
                    setText(null);
                } else {
                    setGraphic(null);
                    setText("—");
                }
                getStyleClass().add("tabla-celda-centrada");
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

    // Handlers

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
            LOGGER.severe("AusenciasController: Error al abrir modal de revisión: "
                    + e.getMessage());
        }
    }

    /**
     * Descarga el justificante de una ausencia y muestra confirmación con la ruta.
     *
     * @param nombreArchivo Nombre del archivo a descargar.
     */
    private void handleDescargarJustificante(String nombreArchivo) {
        viewModel.descargarJustificante(nombreArchivo)
                .thenAccept(file -> Platform.runLater(() -> {
                    String mensaje = LanguageManager.getInstance()
                            .getString("ausencias.justificante.descargado")
                            .replace("{0}", file.getAbsolutePath());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle(LanguageManager.getInstance()
                            .getString("dialog.confirm.title"));
                    alert.setHeaderText(null);
                    alert.setContentText(mensaje);
                    alert.showAndWait();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle(LanguageManager.getInstance()
                                .getString("dialog.error.title"));
                        alert.setHeaderText(null);
                        alert.setContentText(causa.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
    }

    // Utilidades

    /**
     * Formatea un rango de fechas ISO a formato legible (dd/MM/yyyy – dd/MM/yyyy).
     *
     * @param inicioIso Fecha de inicio en formato ISO.
     * @param finIso    Fecha de fin en formato ISO.
     * @return Rango formateado.
     */
    private String formatearRangoFechas(String inicioIso, String finIso) {
        try {
            return LocalDate.parse(inicioIso).format(FORMATTER)
                    + " – "
                    + LocalDate.parse(finIso).format(FORMATTER);
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
            return ChronoUnit.DAYS.between(
                    LocalDate.parse(inicioIso),
                    LocalDate.parse(finIso)) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Atajo para obtener un texto localizado del LanguageManager.
     *
     * @param clave Clave i18n.
     * @return Texto traducido.
     */
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

        colAprEmpleado.setText(lang.getString("ausencias.col.empleado"));
        colAprTipo.setText(lang.getString("ausencias.col.tipo"));
        colAprFechas.setText(lang.getString("ausencias.col.fechas"));
        colAprDias.setText(lang.getString("ausencias.col.dias"));
        colAprJustificante.setText(lang.getString("ausencias.col.justificante"));
        colAprEstado.setText(lang.getString("ausencias.col.estado"));
        colAprResponsable.setText(lang.getString("ausencias.col.responsable"));
        colAprObservaciones.setText(lang.getString("ausencias.col.observaciones"));

        colRecEmpleado.setText(lang.getString("ausencias.col.empleado"));
        colRecTipo.setText(lang.getString("ausencias.col.tipo"));
        colRecFechas.setText(lang.getString("ausencias.col.fechas"));
        colRecDias.setText(lang.getString("ausencias.col.dias"));
        colRecJustificante.setText(lang.getString("ausencias.col.justificante"));
        colRecEstado.setText(lang.getString("ausencias.col.estado"));
        colRecResponsable.setText(lang.getString("ausencias.col.responsable"));
        colRecObservaciones.setText(lang.getString("ausencias.col.observaciones"));

        tablaPendientes.refresh();
        tablaAprobadas.refresh();
        tablaRechazadas.refresh();
    }
}
