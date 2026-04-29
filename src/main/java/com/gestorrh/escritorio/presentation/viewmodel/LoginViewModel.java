package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.repository.AuthRepository;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

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

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Intenta realizar el inicio de sesión.
     *
     * @return CompletableFuture con éxito o excepción.
     */
    public CompletableFuture<Void> login() {
        if (email.get().isBlank() || password.get().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("login.error.empty"));
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
}
