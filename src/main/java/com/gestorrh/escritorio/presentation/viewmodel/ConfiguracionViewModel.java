package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.service.GeocodingService;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionActualizarEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.PeticionCambiarPasswordEmpresaDTO;
import com.gestorrh.escritorio.data.network.dto.empresa.RespuestaEmpresaDTO;
import com.gestorrh.escritorio.data.repository.EmpresaRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.CompletableFuture;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la vista de configuración
 * de empresa. Expone Properties reactivas para el binding con la vista y gestiona
 * las operaciones asíncronas de carga, guardado, geocodificación y cambio de contraseña.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ConfiguracionViewModel {

    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int RADIO_MIN = 25;
    private static final int RADIO_MAX = 500;
    private static final int RADIO_DEFAULT = 50;

    private final EmpresaRepository empresaRepository;
    private final GeocodingService geocodingService = new GeocodingService();

    private RespuestaEmpresaDTO perfilOriginal;

    private final StringProperty nombre           = new SimpleStringProperty("");
    private final StringProperty direccion        = new SimpleStringProperty("");
    private final StringProperty telefono         = new SimpleStringProperty("");
    private final StringProperty emailDisplay     = new SimpleStringProperty("");

    private final ObjectProperty<Double> latitudSede   = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Double> longitudSede  = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> radioValidez = new SimpleObjectProperty<>(RADIO_DEFAULT);
    private final StringProperty campoBusquedaMapa     = new SimpleStringProperty("");
    private final BooleanProperty geocodificando       = new SimpleBooleanProperty(false);
    private final StringProperty errorGeocodificacion  = new SimpleStringProperty("");

    private final StringProperty latitudManualInput        = new SimpleStringProperty("");
    private final StringProperty longitudManualInput       = new SimpleStringProperty("");
    private final StringProperty errorCoordenadasManuales  = new SimpleStringProperty("");

    private final StringProperty passwordActual      = new SimpleStringProperty("");
    private final StringProperty nuevaPassword       = new SimpleStringProperty("");
    private final StringProperty confirmarPassword   = new SimpleStringProperty("");
    private final BooleanProperty cambioPasswordValido = new SimpleBooleanProperty(false);

    private final BooleanProperty cargando         = new SimpleBooleanProperty(false);
    private final BooleanProperty guardando        = new SimpleBooleanProperty(false);
    private final BooleanProperty formularioValido = new SimpleBooleanProperty(false);
    private final StringProperty mensajeExito      = new SimpleStringProperty("");
    private final StringProperty mensajeError      = new SimpleStringProperty("");
    private final BooleanProperty exitoVisible     = new SimpleBooleanProperty(false);
    private final BooleanProperty errorVisible     = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección manual de dependencias.
     * Configura los listeners reactivos de validación al inicializarse.
     *
     * @param empresaRepository Repositorio de datos de la empresa.
     */
    public ConfiguracionViewModel(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
        configurarValidacionReactiva();
    }

    // Validación reactiva

    /**
     * Configura los listeners que recalculan la validez del formulario y del
     * panel de cambio de contraseña ante cualquier cambio en sus campos.
     */
    private void configurarValidacionReactiva() {
        nombre.addListener((obs, o, n)    -> recalcularFormularioValido());
        direccion.addListener((obs, o, n) -> recalcularFormularioValido());
        telefono.addListener((obs, o, n)  -> recalcularFormularioValido());
        latitudSede.addListener((obs, o, n)  -> recalcularFormularioValido());
        longitudSede.addListener((obs, o, n) -> recalcularFormularioValido());
        radioValidez.addListener((obs, o, n) -> recalcularFormularioValido());

        passwordActual.addListener((obs, o, n)    -> recalcularCambioPasswordValido());
        nuevaPassword.addListener((obs, o, n)     -> recalcularCambioPasswordValido());
        confirmarPassword.addListener((obs, o, n) -> recalcularCambioPasswordValido());
    }

    /**
     * Recalcula si el formulario principal es válido.
     * Los campos nombre, dirección y teléfono son obligatorios.
     * Las coordenadas son opcionales, pero si se introduce alguna,
     * las tres (latitud, longitud y radio) deben estar presentes.
     */
    private void recalcularFormularioValido() {
        boolean camposBase = !nombre.get().isBlank()
                && !direccion.get().isBlank()
                && !telefono.get().isBlank();

        Double lat  = latitudSede.get();
        Double lng  = longitudSede.get();
        Integer rad = radioValidez.get();

        boolean coordenadasValidas;
        if (lat == null && lng == null) {
            coordenadasValidas = true;
        } else if (lat != null && lng != null && rad != null) {
            coordenadasValidas = lat >= -90 && lat <= 90
                    && lng >= -180 && lng <= 180
                    && rad >= RADIO_MIN && rad <= RADIO_MAX;
        } else {
            coordenadasValidas = false;
        }

        formularioValido.set(camposBase && coordenadasValidas);
    }

    /**
     * Recalcula si el panel de cambio de contraseña tiene datos válidos:
     * contraseña actual no vacía, nueva contraseña con longitud mínima
     * y coincidencia entre nueva contraseña y confirmación.
     */
    private void recalcularCambioPasswordValido() {
        boolean valido = !passwordActual.get().isBlank()
                && nuevaPassword.get().length() >= PASSWORD_MIN_LENGTH
                && nuevaPassword.get().equals(confirmarPassword.get());
        cambioPasswordValido.set(valido);
    }


    /**
     * Carga el perfil de la empresa desde la API de forma asíncrona
     * y rellena todas las Properties con los datos recibidos.
     */
    public void cargarPerfil() {
        cargando.set(true);
        limpiarMensajes();

        empresaRepository.getPerfil()
                .thenAccept(perfil -> Platform.runLater(() -> {
                    this.perfilOriginal = perfil;
                    rellenarPropertiesDesde(perfil);
                    cargando.set(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargando.set(false);
                        mostrarError(ex.getCause() != null
                                ? ex.getCause().getMessage()
                                : ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Guarda el perfil actualizado en la API de forma asíncrona.
     * Construye el DTO desde las Properties actuales antes de enviar.
     *
     * @return CompletableFuture con el perfil actualizado devuelto por la API.
     */
    public CompletableFuture<RespuestaEmpresaDTO> guardarPerfil() {
        guardando.set(true);
        limpiarMensajes();

        PeticionActualizarEmpresaDTO dto = new PeticionActualizarEmpresaDTO(
                nombre.get().trim(),
                direccion.get().trim(),
                telefono.get().trim(),
                latitudSede.get(),
                longitudSede.get(),
                radioValidez.get()
        );

        return empresaRepository.actualizarPerfil(dto)
                .whenComplete((perfil, ex) -> Platform.runLater(() -> {
                    guardando.set(false);
                    if (perfil != null) {
                        this.perfilOriginal = perfil;
                    }
                }));
    }

    /**
     * Cambia la contraseña de la empresa de forma asíncrona.
     * Limpia los campos del panel de contraseña tras el éxito.
     *
     * @return CompletableFuture que se completa con null tras el cambio exitoso.
     */
    public CompletableFuture<Void> cambiarPassword() {
        guardando.set(true);
        limpiarMensajes();

        PeticionCambiarPasswordEmpresaDTO dto = new PeticionCambiarPasswordEmpresaDTO(
                passwordActual.get(),
                nuevaPassword.get()
        );

        return empresaRepository.cambiarPassword(dto)
                .whenComplete((res, ex) -> Platform.runLater(() -> {
                    guardando.set(false);
                    if (ex == null) {
                        passwordActual.set("");
                        nuevaPassword.set("");
                        confirmarPassword.set("");
                    }
                }));
    }

    /**
     * Descarta los cambios pendientes restaurando todas las Properties
     * a los valores del último perfil cargado desde la API.
     * No realiza ninguna llamada de red.
     */
    public void descartarCambios() {
        if (perfilOriginal != null) {
            rellenarPropertiesDesde(perfilOriginal);
        }
        latitudManualInput.set("");
        longitudManualInput.set("");
        errorCoordenadasManuales.set("");
        errorGeocodificacion.set("");
        limpiarMensajes();
    }

    /**
     * Geocodifica una dirección textual usando el GeocodingService
     * y actualiza las Properties de latitud y longitud con el resultado.
     *
     * @param query Dirección textual a geocodificar.
     * @return CompletableFuture que se completa cuando finaliza la geocodificación.
     */
    public CompletableFuture<Void> geocodificarDireccion(String query) {
        geocodificando.set(true);
        errorGeocodificacion.set("");

        return geocodingService.geocodificar(query)
                .thenAccept(resultado -> Platform.runLater(() -> {
                    latitudSede.set(resultado.latitud());
                    longitudSede.set(resultado.longitud());
                    if (radioValidez.get() == null) {
                        radioValidez.set(RADIO_DEFAULT);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() ->
                            errorGeocodificacion.set("configuracion.error.geocoding")
                    );
                    return null;
                })
                .whenComplete((res, ex) ->
                        Platform.runLater(() -> geocodificando.set(false))
                );
    }

    /**
     * Valida y aplica las coordenadas introducidas manualmente en los campos
     * de latitud y longitud. Si son válidas, actualiza las Properties de
     * geolocalización. Si no, rellena el mensaje de error correspondiente.
     */
    public void aplicarCoordenadasManuales() {
        errorCoordenadasManuales.set("");

        String latStr = latitudManualInput.get().trim().replace(",", ".");
        String lngStr = longitudManualInput.get().trim().replace(",", ".");

        double lat;
        double lng;

        try {
            lat = Double.parseDouble(latStr);
            lng = Double.parseDouble(lngStr);
        } catch (NumberFormatException e) {
            errorCoordenadasManuales.set("configuracion.coordenadas.manuales.error.formato");
            return;
        }

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            errorCoordenadasManuales.set("configuracion.coordenadas.manuales.error.rango");
            return;
        }

        latitudSede.set(lat);
        longitudSede.set(lng);
        if (radioValidez.get() == null) {
            radioValidez.set(RADIO_DEFAULT);
        }
        latitudManualInput.set("");
        longitudManualInput.set("");
    }

    // Métodos auxiliares

    /**
     * Rellena todas las Properties editables con los datos de un perfil dado.
     * Usado tanto al cargar el perfil inicial como al descartar cambios.
     *
     * @param perfil DTO del perfil desde el que se extraen los valores.
     */
    private void rellenarPropertiesDesde(RespuestaEmpresaDTO perfil) {
        nombre.set(perfil.nombre()    != null ? perfil.nombre()    : "");
        direccion.set(perfil.direccion() != null ? perfil.direccion() : "");
        telefono.set(perfil.telefono() != null ? perfil.telefono() : "");
        emailDisplay.set(perfil.email() != null ? perfil.email()   : "");
        latitudSede.set(perfil.latitudSede());
        longitudSede.set(perfil.longitudSede());
        radioValidez.set(perfil.radioValidez() != null
                ? perfil.radioValidez()
                : RADIO_DEFAULT);
    }

    /**
     * Muestra un mensaje de error en el banner superior.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    public void mostrarError(String mensaje) {
        mensajeError.set(mensaje);
        errorVisible.set(true);
    }

    /**
     * Limpia los mensajes de éxito y error del banner superior.
     */
    public void limpiarMensajes() {
        mensajeExito.set("");
        mensajeError.set("");
        exitoVisible.set(false);
        errorVisible.set(false);
    }

    /**
     * @return Valor mínimo permitido para el radio de validez en metros.
     */
    public static int getRadioMin() { return RADIO_MIN; }

    /**
     * @return Valor máximo permitido para el radio de validez en metros.
     */
    public static int getRadioMax() { return RADIO_MAX; }

    /**
     * @return Valor por defecto para el radio de validez en metros.
     */
    public static int getRadioDefault() { return RADIO_DEFAULT; }

    /** @return Property del nombre de la empresa. */
    public StringProperty nombreProperty() { return nombre; }

    /** @return Property de la dirección de la empresa. */
    public StringProperty direccionProperty() { return direccion; }

    /** @return Property del teléfono de la empresa. */
    public StringProperty telefonoProperty() { return telefono; }

    /** @return Property del email (solo lectura). */
    public StringProperty emailDisplayProperty() { return emailDisplay; }

    /** @return Property de la latitud de la sede. */
    public ObjectProperty<Double> latitudSedeProperty() { return latitudSede; }

    /** @return Property de la longitud de la sede. */
    public ObjectProperty<Double> longitudSedeProperty() { return longitudSede; }

    /** @return Property del radio de validez en metros. */
    public ObjectProperty<Integer> radioValidezProperty() { return radioValidez; }

    /** @return Property del texto de búsqueda de dirección en el mapa. */
    public StringProperty campoBusquedaMapaProperty() { return campoBusquedaMapa; }

    /** @return Property que indica si se está geocodificando. */
    public BooleanProperty geocodificandoProperty() { return geocodificando; }

    /** @return Property del mensaje de error de geocodificación. */
    public StringProperty errorGeocodificacionProperty() { return errorGeocodificacion; }

    /** @return Property del input de latitud manual. */
    public StringProperty latitudManualInputProperty() { return latitudManualInput; }

    /** @return Property del input de longitud manual. */
    public StringProperty longitudManualInputProperty() { return longitudManualInput; }

    /** @return Property del mensaje de error de coordenadas manuales. */
    public StringProperty errorCoordenadasManualessProperty() { return errorCoordenadasManuales; }

    /** @return Property de la contraseña actual. */
    public StringProperty passwordActualProperty() { return passwordActual; }

    /** @return Property de la nueva contraseña. */
    public StringProperty nuevaPasswordProperty() { return nuevaPassword; }

    /** @return Property de la confirmación de la nueva contraseña. */
    public StringProperty confirmarPasswordProperty() { return confirmarPassword; }

    /** @return Property que indica si el cambio de contraseña es válido. */
    public BooleanProperty cambioPasswordValidoProperty() { return cambioPasswordValido; }

    /** @return Property que indica si se está cargando el perfil. */
    public BooleanProperty cargandoProperty() { return cargando; }

    /** @return Property que indica si se está guardando. */
    public BooleanProperty guardandoProperty() { return guardando; }

    /** @return Property que indica si el formulario principal es válido. */
    public BooleanProperty formularioValidoProperty() { return formularioValido; }

    /** @return Property del mensaje de éxito del banner. */
    public StringProperty mensajeExitoProperty() { return mensajeExito; }

    /** @return Property del mensaje de error del banner. */
    public StringProperty mensajeErrorProperty() { return mensajeError; }

    /** @return Property de visibilidad del banner de éxito. */
    public BooleanProperty exitoVisibleProperty() { return exitoVisible; }

    /** @return Property de visibilidad del banner de error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
