package com.gestorrh.escritorio.core.exception;

/**
 * Excepción personalizada para encapsular los errores devueltos por la API REST.
 * Extiende de RuntimeException para no forzar bloques try-catch innecesarios
 * en toda la cadena de llamadas, alineándose con las buenas prácticas modernas.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class ApiException extends RuntimeException {

    private final int statusCode;
    private final String i18nKey;

    /**
     * Construye una ApiException con mensaje directo de la API (sin clave i18n).
     * Usar cuando el mensaje ya viene traducido desde el servidor.
     *
     * @param message    El mensaje de error extraído del JSON (RespuestaErrorDTO).
     * @param statusCode El código HTTP del error (ej. 400, 401, 404).
     */
    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.i18nKey = null;
    }

    /**
     * Construye una ApiException con clave i18n para errores generados en el cliente.
     * Usar para errores de red, timeout o estados HTTP genéricos sin mensaje de la API.
     *
     * @param message    Mensaje técnico interno (para logs).
     * @param statusCode El código HTTP del error.
     * @param i18nKey    Clave del ResourceBundle para mostrar al usuario.
     */
    public ApiException(String message, int statusCode, String i18nKey) {
        super(message);
        this.statusCode = statusCode;
        this.i18nKey = i18nKey;
    }

    /**
     * @return El código de estado HTTP original.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @return Clave i18n para resolución en el cliente, o null si el mensaje
     *         ya viene traducido desde la API.
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * Indica si este error debe resolverse mediante el ResourceBundle del cliente.
     *
     * @return true si tiene clave i18n, false si el mensaje es directo de la API.
     */
    public boolean hasI18nKey() {
        return i18nKey != null && !i18nKey.isBlank();
    }
}