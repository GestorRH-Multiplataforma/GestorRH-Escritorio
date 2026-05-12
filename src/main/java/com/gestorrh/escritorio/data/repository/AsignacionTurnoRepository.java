package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.service.AsignacionTurnoService;
import com.gestorrh.escritorio.data.network.dto.turno.PeticionAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaAsignacionTurnoDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Asignaciones de Turnos.
 * Actúa como mediador entre la capa de red y la lógica de negocio,
 * exponiendo operaciones asíncronas mediante CompletableFuture.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class AsignacionTurnoRepository extends BaseRepository {

    private final AsignacionTurnoService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad AsignacionTurno.
     */
    public AsignacionTurnoRepository(AsignacionTurnoService service) {
        this.service = service;
    }

    /**
     * Obtiene todas las asignaciones de la empresa autenticada de forma asíncrona.
     * El filtrado por mes se realiza en el cliente.
     *
     * @return CompletableFuture con la lista de asignaciones o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaAsignacionTurnoDTO>> getAsignaciones() {
        return executeAsync(service.listarAsignaciones());
    }

    /**
     * Obtiene las modalidades de trabajo disponibles de forma asíncrona.
     *
     * @return CompletableFuture con la lista de modalidades o ApiException si falla.
     */
    public CompletableFuture<List<String>> getModalidades() {
        return executeAsync(service.listarModalidades());
    }

    /**
     * Crea una nueva asignación de turno de forma asíncrona.
     *
     * @param dto DTO con los datos de la nueva asignación.
     * @return CompletableFuture con los datos de la asignación creada.
     */
    public CompletableFuture<RespuestaAsignacionTurnoDTO> crearAsignacion(PeticionAsignacionTurnoDTO dto) {
        return executeAsync(service.crearAsignacion(dto));
    }

    /**
     * Edita una asignación de turno existente de forma asíncrona.
     *
     * @param id  Identificador único de la asignación a editar.
     * @param dto DTO con los nuevos datos de la asignación.
     * @return CompletableFuture con los datos actualizados de la asignación.
     */
    public CompletableFuture<RespuestaAsignacionTurnoDTO> editarAsignacion(Long id, PeticionAsignacionTurnoDTO dto) {
        return executeAsync(service.editarAsignacion(id, dto));
    }

    /**
     * Elimina una asignación de turno de forma asíncrona.
     *
     * @param id Identificador único de la asignación a eliminar.
     * @return CompletableFuture que se completa con null tras la eliminación exitosa.
     */
    public CompletableFuture<Void> eliminarAsignacion(Long id) {
        return executeAsyncVoid(service.eliminarAsignacion(id));
    }
}
