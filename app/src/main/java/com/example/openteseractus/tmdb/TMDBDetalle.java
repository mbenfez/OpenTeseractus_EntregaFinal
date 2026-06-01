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

    @SerializedName("credits")
    private Credits credits;

    @SerializedName("created_by")
    private java.util.List<Creator> createdBy;

    private String mediaType;

    public static class Credits {
        private java.util.List<CrewMember> crew = new java.util.ArrayList<>();
        public java.util.List<CrewMember> getCrew() { return crew; }
    }

    public static class CrewMember {
        private String job;
        private String name;
        public String getJob() { return job; }
        public String getName() { return name; }
    }

    public static class Creator {
        private String name;
        public String getName() { return name; }
    }

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

    public String getDirectorOCreador() {
        if ("movie".equals(mediaType)) {
            if (credits != null && credits.getCrew() != null) {
                for (CrewMember m : credits.getCrew()) {
                    if ("Director".equals(m.getJob())) return m.getName();
                }
            }
        } else if ("tv".equals(mediaType)) {
            if (createdBy != null && !createdBy.isEmpty()) {
                return createdBy.get(0).getName();
            }
        }
        return null;
    }
}