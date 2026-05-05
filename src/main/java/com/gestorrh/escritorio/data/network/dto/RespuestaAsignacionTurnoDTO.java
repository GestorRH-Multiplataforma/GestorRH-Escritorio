package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la respuesta de una asignación de turno devuelta por la API REST.
 * Las horas se reciben como cadenas en formato "HH:mm".
 * La fecha se recibe como cadena en formato "yyyy-MM-dd".
 *
 * @param idAsignacion           Identificador único de la asignación.
 * @param idEmpleado             Identificador del empleado asignado.
 * @param nombreCompletoEmpleado Nombre completo del empleado.
 * @param idTurno                Identificador del turno asignado.
 * @param descripcionTurno       Descripción del turno.
 * @param horaInicio             Hora de inicio del turno en formato "HH:mm".
 * @param horaFin                Hora de fin del turno en formato "HH:mm".
 * @param fecha                  Fecha de la asignación en formato "yyyy-MM-dd".
 * @param modalidad              Modalidad de trabajo (ej. "PRESENCIAL", "TELETRABAJO").
 * @param motivoCambio           Motivo del último cambio registrado. Puede ser null.
 * @param fechaCambio            Fecha y hora del último cambio. Puede ser null.
 * @param responsableCambio      Nombre del responsable del último cambio. Puede ser null.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaAsignacionTurnoDTO(
        Long idAsignacion,
        Long idEmpleado,
        String nombreCompletoEmpleado,
        Long idTurno,
        String descripcionTurno,
        String horaInicio,
        String horaFin,
        String fecha,
        String modalidad,
        String motivoCambio,
        String fechaCambio,
        String responsableCambio
) {}
