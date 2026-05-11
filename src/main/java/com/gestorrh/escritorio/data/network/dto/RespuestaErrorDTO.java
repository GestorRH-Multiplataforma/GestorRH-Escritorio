package com.gestorrh.escritorio.data.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO que mapea exactamente la estructura de errores devuelta por la API REST de GestorRH.
 * Gson puede deserializar records correctamente desde la versión 2.10+.
 *
 * @param mensaje   Mensaje de error devuelto por la API.
 * @param status    Código de estado HTTP del error.
 * @param timestamp Marca de tiempo del error.
 * @param ruta      Ruta del endpoint que generó el error.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public record RespuestaErrorDTO(
        @SerializedName("message") String mensaje,
        int status,
        String timestamp,
        String ruta
) {}
