package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.turno.PeticionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaTurnoDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Turno.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface TurnoService {

    /**
     * Obtiene el listado completo de turnos de la empresa autenticada.
     *
     * @return Lista de turnos devuelta por la API.
     */
    @GET("api/turnos")
    Call<List<RespuestaTurnoDTO>> listarTurnos();

    /**
     * Crea un nuevo turno para la empresa autenticada.
     *
     * @param peticion DTO con los datos del nuevo turno.
     * @return Datos del turno creado.
     */
    @POST("api/turnos")
    Call<RespuestaTurnoDTO> crearTurno(@Body PeticionTurnoDTO peticion);

    /**
     * Actualiza un turno existente.
     *
     * @param id       Identificador único del turno a actualizar.
     * @param peticion DTO con los nuevos datos del turno.
     * @return Datos actualizados del turno.
     */
    @PUT("api/turnos/{id}")
    Call<RespuestaTurnoDTO> actualizarTurno(@Path("id") Long id, @Body PeticionTurnoDTO peticion);

    /**
     * Elimina un turno. Si tiene asignaciones activas, la API responde con error 4xx.
     *
     * @param id Identificador único del turno a eliminar.
     * @return Llamada sin cuerpo de respuesta (204 en caso de éxito).
     */
    @DELETE("api/turnos/{id}")
    Call<Void> eliminarTurno(@Path("id") Long id);
}
