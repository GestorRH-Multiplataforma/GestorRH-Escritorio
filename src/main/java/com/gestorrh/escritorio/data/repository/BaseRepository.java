package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.core.exception.ApiException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Clase base para todos los repositorios de la aplicación.
 * Centraliza el patrón de ejecución asíncrona de llamadas Retrofit,
 * evitando duplicación de la gestión de errores en cada repositorio.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public abstract class BaseRepository {

    /**
     * Ejecuta una llamada Retrofit de forma asíncrona en un hilo de background.
     * Gestiona de forma centralizada los errores de red y de la API.
     *
     * @param <T>  Tipo del cuerpo de la respuesta esperada.
     * @param call La llamada Retrofit a ejecutar.
     * @return CompletableFuture que se completa con el cuerpo de la respuesta,
     *         o falla con ApiException si hay error de red o de la API.
     */
    protected <T> CompletableFuture<T> executeAsync(Call<T> call) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<T> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    return response.body();
                }
                throw new ApiException("error.unknown", response.code(), "error.unknown");
            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                throw new ApiException("error.timeout", 0, "error.timeout");
            }
        });
    }
}
