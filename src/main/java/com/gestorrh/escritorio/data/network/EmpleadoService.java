package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.PeticionActualizarEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.PeticionCrearEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.PeticionResetPasswordDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaCrearEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;

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
}
