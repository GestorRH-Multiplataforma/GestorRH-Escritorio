package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.EmpleadoService;

/**
 * Repositorio encargado de la gestión de datos de Empleados.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadoRepository {

    private final EmpleadoService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public EmpleadoRepository(EmpleadoService service) {
        this.service = service;
    }

    // Los métodos para obtener/enviar datos se implementarán aquí.
}
