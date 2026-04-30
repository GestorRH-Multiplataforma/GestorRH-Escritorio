package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de login enviada a la API REST.
 *
 * @param email    Correo electrónico de la empresa.
 * @param password Contraseña en texto plano (viaja segura por HTTPS).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionLoginDTO(String email, String password) {}