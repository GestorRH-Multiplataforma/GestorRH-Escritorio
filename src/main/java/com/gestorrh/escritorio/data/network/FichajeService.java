package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.RespuestaFichajeDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Fichaje.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface FichajeService {

    /**
     * Consulta el historial de fichajes de la empresa autenticada.
     * Cuando los parámetros opcionales se omiten (null), el servidor aplica
     * los valores por defecto: {@code fechaInicio} al día 1 del mes actual,
     * {@code fechaFin} a la fecha de hoy y devuelve fichajes de todos los empleados.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Opcional.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Opcional.
     * @param empleadoId  Identificador del empleado a filtrar. Opcional.
     * @return Lista de fichajes que cumplen los criterios indicados.
     */
    @GET("api/fichajes")
    Call<List<RespuestaFichajeDTO>> consultarFichajes(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin") String fechaFin,
            @Query("empleadoId") Long empleadoId
    );
}
