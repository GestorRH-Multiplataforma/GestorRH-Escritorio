package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.DatoGraficoDTO;
import com.gestorrh.escritorio.data.network.dto.KpisDTO;
import com.gestorrh.escritorio.data.repository.EstadisticasRepository;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista del Dashboard.
 * Expone los KPIs y el ranking de retrasos como Properties reactivas para el binding
 * con el controlador.
 *
 * @author Fco Javier García Cañero
 * @version 1.3
 */
public class DashboardViewModel {

    private static final int MAX_TOP_RETRASOS = 5;
    private static final int UMBRAL_ADVERTENCIA_RETRASOS = 5;

    private final EstadisticasRepository estadisticasRepository;

    private final ObjectProperty<KpisDTO> kpis = new SimpleObjectProperty<>();
    private final ObservableList<DatoGraficoDTO> topRetrasos = FXCollections.observableArrayList();
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty topRetrasosCargando = new SimpleBooleanProperty(false);
    private final StringProperty mensajeError = new SimpleStringProperty("");
    private final BooleanProperty errorVisible = new SimpleBooleanProperty(false);
    private final BooleanProperty topRetrasosVacio = new SimpleBooleanProperty(false);
    private final ObservableList<DatoGraficoDTO> ausenciasPorEstado = FXCollections.observableArrayList();
    private final BooleanProperty ausenciasEstadoCargando = new SimpleBooleanProperty(false);
    private final BooleanProperty ausenciasEstadoVacio = new SimpleBooleanProperty(false);
    private final ObservableList<DatoGraficoDTO> empleadosPorDepartamento = FXCollections.observableArrayList();
    private final BooleanProperty empleadosDepartamentoCargando = new SimpleBooleanProperty(false);
    private final BooleanProperty empleadosDepartamentoVacio = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoTodo = new SimpleBooleanProperty(false);

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
     * reactivas con el resultado.
     *
     * @return CompletableFuture que se completa cuando la carga finaliza,
     *         con éxito o con error.
     */
    public CompletableFuture<Void> cargarKpis() {
        cargando.set(true);
        errorVisible.set(false);
        mensajeError.set("");

        return estadisticasRepository.getKpis()
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
     * Solicita el ranking de retrasos a la API de forma asíncrona y actualiza
     * la lista observable. Limita los resultados a {@value MAX_TOP_RETRASOS} elementos.
     *
     * @return CompletableFuture que se completa cuando la carga finaliza,
     *         con éxito o con error.
     */
    public CompletableFuture<Void> cargarTopRetrasos() {
        topRetrasosCargando.set(true);

        return estadisticasRepository.getTopRetrasos()
                .thenAccept(lista -> Platform.runLater(() -> {
                    List<DatoGraficoDTO> truncada = lista.subList(0, Math.min(lista.size(), MAX_TOP_RETRASOS));
                    List<DatoGraficoDTO> rellena = new java.util.ArrayList<>(truncada);
                    while (rellena.size() < MAX_TOP_RETRASOS) {
                        rellena.add(null);
                    }
                    topRetrasos.setAll(rellena);
                    topRetrasosVacio.set(truncada.isEmpty());
                    topRetrasosCargando.set(false);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        topRetrasos.clear();
                        topRetrasosVacio.set(true);
                        topRetrasosCargando.set(false);
                    });
                    return null;
                });
    }

    /**
     * Solicita la distribución de empleados por departamento a la API de forma asíncrona.
     * Un fallo en este método no afecta a las demás cargas del Dashboard.
     *
     * @return CompletableFuture que se completa cuando la carga finaliza,
     *         con éxito o con error.
     */
    public CompletableFuture<Void> cargarEmpleadosPorDepartamento() {
        empleadosDepartamentoCargando.set(true);

        return estadisticasRepository.getEmpleadosPorDepartamento()
                .thenAccept(lista -> Platform.runLater(() -> {
                    empleadosPorDepartamento.setAll(lista);
                    empleadosDepartamentoVacio.set(lista.isEmpty());
                    empleadosDepartamentoCargando.set(false);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        empleadosPorDepartamento.clear();
                        empleadosDepartamentoVacio.set(true);
                        empleadosDepartamentoCargando.set(false);
                    });
                    return null;
                });
    }

    /**
     * Solicita las ausencias por estado a la API de forma asíncrona.
     * Un fallo en este método no afecta a las demás cargas del Dashboard.
     *
     * @return CompletableFuture que se completa cuando la carga finaliza,
     *         con éxito o con error.
     */
    public CompletableFuture<Void> cargarAusenciasPorEstado() {
        ausenciasEstadoCargando.set(true);

        return estadisticasRepository.getAusenciasPorEstado()
                .thenAccept(lista -> Platform.runLater(() -> {
                    ausenciasPorEstado.setAll(lista);
                    ausenciasEstadoVacio.set(lista.isEmpty());
                    ausenciasEstadoCargando.set(false);
                }))
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        ausenciasPorEstado.clear();
                        ausenciasEstadoVacio.set(true);
                        ausenciasEstadoCargando.set(false);
                    });
                    return null;
                });
    }

    /**
     * Indica si el número de retrasos de un empleado supera el umbral de advertencia.
     *
     * @param valor Número de retrasos del empleado.
     * @return true si supera el umbral {@value UMBRAL_ADVERTENCIA_RETRASOS}.
     */
    public boolean superaUmbralAdvertencia(Number valor) {
        if (valor == null) return false;
        return valor.intValue() > UMBRAL_ADVERTENCIA_RETRASOS;
    }

    /** @return Property con el objeto KpisDTO cargado desde la API. */
    public ObjectProperty<KpisDTO> kpisProperty() { return kpis; }

    /** @return Lista observable con el ranking de empleados con más retrasos. */
    public ObservableList<DatoGraficoDTO> getTopRetrasos() { return topRetrasos; }

    /** @return Property que indica si se están cargando los KPIs. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property que indica si se está cargando el ranking de retrasos. */
    public BooleanProperty topRetrasosCargandoProperty() { return topRetrasosCargando; }

    /** @return Property con el mensaje de error a mostrar en la vista. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property que controla la visibilidad del panel de error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }

    /** @return Property que indica si el ranking de retrasos está vacío. */
    public BooleanProperty topRetrasosVacioProperty() { return topRetrasosVacio; }

    /** @return Lista observable de ausencias agrupadas por estado. */
    public ObservableList<DatoGraficoDTO> getAusenciasPorEstado() { return ausenciasPorEstado; }

    /** @return Property que indica si se están cargando las ausencias por estado. */
    public BooleanProperty ausenciasEstadoCargandoProperty() { return ausenciasEstadoCargando; }

    /** @return Property que indica si la lista de ausencias por estado está vacía. */
    public BooleanProperty ausenciasEstadoVacioProperty() { return ausenciasEstadoVacio; }

    /** @return Lista observable de empleados agrupados por departamento. */
    public ObservableList<DatoGraficoDTO> getEmpleadosPorDepartamento() { return empleadosPorDepartamento; }

    /** @return Property que indica si se están cargando los empleados por departamento. */
    public BooleanProperty empleadosDepartamentoCargandoProperty() { return empleadosDepartamentoCargando; }

    /** @return Property que indica si la lista de empleados por departamento está vacía. */
    public BooleanProperty empleadosDepartamentoVacioProperty() { return empleadosDepartamentoVacio; }

    /** @return Property que indica si alguna carga del dashboard está en curso. */
    public BooleanProperty cargandoTodoProperty() { return cargandoTodo; }
}
