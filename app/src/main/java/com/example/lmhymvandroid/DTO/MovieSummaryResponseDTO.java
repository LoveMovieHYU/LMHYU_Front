package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieSummaryResponseDTO {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("posterPath")
    private String posterPath;
    @SerializedName("rating")
    private double rating;
    @SerializedName("genres")
    private List<String> genres;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public double getRating() { return rating; }
    public List<String> getGenres() { return genres; }
}