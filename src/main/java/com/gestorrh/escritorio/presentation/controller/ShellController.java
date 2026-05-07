package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.di.RepositoryFactory;
import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.core.navigation.NavigationManager;
import com.gestorrh.escritorio.core.security.SessionManager;
import com.gestorrh.escritorio.data.repository.AusenciaRepository;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Controlador del Shell principal de la aplicación.
 * Gestiona el Header, el Sidebar colapsable, el Footer con reloj
 * y la navegación entre secciones mediante el NavigationManager.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class ShellController {

    private static final Logger LOGGER = Logger.getLogger(ShellController.class.getName());

    private static final double ANCHO_EXPANDIDO  = 250.0;
    private static final double ANCHO_COLAPSADO  = 60.0;
    private static final double DURACION_ANIMACION_MS = 200.0;

    @FXML private Label headerLogoLabel;
    @FXML private Label headerNombreEmpresaLabel;
    @FXML private Label headerSeccionLabel;
    @FXML private Button headerLangEsBtn;
    @FXML private Button headerLangEnBtn;
    @FXML private HBox headerAvatarContainer;

    @FXML private VBox sidebar;
    @FXML private Button btnColapsarSidebar;
    @FXML private FontIcon iconoColapsar;

    @FXML private Button menuDashboardBtn;
    @FXML private Label  menuDashboardLabel;

    @FXML private Button menuEmpleadosBtn;
    @FXML private Label  menuEmpleadosLabel;

    @FXML private Button menuTurnosBtn;
    @FXML private Label  menuTurnosLabel;

    @FXML private Button menuAusenciasBtn;
    @FXML private Label  menuAusenciasLabel;
    @FXML private Label badgeAusencias;

    @FXML private Button menuInformesBtn;
    @FXML private Label  menuInformesLabel;

    @FXML private Button menuConfiguracionBtn;
    @FXML private Label  menuConfiguracionLabel;

    @FXML private Button menuCerrarSesionBtn;
    @FXML private Label  menuCerrarSesionLabel;

    @FXML private BorderPane panelContenido;

    @FXML private Label footerVersionLabel;
    @FXML private Label footerHoraLabel;

    private boolean sidebarColapsado = false;
    private Timeline animacionSidebar;
    private Timeline relojTimeline;

    private int totalAusenciasPendientes = 0;

    private final AusenciaRepository ausenciaRepository =
            RepositoryFactory.getInstance().getAusenciaRepository();
    private final Runnable actualizadorTextos = this::actualizarTextos;
    private String claveSeccionActual = "menu.dashboard";

    /**
     * Inicializa el Shell: registra el ContentPane en el NavigationManager,
     * carga la vista inicial del Dashboard, arranca el reloj del footer
     * y configura el listener de idioma.
     */
    @FXML
    public void initialize() {
        NavigationManager.getInstance().setPanelContenido(panelContenido);

        headerNombreEmpresaLabel.setText(SessionManager.getInstance().getNombreEmpresa());
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        construirAvatar();

        actualizarTextos();
        actualizarToggleIdioma();
        LanguageManager.getInstance().addListener(actualizadorTextos);

        iniciarRelojFooter();

        NavigationManager.getInstance().navegar("/fxml/dashboard-view.fxml");
        cargarBadgeAusenciasInicial();
    }

    /**
     * Libera recursos del controlador: elimina el listener de idioma
     * y detiene el Timeline del reloj para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
        if (relojTimeline != null) {
            relojTimeline.stop();
        }
    }

    /** Navega a la sección Dashboard. */
    @FXML
    private void handleMenuDashboard() {
        claveSeccionActual = "menu.dashboard";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuDashboardBtn);
        NavigationManager.getInstance().navegar("/fxml/dashboard-view.fxml", ctrl -> {
            DashboardController dashboard = (DashboardController) ctrl;
            dashboard.setOnNavegar(ruta -> {
                if ("/fxml/empleados-view.fxml".equals(ruta)) {
                    marcarMenuActivo(menuEmpleadosBtn);
                } else if ("/fxml/turnos-view.fxml".equals(ruta)) {
                    marcarMenuActivo(menuTurnosBtn);
                } else if ("/fxml/ausencias-view.fxml".equals(ruta)) {
                    marcarMenuActivo(menuAusenciasBtn);
                }
                NavigationManager.getInstance().navegar(ruta);
            });
        });
    }

    /** Navega a la sección Empleados. */
    @FXML
    private void handleMenuEmpleados() {
        claveSeccionActual = "menu.employees";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuEmpleadosBtn);
        NavigationManager.getInstance().navegar("/fxml/empleados-view.fxml");
    }

    /** Navega a la sección Turnos. */
    @FXML
    private void handleMenuTurnos() {
        claveSeccionActual = "menu.shifts";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuTurnosBtn);
        NavigationManager.getInstance().navegar("/fxml/turnos-view.fxml");
    }

    /** Navega a la sección Ausencias. */
    @FXML
    private void handleMenuAusencias() {
        claveSeccionActual = "menu.absences";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuAusenciasBtn);
        NavigationManager.getInstance().navegar("/fxml/ausencias-view.fxml", ctrl -> {
            AusenciasController ausenciasController = (AusenciasController) ctrl;
            ausenciasController.setOnPendientesActualizados(total -> {
                Platform.runLater(() -> actualizarBadgeAusencias(total));
            });
        });
    }

    /** Navega a la sección Informes. */
    @FXML
    private void handleMenuInformes() {
        claveSeccionActual = "menu.reports";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuInformesBtn);
        NavigationManager.getInstance().navegar("/fxml/informes-view.fxml", ctrl ->
                ((PlaceholderController) ctrl).setTituloSeccion("placeholder.reports.title")
        );
    }

    /** Navega a la sección Configuración. */
    @FXML
    private void handleMenuConfiguracion() {
        claveSeccionActual = "menu.settings";
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));
        marcarMenuActivo(menuConfiguracionBtn);
        NavigationManager.getInstance().navegar("/fxml/configuracion-view.fxml");
    }

    /**
     * Gestiona el cierre de sesión: limpia el token, detiene el reloj,
     * elimina el listener de idioma y navega de vuelta a la pantalla de Login
     * restaurando el tamaño original de la ventana.
     */
    @FXML
    private void handleCerrarSesion() {
        NavigationManager.getInstance().limpiarControladorActual();
        limpiar();
        SessionManager.getInstance().clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
            Scene escenaLogin = new Scene(loader.load());

            Stage stage = (Stage) menuCerrarSesionBtn.getScene().getWindow();

            stage.setFullScreen(false);
            stage.setMaximized(false);
            stage.setMinWidth(0);
            stage.setMinHeight(0);
            stage.setResizable(false);
            stage.setWidth(1100.0);
            stage.setHeight(660.0);
            stage.setScene(escenaLogin);
            stage.centerOnScreen();

        } catch (IOException e) {
            LOGGER.severe("ShellController: Error al volver al Login: " + e.getMessage());
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle(LanguageManager.getInstance().getString("dialog.error.title"));
            alerta.setHeaderText(null);
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    /** Cambia el idioma a Español. */
    @FXML
    private void handleLangEs() {
        LanguageManager.getInstance().setLocale(Locale.of("es"));
        actualizarToggleIdioma();
    }

    /** Cambia el idioma a Inglés. */
    @FXML
    private void handleLangEn() {
        LanguageManager.getInstance().setLocale(Locale.of("en"));
        actualizarToggleIdioma();
    }

    /**
     * Construye el avatar circular con las iniciales de la empresa
     * y lo inserta al inicio del contenedor del header.
     */
    private void construirAvatar() {
        String nombre = SessionManager.getInstance().getNombreEmpresa();
        String iniciales = obtenerIniciales(nombre);

        javafx.scene.shape.Circle circulo = new javafx.scene.shape.Circle(16);
        circulo.getStyleClass().add("shell-avatar-circulo");

        Label lblIniciales = new Label(iniciales);
        lblIniciales.getStyleClass().add("shell-avatar-iniciales");

        StackPane avatar = new StackPane(circulo, lblIniciales);
        avatar.getStyleClass().add("shell-avatar");
        avatar.setCursor(javafx.scene.Cursor.HAND);

        headerAvatarContainer.getChildren().add(0, avatar);
    }

    /**
     * Extrae las iniciales del nombre de empresa (máximo 2 caracteres).
     *
     * @param nombre Nombre completo de la empresa.
     * @return Iniciales en mayúsculas.
     */
    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] palabras = nombre.trim().split("\\s+");
        if (palabras.length == 1) {
            return palabras[0].substring(0, Math.min(2, palabras[0].length())).toUpperCase();
        }
        return (String.valueOf(palabras[0].charAt(0)) + String.valueOf(palabras[1].charAt(0))).toUpperCase();
    }

    /**
     * Carga el número de ausencias pendientes al iniciar el shell
     * para mostrar el badge en el sidebar sin necesidad de navegar a la vista.
     */
    private void cargarBadgeAusenciasInicial() {
        ausenciaRepository.listar("SOLICITADA")
                .thenAccept(lista -> Platform.runLater(() ->
                        actualizarBadgeAusencias(lista.size())))
                .exceptionally(ex -> null);
    }

    /**
     * Actualiza el badge de ausencias pendientes en el sidebar.
     * Muestra el contador si hay pendientes, lo oculta si no hay ninguna.
     *
     * @param total Número de ausencias pendientes.
     */
    private void actualizarBadgeAusencias(int total) {
        totalAusenciasPendientes = total;
        badgeAusencias.setText(String.valueOf(total));
        badgeAusencias.setVisible(total > 0);
        badgeAusencias.setManaged(total > 0);
    }

    /**
     * Alterna el estado del sidebar entre expandido y colapsado
     * con una animación suave de interpolación de ancho.
     */
    @FXML
    private void handleToggleSidebar() {
        if (animacionSidebar != null) {
            animacionSidebar.stop();
        }

        double anchoDestino = sidebarColapsado ? ANCHO_EXPANDIDO : ANCHO_COLAPSADO;
        sidebarColapsado = !sidebarColapsado;

        animacionSidebar = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth())),
                new KeyFrame(Duration.millis(DURACION_ANIMACION_MS),
                        new KeyValue(sidebar.prefWidthProperty(), anchoDestino))
        );

        animacionSidebar.setOnFinished(e -> actualizarVisibilidadTextosSidebar());
        animacionSidebar.play();

        iconoColapsar.setIconLiteral(sidebarColapsado
                ? "mdi2c-chevron-right"
                : "mdi2c-chevron-left");

        if (!sidebarColapsado) {
            actualizarVisibilidadTextosSidebar();
        }
    }

    /**
     * Actualiza todos los textos del Shell con el idioma activo.
     * Se ejecuta al inicializar y cada vez que cambia el idioma.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        headerLogoLabel.setText("GestorRH");

        menuDashboardLabel.setText(lang.getString("menu.dashboard"));
        menuEmpleadosLabel.setText(lang.getString("menu.employees"));
        menuTurnosLabel.setText(lang.getString("menu.shifts"));
        menuAusenciasLabel.setText(lang.getString("menu.absences"));
        menuInformesLabel.setText(lang.getString("menu.reports"));
        menuConfiguracionLabel.setText(lang.getString("menu.settings"));
        menuCerrarSesionLabel.setText(lang.getString("menu.logout"));

        footerVersionLabel.setText(lang.getString("shell.footer.version"));
        headerSeccionLabel.setText(LanguageManager.getInstance().getString(claveSeccionActual));

        actualizarToggleIdioma();
    }

    /**
     * Muestra u oculta los Labels de texto del sidebar según su estado.
     * En modo colapsado solo son visibles los iconos.
     */
    private void actualizarVisibilidadTextosSidebar() {
        boolean mostrarTexto = !sidebarColapsado;

        menuDashboardLabel.setVisible(mostrarTexto);
        menuDashboardLabel.setManaged(mostrarTexto);

        menuEmpleadosLabel.setVisible(mostrarTexto);
        menuEmpleadosLabel.setManaged(mostrarTexto);

        menuTurnosLabel.setVisible(mostrarTexto);
        menuTurnosLabel.setManaged(mostrarTexto);

        menuAusenciasLabel.setVisible(mostrarTexto);
        menuAusenciasLabel.setManaged(mostrarTexto);

        menuInformesLabel.setVisible(mostrarTexto);
        menuInformesLabel.setManaged(mostrarTexto);

        menuConfiguracionLabel.setVisible(mostrarTexto);
        menuConfiguracionLabel.setManaged(mostrarTexto);

        menuCerrarSesionLabel.setVisible(mostrarTexto);
        menuCerrarSesionLabel.setManaged(mostrarTexto);

        badgeAusencias.setVisible(mostrarTexto && totalAusenciasPendientes > 0);
        badgeAusencias.setManaged(mostrarTexto && totalAusenciasPendientes > 0);
    }

    /**
     * Aplica el estilo visual activo al botón de idioma correcto
     * y lo quita del otro.
     */
    private void actualizarToggleIdioma() {
        String idiomaActual = LanguageManager.getInstance().getCurrentLocale().getLanguage();

        headerLangEsBtn.getStyleClass().removeAll("shell-lang-btn-active");
        headerLangEnBtn.getStyleClass().removeAll("shell-lang-btn-active");

        if ("es".equals(idiomaActual)) {
            headerLangEsBtn.getStyleClass().add("shell-lang-btn-active");
        } else {
            headerLangEnBtn.getStyleClass().add("shell-lang-btn-active");
        }
    }

    /**
     * Marca visualmente como activo el botón de menú pulsado
     * y elimina el estado activo del resto.
     *
     * @param botonActivo Botón de menú que debe quedar marcado.
     */
    private void marcarMenuActivo(Button botonActivo) {
        Button[] botonesMenu = {
                menuDashboardBtn, menuEmpleadosBtn, menuTurnosBtn,
                menuAusenciasBtn, menuInformesBtn, menuConfiguracionBtn
        };

        for (Button btn : botonesMenu) {
            btn.getStyleClass().removeAll("sidebar-btn-active");
        }
        botonActivo.getStyleClass().add("sidebar-btn-active");
    }

    /**
     * Inicia el Timeline que actualiza la hora del footer cada segundo.
     */
    private void iniciarRelojFooter() {
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("HH:mm:ss");

        relojTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e ->
                        Platform.runLater(() ->
                                footerHoraLabel.setText(LocalTime.now().format(formateador))
                        )
                )
        );
        relojTimeline.setCycleCount(Timeline.INDEFINITE);
        relojTimeline.play();

        footerHoraLabel.setText(LocalTime.now().format(formateador));
    }
}
