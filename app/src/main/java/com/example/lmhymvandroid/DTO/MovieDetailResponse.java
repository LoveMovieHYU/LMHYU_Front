package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class MovieDetailResponse {
    @SerializedName("title")
    private String title;

    @SerializedName("genre")
    private String genre;

    @SerializedName("releaseDate")
    private String releaseDate;

    @SerializedName("plot")
    private String plot;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("avgRating")
    private double avgRating;

    // Getter 메서드들
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getReleaseDate() { return releaseDate; }
    public String getPlot() { return plot; }
    public String getPosterUrl() { return posterUrl; }
    public double getAvgRating() { return avgRating; }
}