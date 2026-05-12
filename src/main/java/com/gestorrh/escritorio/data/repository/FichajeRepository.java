package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.service.FichajeService;
import com.gestorrh.escritorio.data.network.dto.fichaje.RespuestaFichajeDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Fichajes.
 * Actúa como mediador entre la capa de red y la lógica de negocio,
 * exponiendo operaciones asíncronas mediante CompletableFuture.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class FichajeRepository extends BaseRepository {

    private final FichajeService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad Fichaje.
     */
    public FichajeRepository(FichajeService service) {
        this.service = service;
    }

    /**
     * Consulta el historial de fichajes de forma asíncrona aplicando los filtros indicados.
     * Cuando los tres parámetros son {@code null}, el servidor devuelve los fichajes
     * del mes actual para todos los empleados.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Opcional.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Opcional.
     * @param empleadoId  Identificador del empleado a filtrar. Opcional.
     * @return CompletableFuture con la lista de fichajes o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaFichajeDTO>> consultarFichajes(
            String fechaInicio,
            String fechaFin,
            Long empleadoId) {
        return executeAsync(service.consultarFichajes(fechaInicio, fechaFin, empleadoId));
    }
}
