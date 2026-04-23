package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.FichajeService;

/**
 * Repositorio encargado de la gestión de datos de Fichajes.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class FichajeRepository {
    private final FichajeService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public FichajeRepository(FichajeService service) {
        this.service = service;
    }

    // Los métodos para obtener/enviar datos se implementarán aquí.
}
