package com.gestorrh.escritorio.data.network.dto.turno;

/**
 * DTO que representa la petición de creación o actualización de un turno
 * enviada a la API REST. Las horas deben enviarse en formato "HH:mm:ss".
 *
 * @param descripcion  Descripción del turno. Requerido, máximo 100 caracteres.
 * @param horaInicio   Hora de inicio en formato "HH:mm:ss". Requerido.
 * @param horaFin      Hora de fin en formato "HH:mm:ss". Requerido.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionTurnoDTO(
        String descripcion,
        String horaInicio,
        String horaFin
) {}
