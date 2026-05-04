package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.EmpleadoService;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Empleados.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class EmpleadoRepository extends BaseRepository {

    private final EmpleadoService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public EmpleadoRepository(EmpleadoService service) {
        this.service = service;
    }

    /**
     * Obtiene el listado completo de empleados de la empresa autenticada
     * de forma asíncrona para no bloquear el hilo de UI.
     *
     * @return CompletableFuture con la lista de empleados o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaEmpleadoDTO>> getEmpleados() {
        return executeAsync(service.listarEmpleados());
    }
}
