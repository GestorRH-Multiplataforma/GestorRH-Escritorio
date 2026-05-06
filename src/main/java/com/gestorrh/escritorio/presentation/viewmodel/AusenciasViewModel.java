package com.gestorrh.escritorio.presentation.viewmodel;

import com.gestorrh.escritorio.data.network.dto.PeticionRevisionAusenciaDTO;
import com.gestorrh.escritorio.data.network.dto.RespuestaAusenciaDTO;
import com.gestorrh.escritorio.data.repository.AusenciaRepository;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel encargado de gestionar el estado y la lógica de la pantalla
 * del buzón de ausencias. Gestiona tres listas independientes (una por estado)
 * con su propio indicador de carga, y expone las operaciones de revisión
 * y descarga de justificantes.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class AusenciasViewModel {

    private static final String ESTADO_SOLICITADA = "SOLICITADA";
    private static final String ESTADO_APROBADA   = "APROBADA";
    private static final String ESTADO_RECHAZADA  = "RECHAZADA";

    private final AusenciaRepository ausenciaRepository;

    private final ObservableList<RespuestaAusenciaDTO> pendientes  =
            FXCollections.observableArrayList();
    private final ObservableList<RespuestaAusenciaDTO> aprobadas   =
            FXCollections.observableArrayList();
    private final ObservableList<RespuestaAusenciaDTO> rechazadas  =
            FXCollections.observableArrayList();

    private final ObservableList<String> tipos   = FXCollections.observableArrayList();
    private final ObservableList<String> estados = FXCollections.observableArrayList();

    private final BooleanProperty cargandoPendientes  = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoAprobadas   = new SimpleBooleanProperty(false);
    private final BooleanProperty cargandoRechazadas  = new SimpleBooleanProperty(false);

    private final StringProperty mensajeError        = new SimpleStringProperty("");
    private final BooleanProperty errorVisible       = new SimpleBooleanProperty(false);

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param ausenciaRepository Repositorio de datos de ausencias.
     */
    public AusenciasViewModel(AusenciaRepository ausenciaRepository) {
        this.ausenciaRepository = ausenciaRepository;
    }

    /**
     * Carga las ausencias pendientes (SOLICITADA) y los catálogos de tipos y estados.
     * Debe llamarse al inicializar la vista.
     */
    public void inicializar() {
        cargarCatalogos();
        cargarPendientes();
    }

    /**
     * Carga los catálogos de tipos y estados desde la API de forma asíncrona.
     * Se usan para localizar los badges de la tabla.
     */
    private void cargarCatalogos() {
        ausenciaRepository.getTipos()
                .thenAccept(lista -> Platform.runLater(() -> tipos.setAll(lista)))
                .exceptionally(ex -> null);

        ausenciaRepository.getEstados()
                .thenAccept(lista -> Platform.runLater(() -> estados.setAll(lista)))
                .exceptionally(ex -> null);
    }

    /**
     * Carga las ausencias en estado SOLICITADA de forma asíncrona.
     */
    public void cargarPendientes() {
        cargandoPendientes.set(true);
        ocultarError();

        ausenciaRepository.listar(ESTADO_SOLICITADA)
                .thenAccept(lista -> Platform.runLater(() -> {
                    pendientes.setAll(lista);
                    cargandoPendientes.set(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargandoPendientes.set(false);
                        mostrarError(ex.getCause() != null
                                ? ex.getCause().getMessage()
                                : ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Carga las ausencias en estado APROBADA de forma asíncrona.
     * Llamado de forma lazy al seleccionar la pestaña por primera vez.
     */
    public void cargarAprobadas() {
        cargandoAprobadas.set(true);
        ocultarError();

        ausenciaRepository.listar(ESTADO_APROBADA)
                .thenAccept(lista -> Platform.runLater(() -> {
                    aprobadas.setAll(lista);
                    cargandoAprobadas.set(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargandoAprobadas.set(false);
                        mostrarError(ex.getCause() != null
                                ? ex.getCause().getMessage()
                                : ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Carga las ausencias en estado RECHAZADA de forma asíncrona.
     * Llamado de forma lazy al seleccionar la pestaña por primera vez.
     */
    public void cargarRechazadas() {
        cargandoRechazadas.set(true);
        ocultarError();

        ausenciaRepository.listar(ESTADO_RECHAZADA)
                .thenAccept(lista -> Platform.runLater(() -> {
                    rechazadas.setAll(lista);
                    cargandoRechazadas.set(false);
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        cargandoRechazadas.set(false);
                        mostrarError(ex.getCause() != null
                                ? ex.getCause().getMessage()
                                : ex.getMessage());
                    });
                    return null;
                });
    }

    /**
     * Aprueba o rechaza una ausencia y recarga las pestañas afectadas.
     * Tras éxito, la fila desaparece de pendientes y aparece en la pestaña destino.
     *
     * @param id                    Identificador de la ausencia a revisar.
     * @param estadoDestino         APROBADA o RECHAZADA.
     * @param observaciones         Texto libre opcional del revisor.
     * @return CompletableFuture con la ausencia actualizada.
     */
    public CompletableFuture<RespuestaAusenciaDTO> revisar(Long id,
                                                           String estadoDestino,
                                                           String observaciones) {
        PeticionRevisionAusenciaDTO dto = new PeticionRevisionAusenciaDTO(
                estadoDestino,
                observaciones == null || observaciones.isBlank() ? null : observaciones
        );

        return ausenciaRepository.revisar(id, dto)
                .thenApply(actualizada -> {
                    Platform.runLater(() -> {
                        cargarPendientes();
                        if (ESTADO_APROBADA.equals(estadoDestino)) {
                            cargarAprobadas();
                        } else {
                            cargarRechazadas();
                        }
                    });
                    return actualizada;
                });
    }

    /**
     * Descarga el justificante de una ausencia en la ruta indicada por el usuario.
     *
     * @param nombreArchivo Nombre del archivo tal como viene en el DTO.
     * @param destino       Ruta completa donde guardar el archivo, elegida por el usuario.
     * @return CompletableFuture con el File guardado en disco.
     */
    public CompletableFuture<File> descargarJustificante(String nombreArchivo, Path destino) {
        return ausenciaRepository.descargarJustificante(nombreArchivo, destino.getParent())
                .thenApply(file -> {
                    if (!file.toPath().equals(destino)) {
                        file.renameTo(destino.toFile());
                        return destino.toFile();
                    }
                    return file;
                });
    }

    /**
     * Muestra un mensaje de error en la vista.
     *
     * @param mensaje Mensaje de error a mostrar.
     */
    private void mostrarError(String mensaje) {
        mensajeError.set(mensaje);
        errorVisible.set(true);
    }

    /**
     * Oculta el mensaje de error.
     */
    private void ocultarError() {
        mensajeError.set("");
        errorVisible.set(false);
    }

    /** @return Lista observable de ausencias pendientes (SOLICITADA). */
    public ObservableList<RespuestaAusenciaDTO> getPendientes()  { return pendientes; }

    /** @return Lista observable de ausencias aprobadas (APROBADA). */
    public ObservableList<RespuestaAusenciaDTO> getAprobadas()   { return aprobadas; }

    /** @return Lista observable de ausencias rechazadas (RECHAZADA). */
    public ObservableList<RespuestaAusenciaDTO> getRechazadas()  { return rechazadas; }

    /** @return Lista observable de tipos de ausencia disponibles. */
    public ObservableList<String> getTipos()   { return tipos; }

    /** @return Lista observable de estados de ausencia disponibles. */
    public ObservableList<String> getEstados() { return estados; }

    /** @return Property de carga de la pestaña pendientes. */
    public BooleanProperty cargandoPendientesProperty()  { return cargandoPendientes; }

    /** @return Property de carga de la pestaña aprobadas. */
    public BooleanProperty cargandoAprobadasProperty()   { return cargandoAprobadas; }

    /** @return Property de carga de la pestaña rechazadas. */
    public BooleanProperty cargandoRechadasProperty()  { return cargandoRechazadas; }

    /** @return Property del mensaje de error. */
    public StringProperty mensajeErrorProperty()  { return mensajeError; }

    /** @return Property de visibilidad del error. */
    public BooleanProperty errorVisibleProperty() { return errorVisible; }
}
