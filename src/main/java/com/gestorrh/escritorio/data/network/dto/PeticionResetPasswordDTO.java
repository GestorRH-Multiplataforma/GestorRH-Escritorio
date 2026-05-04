package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de restablecimiento de contraseña de un empleado.
 * Utilizado por el personal de RRHH cuando un empleado ha olvidado su contraseña.
 * La nueva contraseña nunca se devuelve en la respuesta de la API.
 *
 * @param nuevaPassword Nueva contraseña establecida por el administrador (mínimo 8 caracteres).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionResetPasswordDTO(String nuevaPassword) {}
