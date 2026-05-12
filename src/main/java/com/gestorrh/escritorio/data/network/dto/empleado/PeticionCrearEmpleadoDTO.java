package com.gestorrh.escritorio.data.network.dto.empleado;

/**
 * DTO que representa la petición de alta de un nuevo empleado enviada a la API REST.
 * La contraseña inicial es generada automáticamente por el servidor y devuelta
 * en la respuesta únicamente en el momento del alta.
 *
 * @param email         Correo electrónico del empleado (usado como credencial de acceso).
 * @param nombre        Nombre del empleado.
 * @param apellidos     Apellidos del empleado.
 * @param telefono      Teléfono de contacto (opcional).
 * @param puesto        Puesto o cargo que ocupará el empleado.
 * @param departamento  Departamento al que pertenecerá el empleado.
 * @param rol           Rol del empleado en el sistema (EMPLEADO o SUPERVISOR).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionCrearEmpleadoDTO(
        String email,
        String nombre,
        String apellidos,
        String telefono,
        String puesto,
        String departamento,
        String rol
) {}
