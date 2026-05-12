package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.turno.PeticionAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaAsignacionTurnoDTO;
import com.gestorrh.escritorio.data.network.dto.empleado.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.turno.RespuestaTurnoDTO;
import com.gestorrh.escritorio.data.repository.AsignacionTurnoRepository;
import com.gestorrh.escritorio.data.repository.EmpleadoRepository;
import com.gestorrh.escritorio.data.repository.EmpresaRepository;
import com.gestorrh.escritorio.data.repository.TurnoRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la pantalla
 * de asignación de turnos en calendario. Gestiona la carga de datos inicial,
 * el filtrado por mes y día en cliente, y las operaciones CRUD de asignaciones.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AsignacionTurnosViewModel {

    private final AsignacionTurnoRepository asignacionTurnoRepository;
    private final EmpleadoRepository empleadoRepository;
    private final TurnoRepository turnoRepository;
    private final EmpresaRepository empresaRepository;

    private final ObservableList<RespuestaAsignacionTurnoDTO> todasAsignaciones =
            FXCollections.observableArrayList();

    private final ObservableList<RespuestaAsignacionTurnoDTO> asignacionesMes =
            FXCollections.observableArrayList();

    private final ObservableList<RespuestaAsignacionTurnoDTO> asignacionesDia =
            FXCollections.observableArrayList();

    private final ObservableList<RespuestaEmpleadoDTO> empleados =
            FXCollections.observableArrayList();

    private final ObservableList<RespuestaTurnoDTO> turnos =
            FXCollections.observableArrayList();

    private final ObservableList<String> modalidades =
            FXCollections.observableArrayList();

    private final ObjectProperty<YearMonth> mesActual =
            new SimpleObjectProperty<>(YearMonth.now());

    private final ObjectProperty<LocalDate> diaSeleccionado =
            new SimpleObjectProperty<>(null);

    private final ObjectProperty<RespuestaEmpleadoDTO> empleadoSeleccionado =
            new SimpleObjectProperty<>(null);

    private final ObjectProperty<RespuestaTurnoDTO> turnoSeleccionado =
            new SimpleObjectProperty<>(null);

    private final StringProperty modalidadSeleccionada = new SimpleStringProperty(null);
    private final StringProperty motivoCambio = new SimpleStringProperty("");

    private final BooleanProperty sedeConfigurada = new SimpleBooleanProperty(true);
    private final BooleanProperty cargando = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible = new SimpleBooleanProperty(false);
    private final StringProperty mensajeError = new SimpleStringProperty("");

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param asignacionTurnoRepository Repositorio de asignaciones de turnos.
     * @param empleadoRepository        Repositorio de empleados.
     * @param turnoRepository           Repositorio de turnos.
     * @param empresaRepository         Repositorio de empresa para verificar sede.
     */
    public AsignacionTurnosViewModel(
            AsignacionTurnoRepository asignacionTurnoRepository,
            EmpleadoRepository empleadoRepository,
            TurnoRepository turnoRepository,
            EmpresaRepository empresaRepository) {
        this.asignacionTurnoRepository = asignacionTurnoRepository;
        this.empleadoRepository = empleadoRepository;
        this.turnoRepository = turnoRepository;
        this.empresaRepository = empresaRepository;
    }

    /**
     * Lanza en paralelo la carga de todos los datos necesarios para la vista:
     * asignaciones, empleados, turnos, modalidades y verificación de sede.
     */
    public void inicializar() {
        cargando.set(true);
        errorVisible.set(false);

        CompletableFuture<Void> cargaAsignaciones = asignacionTurnoRepository.getAsignaciones()
                .thenAccept(lista -> Platform.runLater(() -> {
                    todasAsignaciones.setAll(lista);
                    filtrarPorMes(mesActual.get());
                }));

        CompletableFuture<Void> cargaEmpleados = empleadoRepository.getEmpleados()
                .thenAccept(lista -> Platform.runLater(() ->
                        empleados.setAll(lista.stream()
                                .filter(RespuestaEmpleadoDTO::activo)
                                .collect(Collectors.toList()))
                ));

        CompletableFuture<Void> cargaTurnos = turnoRepository.getTurnos()
                .thenAccept(lista -> Platform.runLater(() -> turnos.setAll(lista)));

        CompletableFuture<Void> cargaModalidades = asignacionTurnoRepository.getModalidades()
                .thenAccept(lista -> Platform.runLater(() -> modalidades.setAll(lista)));

        CompletableFuture<Void> verificacion = verificarSedeConfigurada();

        CompletableFuture.allOf(cargaAsignaciones, cargaEmpleados, cargaTurnos, cargaModalidades, verificacion)
                .whenComplete((res, ex) -> Platform.runLater(() -> {
                    cargando.set(false);
                    if (ex != null) {
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mensajeError.set(causa.getMessage());
                        errorVisible.set(true);
                    }
                }));
    }

    /**
     * Llama a GET /api/empresas/me y verifica que latitudSede, longitudSede
     * y radioValidez estén configurados. Actualiza sedeConfigurada en consecuencia.
     *
     * @return CompletableFuture que se completa cuando finaliza la verificación.
     */
    public CompletableFuture<Void> verificarSedeConfigurada() {
        return empresaRepository.getPerfil()
                .thenAccept(perfil -> Platform.runLater(() -> {
                    boolean configurada = perfil.latitudSede() != null
                            && perfil.longitudSede() != null
                            && perfil.radioValidez() != null;
                    sedeConfigurada.set(configurada);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> sedeConfigurada.set(false));
                    return null;
                });
    }

    /**
     * Filtra todasAsignaciones por el mes indicado y actualiza asignacionesMes.
     * Si hay un día seleccionado en ese mes, también actualiza asignacionesDia.
     *
     * @param mes Mes y año por el que filtrar.
     */
    public void filtrarPorMes(YearMonth mes) {
        mesActual.set(mes);
        List<RespuestaAsignacionTurnoDTO> filtradas = todasAsignaciones.stream()
                .filter(a -> {
                    if (a.fecha() == null) return false;
                    LocalDate fecha = LocalDate.parse(a.fecha());
                    return YearMonth.from(fecha).equals(mes);
                })
                .collect(Collectors.toList());
        asignacionesMes.setAll(filtradas);

        LocalDate dia = diaSeleccionado.get();
        if (dia != null && YearMonth.from(dia).equals(mes)) {
            filtrarPorDia(dia);
        } else {
            asignacionesDia.clear();
        }
    }

    /**
     * Filtra asignacionesMes por el día indicado y actualiza asignacionesDia.
     *
     * @param fecha Día por el que filtrar.
     */
    public void filtrarPorDia(LocalDate fecha) {
        diaSeleccionado.set(fecha);
        List<RespuestaAsignacionTurnoDTO> filtradas = asignacionesMes.stream()
                .filter(a -> a.fecha() != null && LocalDate.parse(a.fecha()).equals(fecha))
                .collect(Collectors.toList());
        asignacionesDia.setAll(filtradas);
    }

    /**
     * Crea una nueva asignación de turno con los datos del formulario.
     * Valida que empleado, turno, modalidad estén seleccionados y que la
     * fecha no sea anterior a hoy antes de llamar a la API.
     *
     * @param fecha Fecha de la asignación.
     * @return CompletableFuture con la asignación creada.
     */
    public CompletableFuture<RespuestaAsignacionTurnoDTO> crearAsignacion(LocalDate fecha) {
        PeticionAsignacionTurnoDTO dto = new PeticionAsignacionTurnoDTO(
                empleadoSeleccionado.get().idEmpleado(),
                turnoSeleccionado.get().idTurno(),
                fecha.toString(),
                modalidadSeleccionada.get(),
                null
        );

        return asignacionTurnoRepository.crearAsignacion(dto)
                .thenApply(nueva -> {
                    Platform.runLater(() -> {
                        todasAsignaciones.add(nueva);
                        filtrarPorMes(mesActual.get());
                    });
                    return nueva;
                });
    }

    /**
     * Edita una asignación existente con los datos actuales del formulario.
     * motivoCambio es obligatorio en edición.
     *
     * @param idAsignacion Identificador de la asignación a editar.
     * @param empleado     Empleado seleccionado.
     * @param turno        Turno seleccionado.
     * @param modalidad    Modalidad seleccionada.
     * @param motivo       Motivo del cambio (obligatorio).
     * @param fecha        Fecha de la asignación.
     * @return CompletableFuture con la asignación actualizada.
     */
    public CompletableFuture<RespuestaAsignacionTurnoDTO> editarAsignacion(
            Long idAsignacion,
            RespuestaEmpleadoDTO empleado,
            RespuestaTurnoDTO turno,
            String modalidad,
            String motivo,
            LocalDate fecha) {

        PeticionAsignacionTurnoDTO dto = new PeticionAsignacionTurnoDTO(
                empleado.idEmpleado(),
                turno.idTurno(),
                fecha.toString(),
                modalidad,
                motivo
        );

        return asignacionTurnoRepository.editarAsignacion(idAsignacion, dto)
                .thenApply(actualizada -> {
                    Platform.runLater(() -> {
                        todasAsignaciones.removeIf(a -> a.idAsignacion().equals(idAsignacion));
                        todasAsignaciones.add(actualizada);
                        filtrarPorMes(mesActual.get());
                    });
                    return actualizada;
                });
    }

    /**
     * Elimina una asignación de turno por su identificador.
     *
     * @param idAsignacion Identificador de la asignación a eliminar.
     * @return CompletableFuture que se completa con null tras la eliminación exitosa.
     */
    public CompletableFuture<Void> eliminarAsignacion(Long idAsignacion) {
        return asignacionTurnoRepository.eliminarAsignacion(idAsignacion)
                .thenRun(() -> Platform.runLater(() -> {
                    todasAsignaciones.removeIf(a -> a.idAsignacion().equals(idAsignacion));
                    filtrarPorMes(mesActual.get());
                }));
    }

    /**
     * Recarga todas las asignaciones desde la API y reaplica el filtro del mes actual.
     */
    public void recargarAsignaciones() {
        asignacionTurnoRepository.getAsignaciones()
                .thenAccept(lista -> Platform.runLater(() -> {
                    todasAsignaciones.setAll(lista);
                    filtrarPorMes(mesActual.get());
                }))
                .exceptionally(ex -> null);
    }

    /** @return Lista observable de todas las asignaciones sin filtrar. */
    public ObservableList<RespuestaAsignacionTurnoDTO> getTodasAsignaciones() {
        return todasAsignaciones;
    }

    /** @return Lista observable de asignaciones filtradas por mes actual. */
    public ObservableList<RespuestaAsignacionTurnoDTO> getAsignacionesMes() {
        return asignacionesMes;
    }

    /** @return Lista observable de asignaciones del día seleccionado. */
    public ObservableList<RespuestaAsignacionTurnoDTO> getAsignacionesDia() {
        return asignacionesDia;
    }

    /** @return Lista observable de empleados activos. */
    public ObservableList<RespuestaEmpleadoDTO> getEmpleados() {
        return empleados;
    }

    /** @return Lista observable de turnos disponibles. */
    public ObservableList<RespuestaTurnoDTO> getTurnos() {
        return turnos;
    }

    /** @return Lista observable de modalidades disponibles. */
    public ObservableList<String> getModalidades() {
        return modalidades;
    }

    /** @return Property del mes actualmente visible en el calendario. */
    public ObjectProperty<YearMonth> mesActualProperty() { return mesActual; }

    /** @return Property del día seleccionado en el calendario. */
    public ObjectProperty<LocalDate> diaSeleccionadoProperty() { return diaSeleccionado; }

    /** @return Property del empleado seleccionado en el formulario. */
    public ObjectProperty<RespuestaEmpleadoDTO> empleadoSeleccionadoProperty() {
        return empleadoSeleccionado;
    }

    /** @return Property del turno seleccionado en el formulario. */
    public ObjectProperty<RespuestaTurnoDTO> turnoSeleccionadoProperty() {
        return turnoSeleccionado;
    }

    /** @return Property de la modalidad seleccionada en el formulario. */
    public StringProperty modalidadSeleccionadaProperty() { return modalidadSeleccionada; }

    /** @return Property del motivo de cambio del formulario. */
    public StringProperty motivoCambioProperty() { return motivoCambio; }

    /** @return Property que indica si la sede de la empresa está configurada. */
    public BooleanProperty sedeConfiguradaProperty() { return sedeConfigurada; }

    /** @return Property que indica si se está cargando datos. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
