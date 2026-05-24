package com.example.openteseractus.tmdb;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TMDBClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private static Retrofit retrofit;

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