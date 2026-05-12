package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.empresa.PeticionActualizarEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionCambiarPasswordEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionRegistroEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.RespuestaEmpresaDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Interfaz de Retrofit para las operaciones de red de la entidad Empresa.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public interface EmpresaService {

    /**
     * Obtiene el perfil de la empresa autenticada.
     *
     * @return Datos completos del perfil de la empresa.
     */
    @GET("api/empresas/me")
    Call<RespuestaEmpresaDTO> getPerfil();

    /**
     * Actualiza el perfil de la empresa autenticada.
     *
     * @param dto DTO con los nuevos datos del perfil.
     * @return Datos actualizados del perfil de la empresa.
     */
    @PUT("api/empresas/me")
    Call<RespuestaEmpresaDTO> actualizarPerfil(@Body PeticionActualizarEmpresaDTO dto);

    /**
     * Cambia la contraseña de acceso de la empresa autenticada.
     * Requiere verificar la contraseña actual por seguridad.
     *
     * @param dto DTO con la contraseña actual y la nueva contraseña deseada.
     * @return Llamada sin cuerpo de respuesta (204 en caso de éxito).
     */
    @PUT("api/empresas/me/contrasena")
    Call<Void> cambiarPassword(@Body PeticionCambiarPasswordEmpresaDTO dto);

    /**
     * Registra una nueva empresa en el sistema.
     * Este endpoint es público y no requiere token JWT.
     *
     * @param dto DTO con los datos de la nueva empresa.
     * @return Datos del perfil de la empresa recién creada.
     */
    @POST("api/empresas/registro")
    Call<RespuestaEmpresaDTO> registrar(@Body PeticionRegistroEmpresaDTO dto);
}
