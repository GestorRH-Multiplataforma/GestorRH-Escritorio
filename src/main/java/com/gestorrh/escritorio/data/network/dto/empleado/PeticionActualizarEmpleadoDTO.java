package com.gestorrh.escritorio.data.network.dto.empleado;

/**
 * DTO que representa la petición de actualización de datos de un empleado existente.
 * No incluye el email ya que no es un campo editable tras el alta.
 *
 * @param nombre        Nombre del empleado.
 * @param apellidos     Apellidos del empleado.
 * @param telefono      Teléfono de contacto (opcional).
 * @param puesto        Puesto o cargo del empleado.
 * @param departamento  Departamento al que pertenece el empleado.
 * @param rol           Rol del empleado en el sistema (EMPLEADO o SUPERVISOR).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionActualizarEmpleadoDTO(
        String nombre,
        String apellidos,
        String telefono,
        String puesto,
        String departamento,
        String rol
) {}
