package com.gestorrh.escritorio.data.network.interceptor;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.data.network.dto.RespuestaErrorDTO;
import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Interceptor global para el manejo de errores HTTP.
 * Convierte las respuestas fallidas de la API REST en excepciones manejables (ApiException).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ErrorInterceptor implements Interceptor {

    private final Gson gson = new Gson();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (response.isSuccessful()) {
            return response;
        }

        String errorMsg = "Error desconocido de comunicación con el servidor.";
        int statusCode = response.code();

        ResponseBody body = response.body();
        if (body != null) {
            try {
                String errorJson = body.string();

                RespuestaErrorDTO errorDTO = gson.fromJson(errorJson, RespuestaErrorDTO.class);

                if (errorDTO != null && errorDTO.getMensaje() != null) {
                    errorMsg = errorDTO.getMensaje();
                }
            } catch (Exception e) {
                errorMsg = "Error en el servidor: " + response.message();
            }
        }

        throw new ApiException(errorMsg, statusCode);
    }
}
