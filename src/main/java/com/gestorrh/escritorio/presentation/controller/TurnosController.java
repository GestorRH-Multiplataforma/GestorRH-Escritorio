package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.NavigationManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaTurnoDTO;
import com.gestorrh.escritorio.presentation.component.CalendarioMensual;
import com.gestorrh.escritorio.presentation.viewmodel.AsignacionTurnosViewModel;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Controlador para la vista de turnos. Gestiona dos pestañas:
 * la pestaña de asignación de turnos en calendario y la pestaña
 * del catálogo de turnos (CRUD).
 *
 * @author Fco Javier García Cañero
 * @version 2.0
 */
public class TurnosController {

    private static final Logger LOGGER = Logger.getLogger(TurnosController.class.getName());

    @FXML private TabPane tabPane;
    @FXML private Tab tabAsignaciones;
    @FXML private Tab tabCatalogo;

    @FXML private HBox bannerSedeNoConfigurada;
    @FXML private Label lblBannerSede;
    @FXML private Hyperlink linkConfigurar;

    @FXML private Label lblFormTitulo;
    @FXML private Label lblEmpleado;
    @FXML private Label lblTurno;
    @FXML private Label lblModalidad;
    @FXML private ComboBox<RespuestaEmpleadoDTO> comboEmpleado;
    @FXML private ComboBox<RespuestaTurnoDTO> comboTurno;
    @FXML private ComboBox<String> comboModalidad;
    @FXML private Button btnAsignar;
    @FXML private Label lblErrorFormulario;

    @FXML private CalendarioMensual calendarioAsignaciones;

    @FXML private Label lblResumenTitulo;
    @FXML private ProgressIndicator indicadorCargaAsignaciones;
    @FXML private Label lblResumenVacio;
    @FXML private VBox listaAsignacionesDia;
    @FXML private Label lblTotalDia;

    @FXML private TextField campoBusqueda;
    @FXML private Button btnNuevoTurno;
    @FXML private TableView<RespuestaTurnoDTO> tablaTurnos;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colDescripcion;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colHoraInicio;
    @FXML private TableColumn<RespuestaTurnoDTO, String> colHoraFin;
    @FXML private TableColumn<RespuestaTurnoDTO, Void> colAcciones;
    @FXML private Label labelError;
    @FXML private Label labelTablaVacia;
    @FXML private ProgressIndicator indicadorCarga;

    @FXML private VBox panelTips;

    private final TurnoViewModel turnoViewModel;
    private final AsignacionTurnosViewModel asignacionViewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Constructor del controlador. Obtiene ambos ViewModels desde la fábrica.
     */
    public TurnosController() {
        this.turnoViewModel = ViewModelFactory.getInstance().createTurnoViewModel();
        this.asignacionViewModel = ViewModelFactory.getInstance().createAsignacionTurnosViewModel();
    }

    /**
     * Inicializa los bindings, configura ambas pestañas y lanza la carga de datos.
     */
    @FXML
    public void initialize() {
        configurarPestanaCatalogo();
        configurarPestanaAsignaciones();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        turnoViewModel.cargarTurnos();
        asignacionViewModel.inicializar();
        asignacionViewModel.getAsignacionesMes().addListener(
                (javafx.collections.ListChangeListener<RespuestaAsignacionTurnoDTO>) cambio -> {
                    if (asignacionViewModel.diaSeleccionadoProperty().get() == null) {
                        Platform.runLater(() -> handleDiaClick(LocalDate.now()));
                    }
                }
        );
        mostrarTips();
    }

    /**
     * Libera los listeners de idioma y del calendario al destruirse la vista.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
        if (calendarioAsignaciones != null) {
            calendarioAsignaciones.limpiar();
        }
    }

    /**
     * Configura las columnas, bindings y la tabla del catálogo de turnos.
     */
    private void configurarPestanaCatalogo() {
        tablaTurnos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colDescripcion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().descripcion()));
        colHoraInicio.setCellValueFactory(data ->
                new SimpleStringProperty(formatearHora(data.getValue().horaInicio())));
        colHoraFin.setCellValueFactory(data ->
                new SimpleStringProperty(formatearHora(data.getValue().horaFin())));

        configurarColumnaAccionesCatalogo();

        campoBusqueda.textProperty().bindBidirectional(turnoViewModel.filtroTextoProperty());
        indicadorCarga.visibleProperty().bind(turnoViewModel.cargandoProperty());
        indicadorCarga.managedProperty().bind(turnoViewModel.cargandoProperty());
        labelError.textProperty().bind(turnoViewModel.mensajeErrorProperty());
        labelError.visibleProperty().bind(turnoViewModel.errorVisibleProperty());
        labelError.managedProperty().bind(turnoViewModel.errorVisibleProperty());
        tablaTurnos.setItems(turnoViewModel.getTurnosFiltrados());
    }

    /**
     * Configura la columna de acciones del catálogo con los botones Editar y Eliminar.
     */
    private void configurarColumnaAccionesCatalogo() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnEliminar = new Button();
            private final HBox contenedor = new HBox(6, btnEditar, btnEliminar);

            {
                contenedor.setAlignment(javafx.geometry.Pos.CENTER);
                btnEditar.getStyleClass().addAll("btn-tabla", "btn-tabla-editar");
                btnEliminar.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");

                btnEditar.setOnAction(e -> {
                    RespuestaTurnoDTO turno = getTableView().getItems().get(getIndex());
                    abrirModalTurno(ModoFormulario.EDICION, turno);
                });

                btnEliminar.setOnAction(e -> {
                    RespuestaTurnoDTO turno = getTableView().getItems().get(getIndex());
                    handleEliminarTurno(turno);
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
     * Configura los combos, el calendario, los bindings y los listeners
     * de la pestaña de asignación de turnos.
     */
    private void configurarPestanaAsignaciones() {
        configurarComboEmpleado();
        configurarComboTurno();

        comboModalidad.setItems(asignacionViewModel.getModalidades());
        comboModalidad.setConverter(new StringConverter<>() {
            @Override
            public String toString(String modalidad) {
                if (modalidad == null) return "";
                return switch (modalidad.toUpperCase()) {
                    case "PRESENCIAL" -> LanguageManager.getInstance()
                            .getString("asignaciones.modalidad.presencial");
                    case "TELETRABAJO" -> LanguageManager.getInstance()
                            .getString("asignaciones.modalidad.teletrabajo");
                    default -> modalidad.charAt(0) + modalidad.substring(1).toLowerCase();
                };
            }
            @Override
            public String fromString(String s) { return null; }
        });
        comboModalidad.setPromptText(LanguageManager.getInstance()
                .getString("asignaciones.form.modalidad.placeholder"));
        comboModalidad.valueProperty().bindBidirectional(
                asignacionViewModel.modalidadSeleccionadaProperty());

        asignacionViewModel.sedeConfiguradaProperty().addListener((obs, anterior, configurada) ->
                Platform.runLater(() -> btnAsignar.setDisable(!configurada))
        );
        btnAsignar.setDisable(!asignacionViewModel.sedeConfiguradaProperty().get());

        asignacionViewModel.sedeConfiguradaProperty().addListener((obs, anterior, configurada) ->
                Platform.runLater(() -> actualizarBannerSede(configurada))
        );

        asignacionViewModel.cargandoProperty().addListener((obs, anterior, cargando) ->
                Platform.runLater(() -> {
                    indicadorCargaAsignaciones.setVisible(cargando);
                    indicadorCargaAsignaciones.setManaged(cargando);
                })
        );

        calendarioAsignaciones.setOnDiaClick(this::handleDiaClick);

        calendarioAsignaciones.mesActualProperty().addListener((obs, anterior, nuevoMes) -> {
            asignacionViewModel.filtrarPorMes(nuevoMes);
            actualizarMarcasCalendario();
        });

        asignacionViewModel.getAsignacionesMes().addListener(
                (javafx.collections.ListChangeListener<RespuestaAsignacionTurnoDTO>) cambio ->
                        Platform.runLater(this::actualizarMarcasCalendario)
        );

        asignacionViewModel.getAsignacionesDia().addListener(
                (javafx.collections.ListChangeListener<RespuestaAsignacionTurnoDTO>) cambio ->
                        Platform.runLater(this::actualizarResumenDia)
        );
        LanguageManager.getInstance().addListener(() ->
                Platform.runLater(this::actualizarMarcasCalendario)
        );
    }

    /**
     * Configura el ComboBox de empleados con su StringConverter y binding.
     */
    private void configurarComboEmpleado() {
        comboEmpleado.setItems(asignacionViewModel.getEmpleados());
        comboEmpleado.setConverter(new StringConverter<>() {
            @Override
            public String toString(RespuestaEmpleadoDTO empleado) {
                if (empleado == null) return "";
                return empleado.nombre() + " " + empleado.apellidos();
            }
            @Override
            public RespuestaEmpleadoDTO fromString(String s) { return null; }
        });
        comboEmpleado.setPromptText(LanguageManager.getInstance()
                .getString("asignaciones.form.empleado.placeholder"));
        comboEmpleado.valueProperty().bindBidirectional(
                asignacionViewModel.empleadoSeleccionadoProperty());
    }

    /**
     * Configura el ComboBox de turnos con su StringConverter y binding.
     */
    private void configurarComboTurno() {
        comboTurno.setItems(asignacionViewModel.getTurnos());
        comboTurno.setConverter(new StringConverter<>() {
            @Override
            public String toString(RespuestaTurnoDTO turno) {
                if (turno == null) return "";
                return turno.descripcion() + " ("
                        + formatearHora(turno.horaInicio()) + " - "
                        + formatearHora(turno.horaFin()) + ")";
            }
            @Override
            public RespuestaTurnoDTO fromString(String s) { return null; }
        });
        comboTurno.setPromptText(LanguageManager.getInstance()
                .getString("asignaciones.form.turno.placeholder"));
        comboTurno.valueProperty().bindBidirectional(
                asignacionViewModel.turnoSeleccionadoProperty());
    }

    /**
     * Muestra u oculta el banner de sede no configurada según el estado.
     *
     * @param configurada true si la sede está configurada correctamente.
     */
    private void actualizarBannerSede(boolean configurada) {
        bannerSedeNoConfigurada.setVisible(!configurada);
        bannerSedeNoConfigurada.setManaged(!configurada);
    }

    /**
     * Limpia las marcas del calendario y las vuelve a aplicar según las
     * asignaciones del mes actual. Diferencia presencial de teletrabajo por color.
     */
    private void actualizarMarcasCalendario() {
        calendarioAsignaciones.limpiarMarcas();
        for (RespuestaAsignacionTurnoDTO asignacion : asignacionViewModel.getAsignacionesMes()) {
            if (asignacion.fecha() == null) continue;
            LocalDate fecha = LocalDate.parse(asignacion.fecha());
            calendarioAsignaciones.marcarDia(fecha, "calendario-dia--con-asignacion");
        }
    }

    /**
     * Gestiona el clic en un día del calendario. Actualiza el ViewModel
     * y refresca el panel de resumen del día.
     *
     * @param fecha Fecha clicada en el calendario.
     */
    private void handleDiaClick(LocalDate fecha) {
        asignacionViewModel.filtrarPorDia(fecha);
        actualizarTituloResumen(fecha);
    }

    /**
     * Actualiza el título del panel de resumen con la fecha seleccionada.
     *
     * @param fecha Fecha seleccionada.
     */
    private void actualizarTituloResumen(LocalDate fecha) {
        String fechaFormateada = fecha.format(
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
                        LanguageManager.getInstance().getCurrentLocale()));
        String plantilla = LanguageManager.getInstance().getString("asignaciones.resumen.titulo");
        lblResumenTitulo.setText(plantilla.replace("{0}", fechaFormateada));
    }

    /**
     * Reconstruye el panel de resumen del día con las asignaciones actuales.
     * Muestra el mensaje vacío si no hay asignaciones.
     */
    private void actualizarResumenDia() {
        listaAsignacionesDia.getChildren().clear();
        lblResumenVacio.setVisible(false);
        lblResumenVacio.setManaged(false);
        lblTotalDia.setVisible(false);
        lblTotalDia.setManaged(false);

        if (asignacionViewModel.diaSeleccionadoProperty().get() == null) {
            return;
        }

        if (asignacionViewModel.getAsignacionesDia().isEmpty()) {
            lblResumenVacio.setText(LanguageManager.getInstance()
                    .getString("asignaciones.resumen.vacio"));
            lblResumenVacio.setVisible(true);
            lblResumenVacio.setManaged(true);
            return;
        }

        for (RespuestaAsignacionTurnoDTO asignacion : asignacionViewModel.getAsignacionesDia()) {
            listaAsignacionesDia.getChildren().add(crearTarjetaAsignacion(asignacion));
        }

        int total = asignacionViewModel.getAsignacionesDia().size();
        String plantilla = LanguageManager.getInstance().getString("asignaciones.resumen.total");
        lblTotalDia.setText(plantilla.replace("{0}", String.valueOf(total)));
        lblTotalDia.setVisible(true);
        lblTotalDia.setManaged(true);
    }

    /**
     * Crea una tarjeta visual para una asignación en el panel de resumen del día.
     *
     * @param asignacion Datos de la asignación a representar.
     * @return VBox con la información y botones de acción de la asignación.
     */
    private VBox crearTarjetaAsignacion(RespuestaAsignacionTurnoDTO asignacion) {
        LanguageManager lang = LanguageManager.getInstance();

        Label lblNombre = new Label(asignacion.nombreCompletoEmpleado());
        lblNombre.getStyleClass().add("asignacion-tarjeta-nombre");

        Label lblTurnoInfo = new Label(asignacion.descripcionTurno()
                + " · " + formatearHora(asignacion.horaInicio())
                + " - " + formatearHora(asignacion.horaFin()));
        lblTurnoInfo.getStyleClass().add("asignacion-tarjeta-turno");

        Label lblModalidadBadge = new Label(asignacion.modalidad());
        lblModalidadBadge.getStyleClass().addAll("badge",
                esTeletrabajo(asignacion.modalidad())
                        ? "badge-teletrabajo"
                        : "badge-presencial");

        Button btnEditar = new Button(lang.getString("asignaciones.btn.editar"));
        btnEditar.getStyleClass().addAll("btn-tabla", "btn-tabla-editar");
        btnEditar.setDisable(!asignacionViewModel.sedeConfiguradaProperty().get());
        btnEditar.setOnAction(e -> handleEditarAsignacion(asignacion));

        Button btnEliminar = new Button(lang.getString("asignaciones.btn.eliminar"));
        btnEliminar.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");
        btnEliminar.setDisable(!asignacionViewModel.sedeConfiguradaProperty().get());
        btnEliminar.setOnAction(e -> handleEliminarAsignacion(asignacion));

        HBox acciones = new HBox(6, btnEditar, btnEliminar);
        acciones.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        HBox fila = new HBox(8, lblModalidadBadge, acciones);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox tarjeta = new VBox(4, lblNombre, lblTurnoInfo, fila);
        tarjeta.getStyleClass().add("asignacion-tarjeta");

        Separator separador = new Separator();

        VBox contenedor = new VBox(8, tarjeta, separador);
        return contenedor;
    }

    /**
     * Gestiona el botón Asignar. Valida los campos del formulario y la fecha
     * seleccionada antes de llamar al ViewModel.
     */
    @FXML
    private void handleAsignar() {
        ocultarErrorFormulario();

        LocalDate diaSeleccionado = asignacionViewModel.diaSeleccionadoProperty().get();

        if (diaSeleccionado == null) {
            mostrarErrorFormulario(LanguageManager.getInstance()
                    .getString("asignaciones.error.sinDia"));
            return;
        }

        if (diaSeleccionado.isBefore(LocalDate.now())) {
            mostrarErrorFormulario(LanguageManager.getInstance()
                    .getString("asignaciones.error.fechaPasada"));
            return;
        }

        if (asignacionViewModel.empleadoSeleccionadoProperty().get() == null
                || asignacionViewModel.turnoSeleccionadoProperty().get() == null
                || asignacionViewModel.modalidadSeleccionadaProperty().get() == null) {
            mostrarErrorFormulario(LanguageManager.getInstance()
                    .getString("asignaciones.error.camposRequeridos"));
            return;
        }

        btnAsignar.setDisable(true);

        asignacionViewModel.crearAsignacion(diaSeleccionado)
                .thenAccept(nueva -> Platform.runLater(() -> btnAsignar.setDisable(false)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnAsignar.setDisable(false);
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarAlertaError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Abre el modal de edición de una asignación existente.
     *
     * @param asignacion Asignación a editar.
     */
    private void handleEditarAsignacion(RespuestaAsignacionTurnoDTO asignacion) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/asignacion-editar-modal.fxml"));
            Parent root = loader.load();
            AsignacionEditarModalController controller = loader.getController();
            controller.inicializar(asignacion, asignacionViewModel);
            controller.setOnGuardadoExitoso(this::actualizarResumenDia);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaTurnos.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("TurnosController: Error al abrir el modal de edición: " + e.getMessage());
        }
    }

    /**
     * Pide confirmación y elimina la asignación indicada.
     *
     * @param asignacion Asignación a eliminar.
     */
    private void handleEliminarAsignacion(RespuestaAsignacionTurnoDTO asignacion) {
        LanguageManager lang = LanguageManager.getInstance();

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(lang.getString("dialog.confirm.title"));
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(lang.getString("asignaciones.eliminar.confirmacion"));

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) return;

        asignacionViewModel.eliminarAsignacion(asignacion.idAsignacion())
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarAlertaError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Navega a la vista de Configuración cuando el usuario pulsa el enlace del banner.
     */
    @FXML
    private void handleIrAConfiguracion() {
        NavigationManager.getInstance().navegar("/fxml/configuracion-view.fxml");
    }

    /**
     * Abre el modal de alta de turno con todos los campos vacíos.
     */
    @FXML
    private void handleNuevoTurno() {
        abrirModalTurno(ModoFormulario.ALTA, null);
    }

    /**
     * Muestra un diálogo de confirmación y elimina el turno si el usuario confirma.
     *
     * @param turno Turno a eliminar.
     */
    private void handleEliminarTurno(RespuestaTurnoDTO turno) {
        LanguageManager lang = LanguageManager.getInstance();

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle(lang.getString("dialog.confirm.title"));
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(lang.getString("turnos.eliminar.confirmacion"));

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) return;

        turnoViewModel.eliminarTurno(turno.idTurno())
                .thenRun(() -> Platform.runLater(turnoViewModel::cargarTurnos))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarAlertaError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Abre el modal de alta o edición de turno del catálogo.
     *
     * @param modo  Modo de operación del formulario.
     * @param turno Datos del turno a editar, o null en modo Alta.
     */
    private void abrirModalTurno(ModoFormulario modo, RespuestaTurnoDTO turno) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/turno-form-modal.fxml"));
            Parent root = loader.load();
            TurnoFormController controller = loader.getController();
            controller.inicializar(modo, turno);
            controller.setOnGuardadoExitoso(turnoViewModel::cargarTurnos);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaTurnos.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("TurnosController: Error al abrir el modal de turno: " + e.getMessage());
        }
    }

    /**
     * Indica si una modalidad corresponde a teletrabajo.
     *
     * @param modalidad Cadena con el valor de la modalidad.
     * @return true si la modalidad es teletrabajo.
     */
    private boolean esTeletrabajo(String modalidad) {
        return modalidad != null && modalidad.equalsIgnoreCase("TELETRABAJO");
    }

    /**
     * Convierte una hora en formato "HH:mm:ss" a "HH:mm" para mostrar en la UI.
     *
     * @param hora Hora en formato "HH:mm:ss" o "HH:mm".
     * @return Hora en formato "HH:mm", o "—" si el valor es nulo.
     */
    private String formatearHora(String hora) {
        if (hora == null || hora.isBlank()) return "—";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    /**
     * Rellena el panel de tips con 4 tips en orden aleatorio,
     * ocupando el espacio restante bajo el calendario.
     */
    private void mostrarTips() {
        if (panelTips == null) return;
        panelTips.getChildren().clear();

        LanguageManager lang = LanguageManager.getInstance();
        int total = 6;

        java.util.List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 1; i <= total; i++) indices.add(i);
        java.util.Collections.shuffle(indices);

        String[] iconos = {
                "mdi2l-lightbulb-outline",
                "mdi2c-calendar-check",
                "mdi2a-account-group",
                "mdi2r-refresh",
                "mdi2c-chart-line",
                "mdi2s-star-outline"
        };

        for (int i = 0; i < 4; i++) {
            int indice = indices.get(i);
            String texto = lang.getString("tip." + indice);
            String icono = iconos[i % iconos.length];

            org.kordamp.ikonli.javafx.FontIcon fontIcono =
                    new org.kordamp.ikonli.javafx.FontIcon(icono);
            fontIcono.setIconSize(16);
            fontIcono.getStyleClass().add("tip-icono");

            javafx.scene.control.Label lblTexto = new javafx.scene.control.Label(texto);
            lblTexto.getStyleClass().add("tip-texto");
            lblTexto.setWrapText(true);
            lblTexto.setMaxWidth(Double.MAX_VALUE);

            javafx.scene.layout.HBox fila = new javafx.scene.layout.HBox(10, fontIcono, lblTexto);
            fila.setAlignment(javafx.geometry.Pos.TOP_LEFT);
            javafx.scene.layout.HBox.setHgrow(lblTexto, javafx.scene.layout.Priority.ALWAYS);

            javafx.scene.layout.VBox tarjeta = new javafx.scene.layout.VBox(fila);
            tarjeta.getStyleClass().add("tip-tarjeta");
            javafx.scene.layout.VBox.setVgrow(tarjeta, javafx.scene.layout.Priority.ALWAYS);

            panelTips.getChildren().add(tarjeta);
        }
    }

    /**
     * Muestra el label de error del formulario de asignación.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarErrorFormulario(String mensaje) {
        lblErrorFormulario.setText(mensaje);
        lblErrorFormulario.setVisible(true);
        lblErrorFormulario.setManaged(true);
    }

    /**
     * Oculta el label de error del formulario de asignación.
     */
    private void ocultarErrorFormulario() {
        lblErrorFormulario.setVisible(false);
        lblErrorFormulario.setManaged(false);
    }

    /**
     * Muestra un Alert de error con el mensaje proporcionado.
     *
     * @param mensaje Mensaje de error a mostrar al usuario.
     */
    private void mostrarAlertaError(String mensaje) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(LanguageManager.getInstance().getString("dialog.error.title"));
        error.setHeaderText(null);
        error.setContentText(mensaje);
        error.showAndWait();
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        tabAsignaciones.setText(lang.getString("turnos.pestana.asignaciones"));
        tabCatalogo.setText(lang.getString("turnos.pestana.catalogo"));

        lblBannerSede.setText(lang.getString("asignaciones.banner.sedeNoConfigurada"));
        linkConfigurar.setText(lang.getString("asignaciones.banner.enlace.configuracion"));

        lblFormTitulo.setText(lang.getString("asignaciones.titulo"));
        lblEmpleado.setText(lang.getString("asignaciones.form.empleado"));
        lblTurno.setText(lang.getString("asignaciones.form.turno"));
        lblModalidad.setText(lang.getString("asignaciones.form.modalidad"));
        btnAsignar.setText(lang.getString("asignaciones.btn.asignar"));

        LocalDate diaActual = asignacionViewModel.diaSeleccionadoProperty().get();
        if (diaActual == null) {
            lblResumenTitulo.setText(lang.getString("asignaciones.titulo"));
            lblResumenVacio.setText(lang.getString("asignaciones.resumen.vacio"));
        } else {
            actualizarTituloResumen(diaActual);
            actualizarResumenDia();
        }

        campoBusqueda.setPromptText(lang.getString("turnos.buscar.placeholder"));
        btnNuevoTurno.setText(lang.getString("turnos.btn.nuevo"));

        colDescripcion.setText(lang.getString("turnos.col.descripcion"));
        colHoraInicio.setText(lang.getString("turnos.col.horaInicio"));
        colHoraFin.setText(lang.getString("turnos.col.horaFin"));
        colAcciones.setText(lang.getString("turnos.col.acciones"));

        if (labelTablaVacia != null) {
            labelTablaVacia.setText(lang.getString("turnos.tabla.vacia"));
        }

        comboEmpleado.setPromptText(lang.getString("asignaciones.form.empleado.placeholder"));
        comboTurno.setPromptText(lang.getString("asignaciones.form.turno.placeholder"));
        comboModalidad.setPromptText(lang.getString("asignaciones.form.modalidad.placeholder"));
        comboModalidad.setConverter(comboModalidad.getConverter());
        tablaTurnos.refresh();
        mostrarTips();
    }
}
