package com.gestorrh.escritorio.presentation.component;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Componente reutilizable de calendario mensual.
 * Renderiza una cuadrícula de días para un {@link YearMonth} dado y permite
 * marcar días con clases CSS arbitrarias. Es agnóstico al origen de datos:
 * no realiza llamadas HTTP y delega en el consumidor la responsabilidad de
 * cargar y pasar los datos a mostrar.
 *
 * <p>Uso básico:</p>
 * <pre>
 *     CalendarioMensual cal = new CalendarioMensual();
 *     cal.setOnDiaClick(fecha -&gt; System.out.println("Clic: " + fecha));
 *     cal.marcarDia(LocalDate.now(), "calendario-dia--con-asignacion");
 * </pre>
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class CalendarioMensual extends VBox {

    private static final int COLUMNAS = 7;
    private static final int FILAS    = 6;
    private static final int TOTAL_CELDAS = COLUMNAS * FILAS;

    private final ObjectProperty<YearMonth> mesActual =
            new SimpleObjectProperty<>(YearMonth.now());

    private final ObjectProperty<LocalDate> diaSeleccionado =
            new SimpleObjectProperty<>(null);

    private Consumer<LocalDate> onDiaClick;
    private BiConsumer<LocalDate, Pane> renderizadorCelda;

    private final Label labelMes   = new Label();
    private final GridPane cabecera = new GridPane();
    private final GridPane grid     = new GridPane();

    private final Map<LocalDate, StackPane> celdas = new HashMap<>();

    private final Runnable actualizadorTextos = this::renderizarTodo;

    /**
     * Crea el componente con el mes actual y registra el listener de idioma.
     */
    public CalendarioMensual() {
        getStyleClass().add("calendario");

        construirNavBar();
        construirCabecera();
        construirGrid();

        getChildren().addAll(construirNavBar(), cabecera, grid);

        mesActual.addListener((obs, oldVal, newVal) -> renderizarTodo());

        LanguageManager.getInstance().addListener(actualizadorTextos);

        renderizarTodo();
    }

    /**
     * Cambia el mes mostrado y re-renderiza el calendario.
     *
     * @param mes Mes y año a mostrar.
     */
    public void setMes(YearMonth mes) {
        mesActual.set(mes);
    }

    /**
     * Aplica una clase CSS adicional a la celda del día indicado.
     * Si la fecha no pertenece al mes actual visible, la llamada no tiene efecto.
     *
     * @param fecha      Día al que aplicar la marca.
     * @param styleClass Clase CSS a añadir (p. ej. {@code "calendario-dia--con-asignacion"}).
     */
    public void marcarDia(LocalDate fecha, String styleClass) {
        StackPane celda = celdas.get(fecha);
        if (celda != null && !celda.getStyleClass().contains(styleClass)) {
            celda.getStyleClass().add(styleClass);
        }
    }

    /**
     * Elimina todas las clases CSS de marca de contenido de todas las celdas.
     * Conserva las clases estructurales ({@code calendario-dia},
     * {@code calendario-dia--hoy}, {@code calendario-dia--seleccionado}, etc.).
     */
    public void limpiarMarcas() {
        celdas.values().forEach(celda ->
                celda.getStyleClass().removeIf(c ->
                        c.equals("calendario-dia--con-asignacion") ||
                                c.equals("calendario-dia--con-ausencia")
                )
        );
    }

    /**
     * Registra el callback que se ejecutará al hacer clic en un día válido
     * del mes actual. Los días de meses adyacentes no disparan este evento.
     *
     * @param handler Consumer que recibe la fecha clicada.
     */
    public void setOnDiaClick(Consumer<LocalDate> handler) {
        this.onDiaClick = handler;
    }

    /**
     * Registra un renderer personalizado que se invoca para cada celda del mes
     * actual tras construirla. Permite al consumidor decorar la celda con
     * contenido arbitrario (p. ej. iniciales de turnos asignados).
     *
     * @param renderer BiConsumer que recibe la fecha y el {@link Pane} de la celda.
     */
    public void setRenderizadorCelda(BiConsumer<LocalDate, Pane> renderer) {
        this.renderizadorCelda = renderer;
        renderizarTodo();
    }

    /**
     * Elimina el listener de internacionalización. Debe llamarse cuando la
     * vista que contiene este componente se destruye, para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * @return Property del mes actualmente visible.
     */
    public ObjectProperty<YearMonth> mesActualProperty() {
        return mesActual;
    }

    /**
     * @return Property del día actualmente seleccionado, o {@code null} si ninguno.
     */
    public ObjectProperty<LocalDate> diaSeleccionadoProperty() {
        return diaSeleccionado;
    }

    /**
     * Construye la barra de navegación con los botones anterior/siguiente y el
     * label del mes/año.
     *
     * @return HBox con la barra de navegación.
     */
    private HBox construirNavBar() {
        Button btnAnterior  = new Button();
        Button btnSiguiente = new Button();

        btnAnterior.getStyleClass().add("calendario-btn-nav");
        btnSiguiente.getStyleClass().add("calendario-btn-nav");

        actualizarTextosBotones(btnAnterior, btnSiguiente);

        btnAnterior.setOnAction(e ->
                mesActual.set(mesActual.get().minusMonths(1)));
        btnSiguiente.setOnAction(e ->
                mesActual.set(mesActual.get().plusMonths(1)));

        labelMes.getStyleClass().add("calendario-titulo-mes");

        Region spacerIzq = new Region();
        Region spacerDer = new Region();
        HBox.setHgrow(spacerIzq, Priority.ALWAYS);
        HBox.setHgrow(spacerDer, Priority.ALWAYS);

        HBox navBar = new HBox(btnAnterior, spacerIzq, labelMes, spacerDer, btnSiguiente);
        navBar.getStyleClass().add("calendario-nav");
        navBar.setAlignment(Pos.CENTER);

        navBar.setUserData(new Button[]{btnAnterior, btnSiguiente});

        return navBar;
    }

    /**
     * Construye la fila de cabecera con los nombres cortos de los días de la semana,
     * empezando el lunes.
     */
    private void construirCabecera() {
        cabecera.getStyleClass().add("calendario-cabecera");
        cabecera.setHgap(2);
        rellenarCabecera();
    }

    /**
     * Rellena la cabecera con los nombres localizados de los días de la semana.
     */
    private void rellenarCabecera() {
        cabecera.getChildren().clear();
        Locale locale = LanguageManager.getInstance().getCurrentLocale();

        for (int col = 0; col < COLUMNAS; col++) {
            DayOfWeek dia = DayOfWeek.of(col + 1);
            String nombre = dia.getDisplayName(TextStyle.SHORT, locale);
            nombre = nombre.substring(0, 1).toUpperCase(locale) + nombre.substring(1);

            Label label = new Label(nombre);
            label.getStyleClass().add("calendario-cabecera-dia");
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
            GridPane.setColumnIndex(label, col);
            cabecera.getChildren().add(label);
        }
    }

    /**
     * Inicializa el GridPane del calendario (sin contenido todavía).
     */
    private void construirGrid() {
        grid.getStyleClass().add("calendario-grid");
        grid.setHgap(2);
        grid.setVgap(2);
    }

    /**
     * Punto de entrada del renderizado completo. Re-renderiza cabecera, título
     * y grid de días. Se llama al cambiar el mes o el idioma.
     */
    private void renderizarTodo() {
        rellenarCabecera();
        actualizarTituloMes();
        renderizarGrid();
        actualizarBotonesNav();
    }

    /**
     * Actualiza el label del título con el mes y año actuales localizados.
     */
    private void actualizarTituloMes() {
        Locale locale = LanguageManager.getInstance().getCurrentLocale();
        YearMonth ym  = mesActual.get();

        String nombreMes = ym.getMonth().getDisplayName(TextStyle.FULL, locale);
        nombreMes = nombreMes.substring(0, 1).toUpperCase(locale) + nombreMes.substring(1);

        labelMes.setText(nombreMes + " " + ym.getYear());
    }

    /**
     * Renderiza el grid de 6×7 celdas para el mes actual.
     * Los días fuera del mes se muestran en gris y no son clicables.
     */
    private void renderizarGrid() {
        grid.getChildren().clear();
        celdas.clear();

        YearMonth ym           = mesActual.get();
        LocalDate primerDia    = ym.atDay(1);
        LocalDate hoy          = LocalDate.now();
        int offsetInicio = primerDia.getDayOfWeek().getValue() - 1;

        for (int celda = 0; celda < TOTAL_CELDAS; celda++) {
            int col = celda % COLUMNAS;
            int fila = celda / COLUMNAS;

            LocalDate fecha = primerDia.minusDays(offsetInicio).plusDays(celda);
            boolean esMesActual = fecha.getMonth() == ym.getMonth()
                    && fecha.getYear() == ym.getYear();

            StackPane pane = crearCelda(fecha, hoy, esMesActual);

            GridPane.setColumnIndex(pane, col);
            GridPane.setRowIndex(pane, fila);
            grid.getChildren().add(pane);

            if (esMesActual) {
                celdas.put(fecha, pane);
                if (renderizadorCelda != null) {
                    renderizadorCelda.accept(fecha, pane);
                }
            }
        }
    }

    /**
     * Crea una celda individual del calendario con sus estilos y eventos.
     *
     * @param fecha       Fecha que representa esta celda.
     * @param hoy         Fecha actual del sistema.
     * @param esMesActual {@code true} si la fecha pertenece al mes visible.
     * @return StackPane configurado.
     */
    private StackPane crearCelda(LocalDate fecha, LocalDate hoy, boolean esMesActual) {
        Label numero = new Label(String.valueOf(fecha.getDayOfMonth()));
        numero.getStyleClass().add("calendario-dia-numero");

        StackPane pane = new StackPane(numero);
        pane.getStyleClass().add("calendario-dia");
        pane.setAlignment(Pos.CENTER);

        if (!esMesActual) {
            pane.getStyleClass().add("calendario-dia--otro-mes");
            return pane;
        }

        if (fecha.equals(hoy)) {
            pane.getStyleClass().add("calendario-dia--hoy");
        }

        if (fecha.equals(diaSeleccionado.get())) {
            pane.getStyleClass().add("calendario-dia--seleccionado");
        }

        pane.setOnMouseClicked(e -> handleDiaClick(fecha));

        return pane;
    }

    /**
     * Gestiona el clic sobre un día del mes actual: actualiza la selección,
     * refresca los estilos y dispara el callback externo.
     *
     * @param fecha Fecha clicada.
     */
    private void handleDiaClick(LocalDate fecha) {
        LocalDate anterior = diaSeleccionado.get();

        if (anterior != null) {
            StackPane celdaAnterior = celdas.get(anterior);
            if (celdaAnterior != null) {
                celdaAnterior.getStyleClass().remove("calendario-dia--seleccionado");
            }
        }

        diaSeleccionado.set(fecha);

        StackPane celdaNueva = celdas.get(fecha);
        if (celdaNueva != null) {
            celdaNueva.getStyleClass().add("calendario-dia--seleccionado");
        }

        if (onDiaClick != null) {
            onDiaClick.accept(fecha);
        }
    }

    /**
     * Actualiza los textos de los botones de navegación con el idioma activo.
     *
     * @param btnAnterior  Botón de mes anterior.
     * @param btnSiguiente Botón de mes siguiente.
     */
    private void actualizarTextosBotones(Button btnAnterior, Button btnSiguiente) {
        btnAnterior.setText(
                LanguageManager.getInstance().getString("calendario.btn.anterior"));
        btnSiguiente.setText(
                LanguageManager.getInstance().getString("calendario.btn.siguiente"));
    }

    /**
     * Recorre los nodos hijos buscando la navBar y actualiza los textos de sus botones.
     * Se llama al cambiar el idioma.
     */
    private void actualizarBotonesNav() {
        getChildren().stream()
                .filter(n -> n instanceof HBox && n.getUserData() instanceof Button[])
                .findFirst()
                .ifPresent(n -> {
                    Button[] btns = (Button[]) n.getUserData();
                    actualizarTextosBotones(btns[0], btns[1]);
                });
    }
}
