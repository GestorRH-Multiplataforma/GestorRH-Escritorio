package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.AusenciaService;
import com.gestorrh.escritorio.data.network.dto.PeticionRevisionAusenciaDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaAusenciaDTO;
import com.gestorrh.escritorio.core.exception.ApiException;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Ausencias.
 * Actúa como mediador entre la capa de red y la lógica de negocio,
 * exponiendo operaciones asíncronas mediante CompletableFuture.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class AusenciaRepository extends BaseRepository {

    private final AusenciaService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para la entidad Ausencia.
     */
    public AusenciaRepository(AusenciaService service) {
        this.service = service;
    }

    /**
     * Obtiene las ausencias filtradas por estado de forma asíncrona.
     *
     * @param estado Estado a filtrar: SOLICITADA, APROBADA o RECHAZADA.
     * @return CompletableFuture con la lista de ausencias o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaAusenciaDTO>> listar(String estado) {
        return executeAsync(service.listarPorEstado(estado));
    }

    /**
     * Obtiene los tipos de ausencia disponibles de forma asíncrona.
     *
     * @return CompletableFuture con la lista de tipos o ApiException si falla.
     */
    public CompletableFuture<List<String>> getTipos() {
        return executeAsync(service.listarTipos());
    }

    /**
     * Obtiene los estados de ausencia disponibles de forma asíncrona.
     *
     * @return CompletableFuture con la lista de estados o ApiException si falla.
     */
    public CompletableFuture<List<String>> getEstados() {
        return executeAsync(service.listarEstados());
    }

    /**
     * Aprueba o rechaza una ausencia de forma asíncrona.
     *
     * @param id      Identificador único de la ausencia.
     * @param dto     DTO con el nuevo estado y observaciones opcionales.
     * @return CompletableFuture con la ausencia actualizada o ApiException si falla.
     */
    public CompletableFuture<RespuestaAusenciaDTO> revisar(Long id, PeticionRevisionAusenciaDTO dto) {
        return executeAsync(service.revisar(id, dto));
    }

    /**
     * Descarga el justificante de una ausencia y lo guarda en el directorio destino.
     * Usa @Streaming para no cargar el fichero completo en memoria de una vez.
     *
     * @param nombreArchivo Nombre del archivo tal como viene en el campo justificante del DTO.
     * @param destino       Directorio donde se guardará el archivo (ej. ~/Descargas/).
     * @return CompletableFuture con el File guardado en disco o ApiException si falla.
     */
    public CompletableFuture<File> descargarJustificante(String nombreArchivo, Path destino) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<ResponseBody> response = service.descargarJustificante(nombreArchivo).execute();

                if (!response.isSuccessful() || response.body() == null) {
                    throw new ApiException("Error al descargar el justificante", response.code(), "error.unknown");
                }

                Files.createDirectories(destino);
                Path rutaArchivo = destino.resolve(nombreArchivo);

                try (ResponseBody body = response.body();
                     InputStream input = body.byteStream();
                     OutputStream output = Files.newOutputStream(rutaArchivo)) {
                    byte[] buffer = new byte[8192];
                    int bytesLeidos;
                    while ((bytesLeidos = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesLeidos);
                    }
                }

                return rutaArchivo.toFile();

            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                throw new ApiException("error.timeout", 0, "error.timeout");
            }
        });
    }
}
