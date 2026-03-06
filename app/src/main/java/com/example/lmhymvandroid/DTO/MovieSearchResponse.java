package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class MovieSearchResponse {
    @SerializedName("tmdbId")
    private int movieId;

    @SerializedName("title")
    private String title;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("releaseDate")
    private String releaseDate; // 백엔드 LocalDate -> JSON String ("2025-01-01")

    // Getter
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public String getReleaseDate() { return releaseDate; }
}