package com.gestorrh.escritorio.data.network.interceptor;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaErrorDTO;
import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Interceptor global para el manejo de errores HTTP.
 * Convierte las respuestas fallidas de la API REST en excepciones manejables (ApiException).
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class ErrorInterceptor implements Interceptor {

    private static final Logger LOGGER = Logger.getLogger(ErrorInterceptor.class.getName());
    private final Gson gson = new Gson();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (response.isSuccessful()) {
            return response;
        }

        int statusCode = response.code();
        String errorMsg = extraerMensajeError(response, statusCode);

        if (statusCode == 401) {
            SessionManager.getInstance().clearSession();
        }

        throw new ApiException(errorMsg, statusCode);
    }

    /**
     * Extrae el mensaje de error del cuerpo de la respuesta de forma segura.
     * Si el body ya fue consumido, está vacío o no es parseable como JSON,
     * devuelve un mensaje genérico en lugar de lanzar una excepción secundaria.
     *
     * @param response   Respuesta HTTP fallida.
     * @param statusCode Código de estado HTTP.
     * @return Mensaje de error extraído o mensaje genérico si no se puede extraer.
     */
    private String extraerMensajeError(Response response, int statusCode) {
        ResponseBody body = response.body();

        if (body == null) {
            LOGGER.warning("ErrorInterceptor: respuesta " + statusCode + " sin body.");
            return "Error " + statusCode + " sin detalle del servidor.";
        }

        try {
            String errorJson = body.string();

            if (errorJson == null || errorJson.isBlank()) {
                LOGGER.warning("ErrorInterceptor: body vacío para respuesta " + statusCode);
                return "Error " + statusCode + " sin detalle del servidor.";
            }

            RespuestaErrorDTO errorDTO = gson.fromJson(errorJson, RespuestaErrorDTO.class);

            if (errorDTO != null && errorDTO.mensaje() != null && !errorDTO.mensaje().isBlank()) {
                return errorDTO.mensaje();
            }

            LOGGER.warning("ErrorInterceptor: body no parseable como RespuestaErrorDTO: "
                    + errorJson);
            return "Error en el servidor: " + response.message();

        } catch (Exception e) {
            LOGGER.warning("ErrorInterceptor: excepción al leer body: " + e.getMessage());
            return "Error en el servidor: " + response.message();
        }
    }
}
