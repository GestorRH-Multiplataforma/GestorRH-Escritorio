package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa el resultado de una geocodificación de dirección.
 *
 * @param latitud  Latitud de la ubicación encontrada.
 * @param longitud Longitud de la ubicación encontrada.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaGeocodingDTO(double latitud, double longitud) { }
