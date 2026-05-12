package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.service.EmpresaService;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionActualizarEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionCambiarPasswordEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionRegistroEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.RespuestaEmpresaDTO;

import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos del perfil de la empresa autenticada.
 * Actúa como mediador entre la capa de red y la lógica de presentación,
 * exponiendo operaciones asíncronas mediante CompletableFuture.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class EmpresaRepository extends BaseRepository {

    private final EmpresaService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad Empresa.
     */
    public EmpresaRepository(EmpresaService service) {
        this.service = service;
    }

    /**
     * Obtiene el perfil completo de la empresa autenticada de forma asíncrona.
     *
     * @return CompletableFuture con los datos del perfil o ApiException si falla.
     */
    public CompletableFuture<RespuestaEmpresaDTO> getPerfil() {
        return executeAsync(service.getPerfil());
    }

    /**
     * Actualiza el perfil de la empresa autenticada de forma asíncrona.
     *
     * @param dto DTO con los nuevos datos del perfil.
     * @return CompletableFuture con el perfil actualizado o ApiException si falla.
     */
    public CompletableFuture<RespuestaEmpresaDTO> actualizarPerfil(PeticionActualizarEmpresaDTO dto) {
        return executeAsync(service.actualizarPerfil(dto));
    }

    /**
     * Cambia la contraseña de la empresa autenticada de forma asíncrona.
     *
     * @param dto DTO con la contraseña actual y la nueva contraseña deseada.
     * @return CompletableFuture que se completa con null tras el cambio exitoso.
     */
    public CompletableFuture<Void> cambiarPassword(PeticionCambiarPasswordEmpresaDTO dto) {
        return executeAsyncVoid(service.cambiarPassword(dto));
    }

    /**
     * Registra una nueva empresa de forma asíncrona.
     * Endpoint público, no requiere token JWT.
     *
     * @param dto DTO con los datos de registro de la nueva empresa.
     * @return CompletableFuture con el perfil de la empresa creada o ApiException si falla.
     */
    public CompletableFuture<RespuestaEmpresaDTO> registrar(PeticionRegistroEmpresaDTO dto) {
        return executeAsync(service.registrar(dto));
    }
}
