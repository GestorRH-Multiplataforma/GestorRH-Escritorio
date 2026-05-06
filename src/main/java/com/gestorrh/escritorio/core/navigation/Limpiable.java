package com.gestorrh.escritorio.core.navigation;

/**
 * Interfaz que deben implementar los controladores FXML que registran
 * listeners externos (idioma, eventos, etc.) para que el NavigationManager
 * pueda limpiarlos automáticamente al navegar a otra vista.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public interface Limpiable {
    void limpiar();
}
