package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeResponseDTO {
    @SerializedName("recommendedMovie")
    private MovieSummaryResponseDTO recommendedMovie;

    @SerializedName("sections")
    private List<GenreSectionResponseDTO> sections;

    public MovieSummaryResponseDTO getRecommendedMovie() { return recommendedMovie; }
    public List<GenreSectionResponseDTO> getSections() { return sections; }
}