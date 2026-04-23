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

    private static RepositoryFactory instance;

    private EmpleadoRepository empleadoRepository;
    private EmpresaRepository empresaRepository;
    private AusenciaRepository ausenciaRepository;
    private TurnoRepository turnoRepository;
    private FichajeRepository fichajeRepository;
    private AsignacionTurnoRepository asignacionTurnoRepository;
    private EstadisticasRepository estadisticasRepository;

    private RepositoryFactory() {}

    /**
     * @return Instancia única de RepositoryFactory.
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public EmpleadoRepository getEmpleadoRepository() {
        if (empleadoRepository == null) {
            empleadoRepository = new EmpleadoRepository(ServiceFactory.getInstance().getEmpleadoService());
        }
        return empleadoRepository;
    }

    public EmpresaRepository getEmpresaRepository() {
        if (empresaRepository == null) {
            empresaRepository = new EmpresaRepository(ServiceFactory.getInstance().getEmpresaService());
        }
        return empresaRepository;
    }

    public AusenciaRepository getAusenciaRepository() {
        if (ausenciaRepository == null) {
            ausenciaRepository = new AusenciaRepository(ServiceFactory.getInstance().getAusenciaService());
        }
        return ausenciaRepository;
    }

    public TurnoRepository getTurnoRepository() {
        if (turnoRepository == null) {
            turnoRepository = new TurnoRepository(ServiceFactory.getInstance().getTurnoService());
        }
        return turnoRepository;
    }

    public FichajeRepository getFichajeRepository() {
        if (fichajeRepository == null) {
            fichajeRepository = new FichajeRepository(ServiceFactory.getInstance().getFichajeService());
        }
        return fichajeRepository;
    }

    public AsignacionTurnoRepository getAsignacionTurnoRepository() {
        if (asignacionTurnoRepository == null) {
            asignacionTurnoRepository = new AsignacionTurnoRepository(ServiceFactory.getInstance().getAsignacionTurnoService());
        }
        return asignacionTurnoRepository;
    }

    public EstadisticasRepository getEstadisticasRepository() {
        if (estadisticasRepository == null) {
            estadisticasRepository = new EstadisticasRepository(ServiceFactory.getInstance().getEstadisticasService());
        }
        return estadisticasRepository;
    }
}
