package com.gestorrh.escritorio.core.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Gestor centralizado de internacionalización (i18n).
 * Implementa el patrón Singleton y el patrón Observer para permitir
 * el cambio de idioma dinámico en tiempo de ejecución.
 * Persiste la preferencia del usuario en el sistema operativo mediante java.util.prefs.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class LanguageManager {

    private static LanguageManager instance;

    private static final String PREF_LANG_KEY = "gestorrh_language";
    private static final String BASE_NAME = "i18n/messages";

    private ResourceBundle bundle;
    private Locale currentLocale;
    private final Preferences prefs;

    private final List<Runnable> listeners;

    /**
     * Constructor privado (Singleton).
     * Recupera el último idioma usado o establece español ('es') por defecto.
     */
    private LanguageManager() {
        this.listeners = new ArrayList<>();
        this.prefs = Preferences.userNodeForPackage(LanguageManager.class);

        String savedLang = prefs.get(PREF_LANG_KEY, "es");
        setLocale(new Locale(savedLang));
    }

    /**
     * @return Instancia única del gestor de idiomas.
     */
    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    /**
     * Cambia el idioma de la aplicación, guarda la preferencia y notifica a las vistas.
     *
     * @param locale Nuevo Locale (ej. new Locale("en"))
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        this.bundle = ResourceBundle.getBundle(BASE_NAME, locale);

        prefs.put(PREF_LANG_KEY, locale.getLanguage());

        notifyListeners();
    }

    /**
     * Obtiene el texto traducido para una clave dada.
     *
     * @param key Clave del texto en el archivo .properties (ej. "login.title")
     * @return Texto traducido. Si no existe, devuelve la clave rodeada de exclamaciones.
     */
    public String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            System.err.println("i18n: Falta la clave '" + key + "' para el idioma '" + currentLocale.getLanguage() + "'");
            return "!" + key + "!";
        }
    }

    /**
     * Registra un nuevo método que se ejecutará cuando cambie el idioma.
     * Normalmente será el método updateTexts() de un Controller FXML.
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * Elimina un listener. Útil cuando se cierra una pantalla para liberar memoria.
     */
    public void removeListener(Runnable listener) {
        listeners.remove(listener);
    }

    /**
     * Dispara la ejecución de todos los métodos registrados.
     */
    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    /**
     * @return El Locale activo actualmente.
     */
    public Locale getCurrentLocale() {
        return currentLocale;
    }
}
