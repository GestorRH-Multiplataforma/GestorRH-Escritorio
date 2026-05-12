package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionRegistroEmpresaDTO;
import com.gestorrh.escritorio.data.repository.AuthRepository;
import com.gestorrh.escritorio.data.repository.EmpresaRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la pantalla
 * de registro de nueva empresa. Encadena el registro con el auto-login
 * y expone el resultado mediante Properties reactivas.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class RegistroViewModel {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int PASSWORD_MIN_LENGTH = 8;

    private final EmpresaRepository empresaRepository;
    private final AuthRepository authRepository;

    private final StringProperty nombre           = new SimpleStringProperty("");
    private final StringProperty email            = new SimpleStringProperty("");
    private final StringProperty password         = new SimpleStringProperty("");
    private final StringProperty confirmarPassword = new SimpleStringProperty("");
    private final StringProperty direccion        = new SimpleStringProperty("");
    private final StringProperty telefono         = new SimpleStringProperty("");
    private final StringProperty mensajeError     = new SimpleStringProperty("");

    private final BooleanProperty registrando       = new SimpleBooleanProperty(false);
    private final BooleanProperty formularioValido  = new SimpleBooleanProperty(false);
    private final BooleanProperty autoLoginExitoso  = new SimpleBooleanProperty(false);
    private final BooleanProperty autoLoginFallido  = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible      = new SimpleBooleanProperty(false);
    private final BooleanProperty emailDuplicado    = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param empresaRepository Repositorio de datos de empresa.
     * @param authRepository    Repositorio de autenticación.
     */
    public RegistroViewModel(EmpresaRepository empresaRepository, AuthRepository authRepository) {
        this.empresaRepository = empresaRepository;
        this.authRepository = authRepository;
        configurarValidacionReactiva();
    }

    /**
     * Configura los listeners reactivos que recalculan {@code formularioValido}
     * ante cualquier cambio en los campos del formulario.
     */
    private void configurarValidacionReactiva() {
        nombre.addListener((obs, o, n)            -> recalcularFormularioValido());
        email.addListener((obs, o, n)             -> recalcularFormularioValido());
        password.addListener((obs, o, n)          -> recalcularFormularioValido());
        confirmarPassword.addListener((obs, o, n) -> recalcularFormularioValido());
        direccion.addListener((obs, o, n)         -> recalcularFormularioValido());
        telefono.addListener((obs, o, n)          -> recalcularFormularioValido());
    }

    /**
     * Recalcula si el formulario es válido:
     * todos los campos no vacíos, email con formato válido,
     * password con longitud mínima y coincidencia con confirmarPassword.
     */
    private void recalcularFormularioValido() {
        boolean valido = !nombre.get().isBlank()
                && !direccion.get().isBlank()
                && !telefono.get().isBlank()
                && EMAIL_PATTERN.matcher(email.get().trim()).matches()
                && password.get().length() >= PASSWORD_MIN_LENGTH
                && password.get().equals(confirmarPassword.get());
        formularioValido.set(valido);
    }

    /**
     * Ejecuta el flujo de registro y auto-login de forma asíncrona.
     * Primero registra la empresa, luego encadena el login con las mismas
     * credenciales y finalmente guarda la sesión en {@link SessionManager}.
     * Si el registro es exitoso pero el auto-login falla, activa
     * {@code autoLoginFallido} para que el controlador navegue al login manual.
     *
     * @return CompletableFuture que se completa cuando el flujo termina.
     */
    public CompletableFuture<Void> registrar() {
        limpiarError();
        registrando.set(true);
        emailDuplicado.set(false);

        PeticionRegistroEmpresaDTO dto = new PeticionRegistroEmpresaDTO(
                email.get().trim(),
                password.get(),
                nombre.get().trim(),
                direccion.get().trim(),
                telefono.get().trim()
        );

        return empresaRepository.registrar(dto)
                .thenCompose(empresa -> authRepository.login(email.get().trim(), password.get()))
                .thenAccept(loginResp -> Platform.runLater(() -> {
                    SessionManager.getInstance().saveSession(
                            loginResp.token(),
                            loginResp.id(),
                            loginResp.nombre()
                    );
                    registrando.set(false);
                    autoLoginExitoso.set(true);
                }))
                .exceptionally(ex -> {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    Platform.runLater(() -> {
                        registrando.set(false);

                        // Distinguir si el registro fue OK pero falló el auto-login
                        // En ese caso la causa NO es ApiException con 400/500
                        if (causa instanceof ApiException apiEx) {
                            if (apiEx.getMessage() != null
                                    && apiEx.getMessage().toLowerCase()
                                    .contains("email ya registrado")) {
                                emailDuplicado.set(true);
                                mostrarError(apiEx.getMessage());
                            } else if (apiEx.getStatusCode() == 0) {
                                // Error de red tras registro exitoso → auto-login fallido
                                autoLoginFallido.set(true);
                            } else {
                                mostrarError(apiEx.getMessage());
                            }
                        } else {
                            mostrarError(
                                    LanguageManager.getInstance().getString("error.unknown")
                            );
                        }
                    });
                    return null;
                });
    }

    /**
     * Muestra un mensaje de error y lo hace visible.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    public void mostrarError(String mensaje) {
        mensajeError.set(mensaje);
        errorVisible.set(true);
    }

    /**
     * Limpia el mensaje de error y lo oculta.
     */
    public void limpiarError() {
        mensajeError.set("");
        errorVisible.set(false);
        emailDuplicado.set(false);
    }

    /** @return Property del campo nombre. */
    public StringProperty nombreProperty()            { return nombre; }

    /** @return Property del campo email. */
    public StringProperty emailProperty()             { return email; }

    /** @return Property del campo password. */
    public StringProperty passwordProperty()          { return password; }

    /** @return Property del campo confirmar password. */
    public StringProperty confirmarPasswordProperty() { return confirmarPassword; }

    /** @return Property del campo dirección. */
    public StringProperty direccionProperty()         { return direccion; }

    /** @return Property del campo teléfono. */
    public StringProperty telefonoProperty()          { return telefono; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty()      { return mensajeError; }

    /** @return Property que indica si se está procesando el registro. */
    public BooleanProperty registrandoProperty()      { return registrando; }

    /** @return Property que indica si el formulario es válido para enviar. */
    public BooleanProperty formularioValidoProperty() { return formularioValido; }

    /** @return Property que se activa cuando el registro y auto-login son exitosos. */
    public BooleanProperty autoLoginExitosoProperty() { return autoLoginExitoso; }

    /** @return Property que se activa cuando el registro fue OK pero el auto-login falló. */
    public BooleanProperty autoLoginFallidoProperty() { return autoLoginFallido; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty()     { return errorVisible; }

    /** @return Property que indica si el error es por email duplicado. */
    public BooleanProperty emailDuplicadoProperty()   { return emailDuplicado; }
}
