package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.ViewModelFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.presentation.viewmodel.ConfiguracionViewModel;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;

/**
 * Controlador para la vista de configuración de empresa.
 * Gestiona los datos generales del perfil, la geocodificación de la sede,
 * la introducción manual de coordenadas y el cambio de contraseña.
 * Sigue el patrón MVVM del proyecto, delegando toda la lógica en
 * {@link ConfiguracionViewModel}.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ConfiguracionController {

    @FXML private Label bannerExito;
    @FXML private Label bannerError;

    @FXML private Label labelSeccionGenerales;
    @FXML private Label labelEmail;
    @FXML private Label labelEmailValor;
    @FXML private Label labelNombre;
    @FXML private TextField fieldNombre;
    @FXML private Label errorNombre;
    @FXML private Label labelDireccion;
    @FXML private TextField fieldDireccion;
    @FXML private Label errorDireccion;
    @FXML private Label labelTelefono;
    @FXML private TextField fieldTelefono;
    @FXML private Label errorTelefono;

    @FXML private Label labelSeccionUbicacion;
    @FXML private Label labelBannerUbicacion;
    @FXML private TextField fieldBusquedaMapa;
    @FXML private Button btnBuscarMapa;
    @FXML private ProgressIndicator indicadorGeocodificacion;
    @FXML private Label errorGeocodificacion;
    @FXML private Label labelLatitudTitulo;
    @FXML private Label labelLatitud;
    @FXML private Label labelLongitudTitulo;
    @FXML private Label labelLongitud;
    @FXML private Label labelRadioTitulo;
    @FXML private Spinner<Integer> spinnerRadio;
    @FXML private TitledPane panelCoordenadasManuales;
    @FXML private Label labelAvisoManual;
    @FXML private Label labelLatitudManual;
    @FXML private TextField fieldLatitudManual;
    @FXML private Label labelLongitudManual;
    @FXML private TextField fieldLongitudManual;
    @FXML private Label errorCoordenadasManuales;
    @FXML private Button btnAplicarCoordenadas;

    @FXML private TitledPane panelPassword;
    @FXML private Label labelPasswordActual;
    @FXML private PasswordField fieldPasswordActual;
    @FXML private Label errorPasswordActual;
    @FXML private Label labelNuevaPassword;
    @FXML private PasswordField fieldNuevaPassword;
    @FXML private Label errorNuevaPassword;
    @FXML private Label labelConfirmarPassword;
    @FXML private PasswordField fieldConfirmarPassword;
    @FXML private Label errorConfirmarPassword;
    @FXML private Button btnCambiarPassword;

    @FXML private Button btnDescartar;
    @FXML private Button btnGuardar;
    @FXML private ProgressIndicator indicadorCarga;

    private ConfiguracionViewModel viewModel;
    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Inicializa el controlador: crea el ViewModel, configura el Spinner,
     * establece los bindings, actualiza los textos y lanza la carga del perfil.
     */
    @FXML
    public void initialize() {
        viewModel = ViewModelFactory.getInstance().createConfiguracionViewModel();

        configurarSpinner();
        configurarBindings();
        configurarValidacionInline();

        actualizarTextos();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        viewModel.cargarPerfil();
    }

    /**
     * Libera el listener de idioma al destruirse la vista para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    // Configuración inicial

    /**
     * Configura el Spinner de radio de validez con su rango y valor por defecto,
     * y lo sincroniza bidireccionalmente con la Property del ViewModel.
     */
    private void configurarSpinner() {
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
                25, 500, 50, 25
        );
        spinnerRadio.setValueFactory(factory);

        spinnerRadio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                viewModel.radioValidezProperty().set(newVal);
            }
        });

        viewModel.radioValidezProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(spinnerRadio.getValue())) {
                spinnerRadio.getValueFactory().setValue(newVal);
            }
        });
    }

    /**
     * Configura todos los bindings entre los componentes de la vista
     * y las Properties del ViewModel.
     */
    private void configurarBindings() {
        fieldNombre.textProperty().bindBidirectional(viewModel.nombreProperty());
        fieldDireccion.textProperty().bindBidirectional(viewModel.direccionProperty());
        fieldTelefono.textProperty().bindBidirectional(viewModel.telefonoProperty());
        labelEmailValor.textProperty().bind(viewModel.emailDisplayProperty());

        fieldBusquedaMapa.textProperty().bindBidirectional(viewModel.campoBusquedaMapaProperty());

        indicadorGeocodificacion.visibleProperty().bind(viewModel.geocodificandoProperty());
        indicadorGeocodificacion.managedProperty().bind(viewModel.geocodificandoProperty());
        btnBuscarMapa.disableProperty().bind(viewModel.geocodificandoProperty());

        fieldLatitudManual.textProperty()
                .bindBidirectional(viewModel.latitudManualInputProperty());
        fieldLongitudManual.textProperty()
                .bindBidirectional(viewModel.longitudManualInputProperty());

        btnAplicarCoordenadas.disableProperty().bind(
                viewModel.latitudManualInputProperty().isEmpty()
                        .or(viewModel.longitudManualInputProperty().isEmpty())
        );

        viewModel.latitudSedeProperty().addListener((obs, oldVal, newVal) ->
                actualizarDisplayCoordenadas()
        );
        viewModel.longitudSedeProperty().addListener((obs, oldVal, newVal) ->
                actualizarDisplayCoordenadas()
        );

        fieldPasswordActual.textProperty()
                .bindBidirectional(viewModel.passwordActualProperty());
        fieldNuevaPassword.textProperty()
                .bindBidirectional(viewModel.nuevaPasswordProperty());
        fieldConfirmarPassword.textProperty()
                .bindBidirectional(viewModel.confirmarPasswordProperty());

        btnCambiarPassword.disableProperty().bind(
                viewModel.cambioPasswordValidoProperty().not()
                        .or(viewModel.guardandoProperty())
        );

        btnGuardar.disableProperty().bind(
                viewModel.formularioValidoProperty().not()
                        .or(viewModel.guardandoProperty())
        );

        indicadorCarga.visibleProperty().bind(viewModel.cargandoProperty());
        indicadorCarga.managedProperty().bind(viewModel.cargandoProperty());
    }

    /**
     * Configura los listeners de validación inline sobre los campos obligatorios,
     * siguiendo el mismo patrón que EmpleadoFormController.
     */
    private void configurarValidacionInline() {
        viewModel.nombreProperty().addListener((obs, o, n) -> actualizarErroresFormulario());
        viewModel.direccionProperty().addListener((obs, o, n) -> actualizarErroresFormulario());
        viewModel.telefonoProperty().addListener((obs, o, n) -> actualizarErroresFormulario());

        viewModel.nuevaPasswordProperty().addListener((obs, o, n) -> actualizarErroresPassword());
        viewModel.confirmarPasswordProperty().addListener((obs, o, n) -> actualizarErroresPassword());

        viewModel.errorGeocodificacionProperty().addListener((obs, o, n) -> {
            boolean hayError = n != null && !n.isBlank();
            String texto = hayError
                    ? LanguageManager.getInstance().getString(n)
                    : "";
            errorGeocodificacion.setText(texto);
            errorGeocodificacion.setVisible(hayError);
            errorGeocodificacion.setManaged(hayError);
        });

        viewModel.errorCoordenadasManualessProperty().addListener((obs, o, n) -> {
            boolean hayError = n != null && !n.isBlank();
            String texto = hayError
                    ? LanguageManager.getInstance().getString(n)
                    : "";
            errorCoordenadasManuales.setText(texto);
            errorCoordenadasManuales.setVisible(hayError);
            errorCoordenadasManuales.setManaged(hayError);
        });
    }

    // Handlers FXML

    /**
     * Lanza la geocodificación de la dirección introducida en el campo de búsqueda.
     * Muestra el spinner durante la operación y actualiza los campos de coordenadas
     * al completarse.
     */
    @FXML
    private void handleBuscarMapa() {
        String query = fieldBusquedaMapa.getText().trim();
        if (query.isBlank()) return;

        viewModel.geocodificarDireccion(query)
                .thenRun(() -> Platform.runLater(this::actualizarDisplayCoordenadas))
                .exceptionally(ex -> null);
    }

    /**
     * Aplica las coordenadas introducidas manualmente validándolas en el ViewModel.
     * Si son válidas, actualiza los displays de latitud y longitud.
     */
    @FXML
    private void handleAplicarCoordenadas() {
        viewModel.aplicarCoordenadasManuales();
        actualizarDisplayCoordenadas();

        boolean sinError = viewModel.errorCoordenadasManualessProperty().get().isBlank();
        if (sinError) {
            panelCoordenadasManuales.setExpanded(false);
        }
    }

    /**
     * Guarda el perfil actualizado enviando los datos a la API.
     * Muestra el banner de éxito con auto-dismiss de 4 segundos o el banner
     * de error si la operación falla.
     */
    @FXML
    private void handleGuardar() {
        viewModel.guardarPerfil()
                .thenAccept(resp -> Platform.runLater(() ->
                        mostrarBannerExito(
                                LanguageManager.getInstance()
                                        .getString("configuracion.exito.guardado")
                        )
                ))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarBannerError(cause.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Descarta los cambios pendientes restaurando el perfil original.
     * Colapsa también los paneles expandibles.
     */
    @FXML
    private void handleDescartar() {
        viewModel.descartarCambios();
        actualizarDisplayCoordenadas();
        panelCoordenadasManuales.setExpanded(false);
        panelPassword.setExpanded(false);
    }

    /**
     * Cambia la contraseña de la empresa enviando los datos a la API.
     * Colapsa el panel de contraseña y muestra el banner de éxito si la
     * operación es correcta.
     */
    @FXML
    private void handleCambiarPassword() {
        viewModel.cambiarPassword()
                .thenRun(() -> Platform.runLater(() -> {
                    panelPassword.setExpanded(false);
                    mostrarBannerExito(
                            LanguageManager.getInstance()
                                    .getString("configuracion.exito.passwordCambiada")
                    );
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarBannerError(cause.getMessage());
                    });
                    return null;
                });
    }

    // Métodos auxiliares

    /**
     * Actualiza los Labels de display de latitud y longitud con los valores
     * actuales de las Properties del ViewModel. Muestra "—" si son nulos.
     */
    private void actualizarDisplayCoordenadas() {
        Double lat = viewModel.latitudSedeProperty().get();
        Double lng = viewModel.longitudSedeProperty().get();
        labelLatitud.setText(lat != null ? String.format("%.6f", lat) : "—");
        labelLongitud.setText(lng != null ? String.format("%.6f", lng) : "—");
    }

    /**
     * Muestra el banner de éxito con el mensaje indicado y lo oculta
     * automáticamente tras 4 segundos.
     *
     * @param mensaje Mensaje a mostrar en el banner.
     */
    private void mostrarBannerExito(String mensaje) {
        bannerError.setVisible(false);
        bannerError.setManaged(false);
        bannerExito.setText(mensaje);
        bannerExito.setVisible(true);
        bannerExito.setManaged(true);

        PauseTransition pausa = new PauseTransition(Duration.seconds(4));
        pausa.setOnFinished(e -> {
            bannerExito.setVisible(false);
            bannerExito.setManaged(false);
        });
        pausa.play();
    }

    /**
     * Muestra el banner de error con el mensaje indicado y lo oculta
     * automáticamente tras 4 segundos.
     *
     * @param mensaje Mensaje a mostrar en el banner.
     */
    private void mostrarBannerError(String mensaje) {
        bannerExito.setVisible(false);
        bannerExito.setManaged(false);
        bannerError.setText(mensaje);
        bannerError.setVisible(true);
        bannerError.setManaged(true);

        PauseTransition pausa = new PauseTransition(Duration.seconds(4));
        pausa.setOnFinished(e -> {
            bannerError.setVisible(false);
            bannerError.setManaged(false);
        });
        pausa.play();
    }

    /**
     * Actualiza los mensajes de error inline de los campos obligatorios
     * del formulario principal siguiendo el patrón de EmpleadoFormController.
     */
    private void actualizarErroresFormulario() {
        LanguageManager lang = LanguageManager.getInstance();
        String msgRequerido = lang.getString("configuracion.error.campo.requerido");

        mostrarErrorCampo(errorNombre,
                campoTocado(fieldNombre),
                viewModel.nombreProperty().get().isBlank(),
                msgRequerido);

        mostrarErrorCampo(errorDireccion,
                campoTocado(fieldDireccion),
                viewModel.direccionProperty().get().isBlank(),
                msgRequerido);

        mostrarErrorCampo(errorTelefono,
                campoTocado(fieldTelefono),
                viewModel.telefonoProperty().get().isBlank(),
                msgRequerido);
    }

    /**
     * Actualiza los mensajes de error inline del panel de cambio de contraseña.
     */
    private void actualizarErroresPassword() {
        LanguageManager lang = LanguageManager.getInstance();

        String nueva     = viewModel.nuevaPasswordProperty().get();
        String confirmar = viewModel.confirmarPasswordProperty().get();

        boolean nuevaCorta = !nueva.isEmpty() && nueva.length() < 8;
        mostrarErrorCampo(errorNuevaPassword, !nueva.isEmpty(), nuevaCorta,
                lang.getString("configuracion.password.error.minLength"));

        boolean noCoincide = !confirmar.isEmpty() && !nueva.equals(confirmar);
        mostrarErrorCampo(errorConfirmarPassword, !confirmar.isEmpty(), noCoincide,
                lang.getString("configuracion.password.error.coincide"));
    }

    /**
     * Muestra u oculta el label de error de un campo según si ha sido
     * tocado y si su valor es inválido.
     *
     * @param errorLabel Label de error del campo.
     * @param tocado     Indica si el usuario ha interactuado con el campo.
     * @param invalido   Indica si el valor actual es inválido.
     * @param mensaje    Mensaje de error a mostrar.
     */
    private void mostrarErrorCampo(Label errorLabel, boolean tocado,
                                   boolean invalido, String mensaje) {
        if (tocado && invalido) {
            errorLabel.setText(mensaje);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Indica si el usuario ha interactuado con un campo de texto.
     *
     * @param field Campo de texto a comprobar.
     * @return true si el campo tiene contenido o está enfocado.
     */
    private boolean campoTocado(TextField field) {
        return field.isFocused() || !field.getText().isEmpty();
    }

    /**
     * Actualiza todos los textos de la vista con el idioma activo.
     * Se ejecuta al inicializar y cada vez que cambia el idioma.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        labelSeccionGenerales.setText(lang.getString("configuracion.seccion.generales"));
        labelEmail.setText(lang.getString("configuracion.campo.email"));
        labelNombre.setText(lang.getString("configuracion.campo.nombre"));
        labelDireccion.setText(lang.getString("configuracion.campo.direccion"));
        labelTelefono.setText(lang.getString("configuracion.campo.telefono"));

        fieldNombre.setPromptText(lang.getString("configuracion.campo.nombre.placeholder"));
        fieldDireccion.setPromptText(lang.getString("configuracion.campo.direccion.placeholder"));
        fieldTelefono.setPromptText(lang.getString("configuracion.campo.telefono.placeholder"));

        labelSeccionUbicacion.setText(lang.getString("configuracion.seccion.ubicacion"));
        labelBannerUbicacion.setText(lang.getString("configuracion.banner.ubicacionRequerida"));
        fieldBusquedaMapa.setPromptText(lang.getString("configuracion.mapa.placeholder"));
        btnBuscarMapa.setText(lang.getString("configuracion.mapa.buscar"));
        labelLatitudTitulo.setText(lang.getString("configuracion.campo.latitud"));
        labelLongitudTitulo.setText(lang.getString("configuracion.campo.longitud"));
        labelRadioTitulo.setText(lang.getString("configuracion.radio"));

        panelCoordenadasManuales.setText(
                lang.getString("configuracion.coordenadas.manuales.titulo"));
        labelAvisoManual.setText(lang.getString("configuracion.coordenadas.manuales.aviso"));
        labelLatitudManual.setText(lang.getString("configuracion.coordenadas.manuales.lat"));
        labelLongitudManual.setText(lang.getString("configuracion.coordenadas.manuales.lng"));
        fieldLatitudManual.setPromptText("-90 … 90");
        fieldLongitudManual.setPromptText("-180 … 180");
        btnAplicarCoordenadas.setText(
                lang.getString("configuracion.coordenadas.manuales.btn.aplicar"));

        panelPassword.setText(lang.getString("configuracion.seccion.password"));
        labelPasswordActual.setText(lang.getString("configuracion.password.actual"));
        labelNuevaPassword.setText(lang.getString("configuracion.password.nueva"));
        labelConfirmarPassword.setText(lang.getString("configuracion.password.confirmar"));
        btnCambiarPassword.setText(lang.getString("configuracion.btn.cambiarPassword"));

        btnDescartar.setText(lang.getString("configuracion.btn.descartar"));
        btnGuardar.setText(lang.getString("configuracion.btn.guardar"));
    }
}
