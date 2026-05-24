package com.example.openteseractus.tmdb;

import com.google.gson.annotations.SerializedName;

public class TMDBMedia {

    private int id;
    private String title;
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("media_type")
    private String mediaType;

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
}