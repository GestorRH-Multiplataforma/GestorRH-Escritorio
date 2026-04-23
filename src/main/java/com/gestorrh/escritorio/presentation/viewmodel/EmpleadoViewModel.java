package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.repository.EmpleadoRepository;

/**
 * ViewModel encargado de gestionar el estado y la lógica del directorio de empleados.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadoViewModel {

    private final EmpleadoRepository empleadoRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param empleadoRepository Repositorio de datos de empleados.
     */
    public EmpleadoViewModel(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }
}
