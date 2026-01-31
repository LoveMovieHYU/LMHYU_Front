package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieRecommendationResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("movieList")
    private List<MovieItem> movieList; // 영화 리스트

    @SerializedName("cached")
    private boolean cached;

    // Getters
    public String getMessage() { return message; }
    public List<MovieItem> getMovieList() { return movieList; }
    public boolean isCached() { return cached; }
}