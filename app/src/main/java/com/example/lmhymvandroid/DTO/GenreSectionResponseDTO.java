package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GenreSectionResponseDTO {
    @SerializedName("genreName")
    private String genreName;
    @SerializedName("movies")
    private List<MovieSummaryResponseDTO> movies;

    public String getGenreName() { return genreName; }
    public List<MovieSummaryResponseDTO> getMovies() { return movies; }
}