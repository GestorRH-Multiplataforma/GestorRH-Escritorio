package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.repository.EmpleadoRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.List;

/**
 * ViewModel encargado de gestionar el estado y la lógica del directorio de empleados.
 * Expone listas filtradas y propiedades reactivas para el binding con la vista.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class EmpleadoViewModel {

    /**
     * Enum que representa los posibles estados del filtro de empleados.
     */
    public enum FiltroEstado {
        SOLO_ACTIVOS,
        SOLO_INACTIVOS,
        TODOS
    }

    private final EmpleadoRepository empleadoRepository;

    private final ObservableList<RespuestaEmpleadoDTO> empleados =
            FXCollections.observableArrayList();

    private final FilteredList<RespuestaEmpleadoDTO> empleadosFiltrados =
            new FilteredList<>(empleados);

    private final StringProperty filtroTexto = new SimpleStringProperty("");
    private final ObjectProperty<FiltroEstado> filtroEstado =
            new SimpleObjectProperty<>(FiltroEstado.SOLO_ACTIVOS);
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty mostrarColumnaEstado = new SimpleBooleanProperty(false);
    private final StringProperty mensajeError = new SimpleStringProperty("");
    private final BooleanProperty errorVisible = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección de dependencias.
     *
     * @param empleadoRepository Repositorio de datos de empleados.
     */
    public EmpleadoViewModel(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
        configurarFiltros();
    }

    /**
     * Configura los listeners reactivos sobre filtroTexto y filtroEstado
     * para que la lista filtrada se actualice automáticamente ante cualquier cambio.
     */
    private void configurarFiltros() {
        filtroTexto.addListener((obs, oldVal, newVal) -> aplicarFiltro());
        filtroEstado.addListener((obs, oldVal, newVal) -> {
            aplicarFiltro();
            mostrarColumnaEstado.set(newVal != FiltroEstado.SOLO_ACTIVOS);
        });
    }

    /**
     * Aplica el predicado de filtrado combinando el texto de búsqueda
     * y el estado del filtro activo sobre la FilteredList.
     */
    private void aplicarFiltro() {
        String texto = filtroTexto.get() == null ? "" : filtroTexto.get().toLowerCase().trim();
        FiltroEstado estado = filtroEstado.get();

        empleadosFiltrados.setPredicate(emp -> {
            boolean coincideTexto = texto.isEmpty()
                    || emp.nombre().toLowerCase().contains(texto)
                    || emp.apellidos().toLowerCase().contains(texto)
                    || emp.email().toLowerCase().contains(texto)
                    || emp.departamento().toLowerCase().contains(texto);

            boolean coincideEstado = switch (estado) {
                case SOLO_ACTIVOS   -> emp.activo();
                case SOLO_INACTIVOS -> !emp.activo();
                case TODOS          -> true;
            };

            return coincideTexto && coincideEstado;
        });
    }

    /**
     * Carga el listado completo de empleados desde la API de forma asíncrona.
     * Actualiza el estado de carga y gestiona los errores producidos.
     */
    public void cargarEmpleados() {
        cargando.set(true);
        errorVisible.set(false);

        empleadoRepository.getEmpleados()
                .thenAccept(lista -> Platform.runLater(() -> {
                    empleados.setAll(lista);
                    cargando.set(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargando.set(false);
                        mensajeError.set(ex.getCause() != null
                                ? ex.getCause().getMessage()
                                : ex.getMessage());
                        errorVisible.set(true);
                    });
                    return null;
                });
    }

    /**
     * Devuelve la lista de empleados con los filtros aplicados, lista para
     * enlazarse directamente con el TableView de la vista.
     *
     * @return FilteredList de empleados filtrada por texto y estado.
     */
    public FilteredList<RespuestaEmpleadoDTO> getEmpleadosFiltrados() {
        return empleadosFiltrados;
    }

    /**
     * Devuelve el número total de empleados que cumplen el filtro activo.
     *
     * @return Número de empleados filtrados.
     */
    public int getTotalFiltrados() {
        return empleadosFiltrados.size();
    }

    /** @return Property del texto de búsqueda. */
    public StringProperty filtroTextoProperty() { return filtroTexto; }

    /** @return Property del filtro de estado activo. */
    public ObjectProperty<FiltroEstado> filtroEstadoProperty() { return filtroEstado; }

    /** @return Property del estado de carga. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property que indica si la columna Estado debe mostrarse. */
    public BooleanProperty mostrarColumnaEstadoProperty() { return mostrarColumnaEstado; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
