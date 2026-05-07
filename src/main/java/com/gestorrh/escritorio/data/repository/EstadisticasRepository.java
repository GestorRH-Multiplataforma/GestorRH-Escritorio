package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.EstadisticasService;
import com.gestorrh.escritorio.data.network.dto.DatoGraficoDTO;
import com.gestorrh.escritorio.data.network.dto.KpisDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Estadísticas.
 * Actúa como mediador entre la capa de red y la lógica de negocio,
 * construyendo DTOs tipados a partir de las respuestas de la API.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class EstadisticasRepository extends BaseRepository {

    private final EstadisticasService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad Estadisticas.
     */
    public EstadisticasRepository(EstadisticasService service) {
        this.service = service;
    }

    /**
     * Obtiene los KPIs del Dashboard de forma asíncrona y los mapea
     * a un {@link KpisDTO} tipado a partir del mapa devuelto por la API.
     *
     * @return CompletableFuture con el {@link KpisDTO} construido, o ApiException si falla.
     */
    public CompletableFuture<KpisDTO> getKpis() {
        return executeAsync(service.getKpis())
                .thenApply(this::mapearKpis);
    }

    /**
     * Obtiene el ranking de empleados con más retrasos de forma asíncrona.
     *
     * @return CompletableFuture con la lista de datos del ranking, o ApiException si falla.
     */
    public CompletableFuture<List<DatoGraficoDTO>> getTopRetrasos() {
        return executeAsync(service.getTopRetrasos());
    }

    /**
     * Convierte el mapa de métricas devuelto por la API en un {@link KpisDTO} tipado.
     * Si alguna clave no está presente en el mapa, su valor se inicializa a cero.
     *
     * @param mapa Mapa con las claves y valores numéricos devuelto por la API.
     * @return {@link KpisDTO} con los valores mapeados.
     */
    private KpisDTO mapearKpis(Map<String, Long> mapa) {
        return new KpisDTO(
                mapa.getOrDefault("totalEmpleados", 0L),
                mapa.getOrDefault("planificadosHoy", 0L),
                mapa.getOrDefault("ausentesHoy", 0L)
        );
    }
}
