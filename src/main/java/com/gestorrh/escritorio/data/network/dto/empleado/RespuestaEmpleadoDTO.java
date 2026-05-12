package com.gestorrh.escritorio.data.network.dto.empleado;

/**
 * DTO que representa la respuesta de un empleado devuelta por la API REST.
 *
 * @param idEmpleado          Identificador único del empleado.
 * @param nombre              Nombre del empleado.
 * @param apellidos           Apellidos del empleado.
 * @param email               Correo electrónico del empleado.
 * @param telefono            Teléfono de contacto del empleado.
 * @param puesto              Puesto o cargo que ocupa el empleado.
 * @param departamento        Departamento al que pertenece el empleado.
 * @param rol                 Rol del empleado en el sistema (EMPLEADO o SUPERVISOR).
 * @param activo              Indica si el empleado está activo o ha sido dado de baja.
 * @param fechaAlta           Fecha de alta del empleado en formato ISO (yyyy-MM-dd).
 * @param fechaBajaContrato   Fecha de baja del contrato en formato ISO (yyyy-MM-dd).
 *                            Null si el empleado no tiene baja registrada.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public record RespuestaEmpleadoDTO(
        Long idEmpleado,
        String nombre,
        String apellidos,
        String email,
        String telefono,
        String puesto,
        String departamento,
        String rol,
        boolean activo,
        String fechaAlta,
        String fechaBajaContrato
) {}
