package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.core.exception.ApiException;
import com.gestorrh.escritorio.data.network.ReporteService;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteDetalleDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteResumenDTO;
import okhttp3.ResponseBody;
import retrofit2.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Repositorio encargado de la obtención de datos de informes de control horario.
 * Gestiona tanto la previsualización en formato JSON como la descarga de documentos
 * PDF a disco, delegando en {@link ReporteService} para las llamadas de red.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ReporteRepository extends BaseRepository {

    private static final ExecutorService IO_EXECUTOR =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "gestorrh-io-" + r.hashCode());
                t.setDaemon(true);
                return t;
            });

    private final ReporteService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit para los endpoints de reportes.
     */
    public ReporteRepository(ReporteService service) {
        this.service = service;
    }

    /**
     * Obtiene el reporte detallado de fichajes en formato JSON de forma asíncrona.
     * Usado para poblar la tabla de previsualización en la vista.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd).
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd).
     * @param idEmpleado  Identificador del empleado a filtrar. Null devuelve todos.
     * @return CompletableFuture con la lista de registros detallados o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaReporteDetalleDTO>> obtenerDetalle(
            String fechaInicio, String fechaFin, Long idEmpleado) {
        return executeAsync(service.obtenerDetalle(fechaInicio, fechaFin, idEmpleado));
    }

    /**
     * Obtiene el reporte resumido por empleado en formato JSON de forma asíncrona.
     * Usado para poblar la tabla de previsualización en la vista.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd).
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd).
     * @param idEmpleado  Identificador del empleado a filtrar. Null devuelve todos.
     * @return CompletableFuture con la lista de registros resumidos o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaReporteResumenDTO>> obtenerResumen(
            String fechaInicio, String fechaFin, Long idEmpleado) {
        return executeAsync(service.obtenerResumen(fechaInicio, fechaFin, idEmpleado));
    }

    /**
     * Descarga el informe detallado en PDF y lo escribe en la ruta indicada de forma asíncrona.
     * Usa streaming para evitar cargar el documento completo en memoria.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd).
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd).
     * @param idEmpleado  Identificador del empleado a filtrar. Null genera para todos.
     * @param destino     Ruta completa del archivo de destino en disco.
     * @return CompletableFuture que se completa con null tras la escritura exitosa.
     */
    public CompletableFuture<Void> descargarPdfDetalle(
            String fechaInicio, String fechaFin, Long idEmpleado, Path destino) {
        return descargarPdf(
                service.descargarPdfDetalle(fechaInicio, fechaFin, idEmpleado),
                destino);
    }

    /**
     * Descarga el informe resumido en PDF y lo escribe en la ruta indicada de forma asíncrona.
     * Usa streaming para evitar cargar el documento completo en memoria.
     *
     * @param fechaInicio Fecha de inicio del rango en formato ISO (yyyy-MM-dd).
     * @param fechaFin    Fecha de fin del rango en formato ISO (yyyy-MM-dd).
     * @param idEmpleado  Identificador del empleado a filtrar. Null genera para todos.
     * @param destino     Ruta completa del archivo de destino en disco.
     * @return CompletableFuture que se completa con null tras la escritura exitosa.
     */
    public CompletableFuture<Void> descargarPdfResumen(
            String fechaInicio, String fechaFin, Long idEmpleado, Path destino) {
        return descargarPdf(
                service.descargarPdfResumen(fechaInicio, fechaFin, idEmpleado),
                destino);
    }

    /**
     * Método interno que ejecuta la descarga de un PDF en el pool de I/O y lo escribe a disco.
     * Extrae el stream del {@link ResponseBody} y lo copia directamente a la ruta destino
     * usando {@link Files#copy} con reemplazo atómico.
     *
     * @param call    Llamada Retrofit con streaming activado.
     * @param destino Ruta completa del archivo de destino en disco.
     * @return CompletableFuture que se completa con null tras la escritura exitosa.
     */
    private CompletableFuture<Void> descargarPdf(
            retrofit2.Call<ResponseBody> call, Path destino) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<ResponseBody> response = call.execute();
                if (!response.isSuccessful() || response.body() == null) {
                    throw new ApiException(
                            "Error al descargar el informe PDF",
                            response.code(),
                            "error.unknown");
                }
                try (ResponseBody body = response.body()) {
                    Files.copy(body.byteStream(), destino,
                            StandardCopyOption.REPLACE_EXISTING);
                }
                return null;
            } catch (ApiException e) {
                throw e;
            } catch (IOException e) {
                throw new ApiException("error.timeout", 0, "error.timeout");
            }
        }, IO_EXECUTOR);
    }
}
