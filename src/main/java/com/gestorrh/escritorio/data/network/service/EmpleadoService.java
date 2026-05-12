package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.empleado.*;
import retrofit2.*;
import retrofit2.http.*;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Empleado.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public interface EmpleadoService {

    /**
     * Obtiene el listado completo de empleados de la empresa autenticada.
     *
     * @return Lista de empleados devuelta por la API.
     */
    @GET("api/empleados")
    Call<List<RespuestaEmpleadoDTO>> listarEmpleados();

    /**
     * Da de alta a un nuevo empleado. La contraseña inicial es generada
     * automáticamente por el servidor y devuelta en la respuesta.
     *
     * @param peticion DTO con los datos del nuevo empleado.
     * @return Datos del empleado creado incluyendo la contraseña generada.
     */
    @POST("api/empleados")
    Call<RespuestaCrearEmpleadoDTO> crearEmpleado(@Body PeticionCrearEmpleadoDTO peticion);

    /**
     * Actualiza los datos de un empleado existente.
     *
     * @param id      Identificador único del empleado a actualizar.
     * @param peticion DTO con los nuevos datos del empleado.
     * @return Datos actualizados del empleado.
     */
    @PUT("api/empleados/{id}")
    Call<RespuestaEmpleadoDTO> actualizarEmpleado(@Path("id") Long id,
                                                  @Body PeticionActualizarEmpleadoDTO peticion);

    /**
     * Restablece la contraseña de un empleado. Usado por RRHH cuando el empleado
     * ha olvidado su contraseña. La nueva contraseña no se devuelve en la respuesta.
     *
     * @param id      Identificador único del empleado.
     * @param peticion DTO con la nueva contraseña establecida por el administrador.
     * @return Datos actualizados del empleado.
     */
    @PUT("api/empleados/{id}/reset-password")
    Call<RespuestaEmpleadoDTO> resetPassword(@Path("id") Long id,
                                             @Body PeticionResetPasswordDTO peticion);

    /**
     * Tramita la baja de un empleado estableciendo la fecha de baja del contrato.
     * La API gestionará automáticamente el cambio de estado a partir de esa fecha.
     *
     * @param id      Identificador único del empleado.
     * @param peticion DTO con la fecha de baja del contrato.
     * @return Llamada sin cuerpo de respuesta (204 en caso de éxito).
     */
    @POST("api/empleados/{id}/baja")
    Call<Void> darDeBaja(@Path("id") Long id, @Body PeticionBajaEmpleadoDTO peticion);

    /**
     * Readmite a un empleado dado de baja, reactivando su acceso al sistema.
     *
     * @param id Identificador único del empleado a readmitir.
     * @return Llamada sin cuerpo de respuesta (204 en caso de éxito).
     */
    @POST("api/empleados/{id}/readmitir")
    Call<RespuestaCrearEmpleadoDTO> readmitir(@Path("id") Long id);
}
