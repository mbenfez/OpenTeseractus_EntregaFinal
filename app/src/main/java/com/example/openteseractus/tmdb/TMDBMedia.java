package com.example.openteseractus.tmdb;

import com.google.gson.annotations.SerializedName;

/**
 * Resultado básico de búsqueda de la API de TMDB.
 * Representa una película ({@code media_type = "movie"}) o serie ({@code media_type = "tv"})
 * tal como aparece en la respuesta de {@code /search/multi}.
 */
public class TMDBMedia {

    private int id;
    private String title;
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("first_air_date")
    private String firstAirDate;

    public TMDBMedia() {}

    public int getId() {
        return id;
    }

    public String getTitulo() {
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return name;
    }

    public String getPosterUrl() {
        if (posterPath == null) return "";
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getAnio() {
        String fecha = releaseDate != null && !releaseDate.isEmpty() ? releaseDate : firstAirDate;
        if (fecha != null && fecha.length() >= 4) return fecha.substring(0, 4);
        return "";
    }
}