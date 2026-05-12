package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.auth.PeticionLoginDTO;
import com.gestorrh.escritorio.data.network.dto.auth.RespuestaLoginDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Interfaz de Retrofit para el endpoint de autenticación de empresas.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public interface AutenticacionService {

    /**
     * Autentica a una empresa en el sistema mediante email y contraseña.
     *
     * @param peticion DTO con las credenciales de acceso de la empresa.
     * @return Llamada Retrofit con el token JWT y los datos de la sesión iniciada.
     */
    @POST("api/auth/login-empresa")
    Call<RespuestaLoginDTO> loginEmpresa(@Body PeticionLoginDTO peticion);
}
