package com.example.lmhymvandroid.DTO;

import java.util.List;

// HomeResponse.java
public class HomeResponse {
    public MovieSummaryResponse recommendedMovie;
    public List<GenreSectionResponse> sections;

    public static class MovieSummaryResponse {
        public int id;
        public String title;
        public String posterPath; // <--- 이 경로를 사용할 겁니다!
        public double rating;
    }

    public static class GenreSectionResponse {
        private String genreName; // "액션 인기 영화"
        private List<MovieSummaryResponse> movies; // 해당 장르의 영화들 (가로 리스트용)
    }
}