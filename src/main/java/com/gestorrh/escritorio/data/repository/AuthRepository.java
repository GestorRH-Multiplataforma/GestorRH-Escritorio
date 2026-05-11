package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.AutenticacionService;
import com.gestorrh.escritorio.data.network.dto.PeticionLoginDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaLoginDTO;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio que maneja la lógica de autenticación aislando la capa de red (Retrofit)
 * de la capa de presentación (ViewModels).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AuthRepository extends BaseRepository{

    private final AutenticacionService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la autenticación.
     */
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
        PeticionLoginDTO peticion = new PeticionLoginDTO(email, password);
        return executeAsync(service.loginEmpresa(peticion));
    }
}
