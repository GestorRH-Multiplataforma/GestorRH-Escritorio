package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.reporte.RespuestaReporteDetalleDTO;
import com.gestorrh.escritorio.data.network.dto.reporte.RespuestaReporteResumenDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

import java.util.List;

/**
 * Interfaz de Retrofit para los endpoints de generación de informes de control horario.
 * Expone tanto los endpoints de previsualización JSON como los de descarga de PDF.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public interface ReporteService {

    /**
     * Obtiene el reporte detallado de fichajes en formato JSON para previsualización.
     * Cada elemento de la lista corresponde a un fichaje completo de un empleado.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param idEmpleado  Identificador del empleado a filtrar. Opcional, null devuelve todos.
     * @return Llamada Retrofit con la lista de registros detallados.
     */
    @GET("api/reportes/detalle")
    Call<List<RespuestaReporteDetalleDTO>> obtenerDetalle(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin")    String fechaFin,
            @Query("idEmpleado")  Long idEmpleado);

    /**
     * Obtiene el reporte resumido por empleado en formato JSON para previsualización.
     * Cada elemento agrupa el total de horas de un empleado en el periodo indicado.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param idEmpleado  Identificador del empleado a filtrar. Opcional, null devuelve todos.
     * @return Llamada Retrofit con la lista de registros resumidos.
     */
    @GET("api/reportes/resumen")
    Call<List<RespuestaReporteResumenDTO>> obtenerResumen(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin")    String fechaFin,
            @Query("idEmpleado")  Long idEmpleado);

    /**
     * Descarga el informe detallado de fichajes en formato PDF.
     * Usa {@code @Streaming} para no cargar el documento completo en memoria.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param idEmpleado  Identificador del empleado a filtrar. Opcional, null genera para todos.
     * @return Llamada Retrofit con los bytes del PDF listos para escribir a disco.
     */
    @Streaming
    @GET("api/reportes/detalle/pdf")
    Call<ResponseBody> descargarPdfDetalle(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin")    String fechaFin,
            @Query("idEmpleado")  Long idEmpleado);

    /**
     * Descarga el informe resumido por empleado en formato PDF.
     * Usa {@code @Streaming} para no cargar el documento completo en memoria.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd). Requerido.
     * @param idEmpleado  Identificador del empleado a filtrar. Opcional, null genera para todos.
     * @return Llamada Retrofit con los bytes del PDF listos para escribir a disco.
     */
    @Streaming
    @GET("api/reportes/resumen/pdf")
    Call<ResponseBody> descargarPdfResumen(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin")    String fechaFin,
            @Query("idEmpleado")  Long idEmpleado);
}
