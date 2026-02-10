package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.BiorhythmResponse;
import com.example.lmhymvandroid.DTO.HomeResponse;
import com.example.lmhymvandroid.DTO.MovieItem;
import com.example.lmhymvandroid.DTO.MovieRecommendationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieService {
    @GET("/api/movies/home")
    Call<HomeResponse> getHomeData();

    @GET("/api/recommend/biorhythm/check")
    Call<BiorhythmResponse> getBiorhythmAnalyze();

    @GET("/api/recommend/list")
    Call<List<MovieItem>>getRecommendedMovies();

    // 예시: /api/recommend/custom/list?p=50.0&e=-20.0&i=10.0
    @GET("/api/recommend/custom/list")
    Call<MovieRecommendationResponse> getCustomRecommendations(
            @Query("p") float physical,    // 신체 지수 (Green)
            @Query("e") float emotional,   // 감정 지수 (Pink)
            @Query("i") float intellectual // 지성 지수 (Blue)
    );

    // [신규 추가] 영화 검색 API
    // 예시: /api/movies/search?keyword=인셉션&page=1
    @GET("/api/movies/search")
    Call<MovieRecommendationResponse> searchMovies(
            @Query("keyword") String keyword,
            @Query("page") int page
    );
}