package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.data.network.dto.RespuestaLoginDTO;
import com.gestorrh.escritorio.data.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoginViewModel.
 * Cubre validaciones de campos, login exitoso y escenarios de error de la API.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class LoginViewModelTest {

    @Mock
    private AuthRepository authRepository;

    private LoginViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new LoginViewModel(authRepository);
    }

    @Test
    @DisplayName("Login falla si los campos están vacíos")
    void login_camposVacios_lanzaExcepcion() {
        viewModel.emailProperty().set("");
        viewModel.passwordProperty().set("");

        CompletableFuture<Void> result = viewModel.login();

        ExecutionException ex = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("login.error.empty", ex.getCause().getMessage());
        verifyNoInteractions(authRepository);
    }

    @Test
    @DisplayName("Login falla si el email tiene formato inválido")
    void login_emailInvalido_lanzaExcepcion() {
        viewModel.emailProperty().set("no-es-un-email");
        viewModel.passwordProperty().set("password123");

        CompletableFuture<Void> result = viewModel.login();

        ExecutionException ex = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("login.error.email.invalid", ex.getCause().getMessage());
        verifyNoInteractions(authRepository);
    }

    @Test
    @DisplayName("Login falla si la contraseña es demasiado corta")
    void login_passwordCorta_lanzaExcepcion() {
        viewModel.emailProperty().set("test@empresa.com");
        viewModel.passwordProperty().set("123");

        CompletableFuture<Void> result = viewModel.login();

        ExecutionException ex = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertEquals("login.error.password.short", ex.getCause().getMessage());
        verifyNoInteractions(authRepository);
    }

    @Test
    @DisplayName("Login exitoso guarda la sesión y limpia la contraseña")
    void login_credencialesCorrectas_guardaSesionYLimpiaPassword() throws Exception {
        viewModel.emailProperty().set("test@empresa.com");
        viewModel.passwordProperty().set("password123");

        RespuestaLoginDTO respuesta = new RespuestaLoginDTO("token-jwt", "EMPRESA", 1L, "Empresa Test");
        when(authRepository.login("test@empresa.com", "password123"))
                .thenReturn(CompletableFuture.completedFuture(respuesta));

        viewModel.login();

        verify(authRepository).login("test@empresa.com", "password123");
    }

    @Test
    @DisplayName("Login con error 401 propaga ApiException con código 401")
    void login_error401_propagaApiException() {
        viewModel.emailProperty().set("test@empresa.com");
        viewModel.passwordProperty().set("password123");

        ApiException error401 = new ApiException("No autorizado", 401);
        when(authRepository.login(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(error401));

        CompletableFuture<Void> result = viewModel.login();

        ExecutionException ex = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(ApiException.class, ex.getCause());
        assertEquals(401, ((ApiException) ex.getCause()).getStatusCode());
    }

    @Test
    @DisplayName("Login con error 500 propaga ApiException con código 500")
    void login_error500_propagaApiException() {
        viewModel.emailProperty().set("test@empresa.com");
        viewModel.passwordProperty().set("password123");

        ApiException error500 = new ApiException("Error interno", 500);
        when(authRepository.login(anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(error500));

        CompletableFuture<Void> result = viewModel.login();

        ExecutionException ex = assertThrows(ExecutionException.class, result::get);
        assertInstanceOf(ApiException.class, ex.getCause());
        assertEquals(500, ((ApiException) ex.getCause()).getStatusCode());
    }
}
