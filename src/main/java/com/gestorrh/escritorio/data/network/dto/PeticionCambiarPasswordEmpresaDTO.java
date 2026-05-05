package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de cambio de contraseña de la empresa enviada a la API REST.
 * A diferencia del reset de empleados (que solo requiere la nueva contraseña),
 * este endpoint exige verificar la contraseña actual por seguridad.
 *
 * @param passwordActual  Contraseña actual de la empresa. Requerido.
 * @param nuevaPassword   Nueva contraseña deseada. Mínimo 8 caracteres. Requerido.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionCambiarPasswordEmpresaDTO(
        String passwordActual,
        String nuevaPassword
) {}
