package com.example.openteseractus.tmdb;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TMDBApi {

    @GET("search/multi")
    Call<TMDBResponse> buscarContenido(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("language") String language
    );

    @GET("movie/{id}")
    Call<TMDBDetalle> obtenerPelicula(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("append_to_response") String appendToResponse
    );

    @GET("tv/{id}")
    Call<TMDBDetalle> obtenerSerie(
            @Path("id") int id,
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("append_to_response") String appendToResponse
    );
}