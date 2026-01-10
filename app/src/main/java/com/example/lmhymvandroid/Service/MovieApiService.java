package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.HomeResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MovieApiService {
    @GET("/api/movies/home") // 백엔드 API 엔드포인트
    Call<HomeResponse> getHomeData();
}