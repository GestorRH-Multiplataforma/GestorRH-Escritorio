package com.gestorrh.escritorio.core.di;

import com.gestorrh.escritorio.config.ConfigManager;
import com.gestorrh.escritorio.data.network.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fábrica centralizada para los servicios de red (Retrofit).
 * Implementa el patrón Singleton para garantizar una única conexión HTTP base
 * y reutilizar las interfaces generadas, optimizando el uso de memoria.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
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

    /**
     * Constructor privado. Inicializa el cliente Retrofit con la configuración global
     * obtenida desde el ConfigManager.
     */
    private ServiceFactory() {
        String baseUrl = ConfigManager.getInstance().getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        this.retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Devuelve la instancia única de ServiceFactory.
     *
     * @return Instancia Singleton.
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
}
