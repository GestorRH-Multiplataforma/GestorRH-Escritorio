package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la respuesta del servidor tras dar de alta un nuevo empleado.
 * A diferencia de {@link RespuestaEmpleadoDTO}, incluye el campo {@code passwordGenerada}
 * que contiene la contraseña inicial generada automáticamente por la API.
 * Este campo solo está disponible en el momento del alta y nunca se vuelve a exponer.
 *
 * @param idEmpleado        Identificador único del empleado recién creado.
 * @param nombre            Nombre del empleado.
 * @param apellidos         Apellidos del empleado.
 * @param email             Correo electrónico del empleado.
 * @param rol               Rol asignado en el sistema (EMPLEADO o SUPERVISOR).
 * @param passwordGenerada  Contraseña inicial generada por el servidor. Solo visible en esta respuesta.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaCrearEmpleadoDTO(
        Long idEmpleado,
        String nombre,
        String apellidos,
        String email,
        String rol,
        String passwordGenerada
) {}
