package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.HomeResponse;
import com.example.lmhymvandroid.DTO.MovieDetailResponse;
import com.example.lmhymvandroid.DTO.MovieSearchDTO;
import com.example.lmhymvandroid.DTO.ReactionRequest;
import com.example.lmhymvandroid.DTO.RecommendationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("/api/movies/home") // 백엔드 API 엔드포인트
    Call<HomeResponse> getHomeData();

    // 기존 MovieApiService 인터페이스 안에 추가
    @GET("/api/movies/{movieId}")
    Call<MovieDetailResponse> getMovieDetail(@Path("movieId") Long movieId);

    // 1. 감정 기반 추천 영화 가져오기 (리스트 형태로 옴)
    @GET("/api/movies/recommendations")
    Call<List<RecommendationResponse>> getRecommendations(
            @Query("emotion") String emotion,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // 2. 좋아요/싫어요 반응 보내기
    @POST("/api/feedback/{movieId}/reaction")
    Call<Void> sendReaction(
            @Path("movieId") Long movieId,
            @Body ReactionRequest request
    );

    // API 명세: GET /api/movies/search?keyword={keyword}&page={page}
    @GET("/api/movies/search")
    Call<List<MovieSearchDTO>> searchMovies(
            @Query("keyword") String keyword,
            @Query("page") int page
    );
}