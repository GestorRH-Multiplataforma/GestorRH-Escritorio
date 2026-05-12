package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.empleado.PeticionActualizarEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.empleado.PeticionCrearEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.empleado.PeticionResetPasswordDTO;
import com.gestorrh.escritorio.data.network.dto.empleado.RespuestaCrearEmpleadoDTO;
import com.gestorrh.escritorio.data.network.dto.empleado.RespuestaEmpleadoDTO;
import com.gestorrh.escritorio.data.repository.EmpleadoRepository;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

/**
 * ViewModel encargado de gestionar el estado y la lógica del formulario
 * de alta y edición de empleados. Generado como Prototype (nueva instancia
 * por cada apertura del modal) desde {@link com.gestorrh.escritorio.core.di.ViewModelFactory}.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class EmpleadoFormViewModel {

    /**
     * Enum que representa el modo de operación del formulario.
     */
    public enum ModoFormulario {
        ALTA,
        EDICION
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int PASSWORD_MIN_LENGTH = 8;

    private final EmpleadoRepository empleadoRepository;

    private ModoFormulario modo;
    private Long idEmpleadoEnEdicion;

    // Properties del formulario principal
    private final StringProperty nombre          = new SimpleStringProperty("");
    private final StringProperty apellidos       = new SimpleStringProperty("");
    private final StringProperty email           = new SimpleStringProperty("");
    private final StringProperty telefono        = new SimpleStringProperty("");
    private final StringProperty puesto          = new SimpleStringProperty("");
    private final StringProperty departamento    = new SimpleStringProperty("");
    private final StringProperty rol             = new SimpleStringProperty("EMPLEADO");

    // Properties del panel de reset de contraseña (solo en edición)
    private final StringProperty nuevaPassword    = new SimpleStringProperty("");
    private final StringProperty confirmarPassword = new SimpleStringProperty("");

    // Properties de estado
    private final BooleanProperty cargando           = new SimpleBooleanProperty(false);
    private final BooleanProperty formularioValido   = new SimpleBooleanProperty(false);
    private final BooleanProperty resetValido        = new SimpleBooleanProperty(false);
    private final BooleanProperty panelResetVisible  = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible       = new SimpleBooleanProperty(false);
    private final StringProperty  mensajeError       = new SimpleStringProperty("");

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param empleadoRepository Repositorio de datos de empleados.
     */
    public EmpleadoFormViewModel(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
        configurarValidacionReactiva();
    }

    /**
     * Configura los listeners reactivos que recalculan {@code formularioValido}
     * y {@code resetValido} ante cualquier cambio en los campos del formulario.
     */
    private void configurarValidacionReactiva() {
        nombre.addListener((obs, o, n)       -> recalcularFormularioValido());
        apellidos.addListener((obs, o, n)    -> recalcularFormularioValido());
        email.addListener((obs, o, n)        -> recalcularFormularioValido());
        puesto.addListener((obs, o, n)       -> recalcularFormularioValido());
        departamento.addListener((obs, o, n) -> recalcularFormularioValido());

        nuevaPassword.addListener((obs, o, n)     -> recalcularResetValido());
        confirmarPassword.addListener((obs, o, n) -> recalcularResetValido());
    }

    /**
     * Recalcula si el formulario principal es válido según el modo activo.
     * En alta se valida también el email. En edición el email no es editable.
     */
    private void recalcularFormularioValido() {
        boolean camposBase = !nombre.get().isBlank()
                && !apellidos.get().isBlank()
                && !puesto.get().isBlank()
                && !departamento.get().isBlank();

        if (modo == ModoFormulario.ALTA) {
            formularioValido.set(camposBase
                    && EMAIL_PATTERN.matcher(email.get().trim()).matches());
        } else {
            formularioValido.set(camposBase);
        }
    }

    /**
     * Recalcula si el panel de reset de contraseña es válido:
     * longitud mínima y coincidencia entre ambos campos.
     */
    private void recalcularResetValido() {
        String pass    = nuevaPassword.get();
        String confirm = confirmarPassword.get();
        resetValido.set(
                pass.length() >= PASSWORD_MIN_LENGTH
                        && pass.equals(confirm)
        );
    }

    /**
     * Inicializa el ViewModel en modo Alta con todos los campos vacíos.
     */
    public void inicializarParaAlta() {
        this.modo = ModoFormulario.ALTA;
        this.idEmpleadoEnEdicion = null;
        nombre.set("");
        apellidos.set("");
        email.set("");
        telefono.set("");
        puesto.set("");
        departamento.set("");
        rol.set("EMPLEADO");
        nuevaPassword.set("");
        confirmarPassword.set("");
        panelResetVisible.set(false);
        limpiarError();
        recalcularFormularioValido();
    }

    /**
     * Inicializa el ViewModel en modo Edición con los datos del empleado existente.
     *
     * @param empleado DTO con los datos actuales del empleado a editar.
     */
    public void inicializarParaEdicion(RespuestaEmpleadoDTO empleado) {
        this.modo = ModoFormulario.EDICION;
        this.idEmpleadoEnEdicion = empleado.idEmpleado();
        nombre.set(empleado.nombre() != null ? empleado.nombre() : "");
        apellidos.set(empleado.apellidos() != null ? empleado.apellidos() : "");
        email.set(empleado.email() != null ? empleado.email() : "");
        telefono.set(empleado.telefono() != null ? empleado.telefono() : "");
        puesto.set(empleado.puesto() != null ? empleado.puesto() : "");
        departamento.set(empleado.departamento() != null ? empleado.departamento() : "");
        rol.set(empleado.rol() != null ? empleado.rol() : "EMPLEADO");
        nuevaPassword.set("");
        confirmarPassword.set("");
        panelResetVisible.set(false);
        limpiarError();
        recalcularFormularioValido();
    }

    /**
     * Ejecuta el alta del nuevo empleado de forma asíncrona.
     *
     * @return CompletableFuture con los datos del empleado creado y su contraseña generada.
     */
    public CompletableFuture<RespuestaCrearEmpleadoDTO> guardarAlta() {
        cargando.set(true);
        limpiarError();

        PeticionCrearEmpleadoDTO dto = new PeticionCrearEmpleadoDTO(
                email.get().trim(),
                nombre.get().trim(),
                apellidos.get().trim(),
                telefono.get().trim().isEmpty() ? null : telefono.get().trim(),
                puesto.get().trim(),
                departamento.get().trim(),
                rol.get()
        );

        return empleadoRepository.crearEmpleado(dto)
                .whenComplete((res, ex) -> cargando.set(false));
    }

    /**
     * Ejecuta la actualización del empleado en edición de forma asíncrona.
     *
     * @return CompletableFuture con los datos actualizados del empleado.
     */
    public CompletableFuture<RespuestaEmpleadoDTO> guardarEdicion() {
        cargando.set(true);
        limpiarError();

        PeticionActualizarEmpleadoDTO dto = new PeticionActualizarEmpleadoDTO(
                nombre.get().trim(),
                apellidos.get().trim(),
                telefono.get().trim().isEmpty() ? null : telefono.get().trim(),
                puesto.get().trim(),
                departamento.get().trim(),
                rol.get()
        );

        return empleadoRepository.actualizarEmpleado(idEmpleadoEnEdicion, dto)
                .whenComplete((res, ex) -> cargando.set(false));
    }

    /**
     * Ejecuta el restablecimiento de contraseña del empleado en edición de forma asíncrona.
     *
     * @return CompletableFuture con los datos actualizados del empleado.
     */
    public CompletableFuture<RespuestaEmpleadoDTO> ejecutarResetPassword() {
        cargando.set(true);
        limpiarError();

        PeticionResetPasswordDTO dto = new PeticionResetPasswordDTO(nuevaPassword.get());

        return empleadoRepository.resetPassword(idEmpleadoEnEdicion, dto)
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

    /** @return Property del campo nombre. */
    public StringProperty nombreProperty() { return nombre; }

    /** @return Property del campo apellidos. */
    public StringProperty apellidosProperty() { return apellidos; }

    /** @return Property del campo email. */
    public StringProperty emailProperty() { return email; }

    /** @return Property del campo teléfono. */
    public StringProperty telefonoProperty() { return telefono; }

    /** @return Property del campo puesto. */
    public StringProperty puestoProperty() { return puesto; }

    /** @return Property del campo departamento. */
    public StringProperty departamentoProperty() { return departamento; }

    /** @return Property del campo rol. */
    public StringProperty rolProperty() { return rol; }

    /** @return Property del campo nueva contraseña (reset). */
    public StringProperty nuevaPasswordProperty() { return nuevaPassword; }

    /** @return Property del campo confirmar contraseña (reset). */
    public StringProperty confirmarPasswordProperty() { return confirmarPassword; }

    /** @return Property del estado de carga. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property que indica si el formulario principal es válido. */
    public BooleanProperty formularioValidoProperty() { return formularioValido; }

    /** @return Property que indica si el panel de reset tiene datos válidos. */
    public BooleanProperty resetValidoProperty() { return resetValido; }

    /** @return Property que controla la visibilidad del panel de reset. */
    public BooleanProperty panelResetVisibleProperty() { return panelResetVisible; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
