package com.gestorrh.escritorio.core.di;

import com.gestorrh.escritorio.config.ConfigManager;
import com.gestorrh.escritorio.data.network.*;
import com.gestorrh.escritorio.data.network.interceptor.AuthInterceptor;
import com.gestorrh.escritorio.data.network.interceptor.ErrorInterceptor;
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

    private static ServiceFactory instance;
    private final Retrofit retrofit;

    private EmpleadoService empleadoService;
    private EmpresaService empresaService;
    private AusenciaService ausenciaService;
    private TurnoService turnoService;
    private FichajeService fichajeService;
    private AsignacionTurnoService asignacionTurnoService;
    private EstadisticasService estadisticasService;
    private AutenticacionService autenticacionService;

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
                .client(customHttpClient) //
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * @return Instancia Singleton de ServiceFactory.
     */
    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public EmpleadoService getEmpleadoService() {
        if (empleadoService == null) {
            empleadoService = retrofit.create(EmpleadoService.class);
        }
        return empleadoService;
    }

    public EmpresaService getEmpresaService() {
        if (empresaService == null) {
            empresaService = retrofit.create(EmpresaService.class);
        }
        return empresaService;
    }

    public AusenciaService getAusenciaService() {
        if (ausenciaService == null) {
            ausenciaService = retrofit.create(AusenciaService.class);
        }
        return ausenciaService;
    }

    public TurnoService getTurnoService() {
        if (turnoService == null) {
            turnoService = retrofit.create(TurnoService.class);
        }
        return turnoService;
    }

    public FichajeService getFichajeService() {
        if (fichajeService == null) {
            fichajeService = retrofit.create(FichajeService.class);
        }
        return fichajeService;
    }

    public AsignacionTurnoService getAsignacionTurnoService() {
        if (asignacionTurnoService == null) {
            asignacionTurnoService = retrofit.create(AsignacionTurnoService.class);
        }
        return asignacionTurnoService;
    }

    public EstadisticasService getEstadisticasService() {
        if (estadisticasService == null) {
            estadisticasService = retrofit.create(EstadisticasService.class);
        }
        return estadisticasService;
    }

    public AutenticacionService getAutenticacionService() {
        if (autenticacionService == null) {
            autenticacionService = retrofit.create(AutenticacionService.class);
        }
        return autenticacionService;
    }
}
