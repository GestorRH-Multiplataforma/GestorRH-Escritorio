package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaTurnoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.TurnoFormViewModel.ModoFormulario;
import com.gestorrh.escritorio.presentation.viewmodel.TurnoViewModel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controlador para la vista del catálogo de turnos.
 * Gestiona la tabla, el filtro de búsqueda y la apertura del modal
 * de alta y edición de turnos.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class TurnosController {

    private static final Logger LOGGER = Logger.getLogger(TurnosController.class.getName());

    @FXML private TextField      campoBusqueda;
    @FXML private Button         btnNuevoTurno;

    @FXML private TableView<RespuestaTurnoDTO>   tablaTurnos;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colId;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colDescripcion;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colHoraInicio;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colHoraFin;
    @FXML private TableColumn<RespuestaTurnoDTO, Void>   colAcciones;

    @FXML private Label             labelError;
    @FXML private Label             labelTablaVacia;
    @FXML private ProgressIndicator indicadorCarga;

    private final TurnoViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Constructor del controlador. Obtiene el ViewModel desde la fábrica.
     */
    public TurnosController() {
        this.viewModel = ViewModelFactory.getInstance().createTurnoViewModel();
    }

    /**
     * Inicializa los bindings, configura la tabla y lanza la carga de datos.
     */
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarBindings();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.cargarTurnos();
    }

    /**
     * Libera el listener de idioma al destruirse la vista para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Configura los CellValueFactory de cada columna de la tabla.
     */
    private void configurarColumnas() {
        tablaTurnos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colId.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().idTurno())));

        colDescripcion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().descripcion()));

        colHoraInicio.setCellValueFactory(data ->
                new SimpleStringProperty(formatearHora(data.getValue().horaInicio())));

        colHoraFin.setCellValueFactory(data ->
                new SimpleStringProperty(formatearHora(data.getValue().horaFin())));

        configurarColumnaAcciones();
    }

    /**
     * Configura la columna Acciones con los botones Editar y Eliminar.
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button();
            private final Button btnEliminar = new Button();
            private final HBox contenedor    = new HBox(6, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
                btnEditar.getStyleClass().addAll("btn-tabla", "btn-tabla-editar");
                btnEliminar.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");

                btnEditar.setOnAction(e -> {
                    RespuestaTurnoDTO turno = getTableView().getItems().get(getIndex());
                    abrirModal(ModoFormulario.EDICION, turno);
                });

                btnEliminar.setOnAction(e -> {
                    RespuestaTurnoDTO turno = getTableView().getItems().get(getIndex());
                    handleEliminar(turno);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(javafx.geometry.Pos.CENTER);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                LanguageManager lang = LanguageManager.getInstance();
                btnEditar.setText(lang.getString("turnos.btn.editar"));
                btnEliminar.setText(lang.getString("turnos.btn.eliminar"));
                setGraphic(contenedor);
            }
        });
    }

    /**
     * Configura los bindings reactivos entre la vista y el ViewModel.
     */
    private void configurarBindings() {
        campoBusqueda.textProperty().bindBidirectional(viewModel.filtroTextoProperty());

        indicadorCarga.visibleProperty().bind(viewModel.cargandoProperty());
        indicadorCarga.managedProperty().bind(viewModel.cargandoProperty());

        labelError.textProperty().bind(viewModel.mensajeErrorProperty());
        labelError.visibleProperty().bind(viewModel.errorVisibleProperty());
        labelError.managedProperty().bind(viewModel.errorVisibleProperty());

        tablaTurnos.setItems(viewModel.getTurnosFiltrados());
    }

    /**
     * Abre el modal en modo Alta con todos los campos vacíos.
     */
    @FXML
    private void handleNuevoTurno() {
        abrirModal(ModoFormulario.ALTA, null);
    }

    /**
     * Muestra un diálogo de confirmación y elimina el turno si el usuario confirma.
     * Si la API rechaza el DELETE por asignaciones activas, muestra el mensaje del servidor.
     *
     * @param turno Turno a eliminar.
     */
    private void handleEliminar(RespuestaTurnoDTO turno) {
        LanguageManager lang = LanguageManager.getInstance();

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(lang.getString("dialog.confirm.title"));
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(lang.getString("turnos.eliminar.confirmacion"));

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) return;

        viewModel.eliminarTurno(turno.idTurno())
                .thenRun(() -> Platform.runLater(viewModel::cargarTurnos))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle(lang.getString("dialog.error.title"));
                        error.setHeaderText(null);
                        error.setContentText(cause.getMessage());
                        error.showAndWait();
                    });
                    return null;
                });
    }

    /**
     * Abre el modal de alta o edición de turno.
     *
     * @param modo  Modo de operación del formulario.
     * @param turno Datos del turno a editar, o null en modo Alta.
     */
    private void abrirModal(ModoFormulario modo, RespuestaTurnoDTO turno) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/turno-form-modal.fxml")
            );
            Parent root = loader.load();
            TurnoFormController controller = loader.getController();
            controller.inicializar(modo, turno);
            controller.setOnGuardadoExitoso(viewModel::cargarTurnos);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaTurnos.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("TurnosController: Error al abrir el modal de turno: " + e.getMessage());
        }
    }

    /**
     * Convierte una hora en formato "HH:mm:ss" (API) a "HH:mm" (display).
     *
     * @param horaApi Hora en formato "HH:mm:ss".
     * @return Hora en formato "HH:mm", o "—" si el valor es nulo.
     */
    private String formatearHora(String horaApi) {
        if (horaApi == null || horaApi.isBlank()) return "—";
        try {
            return horaApi.substring(0, 5);
        } catch (Exception e) {
            return horaApi;
        }
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        campoBusqueda.setPromptText(lang.getString("turnos.buscar.placeholder"));
        btnNuevoTurno.setText(lang.getString("turnos.btn.nuevo"));

        colId.setText(lang.getString("turnos.col.id"));
        colDescripcion.setText(lang.getString("turnos.col.descripcion"));
        colHoraInicio.setText(lang.getString("turnos.col.horaInicio"));
        colHoraFin.setText(lang.getString("turnos.col.horaFin"));
        colAcciones.setText(lang.getString("turnos.col.acciones"));

        if (labelTablaVacia != null) {
            labelTablaVacia.setText(lang.getString("turnos.tabla.vacia"));
        }
    }
}
