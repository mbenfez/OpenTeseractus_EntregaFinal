package com.example.openteseractus.tmdb;

import android.util.Log;

import com.example.openteseractus.callbacks.FirestoreCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repositorio que centraliza las llamadas a la API de TMDB usando Retrofit.
 * Filtra los resultados de búsqueda para devolver únicamente películas y series,
 * descartando personas u otros tipos de media.
 */
public class TMDBRepository {

    private static final String TAG = "TMDBRepository";
    private static final String API_KEY = "938d586e408125af4c9a5338a3ba1860";
    private TMDBApi api;

    public TMDBRepository() {
        api = TMDBClient.getApi();
    }

    public void buscar(String query, FirestoreCallback<List<TMDBMedia>> callback) {
        api.buscarContenido(API_KEY, query, "es-ES")
                .enqueue(new Callback<TMDBResponse>() {

                    @Override
                    public void onResponse(Call<TMDBResponse> call, Response<TMDBResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<TMDBMedia> filtrados = new ArrayList<>();

                            for (TMDBMedia item : response.body().getResults()) {
                                if ("movie".equals(item.getMediaType()) || "tv".equals(item.getMediaType())) {
                                    filtrados.add(item);
                                }
                            }

                            callback.onSuccess(filtrados);
                        } else {
                            callback.onFailure("Error TMDB");
                        }
                    }

                    @Override
                    public void onFailure(Call<TMDBResponse> call, Throwable t) {
                        Log.e(TAG, "Error TMDB", t);
                        callback.onFailure(t.getMessage());
                    }
                });
    }

    public void obtenerDetalle(int tmdbId, String mediaType, FirestoreCallback<TMDBDetalle> callback) {
        Call<TMDBDetalle> call;

        if ("movie".equals(mediaType)) {
            call = api.obtenerPelicula(tmdbId, API_KEY, "es-ES", "credits");
        } else {
            call = api.obtenerSerie(tmdbId, API_KEY, "es-ES", "credits");
        }

        call.enqueue(new Callback<TMDBDetalle>() {
            @Override
            public void onResponse(Call<TMDBDetalle> call, Response<TMDBDetalle> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TMDBDetalle detalle = response.body();
                    detalle.setMediaType(mediaType);
                    callback.onSuccess(detalle);
                } else {
                    callback.onFailure("Error TMDB");
                }
            }

            @Override
            public void onFailure(Call<TMDBDetalle> call, Throwable t) {
                Log.e(TAG, "Error detalle", t);
                callback.onFailure(t.getMessage());
            }
        });
    }
}