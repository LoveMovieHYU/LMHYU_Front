package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieItem {
    @SerializedName("movieId")
    private int movieId;

    @SerializedName("title")
    private String title;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("rating")
    private double rating;

    @SerializedName("releaseYear")
    private String releaseYear;

    @SerializedName("genres")
    private List<String> genres;

    // Getters
    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public double getRating() { return rating; }
    public String getReleaseYear() { return releaseYear; }
    public List<String> getGenres() { return genres; }
}