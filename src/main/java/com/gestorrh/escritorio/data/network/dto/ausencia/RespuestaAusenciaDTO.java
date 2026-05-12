package com.gestorrh.escritorio.data.network.dto.ausencia;

/**
 * DTO que representa la respuesta de una ausencia devuelta por la API REST.
 *
 * @param idAusencia              Identificador único de la ausencia.
 * @param idEmpleado              Identificador del empleado solicitante.
 * @param nombreCompletoEmpleado  Nombre completo del empleado.
 * @param tipo                    Tipo de ausencia (VACACIONES, MEDICA, MOTIVO_PERSONAL, OTROS).
 * @param descripcion             Descripción libre de la ausencia.
 * @param fechaInicio             Fecha de inicio en formato ISO (yyyy-MM-dd).
 * @param fechaFin                Fecha de fin en formato ISO (yyyy-MM-dd).
 * @param justificante            Nombre del archivo justificante. Null si no hay adjunto.
 * @param estado                  Estado actual (SOLICITADA, APROBADA, RECHAZADA).
 * @param responsableRevision     Nombre del responsable que revisó. Null si pendiente.
 * @param observacionesRevision   Observaciones de la revisión. Null si no se indicaron.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaAusenciaDTO(
        Long idAusencia,
        Long idEmpleado,
        String nombreCompletoEmpleado,
        String tipo,
        String descripcion,
        String fechaInicio,
        String fechaFin,
        String justificante,
        String estado,
        String responsableRevision,
        String observacionesRevision
) {}
