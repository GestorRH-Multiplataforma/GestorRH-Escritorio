package com.gestorrh.escritorio.core.exception;

/**
 * Excepción personalizada para encapsular los errores devueltos por la API REST.
 * Extiende de RuntimeException para no forzar bloques try-catch innecesarios
 * en toda la cadena de llamadas, alineándose con las buenas prácticas modernas.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    /**
     * Construye una nueva ApiException.
     *
     * @param message    El mensaje de error limpio extraído del JSON (RespuestaErrorDTO).
     * @param statusCode El código HTTP del error (ej. 400, 401, 404).
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * @return El código de estado HTTP original.
     */
    public int getStatusCode() {
        return statusCode;
    }
}