package com.gestorrh.escritorio.core.security;

/**
 * Gestor de sesión centralizado en memoria.
 * Implementa el patrón Singleton para garantizar que toda la aplicación
 * comparta el mismo estado de autenticación.
 * Cumple con el requisito de volatilidad: los datos no se persisten en disco.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class SessionManager {

    private static SessionManager instance;

    private String token;
    private Long empresaId;
    private String nombreEmpresa;

    /**
     * Constructor privado para evitar instanciación directa (Singleton).
     */
    private SessionManager() {
        // Inicialmente la sesión está vacía
    }

    /**
     * Devuelve la instancia única del gestor de sesión.
     *
     * @return Instancia Singleton de SessionManager.
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Guarda los datos de la sesión tras un login exitoso.
     * Adaptado exclusivamente para el rol EMPRESA.
     *
     * @param token         Token JWT devuelto por la API.
     * @param empresaId     Identificador único de la empresa.
     * @param nombreEmpresa Nombre o razón social de la empresa.
     */
    public void saveSession(String token, Long empresaId, String nombreEmpresa) {
        this.token = token;
        this.empresaId = empresaId;
        this.nombreEmpresa = nombreEmpresa;
    }

    /**
     * Obtiene el token JWT actual.
     *
     * @return El token JWT o null si no hay sesión iniciada.
     */
    public String getToken() {
        return token;
    }

    /**
     * Obtiene el ID de la empresa autenticada.
     *
     * @return ID de la empresa.
     */
    public Long getEmpresaId() {
        return empresaId;
    }

    /**
     * Obtiene el nombre de la empresa autenticada.
     *
     * @return Nombre de la empresa.
     */
    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    /**
     * Cierra la sesión eliminando los datos de la memoria.
     */
    public void clearSession() {
        this.token = null;
        this.empresaId = null;
        this.nombreEmpresa = null;
    }

    /**
     * Verifica si hay una sesión activa basándose en la existencia del token.
     *
     * @return true si el usuario está autenticado, false en caso contrario.
     */
    public boolean isAuthenticated() {
        return token != null && !token.isBlank();
    }
}
