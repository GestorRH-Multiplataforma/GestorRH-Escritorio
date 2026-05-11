package com.gestorrh.escritorio.data.network.interceptor;

import com.gestorrh.escritorio.core.security.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Set;

/**
 * Interceptor encargado de inyectar el token JWT en las cabeceras HTTP.
 * Excluye dinámicamente las rutas de autenticación públicas usando
 * coincidencia por sufijo para mayor robustez ante cambios de versión en la API.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class AuthInterceptor implements Interceptor {

    private static final Set<String> SUFIJOS_RUTAS_PUBLICAS = Set.of(
            "/api/auth/login-empresa",
            "/api/empresas/registro"
    );

    /**
     * Intercepta cada petición HTTP y añade el token JWT en la cabecera
     * Authorization si la ruta no es pública.
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Respuesta HTTP tras procesar la petición.
     * @throws IOException Si ocurre un error de red durante la ejecución.
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (esRutaPublica(path)) {
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

    /**
     * Determina si una ruta es pública comprobando si termina con alguno
     * de los sufijos registrados. Esto la hace robusta ante prefijos de
     * versión como /v1/, /v2/, etc.
     *
     * @param path Ruta codificada de la petición.
     * @return true si la ruta es pública y no requiere token JWT.
     */
    private boolean esRutaPublica(String path) {
        return SUFIJOS_RUTAS_PUBLICAS.stream().anyMatch(path::endsWith);
    }
}
