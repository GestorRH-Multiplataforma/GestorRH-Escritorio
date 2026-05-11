package com.gestorrh.escritorio.core.security;

/**
 * Gestor de sesión centralizado en memoria.
 * Implementa el patrón Singleton para garantizar que toda la aplicación
 * comparta el mismo estado de autenticación.
 * Cumple con el requisito de volatilidad: los datos no se persisten en disco.
 * El estado de sesión se almacena en un record inmutable para garantizar
 * consistencia ante accesos concurrentes.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class SessionManager {

    /**
     * Record inmutable que encapsula los datos de la sesión activa.
     * Al ser inmutable, su asignación es atómica desde el punto de vista
     * de los consumidores — nunca se lee un estado parcialmente escrito.
     */
    private record DatosSesion(String token, Long empresaId, String nombreEmpresa) {}

    private volatile DatosSesion sesionActual;

    /**
     * Constructor privado para evitar instanciación directa (Singleton).
     */
    private SessionManager() {
        this.sesionActual = null;
    }

    /**
     * Devuelve la instancia única del gestor de sesión.
     *
     * @return Instancia Singleton de SessionManager.
     */
    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Clase interna estática que garantiza la inicialización lazy y thread-safe
     * del Singleton sin necesidad de sincronización explícita.
     */
    private static final class Holder {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    /**
     * Guarda los datos de la sesión tras un login exitoso.
     * La asignación del record es atómica, evitando estados inconsistentes
     * entre los tres campos.
     *
     * @param token         Token JWT devuelto por la API.
     * @param empresaId     Identificador único de la empresa.
     * @param nombreEmpresa Nombre o razón social de la empresa.
     */
    public void saveSession(String token, Long empresaId, String nombreEmpresa) {
        this.sesionActual = new DatosSesion(token, empresaId, nombreEmpresa);
    }

    /**
     * Obtiene el token JWT actual.
     *
     * @return El token JWT o null si no hay sesión iniciada.
     */
    public String getToken() {
        DatosSesion sesion = sesionActual;
        return sesion != null ? sesion.token() : null;
    }

    /**
     * Obtiene el ID de la empresa autenticada.
     *
     * @return ID de la empresa o null si no hay sesión iniciada.
     */
    public Long getEmpresaId() {
        DatosSesion sesion = sesionActual;
        return sesion != null ? sesion.empresaId() : null;
    }

    /**
     * Obtiene el nombre de la empresa autenticada.
     *
     * @return Nombre de la empresa o null si no hay sesión iniciada.
     */
    public String getNombreEmpresa() {
        DatosSesion sesion = sesionActual;
        return sesion != null ? sesion.nombreEmpresa() : null;
    }

    /**
     * Cierra la sesión eliminando los datos de la memoria.
     */
    public void clearSession() {
        this.sesionActual = null;
    }

    /**
     * Verifica si hay una sesión activa basándose en la existencia del token.
     *
     * @return true si el usuario está autenticado, false en caso contrario.
     */
    public boolean isAuthenticated() {
        DatosSesion sesion = sesionActual;
        return sesion != null && sesion.token() != null && !sesion.token().isBlank();
    }
}
