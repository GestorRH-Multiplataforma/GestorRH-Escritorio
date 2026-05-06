package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.KpisDTO;
import com.gestorrh.escritorio.data.repository.EstadisticasRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista del Dashboard.
 * Expone los KPIs como Properties reactivas para el binding con el controlador.
 *
 * @author Fco Javier García Cañero
 * @version 1.1
 */
public class DashboardViewModel {

    private final EstadisticasRepository estadisticasRepository;

    private final ObjectProperty<KpisDTO> kpis = new SimpleObjectProperty<>();
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final StringProperty mensajeError = new SimpleStringProperty("");
    private final BooleanProperty errorVisible = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección de dependencias.
     *
     * @param estadisticasRepository Repositorio de datos analíticos.
     */
    public DashboardViewModel(EstadisticasRepository estadisticasRepository) {
        this.estadisticasRepository = estadisticasRepository;
    }

    /**
     * Solicita los KPIs a la API de forma asíncrona y actualiza las Properties
     * reactivas con el resultado. Gestiona los estados de carga y error.
     */
    public void cargarKpis() {
        cargando.set(true);
        errorVisible.set(false);
        mensajeError.set("");

        estadisticasRepository.getKpis()
                .thenAccept(resultado -> Platform.runLater(() -> {
                    kpis.set(resultado);
                    cargando.set(false);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        cargando.set(false);
                        mensajeError.set(
                                LanguageManager.getInstance().getString("dashboard.error.cargaKpis")
                        );
                        errorVisible.set(true);
                    });
                    return null;
                });
    }

    /**
     * @return Property con el objeto KpisDTO cargado desde la API.
     */
    public ObjectProperty<KpisDTO> kpisProperty() { return kpis; }

    /**
     * @return Property que indica si se está realizando una petición en curso.
     */
    public BooleanProperty cargandoProperty() { return cargando; }

    /**
     * @return Property con el mensaje de error a mostrar en la vista.
     */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /**
     * @return Property que controla la visibilidad del panel de error.
     */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
