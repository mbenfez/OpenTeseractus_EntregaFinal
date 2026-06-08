package com.example.openteseractus.tmdb;

import java.util.List;

/**
 * Respuesta paginada de la API de TMDB para búsquedas multi-media.
 * Solo se utiliza el campo {@code results}; el resto de campos de paginación
 * se ignoran ya que la aplicación no implementa carga bajo demanda.
 */
public class TMDBResponse {

    private List<TMDBMedia> results;

    public List<TMDBMedia> getResults() {
        return results;
    }

    public void setResults(List<TMDBMedia> results) {
        this.results = results;
    }
}