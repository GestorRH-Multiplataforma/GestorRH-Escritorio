package com.gestorrh.escritorio.data.network;

import retrofit2.Call;
import retrofit2.http.GET;

import java.util.Map;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Estadisticas.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
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
}
