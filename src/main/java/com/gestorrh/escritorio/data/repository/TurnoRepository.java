package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.TurnoService;
import com.gestorrh.escritorio.data.network.dto.PeticionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaTurnoDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Turnos.
 * Actúa como mediador entre la capa de red y la lógica de negocio,
 * exponiendo operaciones asíncronas mediante CompletableFuture.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class TurnoRepository extends BaseRepository {

    private final TurnoService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad Turno.
     */
    public TurnoRepository(TurnoService service) {
        this.service = service;
    }

    /**
     * Obtiene el listado completo de turnos de la empresa autenticada
     * de forma asíncrona para no bloquear el hilo de UI.
     *
     * @return CompletableFuture con la lista de turnos o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaTurnoDTO>> getTurnos() {
        return executeAsync(service.listarTurnos());
    }

    /**
     * Crea un nuevo turno de forma asíncrona.
     *
     * @param dto DTO con los datos del nuevo turno.
     * @return CompletableFuture con los datos del turno creado.
     */
    public CompletableFuture<RespuestaTurnoDTO> crearTurno(PeticionTurnoDTO dto) {
        return executeAsync(service.crearTurno(dto));
    }

    /**
     * Actualiza un turno existente de forma asíncrona.
     *
     * @param id  Identificador único del turno a actualizar.
     * @param dto DTO con los nuevos datos del turno.
     * @return CompletableFuture con los datos actualizados del turno.
     */
    public CompletableFuture<RespuestaTurnoDTO> actualizarTurno(Long id, PeticionTurnoDTO dto) {
        return executeAsync(service.actualizarTurno(id, dto));
    }

    /**
     * Elimina un turno de forma asíncrona.
     * Si el turno tiene asignaciones activas, la API responde con error 4xx
     * que se propaga como ApiException con el mensaje del servidor.
     *
     * @param id Identificador único del turno a eliminar.
     * @return CompletableFuture que se completa con null tras la eliminación exitosa.
     */
    public CompletableFuture<Void> eliminarTurno(Long id) {
        return executeAsyncVoid(service.eliminarTurno(id));
    }
}
