package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.DatoGraficoDTO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;
import java.util.Map;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Estadisticas.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public interface EstadisticasService {

    /**
     * Obtiene los KPIs generales del Dashboard para la empresa autenticada.
     * La API devuelve un mapa con las claves {@code totalEmpleados},
     * {@code planificadosHoy} y {@code ausentesHoy}.
     *
     * @return Llamada Retrofit con el mapa de métricas y sus valores numéricos.
     */
    @GET("api/estadisticas/kpis")
    Call<Map<String, Long>> getKpis();

    /**
     * Obtiene el ranking de los empleados con mayor número de retrasos acumulados
     * en sus fichajes de entrada. La API limita la respuesta a los 5 primeros.
     *
     * @return Llamada Retrofit con la lista de datos del ranking.
     */
    @GET("api/estadisticas/top-retrasos")
    Call<List<DatoGraficoDTO>> getTopRetrasos();

    /**
     * Obtiene las ausencias aprobadas agrupadas por tipo de ausencia.
     *
     * @return Llamada Retrofit con la lista de datos para el gráfico.
     */
    @GET("api/estadisticas/ausencias-tipo")
    Call<List<DatoGraficoDTO>> getAusenciasAprobadasPorTipo();

    /**
     * Obtiene el volumen de ausencias agrupadas por estado actual.
     *
     * @return Llamada Retrofit con la lista de datos para el gráfico.
     */
    @GET("api/estadisticas/ausencias-estado")
    Call<List<DatoGraficoDTO>> getAusenciasPorEstado();
}
