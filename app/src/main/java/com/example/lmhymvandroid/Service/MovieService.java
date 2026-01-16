package com.example.lmhymvandroid.Service;

import com.example.lmhymvandroid.DTO.HomeResponseDTO;
import retrofit2.Call;
import retrofit2.http.GET;

public interface MovieService {
    @GET("/api/movies/home")
    Call<HomeResponseDTO> getHomeData();
}