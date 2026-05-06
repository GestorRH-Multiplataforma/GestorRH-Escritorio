package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de revisión (aprobación o rechazo) de una ausencia.
 * El estado debe ser APROBADA o RECHAZADA — no se puede volver a SOLICITADA.
 *
 * @param estado                Estado destino: APROBADA o RECHAZADA.
 * @param observacionesRevision Observaciones opcionales del revisor.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionRevisionAusenciaDTO(
        String estado,
        String observacionesRevision
) {}
