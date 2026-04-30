package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.repository.AuthRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * ViewModel para la pantalla de Login.
 * Gestiona el estado de los campos, la carga, el error inline
 * y la comunicación con el repositorio de autenticación.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class LoginViewModel {

    private final AuthRepository authRepository;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty errorVisible = new SimpleBooleanProperty(false);

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int PASSWORD_MIN_LENGTH = 6;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param authRepository Repositorio de autenticación.
     */
    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Intenta realizar el inicio de sesión de forma asíncrona.
     * Valida formato de email y longitud mínima de contraseña antes de llamar a la API.
     *
     * @return CompletableFuture que se completa con éxito o falla con excepción.
     */
    public CompletableFuture<Void> login() {
        clearError();

        if (email.get().isBlank() || password.get().isBlank()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("login.error.empty"));
        }
        if (!EMAIL_PATTERN.matcher(email.get().trim()).matches()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("login.error.email.invalid"));
        }
        if (password.get().length() < PASSWORD_MIN_LENGTH) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("login.error.password.short"));
        }

        Platform.runLater(() -> loading.set(true));

        return authRepository.login(email.get().trim(), password.get())
                .thenAccept(respuesta -> SessionManager.getInstance().saveSession(
                        respuesta.token(),
                        respuesta.id(),
                        respuesta.nombre()
                ))
                .whenComplete((res, ex) -> Platform.runLater(() -> loading.set(false)));
    }

    /**
     * Procesa un error y lo expone como mensaje inline en la vista.
     * Resuelve la clave i18n si el error la incluye, o usa el mensaje directo de la API.
     *
     * @param cause Excepción recibida desde el Controller.
     */
    public void handleError(Throwable cause) {
        LanguageManager lang = LanguageManager.getInstance();
        String message;

        if (cause instanceof ApiException apiEx && apiEx.hasI18nKey()) {
            message = lang.getString(apiEx.getI18nKey());
        } else if (cause instanceof IllegalArgumentException && cause.getMessage() != null) {
            message = lang.getString(cause.getMessage());
        } else {
            message = (cause != null && cause.getMessage() != null)
                    ? cause.getMessage()
                    : lang.getString("error.unknown");
        }

        setError(message);
    }

    /**
     * Establece el mensaje de error y lo hace visible en la vista.
     *
     * @param message Mensaje de error ya traducido.
     */
    private void setError(String message) {
        errorMessage.set(message);
        errorVisible.set(true);
    }

    /**
     * Limpia el error inline. Se llama al escribir en cualquier campo
     * o al iniciar un nuevo intento de login.
     */
    public void clearError() {
        errorMessage.set("");
        errorVisible.set(false);
    }

    /**
     * Limpia la contraseña de memoria por seguridad.
     * Debe llamarse tras login exitoso y tras login fallido.
     */
    public void clearPassword() {
        password.set("");
    }

    /** @return Property del campo email. */
    public StringProperty emailProperty() { return email; }

    /** @return Property del campo contraseña. */
    public StringProperty passwordProperty() { return password; }

    /** @return Property del estado de carga. */
    public BooleanProperty loadingProperty() { return loading; }

    /** @return Property del mensaje de error inline. */
    public StringProperty errorMessageProperty() { return errorMessage; }

    /** @return Property de visibilidad del error inline. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
