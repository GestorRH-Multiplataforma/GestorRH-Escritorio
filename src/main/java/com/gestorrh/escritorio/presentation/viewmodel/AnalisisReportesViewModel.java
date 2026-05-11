package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaFichajeDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteDetalleDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaReporteResumenDTO;
import com.gestorrh.escritorio.data.repository.EmpleadoRepository;
import com.gestorrh.escritorio.data.repository.FichajeRepository;
import com.gestorrh.escritorio.data.repository.ReporteRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista de
 * Análisis y Reportes. Gestiona la carga de fichajes del mes actual para
 * el gráfico existente, y la generación de informes de control horario
 * con soporte para previsualización JSON y descarga de PDF.
 * Generado como Prototype (nueva instancia por cada apertura de la vista)
 * desde {@link com.gestorrh.escritorio.core.di.ViewModelFactory}.
 *
 * @author Fco Javier García Cañero
 * @version 2.0
 */
public class AnalisisReportesViewModel {

    /**
     * Tipos de informe disponibles para la sección de control horario.
     */
    public enum TipoInforme {
        DETALLE,
        RESUMEN
    }

    private final FichajeRepository fichajeRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ReporteRepository reporteRepository;

    private final ObservableList<XYChart.Data<String, Number>> fichajesPorDia =
            FXCollections.observableArrayList();

    private final BooleanProperty cargandoFichajes  = new SimpleBooleanProperty(false);
    private final BooleanProperty fichajesVacio     = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible      = new SimpleBooleanProperty(false);
    private final StringProperty  mensajeError      = new SimpleStringProperty("");
    private final StringProperty  tituloGrafico     = new SimpleStringProperty("");

    private final ObservableList<RespuestaEmpleadoDTO> empleados =
            FXCollections.observableArrayList();

    private final ObjectProperty<TipoInforme> tipoInforme =
            new SimpleObjectProperty<>(TipoInforme.DETALLE);

    private final ObjectProperty<RespuestaEmpleadoDTO> empleadoSeleccionado =
            new SimpleObjectProperty<>(null);

    private final ObjectProperty<LocalDate> fechaInicio =
            new SimpleObjectProperty<>(null);

    private final ObjectProperty<LocalDate> fechaFin =
            new SimpleObjectProperty<>(null);

    private final BooleanProperty rangoValido       = new SimpleBooleanProperty(false);
    private final BooleanProperty previsualizando   = new SimpleBooleanProperty(false);
    private final BooleanProperty descargando       = new SimpleBooleanProperty(false);
    private final BooleanProperty hayDatosPrevia    = new SimpleBooleanProperty(false);
    private final StringProperty  mensajeErrorInforme = new SimpleStringProperty("");
    private final BooleanProperty errorInformeVisible = new SimpleBooleanProperty(false);

    private final ObservableList<RespuestaReporteDetalleDTO> datosDetalle =
            FXCollections.observableArrayList();

    private final ObservableList<RespuestaReporteResumenDTO> datosResumen =
            FXCollections.observableArrayList();

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param fichajeRepository  Repositorio de datos de fichajes para el gráfico.
     * @param empleadoRepository Repositorio de empleados para poblar el combo.
     * @param reporteRepository  Repositorio de reportes para previsualización y descarga.
     */
    public AnalisisReportesViewModel(
            FichajeRepository fichajeRepository,
            EmpleadoRepository empleadoRepository,
            ReporteRepository reporteRepository) {
        this.fichajeRepository  = fichajeRepository;
        this.empleadoRepository = empleadoRepository;
        this.reporteRepository  = reporteRepository;
        configurarValidacionRango();
        actualizarTituloGrafico();
    }

    /**
     * Configura los listeners reactivos que recalculan {@code rangoValido}
     * cada vez que cambian {@code fechaInicio} o {@code fechaFin}.
     */
    private void configurarValidacionRango() {
        fechaInicio.addListener((obs, o, n) -> recalcularRangoValido());
        fechaFin.addListener((obs, o, n)    -> recalcularRangoValido());
    }

    /**
     * Recalcula si el rango de fechas es válido.
     * El rango es válido cuando ambas fechas están presentes y fechaInicio
     * no es posterior a fechaFin.
     */
    private void recalcularRangoValido() {
        LocalDate ini = fechaInicio.get();
        LocalDate fin = fechaFin.get();
        rangoValido.set(ini != null && fin != null && !ini.isAfter(fin));
    }

    /**
     * Carga los fichajes del mes actual de forma asíncrona para el gráfico de barras.
     * Agrupa los registros por fecha y rellena todos los días del mes,
     * asignando 0 a los días sin actividad.
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
     * Carga la lista de empleados activos de forma asíncrona para poblar el combo.
     * No incluye la opción "Todos" en la lista — esa opción se gestiona en el controlador
     * representada por un valor null en el combo.
     */
    public void cargarEmpleados() {
        empleadoRepository.getEmpleados()
                .thenAccept(lista -> Platform.runLater(() ->
                        empleados.setAll(lista.stream()
                                .filter(RespuestaEmpleadoDTO::activo)
                                .collect(Collectors.toList()))
                ))
                .exceptionally(ex -> null);
    }

    /**
     * Lanza la previsualización del informe de forma asíncrona.
     * Llama al endpoint JSON correspondiente según el tipo de informe seleccionado
     * y actualiza la lista observable de datos para que el controlador la muestre.
     *
     * @return CompletableFuture que se completa cuando los datos están disponibles.
     */
    public CompletableFuture<Void> previsualizar() {
        previsualizando.set(true);
        errorInformeVisible.set(false);
        mensajeErrorInforme.set("");
        datosDetalle.clear();
        datosResumen.clear();
        hayDatosPrevia.set(false);

        String ini = fechaInicio.get().toString();
        String fin = fechaFin.get().toString();
        Long idEmpleado = getIdEmpleado();

        if (tipoInforme.get() == TipoInforme.DETALLE) {
            return reporteRepository.obtenerDetalle(ini, fin, idEmpleado)
                    .thenAccept(lista -> Platform.runLater(() -> {
                        datosDetalle.setAll(lista);
                        hayDatosPrevia.set(!lista.isEmpty());
                        previsualizando.set(false);
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            previsualizando.set(false);
                            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                            mensajeErrorInforme.set(causa.getMessage());
                            errorInformeVisible.set(true);
                        });
                        return null;
                    });
        } else {
            return reporteRepository.obtenerResumen(ini, fin, idEmpleado)
                    .thenAccept(lista -> Platform.runLater(() -> {
                        datosResumen.setAll(lista);
                        hayDatosPrevia.set(!lista.isEmpty());
                        previsualizando.set(false);
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            previsualizando.set(false);
                            Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                            mensajeErrorInforme.set(causa.getMessage());
                            errorInformeVisible.set(true);
                        });
                        return null;
                    });
        }
    }

    /**
     * Descarga el PDF del informe en la ruta indicada de forma asíncrona.
     * Delega en el repositorio según el tipo de informe seleccionado.
     *
     * @param destino Ruta completa del archivo PDF de destino elegida por el usuario.
     * @return CompletableFuture que se completa con null tras la escritura exitosa.
     */
    public CompletableFuture<Void> descargarPdf(Path destino) {
        descargando.set(true);
        errorInformeVisible.set(false);
        mensajeErrorInforme.set("");

        String ini = fechaInicio.get().toString();
        String fin = fechaFin.get().toString();
        Long idEmpleado = getIdEmpleado();

        CompletableFuture<Void> tarea;

        if (tipoInforme.get() == TipoInforme.DETALLE) {
            tarea = reporteRepository.descargarPdfDetalle(ini, fin, idEmpleado, destino);
        } else {
            tarea = reporteRepository.descargarPdfResumen(ini, fin, idEmpleado, destino);
        }

        return tarea.whenComplete((res, ex) ->
                Platform.runLater(() -> descargando.set(false))
        );
    }

    /**
     * Construye el nombre de archivo PDF por defecto según el tipo de informe,
     * el empleado seleccionado y el rango de fechas.
     * Formato con empleado:  informe_{tipo}_{apellidos}_{fechaInicio}_{fechaFin}.pdf
     * Formato sin empleado:  informe_{tipo}_TODOS_{fechaInicio}_{fechaFin}.pdf
     *
     * @return Nombre de archivo sugerido para el FileChooser.
     */
    public String getNombreArchivoDefecto() {
        String tipo = tipoInforme.get() == TipoInforme.DETALLE ? "detalle" : "resumen";
        String ini  = fechaInicio.get().toString();
        String fin  = fechaFin.get().toString();

        RespuestaEmpleadoDTO emp = empleadoSeleccionado.get();
        String nombreParte = (emp != null)
                ? emp.apellidos().replace(" ", "_")
                : "TODOS";

        return "informe_" + tipo + "_" + nombreParte + "_" + ini + "_" + fin + ".pdf";
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
     * Devuelve el identificador del empleado seleccionado, o null si se eligió "Todos".
     *
     * @return ID del empleado o null.
     */
    private Long getIdEmpleado() {
        RespuestaEmpleadoDTO emp = empleadoSeleccionado.get();
        return emp != null ? emp.idEmpleado() : null;
    }

    /**
     * Agrupa los fichajes recibidos por fecha y rellena todos los días del mes actual.
     * Los días sin fichajes reciben valor 0 para mantener el eje X continuo.
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

    /** @return Lista observable de datos del gráfico de fichajes por día. */
    public ObservableList<XYChart.Data<String, Number>> getFichajesPorDia() {
        return fichajesPorDia;
    }

    /** @return Property que indica si se están cargando los fichajes. */
    public BooleanProperty cargandoFichajesProperty() { return cargandoFichajes; }

    /** @return Property que indica si la lista de fichajes está vacía. */
    public BooleanProperty fichajesVacioProperty() { return fichajesVacio; }

    /** @return Property de visibilidad del error del gráfico. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }

    /** @return Property del mensaje de error del gráfico. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property del título localizado del gráfico. */
    public StringProperty tituloGraficoProperty() { return tituloGrafico; }

    /** @return Lista observable de empleados activos para el combo. */
    public ObservableList<RespuestaEmpleadoDTO> getEmpleados() { return empleados; }

    /** @return Property del tipo de informe seleccionado. */
    public ObjectProperty<TipoInforme> tipoInformeProperty() { return tipoInforme; }

    /** @return Property del empleado seleccionado. Null significa todos. */
    public ObjectProperty<RespuestaEmpleadoDTO> empleadoSeleccionadoProperty() {
        return empleadoSeleccionado;
    }

    /** @return Property de la fecha de inicio del rango. */
    public ObjectProperty<LocalDate> fechaInicioProperty() { return fechaInicio; }

    /** @return Property de la fecha de fin del rango. */
    public ObjectProperty<LocalDate> fechaFinProperty() { return fechaFin; }

    /** @return Property que indica si el rango de fechas es válido. */
    public BooleanProperty rangoValidoProperty() { return rangoValido; }

    /** @return Property que indica si se está ejecutando la previsualización. */
    public BooleanProperty previsualizandoProperty() { return previsualizando; }

    /** @return Property que indica si se está descargando el PDF. */
    public BooleanProperty descargandoProperty() { return descargando; }

    /** @return Property que indica si hay datos cargados en la tabla de previsualización. */
    public BooleanProperty hayDatosPreviaProperty() { return hayDatosPrevia; }

    /** @return Property del mensaje de error de la sección de informes. */
    public StringProperty mensajeErrorInformeProperty() { return mensajeErrorInforme; }

    /** @return Property de visibilidad del error de la sección de informes. */
    public BooleanProperty errorInformeVisibleProperty() { return errorInformeVisible; }

    /** @return Lista observable de datos del informe detallado para la tabla. */
    public ObservableList<RespuestaReporteDetalleDTO> getDatosDetalle() { return datosDetalle; }

    /** @return Lista observable de datos del informe resumido para la tabla. */
    public ObservableList<RespuestaReporteResumenDTO> getDatosResumen() { return datosResumen; }
}
