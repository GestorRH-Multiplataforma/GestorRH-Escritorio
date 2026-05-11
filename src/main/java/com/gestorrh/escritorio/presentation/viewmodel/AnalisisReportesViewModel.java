package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaFichajeDTO;
import com.gestorrh.escritorio.data.repository.FichajeRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista de
 * Análisis y Reportes. Gestiona la carga de fichajes del mes actual,
 * su agrupación por día y la exposición de los datos como Properties
 * reactivas para el binding con el controlador.
 * Generado como Prototype (nueva instancia por cada apertura de la vista)
 * desde {@link com.gestorrh.escritorio.core.di.ViewModelFactory}.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AnalisisReportesViewModel {

    private final FichajeRepository fichajeRepository;

    private final ObservableList<XYChart.Data<String, Number>> fichajesPorDia =
            FXCollections.observableArrayList();

    private final BooleanProperty cargandoFichajes  = new SimpleBooleanProperty(false);
    private final BooleanProperty fichajesVacio     = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible      = new SimpleBooleanProperty(false);
    private final StringProperty  mensajeError      = new SimpleStringProperty("");
    private final StringProperty  tituloGrafico     = new SimpleStringProperty("");

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param fichajeRepository Repositorio de datos de fichajes.
     */
    public AnalisisReportesViewModel(FichajeRepository fichajeRepository) {
        this.fichajeRepository = fichajeRepository;
        actualizarTituloGrafico();
    }

    /**
     * Carga los fichajes del mes actual de forma asíncrona.
     * Los parámetros se omiten para que el servidor aplique sus valores
     * por defecto (día 1 del mes actual hasta hoy, todos los empleados).
     * Una vez recibidos, agrupa los fichajes por fecha y rellena todos
     * los días del mes, asignando 0 a los días sin actividad.
     */
    public void cargarFichajesMesActual() {
        cargandoFichajes.set(true);
        errorVisible.set(false);
        mensajeError.set("");

        fichajeRepository.consultarFichajes(null, null, null)
                .thenAccept(fichajes -> Platform.runLater(() -> {
                    cargandoFichajes.set(false);
                    fichajesVacio.set(fichajes.isEmpty());
                    if (!fichajes.isEmpty()) {
                        poblarDatosPorDia(fichajes);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargandoFichajes.set(false);
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mensajeError.set(causa.getMessage());
                        errorVisible.set(true);
                    });
                    return null;
                });
    }

    /**
     * Actualiza el título del gráfico con el mes y año actuales localizados.
     * Debe llamarse al cambiar el idioma para reflejar el nombre del mes
     * en la lengua activa.
     */
    public void actualizarTituloGrafico() {
        YearMonth mesActual = YearMonth.now();
        String nombreMes = mesActual.getMonth()
                .getDisplayName(TextStyle.FULL,
                        LanguageManager.getInstance().getCurrentLocale());
        String mesAnio = nombreMes.substring(0, 1).toUpperCase(
                LanguageManager.getInstance().getCurrentLocale())
                + nombreMes.substring(1)
                + " " + mesActual.getYear();

        String plantilla = LanguageManager.getInstance()
                .getString("analisis.chart.fichajes.titulo");
        tituloGrafico.set(plantilla.replace("{0}", mesAnio));
    }

    /**
     * Agrupa los fichajes recibidos por fecha y rellena todos los días del
     * mes actual. Los días sin fichajes reciben valor 0 para mantener el
     * eje X continuo.
     *
     * @param fichajes Lista de fichajes devuelta por la API.
     */
    private void poblarDatosPorDia(List<RespuestaFichajeDTO> fichajes) {
        Map<LocalDate, Long> conteoPorDia = fichajes.stream()
                .filter(f -> f.fecha() != null)
                .collect(Collectors.groupingBy(
                        f -> LocalDate.parse(f.fecha()),
                        Collectors.counting()
                ));

        YearMonth mesActual = YearMonth.now();
        List<XYChart.Data<String, Number>> datos = new java.util.ArrayList<>();

        for (int dia = 1; dia <= mesActual.lengthOfMonth(); dia++) {
            LocalDate fecha = mesActual.atDay(dia);
            String etiqueta = String.format("%02d", dia);
            long total = conteoPorDia.getOrDefault(fecha, 0L);
            datos.add(new XYChart.Data<>(etiqueta, total));
        }

        fichajesPorDia.setAll(datos);
    }

    /**
     * @return Lista observable de datos del gráfico de fichajes por día.
     */
    public ObservableList<XYChart.Data<String, Number>> getFichajesPorDia() {
        return fichajesPorDia;
    }

    /**
     * @return Property que indica si se están cargando los fichajes.
     */
    public BooleanProperty cargandoFichajesProperty() {
        return cargandoFichajes;
    }

    /**
     * @return Property que indica si la lista de fichajes está vacía.
     */
    public BooleanProperty fichajesVacioProperty() {
        return fichajesVacio;
    }

    /**
     * @return Property de visibilidad del error.
     */
    public BooleanProperty errorVisibleProperty() {
        return errorVisible;
    }

    /**
     * @return Property del mensaje de error.
     */
    public StringProperty mensajeErrorProperty() {
        return mensajeError;
    }

    /**
     * @return Property del título localizado del gráfico.
     */
    public StringProperty tituloGraficoProperty() {
        return tituloGrafico;
    }
}
