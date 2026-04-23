package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.repository.EstadisticasRepository;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista del Dashboard.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class DashboardViewModel {

    private final EstadisticasRepository estadisticasRepository;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param estadisticasRepository Repositorio de datos analíticos.
     */
    public DashboardViewModel(EstadisticasRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    /**
     * Método placeholder para cargar las estadísticas iniciales de la vista.
     */
    public void cargarEstadisticas() {
        // La lógica reactiva y concurrente (Task de JavaFX) se implementará aquí
    }
}
