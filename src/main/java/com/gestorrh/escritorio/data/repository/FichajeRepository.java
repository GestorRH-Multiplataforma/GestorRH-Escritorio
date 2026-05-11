package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.FichajeService;

/**
 * Repositorio encargado de la gestión de datos de Fichajes.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 * Los métodos se implementarán conforme se desarrollen los endpoints
 * de la Épica correspondiente.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class FichajeRepository extends BaseRepository {

    private final FichajeService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public FichajeRepository(FichajeService service) {
        this.service = service;
    }

    // Los métodos para obtener/enviar datos se implementarán aquí
    // usando executeAsync() y executeAsyncVoid() de BaseRepository.
}
