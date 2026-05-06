package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de registro de una nueva empresa enviada a la API REST.
 * Este endpoint es público y no requiere token JWT.
 *
 * @param email     Correo electrónico de la empresa. Requerido, debe ser único en la BD.
 * @param password  Contraseña de acceso. Requerido, mínimo 8 caracteres.
 * @param nombre    Nombre o razón social de la empresa. Requerido.
 * @param direccion Dirección fiscal de la empresa. Requerido.
 * @param telefono  Teléfono de contacto de la empresa. Requerido.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionRegistroEmpresaDTO(
        String email,
        String password,
        String nombre,
        String direccion,
        String telefono
) {}
