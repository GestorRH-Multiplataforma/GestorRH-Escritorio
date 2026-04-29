package com.gestorrh.escritorio.data.network.interceptor;

import com.gestorrh.escritorio.core.security.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Interceptor encargado de inyectar el token JWT en las cabeceras HTTP.
 * Excluye dinámicamente las rutas de autenticación públicas.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AuthInterceptor implements Interceptor {

    private static final java.util.Set<String> RUTAS_PUBLICAS = java.util.Set.of(
            "/api/auth/login-empresa",
            "/api/empresas/registro"
    );
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (RUTAS_PUBLICAS.stream().anyMatch(path::equals)) {
            return chain.proceed(request);
        }

        String token = SessionManager.getInstance().getToken();

        if (token != null && !token.isBlank()) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }
}
