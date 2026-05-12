package com.gestorrh.escritorio.data.network.dto.auth;

/**
 * DTO que representa la respuesta de login devuelta por la API REST.
 *
 * @param token  Token JWT para autenticar las peticiones posteriores.
 * @param rol    Rol del usuario autenticado (ej. "EMPRESA").
 * @param id     Identificador único de la empresa autenticada.
 * @param nombre Nombre o razón social de la empresa autenticada.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaLoginDTO(String token, String rol, Long id, String nombre) {}
