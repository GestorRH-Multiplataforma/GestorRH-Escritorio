package com.gestorrh.escritorio.presentation.controller;

import com.gestorrh.escritorio.core.i18n.LanguageManager;
import com.gestorrh.escritorio.data.network.dto.RespuestaAusenciaDTO;
import com.gestorrh.escritorio.presentation.viewmodel.AusenciasViewModel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Controlador para el modal de revisión (aprobación o rechazo) de una ausencia.
 * Muestra un resumen de la ausencia y permite añadir observaciones opcionales
 * antes de confirmar la acción.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class RevisionAusenciaModalController {

    @FXML private Label lblTitulo;
    @FXML private Label lblEmpleado;
    @FXML private Label lblTipo;
    @FXML private Label lblFechas;
    @FXML private Label lblDias;
    @FXML private Label lblObservaciones;
    @FXML private TextArea textObservaciones;
    @FXML private Label lblError;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;
    @FXML private Label lblEtiquetaEmpleado;
    @FXML private Label lblEtiquetaTipo;
    @FXML private Label lblEtiquetaFechas;
    @FXML private Label lblEtiquetaDias;

    private AusenciasViewModel viewModel;
    private RespuestaAusenciaDTO ausencia;
    private String estadoDestino;
    private Runnable onRevisionExitosa;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Runnable actualizadorTextos = this::actualizarTextos;

    /**
     * Registra el listener de idioma al inicializarse el controlador.
     */
    @FXML
    public void initialize() {
        LanguageManager.getInstance().addListener(actualizadorTextos);
        textObservaciones.setTextFormatter(
                new javafx.scene.control.TextFormatter<>(change -> {
                    if (change.getControlNewText().length() > 500) return null;
                    return change;
                })
        );
    }

    /**
     * Configura el modal con los datos necesarios.
     * Debe llamarse desde AusenciasController justo después de cargar el FXML.
     *
     * @param ausencia       Ausencia a revisar.
     * @param estadoDestino  APROBADA o RECHAZADA.
     * @param vm             ViewModel compartido con la vista principal.
     */
    public void inicializar(RespuestaAusenciaDTO ausencia,
                            String estadoDestino,
                            AusenciasViewModel vm) {
        this.ausencia = ausencia;
        this.estadoDestino = estadoDestino;
        this.viewModel = vm;
        actualizarTextos();
    }

    /**
     * Registra el callback que se ejecutará tras una revisión exitosa.
     *
     * @param callback Acción a ejecutar tras confirmar correctamente.
     */
    public void setOnRevisionExitosa(Runnable callback) {
        this.onRevisionExitosa = callback;
    }

    /**
     * Gestiona el evento del botón Confirmar.
     * Llama al ViewModel con el estado destino y las observaciones opcionales.
     */
    @FXML
    private void handleConfirmar() {
        ocultarError();
        btnConfirmar.setDisable(true);

        String observaciones = textObservaciones.getText().trim();

        viewModel.revisar(ausencia.idAusencia(), estadoDestino, observaciones)
                .thenAccept(actualizada -> Platform.runLater(() -> {
                    if (onRevisionExitosa != null) onRevisionExitosa.run();
                    cerrar();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmar.setDisable(false);
                        Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                        mostrarError(causa.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Gestiona el evento del botón Cancelar.
     */
    @FXML
    private void handleCancelar() {
        limpiar();
        cerrar();
    }

    /**
     * Muestra el label de error con el mensaje indicado.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    /**
     * Oculta el label de error.
     */
    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    /**
     * Cierra la ventana del modal.
     */
    private void cerrar() {
        ((Stage) btnCancelar.getScene().getWindow()).close();
    }

    /**
     * Elimina el listener de idioma para evitar memory leaks.
     */
    public void limpiar() {
        LanguageManager.getInstance().removeListener(actualizadorTextos);
    }

    /**
     * Calcula el número de días de la ausencia.
     *
     * @return Número de días como long, o 0 si las fechas no son válidas.
     */
    private long calcularDias() {
        try {
            LocalDate inicio = LocalDate.parse(ausencia.fechaInicio());
            LocalDate fin    = LocalDate.parse(ausencia.fechaFin());
            return ChronoUnit.DAYS.between(inicio, fin) + 1;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Formatea una fecha ISO (yyyy-MM-dd) a formato legible (dd/MM/yyyy).
     *
     * @param fechaIso Fecha en formato ISO.
     * @return Fecha formateada o la cadena original si no se puede parsear.
     */
    private String formatearFecha(String fechaIso) {
        try {
            return LocalDate.parse(fechaIso).format(FORMATTER);
        } catch (Exception e) {
            return fechaIso;
        }
    }

    /**
     * Actualiza todos los textos del modal con el idioma activo.
     */
    private void actualizarTextos() {
        LanguageManager lang = LanguageManager.getInstance();

        if (ausencia == null) return;

        lblEtiquetaEmpleado.setText(lang.getString("ausencias.modal.etiqueta.empleado"));
        lblEtiquetaTipo.setText(lang.getString("ausencias.modal.etiqueta.tipo"));
        lblEtiquetaFechas.setText(lang.getString("ausencias.modal.etiqueta.fechas"));
        lblEtiquetaDias.setText(lang.getString("ausencias.modal.etiqueta.dias"));

        boolean aprobando = "APROBADA".equals(estadoDestino);

        lblTitulo.setText(lang.getString(aprobando
                ? "ausencias.modal.aprobar.titulo"
                : "ausencias.modal.rechazar.titulo"));

        btnConfirmar.setText(lang.getString("ausencias.modal.btn.confirmar"));
        btnCancelar.setText(lang.getString("ausencias.modal.btn.cancelar"));
        lblObservaciones.setText(lang.getString("ausencias.modal.observaciones"));

        lblEmpleado.setText(ausencia.nombreCompletoEmpleado());
        lblTipo.setText(lang.getString("ausencias.tipo." + ausencia.tipo().toLowerCase()));

        String fechas = formatearFecha(ausencia.fechaInicio())
                + " – "
                + formatearFecha(ausencia.fechaFin());
        lblFechas.setText(fechas);

        lblDias.setText(calcularDias() + " " + lang.getString("ausencias.col.dias"));
    }
}
