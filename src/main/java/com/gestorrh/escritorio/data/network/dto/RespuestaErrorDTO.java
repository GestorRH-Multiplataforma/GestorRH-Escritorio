package com.gestorrh.escritorio.data.network.dto;

/**
 * Objeto de Transferencia de Datos (DTO) que mapea exactamente la estructura
 * de errores que devuelve la API REST de GestorRH.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class RespuestaErrorDTO {

    private String mensaje;
    private int status;
    private String timestamp;
    private String ruta;

    public RespuestaErrorDTO() {}

    public String getMensaje() { return mensaje; }
    public int getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public String getRuta() { return ruta; }

    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public void setStatus(int status) { this.status = status; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setRuta(String ruta) { this.ruta = ruta; }
}
