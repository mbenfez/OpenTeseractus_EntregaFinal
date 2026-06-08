package com.example.openteseractus.tmdb;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interfaz Retrofit para la API pública de The Movie Database (TMDB) v3.
 * Cada método corresponde a un endpoint REST y es consumido por {@link TMDBRepository}.
 */
public interface TMDBApi {

    /**
     * Realiza una búsqueda multi-media (películas y series) por texto libre.
     *
     * @param apiKey   clave de la API de TMDB
     * @param query    texto a buscar
     * @param language código de idioma BCP-47 (p.ej. {@code "es-ES"})
     * @return llamada asíncrona con los resultados paginados
     */
    @GET("search/multi")
    Call<TMDBResponse> buscarContenido(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language
    );

    /**
     * Obtiene los detalles completos de una película incluyendo créditos.
     *
     * @param id               identificador de TMDB de la película
     * @param apiKey           clave de la API
     * @param language         código de idioma BCP-47
     * @param appendToResponse datos adicionales a incluir (p.ej. {@code "credits"})
     * @return llamada asíncrona con el detalle de la película
     */
    @GET("movie/{id}")
    Call<TMDBDetalle> obtenerPelicula(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("append_to_response") String appendToResponse
    );

    /**
     * Obtiene los detalles completos de una serie incluyendo créditos y creadores.
     *
     * @param id               identificador de TMDB de la serie
     * @param apiKey           clave de la API
     * @param language         código de idioma BCP-47
     * @param appendToResponse datos adicionales a incluir (p.ej. {@code "credits"})
     * @return llamada asíncrona con el detalle de la serie
     */
    @GET("tv/{id}")
    Call<TMDBDetalle> obtenerSerie(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("append_to_response") String appendToResponse
    );
}