package com.gestorrh.escritorio.core.di;

import com.gestorrh.escritorio.data.repository.*;

/**
 * Fábrica centralizada para la creación e inyección de Repositorios.
 * Implementa el patrón Singleton y se encarga de inyectar los servicios
 * necesarios obtenidos de la ServiceFactory.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class RepositoryFactory {

    private EmpleadoRepository empleadoRepository;
    private EmpresaRepository empresaRepository;
    private AusenciaRepository ausenciaRepository;
    private TurnoRepository turnoRepository;
    private FichajeRepository fichajeRepository;
    private AsignacionTurnoRepository asignacionTurnoRepository;
    private EstadisticasRepository estadisticasRepository;
    private AuthRepository authRepository;
    private ReporteRepository reporteRepository;

    private RepositoryFactory() {}

    /**
     * @return Instancia única de RepositoryFactory.
     */
    public static RepositoryFactory getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Clase interna estática que garantiza la inicialización lazy y thread-safe
     * del Singleton sin necesidad de sincronización explícita.
     */
    private static final class Holder {
        private static final RepositoryFactory INSTANCE = new RepositoryFactory();
    }

    /**
     * @return Instancia del repositorio de empleados.
     */
    public EmpleadoRepository getEmpleadoRepository() {
        if (empleadoRepository == null) {
            empleadoRepository = new EmpleadoRepository(ServiceFactory.getInstance().getEmpleadoService());
        }
        return empleadoRepository;
    }

    /**
     * @return Instancia del repositorio de empresas.
     */
    public EmpresaRepository getEmpresaRepository() {
        if (empresaRepository == null) {
            empresaRepository = new EmpresaRepository(ServiceFactory.getInstance().getEmpresaService());
        }
        return empresaRepository;
    }

    /**
     * @return Instancia del repositorio de ausencias.
     */
    public AusenciaRepository getAusenciaRepository() {
        if (ausenciaRepository == null) {
            ausenciaRepository = new AusenciaRepository(ServiceFactory.getInstance().getAusenciaService());
        }
        return ausenciaRepository;
    }

    /**
     * @return Instancia del repositorio de turnos.
     */
    public TurnoRepository getTurnoRepository() {
        if (turnoRepository == null) {
            turnoRepository = new TurnoRepository(ServiceFactory.getInstance().getTurnoService());
        }
        return turnoRepository;
    }

    /**
     * @return Instancia del repositorio de fichajes.
     */

    public FichajeRepository getFichajeRepository() {
        if (fichajeRepository == null) {
            fichajeRepository = new FichajeRepository(ServiceFactory.getInstance().getFichajeService());
        }
        return fichajeRepository;
    }

    /**
     * @return Instancia del repositorio de asignaciones de turno.
     */
    public AsignacionTurnoRepository getAsignacionTurnoRepository() {
        if (asignacionTurnoRepository == null) {
            asignacionTurnoRepository = new AsignacionTurnoRepository(ServiceFactory.getInstance().getAsignacionTurnoService());
        }
        return asignacionTurnoRepository;
    }

    /**
     * @return Instancia del repositorio de estadísticas.
     */
    public EstadisticasRepository getEstadisticasRepository() {
        if (estadisticasRepository == null) {
            estadisticasRepository = new EstadisticasRepository(ServiceFactory.getInstance().getEstadisticasService());
        }
        return estadisticasRepository;
    }

    /**
     * @return Instancia del repositorio de autenticación.
     */
    public AuthRepository getAuthRepository() {
        if (authRepository == null) {
            authRepository = new AuthRepository(ServiceFactory.getInstance().getAutenticacionService());
        }
        return authRepository;
    }

    /**
     * @return Instancia del repositorio de reportes.
     */
    public ReporteRepository getReporteRepository() {
        if (reporteRepository == null) {
            reporteRepository = new ReporteRepository(
                    ServiceFactory.getInstance().getReporteService());
        }
        return reporteRepository;
    }
}
