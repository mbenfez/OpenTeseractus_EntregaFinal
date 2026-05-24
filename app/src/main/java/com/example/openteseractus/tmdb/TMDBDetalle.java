package com.example.openteseractus.tmdb;

import com.google.gson.annotations.SerializedName;

public class TMDBDetalle {

    private int id;

    private String title;
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    private String overview;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("first_air_date")
    private String firstAirDate;

    private int runtime;

    @SerializedName("number_of_seasons")
    private int numberOfSeasons;

    @SerializedName("number_of_episodes")
    private int numberOfEpisodes;

    @SerializedName("vote_average")
    private double voteAverage;

    private String mediaType;

    public TMDBDetalle() {}

    public int getId() { return id; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getTitulo() {
        if (title != null && !title.isEmpty()) {
            return title;
        }

        return name;
    }

    public String getPosterUrl() {
        if (posterPath == null) return null;
        return "https://image.tmdb.org/t/p/w500" + posterPath;
    }

    public String getBackdropUrl() {
        if (backdropPath == null) return null;
        return "https://image.tmdb.org/t/p/original" + backdropPath;
    }

    public String getSinopsis() { return overview; }

    public String getFechaSalida() {
        String fecha;

        if (releaseDate != null && !releaseDate.isEmpty()) {
            fecha = releaseDate;
        } else {
            fecha = firstAirDate;
        }

        if (fecha == null || fecha.length() < 4) {
            return "";
        }

        return fecha.substring(0, 4);
    }

    public int getRuntime() { return runtime; }

    public int getTemporadas() { return numberOfSeasons; }

    public int getEpisodios() { return numberOfEpisodes; }

    public double getVoteAverage() { return voteAverage; }

    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }
}