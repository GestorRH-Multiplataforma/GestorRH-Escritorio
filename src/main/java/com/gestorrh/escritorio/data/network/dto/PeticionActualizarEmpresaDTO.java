package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de actualización del perfil de empresa enviada a la API REST.
 * Los campos de geolocalización son opcionales, pero si se incluye alguno,
 * los tres deben estar presentes para que la API pueda validar fichajes por ubicación.
 *
 * @param nombre        Nombre o razón social de la empresa. Requerido.
 * @param direccion     Dirección fiscal de la empresa. Requerido.
 * @param telefono      Teléfono de contacto de la empresa. Requerido.
 * @param latitudSede   Latitud de la sede principal. Opcional.
 * @param longitudSede  Longitud de la sede principal. Opcional.
 * @param radioValidez  Radio en metros para validación de fichajes por geolocalización. Opcional.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionActualizarEmpresaDTO(
        String nombre,
        String direccion,
        String telefono,
        Double latitudSede,
        Double longitudSede,
        Integer radioValidez
) {}
