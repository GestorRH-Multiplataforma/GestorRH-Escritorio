package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.data.network.AutenticacionService;
import com.gestorrh.escritorio.data.network.dto.PeticionLoginDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaLoginDTO;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio que maneja la lógica de autenticación aislando la capa de red (Retrofit)
 * de la capa de presentación (ViewModels).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AuthRepository {

    private final AutenticacionService service;

    public AuthRepository(AutenticacionService service) {
        this.service = service;
    }

    /**
     * Realiza el login de manera asíncrona para no bloquear la interfaz gráfica.
     *
     * @param email    Correo electrónico de la empresa.
     * @param password Contraseña plana (viajará segura por HTTPs/Interceptor).
     * @return Un CompletableFuture con la respuesta del servidor.
     */
    public CompletableFuture<RespuestaLoginDTO> login(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PeticionLoginDTO peticion = new PeticionLoginDTO(email, password);

                Response<RespuestaLoginDTO> response = service.loginEmpresa(peticion).execute();

                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
                throw new ApiException("error.unknown", response.code());
            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                throw new ApiException("error.timeout", 0);
            }
        });
    }
}
