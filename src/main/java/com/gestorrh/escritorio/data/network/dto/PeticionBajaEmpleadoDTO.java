package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO que representa la petición de baja de un empleado enviada a la API REST.
 * Establece la fecha a partir de la cual la API gestionará automáticamente
 * el cambio de estado del empleado.
 *
 * @param fechaBajaContrato Fecha de baja del contrato en formato ISO (yyyy-MM-dd).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record PeticionBajaEmpleadoDTO(String fechaBajaContrato) {}
