package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Empleado.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface EmpleadoService {

    /**
     * Obtiene el listado completo de empleados de la empresa autenticada.
     *
     * @return Lista de empleados devuelta por la API.
     */
    @GET("api/empleados")
    Call<List<RespuestaEmpleadoDTO>> listarEmpleados();
}
