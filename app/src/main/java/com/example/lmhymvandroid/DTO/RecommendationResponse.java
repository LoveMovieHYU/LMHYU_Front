package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;

public class RecommendationResponse {
    @SerializedName("movieId")
    private Long movieId;

    @SerializedName("title")
    private String title;

    @SerializedName("posterUrl")
    private String posterUrl;

    @SerializedName("reason")
    private String reason; // "슬플 때 위로가 되는..." 같은 추천 사유

    // Getter
    public Long getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterUrl() { return posterUrl; }
    public String getReason() { return reason; }

    // ================= Setter (추가됨) =================
    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}