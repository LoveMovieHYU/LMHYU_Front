package com.example.lmhymvandroid.DTO;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class MovieDetailResponse implements Serializable {

    @SerializedName("movieTitle")
    private String movieTitle;       // 영화 제목

    @SerializedName("releaseYear")
    private String releaseYear;      // 개봉 해 (예: "2025")

    @SerializedName("formattedRuntime")
    private String formattedRuntime; // 상영 시간 (예: "1h 55m")

    @SerializedName("overview")
    private String overview;         // 줄거리

    @SerializedName("rating")
    private double rating;           // 평점 (예: 4.3)

    @SerializedName("posterPath")
    private String posterPath;       // 영화 포스터 경로

    @SerializedName("directors")
    private List<PersonDTO> directors; // 감독 리스트

    @SerializedName("actors")
    private List<PersonDTO> actors;    // 배우 리스트

    @SerializedName("liked")
    private boolean isLiked;

    // ==========================================
    // Getter Methods
    // ==========================================
    public String getMovieTitle() { return movieTitle; }
    public String getReleaseYear() { return releaseYear; }
    public String getFormattedRuntime() { return formattedRuntime; }
    public String getOverview() { return overview; }
    public double getRating() { return rating; }
    public String getPosterPath() { return posterPath; }
    public List<PersonDTO> getDirectors() { return directors; }
    public List<PersonDTO> getActors() { return actors; }

    public boolean isLiked() { return isLiked; }

    // ==========================================
    // 내부 클래스: PersonDTO (백엔드 코드와 일치시킴)
    // ==========================================
    public static class PersonDTO implements Serializable {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("profileImagePath") // 백엔드 필드명과 정확히 일치
        private String profileImagePath;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getProfileImagePath() { return profileImagePath; }
    }
}