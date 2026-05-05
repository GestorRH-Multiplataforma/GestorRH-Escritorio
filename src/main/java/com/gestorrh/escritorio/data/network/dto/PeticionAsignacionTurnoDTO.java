package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de creación o edición de una asignación de turno.
 * En creación, motivoCambio es opcional. En edición, es obligatorio.
 *
 * @param idEmpleado    Identificador del empleado al que se asigna el turno.
 * @param idTurno       Identificador del turno a asignar.
 * @param fecha         Fecha de la asignación en formato ISO (yyyy-MM-dd).
 * @param modalidad     Modalidad de trabajo (ej. "PRESENCIAL", "TELETRABAJO").
 * @param motivoCambio  Motivo del cambio. Obligatorio en edición, opcional en creación.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionAsignacionTurnoDTO(
        Long idEmpleado,
        Long idTurno,
        String fecha,
        String modalidad,
        String motivoCambio
) {}
