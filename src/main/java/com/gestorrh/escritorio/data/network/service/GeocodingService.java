package com.gestorrh.escritorio.data.network.service;

import com.gestorrh.escritorio.data.network.dto.fichaje.RespuestaGeocodingDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de geocodificación de direcciones usando la API pública de Nominatim
 * (OpenStreetMap). Encapsula la llamada HTTP externa manteniéndola fuera
 * de la capa de presentación.
 *
 * @author Fco Javier García Cañero
 * @version 1.0
 */
public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=";
    private static final String USER_AGENT = "GestorRH-Escritorio/1.0";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Geocodifica una dirección textual y devuelve sus coordenadas.
     *
     * @param query Dirección textual a geocodificar.
     * @return CompletableFuture con el resultado o excepción si no se encuentra.
     */
    public CompletableFuture<RespuestaGeocodingDTO> geocodificar(String query) {
        String url = NOMINATIM_URL + URLEncoder.encode(query, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "es")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();
                    if (results.isEmpty()) {
                        throw new RuntimeException("configuracion.error.geocoding");
                    }
                    double lat = results.get(0).getAsJsonObject().get("lat").getAsDouble();
                    double lon = results.get(0).getAsJsonObject().get("lon").getAsDouble();
                    return new RespuestaGeocodingDTO(lat, lon);
                });
    }
}
