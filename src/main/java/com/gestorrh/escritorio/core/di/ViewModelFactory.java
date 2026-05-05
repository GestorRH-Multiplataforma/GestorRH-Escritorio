package com.gestorrh.escritorio.core.di;

import com.gestorrh.escritorio.presentation.viewmodel.*;

/**
 * Fábrica centralizada para la creación de ViewModels.
 * A diferencia de los repositorios, genera nuevas instancias (Prototype)
 * para asegurar que cada vista tenga un estado limpio y aislado.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class ViewModelFactory {

    private static ViewModelFactory instance;

    private ViewModelFactory() {}

    /**
     * @return Instancia única de ViewModelFactory.
     */
    public static synchronized ViewModelFactory getInstance() {
        if (instance == null) {
            instance = new ViewModelFactory();
        }
        return instance;
    }

    /**
     * Crea un ViewModel para el Dashboard inyectando su repositorio.
     *
     * @return Nueva instancia de DashboardViewModel.
     */
    public DashboardViewModel createDashboardViewModel() {
        return new DashboardViewModel(RepositoryFactory.getInstance().getEstadisticasRepository());
    }

    /**
     * Crea un ViewModel para la gestión de Empleados inyectando su repositorio.
     *
     * @return Nueva instancia de EmpleadoViewModel.
     */
    public EmpleadoViewModel createEmpleadoViewModel() {
        return new EmpleadoViewModel(RepositoryFactory.getInstance().getEmpleadoRepository());
    }

    /**
     * Crea un ViewModel para el formulario de alta y edición de empleados.
     * Se genera una nueva instancia por cada apertura del modal para garantizar
     * un estado limpio e independiente.
     *
     * @return Nueva instancia de EmpleadoFormViewModel.
     */
    public EmpleadoFormViewModel createEmpleadoFormViewModel() {
        return new EmpleadoFormViewModel(RepositoryFactory.getInstance().getEmpleadoRepository());
    }

    /**
     * Crea un ViewModel para la pantalla de Login inyectando su repositorio.
     *
     * @return Nueva instancia de LoginViewModel.
     */
    public LoginViewModel createLoginViewModel() {
        return new LoginViewModel(RepositoryFactory.getInstance().getAuthRepository());
    }

    /**
     * Crea un ViewModel para la pantalla de Configuración de empresa.
     * Se genera una nueva instancia por cada apertura de la vista para
     * garantizar un estado limpio e independiente.
     *
     * @return Nueva instancia de ConfiguracionViewModel.
     */
    public ConfiguracionViewModel createConfiguracionViewModel() {
        return new ConfiguracionViewModel(
                RepositoryFactory.getInstance().getEmpresaRepository()
        );
    }

    /**
     * Crea un ViewModel para la gestión del catálogo de turnos.
     *
     * @return Nueva instancia de TurnoViewModel.
     */
    public TurnoViewModel createTurnoViewModel() {
        return new TurnoViewModel(RepositoryFactory.getInstance().getTurnoRepository());
    }

    /**
     * Crea un ViewModel para el formulario de alta y edición de turnos.
     * Se genera una nueva instancia por cada apertura del modal para garantizar
     * un estado limpio e independiente.
     *
     * @return Nueva instancia de TurnoFormViewModel.
     */
    public TurnoFormViewModel createTurnoFormViewModel() {
        return new TurnoFormViewModel(RepositoryFactory.getInstance().getTurnoRepository());
    }

    /**
     * Crea un ViewModel para la pantalla de asignación de turnos en calendario.
     * Se genera una nueva instancia por cada apertura de la vista para
     * garantizar un estado limpio e independiente.
     *
     * @return Nueva instancia de AsignacionTurnosViewModel.
     */
    public AsignacionTurnosViewModel createAsignacionTurnosViewModel() {
        return new AsignacionTurnosViewModel(
                RepositoryFactory.getInstance().getAsignacionTurnoRepository(),
                RepositoryFactory.getInstance().getEmpleadoRepository(),
                RepositoryFactory.getInstance().getTurnoRepository(),
                RepositoryFactory.getInstance().getEmpresaRepository()
        );
    }
}