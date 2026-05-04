package com.gestorrh.escritorio.data.repository;

import com.gestorrh.escritorio.data.network.EmpleadoService;
import com.gestorrh.escritorio.data.network.dto.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repositorio encargado de la gestión de datos de Empleados.
 * Actúa como mediador entre la capa de red y la lógica de negocio.
 *
 * @author Fco Javier García Cañero
 * @version 1.2
 */
public class EmpleadoRepository extends BaseRepository {

    private final EmpleadoService service;

    /**
     * Constructor con inyección manual de dependencias.
     *
     * @param service Servicio de red de Retrofit.
     */
    public EmpleadoRepository(EmpleadoService service) {
        this.service = service;
    }

    /**
     * Obtiene el listado completo de empleados de la empresa autenticada
     * de forma asíncrona para no bloquear el hilo de UI.
     *
     * @return CompletableFuture con la lista de empleados o ApiException si falla.
     */
    public CompletableFuture<List<RespuestaEmpleadoDTO>> getEmpleados() {
        return executeAsync(service.listarEmpleados());
    }

    /**
     * Da de alta a un nuevo empleado de forma asíncrona.
     * La contraseña inicial es generada por el servidor y devuelta en la respuesta.
     *
     * @param dto DTO con los datos del nuevo empleado.
     * @return CompletableFuture con los datos del empleado creado y su contraseña generada.
     */
    public CompletableFuture<RespuestaCrearEmpleadoDTO> crearEmpleado(PeticionCrearEmpleadoDTO dto) {
        return executeAsync(service.crearEmpleado(dto));
    }

    /**
     * Actualiza los datos de un empleado existente de forma asíncrona.
     *
     * @param id  Identificador único del empleado a actualizar.
     * @param dto DTO con los nuevos datos del empleado.
     * @return CompletableFuture con los datos actualizados del empleado.
     */
    public CompletableFuture<RespuestaEmpleadoDTO> actualizarEmpleado(Long id, PeticionActualizarEmpleadoDTO dto) {
        return executeAsync(service.actualizarEmpleado(id, dto));
    }

    /**
     * Restablece la contraseña de un empleado de forma asíncrona.
     * Usado por RRHH cuando el empleado ha olvidado su contraseña.
     *
     * @param id  Identificador único del empleado.
     * @param dto DTO con la nueva contraseña establecida por el administrador.
     * @return CompletableFuture con los datos actualizados del empleado.
     */
    public CompletableFuture<RespuestaEmpleadoDTO> resetPassword(Long id, PeticionResetPasswordDTO dto) {
        return executeAsync(service.resetPassword(id, dto));
    }

    /**
     * Tramita la baja de un empleado de forma asíncrona estableciendo
     * la fecha de baja del contrato.
     *
     * @param id  Identificador único del empleado.
     * @param dto DTO con la fecha de baja del contrato.
     * @return CompletableFuture que se completa con null tras la baja exitosa.
     */
    public CompletableFuture<Void> darDeBaja(Long id, PeticionBajaEmpleadoDTO dto) {
        return executeAsyncVoid(service.darDeBaja(id, dto));
    }

    /**
     * Readmite a un empleado dado de baja de forma asíncrona.
     *
     * @param id Identificador único del empleado a readmitir.
     * @return CompletableFuture que se completa con null tras la readmisión exitosa.
     */
    public CompletableFuture<Void> readmitir(Long id) {
        return executeAsyncVoid(service.readmitir(id));
    }
}
