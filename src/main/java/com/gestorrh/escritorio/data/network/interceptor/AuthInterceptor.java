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

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (path.contains("/auth/login/empresa") || path.contains("/auth/registro/empresa")) {
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
