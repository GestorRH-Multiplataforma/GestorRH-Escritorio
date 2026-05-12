package com.gestorrh.escritorio.core.di;

import com.gestorrh.escritorio.config.ConfigManager;
import com.gestorrh.escritorio.data.network.interceptor.AuthInterceptor;
import com.gestorrh.escritorio.data.network.interceptor.ErrorInterceptor;
import com.gestorrh.escritorio.data.network.service.*;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Fábrica centralizada para los servicios de red (Retrofit).
 * Implementa el patrón Singleton e inyecta la seguridad (JWT) y el
 * manejo global de errores mediante OkHttpClient.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class ServiceFactory {

    private final Retrofit retrofit;

    private final EmpleadoService empleadoService;
    private final EmpresaService empresaService;
    private final AusenciaService ausenciaService;
    private final TurnoService turnoService;
    private final FichajeService fichajeService;
    private final AsignacionTurnoService asignacionTurnoService;
    private final EstadisticasService estadisticasService;
    private final AutenticacionService autenticacionService;
    private final ReporteService reporteService;

    /**
     * Constructor privado. Inicializa el cliente Retrofit con la configuración global,
     * interceptores de seguridad y control de errores.
     */
    private ServiceFactory() {
        String baseUrl = ConfigManager.getInstance().getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor())
                .addInterceptor(new ErrorInterceptor());

        if (ConfigManager.getInstance().isDev()) {
            httpClientBuilder.addNetworkInterceptor(chain -> {
                okhttp3.Request request = chain.request();
                java.util.logging.Logger logger = java.util.logging.Logger.getLogger("GestorRH.HTTP");
                logger.info("--> " + request.method() + " " + request.url());
                long t1 = System.nanoTime();
                okhttp3.Response response = chain.proceed(request);
                long t2 = System.nanoTime();
                logger.info("<-- " + response.code() + " " + response.request().url()
                        + " (" + (t2 - t1) / 1_000_000 + "ms)");
                return response;
            });
        }

        OkHttpClient customHttpClient = httpClientBuilder.build();

        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(customHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.empleadoService = retrofit.create(EmpleadoService.class);
        this.empresaService = retrofit.create(EmpresaService.class);
        this.ausenciaService = retrofit.create(AusenciaService.class);
        this.turnoService = retrofit.create(TurnoService.class);
        this.fichajeService = retrofit.create(FichajeService.class);
        this.asignacionTurnoService = retrofit.create(AsignacionTurnoService.class);
        this.estadisticasService = retrofit.create(EstadisticasService.class);
        this.autenticacionService = retrofit.create(AutenticacionService.class);
        this.reporteService = retrofit.create(ReporteService.class);
    }

    /**
     * @return Instancia Singleton de ServiceFactory.
     */
    public static ServiceFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Clase interna estática que garantiza la inicialización lazy y thread-safe
     * del Singleton sin necesidad de sincronización explícita.
     */
    private static final class Holder {
        private static final ServiceFactory INSTANCE = new ServiceFactory();
    }

    /**
     * @return Servicio de red Retrofit para la entidad Empleado.
     */
    public EmpleadoService getEmpleadoService() { return empleadoService; }

    /**
     * @return Servicio de red Retrofit para la entidad Empresa.
     */
    public EmpresaService getEmpresaService() { return empresaService; }

    /**
     * @return Servicio de red Retrofit para la entidad Ausencia.
     */
    public AusenciaService getAusenciaService() { return ausenciaService; }

    /**
     * @return Servicio de red Retrofit para la entidad Turno.
     */
    public TurnoService getTurnoService() { return turnoService; }

    /**
     * @return Servicio de red Retrofit para la entidad Fichaje.
     */
    public FichajeService getFichajeService() { return fichajeService; }

    /**
     * @return Servicio de red Retrofit para la entidad AsignacionTurno.
     */
    public AsignacionTurnoService getAsignacionTurnoService() { return asignacionTurnoService; }

    /**
     * @return Servicio de red Retrofit para la entidad Estadisticas.
     */
    public EstadisticasService getEstadisticasService() { return estadisticasService; }

    /**
     * @return Servicio de red Retrofit para la autenticación.
     */
    public AutenticacionService getAutenticacionService() { return autenticacionService; }

    /**
     * @return Servicio de red Retrofit para los endpoints de reportes.
     */
    public ReporteService getReporteService() { return reporteService; }
}
