package com.gestorrh.escritorio.data.network.dto.turno;

/**
 * DTO que representa la respuesta de un turno devuelta por la API REST.
 * Las horas se reciben como cadenas en formato "HH:mm:ss".
 *
 * @param idTurno      Identificador único del turno.
 * @param descripcion  Descripción del turno (ej. "Turno Mañana").
 * @param horaInicio   Hora de inicio del turno en formato "HH:mm:ss".
 * @param horaFin      Hora de fin del turno en formato "HH:mm:ss".
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaTurnoDTO(
        Long idTurno,
        String descripcion,
        String horaInicio,
        String horaFin
) {}
