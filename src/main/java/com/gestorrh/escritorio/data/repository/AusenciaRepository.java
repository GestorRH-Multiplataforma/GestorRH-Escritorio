package com.gestorrh.escritorio.data.repository;


import com.gestorrh.escritorio.data.network.AusenciaService;

/**
 * Repositorio encargado de la gestión de datos de Ausencias.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AusenciaRepository {
    private final AusenciaService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public AusenciaRepository(AusenciaService service) {
        this.service = service;
    }

    // Los métodos para obtener/enviar datos se implementarán aquí.
}
