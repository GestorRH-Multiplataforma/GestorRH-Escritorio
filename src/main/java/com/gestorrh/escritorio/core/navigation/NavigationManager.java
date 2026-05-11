package com.gestorrh.escritorio.core.navigation;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Gestor centralizado de navegación entre vistas FXML.
 * Implementa el patrón Singleton. Mantiene referencia al ContentPane del Shell
 * y se encarga de cargar dinámicamente las vistas en él.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class NavigationManager {

    private static final Logger LOGGER = Logger.getLogger(NavigationManager.class.getName());

    private BorderPane panelContenido;
    private Object controladorActual;

    /**
     * Constructor privado (Singleton).
     */
    private NavigationManager() {}

    /**
     * @return Instancia única del gestor de navegación.
     */
    public static NavigationManager getInstance() {
        return Holder.INSTANCIA;
    }

    /**
     * Clase interna estática que garantiza la inicialización lazy y thread-safe
     * del Singleton sin necesidad de sincronización explícita.
     */
    private static final class Holder {
        private static final NavigationManager INSTANCIA = new NavigationManager();
    }

    /**
     * Registra el contenedor central del Shell donde se cargarán las vistas.
     * Debe llamarse desde el ShellController en su método initialize().
     *
     * @param panelContenido BorderPane central del Shell.
     */
    public void setPanelContenido(BorderPane panelContenido) {
        this.panelContenido = panelContenido;
    }

    /**
     * Carga una vista FXML en el ContentPane del Shell.
     *
     * @param rutaFxml Ruta relativa al FXML (ej. "/fxml/dashboard-view.fxml").
     */
    public void navegar(String rutaFxml) {
        navegar(rutaFxml, null);
    }

    /**
     * Carga una vista FXML en el ContentPane del Shell y proporciona acceso
     * al controlador recién creado mediante un callback.
     * Útil para pasar datos a la vista destino antes de que se muestre.
     *
     * @param rutaFxml           Ruta relativa al FXML.
     * @param callbackControlador Consumer que recibe el controlador cargado, o null.
     */
    public void navegar(String rutaFxml, Consumer<Object> callbackControlador) {
        if (panelContenido == null) {
            LOGGER.severe("NavigationManager: panelContenido no está inicializado. "
                    + "Llama a setPanelContenido() desde ShellController.initialize().");
            return;
        }

        if (controladorActual instanceof Limpiable limpiable) {
            limpiable.limpiar();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Node vista = loader.load();

            controladorActual = loader.getController();

            if (controladorActual != null && !(controladorActual instanceof Limpiable)) {
                LOGGER.warning("NavigationManager: el controlador '"
                        + controladorActual.getClass().getSimpleName()
                        + "' no implementa Limpiable. Sus listeners no se limpiarán al navegar.");
            }

            if (callbackControlador != null) {
                callbackControlador.accept(controladorActual);
            }

            panelContenido.setCenter(vista);

        } catch (IOException e) {
            LOGGER.severe("NavigationManager: Error al cargar la vista '" + rutaFxml + "': " + e.getMessage());
            mostrarVistaError(rutaFxml);
        }
    }

    /**
     * Carga una vista de error genérica cuando falla la navegación principal.
     * Evita que el panel quede en blanco sin feedback al usuario.
     *
     * @param rutaFallida Ruta del FXML que falló, para loguear.
     */
    private void mostrarVistaError(String rutaFallida) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/error-view.fxml"));
            panelContenido.setCenter(loader.load());
        } catch (IOException ex) {
            LOGGER.severe("NavigationManager: También falló la vista de error. Ruta original: " + rutaFallida);
        }
    }

    /**
     * Limpia el controlador activo si implementa Limpiable.
     * Debe llamarse antes de destruir el Shell (ej. al cerrar sesión).
     */
    public void limpiarControladorActual() {
        if (controladorActual instanceof Limpiable limpiable) {
            limpiable.limpiar();
        }
        controladorActual = null;
    }
}
