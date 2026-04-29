package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.repository.AuthRepository;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import java.util.regex.Pattern;

/**
 * ViewModel para la pantalla de Login.
 * Gestiona el estado de los campos, la carga y la comunicación con el repositorio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class LoginViewModel {

    private final AuthRepository authRepository;

    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int PASSWORD_MIN_LENGTH = 6;

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Intenta realizar el inicio de sesión.
     * Valida formato de email y longitud mínima de contraseña antes de llamar a la API.
     *
     * @return CompletableFuture con éxito o excepción.
     * @throws IllegalArgumentException si los campos están vacíos, el email es inválido
     *                                  o la contraseña es demasiado corta.
     */
    public CompletableFuture<Void> login() {
        if (email.get().isBlank() || password.get().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("login.error.empty"));
        }
        if (!EMAIL_PATTERN.matcher(email.get().trim()).matches()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("login.error.email.invalid"));
        }
        if (password.get().length() < PASSWORD_MIN_LENGTH) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("login.error.password.short"));
        }

        loading.set(true);

        return authRepository.login(email.get(), password.get())
                .thenAccept(respuesta -> {
                    SessionManager.getInstance().saveSession(
                            respuesta.token(),
                            respuesta.id(),
                            respuesta.nombre()
                    );
                })
                .whenComplete((res, ex) -> Platform.runLater(() -> loading.set(false)));
    }

    public StringProperty emailProperty() { return email; }
    public StringProperty passwordProperty() { return password; }
    public BooleanProperty loadingProperty() { return loading; }

    /**
     * Limpia la contraseña de memoria por seguridad.
     * Debe llamarse tras login exitoso y tras login fallido.
     */
    public void clearPassword() {
        password.set("");
    }
}
