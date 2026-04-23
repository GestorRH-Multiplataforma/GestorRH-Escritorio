package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.EmpresaService;

/**
 * Repositorio encargado de la gestión de datos de Empresas.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpresaRepository {
    private final EmpresaService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public EmpresaRepository(EmpresaService service) {
        this.service = service;
    }

    // Los métodos para obtener/enviar datos se implementarán aquí.
}
