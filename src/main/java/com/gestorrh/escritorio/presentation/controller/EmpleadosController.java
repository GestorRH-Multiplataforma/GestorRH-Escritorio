package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoViewModel;
import com.gestorrh.escritorio.presentation.viewmodel.EmpleadoViewModel.FiltroEstado;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * Controlador para la vista del directorio de empleados.
 * Gestiona la tabla paginada, los filtros y la barra de búsqueda.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadosController {

    private static final int FILAS_POR_PAGINA = 25;

    @FXML private TextField campoBusqueda;
    @FXML private Button btnNuevoEmpleado;
    @FXML private ChoiceBox<String> selectorFiltro;

    @FXML private TableView<RespuestaEmpleadoDTO> tablaEmpleados;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colNombre;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colEmail;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colDepartamento;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colPuesto;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colRol;
    @FXML private TableColumn<RespuestaEmpleadoDTO, String> colEstado;
    @FXML private TableColumn<RespuestaEmpleadoDTO, Void> colAcciones;

    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label labelPaginacion;

    @FXML private ProgressIndicator indicadorCarga;
    @FXML private Label labelError;

    private final EmpleadoViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    private int paginaActual = 0;
    private FilteredList<RespuestaEmpleadoDTO> empleadosFiltrados;

    /**
     * Constructor del controlador. Obtiene el ViewModel desde la fábrica.
     */
    public EmpleadosController() {
        this.viewModel = ViewModelFactory.getInstance().createEmpleadoViewModel();
    }

    /**
     * Inicializa los bindings, configura la tabla y lanza la carga de datos.
     */
    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSelectorFiltro();
        configurarBindings();
        configurarPaginacion();

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

    // Configuración inicial

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
        configurarColumnaEstado();
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
     * Configura la columna Estado con badges de color según actividad.
     * La visibilidad de esta columna se gestiona mediante binding reactivo.
     */
    private void configurarColumnaEstado() {
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(String.valueOf(data.getValue().activo())));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setGraphic(null);
                    return;
                }
                LanguageManager lang = LanguageManager.getInstance();
                boolean estaActivo = "true".equals(activo);
                String texto = estaActivo
                        ? lang.getString("empleados.estado.activo")
                        : lang.getString("empleados.estado.inactivo");
                String estilo = estaActivo ? "badge-activo" : "badge-inactivo";
                Label badge = new Label(texto);
                badge.getStyleClass().addAll("badge", estilo);
                setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(badge);
                setText(null);
            }
        });
        colEstado.visibleProperty().bind(viewModel.mostrarColumnaEstadoProperty());
    }

    /**
     * Configura la columna Acciones con los botones Editar y Baja.
     * Los botones se renderizan pero aún no tienen funcionalidad (issues posteriores).
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button();
            private final Button btnBaja   = new Button();
            private final HBox contenedor  = new HBox(6, btnEditar, btnBaja) {{
                setAlignment(javafx.geometry.Pos.CENTER);
            }};

            {
                btnEditar.getStyleClass().addAll("btn-tabla", "btn-tabla-editar");
                btnBaja.getStyleClass().addAll("btn-tabla", "btn-tabla-baja");
                actualizarTextosBotones();
            }

            private void actualizarTextosBotones() {
                LanguageManager lang = LanguageManager.getInstance();
                btnEditar.setText(lang.getString("empleados.btn.editar"));
                btnBaja.setText(lang.getString("empleados.btn.baja"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                actualizarTextosBotones();
                setAlignment(javafx.geometry.Pos.CENTER);
                setGraphic(empty ? null : contenedor);
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

    // Paginación

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
            if ((paginaActual + 1) * FILAS_POR_PAGINA < empleadosFiltrados.size()) {
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

        int total      = empleadosFiltrados.size();
        int desde      = paginaActual * FILAS_POR_PAGINA;
        int hasta      = Math.min(desde + FILAS_POR_PAGINA, total);

        List<RespuestaEmpleadoDTO> pagina = desde < total
                ? empleadosFiltrados.subList(desde, hasta)
                : List.of();

        tablaEmpleados.getItems().setAll(pagina);

        boolean hayVariasPaginas = total > FILAS_POR_PAGINA;
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
        colEstado.setText(lang.getString("empleados.col.estado"));
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

    // Handlers FXML

    /**
     * Placeholder para el botón "Nuevo Empleado".
     * La funcionalidad se implementará en la issue de alta de empleado.
     */
    @FXML
    private void handleNuevoEmpleado() {
        // TODO: Implementar en issue de alta de empleado
    }
}
