package com.example.openteseractus.tmdb;

import java.util.List;

public class TMDBResponse {

    private List<TMDBMedia> results;

    public List<TMDBMedia> getResults() {
        return results;
    }

    public void setResults(List<TMDBMedia> results) {
        this.results = results;
    }
}