package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.PeticionAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaAsignacionTurnoDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad AsignacionTurno.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface AsignacionTurnoService {

    /**
     * Obtiene todas las asignaciones de turno de la empresa autenticada.
     * El filtrado por mes se realiza en el cliente.
     *
     * @return Lista de asignaciones devuelta por la API.
     */
    @GET("api/asignaciones")
    Call<List<RespuestaAsignacionTurnoDTO>> listarAsignaciones();

    /**
     * Obtiene las modalidades de trabajo disponibles.
     *
     * @return Lista de cadenas con los valores de modalidad (ej. "PRESENCIAL", "TELETRABAJO").
     */
    @GET("api/asignaciones/modalidades")
    Call<List<String>> listarModalidades();

    /**
     * Crea una nueva asignación de turno.
     *
     * @param peticion DTO con los datos de la nueva asignación.
     * @return Datos de la asignación creada.
     */
    @POST("api/asignaciones")
    Call<RespuestaAsignacionTurnoDTO> crearAsignacion(@Body PeticionAsignacionTurnoDTO peticion);

    /**
     * Edita una asignación de turno existente.
     * El campo motivoCambio es obligatorio en edición para registro de auditoría.
     *
     * @param id       Identificador único de la asignación a editar.
     * @param peticion DTO con los nuevos datos de la asignación.
     * @return Datos actualizados de la asignación.
     */
    @PUT("api/asignaciones/{id}")
    Call<RespuestaAsignacionTurnoDTO> editarAsignacion(@Path("id") Long id,
                                                       @Body PeticionAsignacionTurnoDTO peticion);

    /**
     * Elimina una asignación de turno.
     *
     * @param id Identificador único de la asignación a eliminar.
     * @return Llamada sin cuerpo de respuesta (204 en caso de éxito).
     */
    @DELETE("api/asignaciones/{id}")
    Call<Void> eliminarAsignacion(@Path("id") Long id);
}
