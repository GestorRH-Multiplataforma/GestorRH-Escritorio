package com.gestorrh.escritorio.config;

import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor centralizado de configuración de la aplicación.
 * Implementa el patrón Singleton. Lee las propiedades desde application-{env}.properties
 * y provee acceso tipado a las mismas.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ConfigManager {

    private static ConfigManager instance;
    private final Properties properties;

    /**
     * Constructor privado. Carga las propiedades dependiendo del entorno.
     * Prioriza la variable de entorno GESTORRH_ENV (para CI/CD), si no asume 'dev'.
     *
     * @throws IllegalStateException Si el archivo no existe o faltan propiedades críticas.
     */
    private ConfigManager() {
        properties = new Properties();

        String env = System.getenv("GESTORRH_ENV");
        if (env == null || env.isBlank()) {
            env = System.getProperty("env", "dev");
        }

        String propFileName = "config/application-" + env + ".properties";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            if (input == null) {
                throw new IllegalStateException("CRÍTICO: No se encontró el archivo de configuración: " + propFileName);
            }
            properties.load(input);
        } catch (Exception ex) {
            throw new IllegalStateException("CRÍTICO: Error al cargar la configuración de la aplicación", ex);
        }

        validarPropiedadesObligatorias();
    }

    /**
     * @return Instancia única del gestor de configuración.
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Valida que la configuración esencial para arrancar la app esté presente.
     */
    private void validarPropiedadesObligatorias() {
        if (getBaseUrl() == null || getBaseUrl().isBlank()) {
            throw new IllegalStateException("CRÍTICO: La propiedad 'gestorrh.api.url' es obligatoria y no está definida.");
        }
    }

    /**
     * Obtiene la URL base de la API.
     * Prioriza la variable de entorno GESTORRH_API_URL (Perfil Prod).
     *
     * @return URL base del servidor REST.
     */
    public String getBaseUrl() {
        String envUrl = System.getenv("GESTORRH_API_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        return properties.getProperty("gestorrh.api.url");
    }

    /**
     * Obtiene el nivel de log configurado.
     *
     * @return Nivel de log (ej. DEBUG, WARN). Por defecto INFO.
     */
    public String getLogLevel() {
        return properties.getProperty("log.level", "INFO");
    }

    /**
     * Obtiene el directorio de salida para los reportes PDF.
     *
     * @return Ruta del directorio. Por defecto 'reportes_pdf'.
     */
    public String getPdfOutputDir() {
        return properties.getProperty("pdf.output.dir", "reportes_pdf");
    }
}
