package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO de presentación que representa los indicadores clave de rendimiento (KPIs)
 * del Dashboard. Se construye en el repositorio a partir del {@code Map<String, Long>}
 * devuelto por el endpoint {@code GET /api/estadisticas/kpis}.
 *
 * @param totalEmpleados   Número total de empleados activos en la empresa.
 * @param planificadosHoy  Empleados con al menos un turno asignado para la fecha actual.
 * @param ausentesHoy      Empleados con ausencias aprobadas vigentes en la fecha actual.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record KpisDTO(
        long totalEmpleados,
        long planificadosHoy,
        long ausentesHoy
) {}
