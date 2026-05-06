package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.PeticionRevisionAusenciaDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaAusenciaDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Ausencia.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface AusenciaService {

    /**
     * Obtiene la lista de ausencias de la empresa filtradas por estado.
     *
     * @param estado Estado a filtrar: SOLICITADA, APROBADA o RECHAZADA.
     * @return Lista de ausencias que coinciden con el estado indicado.
     */
    @GET("api/ausencias")
    Call<List<RespuestaAusenciaDTO>> listarPorEstado(@Query("estado") String estado);

    /**
     * Obtiene los tipos de ausencia disponibles.
     *
     * @return Lista de cadenas con los valores de tipo (ej. "VACACIONES", "MEDICA").
     */
    @GET("api/ausencias/tipos")
    Call<List<String>> listarTipos();

    /**
     * Obtiene los estados de ausencia disponibles.
     *
     * @return Lista de cadenas con los valores de estado (ej. "SOLICITADA", "APROBADA").
     */
    @GET("api/ausencias/estados")
    Call<List<String>> listarEstados();

    /**
     * Aprueba o rechaza una ausencia existente.
     *
     * @param id      Identificador único de la ausencia a revisar.
     * @param peticion DTO con el nuevo estado y las observaciones opcionales.
     * @return Ausencia actualizada con el nuevo estado y datos de revisión.
     */
    @PUT("api/ausencias/{id}/revision")
    Call<RespuestaAusenciaDTO> revisar(@Path("id") Long id,
                                       @Body PeticionRevisionAusenciaDTO peticion);

    /**
     * Descarga el archivo justificante de una ausencia.
     * Usa @Streaming para evitar cargar el fichero completo en memoria.
     *
     * @param nombreArchivo Nombre del archivo tal como viene en el campo justificante del DTO.
     * @return ResponseBody con los bytes del archivo (PDF o imagen).
     */
    @Streaming
    @GET("api/ausencias/justificantes/{nombreArchivo}")
    Call<ResponseBody> descargarJustificante(@Path("nombreArchivo") String nombreArchivo);
}
