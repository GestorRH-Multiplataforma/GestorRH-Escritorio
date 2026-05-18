package com.gestorrh.escritorio.presentation.controller.empleado;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.Limpiable;
import com.gestorrh.escritorio.data.network.dto.empleado.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoFormViewModel.ModoFormulario;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoViewModel;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoViewModel.FiltroEstado;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controlador para la vista del directorio de empleados.
 * Gestiona la tabla paginada, los filtros, la barra de búsqueda
 * y la apertura del modal de alta y edición de empleados.
 * La paginación se calcula dinámicamente según la altura disponible
 * de la tabla para evitar scroll interno y filas cortadas.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class EmpleadosController implements Limpiable {

    private static final Logger LOGGER = Logger.getLogger(EmpleadosController.class.getName());

    /** Altura fija de cada fila, debe coincidir con fixedCellSize en el FXML. */
    private static final double ALTURA_CELDA = 52.0;

    /** Altura de la cabecera de columnas de la tabla. */
    private static final double ALTURA_CABECERA = 42.0;

    /** Mínimo de filas a mostrar aunque la pantalla sea muy pequeña. */
    private static final int FILAS_MINIMAS = 3;

    @FXML private TextField campoBusqueda;
    @FXML private Button btnNuevoEmpleado;
    @FXML private ChoiceBox<String> selectorFiltro;

    @FXML private TableView<RespuestaEmpleadoDTO> tablaEmpleados;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colNombre;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colEmail;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colDepartamento;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colPuesto;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colRol;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colFechaBaja;
    @FXML private TableColumn<RespuestaEmpleadoDTO, Void> colAcciones;

    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label labelPaginacion;

    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Label labelError;

    private final Runnable actualizadorTextos = this::actualizarTextos;

    private int paginaActual = 0;

    /** Número de filas por página — se recalcula dinámicamente. */
    private int filasPorPagina = 10;

    private FilteredList<RespuestaEmpleadoDTO> empleadosFiltrados;
    private EmpleadoViewModel viewModel;

    /**
     * Inicializa los bindings, configura la tabla y lanza la carga de datos.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createEmpleadoViewModel();
        configurarColumnas();
        configurarSelectorFiltro();
        configurarBindings();
        configurarPaginacion();
        configurarPaginacionDinamica();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.cargarEmpleados();
    }

    /**
     * Libera el listener de idioma al destruirse la vista para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Configura el listener sobre la altura de la tabla para recalcular
     * cuántas filas caben y actualizar la paginación dinámicamente.
     * Se dispara al redimensionar la ventana o al cambiar el layout.
     */
    private void configurarPaginacionDinamica() {
        tablaEmpleados.heightProperty().addListener((obs, oldH, newH) -> {
            int nuevasFilas = calcularFilasPorPagina(newH.doubleValue());
            if (nuevasFilas != filasPorPagina) {
                filasPorPagina = nuevasFilas;
                paginaActual = 0;
                actualizarPagina();
            }
        });
    }

    /**
     * Calcula cuántas filas enteras caben en la altura disponible de la tabla.
     *
     * @param alturaTabla Altura actual en píxeles del componente TableView.
     * @return Número de filas enteras que caben, mínimo {@value FILAS_MINIMAS}.
     */
    private int calcularFilasPorPagina(double alturaTabla) {
        double alturaDisponible = alturaTabla - ALTURA_CABECERA;
        int filas = (int) Math.floor(alturaDisponible / ALTURA_CELDA);
        return Math.max(filas + 1, FILAS_MINIMAS);
    }

    /**
     * Configura los CellValueFactory de cada columna de la tabla.
     */
    private void configurarColumnas() {
        tablaEmpleados.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().nombre() + " " + data.getValue().apellidos()
                ));
        colEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().email()));
        colDepartamento.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().departamento()));
        colPuesto.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().puesto()));

        configurarColumnaRol();
        configurarColumnaFechaBaja();
        configurarColumnaAcciones();
    }

    /**
     * Configura la columna Rol con badges visuales diferenciados.
     */
    private void configurarColumnaRol() {
        colRol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().rol()));
        colRol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setGraphic(null);
                    return;
                }
                LanguageManager lang = LanguageManager.getInstance();
                boolean esSupervisor = "SUPERVISOR".equalsIgnoreCase(rol);
                String texto = esSupervisor
                        ? lang.getString("empleados.rol.supervisor")
                        : lang.getString("empleados.rol.empleado");
                String estilo = esSupervisor ? "badge-supervisor" : "badge-empleado";
                Label badge = new Label(texto);
                badge.getStyleClass().addAll("badge", estilo);
                setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /**
     * Configura la columna Fecha de Baja con badges visuales según el estado:
     * - Guión (—) si el empleado no tiene baja registrada.
     * - Badge amarillo "Baja programada" si la fecha es futura.
     * - Badge gris "Inactivo" con la fecha si ya ha pasado.
     */
    private void configurarColumnaFechaBaja() {
        DateTimeFormatter formatterEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter formatterSalida  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colFechaBaja.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().fechaBajaContrato()));

        colFechaBaja.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String fechaStr, boolean empty) {
                super.updateItem(fechaStr, empty);
                setGraphic(null);
                setText(null);

                if (empty) return;

                if (fechaStr == null) {
                    setText("—");
                    setAlignment(javafx.geometry.Pos.CENTER);
                    return;
                }

                try {
                    LocalDate fecha       = LocalDate.parse(fechaStr, formatterEntrada);
                    String fechaLegible   = fecha.format(formatterSalida);
                    LanguageManager lang  = LanguageManager.getInstance();
                    boolean esFutura      = fecha.isAfter(LocalDate.now());
                    String estilo         = esFutura ? "badge-baja-programada" : "badge-inactivo";
                    String texto          = esFutura
                            ? lang.getString("empleados.badge.baja.programada") + " · " + fechaLegible
                            : lang.getString("empleados.badge.inactivo") + " · " + fechaLegible;

                    Label badge = new Label(texto);
                    badge.getStyleClass().addAll("badge", estilo);
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(badge);
                } catch (Exception e) {
                    setText("—");
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    /**
     * Configura la columna Acciones con los botones Editar, Baja y Readmitir.
     * - Empleado activo sin fecha de baja: botones Editar + Baja habilitado.
     * - Empleado activo con fecha de baja futura: botones Editar + Baja deshabilitado con Tooltip.
     * - Empleado inactivo: botones Editar + Readmitir.
     */
    private void configurarColumnaAcciones() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar    = new Button();
            private final Button btnBaja      = new Button();
            private final Button btnReadmitir = new Button();
            private final HBox contenedor     = new HBox(6, btnEditar, btnBaja, btnReadmitir) {{
                setAlignment(javafx.geometry.Pos.CENTER);
            }};

            {
                btnEditar.getStyleClass().addAll("btn-tabla", "btn-tabla-editar");
                btnBaja.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");
                btnReadmitir.getStyleClass().addAll("btn-tabla", "btn-tabla-readmitir");

                btnEditar.setOnAction(e -> {
                    RespuestaEmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    abrirModal(ModoFormulario.EDICION, empleado);
                });

                btnBaja.setOnAction(e -> {
                    RespuestaEmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    handleBaja(empleado);
                });

                btnReadmitir.setOnAction(e -> {
                    RespuestaEmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                    handleReadmitir(empleado);
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

                RespuestaEmpleadoDTO empleado = getTableView().getItems().get(getIndex());
                LanguageManager lang = LanguageManager.getInstance();

                btnEditar.setText(lang.getString("empleados.btn.editar"));
                btnBaja.setText(lang.getString("empleados.btn.baja"));
                btnReadmitir.setText(lang.getString("empleados.btn.readmitir"));

                String fechaBajaStr   = empleado.fechaBajaContrato();
                boolean tieneFechaBaja = fechaBajaStr != null;
                boolean estaActivo    = empleado.activo();

                if (estaActivo && !tieneFechaBaja) {
                    btnBaja.setDisable(false);
                    btnBaja.setTooltip(null);
                    btnBaja.setVisible(true);
                    btnBaja.setManaged(true);
                    btnReadmitir.setVisible(false);
                    btnReadmitir.setManaged(false);

                } else if (estaActivo && tieneFechaBaja) {
                    try {
                        LocalDate fecha = LocalDate.parse(fechaBajaStr,
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        String fechaLegible = fecha.format(formatter);
                        String tooltipMsg = lang.getString("empleados.baja.tooltip")
                                .replace("{0}", fechaLegible);
                        btnBaja.setTooltip(new Tooltip(tooltipMsg));
                    } catch (Exception ex) {
                        btnBaja.setTooltip(null);
                    }
                    btnBaja.setDisable(true);
                    btnBaja.setVisible(true);
                    btnBaja.setManaged(true);
                    btnReadmitir.setVisible(false);
                    btnReadmitir.setManaged(false);

                } else {
                    btnBaja.setVisible(false);
                    btnBaja.setManaged(false);
                    btnReadmitir.setVisible(true);
                    btnReadmitir.setManaged(true);
                }

                setGraphic(contenedor);
            }
        });
    }

    /**
     * Configura el ChoiceBox de filtro con sus opciones y lo sincroniza
     * con el filtroEstado del ViewModel.
     */
    private void configurarSelectorFiltro() {
        LanguageManager lang = LanguageManager.getInstance();
        selectorFiltro.getItems().addAll(
                lang.getString("empleados.filtro.activos"),
                lang.getString("empleados.filtro.todos"),
                lang.getString("empleados.filtro.inactivos")
        );
        selectorFiltro.getSelectionModel().selectFirst();

        selectorFiltro.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            FiltroEstado estado = switch (newVal.intValue()) {
                case 1  -> FiltroEstado.TODOS;
                case 2  -> FiltroEstado.SOLO_INACTIVOS;
                default -> FiltroEstado.SOLO_ACTIVOS;
            };
            viewModel.filtroEstadoProperty().set(estado);
            paginaActual = 0;
            actualizarPagina();
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

        empleadosFiltrados = viewModel.getEmpleadosFiltrados();
        empleadosFiltrados.predicateProperty().addListener((obs, oldVal, newVal) -> {
            paginaActual = 0;
            actualizarPagina();
        });

        viewModel.cargandoProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) actualizarPagina();
        });
    }

    /**
     * Configura los botones de paginación y el estado inicial.
     */
    private void configurarPaginacion() {
        btnAnterior.setOnAction(e -> {
            if (paginaActual > 0) {
                paginaActual--;
                actualizarPagina();
            }
        });
        btnSiguiente.setOnAction(e -> {
            if ((paginaActual + 1) * filasPorPagina < empleadosFiltrados.size()) {
                paginaActual++;
                actualizarPagina();
            }
        });
    }

    /**
     * Actualiza la tabla con los elementos de la página actual y refresca
     * el indicador de paginación y el estado de los botones.
     */
    private void actualizarPagina() {
        if (empleadosFiltrados == null) return;

        int total = empleadosFiltrados.size();
        int desde = paginaActual * filasPorPagina;
        int hasta = Math.min(desde + filasPorPagina, total);

        List<RespuestaEmpleadoDTO> pagina = desde < total
                ? empleadosFiltrados.subList(desde, hasta)
                : List.of();

        tablaEmpleados.getItems().setAll(pagina);
        tablaEmpleados.setFixedCellSize(ALTURA_CELDA);
        tablaEmpleados.prefHeightProperty().bind(
                javafx.beans.binding.Bindings.size(tablaEmpleados.getItems())
                        .multiply(ALTURA_CELDA)
                        .add(ALTURA_CABECERA + 2)
        );

        boolean hayVariasPaginas = total > filasPorPagina;
        btnAnterior.setVisible(hayVariasPaginas);
        btnAnterior.setManaged(hayVariasPaginas);
        btnSiguiente.setVisible(hayVariasPaginas);
        btnSiguiente.setManaged(hayVariasPaginas);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(hasta >= total);

        actualizarLabelPaginacion(desde + 1, hasta, total);
    }

    /**
     * Actualiza el texto del indicador de paginación.
     *
     * @param desde Índice del primer elemento mostrado (base 1).
     * @param hasta Índice del último elemento mostrado.
     * @param total Total de empleados filtrados.
     */
    private void actualizarLabelPaginacion(int desde, int hasta, int total) {
        if (total == 0) {
            labelPaginacion.setText("0 " + LanguageManager.getInstance().getString("empleados.paginacion.resultados"));
            return;
        }
        String plantilla = LanguageManager.getInstance().getString("empleados.paginacion.mostrando");
        labelPaginacion.setText(
                plantilla.replace("{0}", String.valueOf(desde))
                        .replace("{1}", String.valueOf(hasta))
                        .replace("{2}", String.valueOf(total))
        );
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        campoBusqueda.setPromptText(lang.getString("empleados.buscar.placeholder"));
        btnNuevoEmpleado.setText(lang.getString("empleados.btn.nuevo"));

        colNombre.setText(lang.getString("empleados.col.nombre"));
        colEmail.setText(lang.getString("empleados.col.email"));
        colDepartamento.setText(lang.getString("empleados.col.departamento"));
        colPuesto.setText(lang.getString("empleados.col.puesto"));
        colRol.setText(lang.getString("empleados.col.rol"));
        colFechaBaja.setText(lang.getString("empleados.col.fecha.baja"));
        colAcciones.setText(lang.getString("empleados.col.acciones"));

        int indiceActual = selectorFiltro.getSelectionModel().getSelectedIndex();
        selectorFiltro.getItems().setAll(
                lang.getString("empleados.filtro.activos"),
                lang.getString("empleados.filtro.todos"),
                lang.getString("empleados.filtro.inactivos")
        );
        selectorFiltro.getSelectionModel().select(indiceActual);

        btnAnterior.setText(lang.getString("empleados.btn.anterior"));
        btnSiguiente.setText(lang.getString("empleados.btn.siguiente"));

        actualizarPagina();
    }

    /**
     * Abre el modal en modo Alta con todos los campos vacíos.
     */
    @FXML
    private void handleNuevoEmpleado() {
        abrirModal(ModoFormulario.ALTA, null);
    }

    /**
     * Abre el modal de alta o edición de empleado.
     *
     * @param modo     Modo de operación del formulario.
     * @param empleado Datos del empleado a editar, o null en modo Alta.
     */
    private void abrirModal(ModoFormulario modo, RespuestaEmpleadoDTO empleado) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/empleado-form-modal.fxml")
            );
            Parent root = loader.load();
            EmpleadoFormController controller = loader.getController();
            controller.inicializar(modo, empleado);
            controller.setOnGuardadoExitoso(() -> viewModel.cargarEmpleados());

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaEmpleados.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("EmpleadosController: Error al abrir el modal de empleado: " + e.getMessage());
        }
    }

    /**
     * Abre el modal de confirmación de baja y ejecuta la operación si el usuario confirma.
     *
     * @param empleado Empleado a dar de baja.
     */
    private void handleBaja(RespuestaEmpleadoDTO empleado) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/baja-confirmacion-modal.fxml")
            );
            Parent root = loader.load();
            BajaConfirmacionModalController controller = loader.getController();
            controller.inicializar(
                    BajaConfirmacionModalController.Modo.BAJA,
                    empleado.nombre() + " " + empleado.apellidos(),
                    fecha -> viewModel.darDeBajaEmpleado(empleado.idEmpleado(), fecha)
                            .thenRun(() -> Platform.runLater(() -> {
                                viewModel.cargarEmpleados();
                                paginaActual = 0;
                            }))
                            .exceptionally(ex -> {
                                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                                Platform.runLater(() -> mostrarError(cause.getMessage()));
                                return null;
                            })
            );

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaEmpleados.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("EmpleadosController: Error al abrir modal de baja: " + e.getMessage());
        }
    }

    /**
     * Abre el modal de confirmación de readmisión y muestra la nueva contraseña generada.
     *
     * @param empleado Empleado a readmitir.
     */
    private void handleReadmitir(RespuestaEmpleadoDTO empleado) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/baja-confirmacion-modal.fxml")
            );
            Parent root = loader.load();
            BajaConfirmacionModalController controller = loader.getController();
            controller.inicializar(
                    BajaConfirmacionModalController.Modo.READMITIR,
                    empleado.nombre() + " " + empleado.apellidos(),
                    fecha -> viewModel.readmitirEmpleado(empleado.idEmpleado())
                            .thenAccept(respuesta -> Platform.runLater(() -> {
                                viewModel.cargarEmpleados();
                                paginaActual = 0;
                                mostrarModalPasswordGenerada(respuesta.passwordGenerada());
                            }))
                            .exceptionally(ex -> {
                                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                                Platform.runLater(() -> mostrarError(cause.getMessage()));
                                return null;
                            })
            );

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.initOwner(tablaEmpleados.getScene().getWindow());
            modal.setResizable(false);
            modal.setScene(new Scene(root));
            modal.getScene().getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );
            modal.setOnCloseRequest(e -> controller.limpiar());
            modal.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("EmpleadosController: Error al abrir modal de readmisión: " + e.getMessage());
        }
    }

    /**
     * Abre el modal que muestra la contraseña generada tras readmitir un empleado.
     *
     * @param password Contraseña generada por la API.
     */
    private void mostrarModalPasswordGenerada(String password) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/empleado-password-modal.fxml")
            );
            Parent root = loader.load();
            EmpleadoPasswordModalController controller = loader.getController();
            controller.setPasswordGenerada(password);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(tablaEmpleados.getScene().getWindow());
            stage.setResizable(false);
            stage.setTitle(LanguageManager.getInstance()
                    .getString("empleados.modal.password.titulo"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> controller.limpiar());
            stage.showAndWait();

        } catch (IOException e) {
            LOGGER.severe("EmpleadosController: Error al abrir modal de contraseña: " + e.getMessage());
        }
    }

    /**
     * Muestra un Alert de error genérico con el mensaje proporcionado.
     *
     * @param mensaje Mensaje de error a mostrar al usuario.
     */
    private void mostrarError(String mensaje) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle(LanguageManager.getInstance().getString("dialog.error.title"));
        error.setHeaderText(null);
        error.setContentText(mensaje);
        error.showAndWait();
    }
}
