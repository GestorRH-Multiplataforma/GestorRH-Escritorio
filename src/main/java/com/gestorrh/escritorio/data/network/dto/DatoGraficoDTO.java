package com.gestorrh.escritorio.data.network.dto;

/**
 * DTO universal para representar un dato de gráfico con etiqueta y valor numérico.
 * Usado por los endpoints de estadísticas que devuelven rankings y distribuciones.
 *
 * @param etiqueta Texto descriptivo del eje X (ej. nombre del empleado, departamento).
 * @param valor    Valor numérico del eje Y (ej. número de retrasos, cantidad de ausencias).
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public record DatoGraficoDTO(String etiqueta, Number valor) {}
