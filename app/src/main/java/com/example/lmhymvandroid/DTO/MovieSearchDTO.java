package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class MovieSearchDTO {
    @SerializedName("movieId")
    private Long movieId;

    @SerializedName("title")
    private String title;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("releaseDate")
    private String releaseDate;

    // Getter & Setter (또는 생성자)
    public Long getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public String getReleaseDate() { return releaseDate; }
}