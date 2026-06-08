package com.example.openteseractus.tmdb;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Cliente Retrofit singleton para la API de TMDB.
 * La instancia de {@link retrofit2.Retrofit} se crea de forma perezosa la primera
 * vez que se llama a {@link #getApi()} y se reutiliza en llamadas posteriores.
 */
public class TMDBClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private static Retrofit retrofit;

    /**
     * Devuelve la implementación de {@link TMDBApi} generada por Retrofit.
     * Inicializa el cliente Retrofit la primera vez que se llama.
     *
     * @return instancia de {@link TMDBApi} lista para hacer llamadas
     */
    public static TMDBApi getApi() {

        if (retrofit == null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(TMDBApi.class);
    }
}