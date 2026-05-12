package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.turno.PeticionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaTurnoDTO;
import com.gestorrh.escritorio.data.repository.TurnoRepository;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel encargado de gestionar el estado y la lógica del formulario
 * de alta y edición de turnos. Generado como Prototype (nueva instancia
 * por cada apertura del modal) desde {@link com.gestorrh.escritorio.core.di.ViewModelFactory}.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class TurnoFormViewModel {

    /**
     * Enum que representa el modo de operación del formulario.
     */
    public enum ModoFormulario {
        ALTA,
        EDICION
    }

    private static final DateTimeFormatter FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATTER_API     = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int DESCRIPCION_MAX_LENGTH = 100;

    private final TurnoRepository turnoRepository;

    private ModoFormulario modo;
    private Long idTurnoEnEdicion;

    private final StringProperty descripcion     = new SimpleStringProperty("");
    private final StringProperty horaInicio      = new SimpleStringProperty("");
    private final StringProperty horaFin         = new SimpleStringProperty("");

    private final BooleanProperty cargando         = new SimpleBooleanProperty(false);
    private final BooleanProperty formularioValido = new SimpleBooleanProperty(false);
    private final StringProperty  mensajeError     = new SimpleStringProperty("");
    private final BooleanProperty errorVisible     = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param turnoRepository Repositorio de datos de turnos.
     */
    public TurnoFormViewModel(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
        configurarValidacionReactiva();
    }

    /**
     * Configura los listeners reactivos que recalculan {@code formularioValido}
     * ante cualquier cambio en los campos del formulario.
     */
    private void configurarValidacionReactiva() {
        descripcion.addListener((obs, o, n) -> recalcularFormularioValido());
        horaInicio.addListener((obs, o, n)  -> recalcularFormularioValido());
        horaFin.addListener((obs, o, n)     -> recalcularFormularioValido());
    }

    /**
     * Recalcula si el formulario es válido:
     * - Descripción no vacía y no mayor de 100 caracteres.
     * - Hora inicio parseable como HH:mm.
     * - Hora fin parseable como HH:mm.
     * - Hora inicio distinta de hora fin.
     * - Los turnos nocturnos (horaFin menor que horaInicio) son válidos.
     */
    private void recalcularFormularioValido() {
        if (descripcion.get().isBlank()
                || descripcion.get().length() > DESCRIPCION_MAX_LENGTH) {
            formularioValido.set(false);
            return;
        }

        LocalTime inicio = parsearHora(horaInicio.get());
        LocalTime fin    = parsearHora(horaFin.get());

        if (inicio == null || fin == null) {
            formularioValido.set(false);
            return;
        }

        // Turnos nocturnos permitidos: solo se rechaza inicio == fin
        formularioValido.set(!inicio.equals(fin));
    }

    /**
     * Intenta parsear una cadena de texto como hora en formato HH:mm.
     *
     * @param texto Cadena a parsear.
     * @return LocalTime si el formato es válido, null en caso contrario.
     */
    public LocalTime parsearHora(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            return LocalTime.parse(texto.trim(), FORMATTER_DISPLAY);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Inicializa el ViewModel en modo Alta con todos los campos vacíos.
     */
    public void inicializarParaAlta() {
        this.modo = ModoFormulario.ALTA;
        this.idTurnoEnEdicion = null;
        descripcion.set("");
        horaInicio.set("");
        horaFin.set("");
        limpiarError();
        recalcularFormularioValido();
    }

    /**
     * Inicializa el ViewModel en modo Edición con los datos del turno existente.
     * Las horas se convierten de "HH:mm:ss" (API) a "HH:mm" (display).
     *
     * @param turno DTO con los datos actuales del turno a editar.
     */
    public void inicializarParaEdicion(RespuestaTurnoDTO turno) {
        this.modo = ModoFormulario.EDICION;
        this.idTurnoEnEdicion = turno.idTurno();
        descripcion.set(turno.descripcion() != null ? turno.descripcion() : "");
        horaInicio.set(formatearParaDisplay(turno.horaInicio()));
        horaFin.set(formatearParaDisplay(turno.horaFin()));
        limpiarError();
        recalcularFormularioValido();
    }

    /**
     * Convierte una hora en formato "HH:mm:ss" (API) a "HH:mm" (display).
     *
     * @param horaApi Hora en formato "HH:mm:ss".
     * @return Hora en formato "HH:mm", o cadena vacía si el valor es nulo.
     */
    private String formatearParaDisplay(String horaApi) {
        if (horaApi == null || horaApi.isBlank()) return "";
        try {
            LocalTime time = LocalTime.parse(horaApi, FORMATTER_API);
            return time.format(FORMATTER_DISPLAY);
        } catch (DateTimeParseException e) {
            return horaApi;
        }
    }

    /**
     * Convierte una hora en formato "HH:mm" (display) a "HH:mm:ss" (API).
     *
     * @param horaDisplay Hora en formato "HH:mm".
     * @return Hora en formato "HH:mm:ss".
     */
    private String formatearParaApi(String horaDisplay) {
        LocalTime time = parsearHora(horaDisplay);
        return time != null ? time.format(FORMATTER_API) : horaDisplay;
    }

    /**
     * Ejecuta el alta del nuevo turno de forma asíncrona.
     *
     * @return CompletableFuture con los datos del turno creado.
     */
    public CompletableFuture<RespuestaTurnoDTO> guardarAlta() {
        cargando.set(true);
        limpiarError();

        PeticionTurnoDTO dto = new PeticionTurnoDTO(
                descripcion.get().trim(),
                formatearParaApi(horaInicio.get()),
                formatearParaApi(horaFin.get())
        );

        return turnoRepository.crearTurno(dto)
                .whenComplete((res, ex) -> cargando.set(false));
    }

    /**
     * Ejecuta la actualización del turno en edición de forma asíncrona.
     *
     * @return CompletableFuture con los datos actualizados del turno.
     */
    public CompletableFuture<RespuestaTurnoDTO> guardarEdicion() {
        cargando.set(true);
        limpiarError();

        PeticionTurnoDTO dto = new PeticionTurnoDTO(
                descripcion.get().trim(),
                formatearParaApi(horaInicio.get()),
                formatearParaApi(horaFin.get())
        );

        return turnoRepository.actualizarTurno(idTurnoEnEdicion, dto)
                .whenComplete((res, ex) -> cargando.set(false));
    }

    /**
     * Limpia el mensaje de error y oculta el panel de error.
     */
    public void limpiarError() {
        mensajeError.set("");
        errorVisible.set(false);
    }

    /**
     * Establece un mensaje de error y lo hace visible.
     *
     * @param mensaje Mensaje de error ya traducido para mostrar al usuario.
     */
    public void mostrarError(String mensaje) {
        mensajeError.set(mensaje);
        errorVisible.set(true);
    }

    /** @return Modo activo del formulario (ALTA o EDICION). */
    public ModoFormulario getModo() { return modo; }

    /** @return Property del campo descripción. */
    public StringProperty descripcionProperty() { return descripcion; }

    /** @return Property del campo hora inicio. */
    public StringProperty horaInicioProperty() { return horaInicio; }

    /** @return Property del campo hora fin. */
    public StringProperty horaFinProperty() { return horaFin; }

    /** @return Property del estado de carga. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property que indica si el formulario es válido. */
    public BooleanProperty formularioValidoProperty() { return formularioValido; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
