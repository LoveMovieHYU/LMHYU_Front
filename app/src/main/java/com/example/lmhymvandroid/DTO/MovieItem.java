package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class MovieItem implements Serializable {
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

    public MovieItem(int movieId, String title, String posterUrl, double rating, String releaseYear, List<String> genres) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.rating = rating;
        this.releaseYear = releaseYear;
        this.genres = genres;
    }
}