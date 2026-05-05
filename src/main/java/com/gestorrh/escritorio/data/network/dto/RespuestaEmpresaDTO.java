package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la respuesta del perfil de empresa devuelta por la API REST.
 *
 * @param idEmpresa     Identificador único de la empresa.
 * @param email         Correo electrónico de la empresa (no editable).
 * @param nombre        Nombre o razón social de la empresa.
 * @param direccion     Dirección fiscal de la empresa.
 * @param telefono      Teléfono de contacto de la empresa.
 * @param latitudSede   Latitud de la sede principal. Null si no está configurada.
 * @param longitudSede  Longitud de la sede principal. Null si no está configurada.
 * @param radioValidez  Radio en metros para validación de fichajes por geolocalización.
 *                      Null si no está configurado.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record RespuestaEmpresaDTO(
        Long idEmpresa,
        String email,
        String nombre,
        String direccion,
        String telefono,
        Double latitudSede,
        Double longitudSede,
        Integer radioValidez
) {}
