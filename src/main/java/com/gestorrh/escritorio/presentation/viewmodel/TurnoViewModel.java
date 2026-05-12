package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.turno.PeticionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaTurnoDTO;
import com.gestorrh.escritorio.data.repository.TurnoRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.concurrent.CompletableFuture;

/**
 * ViewModel encargado de gestionar el estado y la lógica del catálogo de turnos.
 * Expone listas filtradas y propiedades reactivas para el binding con la vista.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class TurnoViewModel {

    private final TurnoRepository turnoRepository;

    private final ObservableList<RespuestaTurnoDTO> turnos =
            FXCollections.observableArrayList();

    private final FilteredList<RespuestaTurnoDTO> turnosFiltrados =
            new FilteredList<>(turnos);

    private final StringProperty filtroTexto    = new SimpleStringProperty("");
    private final BooleanProperty cargando      = new SimpleBooleanProperty(false);
    private final StringProperty mensajeError   = new SimpleStringProperty("");
    private final BooleanProperty errorVisible  = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección de dependencias.
     *
     * @param turnoRepository Repositorio de datos de turnos.
     */
    public TurnoViewModel(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
        configurarFiltro();
    }

    /**
     * Configura el listener reactivo sobre filtroTexto para que la lista
     * filtrada se actualice automáticamente ante cualquier cambio.
     */
    private void configurarFiltro() {
        filtroTexto.addListener((obs, oldVal, newVal) -> aplicarFiltro());
    }

    /**
     * Aplica el predicado de filtrado por descripción sobre la FilteredList.
     */
    private void aplicarFiltro() {
        String texto = filtroTexto.get() == null ? "" : filtroTexto.get().toLowerCase().trim();
        turnosFiltrados.setPredicate(turno ->
                texto.isEmpty() || turno.descripcion().toLowerCase().contains(texto)
        );
    }

    /**
     * Carga el listado completo de turnos desde la API de forma asíncrona.
     */
    public void cargarTurnos() {
        cargando.set(true);
        errorVisible.set(false);

        turnoRepository.getTurnos()
                .thenAccept(lista -> Platform.runLater(() -> {
                    turnos.setAll(lista);
                    aplicarFiltro();
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
     * Crea un nuevo turno de forma asíncrona.
     *
     * @param dto DTO con los datos del nuevo turno.
     * @return CompletableFuture con el turno creado.
     */
    public CompletableFuture<RespuestaTurnoDTO> crearTurno(PeticionTurnoDTO dto) {
        return turnoRepository.crearTurno(dto);
    }

    /**
     * Actualiza un turno existente de forma asíncrona.
     *
     * @param id  Identificador único del turno.
     * @param dto DTO con los nuevos datos del turno.
     * @return CompletableFuture con el turno actualizado.
     */
    public CompletableFuture<RespuestaTurnoDTO> actualizarTurno(Long id, PeticionTurnoDTO dto) {
        return turnoRepository.actualizarTurno(id, dto);
    }

    /**
     * Elimina un turno de forma asíncrona.
     * Si tiene asignaciones activas, el CompletableFuture falla con ApiException.
     *
     * @param id Identificador único del turno a eliminar.
     * @return CompletableFuture que se completa con null tras la eliminación exitosa.
     */
    public CompletableFuture<Void> eliminarTurno(Long id) {
        return turnoRepository.eliminarTurno(id);
    }

    /**
     * Devuelve la lista de turnos con el filtro de búsqueda aplicado.
     *
     * @return FilteredList de turnos filtrada por descripción.
     */
    public FilteredList<RespuestaTurnoDTO> getTurnosFiltrados() {
        return turnosFiltrados;
    }

    /** @return Property del texto de búsqueda. */
    public StringProperty filtroTextoProperty() { return filtroTexto; }

    /** @return Property del estado de carga. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
