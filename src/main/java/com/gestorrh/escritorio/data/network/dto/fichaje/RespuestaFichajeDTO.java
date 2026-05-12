package com.gestorrh.escritorio.data.network.dto.fichaje;

/**
 * DTO que representa la respuesta de un fichaje devuelta por la API REST.
 *
 * @param idFichaje      Identificador único del fichaje.
 * @param idEmpleado     Identificador del empleado que realizó el fichaje.
 * @param nombreEmpleado Nombre completo del empleado.
 * @param fecha          Fecha del fichaje en formato ISO (yyyy-MM-dd).
 * @param horaEntrada    Hora de entrada en formato "HH:mm:ss". Null si no se ha fichado entrada.
 * @param horaSalida     Hora de salida en formato "HH:mm:ss". Null si no se ha fichado salida.
 * @param incidencias    Descripción de incidencias asociadas al fichaje. Null si no las hay.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaFichajeDTO(
        Long idFichaje,
        Long idEmpleado,
        String nombreEmpleado,
        String fecha,
        String horaEntrada,
        String horaSalida,
        String incidencias
) {}
