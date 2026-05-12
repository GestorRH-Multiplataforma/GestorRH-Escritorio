package com.gestorrh.escritorio.data.network.dto.reporte;

/**
 * DTO que representa el resumen consolidado de horas trabajadas por un empleado
 * en un rango de fechas, devuelto por la API REST.
 *
 * @param idEmpleado                 Identificador único del empleado.
 * @param nombreEmpleado             Nombre completo del empleado.
 * @param departamento               Departamento al que pertenece el empleado.
 * @param diasTrabajados             Número de días con fichaje completo en el periodo.
 * @param totalTiempoTeoricoMinutos  Total de minutos teóricos según turnos asignados.
 * @param totalTiempoTotalMinutos    Total de minutos realmente trabajados.
 * @param totalMinutosExtra          Total de minutos extra acumulados en el periodo.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaReporteResumenDTO(
        Long idEmpleado,
        String nombreEmpleado,
        String departamento,
        int diasTrabajados,
        Long totalTiempoTeoricoMinutos,
        Long totalTiempoTotalMinutos,
        Long totalMinutosExtra
) {}
