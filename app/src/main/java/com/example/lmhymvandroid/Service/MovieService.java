package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.BiorhythmResponse;
import com.example.lmhymvandroid.DTO.HomeResponse;
import com.example.lmhymvandroid.DTO.MovieRecommendationResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MovieService {
    @GET("/api/movies/home")
    Call<HomeResponse> getHomeData();

    @GET("/api/recommend/biorhythm/analyze")
    Call<BiorhythmResponse> getBiorhythmAnalyze();

    @GET("/api/recommend/biorhythm/check")
    Call<MovieRecommendationResponse> getRecommendedMovies();
}