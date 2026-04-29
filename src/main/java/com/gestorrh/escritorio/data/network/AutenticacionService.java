package com.gestorrh.escritorio.data.network;

import com.gestorrh.escritorio.data.network.dto.PeticionLoginDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaLoginDTO;
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

    @POST("api/auth/login-empresa")
    Call<RespuestaLoginDTO> loginEmpresa(@Body PeticionLoginDTO peticion);
}
