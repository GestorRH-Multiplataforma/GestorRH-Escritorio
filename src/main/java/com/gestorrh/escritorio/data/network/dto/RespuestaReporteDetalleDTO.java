package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa un registro detallado del informe de control horario
 * devuelto por la API REST. Cada instancia corresponde a un fichaje completo
 * (con entrada y salida registradas) de un empleado en un día concreto.
 *
 * @param idEmpleado           Identificador único del empleado.
 * @param nombreEmpleado       Nombre completo del empleado.
 * @param departamento         Departamento al que pertenece el empleado.
 * @param fecha                Fecha del fichaje en formato ISO (yyyy-MM-dd).
 * @param descripcionTurno     Descripción del turno asignado, o indicación de fichaje fantasma.
 * @param horaEntradaReal      Hora de entrada registrada en formato HH:mm:ss.
 * @param horaSalidaReal       Hora de salida registrada en formato HH:mm:ss.
 * @param tiempoTotalMinutos   Minutos totales trabajados en la jornada.
 * @param tiempoTeoricoMinutos Minutos teóricos según el turno asignado.
 * @param minutosExtra         Minutos extra realizados por encima del turno.
 * @param incidencias          Descripción de incidencias del fichaje. Vacío si no las hay.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaReporteDetalleDTO(
        Long idEmpleado,
        String nombreEmpleado,
        String departamento,
        String fecha,
        String descripcionTurno,
        String horaEntradaReal,
        String horaSalidaReal,
        Long tiempoTotalMinutos,
        Long tiempoTeoricoMinutos,
        Long minutosExtra,
        String incidencias
) {}
